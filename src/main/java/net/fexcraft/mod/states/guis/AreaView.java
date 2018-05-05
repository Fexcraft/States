package net.fexcraft.mod.states.guis;

import net.fexcraft.mod.lib.network.PacketHandler;
import net.fexcraft.mod.lib.network.packet.PacketNBTTagCompound;
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
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class AreaView extends GuiContainer {

	public static final ResourceLocation texture = new ResourceLocation("states:textures/gui/area_view_new.png");
	public static final ResourceLocation empty_tex = new ResourceLocation("states:textures/gui/empty.png");
	public static final ResourceLocation map_texture = new ResourceLocation("states:temp/area_view_map.png");
	protected int x, z, px, pz, mode;
	private boolean terrain, sent;
	private NBTTagList list;
	
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
		terrain = false; mode = 0;
		requestData();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY){
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
		this.fontRenderer.drawString("View Mode: " + Listener.MAP_VIEW_MODES[mode], 7, 7, MapColor.SNOW.colorValue);
		this.fontRenderer.drawString("Terrain: " + (terrain ? "shown" : "hidden"), 7, 16, MapColor.SNOW.colorValue);
		this.fontRenderer.drawString(this.x + "x", 7, 25, MapColor.SNOW.colorValue);
		this.fontRenderer.drawString(this.z + "z", 7, 34, MapColor.SNOW.colorValue);
		this.fontRenderer.drawString("SyncRQ: " + (sent ? "true" : "false"), 7, 43, MapColor.SNOW.colorValue);
		//
	}
	
	@Override
	public void initGui(){
		super.initGui();
		this.buttonList.clear();
		this.buttonList.add(new AGB(0, guiLeft, 238, guiTop, 21));
		this.buttonList.add(new AGB(1, guiLeft, 238, guiTop, 37));
		this.buttonList.add(new AGB(2, guiLeft, 238, guiTop, 53));
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
			}
		});
		this.buttonList.add(new AGB(4, guiLeft, 238, guiTop, 83));
		this.buttonList.add(new AGB(5, guiLeft, 238, guiTop, 97));
		this.buttonList.add(new AGB(6, guiLeft, 238, guiTop, 113));
		this.buttonList.add(new AGB(7, guiLeft, 238, guiTop, 129));
		this.buttonList.add(new AGB(8, guiLeft, 238, guiTop, 145));
	}
	
	@Override
    public void onGuiClosed(){
        super.onGuiClosed();
    	mc.getTextureManager().deleteTexture(map_texture);
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
		compound.setInteger("chunk_x", x);
		compound.setInteger("chunk_z", z);
		compound.setBoolean("terrain", terrain);
		compound.setInteger("mode", mode);
		Print.debug(compound);
		PacketHandler.getInstance().sendToServer(new PacketNBTTagCompound(compound));
		sent = true;
	}
	
	public static class AGB extends GuiButton {
		
		private int tx, ty;

		public AGB(int buttonId, int x, int tx, int y, int ty){
			super(buttonId, x + tx, y + ty, 14, 14, "");
			this.tx = tx; this.ty = ty;
		}
		
		@Override
		public void drawButton(Minecraft mc, int mouseX, int mouseY, float f){
			if(!this.visible){ return; }
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			mc.getTextureManager().bindTexture(texture);
			this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
			if(this.hovered){ HOVERCLR.glColorApply(); }
			this.drawTexturedModalRect(this.x, this.y, tx, ty, this.width, this.height);
			if(this.hovered){ RGB.glColorReset(); }
		}
		
	}

}
