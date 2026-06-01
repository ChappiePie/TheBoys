package chappie.theboys.mixin.client;

import chappie.modulus.util.CommonUtil;
import chappie.theboys.common.ability.NodHeadAbility;
import chappie.theboys.util.TBConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {

    @Shadow
    protected EditBox input;

    @Inject(method = "init()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/EditBox;setCanLoseFocus(Z)V"))
    private void mixin$changingFilter(CallbackInfo ci) {
        this.input.setFilter((str) -> {
            var player = Minecraft.getInstance().player;

            if (player != null && !TBConfig.COMMON.chatForMuted.get()
                    && !CommonUtil.listOfType(NodHeadAbility.class, CommonUtil.getAbilities(player)).isEmpty()) {
                if (str.startsWith("/tell ") || str.startsWith("/say ")) {
                    return false;
                }
                return str.isBlank() || str.startsWith("/");
            }
            return true;
        });
    }

    @Inject(method = "handleChatInput(Ljava/lang/String;Z)V", at = @At("HEAD"), cancellable = true)
    private void mixin$cancelChatInput(String str, boolean addToRecentChat, CallbackInfo ci) {
        var player = Minecraft.getInstance().player;

        if (player != null && !TBConfig.COMMON.chatForMuted.get()
                && !CommonUtil.listOfType(NodHeadAbility.class, CommonUtil.getAbilities(player)).isEmpty()) {
            if (!str.isBlank() && !str.startsWith("/") || str.startsWith("/say ") || str.startsWith("/tell ")) {
                ci.cancel();
            }
        }
    }
}