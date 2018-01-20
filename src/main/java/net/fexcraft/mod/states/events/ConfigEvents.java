package net.fexcraft.mod.states.events;

import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.states.States;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class ConfigEvents {
	
	@SubscribeEvent
	public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event){
		if(event.getModID().equals(States.MODID)){
			Config.refresh();
			if(Config.getConfig().hasChanged()){
				Config.getConfig().save();
			}
		}
	}
	
}
