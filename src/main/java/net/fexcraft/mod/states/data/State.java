package net.fexcraft.mod.states.data;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.fexcraft.lib.common.json.JsonUtil;
import net.fexcraft.lib.common.lang.ArrayList;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.api.Bank;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.data.root.AccountHolder;
import net.fexcraft.mod.states.data.root.BuyableType;
import net.fexcraft.mod.states.data.root.ColorHolder;
import net.fexcraft.mod.states.data.root.IconHolder;
import net.fexcraft.mod.states.data.root.Initiator;
import net.fexcraft.mod.states.data.root.MailReceiver;
import net.fexcraft.mod.states.data.root.Ruleable;
import net.fexcraft.mod.states.util.RuleMap;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraft.util.math.BlockPos;

public class State implements ColorHolder, BuyableType, IconHolder, AccountHolder, MailReceiver, Ruleable {

	private int id, capital;
	private String name, color, icon;
	private long created, changed, price;
	private UUID creator, leader;
	private Account account;
	private ArrayList<Integer> neighbors, municipalities, blacklist;
	private ArrayList<UUID> council;
	private byte chunktaxpercent, citizentaxpercent;
	private BlockPos mailbox;
	//
	private String ruleset;
	private RuleMap rules = new RuleMap();
	public final Rule r_CREATE_SIGN_SHOP, r_SET_MAILBOX, r_OPEN_MAILBOX, r_CREATE_MUNICIPALITY;
	public final Rule r_SET_COLOR, r_SET_ICON, r_SET_NAME, r_SET_PRICE, r_SET_LEADER, r_SET_CHUNK_TAX_PERCENT;
	public final Rule r_EDIT_BL, r_MUN_KICK, r_MUN_INVITE, r_COUNCIL_KICK, r_COUNCIL_INVITE, r_COUNCIL_VOTE;
	public final Rule r_SET_CAPITAL, r_SET_CITIZEN_TAX_PERCENT, r_SET_RULESET;

	public State(int value){
		id = value;
		JsonObject obj = StateUtil.getStateJson(value).getAsJsonObject();
		name = JsonUtil.getIfExists(obj, "name", "Unnamed State");
		created = JsonUtil.getIfExists(obj, "created", Time.getDate()).longValue();
		changed = JsonUtil.getIfExists(obj, "changed", Time.getDate()).longValue();
		creator = obj.has("creator") ? UUID.fromString(obj.get("creator").getAsString()) : UUID.fromString(States.CONSOLE_UUID);
		leader = obj.has("leader") ? UUID.fromString(obj.get("leader").getAsString()) : null;
		account = DataManager.getAccount("state:" + id, false, true);
		capital = JsonUtil.getIfExists(obj, "capital", -1).intValue();
		neighbors = JsonUtil.jsonArrayToIntegerArray(JsonUtil.getIfExists(obj, "neighbors", new JsonArray()).getAsJsonArray());
		municipalities = JsonUtil.jsonArrayToIntegerArray(JsonUtil.getIfExists(obj, "municipalities", new JsonArray()).getAsJsonArray());
		council = JsonUtil.jsonArrayToUUIDArray(JsonUtil.getIfExists(obj, "council", new JsonArray()).getAsJsonArray());
		color = JsonUtil.getIfExists(obj, "color", "#ffffff");
		blacklist = JsonUtil.jsonArrayToIntegerArray(JsonUtil.getIfExists(obj, "blacklist", new JsonArray()).getAsJsonArray());
		price = JsonUtil.getIfExists(obj, "price", 0).longValue();
		icon = JsonUtil.getIfExists(obj, "icon", States.DEFAULT_ICON);
		chunktaxpercent = JsonUtil.getIfExists(obj, "chunk_tax_percent", 0).byteValue();
		citizentaxpercent = JsonUtil.getIfExists(obj, "citizen_tax_percent", 0).byteValue();
		mailbox = obj.has("mailbox") ? BlockPos.fromLong(obj.get("mailbox").getAsLong()) : null;
		ruleset = JsonUtil.getIfExists(obj, "ruleset", "Standard Ruleset");
		rules.add(r_CREATE_SIGN_SHOP = new Rule("create.sign-shops", null, true, Initiator.INCHARGE, Initiator.COUNCIL_ANY));
		rules.add(r_SET_MAILBOX = new Rule("set.mailbox", null, true, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_OPEN_MAILBOX = new Rule("open.mailbox", null, true, Initiator.COUNCIL_VOTE, Initiator.COUNCIL_ANY));
		rules.add(r_SET_NAME = new Rule("set.name", null, true, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_SET_PRICE = new Rule("set.price", null, true, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_SET_LEADER = new Rule("set.leader", null, true, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_SET_COLOR = new Rule("set.color", null, true, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_SET_ICON = new Rule("set.icon", null, true, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_SET_CHUNK_TAX_PERCENT = new Rule("set.chunk-tax-percent", null, true, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_SET_CITIZEN_TAX_PERCENT = new Rule("set.citizen-tax-percent", null, true, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_EDIT_BL = new Rule("edit.blacklist", null, true, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_MUN_KICK = new Rule("kick_municipality", null, true, Initiator.COUNCIL_VOTE, Initiator.COUNCIL_ANY));
		rules.add(r_MUN_INVITE = new Rule("invite_municipality", null, true, Initiator.COUNCIL_VOTE, Initiator.COUNCIL_ANY));
		rules.add(r_COUNCIL_KICK = new Rule("kick_council", null, true, Initiator.COUNCIL_VOTE, Initiator.COUNCIL_ANY));
		rules.add(r_COUNCIL_INVITE = new Rule("invite_council", null, true, Initiator.COUNCIL_VOTE, Initiator.COUNCIL_ANY));
		rules.add(r_COUNCIL_VOTE = new Rule("council_vote", null, true, Initiator.INCHARGE, Initiator.COUNCIL_ANY));
		rules.add(r_CREATE_MUNICIPALITY = new Rule("create.municipality", null, true, Initiator.COUNCIL_VOTE, Initiator.CITIZEN_ANY));
		rules.add(r_SET_CAPITAL = new Rule("set.capital", null, true, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_SET_RULESET = new Rule("set.ruleset-name", null, true, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		if(obj.has("rules")){
			JsonObject rls = obj.get("rules").getAsJsonObject();
			for(Map.Entry<String, JsonElement> entry : rls.entrySet()){
				Rule rule = rules.get(entry.getKey());
				if(rule != null) rule.load(entry.getValue().getAsString());
			}
		}
	}

	public JsonObject toJsonObject(){
		JsonObject obj = new JsonObject();
		obj.addProperty("id", id);
		obj.addProperty("name", name);
		obj.addProperty("created", created);
		obj.addProperty("creator", creator.toString());
		obj.addProperty("changed", changed);
		if(!(leader == null)){ obj.addProperty("leader", leader.toString()); }
		obj.add("neighbors", JsonUtil.getArrayFromIntegerList(neighbors));
		obj.add("municipalities", JsonUtil.getArrayFromIntegerList(municipalities));
		obj.addProperty("capital", capital);
		obj.add("council", JsonUtil.getArrayFromUUIDList(council));
		obj.addProperty("balance", account.getBalance());
		obj.addProperty("color", color);
		obj.add("blacklist", JsonUtil.getArrayFromIntegerList(blacklist));
		obj.addProperty("price", price);
		if(icon != null){ obj.addProperty("icon", icon); }
		if(chunktaxpercent > 0){
			obj.addProperty("chunk_tax_percent", chunktaxpercent);
		}
		if(citizentaxpercent > 0){
			obj.addProperty("citizen_tax_percent", citizentaxpercent);
		}
		if(mailbox != null) obj.addProperty("mailbox", mailbox.toLong());
		obj.addProperty("ruleset", ruleset);
		JsonObject rells = new JsonObject();
		for(Rule rule : rules.values()) rells.addProperty(rule.id, rule.save());
		obj.add("rules", rells);
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
	}

	public boolean isUnionCapital(){
		return false;//TODO
	}

	public void setChanged(long new_change){
		changed = new_change;
	}

	public List<Integer> getMunicipalities(){
		return municipalities;
	}

	public List<Integer> getNeighbors(){
		return neighbors;
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

	@Override
	public String getColor(){
		return color;
	}

	@Override
	public void setColor(String newcolor){
		color = newcolor;
	}

	public List<Integer> getBlacklist(){
		return blacklist;
	}

	@Override
	public long getPrice(){
		return price;
	}

	@Override
	public void setPrice(long new_price){
		price = new_price;
	}

	@Override
	public String getIcon(){
		return icon;
	}

	@Override
	public void setIcon(String url){
		icon = url;
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
	
}
