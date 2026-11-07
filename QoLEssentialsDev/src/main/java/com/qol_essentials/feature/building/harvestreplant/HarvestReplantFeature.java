package com.qolessentials.qol_essentials.feature.building.harvestreplant;

import com.qolessentials.qol_essentials.config.QoLServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.List;
import java.util.Map;

/**
 * Right-click harvesting for a deliberately small, known set of vanilla crops.
 * All inventory and world mutations happen on the logical server.
 */
public final class HarvestReplantFeature {
    private static final int MAIN_INVENTORY_SIZE = 36;

    private static final Map<Block, Item> REPLANT_ITEMS = Map.of(
            Blocks.WHEAT, Items.WHEAT_SEEDS,
            Blocks.CARROTS, Items.CARROT,
            Blocks.POTATOES, Items.POTATO,
            Blocks.BEETROOTS, Items.BEETROOT_SEEDS
    );

    private HarvestReplantFeature() {
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        QoLServerConfig config = QoLServerConfig.CONFIG;
        if (!config.HARVEST_REPLANT_ENABLED.get() || player.isShiftKeyDown()) {
            return;
        }

        ItemStack heldStack = event.getItemStack();
        boolean emptyHandAllowed = config.HARVEST_REPLANT_ALLOW_EMPTY_HAND.get();
        boolean hoeAllowed = config.HARVEST_REPLANT_ALLOW_HOE.get();
        boolean usingEmptyHand = heldStack.isEmpty();
        boolean usingHoe = !heldStack.isEmpty() && heldStack.getItem() instanceof HoeItem;

        if (!(usingEmptyHand && emptyHandAllowed) && !(usingHoe && hoeAllowed)) {
            return;
        }

        BlockPos pos = event.getPos();
        BlockState cropState = level.getBlockState(pos);
        if (!(cropState.getBlock() instanceof CropBlock cropBlock)
                || !cropBlock.isMaxAge(cropState)) {
            return;
        }

        Item seedItem = REPLANT_ITEMS.get(cropState.getBlock());
        if (seedItem == null) {
            return;
        }

        Inventory inventory = player.getInventory();
        int seedSlot = player.getAbilities().instabuild
                ? -1
                : findSeedSlot(inventory, seedItem);

        // Survival players need one matching seed. Creative players do not.
        if (!player.getAbilities().instabuild && seedSlot < 0) {
            return;
        }

        // Calculate drops from the mature state before changing the block.
        List<ItemStack> drops = Block.getDrops(
                cropState,
                level,
                pos,
                level.getBlockEntity(pos),
                player,
                heldStack
        );

        BlockState replantedState = cropBlock.getStateForAge(0);
        if (!level.setBlock(pos, replantedState, Block.UPDATE_ALL)) {
            return;
        }

        if (seedSlot >= 0) {
            inventory.getItem(seedSlot).shrink(1);
            inventory.setChanged();
        }

        for (ItemStack drop : drops) {
            if (!drop.isEmpty()) {
                Block.popResource(level, pos, drop);
            }
        }

        // Stop vanilla's block-use pipeline only after the server completed the action.
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    private static int findSeedSlot(Inventory inventory, Item seedItem) {
        for (int slot = 0; slot < MAIN_INVENTORY_SIZE; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.isEmpty() && stack.is(seedItem)) {
                return slot;
            }
        }
        return -1;
    }
}
