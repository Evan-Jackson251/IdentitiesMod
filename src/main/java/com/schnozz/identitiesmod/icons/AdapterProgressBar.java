package com.schnozz.identitiesmod.icons;

import com.schnozz.identitiesmod.attachments.ModDataAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class AdapterProgressBar {

    private ResourceLocation texture;
    private int x;
    private int y;
    private int size;
    private ResourceLocation source;
    private float cap;
    private float percentFull;
    public AdapterProgressBar(int x, int y, int size, ResourceLocation texture, ResourceLocation source, float cap) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.texture = texture;
        this.source = source;
        this.cap = cap;
    }

    public void render(GuiGraphics guiGraphics) {
        Player adapter = Minecraft.getInstance().player;
        if(adapter.getData(ModDataAttachments.ADAPTION).getAdaptationValue(source) > 0){percentFull = 0;}
        else if(1-adapter.getData(ModDataAttachments.ADAPTION).getAdaptationValue(source) == 1){percentFull = 0;}
        else{percentFull = cap/(1-adapter.getData(ModDataAttachments.ADAPTION).getAdaptationValue(source));}

        guiGraphics.blit(texture, x, y, 0, (size*percentFull) - 18, size, size, size, size*2);
    }
}
