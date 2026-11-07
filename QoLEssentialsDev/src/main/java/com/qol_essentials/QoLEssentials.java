package com.qolessentials.qol_essentials;

import com.mojang.logging.LogUtils;
import com.qolessentials.qol_essentials.config.QoLServerConfig;
import com.qolessentials.qol_essentials.feature.building.harvestreplant.HarvestReplantFeature;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

/** Main entrypoint for QoL Essentials. */
@Mod(QoLEssentials.MOD_ID)
public final class QoLEssentials {
    public static final String MOD_ID = "qol_essentials";
    public static final Logger LOGGER = LogUtils.getLogger();

    public QoLEssentials(ModContainer modContainer) {
        // Gameplay rules are stored in the world/server configuration.
        modContainer.registerConfig(ModConfig.Type.SERVER, QoLServerConfig.SPEC);

        // Gameplay events are posted to the NeoForge game bus.
        NeoForge.EVENT_BUS.addListener(HarvestReplantFeature::onRightClickBlock);

        LOGGER.info("QoL Essentials initialized.");
    }
}
