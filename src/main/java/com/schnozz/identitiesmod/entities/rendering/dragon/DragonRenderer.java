package com.schnozz.identitiesmod.entities.rendering.dragon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.entities.custom_entities.DragonEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class DragonRenderer extends MobRenderer<DragonEntity, DragonModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/enderdragon/dragon.png");

    public DragonRenderer(EntityRendererProvider.Context context) {
        super(context,
                new DragonModel(context.bakeLayer(DragonModel.LAYER_LOCATION)),
                1.5F);
    }
    @Override
    protected void scale(DragonEntity entity, PoseStack poseStack, float partialTickTime) {
        float scale = 3F;
        poseStack.scale(scale, scale, scale);
    }
    @Override
    public ResourceLocation getTextureLocation(DragonEntity entity) {
        return TEXTURE;
    }
}
