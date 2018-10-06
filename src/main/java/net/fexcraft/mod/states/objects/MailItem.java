package net.fexcraft.mod.states.objects;

import java.util.List;

import javax.annotation.Nullable;

import net.fexcraft.mod.lib.api.item.fItem;
import net.fexcraft.mod.lib.util.common.Formatter;
import net.fexcraft.mod.states.States;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@fItem(modid = States.MODID, name = "mail", variants = 5, custom_variants = { "empty", "expired", "private", "invite", "system" })
public class MailItem extends Item {
	
	public MailItem(){
		this.setCreativeTab(CreativeTab.INSTANCE);
		this.setMaxStackSize(1);
	}
	
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag){
    	//if(stack.getTagCompound() == null && this.getDamage(stack) > 1){this.setDamage(stack, 1); return; }
    	if(stack.getMetadata() == 0){
        	tooltip.add(Formatter.format("&7Empty Mail."));
    	}
    	else if(stack.getMetadata() == 1){
        	tooltip.add(Formatter.format("&cExpired mail."));
    	}
    	else{
    		if(stack.getTagCompound() == null) return;
        	NBTTagCompound compound = stack.getTagCompound();
        	tooltip.add(Formatter.format("&9Type: &7" + compound.getString("Type")));
        	tooltip.add(Formatter.format("&9Sender: &7" + compound.getString("Sender")));
    	}
    }
    
    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items){
        if(!this.isInCreativeTab(tab)) return;
        for(int i = 0; i < this.getClass().getAnnotation(fItem.class).variants(); i++){
            items.add(new ItemStack(this, 1, i));
        }
    }
    
    @Override
    public String getUnlocalizedName(ItemStack stack){
    	int var = this.getClass().getAnnotation(fItem.class).variants();
    	if(stack.getMetadata() < var && stack.getMetadata() >= 0){
    		return this.getUnlocalizedName() + "_" + this.getClass().getAnnotation(fItem.class).custom_variants()[stack.getMetadata()];
    	} else return this.getUnlocalizedName();
    }
	
}