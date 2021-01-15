package net.fexcraft.mod.states.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.fexcraft.lib.common.json.JsonUtil;

public class AliasLoader {
	
	public static final HashMap<String, ArrayList<String>> ALIASES = new HashMap<>();
	public static final HashMap<String, String> OVERRIDES = new HashMap<>();
	private static final ArrayList<String> NONE = new ArrayList<>();

	public static void load(){
		File file = new File(StConfig.CONFIG_PATH, "st-cmds.json");
		if(!file.exists()) generate(file);
		JsonObject obj = JsonUtil.get(file);
		if(obj.has("aliases")){
			JsonObject als = obj.get("aliases").getAsJsonObject();
			als.entrySet().forEach(entry -> ALIASES.put(entry.getKey(), JsonUtil.jsonArrayToStringArray(entry.getValue().getAsJsonArray())));
		}
		if(obj.has("override")){
			JsonObject als = obj.get("override").getAsJsonObject();
			als.entrySet().forEach(entry -> OVERRIDES.put(entry.getKey(), entry.getValue().getAsString()));
		}
	}

	private static void generate(File file){
		JsonObject obj = new JsonObject();
		obj.addProperty("__comment", "In this file you can define custom aliases for States Mod's Commands.");
		JsonObject als = new JsonObject();
		als.add("st-admin", new JsonArray());
		als.add("st-debug", new JsonArray());
		als.add("st-rule", new JsonArray());
		als.add("st-vote", new JsonArray());
		als.add("st-nick", new JsonArray());
		JsonArray stgui = new JsonArray();
		stgui.add("/states");
		stgui.add("stgui");
		stgui.add("stui");
		als.add("st-gui", stgui);
		als.add("st-mail", new JsonArray());
		als.add("ck", new JsonArray());
		als.add("dis", new JsonArray());
		als.add("mun", new JsonArray());
		als.add("st", new JsonArray());
		obj.add("aliases", als);
		obj.addProperty("___comment", "Bellow you can even override the default command prefix (with example included).");
		JsonObject ovr = new JsonObject();
		ovr.addProperty("some-cmd-prefix", "new-cmd-prefix");
		ovr.addProperty("st-example", "st-override");
		obj.add("override", ovr);
		JsonUtil.write(file, obj);
	}

	public static String getOverride(String string){
		return OVERRIDES.containsKey(string) ? OVERRIDES.get(string) : string;
	}

	public static ArrayList<String> getAlias(String string){
		return ALIASES.containsKey(string) ? ALIASES.get(string) : NONE;
	}

}
