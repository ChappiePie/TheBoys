package chappie.theboys.network.client;

import chappie.theboys.common.capability.BoysCap;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientSetInSpeed {

    public int entityId;
    public boolean InSpeed;

    public ClientSetInSpeed(int entityId, boolean InSpeed) {
        this.entityId = entityId;
        this.InSpeed = InSpeed;
    }

    public ClientSetInSpeed(PacketBuffer buffer) {
        this.entityId = buffer.readInt();
        this.InSpeed = buffer.readBoolean();
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeInt(this.entityId);
        buffer.writeBoolean(this.InSpeed);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Entity entity = net.minecraft.client.Minecraft.getInstance().world.getEntityByID(this.entityId);

            if (entity instanceof AbstractClientPlayerEntity) {
                entity.getCapability(BoysCap.CAPABILITY).ifPresent((a) -> {
                    a.setInSpeed(this.InSpeed);
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}