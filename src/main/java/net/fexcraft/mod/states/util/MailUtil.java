package net.fexcraft.mod.states.util;

import java.util.UUID;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.capabilities.FCLCapabilities;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.states.api.ChunkPos;
import net.fexcraft.mod.states.api.District;
import net.fexcraft.mod.states.api.Mailbox.MailType;
import net.fexcraft.mod.states.api.Mailbox.RecipientType;
import net.fexcraft.mod.states.api.Municipality;
import net.fexcraft.mod.states.api.State;
import net.fexcraft.mod.states.api.capabilities.PlayerCapability;
import net.fexcraft.mod.states.impl.SignMailbox;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

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
						if(dis.getMailbox() == null){ rety = RecipientType.MUNICIPALITY; rec = dis.getMunicipality().getId() + ""; continue; }
						mailbox = validate(world, dis.getMailbox(), rety, rec.toString());
						if(mailbox == null){ rety = RecipientType.MUNICIPALITY; rec = dis.getMunicipality().getId() + ""; continue; }
						break;
					}
					case MUNICIPALITY:{
						Municipality mun = StateUtil.getMunicipality(Integer.parseInt(rec));
						if(mun.getMailbox() == null){ rety = RecipientType.STATE; rec = mun.getState().getId() + ""; continue; }
						mailbox = validate(world, mun.getMailbox(), rety, rec.toString());
						if(mailbox == null){ rety = RecipientType.STATE; rec = mun.getState().getId() + ""; continue; }
						break;
					}
					case PLAYER:{
						PlayerCapability cap = StateUtil.getPlayer(rec.toString(), true);
						if(cap == null){ printFailure(ics, 1, rectype, receiver, sender, message, type, expiry, compound, rety, rec); return false; }
						if(cap.getMailbox() == null){ rety = RecipientType.MUNICIPALITY; rec = cap.getMunicipality().getId() + ""; continue; }
						mailbox = validate(world, cap.getMailbox(), rety, rec.toString());
						if(mailbox == null){ rety = RecipientType.MUNICIPALITY; rec = cap.getMunicipality().getId() + "";  continue; }
						break;
					}
					case STATE:{
						State state = StateUtil.getState(Integer.parseInt(rec));
						if(state.getMailbox() == null){
							if(state.getId() >= 0){
								rety = RecipientType.STATE; rec = "-1"; continue;
							}
							else{
								printFailure(ics, 1, rectype, receiver, sender, message, type, expiry, compound, rety, rec); return false;
							}
						}
						mailbox = validate(world, state.getMailbox(), rety, rec.toString());
						if(mailbox == null){
							if(state.getId() >= 0){
								rety = RecipientType.STATE; rec = "-1"; continue;
							}
							else{
								printFailure(ics, 2, rectype, receiver, sender, message, type, expiry, compound, rety, rec); return false;
							}
						}
						break;
					}
				}
			}
			ChunkPos pos = new ChunkPos(mailbox);
			Ticket ticket = null;
			if(!world.isBlockLoaded(mailbox)){
				ticket = ForcedChunksManager.getFreeTicket();
				ForgeChunkManager.forceChunk(ticket, pos);
			}
			TileEntity tile = Static.getServer().getWorld(0).getTileEntity(mailbox);
			if(tile == null){
				printFailure(ics, 3, rectype, receiver, sender, message, type, expiry, compound, null, null); return false;
			}
			try{
				SignMailbox sign = tile.getCapability(FCLCapabilities.SIGN_CAPABILITY, null).getListener(SignMailbox.class, SignMailbox.RESLOC);
				return sign.insert(ics, (TileEntitySign)tile, rectype, receiver.toString(), sender, message, type, expiry, compound);
			}
			catch(Exception e){
				e.printStackTrace();
			}
			if(ticket != null){
				ForgeChunkManager.unforceChunk(ticket, pos);
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

	private static BlockPos validate(World world, BlockPos mailbox, RecipientType type, String receiver){
		ChunkPos pos = new ChunkPos(mailbox); Ticket ticket = null;
		if(!world.isBlockLoaded(mailbox)){
			ticket = ForcedChunksManager.getFreeTicket();
			ForgeChunkManager.forceChunk(ticket, pos);
		}
		TileEntity entity = world.getTileEntity(mailbox);
		if(entity == null || !entity.hasCapability(FCLCapabilities.SIGN_CAPABILITY, null)){
			ForgeChunkManager.unforceChunk(ticket, pos);return null;
		}
		if(entity.getCapability(FCLCapabilities.SIGN_CAPABILITY, null).getListener(SignMailbox.class, SignMailbox.RESLOC).accepts(mailbox, type, receiver)){
			ForgeChunkManager.unforceChunk(ticket, pos); return mailbox;
		}
		else{ ForgeChunkManager.unforceChunk(ticket, pos); return null; }
	}

	private static void printFailure(ICommandSender ics, int i, RecipientType rectype, Object receiver, String sender, String message, MailType type, long expiry, NBTTagCompound compound, RecipientType rety, Object rec){
		if(ics != null) Print.chat(ics, "Mail couldn't be sent, see log for details. ERRLVL:(" + i + ");");
		Print.log(PRFX + " Mailbox for receiver '" + receiver.toString() + (rec == null ? "" : "/" + rec.toString()) + "' not found or errored! Message cannot be sent!");
		Print.log(PRFX + " Content: " + message);
		Print.log(PRFX + " Level: " + i +" ||  Type: " + type.name() + (rety == null ? "" : "/" + rety.name()) + " || Expiry: " + Time.getAsString(Time.getDate() + expiry) + " || NBT: " + (compound == null ? "none" : compound.toString()));
	}
	
}