package net.fexcraft.mod.states.api;

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

}