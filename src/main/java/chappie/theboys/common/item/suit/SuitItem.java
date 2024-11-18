package chappie.theboys.common.item.suit;

import chappie.theboys.TheBoys;
import chappie.theboys.client.renderer.ClientHeroWithCapeProperties;
import chappie.theboys.util.ClientSuitProperties;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
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
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack suitStack = player.getItemInHand(usedHand);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.isArmor()) {
                ItemStack pStack = player.getItemBySlot(slot);
                if (!pStack.isEmpty() && pStack.getItem() instanceof ArmorItem && suitStack.getItem() instanceof SuitItem item) {
                    EquipmentSlot suitSlot = item.equipmentSlot(pStack, suitStack, player);
                    if (suitSlot == null || slot.equals(suitSlot)) {
                        ItemStack suitItem = ItemStack.EMPTY;
                        if (pStack.getOrCreateTag().contains("Suit")) {
                            CompoundTag tag = pStack.getOrCreateTag().getCompound("Suit");
                            suitItem = ItemStack.of(tag.getCompound("Tags"));
                            pStack.getOrCreateTag().remove("Suit");
                        }
                        if (!pStack.getOrCreateTag().contains("Suit")) {
                            CompoundTag tag = new CompoundTag();
                            tag.put("Tags", suitStack.copyWithCount(1).save(new CompoundTag()));
                            pStack.getOrCreateTag().put("Suit", tag);
                            if (!player.getAbilities().instabuild) {
                                suitStack.shrink(1);
                            }
                        }
                        ItemStack itemStack3 = suitItem.isEmpty() ? suitStack : suitItem.copyAndClear();
                        ItemStack itemStack4 = suitStack.copyAndClear();
                        player.setItemInHand(usedHand, itemStack4);
                        if (!player.isSilent()) {
                            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARMOR_EQUIP_LEATHER, player.getSoundSource(), 1.0F, 1.0F);
                        }
                        return InteractionResultHolder.sidedSuccess(itemStack3, level.isClientSide());
                    }
                }
            }
        }
        player.displayClientMessage(Component.translatable("item.theboys.suit.rmb").withStyle(ChatFormatting.RED), true);
        return InteractionResultHolder.fail(suitStack);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        pTooltipComponents.add(Component.translatable("item.theboys.suit.equip").withStyle(ChatFormatting.GRAY));
    }
}