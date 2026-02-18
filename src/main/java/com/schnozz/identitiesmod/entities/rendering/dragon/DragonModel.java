package com.schnozz.identitiesmod.entities.rendering.dragon;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.entities.custom_entities.DragonEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class DragonModel extends HierarchicalModel<DragonEntity> {

    // Make sure "identities_mod" matches your actual MOD_ID in lowercase
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "dragon"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart leftWing;
    private final ModelPart leftWingTip;
    private final ModelPart rightWing;
    private final ModelPart rightWingTip;

    private final ModelPart[] neckParts = new ModelPart[5];
    private final ModelPart[] tailParts = new ModelPart[12];

    public DragonModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");

        // Hierarchy Fix: neck_0 is now a child of body
        ModelPart currentNeck = this.body.getChild("neck_0");
        this.neckParts[0] = currentNeck;
        for (int i = 1; i < 5; i++) {
            currentNeck = currentNeck.getChild("neck_" + i);
            this.neckParts[i] = currentNeck;
        }

        this.head = currentNeck.getChild("head");
        this.jaw = head.getChild("jaw");

        this.leftWing = body.getChild("left_wing");
        this.leftWingTip = leftWing.getChild("left_wing_tip");
        this.rightWing = body.getChild("right_wing");
        this.rightWingTip = rightWing.getChild("right_wing_tip");

        ModelPart currentTail = body.getChild("tail_0");
        this.tailParts[0] = currentTail;
        for (int i = 1; i < 12; i++) {
            currentTail = currentTail.getChild("tail_" + i);
            this.tailParts[i] = currentTail;
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // BODY - Central anchor
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 40).addBox(-12.0F, 0.0F, -16.0F, 24, 24, 64),
                PartPose.offset(0.0F, 4.0F, 8.0F));

        // NECK - Parented to BODY to prevent disconnection
        // Offset (0, 14, -14) places it at the front-center of the body
        PartDefinition lastNeck = body.addOrReplaceChild("neck_0",
                CubeListBuilder.create().texOffs(192, 104).addBox(-5.0F, -5.0F, -5.0F, 10, 10, 10),
                PartPose.offset(0.0F, 14.0F, -14.0F));

        for (int i = 1; i < 5; i++) {
            lastNeck = lastNeck.addOrReplaceChild("neck_" + i,
                    CubeListBuilder.create().texOffs(192, 104).addBox(-5.0F, -5.0F, -10.0F, 10, 10, 10),
                    PartPose.offset(0.0F, 0.0F, -8.0F));
        }

        // HEAD - Attached to the end of the neck chain
        PartDefinition head = lastNeck.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(176, 44).addBox(-6.0F, -1.0F, -24.0F, 12, 5, 16) // Snout
                        .texOffs(112, 30).addBox(-8.0F, -8.0F, -10.0F, 16, 16, 16) // Main Head
                        .texOffs(0, 0).addBox(-5.0F, -12.0F, -4.0F, 2, 4, 6) // Left Horn
                        .texOffs(0, 0).mirror().addBox(3.0F, -12.0F, -4.0F, 2, 4, 6), // Right Horn
                PartPose.offset(0.0F, 0.0F, -10.0F));

        head.addOrReplaceChild("jaw",
                CubeListBuilder.create().texOffs(176, 65).addBox(-6.0F, 0.0F, -16.0F, 12, 4, 16),
                PartPose.offset(0.0F, 4.0F, -8.0F));

        // WINGS - Using Vanilla dragon.png mapping
        // Left Wing
        PartDefinition leftWing = body.addOrReplaceChild("left_wing",
                CubeListBuilder.create()
                        .texOffs(112, 88).addBox(0.0F, -4.0F, -4.0F, 56, 8, 32) // Bone
                        .texOffs(0, 88).addBox(0.0F, 0.0F, 2.0F, 56, 0, 56),    // Membrane
                PartPose.offset(12.0F, 4.0F, 2.0F));

        leftWing.addOrReplaceChild("left_wing_tip",
                CubeListBuilder.create()
                        .texOffs(112, 0).addBox(0.0F, -2.0F, 0.0F, 56, 4, 32)   // Bone Tip
                        .texOffs(0, 88).addBox(0.0F, 0.0F, 2.0F, 56, 0, 56),    // Membrane Tip
                PartPose.offset(56.0F, 0.0F, 0.0F));

        // Right Wing (Mirrored)
        PartDefinition rightWing = body.addOrReplaceChild("right_wing",
                CubeListBuilder.create()
                        .texOffs(112, 88).mirror().addBox(-56.0F, -4.0F, -4.0F, 56, 8, 32)
                        .texOffs(0, 88).mirror().addBox(-56.0F, 0.0F, 2.0F, 56, 0, 56),
                PartPose.offset(-12.0F, 4.0F, 2.0F));

        rightWing.addOrReplaceChild("right_wing_tip",
                CubeListBuilder.create()
                        .texOffs(112, 0).mirror().addBox(-56.0F, -2.0F, 0.0F, 56, 4, 32)
                        .texOffs(0, 88).mirror().addBox(-56.0F, 0.0F, 2.0F, 56, 0, 56),
                PartPose.offset(-56.0F, 0.0F, 0.0F));

        // TAIL - Parented to BODY
        PartDefinition lastTail = body.addOrReplaceChild("tail_0",
                CubeListBuilder.create().texOffs(192, 104).addBox(-5.0F, -5.0F, 0.0F, 10, 10, 10),
                PartPose.offset(0.0F, 10.0F, 45.0F));

        for (int i = 1; i < 12; i++) {
            lastTail = lastTail.addOrReplaceChild("tail_" + i,
                    CubeListBuilder.create().texOffs(192, 104).addBox(-5.0F, -5.0F, 0.0F, 10, 10, 10),
                    PartPose.offset(0.0F, 0.0F, 10.0F));
        }

        return LayerDefinition.create(mesh, 256, 256);
    }

    @Override
    public void setupAnim(DragonEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float flap = ageInTicks * 0.2F;

        // Wing animation
        this.leftWing.zRot = (Mth.sin(flap) + 0.5F) * 0.8F;
        this.leftWingTip.zRot = (Mth.sin(flap - 0.5F) + 0.5F) * 0.7F;
        this.rightWing.zRot = -this.leftWing.zRot;
        this.rightWingTip.zRot = -this.leftWingTip.zRot;

        // Neck swaying
        for (int i = 0; i < neckParts.length; i++) {
            neckParts[i].yRot = Mth.cos(ageInTicks * 0.15F + (i * 0.4F)) * 0.05F;
            neckParts[i].xRot = (headPitch * (Mth.PI / 180F)) / neckParts.length;
        }

        this.head.yRot = netHeadYaw * (Mth.PI / 180F);
        this.jaw.xRot = (Mth.sin(ageInTicks * 0.1F) + 1.0F) * 0.2F;

        // Tail swaying
        for (int i = 0; i < tailParts.length; i++) {
            tailParts[i].yRot = Mth.sin(ageInTicks * 0.1F + (i * 0.2F)) * 0.1F;
        }
    }

    @Override
    public ModelPart root() {
        return root;
    }
}
