package net.fexcraft.mod.states.data.root;

import java.util.Map;

import net.fexcraft.mod.states.data.Rule;

public interface RuleHolder {
	
	public Map<String, Rule> getRules();
	
	public default boolean getRuleValue(String string){
		return getRules().get(string).get();
	}
	
	public default Rule getRule(String string){
		return getRules().get(string);
	}
	
}
