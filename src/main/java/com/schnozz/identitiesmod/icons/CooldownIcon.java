package com.schnozz.identitiesmod.icons;

import com.schnozz.identitiesmod.IdentitiesMod;
import com.schnozz.identitiesmod.cooldown.Cooldown;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class CooldownIcon {

    private ResourceLocation texture;
    private int x;
    private int y;
    private int size;
    private Cooldown cd = null;

    public CooldownIcon(int x, int y, int size, ResourceLocation texture) {

        this.x = x;
        this.y = y;
        this.size = size;
        this.texture = texture;
    }

    public void setCooldown(Cooldown cd) {this.cd = cd;}

    public void render(GuiGraphics guiGraphics, long currentTime) {

        if(cd != null && (currentTime - cd.startTime()) < cd.duration())
        {
            float percentOfCD = (float) (currentTime - cd.startTime()) / cd.duration();
            guiGraphics.blit(texture, x, y, 0, (size*percentOfCD) - 16, size, size, size, size*2);
        }
        else
        {
            guiGraphics.blit(ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "textures/gui/green_back.png"), x - 1,y - 1,0,0,18,18,18,18);
            guiGraphics.blit(texture, x, y, 0, 0, size, size, size, size*2);
        }

    }
}
