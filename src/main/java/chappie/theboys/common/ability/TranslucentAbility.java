package chappie.theboys.common.ability;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.modulus.common.ability.base.AbilityClientProperties;
import chappie.modulus.util.IHasTimer;
import chappie.modulus.util.events.RendererChangeCallback;
import chappie.theboys.util.TBClientUtil;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.function.Consumer;

public class TranslucentAbility extends Ability implements IHasTimer {

    private final IHasTimer.Timer timer = new IHasTimer.Timer(() -> 5, this::isEnabled);

    public TranslucentAbility(LivingEntity entity, AbilityBuilder builder) {
        super(entity, builder);
    }

    @Override
    public void update(LivingEntity entity, boolean enabled) {
        super.update(entity, enabled);

    }

    public float getAlpha(float partialTicks) {
        return 1.0F - this.timer.value(partialTicks);
    }

    @Override
    public void initializeClient(Consumer<AbilityClientProperties> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new AbilityClientProperties() {

            @Override
            public boolean rendererChange(RendererChangeCallback.RendererChangeEvent<? extends LivingEntity, ? extends EntityModel<?>> event) {
                return this.alphaChange(event);
            }

            public <T extends LivingEntity> boolean alphaChange(RendererChangeCallback.RendererChangeEvent<T, ? extends EntityModel<T>> event) {
                AbilityClientProperties.super.rendererChange(event);
                float alpha = getAlpha(event.modelProperties().partialTicks());

                if (alpha < 1) {
                    RenderType renderType = TBClientUtil.RenderTypes.entityInvisibility(event.renderer().getTextureLocation(event.getEntity()));
                    event.renderer().getModel().renderToBuffer(event.poseStack(), event.multiBufferSource().getBuffer(renderType),
                            event.packedLight(), event.packedOverlay(), event.red(), event.green(), event.blue(), event.alpha() * alpha);
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
