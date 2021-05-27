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
import net.fexcraft.mod.states.events.CountyEvent;
import net.fexcraft.mod.states.util.StConfig;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraftforge.common.MinecraftForge;

public class County implements Layer, AccountHolder, Populated {
	
	private int id;
	private String name;
	private long citizentax;
	private Account account;
	private ArrayList<Integer> districts, municipalities;
	private ArrayList<UUID> citizen;
	private State state;
	//
	public IconHolder icon = new IconHolder();
	public ColorData color = new ColorData();
	public Buyable price = new Buyable(this);
	public MailData mailbox = new MailData();
	public Createable created = new Createable();
	public ExternalDataHolder external = new ExternalDataHolder();
	public Manageable manage = new Manageable(this, true, true, "manager");
	public RuleHolder rules = new RuleHolder();
	public NeighborData neighbors = new NeighborData();
	//
	public final Rule r_OPEN, r_COLOR, r_ICON, r_SET_NAME, r_SET_PRICE, r_SET_MANAGER, r_SET_CITIZENTAX, r_KIB;
	public final Rule r_KICK, r_INVITE, r_COUNCIL_KICK, r_COUNCIL_INVITE, r_VOTE_MANAGER;
	public final Rule r_CREATE_DISTRICT, r_SET_CHUNKRULES, r_CREATE_SIGN_SHOP, r_SET_MAILBOX, r_OPEN_MAILBOX;
	public final Rule r_SET_RULESET, r_RESET_MANAGER, r_SET_TITLE;
	
	public County(int id){
		this.id = id;
		JsonObject obj = StateUtil.getMunicipalityJson(id);
		name = JsonUtil.getIfExists(obj, "name", "Unnamed Place");
		created.load(obj);
		manage.load(obj);
		account = DataManager.getAccount("municipality:" + id, false, true).setName(name);
		neighbors.load(obj);
		districts = JsonUtil.jsonArrayToIntegerArray(JsonUtil.getIfExists(obj, "districts", new JsonArray()).getAsJsonArray());
		municipalities = JsonUtil.jsonArrayToIntegerArray(JsonUtil.getIfExists(obj, "municipalities", new JsonArray()).getAsJsonArray());
		citizen = JsonUtil.jsonArrayToUUIDArray(JsonUtil.getIfExists(obj, "citizen", new JsonArray()).getAsJsonArray());
		state = StateUtil.getState(JsonUtil.getIfExists(obj, "state", -1).intValue());
		color.load(obj);
		price.load(obj);
		icon.load(obj);
		citizentax = JsonUtil.getIfExists(obj, "citizen_tax", 0).longValue();
		mailbox.load(obj);
		manage.linkRuleHolder(rules);
		rules.add(r_SET_NAME = new Rule("set.name", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_SET_PRICE = new Rule("set.price", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_SET_MANAGER = new Rule("set.manager", null, true, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_SET_TITLE = new Rule("set.title", null, true, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_OPEN = new Rule("open_to_join", false, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_COLOR = new Rule("set.color", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_ICON = new Rule("set.icon", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_SET_CITIZENTAX = new Rule("set.citizentax", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_KIB = new Rule("kick_if_bankrupt", false, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_KICK = new Rule("kick_player", null, false, Initiator.COUNCIL_VOTE, Initiator.COUNCIL_ANY));
		rules.add(r_INVITE = new Rule("invite_player", null, false, Initiator.COUNCIL_VOTE, Initiator.COUNCIL_ANY));
		rules.add(r_COUNCIL_KICK = new Rule("kick_council", null, false, Initiator.COUNCIL_VOTE, Initiator.COUNCIL_ANY));
		rules.add(r_COUNCIL_INVITE = new Rule("invite_council", null, false, Initiator.COUNCIL_VOTE, Initiator.COUNCIL_ANY));
		rules.add(r_VOTE_MANAGER = new Rule("vote.mayor", null, true, Initiator.COUNCIL_VOTE, Initiator.COUNCIL_ANY));
		rules.add(r_CREATE_DISTRICT = new Rule("create.district", null, false, Initiator.CITIZEN_VOTE, Initiator.COUNCIL_ANY));
		rules.add(r_SET_CHUNKRULES = new Rule("set.chunkrules", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_CREATE_SIGN_SHOP = new Rule("create.sign-shops", null, false, Initiator.INCHARGE, Initiator.COUNCIL_ANY));
		rules.add(r_SET_MAILBOX = new Rule("set.mailbox", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_OPEN_MAILBOX = new Rule("open.mailbox", null, false, Initiator.COUNCIL_VOTE, Initiator.COUNCIL_ANY));
		rules.add(r_SET_RULESET = new Rule("set.ruleset-name", null, false, Initiator.COUNCIL_VOTE, Initiator.INCHARGE));
		rules.add(r_RESET_MANAGER = new Rule("set.manager.none", null, false, Initiator.CITIZEN_VOTE, Initiator.HIGHERINCHARGE));
		rules.getMap().lock();
		rules.load(obj);
		//import old settings from old saves
		if(obj.has("open")){ r_OPEN.set(obj.get("open").getAsBoolean()); }
		if(obj.has("kick_if_bankrupt")){ r_KIB.set(obj.get("kick_if_bankrupt").getAsBoolean()); }
		//
		MinecraftForge.EVENT_BUS.post(new CountyEvent.Load(this));
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
		obj.add("districts", JsonUtil.getArrayFromIntegerList(districts));
		obj.add("municipalities", JsonUtil.getArrayFromIntegerList(municipalities));
		obj.add("citizen", JsonUtil.getArrayFromUUIDList(citizen));
		obj.addProperty("state", state.getId());
		obj.addProperty("balance", account.getBalance());
		color.save(obj);
		//obj.addProperty("open", open);
		price.save(obj);
		icon.save(obj);
		//obj.addProperty("kick_if_bankrupt", kib);
		obj.addProperty("citizen_tax", citizentax);
		mailbox.save(obj);
		external.save(obj);
		return obj;
	}

	public void save(){
		JsonObject obj = toJsonObject();
		obj.addProperty("last_save", Time.getDate());
		File file = getCountyFile();
		if(!file.getParentFile().exists()){ file.getParentFile().mkdirs(); }
		JsonUtil.write(file, obj);
	}
	
	public final File getCountyFile(){
		return getCountyFile(this.getId());
	}

	public static File getCountyFile(int value){
		return new File(States.getSaveDirectory(), "counties/" + value + ".json");
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

	public List<Integer> getDistricts(){
		return districts;
	}

	public List<Integer> getMunicipalities(){
		return municipalities;
	}

	@Override
	public Account getAccount(){
		return account;
	}

	public State getState(){
		return state;
	}

	public void setState(State new_state){
		state.getCounties().removeIf(pre -> pre == this.getId());
		state = new_state;
		state.getCounties().add(this.getId());
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

	public int getDistrictLimit(){
		return getClaimedChunks() / StConfig.CHUNKS_FOR_DISTRICT + 1;
	}

	public int getChunkLimit(){
		return citizen.size() * StConfig.CHUNK_PER_CITIZEN;
	}

	@Override
	public Layer getParent(){
		return state;
	}

	@Override
	public Layers getLayerType(){
		return Layers.COUNTY;
	}

	@Override
	public List<UUID> getResidents(){
		return citizen;
	}

	@Override
	public List<UUID> getAllResidents(){
		ArrayList<UUID> list = new ArrayList<>();
		list.addAll(citizen);
		for(int id : municipalities){
			Municipality mun = StateUtil.getMunicipality(id);
			if(mun.getId() == -1) continue;
			list.addAll(mun.getResidents());
		}
		return list;
	}

	@Override
	public int getAllResidentCount(){
		int count = citizen.size();
		for(int id : municipalities){
			Municipality mun = StateUtil.getMunicipality(id);
			if(mun.getId() == -1) continue;
			count += mun.getResidentCount();
		}
		return count;
	}

	@Override
	public boolean isCitizen(UUID uuid){
		if(citizen.contains(uuid)) return true;
		for(int id : municipalities){
			Municipality mun = StateUtil.getMunicipality(id);
			if(mun.getId() == -1) continue;
			if(mun.isCitizen(uuid)) return true;
		}
		return false;
	}

}
