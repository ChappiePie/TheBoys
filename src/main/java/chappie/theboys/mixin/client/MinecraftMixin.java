package chappie.theboys.mixin.client;

import chappie.theboys.util.ISetupGameProfiles;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.client.Minecraft;
import net.minecraft.server.Services;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.io.File;

@Mixin(Minecraft.class)
public class MinecraftMixin implements ISetupGameProfiles {
    @Shadow @Final private YggdrasilAuthenticationService authenticationService;

    @Shadow @Final public File gameDirectory;

    @Override
    public void theBoys$setup() {
        Minecraft mc = (Minecraft) (Object) this;
        Services services = Services.create(this.authenticationService, this.gameDirectory);
        services.profileCache().setExecutor(mc);
        SkullBlockEntity.setup(services, mc);
        GameProfileCache.setUsesAuthentication(false);
    }
}
