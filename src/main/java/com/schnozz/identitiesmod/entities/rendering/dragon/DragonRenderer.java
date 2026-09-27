package com.schnozz.identitiesmod.entities.rendering.dragon;

import com.mojang.blaze3d.vertex.PoseStack;

import com.schnozz.identitiesmod.entities.custom_entities.DragonEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

public class DragonRenderer
        extends MobRenderer<DragonEntity, DragonModel> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    "minecraft",
                    "textures/entity/enderdragon/dragon.png"
            );

    private static final RenderType EYES =
            RenderType.eyes(ResourceLocation.fromNamespaceAndPath(
                    "minecraft",
                    "textures/entity/enderdragon/dragon_eyes.png"
            ));

    public DragonRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new DragonModel(
                        context.bakeLayer(ModelLayers.ENDER_DRAGON)
                ),
                1.5F
        );

        addLayer(new EyesLayer<DragonEntity, DragonModel>(this) {
            @Override
            public RenderType renderType() {
                return EYES;
            }
        });
    }

    @Override
    protected void scale(
            DragonEntity entity,
            PoseStack poseStack,
            float partialTickTime
    ) {
        // No 3x scale: the vanilla geometry already has vanilla dimensions.
        // This translation only raises the folded flight pose.
        poseStack.translate(0.0F, -2.0F, 0.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(DragonEntity entity) {
        return TEXTURE;
    }
}
