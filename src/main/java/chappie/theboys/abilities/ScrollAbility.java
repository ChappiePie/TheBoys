package chappie.theboys.abilities;

import com.google.gson.JsonObject;
import net.minecraft.world.entity.player.Player;
import xyz.heroesunited.heroesunited.common.abilities.AbilityType;
import xyz.heroesunited.heroesunited.common.abilities.JSONAbility;

import java.util.function.BiConsumer;

public class ScrollAbility extends JSONAbility {
    public final BiConsumer<ScrollAbility, Double> consumer;

    public ScrollAbility(AbilityType type, Player player, JsonObject jsonObject, BiConsumer<ScrollAbility, Double> consumer) {
        super(type, player, jsonObject);
        this.consumer = consumer;
        this.actionType = ActionType.HELD;
    }
}
