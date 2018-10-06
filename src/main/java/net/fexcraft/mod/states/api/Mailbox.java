package net.fexcraft.mod.states.api;

import net.minecraft.nbt.NBTTagCompound;

public interface Mailbox {
	
	public static enum RecipientType {
		
		PLAYER, DISTRICT, MUNICIPALITY, STATE, COMPANY;
		
	}
	
	public static enum MailType {
		
		PRIVATE(2), INVITE(3), SYSTEM(4);
		
		MailType(int id){ this.id = id; }
		
		private int id;

		public int toMetadata(){ return id; }
		
	}

	public boolean accepts(RecipientType rectype, String receiver);

	public void insert(RecipientType rectype, String receiver, String sender, String message, MailType type, long expiry, NBTTagCompound compound);

}