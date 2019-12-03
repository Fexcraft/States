package net.fexcraft.mod.states.data;

import java.util.UUID;

import net.fexcraft.mod.states.data.root.Initiator;
import net.fexcraft.mod.states.data.root.Ruleable;

public class Rule {
	
	public final String id;
	private Boolean value;
	private boolean votable;
	public Initiator reviser;
	public Initiator setter;
	
	public Rule(String id, Boolean def, boolean votable, Initiator rev, Initiator setter){
		this.id = id; value = def; reviser = rev; this.setter = setter; this.votable = votable;
		if(!setter.isValidAsSetter()){
			if(setter == Initiator.CITIZEN_VOTE) setter = Initiator.COUNCIL_ANY;
			else if(setter == Initiator.COUNCIL_VOTE) setter = Initiator.INCHARGE;
		}
	}
	
	public Rule set(boolean val){
		value = val; return this;
	}
	
	public boolean get(){
		return value;
	}
	
	public String save(){
		String string = reviser.name() + "," + setter.name();
		if(value != null) string += "," + value; return string;
	}
	
	public Rule load(String string){
		String[] arr = string.split(",");
		reviser = Initiator.valueOf(arr[0]);
		setter = Initiator.valueOf(arr[1]);
		if(arr.length > 2) value = Boolean.parseBoolean(arr[2]);
		return this;
	}
	
	public Rule copy(){
		return new Rule(id, value, votable, reviser, setter);
	}

	/** Call to see if this player can SET/APPLY this rule. */
	public boolean isAuthorized(Ruleable holder, UUID uuid){
		switch(setter){
			case CITIZEN_ANY:{
				if(holder instanceof Municipality){
					return ((Municipality)holder).getCitizen().contains(uuid);
				} else return false;//it shouldn't get this far
			}
			case COUNCIL_ANY: return holder.getCouncil().contains(uuid);
			case INCHARGE: return holder.isHead(uuid);
			case HIGHERINCHARGE:
				return holder.hasHigherInstance() ? holder.getHigherInstance().isHead(uuid) : holder.isHead(uuid);
			default: return false;
		}
	}
	
	/** Call to see if this player can REVISE/MODIFY this rule. */
	public boolean canRevise(Ruleable holder, UUID uuid){
		switch(setter){
			case CITIZEN_ANY:{
				if(holder instanceof Municipality){
					return ((Municipality)holder).getCitizen().contains(uuid);
				} else return false;
			}
			case CITIZEN_VOTE:{
				if(holder instanceof Municipality){
					//TODO init vote
					return false;
				} else return false;
			}
			case COUNCIL_ANY: return holder.getCouncil().contains(uuid);
			case COUNCIL_VOTE:{
				if(holder.getCouncil().size() == 1) return holder.getCouncil().get(0).equals(uuid);
				//TODO init vote
				return false;
			}
			case INCHARGE: return holder.isHead(uuid);
			case HIGHERINCHARGE:
				return holder.hasHigherInstance() ? holder.getHigherInstance().isHead(uuid) : holder.isHead(uuid);
			default: return false;
		}
	}

}
