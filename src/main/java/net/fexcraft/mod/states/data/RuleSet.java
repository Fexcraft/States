package net.fexcraft.mod.states.data;

import java.util.Collection;
import java.util.TreeMap;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Deprecated
public class RuleSet {
	
	public final TreeMap<String, Rule> rules = new TreeMap<>();
	public final TreeMap<String, Norm> norms = new TreeMap<>();
	public final RuleHolder ruleholder;
	public String name = "Standard Ruleset";
	
	public RuleSet(RuleHolder holder, Collection<Rule> rules){
		this.ruleholder = holder; for(Rule rule : rules) this.rules.put(rule.id, rule.copy());
	}
	
	public void load(JsonObject obj){
		name = obj.has("name") ? obj.get("name").getAsString() : null;
		//
		if(obj.has("rules")){
			JsonArray array = obj.get("rules").getAsJsonArray();
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
		obj.add("rules", array);
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
	
	public static class Norm {
		
		
		
	}

}
