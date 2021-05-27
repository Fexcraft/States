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
import net.fexcraft.mod.states.data.root.Initiator;
import net.fexcraft.mod.states.data.root.Layer;
import net.fexcraft.mod.states.data.root.Layers;
import net.fexcraft.mod.states.data.root.Populated;
import net.fexcraft.mod.states.data.sub.Buyable;
import net.fexcraft.mod.states.data.sub.ColorData;
import net.fexcraft.mod.states.data.sub.Createable;
import net.fexcraft.mod.states.data.sub.ExternalDataHolder;
import net.fexcraft.mod.states.data.sub.IconHolder;
import net.fexcraft.mod.states.data.sub.MailData;
import net.fexcraft.mod.states.data.sub.Manageable;
import net.fexcraft.mod.states.data.sub.NeighborData;
import net.fexcraft.mod.states.data.sub.RuleHolder;
import net.fexcraft.mod.states.events.StateEvent;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraftforge.common.MinecraftForge;

public class State implements Layer, AccountHolder, Populated {

	private int id, capital;
	private String name;
	private Account account;
	private ArrayList<Integer> municipalities, blacklist;
	private byte chunktaxpercent, citizentaxpercent;
	//
	public IconHolder icon = new IconHolder();
	public ColorData color = new ColorData();
	public Buyable price = new Buyable(this);
	public MailData mailbox = new MailData();
	public Createable created = new Createable();
	public ExternalDataHolder external = new ExternalDataHolder();
	public Manageable manage = new Manageable(this, true, false, "leader");
	public RuleHolder rules = new RuleHolder();
	public NeighborData neighbors = new NeighborData();
	//
	public final Rule r_CREATE_SIGN_SHOP, r_SET_MAILBOX, r_OPEN_MAILBOX, r_CREATE_MUNICIPALITY, r_CLAIM_MUNICIPALITY;
	public final Rule r_SET_COLOR, r_SET_ICON, r_SET_NAME, r_SET_PRICE, r_SET_LEADER, r_SET_CHUNK_TAX_PERCENT;
	public final Rule r_EDIT_BL, r_MUN_KICK, r_MUN_INVITE, r_COUNCIL_KICK, r_COUNCIL_INVITE, r_VOTE_LEADER;
	public final Rule r_SET_CAPITAL, r_SET_CITIZEN_TAX_PERCENT, r_SET_RULESET, r_RESET_HEAD;

	public State(int value){
		id = value;
		JsonObject obj = StateUtil.getStateJson(value).getAsJsonObject();
		name = JsonUtil.getIfExists(obj, "name", "Unnamed State");
		created.load(obj);
		manage.load(obj);
		account = DataManager.getAccount("state:" + id, false, true).setName(name);
		capital = JsonUtil.getIfExists(obj, "capital", -1).intValue();
		neighbors.load(obj);
		municipalities = JsonUtil.jsonArrayToIntegerArray(JsonUtil.getIfExists(obj, "municipalities", new JsonArray()).getAsJsonArray());
		color.load(obj);
		blacklist = JsonUtil.jsonArrayToIntegerArray(JsonUtil.getIfExists(obj, "blacklist", new JsonArray()).getAsJsonArray());
		price.load(obj);
		icon.load(obj);
		chunktaxpercent = JsonUtil.getIfExists(obj, "chunk_tax_percent", 0).byteValue();
		citizentaxpercent = JsonUtil.getIfExists(obj, "citizen_tax_percent", 0).byteValue();
		mailbox.load(obj);
		manage.linkRuleHolder(rules);
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
		MinecraftForge.EVENT_BUS.post(new StateEvent.Load(this));
		rules.loadEx(obj);
		external.load(obj);
	}

	public JsonObject toJsonObject(){
		JsonObject obj = new JsonObject();
		obj.addProperty("id", id);
		obj.addProperty("name", name);
		created.save(obj);
		manage.save(obj);
		neighbors.save(obj);
		obj.add("municipalities", JsonUtil.getArrayFromIntegerList(municipalities));
		obj.addProperty("capital", capital);
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
		rules.save(obj);
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

	@Override
	public Account getAccount(){
		return account;
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
	public Layer getParent(){
		return null;
	}

	@Override
	public Layers getLayerType(){
		return Layers.STATE;
	}

	@Override
	public List<UUID> getResidents(){
		return StateUtil.NO_RESIDENTS;
	}

	@Override
	public List<UUID> getAllResidents(){
		ArrayList<UUID> list = new ArrayList<UUID>();
		for(int id : getMunicipalities()){
			Municipality mun = StateUtil.getMunicipality(id);
			if(mun.getId() == -1) continue;
			list.addAll(mun.getResidents());
		}
		return list;
	}

	@Override
	public int getAllResidentCount(){
		int count = 0;
		for(int id : getMunicipalities()){
			Municipality mun = StateUtil.getMunicipality(id);
			if(mun.getId() == -1) continue;
			count += mun.getResidentCount();
		}
		return count;
	}

	@Override
	public boolean isCitizen(UUID uuid){
		for(int id : getMunicipalities()){
			Municipality mun = StateUtil.getMunicipality(id);
			if(mun.getId() == -1) continue;
			mun.isCitizen(uuid);
		}
		return false;
	}
	
}
