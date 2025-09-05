package chappie.theboys.common.block.entity;

import chappie.modulus.util.IHasTimer;
import chappie.theboys.common.block.menu.SynthesizerMenu;
import chappie.theboys.common.item.TBItems;
import chappie.theboys.common.item.VialItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.function.Supplier;

public class SynthesizerBlockEntity extends BaseContainerBlockEntity implements GeoBlockEntity, IHasTimer {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public int tickCount;
    int waterMb = 0;
    int litTime;
    int litDuration;
    int cookingProgress;
    int cookingTotalTime = 200;
    public final Timer workTimer = new Timer(() -> 5, this::isWork);
    public final Timer rollTimer = new RollTimer(this::isWork);
    boolean work;
    private boolean opened = true;
    public final Timer openTimer = new Timer(() -> 5, () -> this.opened);
    private NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);
    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            SynthesizerBlockEntity be = SynthesizerBlockEntity.this;
            return switch (index) {
                case 0 -> be.work ? 1 : 0;
                case 1 -> be.waterMb;
                case 2 -> be.isEmpty() ? 1 : 0;
                case 3 -> be.litTime;
                case 4 -> be.litDuration;
                case 5 -> be.cookingProgress;
                case 6 -> be.cookingTotalTime;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            SynthesizerBlockEntity be = SynthesizerBlockEntity.this;
            switch (index) {
                case 0:
                    be.setWork(value != 0);
                    break;
                case 1:
                    be.setWaterMb(value);
                    break;
                case 3:
                    be.litTime = value;
                    break;
                case 4:
                    be.litDuration = value;
                    break;
                case 5:
                    be.cookingProgress = value;
                    break;
                case 6:
                    be.cookingTotalTime = value;
            }
        }

        @Override
        public int getCount() {
            return 7;
        }
    };

    public SynthesizerBlockEntity(BlockPos pos, BlockState blockState) {
        super(TBBlockEntities.SYNTHESIZER, pos, blockState);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.theboys.synthesizer");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new SynthesizerMenu(containerId, inventory, this, this.dataAccess);
    }

    @Override
    public int getContainerSize() {
        return this.getItems().size();
    }


    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.waterMb = tag.getInt("waterMb");
        this.work = tag.getBoolean("work");
        this.opened = tag.getBoolean("opened");
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.getItems(), registries);

        this.litTime = tag.getShort("burnTime");
        this.cookingProgress = tag.getShort("time");
        this.cookingTotalTime = tag.getShort("timeTotal");
        this.litDuration = this.getBurnDuration(this.items.get(1));
    }

    protected int getBurnDuration(ItemStack fuel) {
        if (fuel.isEmpty()) {
            return 0;
        } else {
            return AbstractFurnaceBlockEntity.getFuel().getOrDefault(fuel.getItem(), 0);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("waterMb", this.waterMb);
        tag.putBoolean("work", this.work);
        tag.putBoolean("opened", this.opened);
        ContainerHelper.saveAllItems(tag, this.getItems(), registries);

        tag.putShort("burnTime", (short) this.litTime);
        tag.putShort("time", (short) this.cookingProgress);
        tag.putShort("timeTotal", (short) this.cookingTotalTime);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack itemStack = ContainerHelper.removeItem(this.getItems(), slot, amount);
        if (!itemStack.isEmpty()) {
            this.markUpdated();
        }

        return itemStack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.getItems().set(slot, stack);
        if (stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }
        this.markUpdated();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemStack : this.getItems()) {
            if (!itemStack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public boolean isCentrifugeEmpty() {
        var list = this.getItems().subList(3, 9);
        for (ItemStack itemStack : list) {
            if (!itemStack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public int numOfVials() {
        int i = 0;
        for (ItemStack itemStack : this.getItems().subList(3, 9)) {
            if (!itemStack.isEmpty()) {
                itemStack.shrink(1);
                i++;
            }
        }
        return i;
    }

    public float getBurnProgress() {
        int i = this.cookingProgress;
        int j = this.cookingTotalTime;
        return j != 0 && i != 0 ? Mth.clamp((float) i / (float) j, 0.0F, 1.0F) : 0.0F;
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.getItems().get(slot);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.getItems(), slot);
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        this.getItems().clear();
    }

    public NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void markUpdated() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    public boolean isOpened() {
        return this.opened;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
        this.markUpdated();
        this.setWork(!opened);
    }

    public boolean setWaterMb(int waterMb) {
        if (waterMb <= 500) {
            this.waterMb = Math.max(0, waterMb);
            this.markUpdated();
            return true;
        }
        return false;
    }

    public boolean isWork() {
        return this.getBurnProgress() != 0;
    }

    public void setWork(boolean work) {
        boolean work1 = this.waterMb != 0 && !this.isCentrifugeEmpty() && !this.getItems().get(2).isEmpty() && this.getItems().get(2).getCount() <= 1;
        if (work1) {
            if (this.opened) {
                this.setOpened(false);

            }
            this.work = work;
            this.markUpdated();
        }
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        this.cookingTotalTime = 200;
        this.tickCount++;
        for (Timer timer : this.timers()) {
            timer.update();
        }

        if (this.opened || this.waterMb == 0 || this.isCentrifugeEmpty() || this.getItems().get(2).isEmpty() || this.getItems().get(2).getCount() > 1) {
            this.work = false;
            this.markUpdated();
        }

        // set up water
        ItemStack waterStack = this.items.get(0);
        if (!waterStack.isEmpty()) {
            PotionContents potionContents = waterStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            if (waterStack.is(Items.POTION) && potionContents.is(Potions.WATER)) {
                if (this.setWaterMb(this.waterMb + 25)) {
                    this.items.set(0, Items.GLASS_BOTTLE.getDefaultInstance());
                }
            } else if (waterStack.is(Items.WATER_BUCKET)) {
                if (this.setWaterMb(this.waterMb + 100)) {
                    this.items.set(0, Items.BUCKET.getDefaultInstance());
                }
            }
        }

        boolean bl = this.isLit();
        boolean bl2 = false;
        if (this.isLit()) {
            this.litTime--;
        }

        ItemStack fuelStack = this.items.get(1);
        if (!this.isLit() && !fuelStack.isEmpty() && this.work) {
            this.litTime = this.getBurnDuration(fuelStack);
            this.litDuration = this.litTime;
            if (this.isLit()) {
                bl2 = true;
                Item item = fuelStack.getItem();
                fuelStack.shrink(1);
                if (fuelStack.isEmpty()) {
                    Item item2 = item.getCraftingRemainingItem();
                    this.items.set(1, item2 == null ? ItemStack.EMPTY : new ItemStack(item2));
                }
            }
        }

        if (this.isLit() && this.work) {
            this.cookingProgress++;
            int i = level.random.nextInt(6);
            if (this.cookingProgress % (this.cookingTotalTime / 10) == 0) {
                if (!this.setWaterMb(this.waterMb - 25)) {
                    this.setWork(false);
                }
            }

            if (this.cookingProgress % (this.cookingTotalTime / 10) == 0 && level.random.nextInt(100) <= 25) {
                this.removeItem(3 + i, 1);
            }

            if (this.cookingProgress == this.cookingTotalTime) {
                this.cookingProgress = 0;
                this.cookingTotalTime = 200;
                ItemStack stack = this.items.get(2);
                ItemStack resultItem = TBItems.VIAL.getDefaultInstance();
                int vials = this.numOfVials();
                if (stack.is(Items.POISONOUS_POTATO) || stack.getRarity() != Rarity.COMMON || stack.getItem() instanceof PotionItem) {
                    if (vials < 3 ? level.random.nextInt(100) < 75 : level.random.nextInt(100) < 50) {
                        resultItem = VialItem.compoundV();
                    }
                } else {
                    if (level.random.nextInt(100) <= 25) {
                        resultItem = VialItem.compoundV();
                    } else if (level.random.nextInt(100) <= 40) {
                        ItemStack potionStack = new ItemStack(Items.POTION);
                        Potion potion = BuiltInRegistries.POTION.stream().filter(p -> BuiltInRegistries.POTION.getKey(p).getNamespace().equals("minecraft") && !p.getEffects().isEmpty()).findAny().get();
                        potionStack.set(DataComponents.POTION_CONTENTS, new PotionContents(BuiltInRegistries.POTION.wrapAsHolder(potion)));
                        resultItem = potionStack;
                    }
                }
                stack.shrink(1);
                if (stack.isEmpty()) {
                    this.items.set(2, resultItem.copyWithCount(vials));
                }
                this.setOpened(true);
                bl2 = true;
            }
        } else {
            if (this.cookingProgress > this.cookingTotalTime / 2) {
                this.cookingProgress = 0;
            } else {
                this.cookingProgress = Mth.clamp(this.cookingProgress - 1, 0, this.cookingTotalTime);
            }

            if (this.cookingProgress == 0 && this.work) {
                this.setWork(false);
            }
        }

        if (bl != this.isLit()) {
            bl2 = true;
        }

        if (bl2) {
            setChanged(level, pos, state);
        }
    }

    private boolean isLit() {
        return this.litTime > 0;
    }

    @Override
    public Iterable<Timer> timers() {
        return List.of(this.openTimer, this.workTimer, this.rollTimer);
    }

    public static class RollTimer extends Timer {
        public RollTimer(Supplier<Boolean> predicate) {
            super(() -> 20, predicate);
        }

        @Override
        public void update() {
            int maxTimer = this.maxTimer.get();
            boolean predicate = this.predicate.get();
            if (this.timer >= maxTimer) {
                this.timer = 0;
            }
            this.prevTimer = this.timer;
            if (this.timer < maxTimer && predicate) {
                this.timer++;
            }
            if (this.timer > 0 && !predicate) {
                if (this.timer > maxTimer / 2) {
                    this.timer++;
                } else {
                    this.timer--;
                }
            }
        }
    }
}
