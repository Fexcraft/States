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
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.data.root.BuyableType;
import net.fexcraft.mod.states.data.root.ColorHolder;
import net.fexcraft.mod.states.data.root.IconHolder;
import net.fexcraft.mod.states.data.root.Initiator;
import net.fexcraft.mod.states.data.root.MailReceiver;
import net.fexcraft.mod.states.data.root.Ruleable;
import net.fexcraft.mod.states.impl.capabilities.SignTileEntityCapabilityUtil;
import net.fexcraft.mod.states.util.RuleMap;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraft.util.math.BlockPos;

public class District implements ColorHolder, BuyableType, IconHolder, MailReceiver, Ruleable {
	
	private int id, chunks;
	private DistrictType type;
	private long created, changed, price, chunktax;
	private UUID creator, manager;
	private ArrayList<Integer> neighbors;
	private String name, color, icon;
	private Municipality municipality;
	private BlockPos mailbox;
	//
	private RuleMap rules = new RuleMap();
	private String ruleset;
	public final Rule r_CFS, r_ONBANKRUPT, r_SET_MANAGER, r_SET_CHUNKTAX;
	public final Rule r_SET_TYPE, r_SET_NAME, r_SET_PRICE, r_SET_COLOR, r_SET_ICON;
	public final Rule r_ALLOW_EXPLOSIONS;
	
	public District(int id){
		this.id = id; JsonObject obj = StateUtil.getDistrictJson(id);
		type = DistrictType.valueOf(JsonUtil.getIfExists(obj, "type", DistrictType.WILDERNESS.name()));
		created = JsonUtil.getIfExists(obj, "created", Time.getDate()).longValue();
		creator = UUID.fromString(obj.has("creator") ? obj.get("creator").getAsString() : States.CONSOLE_UUID);
		changed = JsonUtil.getIfExists(obj, "changed", Time.getDate()).longValue();
		neighbors = JsonUtil.jsonArrayToIntegerArray(JsonUtil.getIfExists(obj, "neighbors", new JsonArray()).getAsJsonArray());
		name = JsonUtil.getIfExists(obj, "name", "Unnamed District");
		municipality = StateUtil.getMunicipality(JsonUtil.getIfExists(obj, "municipality", -1).intValue());
		manager = obj.has("manager") ? UUID.fromString(obj.get("manager").getAsString()) : null;
		color = obj.has("color") ? obj.get("color").getAsString() : "#ffffff";
		price = JsonUtil.getIfExists(obj, "price", 0).longValue();
		icon = JsonUtil.getIfExists(obj, "icon", States.DEFAULT_ICON);
		chunks = JsonUtil.getIfExists(obj, "chunks", 0).intValue();
		chunktax = JsonUtil.getIfExists(obj, "chunktax", 0).longValue();
		mailbox = obj.has("mailbox") ? BlockPos.fromLong(obj.get("mailbox").getAsLong()) : null;
		ruleset = JsonUtil.getIfExists(obj, "ruleset", "Standard Ruleset");
		rules.add(r_CFS = new Rule("can_foreigners_settle", false, true, Initiator.COUNCIL_ANY, Initiator.INCHARGE));
		rules.add(r_ONBANKRUPT = new Rule("unclaim_chunks_if_bankrupt", false, true, Initiator.COUNCIL_ANY, Initiator.INCHARGE));
		rules.add(r_SET_TYPE = new Rule("set.type", null, true, Initiator.COUNCIL_ANY, Initiator.INCHARGE));
		rules.add(r_SET_NAME = new Rule("set.name", null, true, Initiator.COUNCIL_ANY, Initiator.INCHARGE));
		rules.add(r_SET_PRICE = new Rule("set.price", null, true, Initiator.COUNCIL_ANY, Initiator.HIGHERINCHARGE));
		rules.add(r_SET_MANAGER = new Rule("set.manager", null, true, Initiator.COUNCIL_ANY, Initiator.HIGHERINCHARGE));
		rules.add(r_SET_COLOR = new Rule("set.color", null, true, Initiator.COUNCIL_ANY, Initiator.INCHARGE));
		rules.add(r_SET_ICON = new Rule("set.icon", null, true, Initiator.COUNCIL_ANY, Initiator.INCHARGE));
		rules.add(r_SET_CHUNKTAX = new Rule("set.chunktax", null, true, Initiator.COUNCIL_ANY, Initiator.INCHARGE));
		rules.add(r_ALLOW_EXPLOSIONS = new Rule("allow.explosions", false, true, Initiator.COUNCIL_VOTE, Initiator.HIGHERINCHARGE));
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
	}

	public JsonObject toJsonObject(){
		JsonObject obj = new JsonObject();
		obj.addProperty("id", id);
		obj.addProperty("type", type.name());
		obj.addProperty("created", created);
		obj.addProperty("creator", creator.toString());
		obj.addProperty("changed", changed);
		obj.addProperty("name", name);
		obj.addProperty("municipality", municipality == null ? -1 : municipality.getId());
		obj.add("neighbors", JsonUtil.getArrayFromIntegerList(neighbors));
		if(!(manager == null)){ obj.addProperty("manager", manager.toString()); }
		obj.addProperty("color", color);
		//obj.addProperty("can_foreigners_settle", cfs);
		obj.addProperty("price", price);
		if(icon != null){ obj.addProperty("icon", icon); }
		obj.addProperty("chunks", chunks);
		if(chunktax > 0){ obj.addProperty("chunktax", chunktax); }
		//obj.addProperty("unclaim_chunks_if_bankrupt", onbankrupt);
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

	public long getCreated(){
		return created;
	}

	public void setCreated(long date){
		created = date;
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

	public List<Integer> getNeighbors(){
		return neighbors;
	}

	public void setChanged(long new_change){
		changed = new_change;
		Static.getServer().worlds[0].getChunkProvider().getLoadedChunks().forEach(chunk -> {
			SignTileEntityCapabilityUtil.processChunkChange(chunk, "district");
		});
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

	@Override
	public String getColor(){
		return color;
	}

	@Override
	public void setColor(String newcolor){
		color = newcolor;
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

	@Override
	public BlockPos getMailbox(){
		return mailbox;
	}

	@Override
	public void setMailbox(BlockPos pos){
		this.mailbox = pos;
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
		return getHead().equals(uuid) || municipality.isHead(uuid);
	}

	@Override
	public Ruleable getHigherInstance(){
		return municipality;
	}

}
