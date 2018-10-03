package net.fexcraft.mod.states.objects;

import net.fexcraft.mod.lib.api.block.fBlock;
import net.fexcraft.mod.states.States;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

@fBlock(modid = States.MODID, name = "general_mailbox")
public class GeneralMailbox extends Block {
	
	public GeneralMailbox(){
		super(Material.IRON, MapColor.GRAY);
		
	}

}