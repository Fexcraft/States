package net.fexcraft.mod.states.guis;

import java.awt.Color;
import java.util.ArrayList;

import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.lib.network.PacketHandler;
import net.fexcraft.mod.lib.network.packet.PacketNBTTagCompound;
import net.fexcraft.mod.lib.util.common.Formatter;
import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.lib.util.render.RGB;
import net.fexcraft.mod.states.util.ImageCache;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class AreaView extends GuiContainer {

	public static final ResourceLocation texture = new ResourceLocation("states:textures/gui/area_view_new.png");
	public static final ResourceLocation empty_tex = new ResourceLocation("states:textures/gui/empty.png");
	public static final ResourceLocation map_texture = new ResourceLocation("states:temp/area_view_map.png");
	protected int x, z, px, pz, mode, m;
	private ChunkPos pos;
	private boolean terrain, sent;
	private NBTTagList list;
	private NBTTagCompound namelist;
	private static AreaView instance;
	
	private static final RGB HOVERCLR = new RGB(0x81B539);
	private static final RGB OFFCLR = new RGB(RGB.RED);
	private static final RGB ONCLR = new RGB(RGB.GREEN);
	static { HOVERCLR.alpha = 0.5f; }
	static { OFFCLR.alpha = 0.5f; }
	static { ONCLR.alpha = 0.5f; }
	
	public AreaView(EntityPlayer player, World world, int x, int y, int z){
		super(new PlaceholderContainer());
		this.xSize = 244; this.ySize = 244;
		//
		Chunk chunk = world.getChunkFromBlockCoords(player.getPosition());
		int[] i = ImageCache.getRegion(chunk.x, chunk.z);
		this.x = px = i[0]; this.z = pz = i[1];
		this.pos = chunk.getPos();
		terrain = false; mode = 0;
		instance = this;
		requestData();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY){
		try{ this.pos = mc.world.getChunkFromBlockCoords(mc.player.getPosition()).getPos(); } catch(Exception e){}
		//
		this.mc.getTextureManager().bindTexture(texture);
		this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, xSize + 12, ySize);
		//
		this.mc.getTextureManager().bindTexture(terrain ? map_texture : empty_tex);
		int x = this.guiLeft + 10;
		int y = this.guiTop + 10;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos((double)(x +   0), (double)(y + 224), (double)this.zLevel).tex((double)((float)(0 +   0) * 0.00390625F), (double)((float)(0 + 256) * 0.00390625F)).endVertex();
        bufferbuilder.pos((double)(x + 224), (double)(y + 224), (double)this.zLevel).tex((double)((float)(0 + 256) * 0.00390625F), (double)((float)(0 + 256) * 0.00390625F)).endVertex();
        bufferbuilder.pos((double)(x + 224), (double)(y +   0), (double)this.zLevel).tex((double)((float)(0 + 256) * 0.00390625F), (double)((float)(0 +   0) * 0.00390625F)).endVertex();
        bufferbuilder.pos((double)(x +   0), (double)(y +   0), (double)this.zLevel).tex((double)((float)(0 +   0) * 0.00390625F), (double)((float)(0 +   0) * 0.00390625F)).endVertex();
        tessellator.draw();
        //
		mc.fontRenderer.drawString("VM: " + Listener.MAP_VIEW_MODES[instance.mode], 7, 7, MapColor.SNOW.colorValue);
		mc.fontRenderer.drawString("Terrain: " + (instance.terrain ? "shown" : "hidden"), 7, 16, MapColor.SNOW.colorValue);
		mc.fontRenderer.drawString(instance.x + "x", 7, 25, MapColor.SNOW.colorValue);
		mc.fontRenderer.drawString(instance.z + "z", 7, 34, MapColor.SNOW.colorValue);
		mc.fontRenderer.drawString("SyncRQ: " + (instance.sent ? "true" : "false"), 7, 43, MapColor.SNOW.colorValue);
	}
	
	@Override
	public void initGui(){
		super.initGui();
		this.buttonList.clear();
		this.buttonList.add(new AGB(0, guiLeft, 238, guiTop, 21, "<< West"));
		this.buttonList.add(new AGB(1, guiLeft, 238, guiTop, 37, "Refresh"));
		this.buttonList.add(new AGB(2, guiLeft, 238, guiTop, 53, "East >>"));
		this.buttonList.add(new GuiButton(3, guiLeft + 238, guiTop + 69, 14, 14, ""){			
			@Override
			public void drawButton(Minecraft mc, int mouseX, int mouseY, float f){
				if(!this.visible){ return; }
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				mc.getTextureManager().bindTexture(texture);
				this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
				if(this.hovered){ HOVERCLR.glColorApply(); }
				else{ if(terrain){ ONCLR.glColorApply(); } else { OFFCLR.glColorApply(); }}
				this.drawTexturedModalRect(this.x, this.y, 238, 69, this.width, this.height);
				RGB.glColorReset();
				if(hovered){
					instance.drawHoveringText((!instance.terrain ? "Enable" : "Disable") + " Terrain", mouseX, mouseY);
				}
			}
		});
		this.buttonList.add(new AGB(4, guiLeft, 238, guiTop, 83, "Prev Map Mode"));
		this.buttonList.add(new AGB(5, guiLeft, 238, guiTop, 97, "Next Map Mode"));
		this.buttonList.add(new AGB(6, guiLeft, 238, guiTop, 113, "North"));
		this.buttonList.add(new AGB(7, guiLeft, 238, guiTop, 129, "Refresh"));
		this.buttonList.add(new AGB(8, guiLeft, 238, guiTop, 145, "South"));
		this.buttonList.add(new MapButton(9, guiLeft + 10, guiTop + 10));
	}
	
	@Override
    public void onGuiClosed(){
        super.onGuiClosed();
    	mc.getTextureManager().deleteTexture(map_texture);
    	instance = null;
    }
	
	@Override
	public void actionPerformed(GuiButton button){
		//Print.debug(button, button.id);
		sent = false;
		switch(button.id){
			case 0:{ x--; break; }
			case 1:{ requestData(); break; }
			case 2:{ x++; break; }
			//
			case 3:{ terrain = !terrain; break;}
			case 4:{ mode--; corrmode(); break; }
			case 5:{ mode++; corrmode(); break; }
			//
			case 6:{ z--; break; }
			case 7:{ requestData(); break; }
			case 8:{ z++; break; }
			case 9:{
				NBTTagCompound nbt = list.getCompoundTagAt(m);
				NBTTagCompound compound = new NBTTagCompound();
				compound.setString("target_listener", "states:gui");
				compound.setInteger("from", 1);
				compound.setInteger("x", nbt.getInteger("x"));
				compound.setInteger("z", nbt.getInteger("z"));
				compound.setBoolean("cmd", true);
				PacketHandler.getInstance().sendToServer(new PacketNBTTagCompound(compound));
				sent = true;
				break;
			}
			default: break;
		}
	}
	
	private void corrmode(){
		if(mode < 0){ mode = Listener.MAP_VIEW_MODES.length - 1; }
		if(mode >= Listener.MAP_VIEW_MODES.length){ mode = 0; }
	}

	private void requestData(){
		NBTTagCompound compound = new NBTTagCompound();
		compound.setString("target_listener", "states:gui");
		compound.setInteger("from", 1);
		compound.setInteger("x", x);
		compound.setInteger("z", z);
		compound.setBoolean("terrain", terrain);
		compound.setInteger("mode", mode);
		Print.debug(compound);
		PacketHandler.getInstance().sendToServer(new PacketNBTTagCompound(compound));
		sent = true;
	}
	
	public static class AGB extends GuiButton {
		
		private int tx, ty;

		public AGB(int buttonId, int x, int tx, int y, int ty, String string){
			super(buttonId, x + tx, y + ty, 14, 14, string);
			this.tx = tx; this.ty = ty;
		}
		
		@Override
		public void drawButton(Minecraft mc, int mouseX, int mouseY, float f){
			if(!this.visible){ return; }
			mc.getTextureManager().bindTexture(texture);
			this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
			if(this.hovered){ HOVERCLR.glColorApply(); }
			this.drawTexturedModalRect(this.x, this.y, tx, ty, this.width, this.height);
			if(this.hovered){ RGB.glColorReset(); }
			if(hovered){
				instance.drawHoveringText(this.displayString, mouseX, mouseY);
			}
		}
		
	}

	public static void update(NBTTagCompound nbt){
		instance.list = (NBTTagList)nbt.getTag("list");
		instance.namelist = (NBTTagCompound)nbt.getTag("namelist");
	}
	
	private static class MapButton extends GuiButton {
		
		private int xx = -1, yy = -1;

		public MapButton(int buttonId, int x, int y){
			super(buttonId, x, y, 224, 224, "");
		}
		
		@Override
		public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks){
			if(!visible){ return; }
			mc.getTextureManager().bindTexture(instance.terrain ? map_texture : empty_tex);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            xx = mouseX - x; yy = mouseY - this.y;
            if(xx >= 224){ xx = -1; } if(yy >= 224){ yy = -1; }
            if(instance.list == null){ return; }
            instance.m = -1;
            if(xx >= 0 && yy >= 0){
    			int i = xx, j = yy, k = 0, l = 0;
    			while((i -= 7) > 0){ k++; }
    			k += i > 0 ? 1 : 0;
    			while((j -= 7) > 0){ l++; }
    			l += j > 0 ? 1 : 0;
    			instance.m = l + (k * 32);
            }
            int ppos = instance.x == instance.px && instance.z == instance.pz ? ((instance.pos.x % 32) * 32) + instance.pos.z % 32 : -1;
            //
            if(instance.mode != 0){
    			RGB rgb = null;
            	for(int yy = 0; yy < 32; yy++){
            		for(int xx = 0; xx < 32; xx++){
            			int i = xx * 32 + yy;
            			if(instance.m == i){
                			Color color = new Color(instance.list.getCompoundTagAt(i).getInteger("color"));
                			rgb = new RGB(color.getRed() + 255 / 2, color.getGreen() + 255 / 2, + color.getBlue() + 255 / 2);
                		}
            			else if(ppos == i){
            				rgb = new RGB(RGB.BLACK);
            			}
                		else{
                			rgb = new RGB(instance.list.getCompoundTagAt(i).getInteger("color"));
                		}
            			rgb.alpha = instance.terrain ? 0.8f : 1f;
            			rgb.glColorApply();
            			int x = this.x + (xx * 7), y = this.y + (yy * 7);
            			int tx = xx * 8, ty = yy * 8;
            			Tessellator tessellator = Tessellator.getInstance();
                        BufferBuilder bufferbuilder = tessellator.getBuffer();
                        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
                        bufferbuilder.pos((double)(x + 0), (double)(y + 7), (double)this.zLevel).tex((double)((float)(tx + 0) * 0.00390625F), (double)((float)(ty + 8) * 0.00390625F)).endVertex();
                        bufferbuilder.pos((double)(x + 7), (double)(y + 7), (double)this.zLevel).tex((double)((float)(tx + 8) * 0.00390625F), (double)((float)(ty + 8) * 0.00390625F)).endVertex();
                        bufferbuilder.pos((double)(x + 7), (double)(y + 0), (double)this.zLevel).tex((double)((float)(tx + 8) * 0.00390625F), (double)((float)(ty + 0) * 0.00390625F)).endVertex();
                        bufferbuilder.pos((double)(x + 0), (double)(y + 0), (double)this.zLevel).tex((double)((float)(tx + 0) * 0.00390625F), (double)((float)(ty + 0) * 0.00390625F)).endVertex();
                        tessellator.draw();
        				RGB.glColorReset();
            		}
            	}
            }
            //
            if(instance.m >= 0){
            	ArrayList<String> arr = new ArrayList<String>();
            	NBTTagCompound compound = instance.list.getCompoundTagAt(instance.m);
            	arr.add(Formatter.PARAGRAPH_SIGN + "7Coords: " + compound.getInteger("x") + "x, " + compound.getInteger("z") + "z");
            	switch(instance.mode){
	            	case 1:{
	            		int dis = compound.getInteger("district");
	            		arr.add(Formatter.PARAGRAPH_SIGN + "7District: " + instance.namelist.getString("district:" + dis) + " (" + dis + ");");
	            		break;
	            	}
	            	case 2:{
	            		int mun = compound.getInteger("municipality");
	            		arr.add(Formatter.PARAGRAPH_SIGN + "7Municipality: " + instance.namelist.getString("municipality:" + mun) + " (" + mun + ");");
	            		break;
	            	}
	            	case 3:{
	            		int st = compound.getInteger("state");
	            		arr.add(Formatter.PARAGRAPH_SIGN + "7State: " + instance.namelist.getString("state:" + st) + " (" + st + ");");
	            		break;
	            	}
	            	case 4:{
	            		arr.add(Formatter.PARAGRAPH_SIGN + "7Type: " + compound.getInteger("type"));
	            		break;
	            	}
            	}
            	if(compound.hasKey("linked") && compound.getBoolean("linked")){
            		arr.add(Formatter.PARAGRAPH_SIGN + "&7Linked: " + compound.getIntArray("link")[0] + "x, " + compound.getIntArray("link")[1] + "z");
            	}
            	if(compound.hasKey("owned") && compound.getBoolean("owned")){
            		arr.add(Formatter.PARAGRAPH_SIGN + "9Owner: " + compound.getString("owner"));
            	}
            	if(compound.hasKey("price") && compound.getLong("price") > 0){
            		arr.add(Formatter.PARAGRAPH_SIGN + "6Price: " + Config.getWorthAsString(compound.getLong("price")));
            	}
            	if(ppos == instance.m){
            		arr.add(Formatter.format("&8&oYour current position."));
            	}
    		    instance.drawHoveringText(arr, mouseX, mouseY, mc.fontRenderer);
            }
	    }
		
	}

}
