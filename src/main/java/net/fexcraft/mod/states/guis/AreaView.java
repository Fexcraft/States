package net.fexcraft.mod.states.guis;

import net.fexcraft.mod.lib.network.PacketHandler;
import net.fexcraft.mod.lib.network.packet.PacketNBTTagCompound;
import net.fexcraft.mod.lib.util.common.Print;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class AreaView extends GuiContainer {

	public static final ResourceLocation texture = new ResourceLocation("states:textures/gui/area_view.png");
	public static final ResourceLocation map_texture = new ResourceLocation("states:temp/area_view_map.png");
	private String view_mode;
	protected int x, z;
	
	public AreaView(EntityPlayer player, World world, int x, int y, int z){
		super(new PlaceholderContainer());
		xSize = 150; ySize = 200;
		view_mode = "surface";
		//
		Chunk chunk = world.getChunkFromBlockCoords(new BlockPos(x, y, z));
		requestRegion(this.x = chunk.x, this.z = chunk.z);
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
		this.drawTexturedModalRect(x, y, 0, 0, 128, 128);
	}
	
	@Override
	public void initGui(){
		super.initGui();
		//
	}
	
	@Override
    public void onGuiClosed(){
        super.onGuiClosed();
    	mc.getTextureManager().deleteTexture(map_texture);
    }
	
	@Override
	public void actionPerformed(GuiButton button){
		Print.debug(button, button.id);
		//
	}
	
	private void requestRegion(int x, int z){
		NBTTagCompound compound = new NBTTagCompound();
		compound.setString("target_listener", "states:gui");
		compound.setInteger("from", 1);
		compound.setInteger("chunk_x", x);
		compound.setInteger("chunk_z", z);
		compound.setString("view", view_mode);
		Print.debug(compound);
		PacketHandler.getInstance().sendToServer(new PacketNBTTagCompound(compound));
	}

}
