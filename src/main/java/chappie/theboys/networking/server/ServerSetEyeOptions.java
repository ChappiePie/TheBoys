package chappie.theboys.networking.server;

import chappie.theboys.TheBoys;
import chappie.theboys.common.capability.TheBoysCap;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class ServerSetEyeOptions implements CustomPacketPayload {

    public static final ResourceLocation PACKET_ID = TheBoys.id("server_set_eye_options");
    public static final Type<ServerSetEyeOptions> PACKET = new Type<>(PACKET_ID);
    public static StreamCodec<FriendlyByteBuf, ServerSetEyeOptions> CODEC = CustomPacketPayload.codec(ServerSetEyeOptions::write, ServerSetEyeOptions::new);

    public int eyesHeight;
    public int eyesLength;

    public ServerSetEyeOptions(int eyesHeight, int eyesLength) {
        this.eyesHeight = eyesHeight;
        this.eyesLength = eyesLength;
    }

    public ServerSetEyeOptions(FriendlyByteBuf buf) {
        this.eyesHeight = buf.readInt();
        this.eyesLength = buf.readInt();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.eyesHeight);
        buf.writeInt(this.eyesLength);
    }

    public void handle(ServerPlayer player, PacketSender packetSender) {
        if (player != null) {
            TheBoysCap.getCap(player).setEyeOptions(this.eyesHeight, this.eyesLength);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET;
    }
}