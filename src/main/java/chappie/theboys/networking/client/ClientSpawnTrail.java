package chappie.theboys.networking.client;

import chappie.theboys.TheBoys;
import chappie.theboys.common.entity.TrailEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;

public class ClientSpawnTrail implements CustomPacketPayload {

    public static final ResourceLocation PACKET_ID = TheBoys.id("spawn_trail");
    public static final Type<ClientSpawnTrail> PACKET = new Type<>(PACKET_ID);
    public static StreamCodec<FriendlyByteBuf, ClientSpawnTrail> CODEC = CustomPacketPayload.codec(ClientSpawnTrail::write, ClientSpawnTrail::new);

    public final int entityId;
    public final int lifeTime, ownerId;
    public final Color color;

    public ClientSpawnTrail(TrailEntity e) {
        this.entityId = e.getId();
        this.lifeTime = e.lifeTime;
        if (e.attached != null) {
            this.ownerId = e.attached.getId();
        } else {
            this.ownerId = -1;
        }
        this.color = e.color;
    }

    public ClientSpawnTrail(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
    }

    private ClientSpawnTrail(int entityId, int lifeTime, int ownerId, int red, int green, int blue) {
        this.entityId = entityId;
        this.lifeTime = lifeTime;
        this.ownerId = ownerId;
        this.color = new Color(red, green, blue);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeInt(this.lifeTime);
        buf.writeInt(this.ownerId);

        buf.writeInt(this.color.getRed());
        buf.writeInt(this.color.getGreen());
        buf.writeInt(this.color.getBlue());
    }
}
