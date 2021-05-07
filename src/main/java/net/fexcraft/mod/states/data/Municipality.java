package net.fexcraft.mod.states.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.fexcraft.lib.common.json.JsonUtil;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.api.Bank;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.data.capabilities.PlayerCapability;
import net.fexcraft.mod.states.data.root.*;
import net.fexcraft.mod.states.data.sub.Buyable;
import net.fexcraft.mod.states.data.sub.ColorData;
import net.fexcraft.mod.states.data.sub.IconHolder;
import net.fexcraft.mod.states.events.MunicipalityEvent;
import net.fexcraft.mod.states.util.ForcedChunksManager;
import net.fexcraft.mod.states.util.RuleMap;
import net.fexcraft.mod.states.util.StConfig;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;

public class Municipality implements ChildLayer, AccountHolder, MailReceiver, Ruleable, VoteHolder, Abandonable, ExternalDataHolder {
	
	private int id;
	private String name, title;
	public IconHolder icon = new IconHolder();
	public ColorData color = new ColorData();
	public Buyable price = new Buyable(this, Layer.MUNICIPALITY);
	private long created, changed, citizentax, abandonedat;
	private UUID creator, mayor, abandonedby;
	private boolean abandoned;
	private Account account;
	private ArrayList<Integer> neighbors, districts, com_blacklist;
	private ArrayList<UUID> citizen, council, pl_blacklist;
	private State state;
	private BlockPos mailbox;
	private TreeMap<String, ExternalData> datas = new TreeMap<>();
	//
	private RuleMap rules = new RuleMap();
	private String ruleset_name;
	public final Rule r_OPEN, r_COLOR, r_ICON, r_SET_NAME, r_SET_PRICE, r_SET_MAYOR, r_SET_CITIZENTAX, r_KIB;
	public final Rule r_EDIT_BL, r_KICK, r_INVITE, r_COUNCIL_KICK, r_COUNCIL_INVITE, r_VOTE_MAYOR;
	public final Rule r_CREATE_DISTRICT, r_SET_CHUNKRULES, r_CREATE_SIGN_SHOP, r_SET_MAILBOX, r_OPEN_MAILBOX;
	public final Rule r_FORCE_LOAD_CHUNKS, r_SET_RULESET, r_RESET_MAYOR, r_ABANDON, r_SET_TITLE;
	private ArrayList<Vote> active_votes = new ArrayList<>();
	
	public Municipality(int id){
		this.id = id;
		JsonObject obj = StateUtil.getMunicipalityJson(id);
		name = JsonUtil.getIfExists(obj, "name", "Unnamed Place");
		title = JsonUtil.getIfExists(obj, "title", "Untitled");
		created = JsonUtil.getIfExists(obj, "created", Time.getDate()).longValue();
		creator = UUID.fromString(obj.has("creator") ? obj.get("creator").getAsString() : States.CONSOLE_UUID);
		changed = JsonUtil.getIfExists(obj, "changed", Time.getDate()).longValue();
		account = DataManager.getAccount("municipality:" + id, false, true).setName(name);
		mayor = obj.has("mayor") ? UUID.fromString(obj.get("mayor").getAsString()) : null;
		neighbors = JsonUtil.jsonArrayToIntegerArray(JsonUtil.getIfExists(obj, "neighbors", new JsonArray()).getAsJsonArray());
		districts = JsonUtil.jsonArrayToIntegerArray(JsonUtil.getIfExists(obj, "districts", new JsonArray()).getAsJsonArray());
		citizen = JsonUtil.jsonArrayToUUIDArray(JsonUtil.getIfExists(obj, "citizen", new JsonArray()).getAsJsonArray());
		council = JsonUtil.jsonArrayToUUIDArray(JsonUtil.getIfExists(obj, "council", new JsonArray()).getAsJsonArray());
		state = StateUtil.getState(JsonUtil.getIfExists(obj, "state", -1).intValue());
		color.load(obj);
		com_blacklist = JsonUtil.jsonArrayToIntegerArray(JsonUtil.getIfExists(obj, "company_blacklist", new JsonArray()).getAsJsonArray());
		pl_blacklist = JsonUtil.jsonArrayToUUIDArray(JsonUtil.getIfExists(obj, "player_blacklist", new JsonArray()).getAsJsonArray());
		price.load(obj);
		icon.load(obj);
		citizentax = JsonUtil.getIfExists(obj, "citizen_tax", 0).longValue();
		mailbox = obj.has("mailbox") ? BlockPos.fromLong(obj.get("mailbox").getAsLong()) : null;
		abandoned = JsonUtil.getIfExists(obj, "abandoned", false);
		abandonedby = obj.has("abandoned_by") ? UUID.fromString(obj.get("abandoned_by").getAsString()) : null;
		abandonedat = JsonUtil.getIfExists(obj, "abandoned_at", 0).longValue();
		ruleset_name = JsonUtil.getIfExists(obj, "ruleset", "Standard Ruleset");
		rules.add(r_SET_NAME = new Rule("set.name", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_SET_PRICE = new Rule("set.price", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_SET_MAYOR = new Rule("set.mayor", null, true, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_SET_TITLE = new Rule("set.title", null, true, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_OPEN = new Rule("open_to_join", false, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_COLOR = new Rule("set.color", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_ICON = new Rule("set.icon", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_SET_CITIZENTAX = new Rule("set.citizentax", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_KIB = new Rule("kick_if_bankrupt", false, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_EDIT_BL = new Rule("edit.blacklist", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_KICK = new Rule("kick_player", null, false, Initiator.COUNCIL_VOTE, Initiator.COUNCIL_ANY));
		rules.add(r_INVITE = new Rule("invite_player", null, false, Initiator.COUNCIL_VOTE, Initiator.COUNCIL_ANY));
		rules.add(r_COUNCIL_KICK = new Rule("kick_council", null, false, Initiator.COUNCIL_VOTE, Initiator.COUNCIL_ANY));
		rules.add(r_COUNCIL_INVITE = new Rule("invite_council", null, false, Initiator.COUNCIL_VOTE, Initiator.COUNCIL_ANY));
		rules.add(r_VOTE_MAYOR = new Rule("vote.mayor", null, true, Initiator.COUNCIL_VOTE, Initiator.COUNCIL_ANY));
		rules.add(r_CREATE_DISTRICT = new Rule("create.district", null, false, Initiator.CITIZEN_VOTE, Initiator.COUNCIL_ANY));
		rules.add(r_SET_CHUNKRULES = new Rule("set.chunkrules", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_CREATE_SIGN_SHOP = new Rule("create.sign-shops", null, false, Initiator.INCHARGE, Initiator.COUNCIL_ANY));
		rules.add(r_SET_MAILBOX = new Rule("set.mailbox", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_OPEN_MAILBOX = new Rule("open.mailbox", null, false, Initiator.COUNCIL_VOTE, Initiator.COUNCIL_ANY));
		rules.add(r_FORCE_LOAD_CHUNKS = new Rule("force-load.chunks", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_SET_RULESET = new Rule("set.ruleset-name", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_RESET_MAYOR = new Rule("set.mayor.none", null, false, Initiator.CITIZEN_VOTE, Initiator.HIGHERINCHARGE));
		rules.add(r_ABANDON = new Rule("abandon", null, true, Initiator.INCHARGE, Initiator.INCHARGE));
		rules.lock();
		if(obj.has("rules")){
			JsonObject rls = obj.get("rules").getAsJsonObject();
			for(Map.Entry<String, JsonElement> entry : rls.entrySet()){
				Rule rule = rules.get(entry.getKey());
				if(rule != null) rule.load(entry.getValue().getAsString());
			}
		}
		//import old settings from old saves
		if(obj.has("open")){ r_OPEN.set(obj.get("open").getAsBoolean()); }
		if(obj.has("kick_if_bankrupt")){ r_KIB.set(obj.get("kick_if_bankrupt").getAsBoolean()); }
		//
		if(obj.has("votes")){
			ArrayList<Integer> list = JsonUtil.jsonArrayToIntegerArray(obj.get("votes").getAsJsonArray());
			for(int i : list){
				Vote vote = StateUtil.getVote(this, i); if(vote == null || vote.expired(null)) continue; active_votes.add(vote);
			}
		}
		MinecraftForge.EVENT_BUS.post(new MunicipalityEvent.Load(this));
		if(obj.has("ex-rules") && rules.hasExternal()){
			JsonObject rls = obj.get("ex-rules").getAsJsonObject();
			for(Map.Entry<String, JsonElement> entry : rls.entrySet()){
				Rule rule = rules.get(entry.getKey());
				if(rule != null) rule.load(entry.getValue().getAsString());
			}
		}
		if(obj.has("ex-data") && !datas.isEmpty()){
			JsonObject external = obj.get("ex-data").getAsJsonObject();
			for(Entry<String, JsonElement> elm : external.entrySet()){
				ExternalData data = getExternalData(elm.getKey());
				if(data != null) data.load(elm.getValue());
			}
		}
	}

	public JsonObject toJsonObject(){
		JsonObject obj = new JsonObject();
		obj.addProperty("id", id);
		obj.addProperty("name", name);
		obj.addProperty("title", title);
		obj.addProperty("created", created);
		obj.addProperty("creator", creator.toString());
		obj.addProperty("changed", changed);
		if(mayor != null) obj.addProperty("mayor", mayor.toString());
		obj.add("neighbors", JsonUtil.getArrayFromIntegerList(neighbors));
		obj.add("districts", JsonUtil.getArrayFromIntegerList(districts));
		obj.add("citizen", JsonUtil.getArrayFromUUIDList(citizen));
		obj.add("council", JsonUtil.getArrayFromUUIDList(council));
		obj.addProperty("state", state.getId());
		obj.addProperty("balance", account.getBalance());
		color.save(obj);
		//obj.addProperty("open", open);
		price.save(obj);
		icon.save(obj);
		//obj.addProperty("kick_if_bankrupt", kib);
		obj.addProperty("citizen_tax", citizentax);
		if(mailbox != null) obj.addProperty("mailbox", mailbox.toLong());
		obj.addProperty("abandoned", abandoned);
		if(abandonedby != null) obj.addProperty("abandoned_by", abandonedby.toString());
		if(abandonedat != 0) obj.addProperty("abandoned_", abandonedat);
		obj.addProperty("ruleset", ruleset_name);
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
		if(!datas.isEmpty()){
			JsonObject external = new JsonObject();
			for(Entry<String, ExternalData> entry : datas.entrySet()){
				external.add(entry.getKey(), entry.getValue().save());
			}
			obj.add("ex-data", external);
		}
		return obj;
	}

	public void save(){
		JsonObject obj = toJsonObject();
		obj.addProperty("last_save", Time.getDate());
		File file = getMunicipalityFile();
		if(!file.getParentFile().exists()){ file.getParentFile().mkdirs(); }
		JsonUtil.write(file, obj);
	}
	
	public final File getMunicipalityFile(){
		return getMunicipalityFile(this.getId());
	}

	public static File getMunicipalityFile(int value){
		return new File(States.getSaveDirectory(), "municipalitites/" + value + ".json");
	}

	public int getId(){
		return id;
	}

	public String getName(){
		return name;
	}

	public void setName(String new_name){
		name = new_name;
		account.setName(new_name);
	}

	public boolean isCapital(){
		return this.getState().getCapitalId() == this.getId();
	}

	public void setChanged(long new_change){
		changed = new_change;
	}

	public List<Integer> getNeighbors(){
		return neighbors;
	}

	public List<Integer> getDistricts(){
		return districts;
	}

	public long getCreated(){
		return created;
	}

	public UUID getCreator(){
		return creator;
	}
	
	public void setCreator(UUID uuid){
		creator = uuid;
	}

	public long getChanged(){
		return changed;
	}

	public List<UUID> getCitizen(){
		return citizen;
	}

	@Override
	public Account getAccount(){
		return account;
	}

	@Override
	public UUID getHead(){
		return mayor;
	}

	@Override
	public void setHead(UUID uuid){
		mayor = uuid;
	}

	@Override
	public List<UUID> getCouncil(){
		return council;
	}

	public State getState(){
		return state;
	}

	public void setState(State new_state){
		state.getMunicipalities().removeIf(pre -> pre == this.getId());
		state = new_state;
		state.getMunicipalities().add(this.getId());
	}

	public List<UUID> getPlayerBlacklist(){
		return pl_blacklist;
	}

	public List<Integer> getCompanyBlacklist(){
		return com_blacklist;
	}

	public int getClaimedChunks(){
		int amount = 0;
		District district = null;
		for(int dis : districts){
			district = StateUtil.getDistrict(dis, false);
			if(district != null){
				amount += district.getClaimedChunks();
			}
		}
		return amount;
	}

	public Collection<ChunkPos> getForceLoadedChunks(){
		return States.LOADED_CHUNKS.containsKey(id) ? States.LOADED_CHUNKS.get(id) : null;
	}

	public boolean modifyForceloadedChunk(ICommandSender sender, ChunkPos pos, boolean add_rem){
		if(add_rem){
			if(this.getForceLoadedChunks() != null && this.getForceLoadedChunks().size() + 1 > StConfig.LOADED_CHUNKS_PER_MUNICIPALITY){
				Print.chat(sender, "&9Municipality reached the Server's Limit for Forced-Chunks-per-Municipality.");
				return false;
			}
			if(this.getForceLoadedChunks() == null){
				States.LOADED_CHUNKS.put(id, new ArrayList<>());
			}
			if(this.getForceLoadedChunks().contains(pos)){
				Print.chat(sender, "&cChunk already force-loaded.");
				return false;
			}
			this.getForceLoadedChunks().add(pos);
			ForcedChunksManager.check();
			Print.chat(sender, "&aChunk added.");
			return true;
		}
		else{
			if(this.getForceLoadedChunks() == null){
				Print.chat(sender, "&cMunicipality has no loaded chunk list.");
				return false;
			}
			if(!this.getForceLoadedChunks().contains(pos)){
				Print.chat(sender, "&cChunk is not force loaded.");
				return false;
			}
			this.getForceLoadedChunks().remove(pos);
			ForcedChunksManager.check();
			Print.chat(sender, "&aChunk removed.");
			return true;
		}
	}

	public long getCitizenTax(){
		return citizentax;
	}

	public void setCitizenTax(long newtax){
		citizentax = newtax;
	}

	@Override
	public void unload(){
		DataManager.unloadAccount(account);
	}
	
	@Override
	public void finalize(){ unload(); }

	@Override
	public Bank getBank(){
		return DataManager.getBank(account.getBankId(), true, true);
	}

	@Override
	public BlockPos getMailbox(){
		return mailbox;
	}

	@Override
	public void setMailbox(BlockPos pos){
		this.mailbox = pos;
	}

	@Override
	public Map<String, Rule> getRules(){
		return rules;
	}

	@Override
	public String getRulesetTitle(){
		return ruleset_name;
	}

	@Override
	public Ruleable getHigherInstance(){
		return state;
	}

	@Override
	public void setRulesetTitle(String title){
		ruleset_name = title;
	}

	@Override
	public List<Vote> getActiveVotes(){
		return active_votes;
	}

	@Override
	public boolean isAbandoned(){
		return abandoned;
	}

	@Override
	public void setAbandoned(UUID by){
		abandonedby = by;
		abandonedat = Time.getDate();
		abandoned = true;
		council.clear();
		ArrayList<UUID> list = (ArrayList<UUID>)citizen.clone();
		list.forEach(citizen -> {
			PlayerCapability cap = StateUtil.getPlayer(citizen, true);
			cap.setMunicipality(StateUtil.getMunicipality(-1));
			cap.save();
		});
		citizen.clear();
		mayor = null;
		save();
	}

	@Override
	public long getAbandonedSince(){
		return abandonedat;
	}

	@Override
	public UUID getAbandonedBy(){
		return abandonedby;
	}

	@Override
	public void getAbandoned(PlayerCapability by){
		abandonedby = null;
		abandonedat = Time.getDate();
		abandoned = false;
		council.clear();
		council.add(by.getUUID());
		citizen.clear();
		citizen.add(by.getUUID());
		setState(by.getState());
		by.setMunicipality(this);
		mayor = by.getUUID();
		save();
	}

	public int getDistrictLimit(){
		return getClaimedChunks() / StConfig.CHUNKS_FOR_DISTRICT + 1;
	}

	public int getChunkLimit(){
		return citizen.size() * StConfig.CHUNK_PER_CITIZEN;
	}

	public String getTitle(){
		return title;
	}

	public void setTitle(String newtitle){
		title = newtitle;
	}

	public String getTitledName(){
		return "(" + title + ") " + name;
	}

	@Override
	public <T extends ExternalData> T getExternalData(String id){
		return (T)datas.get(id);
	}

	@Override
	public ExternalData setExternalData(String id, ExternalData obj){
		return datas.put(id, obj);
	}

	@Override
	public Map<String, ExternalData> getExternalObjects(){
		return datas;
	}

	@Override
	public int getParentId(){
		return state.getId();
	}

	@Override
	public Layer getParentLayer(){
		return Layer.STATE;
	}

}
