package com.schnozz.identitiesmod.entities.rendering.black_hole;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.entities.custom_entities.BlackHoleEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;

public class BlackHoleModel extends EntityModel<BlackHoleEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(
                    ResourceLocation.fromNamespaceAndPath(
                            IdentitiesMod.MODID, "black_hole"
                    ),
                    "main"
            );

    private final ModelPart root;

    public BlackHoleModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();

        // 16 model units = 1 block.
        // Origin is the entity's bottom center.
        mesh.getRoot().addOrReplaceChild(
                "cube",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, 0.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.ZERO
        );

        return LayerDefinition.create(mesh, 32, 16);
    }

    @Override
    public void setupAnim(
            BlackHoleEntity entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        // Static model.
    }

    @Override
    public void renderToBuffer(
            PoseStack poseStack,
            VertexConsumer buffer,
            int packedLight,
            int packedOverlay,
            int color
    ) {
        root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
