package net.fexcraft.mod.states.util;

import java.io.File;
import java.util.UUID;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.capabilities.FCLCapabilities;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.data.District;
import net.fexcraft.mod.states.data.Municipality;
import net.fexcraft.mod.states.data.State;
import net.fexcraft.mod.states.data.capabilities.PlayerCapability;
import net.fexcraft.mod.states.data.root.Mailbox.MailType;
import net.fexcraft.mod.states.data.root.Mailbox.RecipientType;
import net.fexcraft.mod.states.impl.SignMailbox;
import net.fexcraft.mod.states.objects.MailItem;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MailUtil {
	
	private static final String PRFX = "[StatesMail]";
	
	public static boolean send(ICommandSender ics, RecipientType rectype, Object receiver, String sender, String message, MailType type){
		return send(ics, rectype, receiver, sender, message, type, Time.DAY_MS * 14);
	}

	public static boolean send(ICommandSender ics, RecipientType rectype, Object receiver, String sender, String message, MailType type, long expiry){
		return send(ics, rectype, receiver, sender, message, type, Time.DAY_MS * 14, null);
	}

	public static boolean send(ICommandSender ics, RecipientType rectype, Object receiver, String sender, String message, MailType type, long expiry, NBTTagCompound compound){
		try{//This was initially intended to run on a separate thread.
			World world = Static.getServer().getWorld(0);
			if(world == null){
				printFailure(ics, 0, rectype, receiver, sender, message, type, expiry, compound, null, null); return false;
			}
			BlockPos mailbox = null; RecipientType rety = rectype; String rec = receiver.toString();
			while(mailbox == null){
				switch(rety){
					case COMPANY: return false;//TODO
					case DISTRICT:{
						District dis = StateUtil.getDistrict(Integer.parseInt(rec));
						if(dis.getMailbox() == null){
							rety = RecipientType.MUNICIPALITY; rec = dis.getMunicipality().getId() + "";
							Print.chat(ics, "&c&oDistrict &7&oMailbox not found, redirecting to &a&oMunicipality&7&o."); continue;
						} else mailbox = dis.getMailbox();
						break;
					}
					case MUNICIPALITY:{
						Municipality mun = StateUtil.getMunicipality(Integer.parseInt(rec));
						if(mun.getMailbox() == null){
							rety = RecipientType.STATE; rec = mun.getState().getId() + "";
							Print.chat(ics, "&c&oMunicipality &7&oMailbox not found, redirecting to &a&oState&7&o."); continue;
						} else mailbox = mun.getMailbox();
						break;
					}
					case PLAYER:{
						PlayerCapability cap = StateUtil.getPlayer(rec.toString(), true);
						if(cap == null){ printFailure(ics, 1, rectype, receiver, sender, message, type, expiry, compound, rety, rec); return false; }
						if(cap.getMailbox() == null){
							rety = RecipientType.MUNICIPALITY; rec = cap.getMunicipality().getId() + "";
							Print.chat(ics, "&c&oPlayer &7&oMailbox not found, redirecting to &a&oMunicipality&7&o."); continue;
						} else mailbox = cap.getMailbox();
						break;
					}
					case STATE:{
						State state = StateUtil.getState(Integer.parseInt(rec));
						if(state.getMailbox() == null){
							if(state.getId() >= 0){
								rety = RecipientType.STATE; rec = "-1";
								Print.chat(ics, "&c&oState &7&oMailbox not found, redirecting to &a&oServer/Fallback&7&o."); continue;
							}
							else{
								printFailure(ics, 1, rectype, receiver, sender, message, type, expiry, compound, rety, rec); return false;
							}
						} else mailbox = state.getMailbox();
						break;
					}
				}
			}
			if(world.isBlockLoaded(mailbox)){
				TileEntity tile = Static.getServer().getWorld(0).getTileEntity(mailbox);
				if(tile == null){
					printFailure(ics, 2, rectype, receiver, sender, message, type, expiry, compound, null, null); return false;
				}
				SignMailbox sign = tile.getCapability(FCLCapabilities.SIGN_CAPABILITY, null).getListener(SignMailbox.class, SignMailbox.RESLOC);
				if(!insert(null, sign, rectype, receiver.toString(), sender, message, type, expiry, compound)){
					printFailure(ics, 3, rectype, receiver, sender, message, type, expiry, compound, null, null);
				}
				sign.updateSize(tile, true);
			}
			else{
				File file = new File(States.getSaveDirectory(), "mailboxes/" + rety.name().toLowerCase() + "_" + rec + ".nbt");
				NBTTagCompound com = file.exists() ? CompressedStreamTools.read(file) : new NBTTagCompound();
				if(com.hasNoTags()){
					com.setString("type", rety.name().toLowerCase());
					com.setString("id", rec); com.setTag("mails", new NBTTagList());
				}
				if(!insert(com, null, rectype, receiver.toString(), sender, message, type, expiry, compound)){
					printFailure(ics, 4, rectype, receiver, sender, message, type, expiry, compound, null, null);
				}
				else{
					if(!file.exists()) file.getParentFile().mkdirs(); CompressedStreamTools.write(com, file);
				}
			}
			if(rectype == RecipientType.PLAYER){
				EntityPlayer player = Static.getServer().getPlayerList().getPlayerByUUID(UUID.fromString(receiver.toString()));
				if(player != null) Print.chat(player, "&6&oYou have got new mail!");
			}
		}
		catch(Exception e){
			e.printStackTrace(); printFailure(ics, -1, rectype, receiver, sender, message, type, expiry, compound, null, null);
		}
		return false;
	}

	private static boolean insert(NBTTagCompound com, SignMailbox box, RecipientType rectype, String receiver, String sender, String message, MailType type, long expiry, NBTTagCompound compound){
		try{
			ItemStack stack = new ItemStack(MailItem.INSTANCE, 1, type.toMetadata());
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setString("Receiver", rectype.name().toLowerCase() + ":" + receiver);
			nbt.setString("Sender", sender);
			nbt.setString("Message", message);
			nbt.setString("Type", type.name());
			nbt.setString("Content", message);
			if(compound != null) nbt.setTag("StatesData", compound);
			if(expiry > 0) nbt.setLong("Expiry", Time.getDate() + expiry);
			stack.setTagCompound(nbt);
			if(box != null){
				box.getMails().add(stack);
			}
			else{
				NBTTagList list = (NBTTagList)com.getTag("mails");
				list.appendTag(stack.serializeNBT());
			}
			return true;
		}
		catch(Exception e){
			e.printStackTrace(); return false;
		}
	}

	private static void printFailure(ICommandSender ics, int i, RecipientType rectype, Object receiver, String sender, String message, MailType type, long expiry, NBTTagCompound compound, RecipientType rety, Object rec){
		if(ics != null) Print.chat(ics, "Mail couldn't be sent, see log for details. ERRLVL:(" + i + ");");
		Print.log(PRFX + " Mailbox for receiver '" + receiver.toString() + (rec == null ? "" : "/" + rec.toString()) + "' not found or errored! Message cannot be sent!");
		Print.log(PRFX + " Content: " + message);
		Print.log(PRFX + " Level: " + i +" ||  Type: " + type.name() + (rety == null ? "" : "/" + rety.name()) + " || Expiry: " + Time.getAsString(Time.getDate() + expiry) + " || NBT: " + (compound == null ? "none" : compound.toString()));
	}
	
}