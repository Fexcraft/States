package net.fexcraft.mod.states.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.fexcraft.lib.common.json.JsonUtil;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.api.Bank;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.data.root.AccountHolder;
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
import net.fexcraft.mod.states.data.sub.RuleHolder;
import net.fexcraft.mod.states.events.StateEvent;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraftforge.common.MinecraftForge;

public class State implements ChildLayer, AccountHolder, Ruleable {

	private int id, capital;
	private String name;
	public IconHolder icon = new IconHolder();
	public ColorData color = new ColorData();
	public Buyable price = new Buyable(this, Layer.UNION);
	public MailData mailbox = new MailData();
	public Createable created = new Createable();
	public ExternalDataHolder external = new ExternalDataHolder();
	public RuleHolder rules = new RuleHolder(this);
	private UUID leader;
	private Account account;
	private ArrayList<Integer> neighbors, municipalities, blacklist;
	private ArrayList<UUID> council;
	private byte chunktaxpercent, citizentaxpercent;
	//
	private String ruleset;
	public final Rule r_CREATE_SIGN_SHOP, r_SET_MAILBOX, r_OPEN_MAILBOX, r_CREATE_MUNICIPALITY, r_CLAIM_MUNICIPALITY;
	public final Rule r_SET_COLOR, r_SET_ICON, r_SET_NAME, r_SET_PRICE, r_SET_LEADER, r_SET_CHUNK_TAX_PERCENT;
	public final Rule r_EDIT_BL, r_MUN_KICK, r_MUN_INVITE, r_COUNCIL_KICK, r_COUNCIL_INVITE, r_VOTE_LEADER;
	public final Rule r_SET_CAPITAL, r_SET_CITIZEN_TAX_PERCENT, r_SET_RULESET, r_RESET_HEAD;
	private ArrayList<Vote> active_votes = new ArrayList<>();

	public State(int value){
		id = value;
		JsonObject obj = StateUtil.getStateJson(value).getAsJsonObject();
		name = JsonUtil.getIfExists(obj, "name", "Unnamed State");
		created.load(obj);
		leader = obj.has("leader") ? UUID.fromString(obj.get("leader").getAsString()) : null;
		account = DataManager.getAccount("state:" + id, false, true).setName(name);
		capital = JsonUtil.getIfExists(obj, "capital", -1).intValue();
		neighbors = JsonUtil.jsonArrayToIntegerArray(JsonUtil.getIfExists(obj, "neighbors", new JsonArray()).getAsJsonArray());
		municipalities = JsonUtil.jsonArrayToIntegerArray(JsonUtil.getIfExists(obj, "municipalities", new JsonArray()).getAsJsonArray());
		council = JsonUtil.jsonArrayToUUIDArray(JsonUtil.getIfExists(obj, "council", new JsonArray()).getAsJsonArray());
		color.load(obj);
		blacklist = JsonUtil.jsonArrayToIntegerArray(JsonUtil.getIfExists(obj, "blacklist", new JsonArray()).getAsJsonArray());
		price.load(obj);
		icon.load(obj);
		chunktaxpercent = JsonUtil.getIfExists(obj, "chunk_tax_percent", 0).byteValue();
		citizentaxpercent = JsonUtil.getIfExists(obj, "citizen_tax_percent", 0).byteValue();
		mailbox.load(obj);
		ruleset = JsonUtil.getIfExists(obj, "ruleset", "Standard Ruleset");
		rules.add(r_CREATE_SIGN_SHOP = new Rule("create.sign-shops", null, false, Initiator.INCHARGE, Initiator.COUNCIL_ANY));
		rules.add(r_SET_MAILBOX = new Rule("set.mailbox", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_OPEN_MAILBOX = new Rule("open.mailbox", null, false, Initiator.COUNCIL_VOTE, Initiator.COUNCIL_ANY));
		rules.add(r_SET_NAME = new Rule("set.name", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_SET_PRICE = new Rule("set.price", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_SET_LEADER = new Rule("set.leader", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_SET_COLOR = new Rule("set.color", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_SET_ICON = new Rule("set.icon", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_SET_CHUNK_TAX_PERCENT = new Rule("set.chunk-tax-percent", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_SET_CITIZEN_TAX_PERCENT = new Rule("set.citizen-tax-percent", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_EDIT_BL = new Rule("edit.blacklist", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_MUN_KICK = new Rule("kick_municipality", null, false, Initiator.COUNCIL_VOTE, Initiator.COUNCIL_ANY));
		rules.add(r_MUN_INVITE = new Rule("invite_municipality", null, false, Initiator.COUNCIL_VOTE, Initiator.COUNCIL_ANY));
		rules.add(r_COUNCIL_KICK = new Rule("kick_council", null, false, Initiator.COUNCIL_VOTE, Initiator.COUNCIL_ANY));
		rules.add(r_COUNCIL_INVITE = new Rule("invite_council", null, false, Initiator.COUNCIL_VOTE, Initiator.COUNCIL_ANY));
		rules.add(r_VOTE_LEADER = new Rule("vote.leader", null, true, Initiator.INCHARGE, Initiator.COUNCIL_ANY));
		rules.add(r_CREATE_MUNICIPALITY = new Rule("create.municipality", null, false, Initiator.COUNCIL_VOTE, Initiator.CITIZEN_ANY));
		rules.add(r_SET_CAPITAL = new Rule("set.capital", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_SET_RULESET = new Rule("set.ruleset-name", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_RESET_HEAD = new Rule("set.leader.none", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_CLAIM_MUNICIPALITY = new Rule("claim.municipality", null, false, Initiator.COUNCIL_VOTE, Initiator.CITIZEN_ANY));
		rules.getMap().lock();
		rules.load(obj);
		//
		if(obj.has("votes")){
			ArrayList<Integer> list = JsonUtil.jsonArrayToIntegerArray(obj.get("votes").getAsJsonArray());
			for(int i : list){
				Vote vote = StateUtil.getVote(this, i); if(vote == null || vote.expired(null)) continue; active_votes.add(vote);
			}
		}
		MinecraftForge.EVENT_BUS.post(new StateEvent.Load(this));
		rules.loadEx(obj);
		external.load(obj);
	}

	public JsonObject toJsonObject(){
		JsonObject obj = new JsonObject();
		obj.addProperty("id", id);
		obj.addProperty("name", name);
		created.save(obj);
		if(!(leader == null)){ obj.addProperty("leader", leader.toString()); }
		obj.add("neighbors", JsonUtil.getArrayFromIntegerList(neighbors));
		obj.add("municipalities", JsonUtil.getArrayFromIntegerList(municipalities));
		obj.addProperty("capital", capital);
		obj.add("council", JsonUtil.getArrayFromUUIDList(council));
		obj.addProperty("balance", account.getBalance());
		color.save(obj);
		obj.add("blacklist", JsonUtil.getArrayFromIntegerList(blacklist));
		price.save(obj);
		icon.save(obj);
		if(chunktaxpercent > 0){
			obj.addProperty("chunk_tax_percent", chunktaxpercent);
		}
		if(citizentaxpercent > 0){
			obj.addProperty("citizen_tax_percent", citizentaxpercent);
		}
		mailbox.save(obj);
		obj.addProperty("ruleset", ruleset);
		rules.save(obj);
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
		File file = getStateFile();
		if(!file.getParentFile().exists()){ file.getParentFile().mkdirs(); }
		JsonUtil.write(file, obj);
	}
	
	public final File getStateFile(){
		return getStateFile(id);
	}

	public static File getStateFile(int value){
		return new File(States.getSaveDirectory(), "states/" + value + ".json");
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

	public boolean isUnionCapital(){
		return false;//TODO
	}

	public List<Integer> getMunicipalities(){
		return municipalities;
	}

	public List<Integer> getNeighbors(){
		return neighbors;
	}

	@Override
	public Account getAccount(){
		return account;
	}

	public List<UUID> getCouncil(){
		return council;
	}

	public int getCapitalId(){
		return capital;
	}

	public void setCapitalId(int id){
		capital = id;
	}

	public List<Integer> getBlacklist(){
		return blacklist;
	}

	public byte getChunkTaxPercentage(){
		return chunktaxpercent;
	}

	public void setChunkTaxPercentage(byte newtax){
		chunktaxpercent = newtax;
	}

	public byte getCitizenTaxPercentage(){
		return citizentaxpercent;
	}

	public void setCitizenTaxPercentage(byte newtax){
		citizentaxpercent = newtax;
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
	public String getRulesetTitle(){
		return ruleset;
	}

	@Override
	public UUID getHead(){
		return leader;
	}

	@Override
	public void setHead(UUID uuid){
		leader = uuid;
	}

	@Override
	public Ruleable getHigherInstance(){
		return null;
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
		return 0;
	}

	@Override
	public Layer getParentLayer(){
		return Layer.UNION;
	}

	@Override
	public RuleHolder getRuleHolder(){
		return rules;
	}
	
}
