package chappie.theboys.common.ability;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.modulus.common.ability.base.AbilityClientProperties;
import chappie.modulus.util.model.ModelProperties;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Consumer;

public class HallucinationAbility extends Ability {
    public HallucinationAbility(LivingEntity entity, AbilityBuilder builder) {
        super(entity, builder);
    }

    @Override
    public void update(LivingEntity entity, boolean enabled) {
        super.update(entity, enabled);
    }

    @Override
    public void initializeClient(Consumer<AbilityClientProperties> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new AbilityClientProperties() {
            @Override
            public void render(LivingEntityRenderer<? extends LivingEntity, ? extends LivingEntityRenderState, ? extends EntityModel<?>> renderer, PoseStack poseStack, SubmitNodeCollector bufferIn, int packedLightIn, LivingEntity entity, ModelProperties modelProperties) {
            }
        });
    }
}