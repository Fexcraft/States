package net.fexcraft.mod.states.util;

import net.fexcraft.mod.lib.util.math.Time;
import net.minecraft.nbt.NBTTagCompound;

public class MailUtil {
	
	public static enum RecipientType {
		
		PLAYER, MUNICIPALITY, STATE, COMPANY;
		
	}
	
	public static enum MailType {
		
		PRIVATE, INVITE, SYSTEM;
		
	}
	
	public static void send(RecipientType rectype, String receiver, String sender, String message, MailType type){
		send(rectype, receiver, sender, message, type, Time.DAY_MS * 14);
	}

	public static void send(RecipientType rectype, String receiver, String sender, String message, MailType type, long expiry){
		send(rectype, receiver, sender, message, type, Time.DAY_MS * 14, null);
	}

	public static void send(RecipientType rectype, String receiver, String sender, String message, MailType type, long expiry, NBTTagCompound compound){
		//TODO
	}

	/*public static void sendMail(Mail mail){
		if(mail.getRecipientType().equals("player")){
			UUID uuid = UUID.fromString(mail.getRecipient());
			EntityPlayerMP player = Static.getServer().getPlayerList().getPlayerByUUID(uuid);
			if(player != null){
				Print.chat(player, "&0[&eSt&0]&6 You have got new mail!");
			}
		}
		mail.save();
	}

	public static List<Mail> gatherMailOf(String type, String string, boolean read){
		File folder = new File(States.getSaveDirectory(), "mails/" + type + "/" + string + "/");
		ArrayList<Mail> list = new ArrayList<>();
		if(!folder.exists()){ return list; }
		for(File file : folder.listFiles()){
			if(file.getName().endsWith(read ? ".read" : ".unread")){
				try{
					list.add(new GenericMail(file.getName().replace(read ? ".read" : ".unread", ""), type, string));
				}
				catch (Exception e){
					e.printStackTrace();
				}
			}
		}
		return list;
	}

	public static int getUnreadMailsOf(String rectype, String string){
		File folder = new File(States.getSaveDirectory(), "mails/" + rectype + "/" + string + "/");
		int i = 0;
		if(folder.exists()){
			for(String file : folder.list()){
				if(file.endsWith(".unread")){
					i++;
				}
			}
		}
		return i;
	}*/
	
	///---///---///
	
	
}