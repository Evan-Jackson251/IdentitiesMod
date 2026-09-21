package com.schnozz.identitiesmod.events.power_events.seer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.screen.SeerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
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
import org.joml.Matrix4f;
import java.util.ArrayList;
import java.util.List;
import static com.schnozz.identitiesmod.keymapping.ModMappings.*;

@EventBusSubscriber(modid = IdentitiesMod.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientSeerEvents {
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
            //SEE INVENTORY
            if(SECONDARY_MAPPING.get().consumeClick()){

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
    }
}
