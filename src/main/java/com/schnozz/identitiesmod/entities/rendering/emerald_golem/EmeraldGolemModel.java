package com.schnozz.identitiesmod.entities.rendering.emerald_golem;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.entities.custom_entities.EmeraldGolemEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class EmeraldGolemModel extends HierarchicalModel<EmeraldGolemEntity> {

    // Added: The ModelLayerLocation directly in the model!
    // Make sure IdentitiesMod.MODID is properly imported or fully qualified here.
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "emerald_golem"),
            "main"
    );

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public EmeraldGolemModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4F, 0F, -2F, 8, 12, 4),
                PartPose.offset(0F, 0F, 0F));

        root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 16)
                        .addBox(-4F, -8F, -4F, 8, 8, 8),
                PartPose.offset(0F, 0F, 0F));

        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(24, 0)
                        .addBox(-3F, -2F, -2F, 4, 12, 4),
                PartPose.offset(-5F, 2F, 0F));

        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(24, 0)
                        .mirror()
                        .addBox(-1F, -2F, -2F, 4, 12, 4),
                PartPose.offset(5F, 2F, 0F));

        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 32)
                        .addBox(-2F, 0F, -2F, 4, 12, 4),
                PartPose.offset(-2F, 12F, 0F));

        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(0, 32)
                        .mirror()
                        .addBox(-2F, 0F, -2F, 4, 12, 4),
                PartPose.offset(2F, 12F, 0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(EmeraldGolemEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Renamed the v, v1, v2 variables so we know exactly what we are animating!

        // Head rotation looking around
        this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
        this.head.xRot = headPitch * ((float)Math.PI / 180F);

        // Legs swinging opposite of each other
        this.rightLeg.xRot = Mth.cos(limbSwing) * 1.4F * limbSwingAmount;
        this.leftLeg.xRot = Mth.cos(limbSwing + (float)Math.PI) * 1.4F * limbSwingAmount;

        // Arms swinging opposite to the legs (standard Minecraft biped walking)
        this.rightArm.xRot = this.leftLeg.xRot;
        this.leftArm.xRot = this.rightLeg.xRot;
    }
}
