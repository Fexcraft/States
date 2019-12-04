package net.fexcraft.mod.states.data;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import com.google.gson.JsonObject;

import net.fexcraft.lib.common.json.JsonUtil;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.data.root.Ruleable;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraft.command.ICommandSender;

public class Vote {
	
	public VoteType type;
	public boolean council;
	public TreeMap<String, Object> votes;
	public UUID beginner;
	public Ruleable target;
	public long created, expiry;
	public final int id;
	
	public Vote(int id){
		this.id = id; JsonObject obj = JsonUtil.get(getVoteFile(id));
		beginner = UUID.fromString(obj.get("by").getAsString());
		created = obj.get("created").getAsLong();
		String[] arr = obj.get("at").getAsString().split(":");
		int sid = Integer.parseInt(arr[1]);
		switch(arr[0]){
			case "dis": target = StateUtil.getDistrict(sid); break;
			case "mun": target = StateUtil.getMunicipality(sid); break;
			case "st": target = StateUtil.getState(sid); break;
			default: new Exception("Invalid or Unsupported RULEABLE for Voting."); break;
		}
		type = VoteType.valueOf(obj.get("type").getAsString());
		expiry = obj.get("expiry").getAsLong();
		council = obj.get("council").getAsBoolean();
	}
	
	public JsonObject toJsonObject(){
		JsonObject obj = new JsonObject();
		obj.addProperty("id", id);
		obj.addProperty("by", beginner.toString());
		obj.addProperty("created", created);
		if(target instanceof District){
			obj.addProperty("at", "dis:" + ((District)target).getId());
		}
		else if(target instanceof Municipality){
			obj.addProperty("at", "mun:" + ((Municipality)target).getId());
		}
		else if(target instanceof State){
			obj.addProperty("at", "st:" + ((State)target).getId());
		}
		else{
			new Exception("Invalid or Unsupported RULEABLE for Voting.");
		}
		obj.addProperty("assignment", type.name());
		JsonObject votes = new JsonObject();
		for(Map.Entry<String, Object> entry : this.votes.entrySet()){
			if(type.assignment()) votes.addProperty(entry.getKey(), (boolean)entry.getValue());
			else votes.addProperty(entry.getKey(), ((UUID)entry.getValue()).toString());
		}
		obj.add("votes_in", votes);
		obj.addProperty("expiry", expiry);
		obj.addProperty("council", council);
		return obj;
	}

	public void save(){
		JsonObject obj = toJsonObject();
		obj.addProperty("last_save", Time.getDate());
		File file = getVoteFile(id);
		if(!file.getParentFile().exists()){ file.getParentFile().mkdirs(); }
		JsonUtil.write(file, obj);
	}

	public static File getVoteFile(int voteid){
		return new File(States.getSaveDirectory(), "votes/" + voteid + ".json");
	}
	
	public boolean vote(ICommandSender sender, UUID from, UUID vfor){
		if(!prevote(sender, from, true)) return false;
		if(!target.getCouncil().contains(vfor)){
			Print.chat(sender, "You have to vote for someone in the Council.");
			return false;
		}
		votes.put(from.toString(), vfor);
		return true;
	}
	
	public boolean vote(ICommandSender sender, UUID from, boolean agree){
		if(!prevote(sender, from, false)) return false;
		votes.put(from.toString(), agree);
		return true;
	}
	
	public boolean prevote(ICommandSender sender, UUID uuid, boolean assig){
		if(assig != type.assignment()){ return false; }
		if(expired(sender)) return false;
		if(!isVoter(sender, uuid)) return false;
		if(voted(sender, uuid)) return false;
		return true;
	}
	
	private boolean voted(ICommandSender sender, UUID uuid){
		for(String vote : votes.keySet()){
			if(vote.equals(uuid.toString())) Print.chat(sender, "You voted already!"); return true;
		} return false;
	}

	public void summary(ICommandSender sender){
		//TODO
	}

	private boolean isVoter(ICommandSender sender, UUID uuid){
		if(this.council){
			if(!target.getCouncil().contains(uuid)){
				Print.chat(sender, "&cYou need to be council member to vote on this!");
			}
		}
		else{
			if(target instanceof Municipality == false) return false;
			if(!((Municipality)target).getCitizen().contains(uuid)){
				Print.chat(sender, "&cYou need to be a citizen to vote on this!");
			}
		}
		return true;
	}

	public boolean expired(ICommandSender sender){
		if(Time.getDate() >= expiry){
			this.end(); Print.chatnn(sender, "Vote expired already!"); return true;
		} return false;
	}

	private void end(){
		//TODO
	}
	
	public static enum VoteType {
		ASSIGNMENT, CHANGE_REVISER, CHANGE_SETTER, CHANGE_VALUE;
		public boolean assignment(){ return this == ASSIGNMENT; }
	}
	
}
