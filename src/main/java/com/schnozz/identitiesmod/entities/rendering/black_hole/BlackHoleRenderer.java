package com.schnozz.identitiesmod.entities.rendering.black_hole;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.schnozz.identitiesmod.entities.custom_entities.BlackHoleEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class BlackHoleRenderer extends EntityRenderer<BlackHoleEntity> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.withDefaultNamespace(
                    "textures/block/white_concrete.png"
            );

    private final BlackHoleModel model;

    public BlackHoleRenderer(EntityRendererProvider.Context context) {
        super(context);

        model = new BlackHoleModel(
                context.bakeLayer(BlackHoleModel.LAYER_LOCATION)
        );

        shadowRadius = 0.0F;
    }

    @Override
    public void render(
            BlackHoleEntity entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight
    ) {
        if(entity.isInvisible()){
            return;
        }
        VertexConsumer buffer = bufferSource.getBuffer(
                RenderType.entitySolid(TEXTURE)
        );

        model.renderToBuffer(
                poseStack,
                buffer,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                0xFF000000 // Fully opaque black.
        );

        super.render(
                entity, entityYaw, partialTick,
                poseStack, bufferSource, packedLight
        );
    }

    @Override
    public ResourceLocation getTextureLocation(BlackHoleEntity entity) {
        return TEXTURE;
    }
}
