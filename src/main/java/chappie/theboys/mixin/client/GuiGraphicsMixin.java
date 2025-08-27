package chappie.theboys.mixin.client;

import chappie.theboys.common.item.datacomponents.TBDataComponents;
import chappie.theboys.common.item.suit.SuitItem;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {

    @Shadow @Final private Minecraft minecraft;

    @Shadow @Final private ItemStackRenderState scratchItemStackRenderState;

    @Shadow @Final private PoseStack pose;
    @Shadow @Final private MultiBufferSource.BufferSource bufferSource;

    @Shadow public abstract void flush();

    @Inject(method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;IIII)V",
            at = @At("TAIL"))
    private void tryRenderGuiItem(LivingEntity entity, Level level, ItemStack stack, int x, int y, int seed, int guiOffset, CallbackInfo ci) {
        if (stack.getItem() instanceof ArmorItem && stack.get(DataComponents.EQUIPPABLE) != null) {
            ItemStack suitStack = stack.getOrDefault(TBDataComponents.SUIT, ItemStack.EMPTY);
            if (suitStack.getItem() instanceof SuitItem item
                    && stack.get(DataComponents.EQUIPPABLE).slot().equals(item.properties.getSlot())) {
                if (!stack.isEmpty()) {
                    this.minecraft.getItemModelResolver().updateForTopItem(this.scratchItemStackRenderState, suitStack, ItemDisplayContext.HEAD, false, level, entity, seed);
                    this.pose.pushPose();
                    this.pose.translate(x, y + 16, 150 + (this.scratchItemStackRenderState.isGui3d() ? guiOffset : 0));

                    try {
                        this.pose.scale(16.0F, -16.0F, 16.0F);
                        boolean bl = !this.scratchItemStackRenderState.usesBlockLight();
                        if (bl) {
                            this.flush();
                            Lighting.setupForFlatItems();
                        }

                        this.scratchItemStackRenderState.render(this.pose, this.bufferSource, 15728880, OverlayTexture.NO_OVERLAY);
                        this.flush();
                        if (bl) {
                            Lighting.setupFor3DItems();
                        }
                    } catch (Throwable var11) {
                        CrashReport crashReport = CrashReport.forThrowable(var11, "Rendering item");
                        CrashReportCategory crashReportCategory = crashReport.addCategory("Item being rendered");
                        crashReportCategory.setDetail("Item Type", () -> String.valueOf(stack.getItem()));
                        crashReportCategory.setDetail("Item Components", () -> String.valueOf(stack.getComponents()));
                        crashReportCategory.setDetail("Item Foil", () -> String.valueOf(stack.hasFoil()));
                        throw new ReportedException(crashReport);
                    }

                    this.pose.popPose();
                }
            }
        }
    }
}