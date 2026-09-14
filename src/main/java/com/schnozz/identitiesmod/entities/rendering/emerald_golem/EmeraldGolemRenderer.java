package com.schnozz.identitiesmod.entities.rendering.emerald_golem;

import com.mojang.blaze3d.vertex.PoseStack;
import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.entities.custom_entities.DragonEntity;
import com.schnozz.identitiesmod.entities.custom_entities.EmeraldGolemEntity;
import com.schnozz.identitiesmod.entities.rendering.dragon.DragonModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class EmeraldGolemRenderer extends MobRenderer<EmeraldGolemEntity, EmeraldGolemModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "textures/entity/emerald_golem.png");

    public EmeraldGolemRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                // FIX: Removed the stray '.' and added a placeholder for your ModelLayers class
                new EmeraldGolemModel(context.bakeLayer(EmeraldGolemModel.LAYER_LOCATION)),
                0.7F // shadow size
        );
    }

    @Override
    public ResourceLocation getTextureLocation(EmeraldGolemEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(EmeraldGolemEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.4F, 1.4F, 1.4F);
    }
}