package chappie.theboys.common;

import chappie.modulus.util.CommonUtil;
import chappie.theboys.TheBoys;
import chappie.theboys.common.ability.DamageImmunityAbility;
import chappie.theboys.common.ability.FlightAbility;
import chappie.theboys.common.capability.TheBoysCap;
import chappie.theboys.common.capability.TheBoysCapProvider;
import chappie.theboys.common.item.suit.SuitItem;
import chappie.theboys.networking.TBNetworking;
import chappie.theboys.networking.client.ClientSyncTheBoysCap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkDirection;

public class CommonEvents {

    @SubscribeEvent
    public void livingTick(LivingEvent.LivingTickEvent event) {
        TheBoysCap cap = TheBoysCap.getCap(event.getEntity());
        if (cap != null) {
            cap.tick();
        }
    }

    @SubscribeEvent
    public void itemAttributeModifier(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof ArmorItem armorItem && armorItem.getEquipmentSlot() == event.getSlotType()) {
            if (stack.getOrCreateTag().contains("Suit")) {
                CompoundTag tag = stack.getOrCreateTag().getCompound("Suit");
                if (ItemStack.of(tag.getCompound("Tags")).getItem() instanceof SuitItem item
                        && armorItem.getEquipmentSlot() == item.properties.getSlot()) {
                    item.properties.defaultModifiers().forEach(event::addModifier);
                }
            }
        }
    }

    @SubscribeEvent
    public void entitySize(EntityEvent.Size event) {
        Entity entity = event.getEntity();
        if (entity != null && entity.isAddedToWorld()) {
            for (FlightAbility ability : CommonUtil.listOfType(FlightAbility.class, CommonUtil.getAbilities(entity))) {
                if (entity.isSprinting() && ability.isEnabled()) {
                    event.setNewSize(EntityDimensions.scalable(0.6F, 0.6F));
                    event.setNewEyeHeight(0.4F);
                }
            }
        }
    }

    @SubscribeEvent
    public void livingFall(LivingFallEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level instanceof ServerLevel level) {
            for (FlightAbility a : CommonUtil.listOfType(FlightAbility.class, CommonUtil.getAbilities(entity))) {
                if (a.dataManager.get(FlightAbility.BREAK_BLOCKS) && event.getDistance() > 20) {
                    for (int x = 0; x < 5; x++) {
                        for (int y = 0; y < 5; y++) {
                            for (int z = 0; z < 5; z++) {
                                double xPos = entity.getX() - 2.5 + x + entity.level.random.nextInt(5);
                                double yPos = entity.getY() - 2.5 + y + entity.level.random.nextInt(5);
                                double zPos = entity.getZ() - 2.5 + z + entity.level.random.nextInt(5);
                                BlockPos pos = new BlockPos((int) xPos, (int) yPos, (int) zPos);
                                Block block = entity.level.getBlockState(pos).getBlock();

                                if (block == Blocks.STONE) {
                                    entity.level.setBlockAndUpdate(pos, Blocks.COBBLESTONE.defaultBlockState());
                                } else if (block == Blocks.STONE_BRICKS) {
                                    entity.level.setBlockAndUpdate(pos, Blocks.CRACKED_STONE_BRICKS.defaultBlockState());
                                } else if (block == Blocks.COBBLESTONE) {
                                    entity.level.setBlockAndUpdate(pos, Blocks.GRAVEL.defaultBlockState());
                                } else if (block == Blocks.GRASS_BLOCK) {
                                    entity.level.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
                                } else if (block == Blocks.DIRT) {
                                    entity.level.setBlockAndUpdate(pos, Blocks.COARSE_DIRT.defaultBlockState());
                                } else if (block == Blocks.OAK_LOG) {
                                    entity.level.setBlockAndUpdate(pos, Blocks.STRIPPED_OAK_LOG.defaultBlockState());
                                } else if (block == Blocks.BIRCH_LOG) {
                                    entity.level.setBlockAndUpdate(pos, Blocks.STRIPPED_BIRCH_LOG.defaultBlockState());
                                } else if (block == Blocks.SPRUCE_LOG) {
                                    entity.level.setBlockAndUpdate(pos, Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState());
                                } else if (block == Blocks.JUNGLE_LOG) {
                                    entity.level.setBlockAndUpdate(pos, Blocks.STRIPPED_JUNGLE_LOG.defaultBlockState());
                                } else if (block == Blocks.DARK_OAK_LOG) {
                                    entity.level.setBlockAndUpdate(pos, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
                                } else if (block == Blocks.ACACIA_LOG) {
                                    entity.level.setBlockAndUpdate(pos, Blocks.STRIPPED_ACACIA_LOG.defaultBlockState());
                                } else if (block == Blocks.GLASS) {
                                    entity.level.destroyBlock(pos, false);
                                }

                                for (ServerPlayer serverPlayer : level.getEntitiesOfClass(ServerPlayer.class, CommonUtil.boxWithRange(entity.position(), 30))) {
                                    level.sendParticles(serverPlayer, ParticleTypes.EXPLOSION, false, entity.getX(), entity.getY() + 0.25F, entity.getZ(), 0, (event.getDistance() / entity.level.getHeight()) * 10, 0.0D, 0.0D, 1F);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void livingAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player) {
            for (DamageImmunityAbility a : CommonUtil.listOfType(DamageImmunityAbility.class, CommonUtil.getAbilities(event.getEntity()))) {
                for (String s : a.damageSources) {
                    if (s.equals(event.getSource().getMsgId()) && a.isEnabled()) {
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void attachCap(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity entity) {
            event.addCapability(new ResourceLocation(TheBoys.MODID, "capability"), new TheBoysCapProvider(entity));
        }
    }

    @SubscribeEvent
    public void clonePlayer(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            event.getOriginal().reviveCaps();
            event.getEntity().getCapability(TheBoysCap.CAPABILITY).ifPresent(cap -> {
                event.getOriginal().getCapability(TheBoysCap.CAPABILITY).ifPresent(oldCap ->
                        cap.deserializeNBT(oldCap.serializeNBT()));
            });
            event.getOriginal().invalidateCaps();
        }
    }

    @SubscribeEvent
    public void onStartTracking(PlayerEvent.StartTracking e) {
        e.getTarget().getCapability(TheBoysCap.CAPABILITY).ifPresent(a -> {
            if (e.getEntity() instanceof ServerPlayer player) {
                TBNetworking.INSTANCE.sendTo(new ClientSyncTheBoysCap(e.getTarget().getId(), a.serializeNBT()), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
            }
        });
    }

    @SubscribeEvent
    public void onJoinWorld(EntityJoinLevelEvent e) {
        if (e.getEntity() instanceof ServerPlayer player) {
            player.getCapability(TheBoysCap.CAPABILITY).ifPresent(TheBoysCap::syncToAll);
        }
    }
}
