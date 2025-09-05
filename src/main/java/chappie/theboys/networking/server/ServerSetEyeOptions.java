package chappie.theboys.networking.server;

import chappie.theboys.TheBoys;
import chappie.theboys.common.capability.TheBoysCap;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class ServerSetEyeOptions implements FabricPacket {

    public static final PacketType<ServerSetEyeOptions> PACKET = PacketType.create(TheBoys.id("server_set_eye_options"), ServerSetEyeOptions::new);
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

    @Override
    public PacketType<?> getType() {
        return PACKET;
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
}