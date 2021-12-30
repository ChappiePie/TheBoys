package chappie.theboys.util;

import chappie.theboys.TheBoys;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class TBRecipeSerializer {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, TheBoys.MODID);

    public static final SimpleRecipeSerializer<InjectionVRecipe> INJECTION_V = register("crafting_special_injection_v", new SimpleRecipeSerializer<>(InjectionVRecipe::new));

    private static <T extends SimpleRecipeSerializer<?>> T register(String name, T recipe) {
        RECIPE_SERIALIZERS.register(name, () -> recipe);
        return recipe;
    }
}