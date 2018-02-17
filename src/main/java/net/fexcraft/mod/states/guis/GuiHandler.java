package net.fexcraft.mod.states.guis;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler{
	
	/**
	 * 0 Welcome Screen
	 * 1 Chunk View
	 * 2 Districts
	 * 3 Municipalities
	 * 4 States
	 * 5 Unions
	 * 6 empty
	 * 7 Companies
	 * 8 Player Data
	 * 9 empty
	 * */

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z){
		return new PlaceholderContainer();
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z){
		//return new GeneralGui(ID, player, world, x, y, z);
		switch(ID){
			case 0:{
				return new WelcomeGui(player, world, x, y, z);
			}
			case 1:{
				return new AreaView(player, world, x, y, z);
			}
		}
		return null;
	}

}
