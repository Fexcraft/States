package net.fexcraft.mod.states.data;

import java.util.ArrayList;

import net.fexcraft.app.json.JsonHandler;
import net.fexcraft.app.json.JsonMap;

public class NeighborData implements Saveable {
	
	private ArrayList<Integer> neighbors = new ArrayList<>();

	@Override
	public void load(JsonMap map){
		if(map.has("neighbors")) neighbors = JsonHandler.dewrapc(map.getArray("neighbors"));
	}

	@Override
	public void save(JsonMap map){
		if(neighbors.size() > 0) map.add("neighbors", JsonHandler.wrap(neighbors, null));
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
