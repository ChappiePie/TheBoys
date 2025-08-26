package chappie.theboys.common.block.menu;

import chappie.theboys.common.item.VialItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;

public class SynthesizerMenu extends AbstractContainerMenu {
    public final Container container;
    public final ContainerData data;
    protected final Level level;

    public SynthesizerMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(9), new SimpleContainerData(7));
    }

    public SynthesizerMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(TBMenus.SYNTHESIZER, containerId);
        this.container = container;
        checkContainerSize(container, 9);
        checkContainerDataCount(data, 7);
        this.data = data;
        this.level = playerInventory.player.level();
        this.addSlot(new WaterSlot(container, 0, 14, 54));
        this.addSlot(new FuelSlot(this, container, 1, 145, 54));
        this.addSlot(new Slot(container, 2, 79, 31) {
            @Override
            public boolean isActive() {
                return data.get(0) == 0;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        this.addSlot(new CentrifugeSlot(container, 3, 51, 14));
        this.addSlot(new CentrifugeSlot(container, 4, 107, 14));
        this.addSlot(new CentrifugeSlot(container, 5, 79, 4));
        this.addSlot(new CentrifugeSlot(container, 6, 51, 49));
        this.addSlot(new CentrifugeSlot(container, 7, 79, 59));
        this.addSlot(new CentrifugeSlot(container, 8, 107, 49));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }

        this.addDataSlots(data);
    }

    public static boolean isWater(ItemStack stack) {
        return stack.is(Items.WATER_BUCKET) || stack.is(Items.POTION) && stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion().orElse(null) == Potions.WATER;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 0) {
            this.setData(0, this.data.get(0) == 0 ? 1 : 0);
            return true;
        }

        return super.clickMenuButton(player, id);
    }

    @Override
    public void setData(int id, int data) {
        super.setData(id, data);
        this.broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            if (index == 2) {
                if (!this.moveItemStackTo(itemStack2, 9, 39, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemStack2, itemStack);
            } else if (index > 8) {
                if (SynthesizerMenu.isWater(itemStack2)) {
                    if (!this.moveItemStackTo(itemStack2, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (player.level().fuelValues().isFuel(itemStack2) && !this.slots.get(1).hasItem()) {
                    if (!this.moveItemStackTo(itemStack2, 1, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (itemStack2.getItem() instanceof VialItem && this.slots.subList(3, 9).stream().anyMatch(p -> !p.hasItem())) {
                    int i = 3;
                    while (!itemStack2.isEmpty() && i < 9) {
                        if (!this.slots.get(i).hasItem() && this.slots.get(i).mayPlace(itemStack2)) {
                            ItemStack itemStack3 = itemStack2.copyWithCount(1);
                            itemStack2.shrink(1);
                            this.slots.get(i).setByPlayer(itemStack3);
                        }
                        i++;
                    }
                } else if (!this.slots.get(2).hasItem()) {
                    if (this.slots.get(2).hasItem() || !this.slots.get(2).mayPlace(itemStack2)) {
                        return ItemStack.EMPTY;
                    }

                    ItemStack itemStack3 = itemStack2.copyWithCount(1);
                    itemStack2.shrink(1);
                    this.slots.get(2).setByPlayer(itemStack3);
                } else if (index < 36) {
                    if (!this.moveItemStackTo(itemStack2, 36, 44, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < 44 && !this.moveItemStackTo(itemStack2, 9, 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemStack2, 9, 39, false)) {
                return ItemStack.EMPTY;
            }

            if (itemStack2.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemStack2);
        }

        return itemStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    public boolean isCentrifugeEmpty() {
        for (Slot slot : this.slots) {
            if (slot instanceof CentrifugeSlot slot1) {
                if (!slot1.getItem().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    public float getBurnProgress() {
        int i = this.data.get(5);
        int j = this.data.get(6);
        return j != 0 && i != 0 ? Mth.clamp((float) i / (float) j, 0.0F, 1.0F) : 0.0F;
    }

    public float getLitProgress() {
        int i = this.data.get(4);
        if (i == 0) {
            i = 200;
        }

        return Mth.clamp((float) this.data.get(3) / (float) i, 0.0F, 1.0F);
    }

    public boolean isLit() {
        return this.data.get(3) > 0;
    }

    public boolean isWork() {
        return this.getBurnProgress() != 0;
    }

    public int getWaterMb() {
        return this.data.get(1);
    }

    private boolean isFuel(ItemStack stack) {
        return this.level.fuelValues().isFuel(stack);
    }

    static class FuelSlot extends Slot {

        private final SynthesizerMenu menu;

        public FuelSlot(SynthesizerMenu menu, Container container, int slot, int xPosition, int yPosition) {
            super(container, slot, xPosition, yPosition);
            this.menu = menu;
        }

        public static boolean isBucket(ItemStack stack) {
            return stack.is(Items.BUCKET);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return this.menu.isFuel(stack) || isBucket(stack);
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return isBucket(stack) ? 1 : super.getMaxStackSize(stack);
        }
    }

    static class WaterSlot extends Slot {

        public WaterSlot(Container container, int slot, int xPosition, int yPosition) {
            super(container, slot, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            PotionContents potionContents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            return stack.is(Items.BUCKET) || stack.is(Items.WATER_BUCKET) || stack.is(Items.GLASS_BOTTLE)
                    || stack.is(Items.POTION) && potionContents.is(Potions.WATER);
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return stack.is(Items.WATER_BUCKET) ? 1 : super.getMaxStackSize(stack);
        }
    }

    public class CentrifugeSlot extends Slot {

        public CentrifugeSlot(Container container, int slot, int xPosition, int yPosition) {
            super(container, slot, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            boolean b = stack.getItem() instanceof VialItem;
            return b;
        }

        @Override
        public boolean isActive() {
            return SynthesizerMenu.this.data.get(0) == 0 || SynthesizerMenu.this.data.get(2) != 0;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
