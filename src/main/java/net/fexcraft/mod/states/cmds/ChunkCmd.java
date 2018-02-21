package net.fexcraft.mod.states.cmds;

import net.fexcraft.mod.fsmm.util.AccountManager;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.lib.api.common.fCommand;
import net.fexcraft.mod.lib.perms.PermManager;
import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.lib.util.common.Static;
import net.fexcraft.mod.lib.util.math.Time;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.api.District;
import net.fexcraft.mod.states.util.ImageCache;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

@fCommand
public class ChunkCmd extends CommandBase {

	@Override
	public String getName(){
		return "ck";
	}

	@Override
	public String getUsage(ICommandSender sender){
		return "/ck";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(args.length == 0){
			Print.chat(sender, "&7/ck claim <args>");
			Print.chat(sender, "&7/ck info");
			Print.chat(sender, "&7/ck map");
			Print.chat(sender, "&7/ck buy");
			Print.chat(sender, "&7/ck sfs (set for-sale)");
			Print.chat(sender, "&7/ck unclaim [admin-only]");
			Print.chat(sender, "&7/ck set <args>");
			Print.chat(sender, "&7/ck link <args>");
			Print.chat(sender, "&7/ck whitelist <args>");
			Print.chat(sender, "&7/ck update <option:range> [admin-only]");
			Print.chat(sender, "&7/ck queue");
			return;
		}
		if(sender instanceof EntityPlayer == false){
			Print.chat(sender, "&7Only available Ingame.");
			return;
		}
		EntityPlayer player = (EntityPlayer)sender.getCommandSenderEntity();
		Chunk chunk = StateUtil.getChunk(player);
		switch(args[0]){
			case "claim":{
				if(args.length == 1){
					Print.chat(sender, "&7/ck claim <district> <optinal:range>");
					return;
				}
				int disid = Integer.parseInt(args[1]);
				int range = args.length == 3 ? Integer.parseInt(args[2]) : 0;
				District district = StateUtil.getDistrict(disid);
				if(district.getId() == -1){
					Print.chat(sender, "&7District not found. (" + disid + ");");
					return;
				}
				boolean can = true;//(district.getManager() != null && district.getManager().equals(player.getGameProfile().getId())) || (district.getMunicipality().getMayor() != null && district.getMunicipality().getMayor().equals(player.getGameProfile().getId())) || PermManager.getPlayerPerms(player).hasPermission(States.ADMIN_PERM);
				if(can){
					if(range > 3){
						Print.chat(sender, "Invalid range, setting to \"3\"!");
						range = 3;
					}
					if(range == 0){
						if(district.getMunicipality().getAccount().getBalance() < chunk.getPrice()){
							Print.chat(sender, "&7Municipality does not have enough money to claim this chunk.");
							Print.chat(sender, "&7Required: &9" + Config.getWorthAsString(chunk.getPrice()) + " &8|| &7Available: &9" + Config.getWorthAsString(district.getMunicipality().getAccount().getBalance()));
							return;
						}
						if(chunk.getDistrict().getId() < 0){
							if(chunk.getPrice() > 0 && !AccountManager.INSTANCE.getBank(district.getMunicipality().getAccount().getBankId()).processTransfer(sender, district.getMunicipality().getAccount(), chunk.getPrice(), States.SERVERACCOUNT)){
								return;
							}
							chunk.setDistrict(district);
							chunk.setClaimer(player.getGameProfile().getId());
							chunk.setChanged(Time.getDate());
							chunk.setPrice(0);
							chunk.save();
							ImageCache.update(player.world, player.world.getChunkFromChunkCoords(chunk.xCoord(), chunk.zCoord()), "claim", "all");
							Print.chat(sender, "&7Chunk Claimed. (" + district.getId() + ");");
						}
						else{
							Print.chat(sender, "&7Chunk is already claimed.");
						}
					}
					else{
						int r = range == 1 ? 3 : range == 2 ? 5 : 7;
						Print.chat(sender, "&6Result: &7(" + district.getId() + ")");
						for(int i = 0; i < r; i++){
							String str = "&0[";
							for(int j = 0; j < r; j++){
								int x = (chunk.xCoord() - range) + i;
								int z = (chunk.zCoord() - range) + j;
								Chunk ck = StateUtil.getChunk(x, z);
								if(ck == null){
									//Print.chat(sender, "&7Chunk at " + x + "x, " + z + "z &creturned null&7!");
									str += "&4#";
									continue;
								}
								if(ck.getDistrict().getId() >= 0){
									//Print.chat(sender, "&7Chunk at " + x + "x, " + z + "z is &calready claimed&7.");
									str += "&c#";
									continue;
								}
								if(district.getMunicipality().getAccount().getBalance() < chunk.getPrice()){
									str += "&b#";
									continue;
								}
								if(chunk.getPrice() > 0 && !AccountManager.INSTANCE.getBank(district.getMunicipality().getAccount().getBankId()).processTransfer(sender, district.getMunicipality().getAccount(), chunk.getPrice(), States.SERVERACCOUNT)){
									str += "&3#";
									continue;
								}
								ck.setDistrict(district);
								ck.setClaimer(player.getGameProfile().getId());
								ck.setChanged(Time.getDate());
								ck.setPrice(0);
								ck.save();
								ImageCache.update(player.world, player.world.getChunkFromChunkCoords(ck.xCoord(), ck.zCoord()), "claim", "all");
								//Print.chat(sender, "&7Chunk at " + x + "x, " + z + "z &2claimed&7!");
								str += "&2#";
							}
							Print.chat(sender, str + "&0]");
						}
						Print.chat(sender, "&4#&7 - chunk data returned null &8| &3#&7 transfer error");
						Print.chat(sender, "&c#&7 - chunk already claimed &8| &b#&7 - no money &8| &2#&7 - chunk claimed.");
					}
				}
				else{
					Print.chat(sender, "&7No permission.");
				}
				return;
			}
			case "map":{
				int r = 9, rh = 4;
				for(int i = 0; i < r; i++){
					String str = "&0[";
					for(int j = 0; j < r; j++){
						int x = (chunk.xCoord() - rh) + i;
						int z = (chunk.zCoord() - rh) + j;
						Chunk ck = StateUtil.getChunk(x, z);
						if(ck == null){
							str += "&4#";
							continue;
						}
						if(ck.getDistrict().getId() >= 0){
							str += "&9#";
							continue;
						}
						else{
							str += "&2#";
						}
					}
					Print.chat(sender, str + "&0]");
				}
				Print.chat(sender, "&4#&7 - null &8| &9#&7 - claimed &8| &2#&7 - not claimed.");
				return;
			}
			case "info":{
				Print.chat(sender, "&2Info of chunk &7" + chunk.xCoord() + "x&2, &7" + chunk.zCoord() + "z&2:");
				Print.chat(sender, "&9District: &7" + chunk.getDistrict().getName() + " (" + chunk.getDistrict().getId() + ")");
				Print.chat(sender, "&9Price: &7" + Config.getWorthAsString(chunk.getPrice()));
				Print.chat(sender, "&9Last change: &7" + Time.getAsString(chunk.getChanged()));
				Print.chat(sender, "&9Linked chunks: &7" + chunk.getLinkedChunks().size());
				if(chunk.getLinkedChunks().size() > 0){
					for(int i = 0; i < chunk.getLinkedChunks().size(); i++){
						Print.chat(sender, "&c-> &9" + chunk.getLinkedChunks().get(i));
					}
				}
				Print.chat(sender, "&2Claimed by &7" + Static.getPlayerNameByUUID(chunk.getClaimer()) + "&2 at &8" + Time.getAsString(chunk.getCreated()));
				return;
			}
			case "update":{
				//TODO reverse;
				if(!PermManager.getPlayerPerms(player).hasPermission(States.ADMIN_PERM)){
					int range = args.length > 1 ? Integer.parseInt(args[1]) : 0;
					if(range <= 0){
						ImageCache.update(player.world, player.world.getChunkFromChunkCoords(chunk.xCoord(), chunk.zCoord()), "update", "all");
						Print.chat(sender, "&9Queued for map update.");
					}
					else{
						int r = (range * 2) + 1;
						int c = 0;
						for(int i = 0; i < r; i++){
							for(int j = 0; j < r; j++){
								int x = (chunk.xCoord() - range) + i;
								int z = (chunk.zCoord() - range) + j;
								Chunk ck = StateUtil.getChunk(x, z);
								if(ck == null){
									continue;
								}
								c++;
								ImageCache.update(player.world, player.world.getChunkFromChunkCoords(x, z), "update", "all");
							}
						}
						Print.chat(sender, "&2" + c + " &9chunks queued for map update.");
						Print.chat(sender, "&9There are &2" + ImageCache.TYPES.length + " &9map modes activated.");
					}
				}
				return;
			}
			case "queue":{
				Print.chat(sender, "&9There are &2" + ImageCache.getQueue().size() + "&9 chunk map updates queued.");
				Print.chat(sender, "&9Current Config allows for &3" + net.fexcraft.mod.states.util.Config.MAP_UPDATES_PER_TICK + "&9 map updates per server tick.");
				return;
			}
			default:{
				Print.chat(sender, "//TODO");
				return;
			}
		}
	}

}
