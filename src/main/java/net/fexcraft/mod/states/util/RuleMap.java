package net.fexcraft.mod.states.util;

import java.util.TreeMap;

import net.fexcraft.mod.states.data.Rule;

public class RuleMap extends TreeMap<String, Rule>{
	
	public final Rule add(Rule rule){
		return put(rule.id, rule);
	}

}
