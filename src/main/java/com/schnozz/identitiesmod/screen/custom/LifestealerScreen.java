package com.schnozz.identitiesmod.screen.custom;

import com.mojang.blaze3d.systems.RenderSystem;
import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import com.schnozz.identitiesmod.buttons.lifestealer_screen_buttons.BuffButtons.*;
import com.schnozz.identitiesmod.buttons.lifestealer_screen_buttons.ToggleButtons.*;
import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.events.power_events.lifestealer.ServerScreenEvents;
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

public class LifestealerScreen extends Screen {
    public static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID,"textures/gui/lifestealer_screen_stinky.png");

    public LifestealerScreen(Component title) {
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
        ServerScreenEvents.resetLifeScreen();
    }

    public void addButtons() //creates and registers all LifestealerScreen buff and toggle renderable widgets
    {
        Player player = Minecraft.getInstance().player;

        //variable initialization and first defining
            //width and height of the screen
        int scaledWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int scaledHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
            //width of the button (dynamic)
        int bWidth = 135;
            //height of the button
        int bHeight = 14;
            //origin of button rendering
        int originX = bWidth - scaledWidth/25;
        int originY = scaledHeight/4 + bHeight;
            //changing x and y (dynamic)
        int dynamicX = originX - scaledWidth/10;
        int dynamicY = originY;
            //button text (dynamic)
        int amount = (int)player.getData(ModDataAttachments.LIFESTEALER_BUFFS).getLifestealerBuff(ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID,"strength"));
        if(amount == -1){amount = 2;}
        else if(amount == 0){amount = 3;}
        else{amount = 5;}
        Component message = Component.literal("Strength ("+ amount +")"); //18 characters
            //I think for tooltips but doesn't show up. Could also literally be for voice narration, if so then useless
        Button.CreateNarration createNarration = new Button.CreateNarration() {
            @Override
            public MutableComponent createNarrationMessage(Supplier<MutableComponent> supplier) {
                return Component.literal("");
            }
        };

        //creating buff buttons
            //Strength Buff Button
        StrengthUp strButton = new StrengthUp(originX,dynamicY,bWidth,bHeight,message,Button::onPress,createNarration); //do onPress last
        this.addRenderableWidget(strButton);

        dynamicY+=bHeight+5;
        amount = (int)player.getData(ModDataAttachments.LIFESTEALER_BUFFS).getLifestealerBuff(ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID,"speed"));
        amount += 2;
        message = Component.literal("Speed (" + amount +")"); //15 characters
        bWidth = (int) (message.getString().length()*6);
        dynamicX = bWidth + scaledWidth/5;

        SpeedUp speedButton = new SpeedUp(dynamicX,dynamicY,bWidth,bHeight,message,Button::onPress,createNarration); //do onPress last
        this.addRenderableWidget(speedButton);

        dynamicY+=bHeight+5;
        amount = (int)player.getData(ModDataAttachments.LIFESTEALER_BUFFS).getLifestealerBuff(ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID,"jump"));
        if(amount == -1){amount = 1;}
        else{amount = 2;}
        message = Component.literal("JumpBoost (" + amount +")"); //19 cha
        bWidth = (int) (message.getString().length()*6);
        dynamicX = bWidth + scaledWidth/7;

        JumpUp jumpButton = new JumpUp(dynamicX,dynamicY,bWidth,bHeight,message,Button::onPress,createNarration); //do onPress last
        this.addRenderableWidget(jumpButton);

        dynamicY+=bHeight+5;
        amount = (int)player.getData(ModDataAttachments.LIFESTEALER_BUFFS).getLifestealerBuff(ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID,"haste"));
        if(amount == -1){amount = 2;}
        else{amount = 3;}
        message = Component.literal("Haste (" + amount +")");
        bWidth = (int) (message.getString().length()*6);
        dynamicX = bWidth + scaledWidth/5;

        HasteUp hasteButton = new HasteUp(dynamicX,dynamicY,bWidth,bHeight,message,Button::onPress,createNarration); //do onPress last
        this.addRenderableWidget(hasteButton);

        dynamicY+=bHeight+5;

        message = Component.literal("Fire Resistance (3)");
        bWidth = (int) (message.getString().length()*6);
        dynamicX = bWidth + scaledWidth/16;

        FireRes fireResButton = new FireRes(dynamicX,dynamicY,bWidth,bHeight,message,Button::onPress,createNarration); //do onPress last
        this.addRenderableWidget(fireResButton);

        dynamicY+=bHeight+5;
        amount = (int)player.getData(ModDataAttachments.LIFESTEALER_BUFFS).getLifestealerBuff(ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID,"night_vision"));
        message = Component.literal("Night Vision (1)");
        bWidth = (int) (message.getString().length()*6);
        dynamicX = bWidth + scaledWidth/11;

        NightVision nightVisionButton = new NightVision(dynamicX,dynamicY,bWidth,bHeight,message,Button::onPress,createNarration); //do onPress last
        this.addRenderableWidget(nightVisionButton);

        //creating toggle buttons
            //Strength Toggle Button
        dynamicY = originY;
        dynamicX+=(int) (25*6) + 20; //25 is longest character length of a buff button

        message = Component.literal("Toggle"); //6 cha
        bWidth = (int) (message.getString().length()*6);

        StrengthToggle strToggle = new StrengthToggle(dynamicX,dynamicY,bWidth,bHeight,message,Button::onPress,createNarration);
        this.addRenderableWidget(strToggle);

        dynamicY+=bHeight+5;

        SpeedToggle speedToggle = new SpeedToggle(dynamicX,dynamicY,bWidth,bHeight,message,Button::onPress,createNarration);
        this.addRenderableWidget(speedToggle);

        dynamicY+=bHeight+5;

        JumpToggle jumpToggle = new JumpToggle(dynamicX,dynamicY,bWidth,bHeight,message,Button::onPress,createNarration);
        this.addRenderableWidget(jumpToggle);

        dynamicY+=bHeight+5;

        HasteToggle hasteToggle = new HasteToggle(dynamicX,dynamicY,bWidth,bHeight,message,Button::onPress,createNarration);
        this.addRenderableWidget(hasteToggle);

        dynamicY+=bHeight+5;

        FireResToggle fireResToggle = new FireResToggle(dynamicX,dynamicY,bWidth,bHeight,message,Button::onPress,createNarration);
        this.addRenderableWidget(fireResToggle);

        dynamicY+=bHeight+5;

        NightVisionToggle nightVisionToggle = new NightVisionToggle(dynamicX,dynamicY,bWidth,bHeight,message,Button::onPress,createNarration);
        this.addRenderableWidget(nightVisionToggle);
    }

}
