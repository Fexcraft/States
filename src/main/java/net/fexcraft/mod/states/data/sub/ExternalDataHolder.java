package net.fexcraft.mod.states.data.sub;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.fexcraft.mod.states.data.root.ExternalData;
import net.fexcraft.mod.states.data.root.Loadable;

public class ExternalDataHolder implements Loadable {
	
	private TreeMap<String, ExternalData> datas = new TreeMap<>();
	
	public <T extends ExternalData> T get(String id){
		return (T)datas.get(id);
	}
	
	public ExternalData set(String id, ExternalData obj){
		return datas.put(id, obj);
	}
	
	public Map<String, ExternalData> getAll(){
		return datas;
	}

	@Override
	public void load(JsonObject obj){
		if(!obj.has("ex-data") || datas.isEmpty()) return;
		JsonObject exobj = obj.get("ex-data").getAsJsonObject();
		for(Entry<String, JsonElement> elm : exobj.entrySet()){
			ExternalData data = get(elm.getKey());
			if(data != null) data.load(elm.getValue());
		}
	}

	@Override
	public void save(JsonObject obj){
		if(datas.isEmpty()) return;
		JsonObject exobj = new JsonObject();
		for(Entry<String, ExternalData> entry : datas.entrySet()){
			exobj.add(entry.getKey(), entry.getValue().save());
		}
		obj.add("ex-data", exobj);
	}

}
