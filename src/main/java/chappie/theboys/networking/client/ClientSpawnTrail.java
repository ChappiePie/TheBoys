package chappie.theboys.networking.client;

import chappie.theboys.TheBoys;
import chappie.theboys.common.entity.TrailEntity;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.UUID;

public class ClientSpawnTrail implements FabricPacket, Packet<ClientGamePacketListener> {

    public static final ResourceLocation PACKET_ID = TheBoys.id("spawn_trail");
    public static final Type<ClientSpawnTrail> PACKET = new Type<>(PACKET_ID);
    public static StreamCodec<FriendlyByteBuf, ClientSpawnTrail> CODEC = CustomPacketPayload.codec(ClientSpawnTrail::write, ClientSpawnTrail::new);
    public final TrailEntity entity;
    public final int typeId;
    public final int entityId;
    public final UUID uuid;
    public final double posX, posY, posZ;
    public final byte pitch, yaw, headYaw;
    public final int velX, velY, velZ;
    public final int lifeTime, ownerId;
    public final Color color;

    public ClientSpawnTrail(TrailEntity e) {
        this.entity = e;
        this.typeId = BuiltInRegistries.ENTITY_TYPE.getId(e.getType());
        this.entityId = e.getId();
        this.uuid = e.getUUID();
        this.posX = e.getX();
        this.posY = e.getY();
        this.posZ = e.getZ();
        this.pitch = (byte) Mth.floor(e.getXRot() * 256.0F / 360.0F);
        this.yaw = (byte) Mth.floor(e.getYRot() * 256.0F / 360.0F);
        this.headYaw = (byte) (e.getYHeadRot() * 256.0F / 360.0F);
        Vec3 vec3d = e.getDeltaMovement();
        double d1 = Mth.clamp(vec3d.x, -3.9D, 3.9D);
        double d2 = Mth.clamp(vec3d.y, -3.9D, 3.9D);
        double d3 = Mth.clamp(vec3d.z, -3.9D, 3.9D);
        this.velX = (int) (d1 * 8000.0D);
        this.velY = (int) (d2 * 8000.0D);
        this.velZ = (int) (d3 * 8000.0D);
        this.lifeTime = e.lifeTime;
        this.ownerId = e.entity.getId();
        this.color = e.color;
    }

    public ClientSpawnTrail(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readInt(), new UUID(buf.readLong(), buf.readLong()), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readByte(), buf.readByte(), buf.readByte(), buf.readShort(), buf.readShort(), buf.readShort(),
                buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
    }

    private ClientSpawnTrail(int typeId, int entityId, UUID uuid, double posX, double posY, double posZ, byte pitch, byte yaw, byte headYaw, int velX, int velY, int velZ, int lifeTime, int ownerId, int red, int green, int blue) {
        this.entity = null;
        this.typeId = typeId;
        this.entityId = entityId;
        this.uuid = uuid;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.pitch = pitch;
        this.yaw = yaw;
        this.headYaw = headYaw;
        this.velX = velX;
        this.velY = velY;
        this.velZ = velZ;
        this.lifeTime = lifeTime;
        this.ownerId = ownerId;
        this.color = new Color(red, green, blue);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.typeId);
        buf.writeInt(this.entityId);
        buf.writeLong(this.uuid.getMostSignificantBits());
        buf.writeLong(this.uuid.getLeastSignificantBits());
        buf.writeDouble(this.posX);
        buf.writeDouble(this.posY);
        buf.writeDouble(this.posZ);
        buf.writeByte(this.pitch);
        buf.writeByte(this.yaw);
        buf.writeByte(this.headYaw);
        buf.writeShort(this.velX);
        buf.writeShort(this.velY);
        buf.writeShort(this.velZ);
        buf.writeInt(this.lifeTime);
        buf.writeInt(this.entity == null ? -1 : this.entity.getId());

        buf.writeInt(this.color.getRed());
        buf.writeInt(this.color.getGreen());
        buf.writeInt(this.color.getBlue());
    }

    @Override
    public net.minecraft.network.protocol.PacketType<? extends Packet<ClientGamePacketListener>> type() {
        return null;
    }

    @Override
    public void handle(ClientGamePacketListener handler) {
        this.handle(Minecraft.getInstance().player, null);

    }

    public void handle(LocalPlayer player, PacketSender packetSender) {
        Minecraft mc = Minecraft.getInstance();
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.byId(this.typeId);
        Entity e = type.create(mc.level);
        if (e == null)
            return;

        /*
         * Sets the postiion on the client, Mirrors what
         * Entity#recreateFromPacket and LivingEntity#recreateFromPacket does.
         */
        e.syncPacketPositionCodec(this.posX, this.posY, this.posZ);
        e.absMoveTo(this.posX, this.posY, this.posZ, (this.yaw * 360) / 256.0F, (this.pitch * 360) / 256.0F);
        e.setYHeadRot((this.headYaw * 360) / 256.0F);
        e.setYBodyRot((this.headYaw * 360) / 256.0F);

        e.setId(this.entityId);
        e.setUUID(this.uuid);
        mc.level.addEntity(e);
        e.lerpMotion(this.velX / 8000.0, this.velY / 8000.0, this.velZ / 8000.0);
        if (e instanceof TrailEntity entity) {
            entity.readSpawnData(this.lifeTime, (LivingEntity) entity.getCommandSenderWorld().getEntity(this.ownerId), this.color);
        }
    }
}