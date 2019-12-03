package net.fexcraft.mod.states.guis;

import net.fexcraft.lib.mc.gui.GenericContainer;
import net.fexcraft.lib.mc.gui.GenericGui;
import net.fexcraft.mod.states.States;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;

public class RulesUIC extends GenericContainer {
	
	protected GenericGui<RulesUIC> gui;

	public RulesUIC(EntityPlayer player, World world, int x, int y, int z){
		super(player);
		//
	}

	@Override
	protected void packet(Side side, NBTTagCompound packet, EntityPlayer player){
		if(!packet.hasKey("cargo")) return;
		if(packet.getString("cargo").equals("open")){
			int[] arr = packet.getIntArray("arr");
			player.openGui(States.MODID, 9, player.world, arr[0], arr[1], arr[2]);
		}
	}

}
