package chappie.theboys.network.client;

import chappie.theboys.common.capability.BoysCap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import xyz.heroesunited.heroesunited.client.gui.AbilitiesScreen;

import java.util.function.Supplier;

public class ClientSyncBoysCap {

    public int entityId;
    private CompoundNBT data;

    public ClientSyncBoysCap(int entityId, CompoundNBT data) {
        this.entityId = entityId;
        this.data = data;
    }

    public ClientSyncBoysCap(PacketBuffer buf) {
        this.entityId = buf.readInt();
        this.data = buf.readNbt();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeInt(this.entityId);
        buf.writeNbt(this.data);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            Entity entity = mc.level.getEntity(this.entityId);
            if (entity instanceof AbstractClientPlayerEntity) {
                entity.getCapability(BoysCap.CAPABILITY).ifPresent(data -> data.deserializeNBT(this.data));
                if (mc.screen instanceof AbilitiesScreen) {
                    mc.screen.init(mc, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
