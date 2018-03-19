package net.fexcraft.mod.states.guis;

import net.fexcraft.mod.lib.network.PacketHandler;
import net.fexcraft.mod.lib.network.packet.PacketNBTTagCompound;
import net.fexcraft.mod.lib.util.common.Print;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class AreaView extends GuiContainer {

	public static final ResourceLocation texture = new ResourceLocation("states:textures/gui/area_view.png");
	public static final ResourceLocation map_texture = new ResourceLocation("states:temp/area_view_map.png");
	private String view_mode, old_mode = "none";
	protected int x, z, px, pz, sel;
	
	public AreaView(EntityPlayer player, World world, int x, int y, int z){
		super(new PlaceholderContainer());
		xSize = 150; ySize = 200;
		view_mode = ImageCache.TYPES[x >= ImageCache.TYPES.length ? 0 : x];
		//
		Chunk chunk = world.getChunkFromBlockCoords(player.getPosition());
		int[] i = ImageCache.getRegion(chunk.x, chunk.z);
		this.x = px = i[0]; this.z = pz = i[1];
		requestRegion();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY){
		//
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks){
		this.mc.getTextureManager().bindTexture(texture);
		this.drawTexturedModalRect((this.width - xSize) / 2, (this.height - ySize) / 2, 0, 0, xSize, ySize);
		//
		this.mc.getTextureManager().bindTexture(map_texture);
		int x = this.guiLeft + 11;
		int y = this.guiTop + 11;
		//this.drawTexturedModalRect(x, y, 0, 0, 128, 128);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos((double)(x +   0), (double)(y + 128), (double)this.zLevel).tex((double)((float)(0 +   0) * 0.00390625F), (double)((float)(0 + 256) * 0.00390625F)).endVertex();
        bufferbuilder.pos((double)(x + 128), (double)(y + 128), (double)this.zLevel).tex((double)((float)(0 + 256) * 0.00390625F), (double)((float)(0 + 256) * 0.00390625F)).endVertex();
        bufferbuilder.pos((double)(x + 128), (double)(y +   0), (double)this.zLevel).tex((double)((float)(0 + 256) * 0.00390625F), (double)((float)(0 +   0) * 0.00390625F)).endVertex();
        bufferbuilder.pos((double)(x +   0), (double)(y +   0), (double)this.zLevel).tex((double)((float)(0 +   0) * 0.00390625F), (double)((float)(0 +   0) * 0.00390625F)).endVertex();
        tessellator.draw();
        //
        if(x == px && z == pz){
        	//int rx = (int)Math.floor(x / 32.0), rz = (int)Math.floor(z / 32.0);
    		//int xx = ((x * 16) - (rx * 512)) / 4, zz = ((z * 16) - (rz * 512)) / 4;
			//this.drawTexturedModalRect(xx, zz, 28, 252, 4, 4);
        	//TODO draw marker on player position
        }
        //
        this.buttonList.forEach(button -> button.drawButton(mc, mouseX, mouseY, partialTicks));
        this.fontRenderer.drawString((view_mode.equals("surface") ? view_mode : view_mode.replace("surface_", "s.")), guiLeft + 57, guiTop + 149, MapColor.BLACK.colorValue, false);
        this.fontRenderer.drawString((old_mode.equals("surface") ? old_mode : old_mode.replace("surface_", "s.")), guiLeft + 57, guiTop + 165, MapColor.BLACK.colorValue, false);
        this.fontRenderer.drawString(this.x + "x, " + this.z + "z", guiLeft + 57, guiTop + 181, MapColor.BLACK.colorValue, false);
	}
	
	@Override
	public void initGui(){
		super.initGui();
		this.buttonList.clear();
		this.buttonList.add(new Button(0, guiLeft + 130, guiTop + 146,  0, 214));
		this.buttonList.add(new Button(1, guiLeft + 130, guiTop + 162,  0, 228));
		this.buttonList.add(new Button(2, guiLeft + 130, guiTop + 178,  0, 242));
		//
		this.buttonList.add(new Button(3, guiLeft +  22, guiTop + 146,  0, 214));//up
		this.buttonList.add(new Button(4, guiLeft +   6, guiTop + 162, 14, 214));//left
		this.buttonList.add(new Button(5, guiLeft +  22, guiTop + 162,  0, 228));//center
		this.buttonList.add(new Button(6, guiLeft +  38, guiTop + 162, 14, 228));//right
		this.buttonList.add(new Button(7, guiLeft +  22, guiTop + 178,  0, 242));//down
	}
	
	@Override
    public void onGuiClosed(){
        super.onGuiClosed();
    	mc.getTextureManager().deleteTexture(map_texture);
    }
	
	@Override
	public void actionPerformed(GuiButton button){
		Print.debug(button, button.id);
		switch(button.id){
			case 0:{ sel--; break; }
			case 1:{ requestRegion(); break; }
			case 2:{ sel++; break; }
			case 3:{ z--; break; }
			case 4:{ x--; break; }
			case 5:{ requestRegion(); break; }
			case 6:{ x++; break; }
			case 7:{ z++; break; }
			default: break;
		}
		if(sel < 0){ sel = ImageCache.TYPES.length - 1; }
		if(sel >= ImageCache.TYPES.length){ sel = 0; }
		view_mode = ImageCache.TYPES[sel];
		//
	}
	
	private void requestRegion(){
		NBTTagCompound compound = new NBTTagCompound();
		compound.setString("target_listener", "states:gui");
		compound.setInteger("from", 1);
		compound.setInteger("chunk_x", x);
		compound.setInteger("chunk_z", z);
		compound.setString("view", old_mode = view_mode);
		Print.debug(compound);
		PacketHandler.getInstance().sendToServer(new PacketNBTTagCompound(compound));
	}
	
	public class Button extends GuiButton {
		
		private int texx, texy;
		
		public Button(int buttonId, int x, int y, int texx, int texy){
			super(buttonId, x, y, 14, 14, "");
			this.texx = texx; this.texy = texy;
		}

		@Override
		public void drawButton(Minecraft mc, int mouseX, int mouseY, float f){
			if(!this.visible){ return; }
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
			if(this.hovered){
				mc.getTextureManager().bindTexture(texture);
				this.drawTexturedModalRect(this.x, this.y, texx, texy, 14, 14);
			}
		}
		
	}

}
