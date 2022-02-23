package chappie.theboys.abilities;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import xyz.heroesunited.heroesunited.common.abilities.Ability;
import xyz.heroesunited.heroesunited.common.abilities.AbilityType;
import xyz.heroesunited.heroesunited.common.abilities.IAbilityClientProperties;
import xyz.heroesunited.heroesunited.common.capabilities.HUPlayer;
import xyz.heroesunited.heroesunited.util.HUClientUtil;
import xyz.heroesunited.heroesunited.util.HUPlayerUtil;

import java.awt.*;
import java.util.Map;
import java.util.function.Consumer;

public class StarLightAbility extends Ability {

    public StarLightAbility(AbilityType type, Player player, @NotNull JsonObject jsonObject) {
        super(type, player, jsonObject);
    }

    @Override
    public void registerData() {
        super.registerData();
        this.dataManager.register("enabled", false);
        this.dataManager.register("energy_eated", 0);
        this.dataManager.register("ticks", 0);
    }

    @Override
    public void onUpdate(Player player) {
        super.onUpdate(player);
        if (this.dataManager.<Boolean>getValue("enabled")) {
            this.dataManager.set("ticks", this.dataManager.<Integer>getValue("ticks") + 1);
        } else this.dataManager.set("ticks", 0);

        if (this.dataManager.<Integer>getValue("ticks") >= 1200 && this.dataManager.<Integer>getValue("energy_eated") > 50) {
            this.dataManager.set("energy_eated", this.dataManager.<Integer>getValue("energy_eated") - 50);
            this.dataManager.set("ticks", this.dataManager.<Integer>getValue("ticks") - 1200);
        }

        if (this.dataManager.<Integer>getValue("ticks") >= 6000) {
            this.dataManager.set("enabled", false);
        }

        if (this.dataManager.<Integer>getValue("energy_eated") > 10 && !this.dataManager.<Boolean>getValue("enabled")) {
            this.dataManager.set("enabled", true);
            this.dataManager.set("energy_eated", this.dataManager.<Integer>getValue("energy_eated") - 10);
        }
    }

    @Override
    public void onKeyInput(Player player, Map<Integer, Boolean> map) {
        super.onKeyInput(player, map);
        if (map.get(5)) {
            BlockPos.betweenClosedStream(HUPlayerUtil.getCollisionBoxWithRange(HUPlayerUtil.getPlayerPos(player), 5)).forEach(pos -> {
                if (!player.level.isClientSide) {
                    BlockState state = player.level.getBlockState(pos);
                    BooleanProperty property = state.getBlock() instanceof RedstoneLampBlock ? RedstoneLampBlock.LIT : BlockStateProperties.POWERED;
                    IntegerProperty property1 = BlockStateProperties.POWER;
                    if (state.getOptionalValue(property1).isPresent() && state.getOptionalValue(property1).get() != 0) {
                        player.level.setBlock(pos, state.setValue(property1, 0), 2);
                        this.dataManager.set("energy_eated", this.dataManager.<Integer>getValue("energy_eated") + 1);
                    }
                    if (state.getOptionalValue(property).isPresent() && state.getOptionalValue(property).get()) {
                        player.level.setBlock(pos, state.setValue(property, false), 2);
                        this.dataManager.set("energy_eated", this.dataManager.<Integer>getValue("energy_eated") + 1);
                    }
                    HUPlayer.getCap(player).sync();
                }
            });
        }
    }

    @Override
    public void initializeClient(Consumer<IAbilityClientProperties> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new IAbilityClientProperties() {
            @Override
            public void render(EntityRendererProvider.Context context, PlayerRenderer renderer, PoseStack poseStack, MultiBufferSource bufferIn, int packedLightIn, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
                if (getDataManager().<Boolean>getValue("enabled")) {
                    for (HumanoidArm side : HumanoidArm.values()) {
                        float r = 0.15F;
                        float i = getDataManager().<Integer>getValue("ticks") <= 1200 ? 1f : 0.5F;
                        AABB box = new AABB(-r, -r, -r, r, r, r);
                        poseStack.pushPose();
                        renderer.getModel().translateToHand(side, poseStack);
                        poseStack.translate(side == HumanoidArm.LEFT ? 0.06 : -0.06, 0.55, 0);
                        HUClientUtil.renderAura(poseStack, bufferIn.getBuffer(HUClientUtil.HURenderTypes.LASER), box, 0.025F, new Color(i, i, 0, i), packedLightIn, player.tickCount);
                        poseStack.popPose();
                    }
                }
            }
        });
    }
}
