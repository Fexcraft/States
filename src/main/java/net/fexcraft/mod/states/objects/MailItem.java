package net.fexcraft.mod.states.objects;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.math.NumberUtils;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.api.registry.fItem;
import net.fexcraft.lib.mc.utils.Formatter;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.states.States;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@fItem(modid = States.MODID, name = "mail", variants = 5, custom_variants = { "empty", "expired", "private", "invite", "system" })
public class MailItem extends Item {
	
	public static MailItem INSTANCE;
	
	public MailItem(){
		this.setCreativeTab(CreativeTab.INSTANCE);
		this.setMaxStackSize(1); INSTANCE = this;
		//this.setContainerItem(this);
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
        	tooltip.add(Formatter.format("&9Sender: &7" + Static.getPlayerNameByUUID(compound.getString("Sender"))));
        	tooltip.add(Formatter.format("&9Receiver: &7" + (NumberUtils.isCreatable(compound.getString("Receiver")) ? compound.getString("Receiver") : Static.getPlayerNameByUUID(compound.getString("Receiver").replace("player:", "")))));
        	if(compound.hasKey("Expiry")){
            	tooltip.add(Formatter.format("&9Expires: &7" + Time.getAsString(compound.getLong("Expiry"))));
				if(Time.getDate() >= stack.getTagCompound().getLong("Expiry")){ stack.setItemDamage(1); }
        	}
        	if(compound.hasKey("StatesData")){
        		NBTTagCompound nbt = compound.getCompoundTag("StatesData");
        		tooltip.add(Formatter.format("&5InviteType: &7" + nbt.getString("type")));
        		tooltip.add(Formatter.format("&5At: &7" + Time.getAsString(compound.getLong("at"))));
        		tooltip.add(Formatter.format("&5Target ID: &7" + nbt.getInteger("id")));
        		if(nbt.hasKey("status")){
        			tooltip.add(Formatter.format("&aStatus: &7" + nbt.getString("status")));
        		}
        	}
    	}
    }
    
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ){
    	if(world.isRemote) return EnumActionResult.PASS;
    	ItemStack stack = player.getHeldItem(hand);
    	if(stack.getMetadata() > 1){ Static.getServer().commandManager.executeCommand(player, "/mail read"); }
        return EnumActionResult.PASS;
    }
    
    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items){
        if(!this.isInCreativeTab(tab)) return;
        for(int i = 0; i < this.getClass().getAnnotation(fItem.class).variants(); i++){
            items.add(new ItemStack(this, 1, i));
        }
    }
    
    @Override
    public String getTranslationKey(ItemStack stack){
    	int var = this.getClass().getAnnotation(fItem.class).variants();
    	if(stack.getMetadata() < var && stack.getMetadata() >= 0){
    		return this.getTranslationKey() + "_" + this.getClass().getAnnotation(fItem.class).custom_variants()[stack.getMetadata()];
    	} else return this.getTranslationKey();
    }
	
}