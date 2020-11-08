package chappie.theboys.client.models;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelCape extends Model {

	public ModelRenderer cape;
	public ModelRenderer utheh;

	public ModelCape() {
		super(RenderType::getEntityTranslucent);
		textureWidth = 32;
		textureHeight = 32;

		cape = new ModelRenderer(this);
		cape.setRotationPoint(0.0F, 0.0F, 2.0F);
		setRotationAngle(cape, 0.0F, 3.1416F, 0.0F);
		cape.setTextureOffset(0, 4).addBox(-7.0F, 0.0F, 0.0F, 14.0F, 24.0F, 0.0F, 0.0F, false);

		utheh = new ModelRenderer(this);
		utheh.setRotationPoint(0.0F, 0.0F, 0.0F);
		setRotationAngle(utheh, 0.0F, 3.1416F, 0.0F);
		utheh.setTextureOffset(0, 0).addBox(-5.0F, 0.0F, -2.0F, 10.0F, 0.0F, 4.0F, 0.0F, false);
	}

	@Override
	public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha){
		cape.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		utheh.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}