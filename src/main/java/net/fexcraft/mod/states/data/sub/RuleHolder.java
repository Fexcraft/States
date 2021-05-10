package net.fexcraft.mod.states.data.sub;

import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.fexcraft.mod.states.data.Rule;
import net.fexcraft.mod.states.data.root.Loadable;
import net.fexcraft.mod.states.data.root.Ruleable;
import net.fexcraft.mod.states.util.RuleMap;

public class RuleHolder implements Loadable {
	
	private RuleMap rules = new RuleMap();
	public final Ruleable root;
	
	public RuleHolder(Ruleable root){
		this.root = root;
	}
	
	public RuleMap getMap(){
		return rules;
	}
	
	public boolean getValue(String string){
		return rules.get(string).get();
	}
	
	public Rule get(String string){
		return rules.get(string);
	}

	public void add(Rule rule){
		rules.add(rule);
	}

	@Override
	public void load(JsonObject obj){
		if(!obj.has("rules")) return;
		JsonObject rls = obj.get("rules").getAsJsonObject();
		for(Map.Entry<String, JsonElement> entry : rls.entrySet()){
			Rule rule = rules.get(entry.getKey());
			if(rule != null) rule.load(entry.getValue().getAsString());
		}
	}

	public void loadEx(JsonObject obj){
		if(!obj.has("ex-rules") || !rules.hasExternal()) return;
		JsonObject rls = obj.get("ex-rules").getAsJsonObject();
		for(Map.Entry<String, JsonElement> entry : rls.entrySet()){
			Rule rule = rules.get(entry.getKey());
			if(rule != null) rule.load(entry.getValue().getAsString());
		}
	}

	@Override
	public void save(JsonObject obj){
		JsonObject rells = new JsonObject();
		for(Rule rule : rules.values()) if(!rule.isExternal()) rells.addProperty(rule.id, rule.save());
		obj.add("rules", rells);
		//
		if(rules.hasExternal()){
			JsonObject erells = new JsonObject();
			for(Rule rule : rules.values()) if(rule.isExternal()) erells.addProperty(rule.id, rule.save());
			obj.add("ex-rules", erells);
		}
	}
	
}
