package net.fexcraft.mod.states.guis;

import net.fexcraft.lib.mc.gui.GenericGui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class ManagerGui extends GenericGui<ManagerContainer> {
	
	public static final ResourceLocation STONE = new ResourceLocation("minecraft:textures/blocks/stone.png");
	public static final ResourceLocation LIST = new ResourceLocation("states:textures/gui/manager_list.png");

	public ManagerGui(EntityPlayer player, int layer, int x, int y, int z){
		super(STONE, new ManagerContainer(player, layer, x, y, z), player);
		this.defbackground = false;
		this.deftexrect = true;
		container.set(this);
	}

	@Override
	protected void init(){
		switch(container.mode){
			case EDIT:
				break;
			case INFO:
				break;
			case LIST:
				xSize = 224;
				ySize = 200;
				texloc = LIST;
				break;
			case NONE:
			default:
				xSize = 0;
				ySize = 0;
				texloc = STONE;
				break;
		}
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
        buttons.clear();
        texts.clear();
        fields.clear();
        //
        
	}

	@Override
	protected void predraw(float pticks, int mouseX, int mouseY){

	}

	@Override
	protected void drawbackground(float pticks, int mouseX, int mouseY){
		//
	}

	@Override
	protected boolean buttonClicked(int mouseX, int mouseY, int mouseButton, String key, BasicButton button){
		//
		return false;
	}

	@Override
	protected void scrollwheel(int am, int x, int y){
		//
	}

}
