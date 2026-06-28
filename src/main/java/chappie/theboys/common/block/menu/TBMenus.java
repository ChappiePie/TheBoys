package chappie.theboys.common.block.menu;

import chappie.theboys.TheBoys;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class TBMenus {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, TheBoys.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<SynthesizerMenu>> SYNTHESIZER = MENU_TYPES.register("synthesizer",
            () -> new MenuType<>(SynthesizerMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static void init(IEventBus modEventBus) {
        MENU_TYPES.register(modEventBus);
    }
}
