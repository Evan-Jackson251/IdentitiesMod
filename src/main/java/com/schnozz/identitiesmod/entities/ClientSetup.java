package com.schnozz.identitiesmod.entities;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.entities.rendering.clone.PlayerCloneRenderer;
import com.schnozz.identitiesmod.entities.rendering.dragon.DragonModel;
import com.schnozz.identitiesmod.entities.rendering.dragon.DragonRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(
        modid = IdentitiesMod.MODID,
        bus = EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public final class ClientSetup {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.PLAYER_CLONE.get(), PlayerCloneRenderer::new);
        event.registerEntityRenderer(ModEntities.DRAGON.get(), DragonRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(
                DragonModel.LAYER_LOCATION,
                DragonModel::createBodyLayer
        );
    }
}