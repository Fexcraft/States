package net.fexcraft.mod.states.util;

import java.util.TreeMap;

import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.states.data.Rule;

public class RuleMap extends TreeMap<String, Rule>{
	
	private boolean locked;
	
	public final Rule add(Rule rule){
		if(locked){
			Print.log("Attemped to add rule after rulemap init, for external rules use 'addExternal' instead!");
			Static.exception(null, false);
			return null;
		}
		return put(rule.id, rule);
	}
	
	public final Rule addExternal(Rule rule){
		if(!rule.id.contains(":") || rule.id.startsWith(":")){
			Print.log("Please use the `mod_id:rule_id` pattern when adding in external rules!");
			Static.exception(null, false);
			return null;
		}
		return put(rule.id, rule.setExternal());
	}

	public void lock(){
		this.locked = true;
	}

	public boolean hasExternal(){
		return this.keySet().stream().filter(r -> r.contains(":")).count() > 0;
	}

}
