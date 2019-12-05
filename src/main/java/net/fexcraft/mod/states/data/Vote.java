package net.fexcraft.mod.states.data;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.fexcraft.lib.common.json.JsonUtil;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.data.root.Initiator;
import net.fexcraft.mod.states.data.root.Ruleable;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraft.command.ICommandSender;

public class Vote {
	
	public VoteType type;
	public boolean council, new_value;
	public TreeMap<String, Object> votes = new TreeMap<>();
	public UUID beginner;
	public Ruleable target;
	public long created, expiry;
	public final int id;
	public Initiator to;
	public String rule;
	public Boolean rev;
	
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
		JsonObject votes_in = obj.get("votes_in").getAsJsonObject();
		for(Entry<String, JsonElement> entry : votes_in.entrySet()){
			if(type.assignment()){
				votes.put(entry.getKey(), UUID.fromString(entry.getValue().getAsString()));
			}
			else{
				votes.put(entry.getKey(), entry.getValue().getAsBoolean());
			}
		}
		new_value = type.valueful() ? obj.get("new_value").getAsBoolean() : false;
		to = !type.valueful() && !type.assignment() ? Initiator.valueOf(obj.get("to").getAsString()) : null;
		rule = !type.assignment() ? obj.get("rule").getAsString() : null;
		rev = !type.valueful() && !type.assignment() ? obj.get("rev").getAsBoolean() : null;
	}
	
	public Vote(int id, String rule, UUID beginner, long created, long expiry, Ruleable target, VoteType type, boolean council, Boolean rev, Object value){
		this.id = id; this.beginner = beginner; this.rule = rule; this.created = created; this.expiry = expiry;
		this.target = target; this.type = type; this.council = council; this.rev = rev;
		switch(type){
			case ASSIGNMENT:{
				new_value = false;
				to = null; return;
			}
			case CHANGE_REVISER:
			case CHANGE_SETTER:{
				new_value = false;
				to = (Initiator)value;
				return;
			}
			case CHANGE_VALUE:{
				new_value = (boolean)value;
				to = null; return;
			}
		}
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
		if(type.valueful()) obj.addProperty("new_value", new_value);
		else if(!type.assignment()) obj.addProperty("to", to.name());
		if(!type.assignment()) obj.addProperty("rule", rule);
		if(!type.assignment() && !type.valueful()) obj.addProperty("rev", rev);
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

	public File getVoteFile(){
		return getVoteFile(id);
	}
	
	/** For assignment-type votes. */
	public boolean vote(ICommandSender sender, UUID from, UUID vfor){
		if(!prevote(sender, from, true)) return false;
		if(!target.getCouncil().contains(vfor)){
			Print.chat(sender, "You have to vote for someone in the Council.");
			return false;
		}
		votes.put(from.toString(), vfor);
		return true;
	}
	
	/** For rule-type votes. */
	public boolean vote(ICommandSender sender, UUID from, boolean agree){
		if(!prevote(sender, from, false)) return false;
		votes.put(from.toString(), agree);
		Print.chat(sender, "&7Use &9/st-vote status " + id + " &7to see the summary!");
		return true;
	}
	
	public boolean prevote(ICommandSender sender, UUID uuid, boolean assig){
		if(assig != type.assignment()){ return false; }
		if(expired(sender)) return false;
		if(!isVoter(sender, uuid)) return false;
		if(voted(sender, uuid)) return false;
		Print.chat(sender, "&7Use &9/st-vote status " + id + " &7to see the summary!");
		return true;
	}
	
	private boolean voted(ICommandSender sender, UUID uuid){
		for(String vote : votes.keySet()){
			if(vote.equals(uuid.toString())) Print.chat(sender, "You voted already!"); return true;
		} return false;
	}

	public void summary(ICommandSender sender){
		Print.chat(sender, "&9Created: &7" + timeformat(created));
		Print.chat(sender, "&9Expiry: &7" + timeformat(expiry));
		Print.chat(sender, "&6Authorized: &b" + (council ? "council vote" : "citizen vote"));
		if(target instanceof District){
			Print.chat(sender, "&9Type: &7" + (type.assignment() ? "Assignment of new Manager" : "Vote For Rule Change"));
		}
		else if(target instanceof Municipality){
			Print.chat(sender, "&9Type: &7" + (type.assignment() ? "Assignment of new Mayor" : "Vote For Rule Change"));
		}
		else if(target instanceof State){
			Print.chat(sender, "&9Type: &7" + (type.assignment() ? "Assignment of new Head" : "Vote For Rule Change"));
		}
		else{
			Print.chat(sender, "&9Type: &7INVALID TARGET");
		}
		if(!type.assignment()){
			Print.chat(sender, "&9Rule: &7" + rule);
			switch(type){
				case CHANGE_REVISER:
					Print.chat(sender, "&9Change: &7Reviser to &a" + to);
					break;
				case CHANGE_SETTER:
					Print.chat(sender, "&9Change: &7Setter to &a" + to);
					break;
				case CHANGE_VALUE:
					Print.chat(sender, "&9Change: &7Value to &a" + new_value);
					break;
				default: break;
			}
		}
		Print.chat(sender, "&6Status:");
		if(type.assignment()){
			TreeMap<String, Integer> votes_for = new TreeMap<>();
			for(Map.Entry<String, Object> vote : votes.entrySet()){
				String uuid = ((UUID)vote.getValue()).toString();
				if(votes_for.containsKey(uuid)){
					votes_for.put(uuid, votes_for.get(uuid) + 1);
				} else votes_for.put(uuid, 1);
			}
			int summary = 0; for(int i : votes_for.values()) summary += i;
			for(String str : votes_for.keySet()){
				Print.chat(sender, "&a" + percent(votes_for.get(str), summary) + "% &7- &e" + Static.getPlayerNameByUUID(str));
			}
			Print.chat(sender, "&6" + votes.size() + " &7 votes received of &2" + (council ? target.getCouncil().size() : ((Municipality)target).getCitizen().size()) + " &7expected.");
		}
		else{
			int agree = 0, disagree = 0;
			for(Object obj : votes.values()){
				boolean bool = (boolean)obj; if(bool) agree++; else disagree++;
			}
			Print.chat(sender, "&e" + percent(agree, votes.size()) + "% &7- &afor the change");
			Print.chat(sender, "&e" + percent(disagree, votes.size()) + "% &7- &cagainst the change");
			Print.chat(sender, "&6" + votes.size() + " &7 votes received of &2" + (council ? target.getCouncil().size() : ((Municipality)target).getCitizen().size()) + " &7expected.");
		}
	}
	
	private static final SimpleDateFormat format = new SimpleDateFormat("dd.LLL.yyyy HH:mm.ss");

	private String timeformat(long date){
		return format.format(new Date(date));
	}

	private int percent(int val, int summary){
		return summary == 0 ? 0 : (val * 100) / summary;
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
		public boolean valueful(){ return this == ASSIGNMENT; }
	}

	public String targetAsString(){
		if(target instanceof District){
			return "dis:" + ((District)target).getId();
		}
		else if(target instanceof Municipality){
			return "mun:" + ((Municipality)target).getId();
		}
		else if(target instanceof State){
			return "st:" + ((State)target).getId();
		}
		else{
			new Exception("Invalid or Unsupported RULEABLE for Voting.");
		}
		return "invalid target";
	}
	
}
