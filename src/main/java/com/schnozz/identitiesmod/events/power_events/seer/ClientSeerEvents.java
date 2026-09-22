package com.schnozz.identitiesmod.events.power_events.seer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.cooldown.Cooldown;
import com.schnozz.identitiesmod.cooldown.CooldownAttachment;
import com.schnozz.identitiesmod.icons.CooldownIcon;
import com.schnozz.identitiesmod.networking.payloads.EffectAddPayload;
import com.schnozz.identitiesmod.networking.payloads.sync_payloads.CooldownSyncPayload;
import com.schnozz.identitiesmod.screen.SeerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Matrix4f;
import java.util.ArrayList;
import java.util.List;
import static com.schnozz.identitiesmod.keymapping.ModMappings.*;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientSeerEvents {
    //Cooldown icons (change texture)
    private static final CooldownIcon BLIND_COOLDOWN_ICON = new CooldownIcon(128,272,19, ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "textures/gui/blind_eye_icon.png"));

    //Cooldown variables
    private static final int BLIND_COOLDOWN = 200; //6000 is real CD

    //X-Ray variables
    private static final int RADIUS = 16;
    private static final List<BlockPos> ORES = new ArrayList<>();
    private static boolean scanEnabled;
    private static ClientLevel scannedLevel;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event){
        LocalPlayer seerPlayer = Minecraft.getInstance().player;
        if (seerPlayer == null) return;
        Level level = seerPlayer.level();
        if (!level.isClientSide()) return;
        Minecraft mc = Minecraft.getInstance();

        String power = seerPlayer.getData(ModDataAttachments.POWER_TYPE);

        if(power.equals("Seer")){
            //OPENS SCREEN (can click button to see perspective and cords shown on screen)
            if(PRIMARY_MAPPING.get().consumeClick()){
                SeerScreen newSeerScreen = new SeerScreen(Component.literal("Seer Screen"));
                Minecraft.getInstance().setScreen(newSeerScreen);
            }
            //BLIND PLAYER while on their perspective
            if(!mc.getCameraEntity().is(seerPlayer) && SECONDARY_MAPPING.get().consumeClick() && !seerPlayer.getData(ModDataAttachments.COOLDOWN).isOnCooldown(ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "blind_cd"),0)){

                Player targetPlayer = (Player)mc.getCameraEntity();
                PacketDistributor.sendToServer(new EffectAddPayload(targetPlayer.getId(),MobEffects.DARKNESS,2,200));

                long currentTime = Minecraft.getInstance().level.getGameTime();

                CooldownAttachment atachment = new CooldownAttachment();
                atachment.getAllCooldowns().putAll(seerPlayer.getData(ModDataAttachments.COOLDOWN).getAllCooldowns());
                atachment.setCooldown(ResourceLocation.fromNamespaceAndPath("identitiesmod", "blind_cd"), currentTime, BLIND_COOLDOWN);

                seerPlayer.setData(ModDataAttachments.COOLDOWN, atachment);
                PacketDistributor.sendToServer(new CooldownSyncPayload(new Cooldown(currentTime, BLIND_COOLDOWN), ResourceLocation.fromNamespaceAndPath("identitiesmod", "blind_cd"), false));
                BLIND_COOLDOWN_ICON.setCooldown(new Cooldown(currentTime, BLIND_COOLDOWN));
            }
            //X-Ray
            if(UTILITY_MAPPING.get().consumeClick()){
                if(scanEnabled){
                    clear();
                } else{
                    scanEnabled = true;
                }
            }

            if(scanEnabled){scan();}
        }
    }
    //X-Ray Classes
    public static void clear() {
        scanEnabled = false;
        scannedLevel = null;
        ORES.clear();
    }
    public static void scan() {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;

        ORES.clear();
        scannedLevel = level;

        if (level == null || mc.player == null) {
            clear();
            return;
        }

        // Scan around the current camera, including another player's POV.
        Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
        BlockPos center = BlockPos.containing(camera);

        int minY = Math.max(
                center.getY() - RADIUS,
                level.getMinBuildHeight()
        );
        int maxY = Math.min(
                center.getY() + RADIUS,
                level.getMaxBuildHeight() - 1
        );

        if (minY > maxY) {
            return;
        }

        BlockPos min = new BlockPos(
                center.getX() - RADIUS,
                minY,
                center.getZ() - RADIUS
        );
        BlockPos max = new BlockPos(
                center.getX() + RADIUS,
                maxY,
                center.getZ() + RADIUS
        );

        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (level.hasChunkAt(pos)
                    && level.getBlockState(pos).is(Tags.Blocks.ORES)) {
                ORES.add(pos.immutable());
            }
        }
    }
    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL
                || !scanEnabled
                || ORES.isEmpty()
                || Minecraft.getInstance().level != scannedLevel) {
            return;
        }

        Vec3 camera = event.getCamera().getPosition();
        Matrix4f view = new Matrix4f(event.getModelViewMatrix());

        var modelView = RenderSystem.getModelViewStack();
        modelView.pushMatrix();
        modelView.identity();
        RenderSystem.applyModelViewMatrix();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        try {
            BufferBuilder buffer = Tesselator.getInstance().begin(
                    VertexFormat.Mode.DEBUG_LINES,
                    DefaultVertexFormat.POSITION_COLOR
            );

            for (BlockPos pos : ORES) {
                float x = (float) (pos.getX() - camera.x);
                float y = (float) (pos.getY() - camera.y);
                float z = (float) (pos.getZ() - camera.z);

                int color = getOreColor(scannedLevel.getBlockState(pos));
                drawBox(buffer, view, x, y, z, color);
            }

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        } finally {
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();

            modelView.popMatrix();
            RenderSystem.applyModelViewMatrix();
        }
    }
    private static int getOreColor(BlockState state) {
        // Tags include both regular and deepslate variants.
        if (state.is(BlockTags.DIAMOND_ORES)) return 0x00FFFF;
        if (state.is(BlockTags.REDSTONE_ORES)) return 0xFF0000;
        if (state.is(BlockTags.EMERALD_ORES)) return 0x00FF00;
        if (state.is(BlockTags.IRON_ORES)) return 0xFFFFFF;
        if (state.is(BlockTags.COPPER_ORES)) return 0xB87333;
        if (state.is(BlockTags.COAL_ORES)) return 0x000000;
        if (state.is(BlockTags.GOLD_ORES)) return 0xFFD700;
        if (state.is(BlockTags.LAPIS_ORES)) return 0x2450FF;

        return 0xAA66FF; // Other ores, including unhandled modded ores.
    }
    private static void drawBox(BufferBuilder buffer, Matrix4f matrix, float x, float y, float z, int color) {
        for (int a = 0; a <= 1; a++) {
            for (int b = 0; b <= 1; b++) {
                drawLine(buffer, matrix,
                        x, y + a, z + b,
                        x + 1, y + a, z + b, color);

                drawLine(buffer, matrix,
                        x + a, y, z + b,
                        x + a, y + 1, z + b, color);

                drawLine(buffer, matrix,
                        x + a, y + b, z,
                        x + a, y + b, z + 1, color);
            }
        }
    }
    private static void drawLine(BufferBuilder buffer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, int color) {
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        buffer.addVertex(matrix, x1, y1, z1)
                .setColor(red, green, blue, 255);

        buffer.addVertex(matrix, x2, y2, z2)
                .setColor(red, green, blue, 255);
    }

    //Enchantment Classes

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiEvent.Post event) {
        if(!Minecraft.getInstance().player.getData(ModDataAttachments.POWER_TYPE).equals("Seer"))
        {
            return;
        }

        long gameTime = Minecraft.getInstance().level.getGameTime();
        GuiGraphics graphics = event.getGuiGraphics();

        BLIND_COOLDOWN_ICON.render(graphics,gameTime);
    }
}
