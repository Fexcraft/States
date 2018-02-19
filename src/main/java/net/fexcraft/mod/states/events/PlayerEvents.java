package net.fexcraft.mod.states.events;

import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

@Mod.EventBusSubscriber
public class PlayerEvents {
	
	@SubscribeEvent
	public static void onLoad(PlayerLoggedInEvent event){
		Print.chat(event.player, "&e====-====-====-====-====-====&0[&2States&0]");
		Print.chat(event.player, "&6Welcome back " + event.player.getName() + "&6!");//TODO replace with nick
		if(event.player.dimension != 0){
			Print.chat(event.player, "&2You are currently in &7DIM($0)&0.".replace("$0", event.player.dimension + ""));
		}
		else{
			Chunk chunk = StateUtil.getChunk(event.player);
			Print.chat(event.player, "&2You are currently in a district called &7$0&2.".replace("$0", chunk.getDistrict().getName()));
			Print.chat(event.player, "&2Which is part of the &6$1 &7$0&2.".replace("$0", chunk.getDistrict().getMunicipality().getName()).replace("$1", chunk.getDistrict().getMunicipality().getType().getTitle()));
			Print.chat(event.player, "&2In the State of &7$0&2.".replace("$0", chunk.getDistrict().getMunicipality().getState().getName()));
		}
		Print.chat(event.player, "&e====-====-====-====-====-====&0[&2States&0]");
	}

}
