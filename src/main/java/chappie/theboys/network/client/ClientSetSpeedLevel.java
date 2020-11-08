package chappie.theboys.network.client;

import chappie.theboys.common.capability.BoysCap;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientSetSpeedLevel {

    public int entityId;
    public int speedLevel;

    public ClientSetSpeedLevel(int entityId, int speedLevel) {
        this.entityId = entityId;
        this.speedLevel = speedLevel;
    }

    public ClientSetSpeedLevel(PacketBuffer buffer) {
        this.entityId = buffer.readInt();
        this.speedLevel = buffer.readInt();
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeInt(this.entityId);
        buffer.writeInt(this.speedLevel);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Entity entity = net.minecraft.client.Minecraft.getInstance().world.getEntityByID(this.entityId);

            if (entity instanceof AbstractClientPlayerEntity) {
                entity.getCapability(BoysCap.CAPABILITY).ifPresent((a) -> {
                    a.setSpeedLevel(this.speedLevel);
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}