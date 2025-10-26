package chappie.theboys.common.ability;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.modulus.common.ability.base.AbilityClientProperties;
import chappie.modulus.util.IHasTimer;
import chappie.modulus.util.events.RendererChangeCallback;
import chappie.theboys.util.TBClientUtil;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.function.Consumer;

public class TranslucentAbility extends Ability implements IHasTimer {

    private final IHasTimer.Timer timer = new IHasTimer.Timer(() -> 5, this::isEnabled);

    public TranslucentAbility(LivingEntity entity, AbilityBuilder builder) {
        super(entity, builder);
    }

    public float getAlpha(float partialTicks) {
        return 1.0F - this.timer.value(partialTicks);
    }

    @Override
    public void initializeClient(Consumer<AbilityClientProperties> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new AbilityClientProperties() {

            @Override
            public boolean rendererChange(RendererChangeCallback.RendererChangeEvent<? extends LivingEntity, ? extends LivingEntityRenderState, ? extends EntityModel<? super LivingEntityRenderState>> event) {
                return this.alphaChange(event);
            }

            @SuppressWarnings("unchecked")
            public <T extends LivingEntityRenderState> boolean alphaChange(RendererChangeCallback.RendererChangeEvent<? extends LivingEntity, T, ? extends EntityModel<? super LivingEntityRenderState>> event) {
                AbilityClientProperties.super.rendererChange(event);
                float alpha = getAlpha(event.modelProperties().partialTicks());

                if (alpha < 1) {
                    RenderType renderType = TBClientUtil.RenderTypes.entityInvisibility(event.renderer().getTextureLocation((T) event.modelProperties().renderstate()));
                    event.renderer().getModel().renderToBuffer(event.poseStack(), event.multiBufferSource().getBuffer(renderType),
                            event.packedLight(), event.packedOverlay(), ARGB.colorFromFloat((event.alpha() / 255F) * alpha, event.red() / 255F, event.green() / 255F, event.blue() / 255F));
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public Iterable<Timer> timers() {
        return List.of(this.timer);
    }
}
