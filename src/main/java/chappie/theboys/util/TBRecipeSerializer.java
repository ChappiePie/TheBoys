package chappie.theboys.util;

import chappie.theboys.TheBoys;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class TBRecipeSerializer {

    public static final DeferredRegister<IRecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, TheBoys.MODID);

    public static final SpecialRecipeSerializer<InjectionVRecipe> INJECTION_V = register("crafting_special_injection_v", new SpecialRecipeSerializer<>(InjectionVRecipe::new));

    private static <T extends SpecialRecipeSerializer<?>> T register(String name, T recipe) {
        RECIPE_SERIALIZERS.register(name, () -> recipe);
        return recipe;
    }
}