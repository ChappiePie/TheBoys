package chappie.theboys.util;

import chappie.theboys.common.capability.TBEntityCap;
import chappie.theboys.common.capability.TheBoysCap;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class TBComponents implements EntityComponentInitializer {
    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.beginRegistration(LivingEntity.class, TheBoysCap.KEY).respawnStrategy(RespawnCopyStrategy.ALWAYS_COPY).end(TheBoysCap::new);
        registry.beginRegistration(Entity.class, TBEntityCap.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(TBEntityCap::new);
    }
}