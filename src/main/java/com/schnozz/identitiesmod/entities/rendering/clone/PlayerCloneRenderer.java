package com.schnozz.identitiesmod.entities.rendering.clone;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.schnozz.identitiesmod.entities.custom_entities.PlayerCloneEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.concurrent.CompletableFuture;

@OnlyIn(Dist.CLIENT)
public class PlayerCloneRenderer
        extends HumanoidMobRenderer<PlayerCloneEntity, PlayerModel<PlayerCloneEntity>> {

    private static final ResourceLocation DEFAULT_STEVE =
            ResourceLocation.fromNamespaceAndPath("minecraft","textures/entity/player/wide/steve.png");

    private final PlayerModel<PlayerCloneEntity> wideModel;
    private final PlayerModel<PlayerCloneEntity> slimModel;

    public PlayerCloneRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false),
                0.5F
        );

        this.wideModel = new PlayerModel<>(
                context.bakeLayer(ModelLayers.PLAYER),
                false
        );
        this.slimModel = new PlayerModel<>(
                context.bakeLayer(ModelLayers.PLAYER_SLIM),
                true
        );
    }

    @Override
    public void render(
            PlayerCloneEntity entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight
    ) {
        this.model = entity.isSlim() ? slimModel : wideModel;
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(PlayerCloneEntity entity) {
        GameProfile profile = entity.getGameProfile();
        if (profile == null) {
            return DEFAULT_STEVE;
        }

        CompletableFuture<PlayerSkin> future =
                Minecraft.getInstance().getSkinManager().getOrLoad(profile);

        PlayerSkin skin = future.getNow(null);
        return skin != null ? skin.texture() : DEFAULT_STEVE;
    }
}