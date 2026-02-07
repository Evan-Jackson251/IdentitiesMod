package com.schnozz.identitiesmod.entities.rendering.clone;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.schnozz.identitiesmod.entities.custom_entities.PlayerCloneEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlayerCloneRenderer extends EntityRenderer<PlayerCloneEntity> {

    private final PlayerRenderer steveRenderer;
    private final PlayerRenderer alexRenderer;

    public PlayerCloneRenderer(EntityRendererProvider.Context context) {
        super(context);

        this.steveRenderer = new PlayerRenderer(
                context,
                //new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false),
                false
        );
        this.alexRenderer = new PlayerRenderer(
                context,
                //new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true),
                false
        );
    }

    @Override
    public void render(
            PlayerCloneEntity entity,
            float yaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light
    ) {
        GameProfile profile = entity.getGameProfile();
        if (profile == null) return; // profile not set yet

        // Decide model type (Alex/Steve) based on a boolean stored in the entity
        boolean slimModel = entity.isSlim(); // you need to add this getter in your entity

        // Create a fake player to hold the skin
        AbstractClientPlayer fakePlayer = new AbstractClientPlayer(
                (ClientLevel) Minecraft.getInstance().level,
                profile
        ) {};
        fakePlayer.setYRot(entity.getYRot());
        fakePlayer.setXRot(entity.getXRot());

        fakePlayer.yBodyRot = entity.yBodyRot;
        fakePlayer.yBodyRotO = entity.yBodyRotO;
        fakePlayer.yHeadRot = entity.yHeadRot;
        fakePlayer.yHeadRotO = entity.yHeadRotO;
        fakePlayer.xRotO = entity.xRotO;

        fakePlayer.tickCount = entity.tickCount;

        // Delegate to the correct renderer
        (slimModel ? alexRenderer : steveRenderer)
                .render(fakePlayer, yaw, partialTicks, poseStack, buffer, light);
    }

    @Override
    public ResourceLocation getTextureLocation(PlayerCloneEntity entity) {
        return null; // handled internally by PlayerRenderer
    }
}
