package chappie.theboys.network.client;

import chappie.theboys.common.capability.BoysCap;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientSetCompoundV {

    public int entityId;
    public boolean compoundV;

    public ClientSetCompoundV(int entityId, boolean compoundV) {
        this.entityId = entityId;
        this.compoundV = compoundV;
    }

    public ClientSetCompoundV(PacketBuffer buffer) {
        this.entityId = buffer.readInt();
        this.compoundV = buffer.readBoolean();
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeInt(this.entityId);
        buffer.writeBoolean(this.compoundV);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Entity entity = net.minecraft.client.Minecraft.getInstance().world.getEntityByID(this.entityId);

            if (entity instanceof AbstractClientPlayerEntity) {
                entity.getCapability(BoysCap.CAPABILITY).ifPresent((a) -> {
                    a.setInSpeed(this.compoundV);
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}