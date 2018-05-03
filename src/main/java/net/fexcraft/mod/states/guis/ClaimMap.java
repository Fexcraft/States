package net.fexcraft.mod.states.guis;

import java.awt.Color;

import net.fexcraft.mod.lib.network.PacketHandler;
import net.fexcraft.mod.lib.network.packet.PacketNBTTagCompound;
import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.lib.util.render.RGB;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class ClaimMap extends GuiContainer {

	public static final ResourceLocation texture = new ResourceLocation("states:textures/gui/claiming_gui.png");
	//private EntityPlayer player;
	//private World world;
	//private int x, y, z;
	private static int district;
	private static NBTTagList list;
	private MapButton map;
	private static String result = "";
	
	public ClaimMap(EntityPlayer player, World world, int x, int y, int z){
		super(new PlaceholderContainer());
		xSize = 130; ySize = 146;
		//this.player = player; this.world = world;
		//this.x = x; this.y = y; this.z = z;
		district = x;
		list = null;
		requestData();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY){
		this.mc.getTextureManager().bindTexture(texture);
		this.drawTexturedModalRect((this.width - xSize) / 2, (this.height - ySize) / 2, 0, 0, xSize, ySize);
		this.fontRenderer.drawStringWithShadow(result, 7, 7, MapColor.SNOW.colorValue);
	}
	
	@Override
	public void initGui(){
		super.initGui();
		this.buttonList.clear();
		this.buttonList.add(map = new MapButton(0, this.guiLeft + 10, this.guiTop + 10));
	}
	
	@Override
    public void onGuiClosed(){
        super.onGuiClosed();
        district = -1;
        result = "";
        list = null;
    }
	
	@Override
	public void actionPerformed(GuiButton button){
		if(list == null || map == null){ return; }
		if(button.id == 0){
			if(map.xx == -1 || map.yy == -1){
				return;
			}
			int i = map.xx, j = map.yy, k = 0, l = 0;
			while((i -= 10) > 0){ k++; }
			k += i > 0 ? 1 : 0;
			while((j -= 10) > 0){ l++; }
			l += j > 0 ? 1 : 0;
			NBTTagCompound compound = list.getCompoundTagAt(k + (l * 11));
			if(compound.getBoolean("claimable") && compound.getInteger("district") < 0){
				sendClaimRequest(compound.getInteger("x"), compound.getInteger("z"));
			}
		}
		return;
	}
	
	private void sendClaimRequest(int x, int z){
		NBTTagCompound compound = new NBTTagCompound();
		compound.setString("target_listener", "states:gui");
		compound.setInteger("from", 10);
		compound.setString("request", "claim");
		compound.setIntArray("data", new int[] { district, x, z });
		Print.debug(compound);
		PacketHandler.getInstance().sendToServer(new PacketNBTTagCompound(compound));
	}

	private void requestData(){
		NBTTagCompound compound = new NBTTagCompound();
		compound.setString("target_listener", "states:gui");
		compound.setInteger("from", 10);
		compound.setString("request", "get_map");
		compound.setInteger("district", district);
		//compound.setIntArray("pos", new int[] { x, y, z });
		Print.debug(compound);
		PacketHandler.getInstance().sendToServer(new PacketNBTTagCompound(compound));
	}

	public static void update(NBTTagCompound compound){
		list = (NBTTagList)compound.getTag("array");
	}
	
	private static class MapButton extends GuiButton {
		
		private int xx = -1, yy = -1;

		public MapButton(int buttonId, int x, int y){
			super(buttonId, x, y, 110, 110, "");
		}
		
		@Override
		public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks){
			if(!visible){ return; }
			mc.getTextureManager().bindTexture(texture);
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            xx = mouseX - x; yy = mouseY - this.y;
            if(xx >= 110){ xx = -1; } if(yy >= 110){ yy = -1; }
            if(list == null){ return; }
            int m = -1;
            if(xx >= 0 && yy >= 0){
    			int i = xx, j = yy, k = 0, l = 0;
    			while((i -= 10) > 0){ k++; }
    			k += i > 0 ? 1 : 0;
    			while((j -= 10) > 0){ l++; }
    			l += j > 0 ? 1 : 0;
    			m = k + (l * 11);
            }
            int k = 0;
            for(int i = 0; i < 11; i++){
            	for(int j = 0; j < 11; j++){
            		if(m == k){
            			Color color = new Color(list.getCompoundTagAt(k).getInteger("color"));
            			new RGB(color.getRed() + 255 / 2, color.getGreen() + 255 / 2, + color.getBlue() + 255 / 2).glColorApply();
            		}
            		else{
            			new RGB(list.getCompoundTagAt(k).getInteger("color")).glColorApply();
            		}
    				this.drawTexturedModalRect(this.x + (j * 10), this.y + (i * 10), 10, 10, 10, 10);
    				RGB.glColorReset();
            		k++;
            	}
            }
	    }
		
	}

	public static void update(boolean claimed, String rs, int x, int z, Integer color){
		if(claimed && list != null){
			list.forEach(nbtbase -> {
				if(nbtbase instanceof NBTTagCompound){
					NBTTagCompound compound = (NBTTagCompound)nbtbase;
					if(compound.getInteger("x") == x && compound.getInteger("z") == z){
						compound.setBoolean("claimable", false);
						compound.setInteger("district", district);
						if(color != null){
							compound.setInteger("color", color);
						}
					}
				}
			});
		}
		result = rs;
	}

}
