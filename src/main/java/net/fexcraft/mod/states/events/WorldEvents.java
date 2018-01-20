package net.fexcraft.mod.states.events;

import net.fexcraft.mod.states.States;
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
			//TODO add wilderness district
		}
		if(!States.DISTRICTS.containsKey(0)){
			//TODO add default district
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
