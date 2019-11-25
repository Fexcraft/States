package net.fexcraft.mod.states.data;

import java.util.List;
import java.util.UUID;

public interface RuleHolder {
	
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
