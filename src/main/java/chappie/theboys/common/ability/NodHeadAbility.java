package chappie.theboys.common.ability;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.modulus.common.ability.base.AbilityClientProperties;
import chappie.modulus.util.IHasTimer;
import chappie.modulus.util.data.DataAccessor;
import chappie.modulus.util.events.SetupAnimCallback;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class NodHeadAbility extends Ability implements IHasTimer {
    public static final DataAccessor<Boolean> ANIMATING = new DataAccessor<>("nod_animating", DataAccessor.DataSerializer.BOOLEAN);
    public static final DataAccessor<Boolean> SIDE_TURNS = new DataAccessor<>("head_side_turns", DataAccessor.DataSerializer.BOOLEAN);
    public static final DataAccessor<Boolean> LEFT_RIGHT = new DataAccessor<>("left_right", DataAccessor.DataSerializer.BOOLEAN);

    private final IHasTimer.Timer timer = new OneWayTimer(() -> 15, this::isAnimating);

    public NodHeadAbility(LivingEntity entity, AbilityBuilder builder) {
        super(entity, builder);
    }

    private static float smoothstep(float edge0, float edge1, float t) {
        float x = Mth.clamp((t - edge0) / (edge1 - edge0), 0F, 1F);
        return x * x * (3F - 2F * x);
    }

    @Override
    public void defineData() {
        super.defineData();
        this.dataManager.define(ANIMATING, false, false, true);
        this.dataManager.define(SIDE_TURNS, false, false, true);
        this.dataManager.define(LEFT_RIGHT, false, false, true);
    }

    @Override
    public void update(LivingEntity entity, boolean enabled) {
        super.update(entity, enabled);
        if (entity.level().isClientSide()) {
            return;
        }
        boolean animating = this.isAnimating();
        if (enabled && !animating && this.isEnded()) {
            boolean noAnim = this.conditionManager.test("no");
            if (this.conditionManager.test("yes") || noAnim) {
                this.dataManager.set(ANIMATING, true);
                this.dataManager.set(SIDE_TURNS, noAnim);
                this.dataManager.set(LEFT_RIGHT, !this.dataManager.get(LEFT_RIGHT));
            }
        }
        if (animating && this.timer.timer >= this.timer.maxTimer.get()) {
            this.dataManager.set(ANIMATING, false);
        }
    }

    public boolean isEnded() {
        return !this.isAnimating() && this.timer.timer == 0;
    }

    @Override
    public void initializeClient(Consumer<AbilityClientProperties> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new AbilityClientProperties() {

            @Override
            public void setupAnim(SetupAnimCallback.SetupAnimEvent event) {
                AbilityClientProperties.super.setupAnim(event);
                if (!NodHeadAbility.this.isEnded()) {
                    if (isSideTurn()) {
                        event.model().head.yRot += (float) Math.toRadians(25F) * turnByShakeCurve(event.modelProperties().partialTicks());
                    } else {
                        event.model().head.xRot += (float) Math.toRadians(30F) * nodCurve(event.modelProperties().partialTicks());
                    }
                }
            }
        });
    }

    @Override
    public Iterable<Timer> timers() {
        return List.of(this.timer);
    }

    private boolean isAnimating() {
        return this.dataManager.get(ANIMATING);
    }

    public boolean isSideTurn() {
        return this.dataManager.get(SIDE_TURNS);
    }

    public float nodCurve(float partialTicks) {
        float p = NodHeadAbility.this.timer.value(partialTicks);
        return p <= 0.2F ? smoothstep(0F, 0.2F, p)
                : p <= 0.5F ? Mth.lerp(smoothstep(0.2F, 0.5F, p), 1F, 0F)
                : p <= 0.7F ? Mth.lerp(smoothstep(0.5F, 0.7F, p), 0F, -0.35F)
                : Mth.lerp(smoothstep(0.7F, 1F, p), -0.35F, 0F);
    }

    public float turnByShakeCurve(float partialTicks) {
        float p = this.timer.value(partialTicks);
        float v = p <= 0.2F ? smoothstep(0F, 0.2F, p)
                : p <= 0.5F ? Mth.lerp(smoothstep(0.2F, 0.5F, p), 1F, -1F)
                : p <= 0.75F ? Mth.lerp(smoothstep(0.5F, 0.75F, p), -1F, 0.2F)
                : Mth.lerp(smoothstep(0.75F, 1F, p), 0.2F, 0F);
        return v * (this.dataManager.get(LEFT_RIGHT) ? -1 : 1);
    }

    private static class OneWayTimer extends Timer {

        public OneWayTimer(Supplier<Integer> maxTimer, Supplier<Boolean> predicate) {
            super(maxTimer, predicate);
        }

        @Override
        public void update() {
            int maxTimer = this.maxTimer.get();
            boolean predicate = this.predicate.get();
            this.prevTimer = this.timer;
            if (predicate) {
                if (this.timer < maxTimer) {
                    this.timer++;
                }
            } else if (this.timer != 0) {
                this.timer = 0;
                this.prevTimer = 0;
            }
        }
    }
}