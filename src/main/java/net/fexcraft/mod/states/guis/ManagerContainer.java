package net.fexcraft.mod.states.guis;

import net.fexcraft.lib.mc.gui.GenericContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;

public class ManagerContainer extends GenericContainer {
	
	private ManagerGui gui;
	protected Mode mode;
	protected Layer layer;

	public ManagerContainer(EntityPlayer player, int layerid, int x, int y, int z){
		super(player);
		mode = Mode.values()[x];
		layer = Layer.values()[layerid];
	}

	@Override
	protected void packet(Side side, NBTTagCompound packet, EntityPlayer player){
		//
	}

	public void set(ManagerGui managerGui){
		this.gui = managerGui;
	}
	
	public static enum Mode {
		
		NONE,
		LIST,
		INFO,
		EDIT
		
	}
	
	public static enum Layer {
		
		DISTRICT,
		MUNICIPALITY,
		STATE,
		UNION,
		COMPANY,
		PLAYERDATA,
		CHUNK,
		PROPERTY
		
	}

}
