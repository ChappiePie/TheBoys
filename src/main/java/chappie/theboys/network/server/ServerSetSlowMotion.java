package chappie.theboys.network.server;

import chappie.theboys.common.capability.BoysCap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import xyz.heroesunited.heroesunited.common.capabilities.HUPlayer;

import java.util.function.Supplier;

public class ServerSetSlowMotion {

    public boolean slowMo;

    public ServerSetSlowMotion(boolean slowMo) {
        this.slowMo = slowMo;
    }

    public ServerSetSlowMotion(PacketBuffer buffer) {
        this.slowMo = buffer.readBoolean();
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeBoolean(this.slowMo);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PlayerEntity player = ctx.get().getSender();
            if (player != null) {
                player.getCapability(BoysCap.CAPABILITY).ifPresent((a) -> {
                    a.setSlowMotion(this.slowMo);
                    HUPlayer.getCap(player).sync();
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}