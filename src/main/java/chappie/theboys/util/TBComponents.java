package chappie.theboys.util;

import chappie.theboys.common.capability.TBEntityCap;
import chappie.theboys.common.capability.TheBoysCap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;

public class TBComponents implements EntityComponentInitializer {
    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.beginRegistration(LivingEntity.class, TheBoysCap.KEY).respawnStrategy(RespawnCopyStrategy.ALWAYS_COPY).end(TheBoysCap::new);
        registry.beginRegistration(Entity.class, TBEntityCap.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(TBEntityCap::new);
    }
}