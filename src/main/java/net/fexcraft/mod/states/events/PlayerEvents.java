package net.fexcraft.mod.states.events;

import net.fexcraft.mod.lib.perms.PermManager;
import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.api.Player;
import net.fexcraft.mod.states.impl.GenericPlayer;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;

@Mod.EventBusSubscriber
public class PlayerEvents {
	
	@SubscribeEvent
	public static void onLogin(PlayerLoggedInEvent event){
		Player player = PermManager.getPlayerPerms(event.player).getAdditionalData(GenericPlayer.class);
		States.PLAYERS.put(player.getUUID(), player);
		Print.chat(event.player, "&e====-====-====-====-====-====&0[&2States&0]");
		Print.chat(event.player, "&6Welcome back " + player.getFormattedNickname(event.player) + "&6!");//TODO replace with nick
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
	
	@SubscribeEvent
	public static void onLogout(PlayerLoggedOutEvent event){
		States.PLAYERS.remove(event.player.getGameProfile().getId());
	}

}
