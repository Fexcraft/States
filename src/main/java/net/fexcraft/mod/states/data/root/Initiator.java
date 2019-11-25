package net.fexcraft.mod.states.data.root;

/** Note that "citizen" initiator is only usable on Municipalities right now, as States do not hold direct lists. */
public enum Initiator {
	
	INCHARGE,//absolute
	COUNCIL_VOTE,//only agreeable things
	COUNCIL_ANY,//corrupt or trustful
	CITIZEN_VOTE,//truly 'equal'
	CITIZEN_ANY//close to anarchy
	
	//please don't mind the commentary.
	
}
