package net.fexcraft.mod.states.data.root;

/** Note that "citizen" initiator is only usable on Municipalities right now, as States do not hold direct lists. */
public enum Initiator {
	
	HIGHERINCHARGE(true),
	INCHARGE(true),
	COUNCIL_VOTE(false),
	COUNCIL_ANY(true),
	CITIZEN_VOTE(false),
	CITIZEN_ANY(true),
	NONE(true);
	
	private final boolean setter;
	
	private Initiator(boolean setter){
		this.setter = setter;
	}
	
	public boolean isValidAsSetter(){
		return setter;
	}

	public boolean isCitizenVote(){
		return this == CITIZEN_ANY || this == CITIZEN_VOTE;
	}
	
}
