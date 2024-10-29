package chappie.theboys.common.block.menu;

import chappie.theboys.TheBoys;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public class TBMenus {

    private static <T extends AbstractContainerMenu> MenuType<T> register(String name, MenuType.MenuSupplier<T> block) {
        return Registry.register(BuiltInRegistries.MENU, TheBoys.id(name), new MenuType<>(block, FeatureFlags.DEFAULT_FLAGS));
    }

    public static void init() {

    }

    public static final MenuType<SynthesizerMenu> SYNTHESIZER = register("synthesizer", SynthesizerMenu::new);




}
