package net.fexcraft.mod.states.data.sub;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.fexcraft.lib.common.json.JsonUtil;
import net.fexcraft.mod.states.data.Rule.Result;
import net.fexcraft.mod.states.data.Vote;
import net.fexcraft.mod.states.data.root.Layer;
import net.fexcraft.mod.states.data.root.Loadable;
import net.fexcraft.mod.states.util.StateUtil;

public class Manageable implements Loadable {
	
	private Layer root;
	private String head_save_string;
	private boolean parent_head;
	//
	private String ru_title;
	private ArrayList<UUID> council;
	private ArrayList<Vote> active_votes = new ArrayList<>();
	private RuleHolder rules;
	private UUID head;
	
	public Manageable(Layer layer, boolean has_council, boolean has_parent, String string){
		root = layer;
		if(has_council) council = new ArrayList<>();
		head_save_string = string;
		parent_head = has_parent;
	}
	
	public String getRulesetTitle(){
		return ru_title;
	}
	
	public void setRulesetTitle(String title){
		ru_title = title;
	}
	
	public List<UUID> getCouncil(){
		return council == null ? hasHigherInstance() ? getHigherInstance().getCouncil() : null : council;
	}
	
	public boolean isInCouncil(UUID uuid){
		return council == null ? false : council.contains(uuid);
	}
	
	public UUID getHead(){
		return head;
	}
	
	public void setHead(UUID uuid){
		head = uuid;
	}
	
	public Manageable getHigherInstance(){
		return (Manageable)root.getParent();
	}
	
	public boolean hasHigherInstance(){
		return root.getParent() != null && root.getParent().getId() >= 0;
	}
	
	public boolean isHead(UUID uuid){
		return getHead() != null && getHead().equals(uuid) || (parent_head && getHigherInstance().getHead() != null && getHigherInstance().getHead().equals(uuid));
	}
	
	public Result isAuthorized(String rule, UUID uuid){
		return getRuleHolder().get(rule).isAuthorized(this, uuid);
	}
	
	public Result canRevise(String rule, UUID uuid){
		return getRuleHolder().get(rule).canRevise(this, uuid);
	}
	
	public RuleHolder getRuleHolder(){
		return rules;
	}
	
	public void linkRuleHolder(RuleHolder ruleholder){
		rules = ruleholder;
	}
	
	public List<Vote> getActiveVotes(){
		return active_votes;
	}

	@Override
	public void load(JsonObject obj){
		if(obj.has(head_save_string)) head = UUID.fromString(obj.get(head_save_string).getAsString());
		else if(obj.has("head")) head = UUID.fromString(obj.get("head").getAsString());
		else head = null;
		if(council != null) council = JsonUtil.jsonArrayToUUIDArray(JsonUtil.getIfExists(obj, "council", new JsonArray()).getAsJsonArray());
		ru_title = JsonUtil.getIfExists(obj, "ruleset", "Standard Ruleset");
		if(obj.has("votes")){
			ArrayList<Integer> list = JsonUtil.jsonArrayToIntegerArray(obj.get("votes").getAsJsonArray());
			for(int i : list){
				Vote vote = StateUtil.getVote(this, i);
				if(vote == null || vote.expired(null)) continue;
				active_votes.add(vote);
			}
		}
	}

	@Override
	public void save(JsonObject obj){
		if(head != null) obj.addProperty("head", head.toString());
		if(council != null) obj.add("council", JsonUtil.getArrayFromUUIDList(council));
		obj.addProperty("ruleset", ru_title);
		if(!active_votes.isEmpty()){
			JsonArray array = new JsonArray();
			for(Vote vote : active_votes) array.add(vote.id);
			obj.add("votes", array);
		}
	}
	
	public Layer getLayer(){
		return root;
	}

}
