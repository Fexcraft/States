package net.fexcraft.mod.states.data.root;

import java.util.List;
import java.util.UUID;

import net.fexcraft.mod.states.data.Rule.Result;
import net.fexcraft.mod.states.data.Vote;
import net.fexcraft.mod.states.data.sub.RuleHolder;

public interface Ruleable {
	
	public String getRulesetTitle();
	
	public void setRulesetTitle(String title);
	
	public List<UUID> getCouncil();
	
	public UUID getHead();
	
	public void setHead(UUID uuid);
	
	public Ruleable getHigherInstance();
	
	public default boolean hasHigherInstance(){
		return getHigherInstance() != null;
	}
	
	public default boolean isHead(UUID uuid){
		return getHead() != null && getHead().equals(uuid);
	}
	
	public default Result isAuthorized(String rule, UUID uuid){
		return getRuleHolder().get(rule).isAuthorized(this, uuid);
	}
	
	public default Result canRevise(String rule, UUID uuid){
		return getRuleHolder().get(rule).canRevise(this, uuid);
	}
	
	public RuleHolder getRuleHolder();
	
	public List<Vote> getActiveVotes();

}
