package chappie.theboys.common.item.suit;

import chappie.theboys.TheBoys;
import chappie.theboys.client.renderer.ClientHeroWithCapeProperties;
import chappie.theboys.util.ClientSuitProperties;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class SuitItem extends Item {

    public final SuitProperties properties;
    private final Supplier<ClientSuitProperties> clientSuitProperties;

    public SuitItem(SuitProperties pProperties) {
        super(pProperties);
        this.properties = pProperties;
        this.clientSuitProperties = () -> switch (this.properties.type) {
            case "homelander", "stormfront" -> new ClientHeroWithCapeProperties(this);
            case "starlight" -> new ClientSuitProperties(this) {
                @Override
                public ResourceLocation suitTexture(EquipmentSlot slot, LivingEntity entity, ItemStack armorStack, String type) {
                    return slot == EquipmentSlot.FEET ? new ResourceLocation(TheBoys.MODID, "textures/suits/%s/layer_1.png".formatted(this.type())) : super.suitTexture(slot, entity, armorStack, type);
                }
            };
            default -> new ClientSuitProperties(this);
        };
        DispenserBlock.registerBehavior(this, SuitProperties.DISPENSE_ITEM_BEHAVIOR);
    }

    public ClientSuitProperties getClientSuitProperties() {
        return this.clientSuitProperties.get();
    }

    public EquipmentSlot equipmentSlot(ItemStack armorStack, ItemStack suitStack, Player pPlayer) {
        return this.properties.getSlot();
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        pTooltipComponents.add(Component.translatable("item.theboys.suit.equip").withStyle(ChatFormatting.GRAY));
    }
}