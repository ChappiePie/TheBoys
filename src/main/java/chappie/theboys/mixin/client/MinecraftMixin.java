package chappie.theboys.mixin.client;

import chappie.modulus.util.ClientUtil;
import chappie.modulus.util.CommonUtil;
import chappie.theboys.common.ability.SuperHearingAbility;
import chappie.theboys.common.capability.TBEntityCap;
import chappie.theboys.common.capability.TheBoysCap;
import chappie.theboys.util.interfaces.ISetupGameProfiles;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMixin implements ISetupGameProfiles {
    @Unique
    private boolean theBoys$initialized = false;

    @Inject(method = "shouldEntityAppearGlowing(Lnet/minecraft/world/entity/Entity;)Z", at = @At("RETURN"), cancellable = true)
    public void mixin$isCurrentlyGlowing(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        for (SuperHearingAbility a : CommonUtil.listOfType(SuperHearingAbility.class, CommonUtil.getAbilities(Minecraft.getInstance().getCameraEntity()))) {
            if (a.isEnabled()) {
                if (entity != null && entity.isAlive() && !cir.getReturnValue()) {
                    TBEntityCap cap = TBEntityCap.getCap(entity);
                    if (cap != null) {
                        cir.setReturnValue(cap.isGlowing());
                    }
                }
            }
        }
    }

    @Override
    public void theBoys$setup() {
        if (!this.theBoys$initialized) {
            this.theBoys$initialized = true;
        }
    }

    @WrapOperation(method = "handleKeybinds()V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/KeyMapping;consumeClick()Z", ordinal = 2))
    private boolean cancelSwapSlots(KeyMapping instance, Operation<Boolean> original) {
        TheBoysCap cap = TheBoysCap.getCap(Minecraft.getInstance().player);
        if (cap != null && cap.vialAnim.timeline.value(ClientUtil.getPartialTick()) > 0) {
            return false;
        } else {
            return original.call(instance);
        }
    }
}
