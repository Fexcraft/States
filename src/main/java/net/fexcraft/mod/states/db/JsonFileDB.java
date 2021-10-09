package net.fexcraft.mod.states.db;

import static net.fexcraft.mod.states.util.Settings.SAVE_SPACED_JSON;

import java.io.File;

import net.fexcraft.app.json.JsonHandler;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.data.Saveable;

public class JsonFileDB implements Database {

	@Override
	public void save(Saveable type){
		File file = new File(States.STATES_DIR, type.saveTable() + "/" + type.saveId() + ".json");
		if(!file.getParentFile().exists()) file.getParentFile().mkdirs();
		JsonMap map = new JsonMap();
		type.save(map);
		JsonHandler.print(file, map, !SAVE_SPACED_JSON, SAVE_SPACED_JSON);
	}

	@Override
	public Object load(String table, String id){
		return JsonHandler.parse(new File(States.STATES_DIR, table + "/" + id + ".json"));
	}

	@Override
	public boolean internal(){
		return true;
	}

}
