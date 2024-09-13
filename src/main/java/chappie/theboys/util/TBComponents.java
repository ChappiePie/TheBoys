package chappie.theboys.util;

import chappie.theboys.common.capability.TheBoysCap;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.minecraft.world.entity.player.Player;

public class TBComponents implements EntityComponentInitializer {
    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.beginRegistration(Player.class, TheBoysCap.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(TheBoysCap::new);
    }
}