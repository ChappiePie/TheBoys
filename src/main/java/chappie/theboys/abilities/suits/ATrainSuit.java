package chappie.theboys.abilities.suits;

import chappie.theboys.abilities.SpeedAbility;
import chappie.theboys.common.items.TBItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.heroesunited.generatorrex.abilities.AbilityType;
import xyz.heroesunited.generatorrex.abilities.suit.Suit;
import xyz.heroesunited.generatorrex.abilities.suit.SuitItem;
import xyz.heroesunited.heroesunited.client.events.HUSetRotationAnglesEvent;

import static net.minecraft.inventory.EquipmentSlotType.*;

public class ATrainSuit extends Suit implements ISpeedSuit {

    public ATrainSuit() {
        super(TBSuitTypes.ATRAIN);
    }

    public IArmorMaterial getSuitMaterial() {
        return ArmorMaterial.CHAIN;
    }

    @Override
    public Item getHelmet() {
        return TBItems.ATRAIN_HELMET.get();
    }

    @Override
    public Item getChestplate() {
        return TBItems.ATRAIN_CHEST.get();
    }

    @Override
    public Item getLegs() {
        return TBItems.ATRAIN_LEGS.get();
    }

    @Override
    public Item getBoots() {
        return TBItems.ATRAIN_BOOTS.get();
    }

    @Override
    public boolean canCombineWithAbility(AbilityType type, PlayerEntity player) {
        return type.create() instanceof SpeedAbility;
    }
    @OnlyIn(Dist.CLIENT)
    @Override
    public void setRotationAngles(HUSetRotationAnglesEvent event) {
        if (event.getPlayer().getItemStackFromSlot(CHEST).getItem() instanceof SuitItem) {
            event.getPlayerModel().bipedBodyWear.showModel = false;
            event.getPlayerModel().bipedRightArmwear.showModel = false;
            event.getPlayerModel().bipedLeftArmwear.showModel = false;
        }

        if (event.getPlayer().getItemStackFromSlot(FEET).getItem() instanceof SuitItem
                || event.getPlayer().getItemStackFromSlot(LEGS).getItem() instanceof SuitItem) {
            event.getPlayerModel().bipedRightLegwear.showModel = false;
            event.getPlayerModel().bipedLeftLegwear.showModel = false;
        }
    }
}
