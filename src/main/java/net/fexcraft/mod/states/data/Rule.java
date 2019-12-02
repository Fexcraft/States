package net.fexcraft.mod.states.data;

import java.util.UUID;

import com.google.gson.JsonArray;

import net.fexcraft.mod.states.data.root.Initiator;

public class Rule {
	
	public final String id;
	private Boolean value;
	private boolean votable;
	public Initiator reviser;
	public Initiator setter;
	
	public Rule(String id, Boolean def, boolean votable, Initiator rev, Initiator setter){
		this.id = id; value = def; reviser = rev; this.setter = setter; this.votable = votable;
	}
	
	public Rule set(boolean val){
		value = val; return this;
	}
	
	public boolean get(){
		return value;
	}
	
	public JsonArray save(){
		JsonArray arr = new JsonArray();
		arr.add(reviser.name()); arr.add(setter.name());
		if(value != null) arr.add(value);
		return arr;
	}
	
	public Rule load(JsonArray array){
		reviser = Initiator.valueOf(array.get(0).getAsString());
		setter = Initiator.valueOf(array.get(1).getAsString());
		if(array.size() > 2) value = array.get(2).getAsBoolean();
		return this;
	}
	
	public Rule copy(){
		return new Rule(id, value, votable, reviser, setter);
	}

	/** Call to see if this player can SET/APPLY this rule. */
	public boolean isAuthorized(RuleHolder holder, UUID uuid){
		switch(setter){
			case CITIZEN_ANY:{
				if(holder instanceof Municipality){
					return ((Municipality)holder).getCitizen().contains(uuid);
				} else return false;//it shouldn't get this far
			}
			case CITIZEN_VOTE:{
				if(holder instanceof Municipality){
					if(!votable) return false;
					//TODO init vote
					return false;
				} else return false;
			}
			case COUNCIL_ANY: return holder.getCouncil().contains(uuid);
			case COUNCIL_VOTE:{
				if(holder.getCouncil().size() == 1) return holder.getCouncil().get(0).equals(uuid);
				if(!votable) return false;
				//TODO init vote
				return false;
			}
			case INCHARGE: return holder.isHead(uuid);
			default: return false;
		}
	}
	
	/** Call to see if this player can REVISE/MODIFY this rule. */
	public boolean canRevise(RuleHolder holder, UUID uuid){
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
			default: return false;
		}
	}

}
