package net.fexcraft.mod.states.guis;

import net.fexcraft.lib.common.math.RGB;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.tmt.ModelBase;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class LocationUpdate extends GuiScreen {
	
	public static long till = Time.getDate();
	public static String[] lines = new String[]{ "", "", "", "" };
	public static final ResourceLocation texture = new ResourceLocation("states:textures/gui/location_gui.png");
	public static ResourceLocation[] icon = new ResourceLocation[5];
	public static int[] icorx = {  3, 35, 105, 81, 62 };
	public static int[] icory = {  3,  3,   3,  3,  7 };
	public static int[] icorz = { 32, 24,  32, 24, 16 };
	public static int[] x = { 32, 64, 0, 96, 32 }, y = { 224, 224, 224, 224, 224 };
	private static Minecraft client;
	private static final LocationUpdate THIS = new LocationUpdate();
	
	public static boolean shown(){
		return till >= Time.getDate();
	}
	
	@SubscribeEvent
	public void displayLocationUpdate(RenderGameOverlayEvent event){
		if(event.getType() != ElementType.HOTBAR || !shown()) return;
		if(client == null)client = Minecraft.getMinecraft();
		ModelBase.bindTexture(texture);
		THIS.drawTexturedModalRect(0, 0, 0, 0, 140, 38);
		Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
		//
        for(int i = 0; i < 5; i++){
        	if(icon[i] == null){
    			THIS.drawTexturedModalRect(icorx[i], icory[i], x[i], y[i], icorz[i], icorz[i]);
        	}
        	else{
    			int x = icorx[i], y = icory[i], width = icorz[i], height = icorz[i];
    			client.getTextureManager().bindTexture(icon[i]);
    	        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
    	        bufferbuilder.pos((x + 0),     (y + height), THIS.zLevel).tex(0, 1).endVertex();
    	        bufferbuilder.pos((x + width), (y + height), THIS.zLevel).tex(1, 1).endVertex();
    	        bufferbuilder.pos((x + width), (y + 0),      THIS.zLevel).tex(1, 0).endVertex();
    	        bufferbuilder.pos((x + 0),     (y + 0),      THIS.zLevel).tex(0, 0).endVertex();
    	        tessellator.draw();
        	}
        }
		for(int i = 0; i < 4; i++){
			RGB.glColorReset();
			if(lines[i].length() == 0) continue;
			ModelBase.bindTexture(texture);
			THIS.drawTexturedModalRect(0, 38 + (12 * i), 0, 38 + (12 * i), client.fontRenderer.getStringWidth(lines[i]) + 10, 12);
			client.fontRenderer.drawString(lines[i], 8, 40 + (12 * i), MapColor.SNOW.colorValue);
		}
		
	}
	
}