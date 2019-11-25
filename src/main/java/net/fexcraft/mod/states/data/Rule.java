package net.fexcraft.mod.states.data;

import java.util.UUID;

import com.google.gson.JsonObject;

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
	
	public JsonObject save(){
		JsonObject obj = new JsonObject();
		obj.addProperty("id", id);
		if(value != null) obj.addProperty("value", value);
		obj.addProperty("reviser", reviser.name());
		obj.addProperty("setter", setter.name());
		return obj;
	}
	
	public Rule load(JsonObject obj){
		if(obj.has("value")) value = obj.get("value").getAsBoolean();
		reviser = Initiator.valueOf(obj.get("reviser").getAsString());
		setter = Initiator.valueOf(obj.get("setter").getAsString());
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
