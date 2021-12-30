package chappie.theboys.network.client;

import chappie.theboys.common.capability.BoysCap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import xyz.heroesunited.heroesunited.client.gui.AbilitiesScreen;

import java.util.function.Supplier;

public class ClientSyncBoysCap {

    public int entityId;
    private CompoundTag data;

    public ClientSyncBoysCap(int entityId, CompoundTag data) {
        this.entityId = entityId;
        this.data = data;
    }

    public ClientSyncBoysCap(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.data = buf.readNbt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeNbt(this.data);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            Entity entity = mc.level.getEntity(this.entityId);
            if (entity instanceof AbstractClientPlayer) {
                entity.getCapability(BoysCap.CAPABILITY).ifPresent(data -> data.deserializeNBT(this.data));
                if (mc.screen instanceof AbilitiesScreen) {
                    mc.screen.init(mc, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
