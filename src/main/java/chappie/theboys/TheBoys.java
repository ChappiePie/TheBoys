package chappie.theboys;

import chappie.theboys.common.ability.base.TBAbilityTypes;
import chappie.theboys.common.ability.base.TBSuperpowers;
import chappie.theboys.common.block.TBBlocks;
import chappie.theboys.common.block.entity.TBBlockEntities;
import chappie.theboys.common.block.menu.TBMenus;
import chappie.theboys.common.entity.TBEntities;
import chappie.theboys.common.item.TBItems;
import chappie.theboys.common.item.datacomponents.TBDataComponents;
import chappie.theboys.common.particle.TBParticleTypes;
import chappie.theboys.networking.TBNetworking;
import chappie.theboys.util.TBConfig;
import chappie.theboys.util.tooltip.ArmorTooltip;
import chappie.theboys.util.tooltip.ClientArmorTooltip;
import chappie.theboys.util.tooltip.ClientSuperpowerTooltip;
import chappie.theboys.util.tooltip.SuperpowerTooltip;
import fuzs.forgeconfigapiport.fabric.api.forge.v4.ForgeConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TheBoys implements ModInitializer {
    public static final String MODID = "theboys";

    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public static ResourceLocation id(String id) {
        return ResourceLocation.fromNamespaceAndPath(MODID, id);
    }

    @Override
    public void onInitialize() {
        TBItems.init();
        TBAbilityTypes.init();
        TBSuperpowers.init();
        TBEntities.init();
        TBParticleTypes.init();
        TBDataComponents.init();

        TBBlocks.init();
        TBBlockEntities.init();
        TBMenus.init();

        ForgeConfigRegistry.INSTANCE.register(MODID, ModConfig.Type.CLIENT, TBConfig.CLIENT_SPEC);
        ForgeConfigRegistry.INSTANCE.register(MODID, ModConfig.Type.COMMON, TBConfig.COMMON_SPEC);

        TBNetworking.registerMessages();
        TooltipComponentCallback.EVENT.register(tooltip -> {
            if (tooltip instanceof ArmorTooltip) {
                return new ClientArmorTooltip((ArmorTooltip) tooltip);
            }
            if (tooltip instanceof SuperpowerTooltip) {
                return new ClientSuperpowerTooltip((SuperpowerTooltip) tooltip);
            }
            return null;
        });
    }
}
