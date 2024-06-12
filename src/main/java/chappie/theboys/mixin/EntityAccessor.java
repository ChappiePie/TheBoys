package chappie.theboys.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityAccessor {

    @Accessor("wasTouchingWater")
    void setWasTouchingWater(boolean wasTouchingWater);
}
