package chappie.theboys.mixin.client;

import chappie.theboys.common.capability.TheBoysCap;
import chappie.theboys.util.interfaces.ISetupGameProfiles;
import chappie.theboys.util.TBClientUtil;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.server.Services;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.io.File;

@Mixin(Minecraft.class)
public class MinecraftMixin implements ISetupGameProfiles {
    @Shadow @Final private YggdrasilAuthenticationService authenticationService;

    @Shadow @Final public File gameDirectory;

    @Override
    public void theBoys$setup() {
        Minecraft mc = (Minecraft) (Object) this;
        Services services = Services.create(this.authenticationService, this.gameDirectory);
        services.profileCache().setExecutor(mc);
        SkullBlockEntity.setup(services, mc);
        GameProfileCache.setUsesAuthentication(false);
    }

    @WrapOperation(method = "handleKeybinds()V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/KeyMapping;consumeClick()Z", ordinal = 2))
    private boolean cancelSwapSlots(KeyMapping instance, Operation<Boolean> original) {
        TheBoysCap cap = TheBoysCap.getCap(Minecraft.getInstance().player);
        if (cap != null && cap.vialAnim.rollVial.value(1) > 0) {
            return false;
        } else {
            return original.call(instance);
        }
    }
}
