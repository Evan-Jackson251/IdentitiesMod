package com.schnozz.identitiesmod.entities.rendering.dragon;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.schnozz.identitiesmod.entities.custom_entities.DragonEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class DragonModel extends HierarchicalModel<DragonEntity> {
    private final ModelPart root;
    private final ModelPart vanilla;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart leftWing;
    private final ModelPart rightWing;

    private final ModelPart[] neck = new ModelPart[5];
    private final ModelPart[] tail = new ModelPart[12];

    public DragonModel(ModelPart bakedVanillaRoot) {
        super(RenderType::entityCutoutNoCull);

        this.vanilla = bakedVanillaRoot;
        this.head = vanilla.getChild("head");
        this.jaw = head.getChild("jaw");
        this.leftWing = vanilla.getChild("left_wing");
        this.rightWing = vanilla.getChild("right_wing");

        // The vanilla dragon reuses one mesh for every neck/tail segment.
        // Create independently posed copies for this hierarchical model.
        vanilla.getChild("neck").visible = false;

        Map<String, ModelPart> children = new LinkedHashMap<>();
        children.put("vanilla", vanilla);

        LayerDefinition layer = EnderDragonRenderer.createBodyLayer();

        for (int i = 0; i < neck.length; i++) {
            neck[i] = layer.bakeRoot().getChild("neck");
            children.put("neck_" + i, neck[i]);
        }

        for (int i = 0; i < tail.length; i++) {
            tail[i] = layer.bakeRoot().getChild("neck");
            children.put("tail_" + i, tail[i]);
        }

        this.root = new ModelPart(List.of(), children);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(
            DragonEntity entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        root.getAllParts().forEach(ModelPart::resetPose);

        float phase = entity.shouldAnimateFlight()
                ? ageInTicks * 0.2F
                : 0.0F;
        float wave = Mth.sin(phase);

        // Jaw
        jaw.xRot = (wave + 1.0F) * 0.2F;

        // Wings
        leftWing.xRot = 0.125F - Mth.cos(phase) * 0.2F;
        leftWing.yRot = -0.25F;
        leftWing.zRot = -(wave + 0.125F) * 0.8F;

        leftWing.getChild("left_wing_tip").zRot =
                (Mth.sin(phase + 2.0F) + 0.5F) * 0.75F;

        rightWing.xRot = leftWing.xRot;
        rightWing.yRot = -leftWing.yRot;
        rightWing.zRot = -leftWing.zRot;

        rightWing.getChild("right_wing_tip").zRot =
                -leftWing.getChild("left_wing_tip").zRot;

        // Folded flight pose
        poseLeg("left", "front", 1.3F, -0.5F, 0.75F);
        poseLeg("right", "front", 1.3F, -0.5F, 0.75F);
        poseLeg("left", "hind", 1.0F, 0.5F, 0.75F);
        poseLeg("right", "hind", 1.0F, 0.5F, 0.75F);

        // Neck
        boolean riddenByPlayer =
                entity.getControllingPassenger() instanceof Player;

        float x = 0.0F;
        float y = 20.0F;
        float z = -12.0F;

        float yaw = riddenByPlayer
                ? 0.0F
                : Mth.clamp(netHeadYaw, -60.0F, 60.0F) * Mth.DEG_TO_RAD;

        float pitch = Mth.clamp(
                headPitch,
                -85.0F,
                85.0F
        ) * Mth.DEG_TO_RAD;

        for (int i = 0; i < neck.length; i++) {
            ModelPart segment = neck[i];

            segment.setPos(x, y, z);

            if (riddenByPlayer) {
                // Stable neck: no idle motion pulling the head away from the camera.
                segment.yRot = 0.0F;
                segment.xRot = 0.0F;
                segment.zRot = 0.0F;
            } else {
                segment.yRot = yaw * (i + 1.0F) / neck.length;
                segment.xRot =
                        pitch + Mth.cos(phase + i * 0.45F) * 0.15F;
            }

            x -= Mth.sin(segment.yRot)
                    * Mth.cos(segment.xRot) * 10.0F;

            y += Mth.sin(segment.xRot) * 10.0F;

            z -= Mth.cos(segment.yRot)
                    * Mth.cos(segment.xRot) * 10.0F;
        }

        head.setPos(x, y, z);
        head.yRot = yaw;
        head.xRot = pitch;
        head.zRot = 0.0F;

        // Tail
        x = 0.0F;
        y = 10.0F;
        z = 60.0F;

        float tailPitch = 0.0F;

        for (int i = 0; i < tail.length; i++) {
            ModelPart segment = tail[i];

            segment.setPos(x, y, z);

            tailPitch += Mth.sin(phase + i * 0.45F) * 0.05F;
            segment.xRot = tailPitch;
            segment.yRot = Mth.PI;

            y += Mth.sin(tailPitch) * 10.0F;
            z += Mth.cos(tailPitch) * 10.0F;
        }

        Minecraft minecraft = Minecraft.getInstance();

        boolean hideHeadForRider =
                minecraft.player != null
                        && minecraft.getCameraEntity() == minecraft.player
                        && minecraft.options.getCameraType().isFirstPerson()
                        && entity.getControllingPassenger() == minecraft.player;

// Hiding the head also hides its child jaw and the eye-layer geometry.
        head.visible = !hideHeadForRider;

        for (ModelPart segment : neck) {
            segment.visible = !hideHeadForRider;
        }
    }

    private void poseLeg(
            String side,
            String position,
            float upperAngle,
            float lowerAngle,
            float footAngle
    ) {
        String prefix = side + "_" + position;

        ModelPart leg = vanilla.getChild(prefix + "_leg");
        ModelPart tip = leg.getChild(prefix + "_leg_tip");

        leg.xRot = upperAngle;
        tip.xRot = lowerAngle;
        tip.getChild(prefix + "_foot").xRot = footAngle;
    }
}
