package com.schnozz.identitiesmod.icons;

import com.schnozz.identitiesmod.IdentitiesMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class ChargeIcon {
    private ResourceLocation texture;
    private int x,y,size;
    private double charge;
    private float percentFull;
    public ChargeIcon(int x, int y, int size, ResourceLocation texture, double charge) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.texture = texture;
        this.charge = charge;
    }
    public void setCharge(double charge)
    {
        this.charge = charge;
    }
    public void render(GuiGraphics guiGraphics) {
        percentFull = (float)((charge/100) * size);
        if(percentFull > 28){percentFull = 28;}
        System.out.println("PERCENT FULL: " + percentFull);

        guiGraphics.blit(texture, x, y, 0, 0, size, size, size, size*2);
        guiGraphics.blit(ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "textures/gui/charge_bar_outside.png"), x+size+1, y, 0, 0, size, size, size, size);
        guiGraphics.blit(ResourceLocation.fromNamespaceAndPath(IdentitiesMod.MODID, "textures/gui/charge_bar_inside.png"), x+size+1, y-2, 0, percentFull, size, size, size, (size)*2);
    }
}
