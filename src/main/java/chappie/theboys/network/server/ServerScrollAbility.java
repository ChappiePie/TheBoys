package chappie.theboys.network.server;

import chappie.theboys.abilities.ScrollAbility;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import xyz.heroesunited.heroesunited.common.abilities.AbilityHelper;

import java.util.function.Supplier;

public class ServerScrollAbility {

    private final String id;
    private final double scrollAmount;

    public ServerScrollAbility(String id, double scrollAmount) {
        this.id = id;
        this.scrollAmount = scrollAmount;
    }

    public ServerScrollAbility(FriendlyByteBuf buf) {
        this.id = buf.readUtf();
        this.scrollAmount = buf.readDouble();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(this.id);
        buf.writeDouble(this.scrollAmount);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = ctx.get().getSender();
            if (AbilityHelper.getActiveAbilityMap(player).get(this.id) instanceof ScrollAbility a) {
                a.consumer.accept(a, this.scrollAmount);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}