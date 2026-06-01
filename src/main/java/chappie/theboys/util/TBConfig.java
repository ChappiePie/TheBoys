package chappie.theboys.util;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class TBConfig {
    public static Client CLIENT;
    public static ForgeConfigSpec CLIENT_SPEC, COMMON_SPEC;

    public static Common COMMON;

    static {
        Pair<Client, ForgeConfigSpec> specClientPair = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT_SPEC = specClientPair.getRight();
        CLIENT = specClientPair.getLeft();

        Pair<Common, ForgeConfigSpec> specCommonPair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON_SPEC = specCommonPair.getRight();
        COMMON = specCommonPair.getLeft();
    }

    public static class Client {

        public final ForgeConfigSpec.BooleanValue eyesOverlay;
        public final ForgeConfigSpec.BooleanValue heatVisionHardcored;
        public final ForgeConfigSpec.BooleanValue abilitiesOverlayToggle;

        public final ForgeConfigSpec.IntValue eyesType;
        public final ForgeConfigSpec.IntValue eyesHeight, eyesHeight2;
        public final ForgeConfigSpec.IntValue eyesLength, eyesLength2;
        
        Client(ForgeConfigSpec.Builder builder) {

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

        public final ForgeConfigSpec.DoubleValue suitOpacity;
        public final ForgeConfigSpec.BooleanValue storeAbilities;
        public final ForgeConfigSpec.BooleanValue chatForMuted;

        public final ForgeConfigSpec.DoubleValue heatVisionDamage;
        public final ForgeConfigSpec.DoubleValue heatVisionRange;

        Common(ForgeConfigSpec.Builder builder) {
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