package net.fexcraft.mod.states.data.root;

public enum AnnounceLevel {
	
	ALL(true),
	UNION(false),
	UNION_ALL(true),
	STATE(false),
	STATE_ALL(true),
	MUNICIPALITY(false),
	MUNICIPALITY_ALL(true),
	DISTRICT(true),
	AREAL(true);
	
	/** If the message should be sent to all being on the defined territory, default true. */
	private boolean toall;
	
	AnnounceLevel(boolean send){
		toall = send;
	}
	
	public boolean sendToAll(){
		return toall;
	}
	
}