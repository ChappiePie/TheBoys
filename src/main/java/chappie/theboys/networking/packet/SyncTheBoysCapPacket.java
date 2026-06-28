package chappie.theboys.networking.packet;

import chappie.theboys.TheBoys;
import chappie.theboys.common.capability.TheBoysCap;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SyncTheBoysCapPacket(CompoundTag tag) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncTheBoysCapPacket> PACKET = new CustomPacketPayload.Type<>(TheBoys.id("sync_theboys_cap"));
    public static final StreamCodec<ByteBuf, SyncTheBoysCapPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG,
            SyncTheBoysCapPacket::tag,
            SyncTheBoysCapPacket::new
    );

    public void handle(LocalPlayer player) {
        TheBoysCap cap = TheBoysCap.getCap(player);
        if (cap != null) {
            cap.deserializeNBT(player.level().registryAccess(), this.tag);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET;
    }
}
