package chappie.theboys.mixin.client;

import chappie.theboys.util.TBClientUtil;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ModelBakery.class)
public abstract class ModelBakeryMixin {

    @Shadow
    protected abstract void loadTopLevel(ModelResourceLocation location);

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/ModelBakery;loadTopLevel(Lnet/minecraft/client/resources/model/ModelResourceLocation;)V", ordinal = 3), method = "<init>")
    private void registerSmth(BlockColors blockColors, ProfilerFiller profilerFiller, Map modelResources, Map blockStateResources, CallbackInfo ci) {
        this.loadTopLevel(TBClientUtil.SYRINGE_MODEL);
        this.loadTopLevel(TBClientUtil.SYRINGE_3D_MODEL);

        this.loadTopLevel(TBClientUtil.VIAL_MODEL);
        this.loadTopLevel(TBClientUtil.VIAL_3D_MODEL);
    }
}