package net.fexcraft.mod.states.impl;

import java.io.File;
import java.util.List;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.util.AccountManager;
import net.fexcraft.mod.lib.util.json.JsonUtil;
import net.fexcraft.mod.lib.util.lang.ArrayList;
import net.fexcraft.mod.lib.util.math.Time;
import net.fexcraft.mod.states.api.State;

public class GenericState implements State {
	
	private int id, capital;
	private String name, color;
	private long created, changed;
	private UUID creator, leader;
	private Account account;
	private ArrayList<Integer> neighbors, municipalities, blacklist;
	private ArrayList<UUID> council;

	public GenericState(int value){
		id = value;
		JsonObject obj = JsonUtil.get(this.getStateFile());
		name = JsonUtil.getIfExists(obj, "name", "Unnamed State");
		created = JsonUtil.getIfExists(obj, "created", Time.getDate()).longValue();
		changed = JsonUtil.getIfExists(obj, "changed", Time.getDate()).longValue();
		creator = obj.has("creator") ? UUID.fromString(obj.get("creator").getAsString()) : null;
		leader = obj.has("leader") ? UUID.fromString(obj.get("leader").getAsString()) : null;
		account = AccountManager.INSTANCE.getAccount("state", id + "", true);
		capital = JsonUtil.getIfExists(obj, "capital", -1).intValue();
		neighbors = JsonUtil.jsonArrayToIntegerArray(JsonUtil.getIfExists(obj, "neighbors", new JsonArray()).getAsJsonArray());
		municipalities = JsonUtil.jsonArrayToIntegerArray(JsonUtil.getIfExists(obj, "municipalities", new JsonArray()).getAsJsonArray());
		council = JsonUtil.jsonArrayToUUIDArray(JsonUtil.getIfExists(obj, "council", new JsonArray()).getAsJsonArray());
		color = JsonUtil.getIfExists(obj, "color", "#ffffff");
		blacklist = JsonUtil.jsonArrayToIntegerArray(JsonUtil.getIfExists(obj, "blacklist", new JsonArray()).getAsJsonArray());
	}

	@Override
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
		return obj;
	}

	@Override
	public void save(){
		JsonObject obj = toJsonObject();
		obj.addProperty("last_save", Time.getDate());
		File file = getStateFile();
		if(!file.getParentFile().exists()){ file.getParentFile().mkdirs(); }
		JsonUtil.write(file, obj);
		//
		AccountManager.INSTANCE.saveAccount(account);
	}

	@Override
	public int getId(){
		return id;
	}

	@Override
	public String getName(){
		return name;
	}

	@Override
	public void setName(String new_name){
		name = new_name;
	}

	@Override
	public boolean isUnionCapital(){
		return false;//TODO
	}

	@Override
	public void setChanged(long new_change){
		changed = new_change;
	}

	@Override
	public List<Integer> getMunicipalities(){
		return municipalities;
	}

	@Override
	public List<Integer> getNeighbors(){
		return neighbors;
	}

	@Override
	public long getCreated(){
		return created;
	}

	@Override
	public UUID getCreator(){
		return creator;
	}

	@Override
	public long getChanged(){
		return changed;
	}

	@Override
	public Account getAccount(){
		return account;
	}

	@Override
	public UUID getLeader(){
		return leader;
	}

	@Override
	public void setLeader(UUID uuid){
		leader = uuid;
	}

	@Override
	public List<UUID> getCouncil(){
		return council;
	}

	@Override
	public int getCapitalId(){
		return capital;
	}

	@Override
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

	@Override
	public List<Integer> getBlacklist(){
		return blacklist;
	}
	
}
