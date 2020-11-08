package chappie.theboys.network.client;

import chappie.theboys.util.TBUtil;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientSetSlowMotion {

    public int entityId;
    public boolean slowMo;

    public ClientSetSlowMotion(int entityId, boolean slowMo) {
        this.entityId = entityId;
        this.slowMo = slowMo;
    }

    public ClientSetSlowMotion(PacketBuffer buffer) {
        this.entityId = buffer.readInt();
        this.slowMo = buffer.readBoolean();
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeInt(this.entityId);
        buffer.writeBoolean(this.slowMo);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Entity entity = net.minecraft.client.Minecraft.getInstance().world.getEntityByID(this.entityId);

            if (entity instanceof AbstractClientPlayerEntity) {
                if(this.slowMo) {
                    TBUtil.updateClientTickrate(6F);
                } else {
                    TBUtil.updateClientTickrate(TBUtil.CLIENT_DEFAULT_TICKS);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}