package chappie.theboys.client.model;

import chappie.modulus.Modulus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.joml.Quaternionf;

public class CapeModel extends Model<HumanoidRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Modulus.id("cape"), "main");

	private final ModelPart cape = this.root.getChild("cape");

	public CapeModel() {
		super(Minecraft.getInstance().getEntityModels().bakeLayer(CapeModel.LAYER_LOCATION).getChild("main"), RenderType::entityTranslucent);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition main = partdefinition.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 0).addBox(-4F, -24.4F, -1.575F, 8.0F, 1.0F, 3.0F, new CubeDeformation(0.3F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		main.addOrReplaceChild("cape", CubeListBuilder.create().texOffs(0, 5).addBox(-7F, -0.25F, -0.5F, 14.0F, 24.0F, 1.0F, new CubeDeformation(0)), PartPose.offset(0.0F, -24.0F, 2.5F));

		return LayerDefinition.create(meshdefinition, 64, 32);
	}

	@Override
	public void setupAnim(HumanoidRenderState renderState) {
		if (renderState instanceof AvatarRenderState playerRenderState) {
			this.cape.rotateBy(
					new Quaternionf()
							.rotateX((6.0F + playerRenderState.capeLean / 2.0F + playerRenderState.capeFlap) * (float) (Math.PI / 180.0))
							.rotateZ(playerRenderState.capeLean2 / 2.0F * (float) (Math.PI / 180.0))
							.rotateY((180.0F - playerRenderState.capeLean2 / 2.0F) * (float) (Math.PI / 180.0))
			);
		}
	}
}
