package chappie.theboys.mixin.client;

import chappie.theboys.TheBoys;
import chappie.theboys.client.item.SyringeHasVialProperty;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SelectItemModelProperties.class)
public class SelectItemModelPropertiesMixin {

    @Inject(method = "bootstrap", at = @At("TAIL"))
    private static void bootstrap(CallbackInfo ci) {
        SelectItemModelProperties.ID_MAPPER.put(TheBoys.id("has_vial"), SyringeHasVialProperty.TYPE);
    }

}
