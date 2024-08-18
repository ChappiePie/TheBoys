package chappie.theboys.networking.server;

import chappie.theboys.common.capability.TheBoysCap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class ServerSetEyeOptions {
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

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.eyesHeight);
        buf.writeInt(this.eyesLength);
    }

    public void handle(CustomPayloadEvent.Context ctx) {
        ServerPlayer player = ctx.getSender();
        if (player != null) {
            player.getCapability(TheBoysCap.CAPABILITY).ifPresent(data ->
                    data.setEyeOptions(this.eyesHeight, this.eyesLength));
        }
        ctx.setPacketHandled(true);
    }
}