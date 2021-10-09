package net.fexcraft.mod.states.data;

import java.util.Map;

import net.fexcraft.app.json.JsonHandler;
import net.fexcraft.app.json.JsonMap;

public interface Saveable {

	public void save(JsonMap map);
	
	public void load(JsonMap map);
	
	public default Map<String, Object> saveMap(){
		JsonMap map = new JsonMap();
		this.save(map);
		return JsonHandler.dewrap(map);
	}
	
	public default String saveId(){
		return null;
	}
	
	public default String saveTable(){
		return null;
	}

}
