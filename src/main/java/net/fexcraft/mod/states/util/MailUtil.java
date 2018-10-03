package net.fexcraft.mod.states.util;

import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.lib.util.common.Static;
import net.fexcraft.mod.lib.util.math.Time;
import net.fexcraft.mod.states.api.ChunkPos;
import net.fexcraft.mod.states.api.District;
import net.fexcraft.mod.states.api.Mailbox;
import net.fexcraft.mod.states.api.Mailbox.MailType;
import net.fexcraft.mod.states.api.Mailbox.RecipientType;
import net.fexcraft.mod.states.api.Municipality;
import net.fexcraft.mod.states.api.State;
import net.fexcraft.mod.states.api.capabilities.PlayerCapability;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

public class MailUtil {
	
	private static final String PRFX = "[StatesMail]";
	
	public static void send(RecipientType rectype, Object receiver, String sender, String message, MailType type){
		send(rectype, receiver, sender, message, type, Time.DAY_MS * 14);
	}

	public static void send(RecipientType rectype, Object receiver, String sender, String message, MailType type, long expiry){
		send(rectype, receiver, sender, message, type, Time.DAY_MS * 14, null);
	}

	public static void send(RecipientType rectype, Object receiver, String sender, String message, MailType type, long expiry, NBTTagCompound compound){
		Static.getServer().addScheduledTask(() -> {
			try{
				BlockPos mailboxpos = getMailbox(rectype, receiver.toString(), null);
				World world = Static.getServer().getWorld(0);
				if(world == null || mailboxpos == null){
					printFailure(0, rectype, receiver, sender, message, type, expiry, compound);
					return;
				}
				ChunkPos pos = new ChunkPos(mailboxpos);
				Ticket ticket = null;
				if(!world.isBlockLoaded(mailboxpos)){
					ticket = ForcedChunksManager.getFreeTicket();
					ForgeChunkManager.forceChunk(ticket, pos);
				}
				Mailbox mailbox = (Mailbox)Static.getServer().getWorld(0).getTileEntity(mailboxpos);
				if(mailbox == null){
					printFailure(1, rectype, receiver, sender, message, type, expiry, compound);
				}
				if(!mailbox.accepts(rectype, receiver.toString())){
					printFailure(2, rectype, receiver, sender, message, type, expiry, compound);
				}
				try{
					mailbox.insert(rectype, receiver.toString(), sender, message, type, expiry, compound);
				}
				catch(Exception e){
					e.printStackTrace(); printFailure(3, rectype, receiver, sender, message, type, expiry, compound);
				}
				if(ticket != null){
					ForgeChunkManager.unforceChunk(ticket, pos);
				}
			}
			catch(Exception e){
				e.printStackTrace(); printFailure(-1, rectype, receiver, sender, message, type, expiry, compound);
			}
		});
	}

	private static void printFailure(int i, RecipientType rectype, Object receiver, String sender, String message, MailType type, long expiry, NBTTagCompound compound){
		Print.log(PRFX + " Mailbox for receiver '" + receiver.toString() + "' not found or errored! Message cannot be sent!");
		Print.log(PRFX + " Content: " + message);
		Print.log(PRFX + " Level: " + i +" ||  Type: " + type.name() + " || Expiry: " + Time.getAsString(Time.getDate() + expiry) + " || NBT: " + (compound == null ? "none" : compound.toString()));
	}
	
	public static BlockPos getMailbox(RecipientType type, String receiver, Integer id){
		switch(type){
			case PLAYER:{
				PlayerCapability cap = StateUtil.getPlayer(receiver, true);
				if(cap == null) return null;
				if(cap.getMailbox() == null) return getMailbox(RecipientType.MUNICIPALITY, receiver, cap.getMunicipality().getId());
				else return cap.getMailbox();
			}
			case COMPANY: break; //TODO
			case DISTRICT:{
				District dis = StateUtil.getDistrict(id == null ? Integer.parseInt(receiver) : id, false);
				if(dis == null) return null;
				return dis.getMailbox() == null ? getMailbox(RecipientType.MUNICIPALITY, receiver, dis.getMunicipality().getId()) : dis.getMailbox();
			}
			case MUNICIPALITY:{
				Municipality mun = StateUtil.getMunicipality(id == null ? Integer.parseInt(receiver) : id, false);
				if(mun == null) return null;
				return mun.getMailbox() == null ? getMailbox(RecipientType.STATE, receiver, mun.getState().getId()) : mun.getMailbox();
			}
			case STATE:{
				State state = StateUtil.getState(id == null ? Integer.parseInt(receiver) : id, false);
				if(state == null) return null; return state.getMailbox();
			}
			default: return null;
		}
		return null;
	}
	
}