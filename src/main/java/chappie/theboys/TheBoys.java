package chappie.theboys;

import chappie.modulus.client.gui.ChappModListWidget;
import chappie.theboys.client.gui.EyeOptionsScreen;
import chappie.theboys.common.ability.base.TBAbilityTypes;
import chappie.theboys.common.ability.base.TBSuperpowers;
import chappie.theboys.common.entity.TBEntities;
import chappie.theboys.common.item.TBItems;
import chappie.theboys.common.particle.TBParticleTypes;
import chappie.theboys.networking.TBNetworking;
import chappie.theboys.util.TBConfig;
import chappie.theboys.util.tooltip.ArmorTooltip;
import chappie.theboys.util.tooltip.ClientArmorTooltip;
import fuzs.forgeconfigapiport.fabric.api.forge.v4.ForgeConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TheBoys implements ModInitializer {
    public static final String MODID = "theboys";

    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    static {
        ChappModListWidget.MOD_CLICKED.put(MODID, (e) -> {
            Minecraft.getInstance().setScreen(new EyeOptionsScreen(e.parent));
        });
    }

    public static ResourceLocation id(String id) {
        return new ResourceLocation(MODID, id);
    }

    @Override
    public void onInitialize() {
        TBItems.init();
        TBAbilityTypes.init();
        TBSuperpowers.init();
        TBEntities.init();
        TBParticleTypes.init();

        ForgeConfigRegistry.INSTANCE.register(MODID, ModConfig.Type.CLIENT, TBConfig.CLIENT_SPEC);
        ForgeConfigRegistry.INSTANCE.register(MODID, ModConfig.Type.COMMON, TBConfig.COMMON_SPEC);

        TBNetworking.registerMessages();
        TooltipComponentCallback.EVENT.register(tooltip -> {
            if (tooltip instanceof ArmorTooltip) {
                return new ClientArmorTooltip((ArmorTooltip) tooltip);
            }
            return null;
        });
    }
}
