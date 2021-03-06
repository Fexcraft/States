package net.fexcraft.mod.states.data;

import java.io.File;

import com.google.gson.JsonObject;

import net.fexcraft.lib.common.json.JsonUtil;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.data.root.Initiator;
import net.fexcraft.mod.states.data.root.Layer;
import net.fexcraft.mod.states.data.root.Layers;
import net.fexcraft.mod.states.data.root.MunCt;
import net.fexcraft.mod.states.data.sub.Buyable;
import net.fexcraft.mod.states.data.sub.ColorData;
import net.fexcraft.mod.states.data.sub.Createable;
import net.fexcraft.mod.states.data.sub.ExternalDataHolder;
import net.fexcraft.mod.states.data.sub.IconHolder;
import net.fexcraft.mod.states.data.sub.MailData;
import net.fexcraft.mod.states.data.sub.Manageable;
import net.fexcraft.mod.states.data.sub.NeighborData;
import net.fexcraft.mod.states.data.sub.RuleHolder;
import net.fexcraft.mod.states.events.DistrictEvent;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraftforge.common.MinecraftForge;

public class District implements Layer {
	
	private int id, chunks;
	private DistrictType type;
	private long chunktax;
	private String name;
	private MunCt munct = new MunCt(this);
	//
	public IconHolder icon = new IconHolder();
	public ColorData color = new ColorData();
	public Buyable price = new Buyable(this);
	public MailData mailbox = new MailData();
	public Createable created = new Createable();
	public ExternalDataHolder external = new ExternalDataHolder();
	public Manageable manage = new Manageable(this, false, true, "manager");
	public RuleHolder rules = new RuleHolder();
	public NeighborData neighbors = new NeighborData();
	//
	public final Rule r_OCCB, r_ONBANKRUPT, r_SET_MANAGER, r_SET_CHUNKTAX;
	public final Rule r_SET_TYPE, r_SET_NAME, r_SET_PRICE, r_SET_COLOR, r_SET_ICON;
	public final Rule r_ALLOW_EXPLOSIONS, r_SET_CHUNKRULES, r_SET_CUSTOM_CHUNKTAX;
	public final Rule r_CLAIM_CHUNK, r_SET_MAILBOX, r_OPEN_MAILBOX, r_SET_RULESET;
	
	public District(int id){
		this.id = id; JsonObject obj = StateUtil.getDistrictJson(id);
		type = DistrictType.valueOf(JsonUtil.getIfExists(obj, "type", DistrictType.WILDERNESS.name()));
		created.load(obj);
		manage.load(obj);
		neighbors.load(obj);
		name = JsonUtil.getIfExists(obj, "name", "Unnamed District");
		munct.load(obj);
		color.load(obj);
		price.load(obj);
		icon.load(obj);
		chunks = JsonUtil.getIfExists(obj, "chunks", 0).intValue();
		chunktax = JsonUtil.getIfExists(obj, "chunktax", 0).longValue();
		mailbox.load(obj);
		manage.linkRuleHolder(rules);
		rules.add(r_OCCB = new Rule("only_citizen_can_buy", false, false, Initiator.COUNCIL_ANY, Initiator.INCHARGE));
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
		rules.getMap().lock();
		rules.load(obj);
		//import old settings from old saves
		if(obj.has("can_foreigners_settle")) r_OCCB.set(!obj.get("can_foreigners_settle").getAsBoolean());
		if(obj.has("unclaim_chunks_if_bankrupt")) r_ONBANKRUPT.set(obj.get("unclaim_chunks_if_bankrupt").getAsBoolean());
		//
		MinecraftForge.EVENT_BUS.post(new DistrictEvent.Load(this));
		rules.loadEx(obj);
		external.load(obj);
	}

	public JsonObject toJsonObject(){
		JsonObject obj = new JsonObject();
		obj.addProperty("id", id);
		obj.addProperty("type", type.name());
		created.save(obj);
		manage.save(obj);
		obj.addProperty("name", name);
		munct.save(obj);
		neighbors.save(obj);
		color.save(obj);
		price.save(obj);
		icon.save(obj);
		obj.addProperty("chunks", chunks);
		if(chunktax > 0){ obj.addProperty("chunktax", chunktax); }
		mailbox.save(obj);
		rules.save(obj);
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

	public String getName(){
		return name;
	}

	public void setName(String new_name){
		name = new_name;
	}

	public Municipality getMunicipality(){
		return munct.municipality;
	}

	public void setMunicipality(Municipality mun){
		if(munct.mun) munct.municipality.getDistricts().removeIf(pre -> pre == this.getId());
		if(!munct.mun) munct.county.getDistricts().removeIf(pre -> pre == this.getId());
		munct.set(mun);
		munct.municipality.getDistricts().add(this.getId());
	}
	
	public County getCounty(){
		return munct.getCounty();
	}

	public void setCounty(County ct){
		if(munct.mun) munct.municipality.getDistricts().removeIf(pre -> pre == this.getId());
		if(!munct.mun) munct.county.getDistricts().removeIf(pre -> pre == this.getId());
		munct.set(ct);
		munct.county.getDistricts().add(this.getId());
	}

	public MunCt getMunCt(){
		return munct;
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
		return munct.getCounty().getState();
	}

	@Override
	public Layer getParent(){
		return munct.getParent();
	}

	@Override
	public Layers getLayerType(){
		return Layers.DISTRICT;
	}

}
