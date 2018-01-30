package net.fexcraft.mod.states.guis;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

public class PlaceholderContainer extends Container {

	@Override
	public boolean canInteractWith(EntityPlayer player){
		return !player.isDead;
	}

}
