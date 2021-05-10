package net.fexcraft.mod.states.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.fexcraft.lib.common.json.JsonUtil;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.data.root.ChildLayer;
import net.fexcraft.mod.states.data.root.Initiator;
import net.fexcraft.mod.states.data.root.Layer;
import net.fexcraft.mod.states.data.root.Ruleable;
import net.fexcraft.mod.states.data.sub.Buyable;
import net.fexcraft.mod.states.data.sub.ColorData;
import net.fexcraft.mod.states.data.sub.Createable;
import net.fexcraft.mod.states.data.sub.ExternalDataHolder;
import net.fexcraft.mod.states.data.sub.IconHolder;
import net.fexcraft.mod.states.data.sub.MailData;
import net.fexcraft.mod.states.events.DistrictEvent;
import net.fexcraft.mod.states.util.RuleMap;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraftforge.common.MinecraftForge;

public class District implements ChildLayer, Ruleable {
	
	private int id, chunks;
	private DistrictType type;
	private long chunktax;
	private UUID manager;
	private ArrayList<Integer> neighbors;
	private String name;
	public IconHolder icon = new IconHolder();
	public ColorData color = new ColorData();
	public Buyable price = new Buyable(this, Layer.DISTRICT);
	public MailData mailbox = new MailData();
	public Createable created = new Createable();
	public ExternalDataHolder external = new ExternalDataHolder();
	private Municipality municipality;
	//
	private RuleMap rules = new RuleMap();
	private String ruleset;
	public final Rule r_CFS, r_ONBANKRUPT, r_SET_MANAGER, r_SET_CHUNKTAX;
	public final Rule r_SET_TYPE, r_SET_NAME, r_SET_PRICE, r_SET_COLOR, r_SET_ICON;
	public final Rule r_ALLOW_EXPLOSIONS, r_SET_CHUNKRULES, r_SET_CUSTOM_CHUNKTAX;
	public final Rule r_CLAIM_CHUNK, r_SET_MAILBOX, r_OPEN_MAILBOX, r_SET_RULESET;
	private ArrayList<Vote> active_votes = new ArrayList<>();
	
	public District(int id){
		this.id = id; JsonObject obj = StateUtil.getDistrictJson(id);
		type = DistrictType.valueOf(JsonUtil.getIfExists(obj, "type", DistrictType.WILDERNESS.name()));
		created.load(obj);
		neighbors = JsonUtil.jsonArrayToIntegerArray(JsonUtil.getIfExists(obj, "neighbors", new JsonArray()).getAsJsonArray());
		name = JsonUtil.getIfExists(obj, "name", "Unnamed District");
		municipality = StateUtil.getMunicipality(JsonUtil.getIfExists(obj, "municipality", -1).intValue());
		manager = obj.has("manager") ? UUID.fromString(obj.get("manager").getAsString()) : null;
		color.load(obj);
		price.load(obj);
		icon.load(obj);
		chunks = JsonUtil.getIfExists(obj, "chunks", 0).intValue();
		chunktax = JsonUtil.getIfExists(obj, "chunktax", 0).longValue();
		mailbox.load(obj);
		ruleset = JsonUtil.getIfExists(obj, "ruleset", "Standard Ruleset");
		rules.add(r_CFS = new Rule("can_foreigners_settle", false, false, Initiator.COUNCIL_ANY, Initiator.INCHARGE));
		rules.add(r_ONBANKRUPT = new Rule("unclaim_chunks_if_bankrupt", false, false, Initiator.COUNCIL_ANY, Initiator.INCHARGE));
		rules.add(r_SET_TYPE = new Rule("set.type", null, false, Initiator.COUNCIL_ANY, Initiator.INCHARGE));
		rules.add(r_SET_NAME = new Rule("set.name", null, false, Initiator.COUNCIL_ANY, Initiator.INCHARGE));
		rules.add(r_SET_PRICE = new Rule("set.price", null, false, Initiator.COUNCIL_ANY, Initiator.HIGHERINCHARGE));
		rules.add(r_SET_MANAGER = new Rule("set.manager", null, false, Initiator.COUNCIL_ANY, Initiator.HIGHERINCHARGE));
		rules.add(r_SET_COLOR = new Rule("set.color", null, false, Initiator.COUNCIL_ANY, Initiator.INCHARGE));
		rules.add(r_SET_ICON = new Rule("set.icon", null, false, Initiator.COUNCIL_ANY, Initiator.INCHARGE));
		rules.add(r_SET_CHUNKTAX = new Rule("set.chunktax", null, false, Initiator.COUNCIL_ANY, Initiator.INCHARGE));
		rules.add(r_ALLOW_EXPLOSIONS = new Rule("allow.explosions", false, false, Initiator.COUNCIL_VOTE, Initiator.HIGHERINCHARGE));
		rules.add(r_SET_CHUNKRULES = new Rule("set.chunkrules", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_SET_CUSTOM_CHUNKTAX = new Rule("set.custom_chunktax", null, false, Initiator.COUNCIL_ANY, Initiator.INCHARGE));
		rules.add(r_CLAIM_CHUNK = new Rule("claim.chunk", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_SET_MAILBOX = new Rule("set.mailbox", null, false, Initiator.COUNCIL_VOTE, Initiator.HIGHERINCHARGE));
		rules.add(r_OPEN_MAILBOX = new Rule("open.mailbox", null, false, Initiator.COUNCIL_VOTE, Initiator.COUNCIL_ANY));
		rules.add(r_SET_RULESET = new Rule("set.ruleset-name", null, false, Initiator.COUNCIL_ANY, Initiator.INCHARGE));
		rules.lock();
		if(obj.has("rules")){
			JsonObject rls = obj.get("rules").getAsJsonObject();
			for(Map.Entry<String, JsonElement> entry : rls.entrySet()){
				Rule rule = rules.get(entry.getKey());
				if(rule != null) rule.load(entry.getValue().getAsString());
			}
		}
		//import old settings from old saves
		if(obj.has("can_foreigners_settle")) r_CFS.set(obj.get("can_foreigners_settle").getAsBoolean());
		if(obj.has("unclaim_chunks_if_bankrupt")) r_ONBANKRUPT.set(obj.get("unclaim_chunks_if_bankrupt").getAsBoolean());
		//
		if(obj.has("votes")){
			ArrayList<Integer> list = JsonUtil.jsonArrayToIntegerArray(obj.get("votes").getAsJsonArray());
			for(int i : list){
				Vote vote = StateUtil.getVote(this, i); if(vote == null || vote.expired(null)) continue; active_votes.add(vote);
			}
		}
		MinecraftForge.EVENT_BUS.post(new DistrictEvent.Load(this));
		if(obj.has("ex-rules") && rules.hasExternal()){
			JsonObject rls = obj.get("ex-rules").getAsJsonObject();
			for(Map.Entry<String, JsonElement> entry : rls.entrySet()){
				Rule rule = rules.get(entry.getKey());
				if(rule != null) rule.load(entry.getValue().getAsString());
			}
		}
		external.load(obj);
	}

	public JsonObject toJsonObject(){
		JsonObject obj = new JsonObject();
		obj.addProperty("id", id);
		obj.addProperty("type", type.name());
		created.save(obj);
		obj.addProperty("name", name);
		obj.addProperty("municipality", municipality == null ? -1 : municipality.getId());
		obj.add("neighbors", JsonUtil.getArrayFromIntegerList(neighbors));
		if(!(manager == null)){ obj.addProperty("manager", manager.toString()); }
		color.save(obj);
		//obj.addProperty("can_foreigners_settle", cfs);
		price.save(obj);
		icon.save(obj);
		obj.addProperty("chunks", chunks);
		if(chunktax > 0){ obj.addProperty("chunktax", chunktax); }
		//obj.addProperty("unclaim_chunks_if_bankrupt", onbankrupt);
		mailbox.save(obj);
		obj.addProperty("ruleset", ruleset);
		{
			JsonObject rells = new JsonObject();
			for(Rule rule : rules.values()) if(!rule.isExternal()) rells.addProperty(rule.id, rule.save());
			obj.add("rules", rells);
		}
		if(rules.hasExternal()){
			JsonObject erells = new JsonObject();
			for(Rule rule : rules.values()) if(rule.isExternal()) erells.addProperty(rule.id, rule.save());
			obj.add("ex-rules", erells);
		}
		if(!active_votes.isEmpty()){
			JsonArray array = new JsonArray();
			for(Vote vote : active_votes) array.add(vote.id);
			obj.add("votes", array);
		}
		external.save(obj);
		return obj;
	}

	public void save(){
		JsonObject obj = toJsonObject();
		obj.addProperty("last_save", Time.getDate());
		File file = getDistrictFile();
		if(!file.getParentFile().exists()){ file.getParentFile().mkdirs(); }
		JsonUtil.write(file, obj);
	}

	public final File getDistrictFile(){
		return getDistrictFile(this.getId());
	}

	public static File getDistrictFile(int value){
		return new File(States.getSaveDirectory(), "districts/" + value + ".json");
	}

	public int getId(){
		return id;
	}

	public boolean isVillage(){
		return type == DistrictType.VILLAGE;
	}

	public DistrictType getType(){
		return type;
	}
	public List<Integer> getNeighbors(){
		return neighbors;
	}

	public String getName(){
		return name;
	}

	public void setName(String new_name){
		name = new_name;
	}

	public Municipality getMunicipality(){
		return municipality;
	}

	public void setMunicipality(Municipality mun){
		municipality.getDistricts().removeIf(pre -> pre == this.getId());
		municipality = mun;
		municipality.getDistricts().add(this.getId());
	}

	public void setType(DistrictType new_type){
		type = new_type;
	}

	public int getClaimedChunks(){
		return chunks;
	}

	public void setClaimedChunks(int i){
		chunks = i; if(chunks < 0){ chunks = 0; }return;
	}

	public long getChunkTax(){
		return chunktax;
	}

	public void setChunkTax(long tax){
		chunktax = tax;
	}

	public State getState(){
		return municipality.getState();
	}

	@Override
	public Map<String, Rule> getRules(){
		return rules;
	}

	@Override
	public String getRulesetTitle(){
		return ruleset;
	}

	@Override
	public List<UUID> getCouncil(){
		return municipality.getCouncil();
	}

	@Override
	public UUID getHead(){
		return manager;
	}

	@Override
	public void setHead(UUID uuid){
		manager = uuid;
	}
	
	@Override
	public boolean isHead(UUID uuid){
		return (getHead() != null && getHead().equals(uuid)) || municipality.isHead(uuid);
	}

	@Override
	public Ruleable getHigherInstance(){
		return municipality;
	}

	@Override
	public void setRulesetTitle(String title){
		ruleset = title;
	}

	@Override
	public List<Vote> getActiveVotes(){
		return active_votes;
	}

	@Override
	public int getParentId(){
		return municipality.getId();
	}

	@Override
	public Layer getParentLayer(){
		return Layer.MUNICIPALITY;
	}

}
