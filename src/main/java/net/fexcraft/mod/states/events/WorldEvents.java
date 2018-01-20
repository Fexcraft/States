package net.fexcraft.mod.states.events;

import com.google.gson.JsonObject;

import net.fexcraft.mod.lib.util.json.JsonUtil;
import net.fexcraft.mod.lib.util.math.Time;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.District;
import net.fexcraft.mod.states.api.DistrictType;
import net.fexcraft.mod.states.impl.GenericDistrict;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class WorldEvents {
	
	@SubscribeEvent
	public static void onWorldLoad(WorldEvent.Load event){
		if(event.getWorld().provider.getDimension() != 0){
			return;
		}
		if(!States.DISTRICTS.containsKey(-1)){
			if(!District.getDistrictFile(-1).exists()){
				JsonObject object = new JsonObject();
				object.addProperty("id", -1);
				object.addProperty("type", DistrictType.WILDERNESS.name());
				object.addProperty("created", Time.getDate());
				object.addProperty("creator", States.CONSOLE_UUID);
				object.addProperty("changed", Time.getDate());
				JsonUtil.write(District.getDistrictFile(-1), object);
			}
			States.DISTRICTS.put(-1, new GenericDistrict(-1));
		}
		if(!States.DISTRICTS.containsKey(0)){
			if(!District.getDistrictFile(0).exists()){
				JsonObject object = new JsonObject();
				object.addProperty("id", 0);
				object.addProperty("type", DistrictType.MUNICIPIAL.name());
				object.addProperty("created", Time.getDate());
				object.addProperty("creator", States.DEF_UUID);
				object.addProperty("changed", Time.getDate());
				JsonUtil.write(District.getDistrictFile(0), object);
			}
			States.DISTRICTS.put(0, new GenericDistrict(0));
		}
	}
	
	@SubscribeEvent
	public static void onWorldLoad(WorldEvent.Unload event){
		if(event.getWorld().provider.getDimension() != 0){
			return;
		}
		//TODO save
	}
	
}
