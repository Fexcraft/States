package net.fexcraft.mod.states.objects;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class CreativeTab extends CreativeTabs {
	
	public static final CreativeTab INSTANCE = new CreativeTab();

	public CreativeTab(){ super("states"); }

	@Override
	public ItemStack createIcon(){
		return new ItemStack(Items.PAPER);
	}
	
}