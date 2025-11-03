package chappie.theboys.common.ability;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.modulus.util.IHasTimer;
import chappie.modulus.util.data.DataAccessor;
import chappie.theboys.TheBoys;
import chappie.theboys.common.capability.TBEntityCap;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.VibrationParticleOption;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationInfo;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SuperHearingAbility extends Ability implements IHasTimer, VibrationSystem {

    public static final DataAccessor<Integer> RECEIVED = new DataAccessor<>("received", DataAccessor.DataSerializer.INT);
    public final DynamicGameEventListener<Listener> dynamicGameEventListener;
    private final VibrationSystem.User vibrationUser;
    public Timer timer = new Timer(() -> 10, this::isEnabled);
    private VibrationSystem.Data vibrationData = new VibrationSystem.Data();

    public SuperHearingAbility(LivingEntity entity, AbilityBuilder builder) {
        super(entity, builder);
        this.vibrationUser = new VibrationUser(this);
        this.dynamicGameEventListener = new DynamicGameEventListener<>(new VibrationSystem.Listener(this));
    }

    @Override
    public void defineData() {
        super.defineData();
        this.dataManager.define(RECEIVED, 0);
    }

    @Override
    public void update(LivingEntity entity, boolean enabled) {
        super.update(entity, enabled);
        if (!entity.level().isClientSide()) {
            this.tick(entity.level(), this.vibrationData, this.vibrationUser);
        }
        if (this.dataManager.get(RECEIVED) > 0) {
            this.dataManager.set(RECEIVED, this.dataManager.get(RECEIVED) - 1);
        }
    }

    @Override
    public List<Timer> timers() {
        return List.of(this.timer);
    }

    @Override
    public @NotNull Data getVibrationData() {
        return this.vibrationData;
    }

    @Override
    public @NotNull User getVibrationUser() {
        return this.vibrationUser;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = super.serializeNBT();
        VibrationSystem.Data.CODEC.encodeStart(NbtOps.INSTANCE, this.vibrationData).resultOrPartial(TheBoys.LOGGER::error).ifPresent(tag1 -> tag.put("listener", tag1));
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        super.deserializeNBT(tag);
        if (tag.contains("listener")) {
            VibrationSystem.Data.CODEC
                    .parse(new Dynamic<>(NbtOps.INSTANCE, tag.getCompound("listener").get()))
                    .resultOrPartial(TheBoys.LOGGER::error)
                    .ifPresent(data -> this.vibrationData = data);
        }
    }

    void tick(Level level, VibrationSystem.Data data, VibrationSystem.User user) {
        if (level instanceof ServerLevel serverLevel) {
            if (data.getCurrentVibration() == null) {
                trySelectAndScheduleVibration(serverLevel, data, user);
            }

            if (data.getCurrentVibration() != null) {
                boolean bl = data.getTravelTimeInTicks() > 0;
                tryReloadVibrationParticle(serverLevel, data, user);
                data.decrementTravelTime();
                if (data.getTravelTimeInTicks() <= 0) {
                    bl = receiveVibration(serverLevel, data, user, data.getCurrentVibration());
                }

                if (bl) {
                    user.onDataChanged();
                }
            }
        }
    }

    private void trySelectAndScheduleVibration(ServerLevel level, VibrationSystem.Data data, VibrationSystem.User user) {
        data.getSelectionStrategy().chosenCandidate(level.getGameTime()).ifPresent(vibrationInfo -> {
            data.setCurrentVibration(vibrationInfo);
            Vec3 vec3 = vibrationInfo.pos();
            data.setTravelTimeInTicks(user.calculateTravelTimeInTicks(vibrationInfo.distance()));
            VibrationParticleOption option = new VibrationParticleOption(user.getPositionSource(), data.getTravelTimeInTicks());
            level.sendParticles(option, vec3.x, vec3.y, vec3.z, 1, 0.0, 0.0, 0.0, 0.0);
            user.onDataChanged();
            data.getSelectionStrategy().startOver();
        });
    }

    private void tryReloadVibrationParticle(ServerLevel level, VibrationSystem.Data data, VibrationSystem.User user) {
        if (data.shouldReloadVibrationParticle()) {
            if (data.getCurrentVibration() == null) {
                data.setReloadVibrationParticle(false);
            } else {
                Vec3 vec3 = data.getCurrentVibration().pos();
                PositionSource positionSource = user.getPositionSource();
                Vec3 vec32 = positionSource.getPosition(level).orElse(vec3);
                int i = data.getTravelTimeInTicks();
                int j = user.calculateTravelTimeInTicks(data.getCurrentVibration().distance());
                double d = 1.0 - (double) i / (double) j;
                double e = Mth.lerp(d, vec3.x, vec32.x);
                double f = Mth.lerp(d, vec3.y, vec32.y);
                double g = Mth.lerp(d, vec3.z, vec32.z);
                VibrationParticleOption option = new VibrationParticleOption(positionSource, i);
                if (this.entity instanceof ServerPlayer player) {
                    level.sendParticles(player, option, false, true, e, f, g, 1, 0.0, 0.0, 0.0, 0.0);
                }
                /*else {
                    level.sendParticles(option, e, f, g, 1, 0.0, 0.0, 0.0, 0.0); // It's probably better not to send particles from an entity that hears to other players.
                }*/
                data.setReloadVibrationParticle(false);
            }
        }
    }

    private boolean receiveVibration(ServerLevel level, VibrationSystem.Data data, VibrationSystem.User user, VibrationInfo vibrationInfo) {
        BlockPos blockPos = BlockPos.containing(vibrationInfo.pos());
        BlockPos blockPos2 = user.getPositionSource().getPosition(level).map(BlockPos::containing).orElse(blockPos);
        if (user.requiresAdjacentChunksToBeTicking() && !areAdjacentChunksTicking(level, blockPos2)) {
            return false;
        } else {
            user.onReceiveVibration(
                    level,
                    blockPos,
                    vibrationInfo.gameEvent(),
                    vibrationInfo.getEntity(level).orElse(null),
                    vibrationInfo.getProjectileOwner(level).orElse(null),
                    VibrationSystem.Listener.distanceBetweenInBlocks(blockPos, blockPos2)
            );
            data.setCurrentVibration(null);
            return true;
        }
    }

    private boolean areAdjacentChunksTicking(Level level, BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);

        for (int i = chunkPos.x - 1; i <= chunkPos.x + 1; i++) {
            for (int j = chunkPos.z - 1; j <= chunkPos.z + 1; j++) {
                if (!level.shouldTickBlocksAt(ChunkPos.asLong(i, j)) || level.getChunkSource().getChunkNow(i, j) == null) {
                    return false;
                }
            }
        }

        return true;
    }

    static class VibrationUser implements VibrationSystem.User {
        private final PositionSource positionSource;

        private final SuperHearingAbility ability;

        public VibrationUser(SuperHearingAbility ability) {
            this.ability = ability;
            this.positionSource = new EntityPositionSource(this.ability.entity, this.ability.entity.getEyeHeight() / 2F);
        }

        @Override
        public int getListenerRadius() {
            return 40;
        }

        @Override
        public int calculateTravelTimeInTicks(float distance) {
            return User.super.calculateTravelTimeInTicks(distance) * 2;
        }

        @Override
        public @NotNull PositionSource getPositionSource() {
            return this.positionSource;
        }

        @Override
        public boolean canReceiveVibration(ServerLevel level, BlockPos pos, Holder<GameEvent> gameEvent, GameEvent.Context context) {
            LivingEntity entity = this.ability.entity;
            if (!entity.isDeadOrDying() && this.ability.isEnabled()
                    && level.getWorldBorder().isWithinBounds(pos)) {
                if (context.sourceEntity() instanceof LivingEntity livingEntity) {
                    boolean b = entity.level() == livingEntity.level()
                            && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(livingEntity)
                            && !entity.isAlliedTo(livingEntity)
                            && livingEntity.getType() != EntityType.ARMOR_STAND
                            && !livingEntity.isInvulnerable()
                            && !livingEntity.isDeadOrDying()
                            && entity.level().getWorldBorder().isWithinBounds(livingEntity.getBoundingBox());
                    return b && !livingEntity.is(entity);
                }

                return true;
            } else {
                return false;
            }
        }

        @Override
        public void onReceiveVibration(ServerLevel level, BlockPos pos, Holder<GameEvent> gameEvent, @Nullable Entity entity, @Nullable Entity playerEntity, float distance) {
            if (entity != null) {
                TBEntityCap cap = TBEntityCap.getCap(entity);
                if (cap != null) {
                    cap.setGlowingTick(20);
                }
            }
            this.ability.dataManager.set(RECEIVED, 10);
        }

        @Override
        public @NotNull TagKey<GameEvent> getListenableEvents() {
            return GameEventTags.VIBRATIONS;
        }
    }
}
