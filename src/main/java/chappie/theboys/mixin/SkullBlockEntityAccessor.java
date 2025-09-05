package chappie.theboys.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Mixin(SkullBlockEntity.class)
public interface SkullBlockEntityAccessor {

    @Invoker("fetchGameProfile")
    static CompletableFuture<Optional<GameProfile>> fetchGameProfile(String pProfileName) {
        throw new AssertionError();
    }
}