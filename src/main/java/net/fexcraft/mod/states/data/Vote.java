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
import net.fexcraft.mod.states.data.capabilities.PlayerCapability;
import net.fexcraft.mod.states.data.root.AnnounceLevel;
import net.fexcraft.mod.states.data.root.Initiator;
import net.fexcraft.mod.states.data.root.Mailbox.MailType;
import net.fexcraft.mod.states.data.root.Mailbox.RecipientType;
import net.fexcraft.mod.states.data.root.Populated;
import net.fexcraft.mod.states.data.sub.Manageable;
import net.fexcraft.mod.states.util.MailUtil;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraft.command.ICommandSender;

public class Vote {
	
	public final Manageable holder;
	public VoteType type;
	public boolean council, new_value, ended;
	public TreeMap<String, Object> votes = new TreeMap<>();
	public UUID beginner;
	public long created, expiry;
	public final int id;
	public Initiator to;
	public String rule;
	public Boolean rev;
	
	public Vote(Manageable holder, int id){
		this.id = id; JsonObject obj = JsonUtil.get(getVoteFile(id));
		beginner = UUID.fromString(obj.get("by").getAsString());
		created = obj.get("created").getAsLong();
		if(holder == null){
			String[] arr = obj.get("at").getAsString().split(":");
			int sid = Integer.parseInt(arr[1]);
			switch(arr[0]){
				case "dis": this.holder = StateUtil.getDistrict(sid).manage; break;
				case "mun": this.holder = StateUtil.getMunicipality(sid).manage; break;
				case "st": this.holder = StateUtil.getState(sid).manage; break;
				default: this.holder = null; new Exception("Invalid or Unsupported RULEABLE for Voting."); break;
			}
		} else this.holder = holder;
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
		ended = obj.get("ended").getAsBoolean();
	}
	
	public Vote(int id, String rule, UUID beginner, long created, long expiry, Manageable target, VoteType type, boolean council, Boolean rev, Object value){
		this.id = id; this.beginner = beginner; this.rule = rule; this.created = created; this.expiry = expiry;
		this.holder = target; this.type = type; this.council = council; this.rev = rev; holder.getActiveVotes().add(this);
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
			case ABANDONMENT:{
				new_value = false;
				to = null; return;
			}
			default:
				break;
		}
	}
	
	public JsonObject toJsonObject(){
		JsonObject obj = new JsonObject();
		obj.addProperty("id", id);
		obj.addProperty("by", beginner.toString());
		obj.addProperty("created", created);
		if(holder.getLayer() instanceof District){
			obj.addProperty("at", "dis:" + ((District)holder.getLayer()).getId());
		}
		else if(holder.getLayer() instanceof Municipality){
			obj.addProperty("at", "mun:" + ((Municipality)holder.getLayer()).getId());
		}
		else if(holder.getLayer() instanceof State){
			obj.addProperty("at", "st:" + ((State)holder.getLayer()).getId());
		}
		else{
			new Exception("Invalid or Unsupported RULEABLE for Voting.");
		}
		obj.addProperty("type", type.name());
		JsonObject votes = new JsonObject();
		for(Map.Entry<String, Object> entry : this.votes.entrySet()){
			if(!type.assignment()) votes.addProperty(entry.getKey(), (boolean)entry.getValue());
			else votes.addProperty(entry.getKey(), ((UUID)entry.getValue()).toString());
		}
		obj.add("votes_in", votes);
		obj.addProperty("expiry", expiry);
		obj.addProperty("council", council);
		if(type.valueful()) obj.addProperty("new_value", new_value);
		if(!type.assignment() && !type.valueful()) obj.addProperty("to", to.name());
		if(!type.assignment()) obj.addProperty("rule", rule);
		if(!type.assignment() && !type.valueful()) obj.addProperty("rev", rev);
		obj.addProperty("ended", ended);
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
		if(!holder.isInCouncil(vfor)){
			Print.chat(sender, "You have to vote for someone in the Council.");
			return false;
		}
		votes.put(from.toString(), vfor);
		if(!shouldEnd(sender)) Print.chat(sender, "&7Use &9/st-vote status " + id + " &7to see the summary!");
		return true;
	}

	/** For rule-type votes. */
	public boolean vote(ICommandSender sender, UUID from, boolean agree){
		if(!prevote(sender, from, false)) return false;
		votes.put(from.toString(), agree);
		if(!shouldEnd(sender)) Print.chat(sender, "&7Use &9/st-vote status " + id + " &7to see the summary!");
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
		expired(null);//checking expiration
		Print.chat(sender, "&9Created: &7" + timeformat(created));
		Print.chat(sender, "&9Expiry: &7" + timeformat(expiry));
		Print.chat(sender, "&6Authorized: &b" + (council ? "council vote" : "citizen vote"));
		String typestr = null;
		switch(type){
			case ABANDONMENT:
				typestr = "Abandonement of " + (holder.getLayer() instanceof State ? "State" : "Municipality");
				break;
			case ASSIGNMENT:
				typestr = "Assignment of new ";
				if(holder.getLayer() instanceof District){
					typestr += "Manager";
				}
				else if(holder.getLayer() instanceof Municipality){
					typestr += "Mayor";
				}
				else if(holder.getLayer() instanceof State){
					typestr += "Head";
				}
				else{
					typestr += "INVALID TARGET";
				}
				break;
			case CHANGE_REVISER:
			case CHANGE_SETTER:
			case CHANGE_VALUE:
				typestr = "Vote For Rule Change";
				break;
			
		}
		Print.chat(sender, "&9Type: &7" + typestr);
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
			Print.chat(sender, "&6" + votes.size() + " &7votes received of &2" + (council ? holder.getCouncil().size() : ((Populated)holder.getLayer()).getAllResidentCount()) + " &7expected.");
		}
		else{
			int agree = 0, disagree = 0;
			for(Object obj : votes.values()){
				boolean bool = (boolean)obj; if(bool) agree++; else disagree++;
			}
			Print.chat(sender, "&e" + percent(agree, votes.size()) + "% &7- &afor the change");
			Print.chat(sender, "&e" + percent(disagree, votes.size()) + "% &7- &cagainst the change");
			Print.chat(sender, "&6" + votes.size() + " &7votes received of &2" + (council ? holder.getCouncil().size() : ((Populated)holder.getLayer()).getAllResidentCount()) + " &7expected.");
		}
		if(ended) Print.chat(sender, "&6&lVOTE ENDED");
	}
	
	private static final SimpleDateFormat format = new SimpleDateFormat("dd.LLL.yyyy HH:mm.ss");

	private String timeformat(long date){
		return format.format(new Date(date));
	}

	private int percent(int val, int summary){
		return summary == 0 ? 0 : (val * 100) / summary;
	}

	public boolean isVoter(ICommandSender sender, UUID uuid){
		if(this.council){
			if(!holder.isInCouncil(uuid)){
				if(sender != null) Print.chat(sender, "&cYou need to be council member to vote on this!");
				return false;
			}
		}
		else{
			if(holder.getLayer() instanceof Populated == false) return false;
			if(!((Populated)holder.getLayer()).isCitizen(uuid)){
				if(sender != null) Print.chat(sender, "&cYou need to be a citizen to vote on this!");
				return false;
			}
		}
		return true;
	}

	public boolean expired(ICommandSender sender){
		if(Time.getDate() >= expiry || ended){
			if(sender != null) Print.chat(sender, "Vote expired already!");
			this.end();
			return true;
		}
		return false;
	}
	
	private boolean shouldEnd(ICommandSender sender){
		if(expired(sender)) return true;
		if(votes.size() >= (council ? holder.getCouncil().size() : ((Populated)holder.getLayer()).getAllResidentCount())){
			this.end();
			return true;
		}
		return false;
	}

	@SuppressWarnings("incomplete-switch")
	private void end(){
		if(this.ended) return; ended = true; this.save();
		if(type.abandonment()){
			boolean isstate = holder.getLayer() instanceof State;
			if(votes.size() < (holder.getCouncil().size() / 2) + (holder.getCouncil().size() % 2 == 1 ? 1 : 0)){
				String string = "&7Vote for new abandonment ended, due to missing votes it got &ccancelled&7.";
				StateUtil.announce(Static.getServer(), isstate ? AnnounceLevel.STATE_ALL : AnnounceLevel.MUNICIPALITY_ALL, string, holder.getLayer().getId());
			}
			int a = 0, d = 0;
			for(Object obj : votes.values())
				if((boolean)obj) a++;
				else d++;
			String text0;
			boolean fail = false;
			if(a == 0 && d == 0){
				text0 = "&7Vote [&9" + id + "&7] for abandonment ended without votes."; fail = true;
			}
			else if(a < d){
				text0 = "&7Vote [&9" + id + "&7] ended. &cAbandonment cancelled."; fail = true;
			}
			else if(a == d){
				text0 = "&7Vote [&9" + id + "&7] for abandonment ended, &esame amount of votes&7."; fail = true;
			}
			else{
				text0 = "&7Vote [&9" + id + "&7] ended. &aAbandonment will be executed.";
			}
			AnnounceLevel level = isstate ? AnnounceLevel.STATE_ALL : AnnounceLevel.MUNICIPALITY_ALL;
			int range = holder.getLayer().getId();
			StateUtil.announce(Static.getServer(), level, text0, range);
			if(!fail){
				PlayerCapability cap = StateUtil.getPlayer(beginner, true);
				if(isstate){
					//TODO ((State)holder)
				}
				else{
					Municipality mun = (Municipality)holder.getLayer();
					mun.abandon.abandon(this.beginner);
					StateUtil.announce(null, "&9A Municipality was voted to be abandoned!");
					StateUtil.announce(null, "&9Name&0: &7" + mun.getName() + " &3(&6" + mun.getId() + "&3)");
					StateUtil.announce(null, "&9By " + cap.getFormattedNickname());
				}
			}
		}
		if(type.assignment()){
			if(votes.size() < (holder.getCouncil().size() / 2) + (holder.getCouncil().size() % 2 == 1 ? 1 : 0)){
				String string = "&7Vote for new Head ended, due to missing votes it got &ccancelled&7.";
				StateUtil.announce(Static.getServer(), holder.getLayer() instanceof State ? AnnounceLevel.STATE_ALL : AnnounceLevel.MUNICIPALITY_ALL, string, holder.getLayer().getId());
			}
			TreeMap<String, Integer> vots = new TreeMap<>();
			for(Map.Entry<String, Object> vote : votes.entrySet()){
				String uuid = ((UUID)vote.getValue()).toString();
				if(vots.containsKey(uuid)){
					vots.put(uuid, vots.get(uuid) + 1);
				} else vots.put(uuid, 1);
			}
			int summary = 0;
			for(int i : vots.values()) summary += i;
			int mostv = -1;
			UUID most = null;
			for(Map.Entry<String, Integer> entry : vots.entrySet()){
				if(entry.getValue() > mostv){
					mostv = entry.getValue(); most = UUID.fromString(entry.getKey());
				}
			}
			StateUtil.announce(Static.getServer(), holder.getLayer() instanceof State ? AnnounceLevel.STATE : AnnounceLevel.MUNICIPALITY,
				"&7Vote for new Head ended, &a" + Static.getPlayerNameByUUID(most) + " &7 was choosen. [" + percent(mostv, summary) + "%]",holder.getLayer().getId());
			for(UUID member : council ? holder.getCouncil() : ((Populated)holder.getLayer()).getAllResidents()){
				MailUtil.send(null, RecipientType.PLAYER, member, null, "&7Head-Vote with ID &b" + id + "&7 ended!\n&7Detailed info via &e/st-vote status " + id, MailType.SYSTEM);
			}
			return;
		}
		AnnounceLevel level = holder.getLayer() instanceof District ? AnnounceLevel.DISTRICT : holder.getLayer() instanceof State ? AnnounceLevel.STATE_ALL : AnnounceLevel.MUNICIPALITY_ALL;
		int range = holder.getLayer().getId();
		int a = 0, d = 0;
		for(Object obj : votes.values()){
			if((boolean)obj) a++;
			else d++;
		}
		String text0, text1, text2;
		boolean fail = false;
		if(a == 0 && d == 0){
			text0 = "&7Vote [&9" + id + "&7] for rule change ended without votes.";
			fail = true;
		}
		else if(a < d){
			text0 = "&7Vote [&9" + id + "&7] for rule change ended. &cChange Denied.";
			fail = true;
		}
		else if(a == d){
			text0 = "&7Vote [&9" + id + "&7] for rule change ended, &esame amount of votes&7.";
			fail = true;
		}
		else{
			text0 = "&7Vote [&9" + id + "&7] for rule change ended. &aChange Accepted.";
		}
		text1 = "&7Vote was to change &b" + (type.valueful() ? "value" : type == VoteType.CHANGE_REVISER ? "revider" : "setter") + " &7to &b" + (type.valueful() ? new_value : to.name()) + "&7.";
		text2 = "&7Rule: &a" + rule; Rule RULE = holder.getRuleHolder().get(rule);
		if(RULE == null){
			StateUtil.announce(Static.getServer(), level, "&7Vote [&9" + id + "&7] for rule change ended. &4RULE NOT FOUND", range); return;
		}
		if(!fail){
			switch(type){
				case CHANGE_REVISER:{
					RULE.reviser = to;
					return;
				}
				case CHANGE_SETTER:{
					RULE.setter = to;
					return;
				}
				case CHANGE_VALUE:{
					RULE.set(new_value);
					return;
				}
			}
		}
		for(UUID member : council ? holder.getCouncil() : ((Municipality)holder.getLayer()).getAllResidents()){
			MailUtil.send(null, RecipientType.PLAYER, member, null, "&7Rule-Vote with ID &b" + id + "&7 ended!\n&7Detailed info via &e/st-vote status " + id, MailType.SYSTEM);
		}
		StateUtil.announce(Static.getServer(), level, text0, range);
		StateUtil.announce(Static.getServer(), level, text1, range);
		StateUtil.announce(Static.getServer(), level, text2, range);
		return;
	}
	
	public static enum VoteType {
		ASSIGNMENT, CHANGE_REVISER, CHANGE_SETTER, CHANGE_VALUE, ABANDONMENT;
		public boolean assignment(){ return this == ASSIGNMENT; }
		public boolean valueful(){ return this == CHANGE_VALUE; }
		public boolean abandonment(){ return this == ABANDONMENT; }
	}

	public String targetAsString(){
		if(holder.getLayer() instanceof District){
			return "dis:" + ((District)holder.getLayer()).getId();
		}
		else if(holder.getLayer() instanceof Municipality){
			return "mun:" + ((Municipality)holder.getLayer()).getId();
		}
		else if(holder.getLayer() instanceof State){
			return "st:" + ((State)holder.getLayer()).getId();
		}
		else{
			new Exception("Invalid or Unsupported RULEABLE for Voting.");
		}
		return "invalid target";
	}

	public void unload(){
		holder.getActiveVotes().remove(this);
	}

	public static boolean exists(Manageable holder, VoteType type, String rule){
		for(Vote vote : holder.getActiveVotes()){
			if(vote.type == type){
				if(type.assignment() || type.abandonment()) return true;
				else if(vote.rule.equals(rule)) return true;
			}
		} return false;
	}
	
}
