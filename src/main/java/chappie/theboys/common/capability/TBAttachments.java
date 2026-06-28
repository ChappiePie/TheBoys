package chappie.theboys.common.capability;

import chappie.theboys.TheBoys;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class TBAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, TheBoys.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<TheBoysCap>> THEBOYS_CAP =
        ATTACHMENT_TYPES.register("cap", () -> AttachmentType.serializable(holder -> {
            if (holder instanceof LivingEntity living) {
                return new TheBoysCap(living);
            }
            return new TheBoysCap(null);
        }).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<TBEntityCap>> ENTITY_CAP =
        ATTACHMENT_TYPES.register("entity", () -> AttachmentType.serializable(holder -> {
            if (holder instanceof Entity entity) {
                return new TBEntityCap(entity);
            }
            return new TBEntityCap(null);
        }).build());

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}
