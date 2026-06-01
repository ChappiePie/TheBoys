package chappie.theboys.common.ability.parkour;

import chappie.modulus.util.IHasTimer;
import chappie.modulus.util.data.DataManager;
import chappie.theboys.common.ability.ParkourAbility;
import net.minecraft.world.entity.player.Player;

import java.util.Collections;
import java.util.List;

public abstract class ParkourHandler {

    protected final ParkourAbility parkourAbility;

    public ParkourHandler(ParkourAbility parkourAbility) {
        this.parkourAbility = parkourAbility;
    }

    public List<IHasTimer.Timer> timers() {
        return Collections.emptyList();
    }

    public void defineData(DataManager dataManager) {
    }

    public abstract void reset();

    public void tick(Player player) {
    }

    public boolean canActivate(Player player) {
        return this.parkourAbility.activationHandlers.stream()
                .filter(p -> p != this)
                .noneMatch(ParkourHandler::isActive);
    }

    public boolean tryActivate(Player player) {
        return false;
    }

    public boolean isActive() {
        return false;
    }
}
