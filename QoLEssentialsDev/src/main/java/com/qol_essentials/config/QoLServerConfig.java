package com.qolessentials.qol_essentials.config;

import org.apache.commons.lang3.tuple.Pair;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Server-authoritative settings for gameplay-affecting QoL features. */
public final class QoLServerConfig {
    public static final QoLServerConfig CONFIG;
    public static final ModConfigSpec SPEC;

    public final ModConfigSpec.BooleanValue HARVEST_REPLANT_ENABLED;
    public final ModConfigSpec.BooleanValue HARVEST_REPLANT_ALLOW_EMPTY_HAND;
    public final ModConfigSpec.BooleanValue HARVEST_REPLANT_ALLOW_HOE;

    static {
        Pair<QoLServerConfig, ModConfigSpec> pair =
                new ModConfigSpec.Builder().configure(QoLServerConfig::new);
        CONFIG = pair.getLeft();
        SPEC = pair.getRight();
    }

    private QoLServerConfig(ModConfigSpec.Builder builder) {
        builder.push("building");

        HARVEST_REPLANT_ENABLED = builder
                .comment("Enable right-click harvesting and replanting for supported vanilla crops.")
                .define("harvest_replant_enabled", true);

        HARVEST_REPLANT_ALLOW_EMPTY_HAND = builder
                .comment("Allow Harvest & Replant when the interacting hand is empty.")
                .define("harvest_replant_allow_empty_hand", true);

        HARVEST_REPLANT_ALLOW_HOE = builder
                .comment("Allow Harvest & Replant when the interacting hand holds a hoe.")
                .define("harvest_replant_allow_hoe", true);

        builder.pop();
    }

    private QoLServerConfig() {
        throw new IllegalStateException("Config class should not be instantiated directly.");
    }
}
