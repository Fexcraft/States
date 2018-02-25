package net.fexcraft.mod.states.events;

import net.fexcraft.mod.lib.perms.PermManager;
import net.fexcraft.mod.lib.util.common.Formatter;
import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.lib.util.common.Static;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.api.Player;
import net.fexcraft.mod.states.impl.GenericPlayer;
import net.fexcraft.mod.states.util.SignUtil;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraft.block.BlockSign;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;

@Mod.EventBusSubscriber
public class PlayerEvents {
	
	@SubscribeEvent
	public static void onLogin(PlayerLoggedInEvent event){
		Player player = PermManager.getPlayerPerms(event.player).getAdditionalData(GenericPlayer.class);
		if(player == null){
			Print.chat(event.player, "Player data couldn't be loaded.");
			return;
		}
		States.PLAYERS.put(event.player.getGameProfile().getId(), player);
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
	
	@SubscribeEvent
	public static void onInteract(PlayerInteractEvent event){
		if(event.getWorld().isRemote || event.getEntityPlayer().getActiveHand() == EnumHand.OFF_HAND){
			return;
		}
		if(event.getWorld().getBlockState(event.getPos()).getBlock() instanceof BlockSign){
			//IBlockState state = event.getWorld().getBlockState(event.getPos());
			TileEntitySign te_sign = (TileEntitySign)event.getWorld().getTileEntity(event.getPos());
			if(te_sign == null || te_sign.signText == null || te_sign.signText[0] == null){
				return;
			}
			Chunk chunk = StateUtil.getChunk(event.getWorld(), event.getPos());
			if(te_sign.signText[0].getUnformattedText().equalsIgnoreCase("[States]")){
				switch(te_sign.signText[1].getUnformattedText().toLowerCase()){
					case "chunk":{
						SignUtil.updateChunkState(event.getEntityPlayer(), chunk, te_sign);
						break;
					}
					default: break;
				}
			}
			else if(te_sign.signText[0].getUnformattedText().startsWith("[States]> ")){
				switch(te_sign.signText[0].getUnformattedText().replace("[States]> ", "").toLowerCase()){
					case "chunk":{
						SignUtil.updateChunkState(event.getEntityPlayer(), chunk, te_sign);
						if(te_sign.signText[1].getUnformattedText().equals(Formatter.format("&2For Sale!"))){
							te_sign.signText[1] = new TextComponentString(Formatter.format("&cProcessing..."));
							Static.getServer().commandManager.executeCommand(event.getEntityPlayer(), "ck buy via-sign " + te_sign.getPos().toLong());
							SignUtil.updateChunkState(event.getEntityPlayer(), chunk, te_sign);
							return;
						}
						break;
					}
					default: break;
				}
			}
			else return;
		}
		else return;
	}
	
	

}
