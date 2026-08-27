package chappie.theboys.client.renderer;

import chappie.theboys.TheBoys;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

public final class LaserRenderTypes {

    private static final RenderPipeline LASER_CORE_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
                    .withLocation(TheBoys.id("pipeline/laser_core"))
                    .withVertexShader("core/rendertype_lightning")
                    .withFragmentShader("core/rendertype_lightning")
                    .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                    .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
                    .withCull(false)
                    .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
                    .withPrimitiveTopology(PrimitiveTopology.QUADS)
                    .build()
    );

    private static final RenderPipeline LASER_GLOW_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
                    .withLocation(TheBoys.id("pipeline/laser_glow"))
                    .withVertexShader("core/rendertype_lightning")
                    .withFragmentShader("core/rendertype_lightning")
                    .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                    .withDepthStencilState(DepthStencilState.DEFAULT)
                    .withCull(false)
                    .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
                    .withPrimitiveTopology(PrimitiveTopology.QUADS)
                    .build()
    );

    // ===== PIP (GUI) laser pipelines - no depth test =====

    private static final RenderPipeline PIP_LASER_CORE_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
                    .withLocation(TheBoys.id("pipeline/pip_laser_core"))
                    .withVertexShader("core/rendertype_lightning")
                    .withFragmentShader("core/rendertype_lightning")
                    .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                    .withDepthStencilState(DepthStencilState.DEFAULT)
                    .withCull(false)
                    .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
                    .withPrimitiveTopology(PrimitiveTopology.QUADS)
                    .build()
    );

    private static final RenderPipeline PIP_LASER_GLOW_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
                    .withLocation(TheBoys.id("pipeline/pip_laser_glow"))
                    .withVertexShader("core/rendertype_lightning")
                    .withFragmentShader("core/rendertype_lightning")
                    .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
                    .withDepthStencilState(new DepthStencilState(com.mojang.blaze3d.platform.CompareOp.ALWAYS_PASS, false))
                    .withCull(false)
                    .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
                    .withPrimitiveTopology(PrimitiveTopology.QUADS)
                    .build()
    );

    public static final RenderType LASER_GLOW = RenderType.create(
            TheBoys.MODID + ":laser_core",
            RenderSetup.builder(LASER_CORE_PIPELINE)
                    .setOutputTarget(OutputTarget.WEATHER_TARGET)
                    .createRenderSetup()
    );

    public static final RenderType LASER_CORE = RenderType.create(
            TheBoys.MODID + ":laser_glow",
            RenderSetup.builder(LASER_GLOW_PIPELINE)
                    .setOutputTarget(OutputTarget.WEATHER_TARGET)
                    .createRenderSetup()
    );

    public static final RenderType PIP_LASER_CORE = RenderType.create(
            TheBoys.MODID + ":pip_laser_core",
            RenderSetup.builder(PIP_LASER_CORE_PIPELINE).createRenderSetup()
    );

    public static final RenderType PIP_LASER_GLOW = RenderType.create(
            TheBoys.MODID + ":pip_laser_glow",
            RenderSetup.builder(PIP_LASER_GLOW_PIPELINE).createRenderSetup()
    );

    public static void renderLaserBox(Matrix4f pose, VertexConsumer builder, AABB box, float red, float green, float blue, float alpha) {
        // Top face
        builder.addVertex(pose, (float) box.minX, (float) box.maxY, (float) box.minZ).setColor(red, green, blue, alpha);
        builder.addVertex(pose, (float) box.minX, (float) box.maxY, (float) box.maxZ).setColor(red, green, blue, alpha);
        builder.addVertex(pose, (float) box.maxX, (float) box.maxY, (float) box.maxZ).setColor(red, green, blue, alpha);
        builder.addVertex(pose, (float) box.maxX, (float) box.maxY, (float) box.minZ).setColor(red, green, blue, alpha);

        // Bottom face
        builder.addVertex(pose, (float) box.minX, (float) box.minY, (float) box.minZ).setColor(red, green, blue, alpha);
        builder.addVertex(pose, (float) box.maxX, (float) box.minY, (float) box.minZ).setColor(red, green, blue, alpha);
        builder.addVertex(pose, (float) box.maxX, (float) box.minY, (float) box.maxZ).setColor(red, green, blue, alpha);
        builder.addVertex(pose, (float) box.minX, (float) box.minY, (float) box.maxZ).setColor(red, green, blue, alpha);

        // North face
        builder.addVertex(pose, (float) box.minX, (float) box.minY, (float) box.minZ).setColor(red, green, blue, alpha);
        builder.addVertex(pose, (float) box.minX, (float) box.maxY, (float) box.minZ).setColor(red, green, blue, alpha);
        builder.addVertex(pose, (float) box.maxX, (float) box.maxY, (float) box.minZ).setColor(red, green, blue, alpha);
        builder.addVertex(pose, (float) box.maxX, (float) box.minY, (float) box.minZ).setColor(red, green, blue, alpha);

        // South face
        builder.addVertex(pose, (float) box.minX, (float) box.minY, (float) box.maxZ).setColor(red, green, blue, alpha);
        builder.addVertex(pose, (float) box.maxX, (float) box.minY, (float) box.maxZ).setColor(red, green, blue, alpha);
        builder.addVertex(pose, (float) box.maxX, (float) box.maxY, (float) box.maxZ).setColor(red, green, blue, alpha);
        builder.addVertex(pose, (float) box.minX, (float) box.maxY, (float) box.maxZ).setColor(red, green, blue, alpha);

        // East face
        builder.addVertex(pose, (float) box.maxX, (float) box.minY, (float) box.minZ).setColor(red, green, blue, alpha);
        builder.addVertex(pose, (float) box.maxX, (float) box.maxY, (float) box.minZ).setColor(red, green, blue, alpha);
        builder.addVertex(pose, (float) box.maxX, (float) box.maxY, (float) box.maxZ).setColor(red, green, blue, alpha);
        builder.addVertex(pose, (float) box.maxX, (float) box.minY, (float) box.maxZ).setColor(red, green, blue, alpha);

        // West face
        builder.addVertex(pose, (float) box.minX, (float) box.minY, (float) box.minZ).setColor(red, green, blue, alpha);
        builder.addVertex(pose, (float) box.minX, (float) box.minY, (float) box.maxZ).setColor(red, green, blue, alpha);
        builder.addVertex(pose, (float) box.minX, (float) box.maxY, (float) box.maxZ).setColor(red, green, blue, alpha);
        builder.addVertex(pose, (float) box.minX, (float) box.maxY, (float) box.minZ).setColor(red, green, blue, alpha);
    }
}
