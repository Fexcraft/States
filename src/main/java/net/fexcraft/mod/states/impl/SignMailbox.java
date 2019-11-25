package net.fexcraft.mod.states.impl;

import java.io.File;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.capabilities.sign.SignCapability;
import net.fexcraft.lib.mc.utils.Formatter;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.api.capabilities.StatesCapabilities;
import net.fexcraft.mod.states.events.PlayerEvents;
import net.fexcraft.mod.states.util.StateUtil;
import net.fexcraft.mod.states.util.StatesPermissions;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class SignMailbox implements SignCapability.Listener {
	
	public static final ResourceLocation RESLOC = new ResourceLocation("states:mailbox");
	private NonNullList<ItemStack> mails = NonNullList.<ItemStack>create();
	private boolean active;
	private String reci;
	private String type;

	@Override
	public ResourceLocation getId(){
		return RESLOC;
	}

	@Override
	public boolean isActive(){
		return active;
	}

	@Override
	public boolean onPlayerInteract(SignCapability cap, PlayerInteractEvent event, IBlockState state, TileEntitySign tileentity){
		if(event.getWorld().isRemote){ return false; }
		if(!active){
			if(tileentity.signText[0].getUnformattedText().toLowerCase().equals("[st-mailbox]")){
				BlockPos back = getPosAtBack(state, tileentity);
				if(!PlayerEvents.checkAccess(event.getWorld(), back, event.getWorld().getBlockState(back), event.getEntityPlayer())){
					Print.chat(event.getEntityPlayer(), "Block/TileEntity behind sign cannot be accessed."); return false;
				}
				Chunk chunk = StateUtil.getChunk(tileentity.getPos());
				String type = tileentity.signText[1].getUnformattedText().toLowerCase();
				switch(type){
					case "state":{
						if(!StatesPermissions.hasPermission(event.getEntityPlayer(), "state.set.mailbox", chunk.getState())){
							Print.chat(event.getEntityPlayer(), "No permission to set the State Mailbox."); return false;
						} reci = type;
					}
					case "municipality":{
						if(!StatesPermissions.hasPermission(event.getEntityPlayer(), "municipality.set.mailbox", chunk.getMunicipality())){
							Print.chat(event.getEntityPlayer(), "No permission to set the Municipality Mailbox."); return false;
						} reci = type;
					}
					case "district":{
						if(!StatesPermissions.hasPermission(event.getEntityPlayer(), "district.set.mailbox", chunk.getDistrict())){
							Print.chat(event.getEntityPlayer(), "No permission to set the District Mailbox."); return false;
						} reci = type;
					}
					case "company": break;//TODO
					case "player":{
						String rec = tileentity.signText[2].getUnformattedText().toLowerCase();
						com.mojang.authlib.GameProfile prof = Static.getServer().getPlayerProfileCache().getGameProfileForUsername(rec);
						if(prof == null){
							Print.chat(event.getEntityPlayer(), "Couldn't find player UUID in cache.");
							return false;
						}
						if(prof.getId().equals(event.getEntityPlayer().getGameProfile().getId()) || StatesPermissions.hasPermission(event.getEntityPlayer(), "admin", null)){
							this.reci = prof.getId().toString();
							tileentity.signText[1] = Formatter.newTextComponentString(prof.getName());
							tileentity.signText[2] = Formatter.newTextComponentString("");
						}//TODO municipality check
						else{
							Print.chat(event.getEntityPlayer(), "No permission to set mailbox of that player.");
						}
						break;
					}
					case "center": case "central": case "fallback":{
						if(!StatesPermissions.hasPermission(event.getEntityPlayer(), "admin", chunk)){
							Print.chat(event.getEntityPlayer(), "No permission to set the Central/Fallback Mailbox."); return false;
						} reci = "state";
						break;
					}
					default:{
						Print.chat(event.getEntityPlayer(), "Invalid mailbox type.");
						return false;
					}
				}
				tileentity.signText[0] = Formatter.newTextComponentString("&0[&3Mailbox&0]");
				try{
					switch(type){
						case "state": chunk.getState().setMailbox(tileentity.getPos()); break;
						case "municipality": chunk.getMunicipality().setMailbox(tileentity.getPos()); break;
						case "district": chunk.getDistrict().setMailbox(tileentity.getPos()); break;
						case "company": break;//TODO
						case "player":{
							if(event.getEntityPlayer().getGameProfile().getId().toString().equals(reci)){
								event.getEntityPlayer().getCapability(StatesCapabilities.PLAYER, null).setMailbox(tileentity.getPos());
							}
							else{
								StateUtil.getPlayer(reci, true).setMailbox(tileentity.getPos());
							}
							break;
						}
						/*case "center":*/ case "central": case "fallback":{
							StateUtil.getState(-1).setMailbox(tileentity.getPos());
							break;
						}
					}
					this.type = type; cap.setActive(); this.active = true;
					this.sendUpdate(tileentity);
				}
				catch(Exception e){
					e.printStackTrace();
					Print.chat(event.getEntityPlayer(), "Error occured, check log for info.");
				}
				return true;
			}
			else return false;
		}
		else{
			//TODO open mailbox GUI
			Print.chat(event.getEntityPlayer(), "&k!000-000!000-000!");
		}
		return false;
	}

	@Override
	public NBTBase writeToNBT(Capability<SignCapability> capability, EnumFacing side){
		if(!active){ return null; }
		NBTTagCompound compound = new NBTTagCompound();
		compound.setBoolean("sign:active", active);
		compound.setString("sign:type", type);
		compound.setString("sign:recipient", reci.toString());
		if(!mails.isEmpty()){
			try{
				File file = new File(States.getSaveDirectory(), "mailboxes/" + type + "_" + reci + ".nbt");
				NBTTagCompound com = file.exists() ? CompressedStreamTools.read(file) : new NBTTagCompound();
				if(com.hasNoTags()){
					com.setString("type", type); com.setString("id", reci);
				}
				NBTTagList list = new NBTTagList();
				for(ItemStack stack : mails){
					list.appendTag(stack.serializeNBT());
				}
				if(!file.exists()) file.getParentFile().mkdirs();
				CompressedStreamTools.write(com, file);
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		return compound;
	}

	@Override
	public void readNBT(Capability<SignCapability> capability, EnumFacing side, NBTBase nbt){
		if(nbt == null || !(nbt instanceof NBTTagCompound)){ active = false; return; }
		NBTTagCompound compound = (NBTTagCompound)nbt;
		try{
			active = compound.getBoolean("sign:active");
			type = compound.getString("sign:type");
			reci = compound.getString("sign:recipient");
			File file = new File(States.getSaveDirectory(), "mailboxes/" + type + "_" + reci + ".nbt");
			if(file.exists()){
				NBTTagCompound com = CompressedStreamTools.read(file);
				if(type == null) type = com.getString("type");
				if(reci == null) reci = com.getString("id");
				mails.clear(); NBTTagList list = (NBTTagList)com.getTag("mails");
				for(NBTBase base : list){
					try{
						ItemStack stack = new ItemStack((NBTTagCompound)base);
						if(stack.getMetadata() >= 2 && Time.getDate() >= stack.getTagCompound().getLong("Expiry")){
							stack.setItemDamage(1);//expired
						} mails.add(stack);
					}
					catch(Exception e){
						e.printStackTrace();
					}
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
			active = false;
		}
	}

	public NonNullList<ItemStack> getMails(){
		return mails;
	}
	
}