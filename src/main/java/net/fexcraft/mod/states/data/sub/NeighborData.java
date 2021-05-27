package net.fexcraft.mod.states.data.sub;

import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.fexcraft.lib.common.json.JsonUtil;
import net.fexcraft.mod.states.data.root.Loadable;

public class NeighborData implements Loadable {
	
	private ArrayList<Integer> neighbors = new ArrayList<>();

	@Override
	public void load(JsonObject obj){
		neighbors = JsonUtil.jsonArrayToIntegerArray(JsonUtil.getIfExists(obj, "neighbors", new JsonArray()).getAsJsonArray());
	}

	@Override
	public void save(JsonObject obj){
		obj.add("neighbors", JsonUtil.getArrayFromIntegerList(neighbors));
	}
	
	public ArrayList<Integer> get(){
		return neighbors;
	}

	public boolean contains(int id){
		return neighbors.contains(id);
	}

	public void add(int id){
		neighbors.add(id);
	}

	public int get(int integer){
		return neighbors.get(integer);
	}

	public int size(){
		return neighbors.size();
	}

}
