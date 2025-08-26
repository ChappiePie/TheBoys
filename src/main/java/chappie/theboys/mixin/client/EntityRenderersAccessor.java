package chappie.theboys.mixin.client;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(EntityRenderers.class)
public interface EntityRenderersAccessor {

    @Accessor("PROVIDERS")
    static Map<EntityType<?>, EntityRendererProvider<?>> providers() {
        throw new AssertionError();
    }

    @Accessor("PLAYER_PROVIDERS")
    static Map<PlayerSkin.Model, EntityRendererProvider<AbstractClientPlayer>> playerProviders() {
        throw new AssertionError();
    }
}
