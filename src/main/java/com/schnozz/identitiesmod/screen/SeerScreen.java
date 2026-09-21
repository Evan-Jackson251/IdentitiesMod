package com.schnozz.identitiesmod.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.buttons.seer_screen_buttons.SeerPerspectiveButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import java.util.Collection;
import java.util.function.Supplier;

public class SeerScreen extends Screen {
    public static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID,"textures/gui/power_screen.png");

    public SeerScreen(Component title) {
        super(title);
    }

    @Override
    public void init() {
        super.init();
        addButtons();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int scaledWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int scaledHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        super.render(graphics, mouseX, mouseY, partialTick); //widget rendering

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);

        int x = (scaledWidth - 256) / 2;
        int y = (scaledHeight - 186) / 2;

        graphics.blit(GUI_TEXTURE, x, y, 0, 0, 256, 186);
    }
    public void addButtons()
    {
        int scaledWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int scaledHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        int bWidth = 135;
        int bHeight = 14;

        int originX = scaledWidth/3;
        int originY = scaledHeight/3 + bHeight;

        Button.CreateNarration createNarration = new Button.CreateNarration() {
            @Override
            public MutableComponent createNarrationMessage(Supplier<MutableComponent> supplier) {
                return Component.literal("");
            }
        };

        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null) {return;}

        Collection<PlayerInfo> playerList = mc.getConnection().getOnlinePlayers();
        int count = 1;

        for(PlayerInfo info: playerList)
        {
            if(count==7)
            {
                originX+=scaledWidth/5;
                originY = scaledHeight/3 + bHeight;
            }
            Player targetPlayer = mc.level == null
                    ? null
                    : mc.level.getPlayerByUUID(info.getProfile().getId());
            if(targetPlayer == null){return;}
            Vec3 cords = targetPlayer.getPosition(1);

            Component message = Component.literal(info.getProfile().getName() + " (" + cords + ")");
            bWidth = message.getString().length()*6 + 3;

            SeerPerspectiveButton seerPerspectiveButton = new SeerPerspectiveButton(
                    originX,originY,bWidth,bHeight,message,Button::onPress,
                    createNarration,info.getProfile().getId());

            this.addRenderableWidget(seerPerspectiveButton);

            originY+=bHeight+5;
            count++;
        }
    }
}
