package net.fexcraft.mod.states.data;

import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.fexcraft.mod.states.api.Municipality;

public class RuleSet {
	
	public final TreeMap<String, Rule> rules = new TreeMap<>();
	public final RuleHolder ruleholder;
	public String name = "Standard Ruleset";
	
	public RuleSet(RuleHolder holder, Collection<Rule> rules){
		this.ruleholder = holder;
	}
	
	public void load(JsonObject obj){
		name = obj.has("name") ? obj.get("name").getAsString() : null;
		//
		if(obj.has("set")){
			JsonArray array = obj.get("set").getAsJsonArray();
			for(JsonElement elm : array){
				JsonObject jsn = elm.getAsJsonObject();
				if(!jsn.has("id")) continue;
				Rule rule = rules.get(jsn.get("id").getAsString());
				if(rule == null) continue;
				rule.load(jsn); continue;
			}
		}
	}
	
	public JsonObject save(){
		JsonObject obj = new JsonObject();
		obj.addProperty("name", name);
		//
		JsonArray array = new JsonArray();
		for(Rule rule : rules.values()) array.add(rule.save());
		obj.add("set", array);
		return obj;
	}
	
	public boolean get(String string){
		return rules.get(string).get();
	}
	
	public Rule getRule(String string){
		return rules.get(string);
	}
	
	public boolean isAuthorized(String rule, UUID uuid){
		return getRule(rule).isAuthorized(ruleholder, uuid);
	}
	
	public boolean canRevise(String rule, UUID uuid){
		return getRule(rule).canRevise(ruleholder, uuid);
	}
	
	public static interface RuleHolder {
		
		public RuleSet getRules();
		
		public default boolean getRuleValue(String string){
			return getRules().get(string);
		}
		
		public default Rule getRule(String string){
			return getRules().getRule(string);
		}
		
		public List<UUID> getCouncil();
		
		public UUID getHead();
		
		public void setHead(UUID uuid);
		
		public default boolean isHead(UUID uuid){
			return getHead().equals(uuid);
		}
		
		public default boolean isAuthorized(String rule, UUID uuid){
			return getRule(rule).isAuthorized(this, uuid);
		}
		
		public default boolean canRevise(String rule, UUID uuid){
			return getRule(rule).canRevise(this, uuid);
		}
		
	}
	
	public static class Rule {
		
		public final String id;
		private Boolean value;
		public Initiator reviser;
		public Initiator setter;
		
		public Rule(String id, Boolean def, Initiator rev, Initiator setter){
			this.id = id; value = def; reviser = rev; this.setter = setter;
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
	
	/** Note that "citizen" initiator is only usable on Municipalities right now, as States do not hold direct lists. */
	public static enum Initiator {
		
		INCHARGE,//absolute
		COUNCIL_VOTE,//only agreeable things
		COUNCIL_ANY,//corrupt or trustful
		CITIZEN_VOTE,//truly 'equal'
		CITIZEN_ANY//close to anarchy
		
		//please don't mind the commentary.
		
	}

}
