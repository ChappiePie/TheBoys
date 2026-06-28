package chappie.theboys.util;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class TBConfig {
    public static Client CLIENT;
    public static ModConfigSpec CLIENT_SPEC, COMMON_SPEC;

    public static Common COMMON;

    static {
        Pair<Client, ModConfigSpec> specClientPair = new ModConfigSpec.Builder().configure(Client::new);
        CLIENT_SPEC = specClientPair.getRight();
        CLIENT = specClientPair.getLeft();

        Pair<Common, ModConfigSpec> specCommonPair = new ModConfigSpec.Builder().configure(Common::new);
        COMMON_SPEC = specCommonPair.getRight();
        COMMON = specCommonPair.getLeft();
    }

    public static class Client {

        public final ModConfigSpec.BooleanValue eyesOverlay;
        public final ModConfigSpec.BooleanValue heatVisionHardcored;
        public final ModConfigSpec.BooleanValue abilitiesOverlayToggle;

        public final ModConfigSpec.IntValue eyesType;
        public final ModConfigSpec.IntValue eyesHeight, eyesHeight2;
        public final ModConfigSpec.IntValue eyesLength, eyesLength2;

        Client(ModConfigSpec.Builder builder) {

            builder.comment("Client Settings").push("client");
            this.abilitiesOverlayToggle = builder.comment("Should abilities overlay key be toggleable").translation("config.theboys.abilitiesOverlayToggle").define("abilitiesOverlayToggle", false);
            this.heatVisionHardcored = builder.comment("Should heat vision be complicated in rendering").translation("config.theboys.heatVisionHardcored").define("heatVisionHardcored", false);
            this.eyesOverlay = builder.comment("Should heat vision eyes overlay be shown in first person").translation("config.theboys.eyesOverlay").define("eyesOverlay", true);

            builder.push("eyes");
            {
                this.eyesType = builder.comment("Eyes type, from 0 to 5", "0 - nothing, 1 - default, 2 - low, 3 - double, 4 and 5 - custom").translation("config.theboys.eyesType").defineInRange("eyesType", 0, 0, 5);

                builder.push("custom_1");
                this.eyesHeight = builder.comment("Lasers from eyes height").translation("config.theboys.eyesHeight").defineInRange("eyesHeight", 5, 1, 8);
                this.eyesLength = builder.comment("Length of lasers").translation("config.theboys.eyesLength").defineInRange("eyesLength", 1, 1, 8);
                builder.pop();

                builder.push("custom_2");
                this.eyesHeight2 = builder.comment("Lasers from eyes height").translation("config.theboys.eyesHeight").defineInRange("eyesHeight", 5, 1, 8);
                this.eyesLength2 = builder.comment("Length of lasers").translation("config.theboys.eyesLength").defineInRange("eyesLength", 1, 1, 8);
                builder.pop();
            }
            builder.pop();

            builder.pop();
        }

    }

    public static class Common {

        public final ModConfigSpec.DoubleValue suitOpacity;
        public final ModConfigSpec.BooleanValue storeAbilities;
        public final ModConfigSpec.BooleanValue chatForMuted;

        public final ModConfigSpec.DoubleValue heatVisionDamage;
        public final ModConfigSpec.DoubleValue heatVisionRange;

        Common(ModConfigSpec.Builder builder) {
            builder.comment("Common Settings").push("common");
            this.suitOpacity = builder.comment("Change suit opacity on armor").translation("config.theboys.suitOpacity").defineInRange("suitOpacity", 1.0D, 0.0D, 1.0D);
            this.storeAbilities = builder.comment("Store abilities").translation("config.theboys.storeAbilities").define("storeAbilities", false);
            this.chatForMuted = builder.comment("Chat for muted").translation("config.theboys.chatForMuted").define("chatForMuted", true);

            this.heatVisionDamage = builder.comment("Heat vision damage against entities").translation("config.theboys.heatVisionDamage").defineInRange("heatVisionDamage", 2.0D, 0.0D, 100.0D);
            this.heatVisionRange = builder.comment("Heat vision maximum range").translation("config.theboys.heatVisionRange").defineInRange("heatVisionRange", 20.0D, 0.0D, 256.0D);
            builder.pop();
        }

    }
}