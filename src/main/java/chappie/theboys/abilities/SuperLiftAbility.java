package chappie.theboys.abilities;

import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.command.impl.CloneCommand;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IClearable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.heroesunited.heroesunited.client.events.HUSetRotationAnglesEvent;
import xyz.heroesunited.heroesunited.common.abilities.JSONAbility;

import java.util.LinkedList;
import java.util.List;

public class SuperLiftAbility extends JSONAbility {

    public SuperLiftAbility() {
        super(TBAbilityTypes.SUPER_LIFT);
    }

    @Override
    public void registerData() {
        super.registerData();
        this.dataManager.register("cooldownLift", 0);
    }

    @Override
    public void action(PlayerEntity player) {
        super.action(player);
        if (this.dataManager.<Integer>getValue("cooldownLift") > 0) {
            this.dataManager.set("cooldownLift", this.dataManager.<Integer>getValue("cooldownLift") - 1);
        }

        if (this.getEnabled() && this.dataManager.<Integer>getValue("cooldownLift") == 0 && player.level instanceof ServerWorld) {
            Vector3d posVc3d = new Vector3d(player.getX(), player.getY() + player.getBbHeight(), player.getZ());
            BlockPos pos = new BlockPos(posVc3d.x + 1, posVc3d.y, posVc3d.z + 1);
            BlockPos pos1 = new BlockPos(posVc3d.x - 1, posVc3d.y + 1, posVc3d.z - 1);
            clone(player, (ServerWorld) player.level, new MutableBoundingBox(pos, pos1));
        }
    }

    /**
     * Made by Mojang,
     * @see CloneCommand
     * @method clone
     *
     * Edited by Chappie
     */
    private void clone(PlayerEntity player, ServerWorld world, MutableBoundingBox box) {
        List<BlockInfo> list = Lists.newArrayList(),list1 = Lists.newArrayList(),list2 = Lists.newArrayList();
        LinkedList<BlockPos> deque = Lists.newLinkedList();
        BlockPos pos2 = new BlockPos(0, 1, 0);

        for (int j = box.z0; j <= box.z1; ++j) {
            for (int k = box.y0; k <= box.y1; ++k) {
                for (int l = box.x0; l <= box.x1; ++l) {
                    BlockPos fuck = new BlockPos(box.getCenter().getX(), k, box.getCenter().getZ());
                    BlockPos blockPos = new BlockPos(l, k, j);
                    BlockPos blockPos1 = blockPos.offset(pos2);
                    if (world.getBlockState(fuck).getMaterial() != Material.AIR && blockPos1.getY() < world.getMaxBuildHeight()) {
                        BlockState blockstate = world.getBlockState(blockPos);
                        BlockState blockstate1 = world.getBlockState(blockPos1);
                        if (blockstate.getMaterial() != Material.AIR && blockstate1.getMaterial() == Material.AIR) {
                            TileEntity tileentity = world.getBlockEntity(blockPos);
                            if (tileentity != null) {
                                CompoundNBT compoundnbt = tileentity.save(new CompoundNBT());
                                list1.add(new BlockInfo(blockPos1, blockstate, compoundnbt));
                                deque.addLast(blockPos);
                            } else if (!blockstate.isSolidRender(world, blockPos) && !blockstate.isCollisionShapeFullBlock(world, blockPos)) {
                                list2.add(new BlockInfo(blockPos1, blockstate, null));
                                deque.addFirst(blockPos);
                            } else {
                                list.add(new BlockInfo(blockPos1, blockstate, null));
                                deque.addLast(blockPos);
                            }
                        }
                    }
                }
            }
        }

        for(BlockPos blockpos4 : deque) {
            IClearable.tryClear(world.getBlockEntity(blockpos4));
        }

        for(BlockPos blockpos5 : deque) {
            world.setBlock(blockpos5, Blocks.AIR.defaultBlockState(), 3);
        }

        List<BlockInfo> list3 = Lists.newArrayList();
        list3.addAll(list);
        list3.addAll(list1);
        list3.addAll(list2);
        List<BlockInfo> list4 = Lists.reverse(list3);

        for (BlockInfo info : list4) {
            IClearable.tryClear(world.getBlockEntity(info.pos));
        }

        for (BlockInfo info : list3) {
            world.setBlock(info.pos, info.state, 2);
        }

        for (BlockInfo info : list1) {
            TileEntity tile = world.getBlockEntity(info.pos);
            if (info.tag != null && tile != null) {
                info.tag.putInt("x", info.pos.getX());
                info.tag.putInt("y", info.pos.getY());
                info.tag.putInt("z", info.pos.getZ());
                tile.load(info.state, info.tag);
                tile.setChanged();
            }

            world.setBlock(info.pos, info.state, 2);
        }

        for (BlockInfo info : list4) {
            world.blockUpdated(info.pos, info.state.getBlock());
        }

        world.getBlockTicks().copy(box, pos2);
        this.dataManager.set("cooldownLift", 20);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void setRotationAngles(HUSetRotationAnglesEvent event) {
        if (this.getEnabled()) {
            event.getPlayerModel().rightArm.xRot = event.getPlayerModel().leftArm.xRot = (float) Math.toRadians(180F);
        }
    }

    public static class BlockInfo {
        public final BlockPos pos;
        public final BlockState state;
        public final CompoundNBT tag;

        public BlockInfo(BlockPos pos, BlockState state, CompoundNBT nbt) {
            this.pos = pos;
            this.state = state;
            this.tag = nbt;
        }
    }
}