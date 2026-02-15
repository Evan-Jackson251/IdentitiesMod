package com.schnozz.identitiesmod.screen.custom;

import com.mojang.blaze3d.systems.RenderSystem;
import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.buttons.lifestealer_screen_buttons.BuffButtons.*;
import com.schnozz.identitiesmod.buttons.lifestealer_screen_buttons.ToggleButtons.*;
import com.schnozz.identitiesmod.buttons.power_screen_buttons.SetPowerButton;
import com.schnozz.identitiesmod.events.ClientAdvancementEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.function.Supplier;

public class PowerScreen extends Screen {
    public static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID,"textures/gui/power_screen.png");

    public PowerScreen(Component title) {
        super(title);
    }

    @Override
    public void init() {
        super.init();
        addButtons();
    }
    // mouseX and mouseY indicate the scaled coordinates of where the cursor is in on the screen
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

        graphics.blit(GUI_TEXTURE, x, y, 0, 0, 256, 186); //texture rendering
    }
    @Override
    public void onClose()
    {
        super.onClose();
        ClientAdvancementEvents.resetPowerScreen();
    }

    public void addButtons()
    {
        Player player = Minecraft.getInstance().player;

        int scaledWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int scaledHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        int bWidth = 135;

        int bHeight = 14;

        int originX = bWidth - scaledWidth/25;
        int originY = scaledHeight/4 + bHeight;

        //int dynamicX = originX - scaledWidth/10;
        //int dynamicY = originY;

        Button.CreateNarration createNarration = new Button.CreateNarration() {
            @Override
            public MutableComponent createNarrationMessage(Supplier<MutableComponent> supplier) {
                return Component.literal("");
            }
        };

        //adds buttons
        for(String power: player.getData(ModDataAttachments.AVAILABLE_POWERS).getAvailablePowers())
        {
            Component message = Component.literal(power);
            SetPowerButton adaptationButton = new SetPowerButton(originX,originY,bWidth,bHeight,message,Button::onPress,createNarration,power);
            this.addRenderableWidget(adaptationButton);
            //increases position for next button and adjusts width based on characters
            bWidth = (int) (message.getString().length()*6);
            //dynamicX = bWidth + scaledWidth/5;
            originY+=bHeight+5;
        }
    }
}
