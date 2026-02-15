package com.schnozz.identitiesmod.entities.rendering.dragon;

import com.schnozz.identitiesmod.entities.custom_entities.DragonEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class DragonRenderer extends MobRenderer<DragonEntity, DragonModel<DragonEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("yourmodid",
                    "textures/entity/my_animal.png");

    public DragonRenderer(EntityRendererProvider.Context context) {
        super(context,
                new DragonModel<>(context.bakeLayer(DragonModel.LAYER_LOCATION)),
                0.5f
        );
    }

    @Override
    public ResourceLocation getTextureLocation(DragonEntity entity) {
        return TEXTURE;
    }
}
