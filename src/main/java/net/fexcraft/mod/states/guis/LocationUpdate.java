package net.fexcraft.mod.states.guis;

import net.fexcraft.lib.common.math.RGB;
import net.fexcraft.lib.common.math.Time;
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
	public static String[] lines = new String[]{ "", "", "" };
	public static final ResourceLocation texture = new ResourceLocation("states:textures/gui/location_gui.png");
	public static ResourceLocation[] icon = new ResourceLocation[3];
	public static int[] x = new int[]{ 96, 64, 32 }, y = new int[]{ 224, 224, 224 };
	private static Minecraft client;
	private static final LocationUpdate THIS = new LocationUpdate();
	
	public static boolean shown(){
		return till >= Time.getDate();
	}
	
	@SubscribeEvent
	public void displayLocationUpdate(RenderGameOverlayEvent event){
		if(event.getType() == ElementType.HOTBAR && shown()){
			if(client == null){ client = Minecraft.getMinecraft(); }
			client.renderEngine.bindTexture(texture);
			THIS.drawTexturedModalRect(0, 0, 0, 0, 54, 38);
			Tessellator tessellator = Tessellator.getInstance();
	        BufferBuilder bufferbuilder = tessellator.getBuffer();
			//
			if(icon[0] == null){
				THIS.drawTexturedModalRect(3, 3, x[0], y[0], 32, 32);
			}
			else{
				int x = 3, y = 3, width = 32, height = 32;
				client.getTextureManager().bindTexture(icon[0]);
		        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		        bufferbuilder.pos((x + 0),     (y + height), THIS.zLevel).tex(0, 1).endVertex();
		        bufferbuilder.pos((x + width), (y + height), THIS.zLevel).tex(1, 1).endVertex();
		        bufferbuilder.pos((x + width), (y + 0),      THIS.zLevel).tex(1, 0).endVertex();
		        bufferbuilder.pos((x + 0),     (y + 0),      THIS.zLevel).tex(0, 0).endVertex();
		        tessellator.draw();
			}
			//
			if(icon[1] == null){
				THIS.drawTexturedModalRect(35, 19, x[1], y[1], 16, 16);
			}
			else{
				int x = 35, y = 19, width = 16, height = 16;
				client.getTextureManager().bindTexture(icon[1]);
		        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		        bufferbuilder.pos((x + 0),     (y + height), THIS.zLevel).tex(0, 1).endVertex();
		        bufferbuilder.pos((x + width), (y + height), THIS.zLevel).tex(1, 1).endVertex();
		        bufferbuilder.pos((x + width), (y + 0),      THIS.zLevel).tex(1, 0).endVertex();
		        bufferbuilder.pos((x + 0),     (y + 0),      THIS.zLevel).tex(0, 0).endVertex();
		        tessellator.draw();
			}
			if(icon[2] == null){
				THIS.drawTexturedModalRect(35,  3, x[2], y[2], 16, 16);
			}
			else{
				int x = 35, y =  3, width = 16, height = 16;
				client.getTextureManager().bindTexture(icon[2]);
		        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		        bufferbuilder.pos((x + 0),     (y + height), THIS.zLevel).tex(0, 1).endVertex();
		        bufferbuilder.pos((x + width), (y + height), THIS.zLevel).tex(1, 1).endVertex();
		        bufferbuilder.pos((x + width), (y + 0),      THIS.zLevel).tex(1, 0).endVertex();
		        bufferbuilder.pos((x + 0),     (y + 0),      THIS.zLevel).tex(0, 0).endVertex();
		        tessellator.draw();
			}
			for(int i = 0; i < 3; i++){
				RGB.glColorReset();
				client.renderEngine.bindTexture(texture);
				THIS.drawTexturedModalRect(54, 2 + (12 * i), 55, 2 + (12 * i), client.fontRenderer.getStringWidth(lines[i]) + 4, 10);
				client.fontRenderer.drawString(lines[i], 56, 3 + (12 * i), MapColor.SNOW.colorValue);
			}
		}
		
	}
	
}