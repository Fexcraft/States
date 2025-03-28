package net.fexcraft.mod.states.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.fexcraft.lib.common.json.JsonUtil;
import net.fexcraft.lib.common.utils.Formatter;
import net.fexcraft.lib.mc.network.Network;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.uni.UniReg;

public class UpdateHandler {

	private static String newversion, lmcv;
	public static String STATE = null;
	
	private static String PREFIX = Formatter.format("&0[&2States&0]");
	
	public static void initialize(){
		getDataFromServer(); sync();
		if(newversion != null){
			if(!newversion.equalsIgnoreCase(States.VERSION)) {
				STATE = PREFIX + "&7 New Version avaible! (&a" + newversion + "&7)."
				+ "\n" + PREFIX + "&7 Your Installed version: (&c" + States.VERSION + "&7).";
			}
		}
		if(lmcv != null && !lmcv.equals(UniReg.LOADER_VERSION)){
			if(STATE == null){
				STATE = PREFIX + "&7 Now avaible for MC " + lmcv + "!";
			}
			else{
				STATE += "\n" + PREFIX + "&7 Now avaible for MC " + lmcv + "!";
			}
		}
	}

	private static void sync(){
		newversion = JsonUtil.getIfExists(data, "latest_version", States.VERSION);
		lmcv = JsonUtil.getIfExists(data, "latest_mc_version", UniReg.LOADER_VERSION);
	}
	
	private static JsonObject data;
	
	public static void getDataFromServer(){
		JsonObject json = Network.getModData("states");
		if(json == null){
			data = new JsonObject();
			data.addProperty("latest_version", States.VERSION);
			data.addProperty("latest_mc_version", UniReg.LOADER_VERSION);
		}
		else{
			try{
				boolean found = false;
				for(JsonElement elm : json.get("versions").getAsJsonArray()){
					if(elm.getAsJsonObject().get("version").getAsString().equals(UniReg.LOADER_VERSION)){
						data = elm.getAsJsonObject();
						found = true; break;
					}
				}
				if(!found){
					data = new JsonObject();
					data.addProperty("latest_version", States.VERSION);
					data.addProperty("latest_mc_version", UniReg.LOADER_VERSION);
				}
			}
			catch(Exception e){
				e.printStackTrace();
				data = new JsonObject();
				data.addProperty("latest_version", States.VERSION);
				data.addProperty("latest_mc_version", UniReg.LOADER_VERSION);
			}
		}
	}
	
}