package net.fexcraft.mod.states.cmds;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.util.AccountManager;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.lib.api.common.fCommand;
import net.fexcraft.mod.lib.perms.PermManager;
import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.lib.util.common.Static;
import net.fexcraft.mod.lib.util.math.Time;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.api.ChunkType;
import net.fexcraft.mod.states.api.District;
import net.fexcraft.mod.states.api.Player;
import net.fexcraft.mod.states.util.ImageCache;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

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
    public boolean checkPermission(MinecraftServer server, ICommandSender sender){
        return sender != null;
    }
	
	@Override
    public int getRequiredPermissionLevel(){
        return 0;
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
			Print.chat(sender, "&7/ck types");
			return;
		}
		if(sender.getCommandSenderEntity() instanceof EntityPlayer == false){
			Print.chat(sender, "&7Only available Ingame.");
			return;
		}
		EntityPlayer player = (EntityPlayer)sender.getCommandSenderEntity();
		Player playerdata = StateUtil.getPlayer(player);
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
				boolean can = (district.getManager() != null && district.getManager().equals(player.getGameProfile().getId())) || (district.getMunicipality().getMayor() != null && district.getMunicipality().getMayor().equals(player.getGameProfile().getId())) || isAdmin(player);
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
								String sign = x == chunk.xCoord() && z == chunk.zCoord() ? "+" : "#";
								Chunk ck = StateUtil.getChunk(x, z);
								if(ck == null){
									//Print.chat(sender, "&7Chunk at " + x + "x, " + z + "z &creturned null&7!");
									str += "&4" + sign;
									continue;
								}
								if(ck.getDistrict().getId() >= 0){
									//Print.chat(sender, "&7Chunk at " + x + "x, " + z + "z is &calready claimed&7.");
									str += "&c" + sign;
									continue;
								}
								if(district.getMunicipality().getAccount().getBalance() < chunk.getPrice()){
									str += "&b" + sign;
									continue;
								}
								if(chunk.getPrice() > 0 && !AccountManager.INSTANCE.getBank(district.getMunicipality().getAccount().getBankId()).processTransfer(sender, district.getMunicipality().getAccount(), chunk.getPrice(), States.SERVERACCOUNT)){
									str += "&3" + sign;
									continue;
								}
								ck.setDistrict(district);
								ck.setClaimer(player.getGameProfile().getId());
								ck.setChanged(Time.getDate());
								ck.setPrice(0);
								ck.save();
								ImageCache.update(player.world, player.world.getChunkFromChunkCoords(ck.xCoord(), ck.zCoord()), "claim", "all");
								//Print.chat(sender, "&7Chunk at " + x + "x, " + z + "z &2claimed&7!");
								str += "&2" + sign;
							}
							Print.chat(sender, str + "&0]");
						}
						Print.chat(sender, "&4#&7 - chunk data returned null &8| &3#&7 transfer error &8| &7+ your position");
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
						String sign = x == chunk.xCoord() && z == chunk.zCoord() ? "+" : "#";
						Chunk ck = StateUtil.getChunk(x, z);
						if(ck == null){
							str += "&4" + sign;
							continue;
						}
						if(ck.getDistrict().getId() >= 0){
							str += "&9" + sign;
							continue;
						}
						else{
							str += "&2" + sign;
						}
					}
					Print.chat(sender, str + "&0]");
				}
				Print.chat(sender, "&4#&7 - null &8| &9#&7 - claimed &8| &2#&7 - not claimed &8| &7+ your position.");
				return;
			}
			case "info":{
				Print.chat(sender, "&e====-====-====-====-====-====&0[&2States&0]");
				Print.chat(sender, "&6Info of chunk &7" + chunk.xCoord() + "x&2, &7" + chunk.zCoord() + "z&2:");
				Print.chat(sender, "&9District: &7" + chunk.getDistrict().getName() + " (" + chunk.getDistrict().getId() + ")");
				Print.chat(sender, "&9Owner: &7" + (chunk.getType() == ChunkType.PRIVATE ? Static.getPlayerNameByUUID(UUID.fromString(chunk.getOwner())) : chunk.getOwner()));
				Print.chat(sender, "&9Price: &7" + (chunk.getPrice() > 0 ? Config.getWorthAsString(chunk.getPrice()) : "not for sale"));
				Print.chat(sender, "&9Type: &7" + chunk.getType().name().toLowerCase());
				Print.chat(sender, "&9Last change: &7" + Time.getAsString(chunk.getChanged()));
				Print.chat(sender, "&9Linked chunks: &7" + chunk.getLinkedChunks().size());
				if(chunk.getLinkedChunks().size() > 0){
					for(int i = 0; i < chunk.getLinkedChunks().size(); i++){
						Print.chat(sender, "&c-> &9" + chunk.getLinkedChunks().get(i));
					}
				}
				Print.chat(sender, "&9Linked: &7" + (chunk.getLink() == null ? "false" : chunk.getLink()[0] + "x, " + chunk.getLink()[1] + "z"));
				Print.chat(sender, "&2Claimed by &7" + Static.getPlayerNameByUUID(chunk.getClaimer()) + "&2 at &8" + Time.getAsString(chunk.getCreated()));
				return;
			}
			case "update":{
				if(isAdmin(player)){
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
			case "unclaim":{
				if(isAdmin(player)){
					int range = args.length > 1 ? Integer.parseInt(args[1]) : 0;
					if(range <= 0){
						chunk.setClaimer(player.getGameProfile().getId());
						chunk.setDistrict(StateUtil.getDistrict(-1));
						chunk.setChanged(Time.getDate());
						chunk.setType(ChunkType.NORMAL);
						chunk.setPrice(net.fexcraft.mod.states.util.Config.DEFAULT_CHUNK_PRICE);
						chunk.save();
						ImageCache.update(player.world, player.world.getChunkFromChunkCoords(chunk.xCoord(), chunk.zCoord()), "unclaim", "all");
						Print.chat(sender, "&9Chunk unclaimed and resseted.");
					}
					else{
						int r = (range * 2) + 1;
						int c = 0;
						for(int i = 0; i < r; i++){
							for(int j = 0; j < r; j++){
								int x = (chunk.xCoord() - range) + i;
								int z = (chunk.zCoord() - range) + j;
								Chunk ck = StateUtil.getTempChunk(x, z);
								if(ck == null){
									continue;
								}
								ck.setClaimer(player.getGameProfile().getId());
								ck.setDistrict(StateUtil.getDistrict(-1));
								ck.setChanged(Time.getDate());
								ck.setType(ChunkType.NORMAL);
								ck.setPrice(net.fexcraft.mod.states.util.Config.DEFAULT_CHUNK_PRICE);
								ck.save();
								c++;
								ImageCache.update(player.world, player.world.getChunkFromChunkCoords(x, z), "unclaim", "all");
							}
						}
						Print.chat(sender, "&2" + c + " &9chunks have been resseted.");
					}
				}
				return;
			}
			case "buy":{
				//TODO add check for companies, based on their type
				if(chunk.getDistrict().getId() < 0){
					Print.chat(sender, "Not claimed chunks can not be bought.");
					return;
				}
				if(chunk.getType() == ChunkType.PRIVATE && UUID.fromString(chunk.getOwner()).equals(player.getGameProfile().getId())){
					Print.chat(sender, "&7&oYou already do own this chunk.");
					return;
				}
				if(args.length >= 2 && args[1].equals("company")){
					//TODO
					return;
				}
				if(playerdata.getMunicipality().getId() < 0){
					Print.chat(sender, "&7You must be citizen of a Municipality to be able to buy chunks.");
					return;
				}
				if(playerdata.getMunicipality().getState().getId() < 0){
					Print.chat(sender, "&7You must be citizen of a State to be able to buy chunks.");
					return;
				}
				if(chunk.getDistrict().getMunicipality().getId() != playerdata.getMunicipality().getId() && !chunk.getDistrict().canForeignersSettle()){
					Print.chat(sender, "&cYou are not part of this Municipality.");
					Print.chat(sender, "&cChunks in this District can not be bought by Foreigners.");
					return;
				}
				if(chunk.getDistrict().getMunicipality().getPlayerBlacklist().contains(player.getGameProfile().getId())){
					Print.chat(sender, "&cYou are blacklisted in this Municipality.");
					return;
				}
				if(chunk.getDistrict().getMunicipality().getState().getBlacklist().contains(playerdata.getMunicipality().getState().getId())){
					Print.chat(sender, "&cPlayers from your State can not buy chunks here.");
					return;
				}
				if(args.length >= 3 && args[1].equals("via-sign")){
					try{
						chunk = StateUtil.getChunk(player.world, BlockPos.fromLong(Long.parseLong(args[2])));
					}
					catch(Exception e){
						Print.chat(sender, "&9Error: &7" + e.getMessage());
					}
				}
				if(chunk.getPrice() <= 0){
					Print.chat(sender, "&cChunk isn't for sale.");
				}
				else{
					Account receiver = null;
					boolean wasloadedin = true;
					switch(chunk.getType()){
						case COMPANY:{
							Print.chat(sender, "&cNot available yet.");//TODO companies.
							return;
						}
						case DISTRICT:
						case MUNICIPAL:
						case NORMAL:{
							receiver = chunk.getDistrict().getMunicipality().getAccount();
							break;
						}
						case PRIVATE:{
							receiver = AccountManager.INSTANCE.getAccount("player", chunk.getOwner());
							if(receiver == null){
								wasloadedin = false;
								receiver = AccountManager.INSTANCE.getAccount("player", chunk.getOwner(), true);
							}
							break;
						}
						case PUBLIC:
						case STATEOWNED:{
							receiver = chunk.getDistrict().getMunicipality().getState().getAccount();
							break;
						}
						default:{
							Print.chat(sender, "&cInvalid Chunk Type! Payment destination unknown.");
							return;
						}
					}
					Account ac_sender = StateUtil.getPlayer(player).getAccount();
					if(!AccountManager.INSTANCE.getBank(ac_sender.getBankId()).processTransfer(sender, ac_sender, chunk.getPrice(), receiver)){
						return;
					}
					long time = Time.getDate();
					chunk.setOwner(player.getGameProfile().getId().toString());
					chunk.setPrice(0);
					chunk.setType(ChunkType.PRIVATE);
					chunk.setChanged(time);
					chunk.save();
					ImageCache.update(player.world, player.world.getChunkFromChunkCoords(chunk.xCoord(), chunk.zCoord()), "bought", "chunk_types");
					Print.chat(sender, "&aChunk bought!");
					if(chunk.getLinkedChunks().size() > 0){
						chunk.getLinkedChunks().forEach(ckpos -> {
							Chunk ck = StateUtil.getTempChunk(ckpos);
							ck.setOwner(player.getGameProfile().getId().toString());
							ck.setPrice(0);
							ck.setType(ChunkType.PRIVATE);
							ck.setChanged(time);
							ck.save();
							ImageCache.update(player.world, player.world.getChunkFromChunkCoords(ck.xCoord(), ck.zCoord()), "bought", "chunk_types");
						});
						Print.chat(sender, "&7" + chunk.getLinkedChunks().size() + "&a linked chunks bought!");
					}
					if(!wasloadedin){
						AccountManager.INSTANCE.unloadAccount(receiver);
					}
				}
				return;
			}
			case "set_for_sale":
			case "set-for-sale":
			case "setforsale":
			case "sell":
			case "sfs":{
				if(!isPermitted(chunk, player)){
					if(args.length < 2){
						Print.chat(sender, "&9Missing argument.");
						Print.chat(sender, "&7/ck set-for-sale <price>");
						Print.chat(sender, "&6Remember!&2 1000 equals &71" + Config.CURRENCY_SIGN + "&2!");
						return;
					}
					try{
						Long price = Long.parseLong(args[1]);
						chunk.setPrice(price);
						chunk.setChanged(Time.getDate());
						chunk.save();
						Print.chat(sender, "&9Price set to &7" + Config.getWorthAsString(price));
					}
					catch(Exception e){
						Print.chat(sender, "&9Error: &7" + e.getMessage());
					}
				}
				return;
			}
			case "set":{
				if(isPermitted(chunk, player)){
					if(args.length < 2){
						Print.chat(sender, "&9Missing argument.");
						Print.chat(sender, "&7/ck set <option> <value>");
						return;
					}
					switch(args[1]){
						case "district":{
							if(isAdmin(player)){
								try{
									chunk.setDistrict(StateUtil.getDistrict(Integer.parseInt(args[2])));
									Print.chat(sender, "&2District set to: " + chunk.getDistrict().getName() + " (" + chunk.getDistrict().getId() + ");");
								}
								catch(Exception e){
									Print.chat(sender, "&9Error: &7" + e.getMessage());
								}
							}
							else{
								Print.chat(sender, "&2Once a chunk is claimed, the district cannot be changed.");
							}
							break;
						}
						case "price":{
							Print.chat(sender, "&2Please use the &7/ck set-for-sale &2command instead!");
							break;
						}
						case "link":{
							Print.chat(sender, "&2Please use the &7/ck link &2command instead!");
							break;
						}
						case "type":{
							if(args.length < 3){
								Print.chat(sender, "&7&o/ck set type <type>");
								break;
							}
							ChunkType type = ChunkType.valueOf(args[2].toUpperCase());
							if(type == null){
								Print.chat(sender, "&9Chunk Type not found. Use &7/ck types &9to see available types.");
							}
							else{
								long time = Time.getDate();
								switch(type){
									case COMPANY:{
										Print.chat(sender, "&2Please use the &7/ck set-for-sale &2command instead.");
										Print.chat(sender, "&2To buy as company use &7/ck buy company");
										break;
									}
									case STATEOWNED:
									case MUNICIPAL:
									case DISTRICT:
									case NORMAL:{
										chunk.setType(type);
										chunk.setOwner(null);
										chunk.setPrice(0);
										chunk.setChanged(time);
										chunk.getLinkedChunks().forEach(link -> {
											Chunk ck = StateUtil.getTempChunk(link);
											ck.setType(type);
											ck.setOwner(null);
											ck.setPrice(0);
											ck.setChanged(time);
											ck.save();
										});
										chunk.save();
										String to = type == ChunkType.NORMAL || type == ChunkType.DISTRICT ? "District" : type == ChunkType.MUNICIPAL ? "Municipality" : type == ChunkType.STATEOWNED ? "State" : "ERROR";
										Print.chat(sender, "&9Chunk given to the &2" + to + "&9!");
										break;
									}
									case PRIVATE:{
										Print.chat(sender, "&2Please use the &7/ck set-for-sale &2command instead.");
										break;
									}
									case PUBLIC:{
										chunk.setType(type);
										chunk.setChanged(time);
										chunk.getLinkedChunks().forEach(link -> {
											Chunk ck = StateUtil.getTempChunk(link);
											ck.setType(type);
											ck.setChanged(time);
											ck.save();
										});
										chunk.save();
										Print.chat(sender, "&2Chunk set to &cPUBLIC&2!");
										Print.chat(sender, "&2It is still yours, but anyone can edit blocks.");
										break;
									}
									default:{
										Print.chat(sender, "&4Invalid chunk type, this actually shouldn't happen.");
										break;
									}
								}
								ImageCache.update(player.world, player.world.getChunkFromChunkCoords(chunk.hashCode(), chunk.zCoord()), "type_changed", "chunk_types");
								chunk.getLinkedChunks().forEach(link -> {
									ImageCache.update(player.world, player.world.getChunkFromChunkCoords(Integer.parseInt(link.getResourceDomain()), Integer.parseInt(link.getResourcePath())), "type_changed", "chunk_types");
								});
							}
							break;
						}
						case "owner":{
							Print.chat(sender, "&2If you want to give the chunk to a player or company, use the &7/ck set-for-sale &2command instead.");
							Print.chat(sender, "&2If you want to give the chunk to the district, municipality or state, use the &7/ck set type &2command.");
							Print.chat(sender, "&c&oGiving the chunk to the district, municipality or state does not give you money, and removes you from ownership!");
							break;
						}
						case "whitelist":{
							Print.chat(sender, "&2Please use the &7/ck whitelist &2command instead!");
							break;
						}
						case "help":
						default:{
							Print.chat(sender, "&9Available options:");
							Print.chat(sender, "&7district, price, link, type, owner, whitelist");
							break;
						}
					}
				}
				return;
			}
			case "link":{
				if(isOwner(chunk, player)){
					if(args.length < 2){
						Print.chat(sender, "&9Missing argument.");
						Print.chat(sender, "&7/ck link <x> <z>");
						Print.chat(sender, "&7/ck link reset");
						return;
					}
					if(args[1].equals("reset")){
						chunk.setLink(null, null);
						Print.chat(sender, "&6Chunk unlinked.");
					}
					else{
						try{
							int x = Integer.parseInt(args[1]);
							int z = Integer.parseInt(args[2]);
							chunk.setLink(x, z);
							Print.chat(sender, "&6Chunk linked. ( " + x + " | " + z + " );");
							
						}
						catch(Exception e){
							Print.chat(sender, "&9Error: &c" + e.getMessage());
						}
					}
				}
				return;
			}
			case "whitelist":{
				if(isOwner(chunk, player) || (args.length >= 2 && args[1].equals("view"))){
					if(args.length < 2){
						Print.chat(sender, "&9Missing argument.");
						Print.chat(sender, "&7/ck whitelist add <playername>");
						Print.chat(sender, "&7/ck whitelist add company <id>");
						Print.chat(sender, "&7/ck whitelist rem <playername>");
						Print.chat(sender, "&7/ck whitelist rem company <id>");
						Print.chat(sender, "&7/ck whitelist clear");
						Print.chat(sender, "&7/ck whitelist view");
						return;
					}
					switch(args[1]){
						case "add":
						case "rem":{
							if(args.length < 3){
								Print.chat(sender, "&9Missing Arguemnt.");
								return;
							}
							if(args[2].equals("company")){
								if(args.length < 4){
									Print.chat(sender, "&9Missing Arguemnt.");
									return;
								}
								Print.chat(sender, "&cNot available yet.");
								//TODO companies
								return;
							}
							GameProfile gp = Static.getServer().getPlayerProfileCache().getGameProfileForUsername(args[2]);
							if(gp == null){
								Print.chat(sender, "&7Player not found.");
							}
							else{
								if(args[1].equals("add")){
									chunk.getPlayerWhitelist().add(gp.getId());
									chunk.save();
									Print.chat(sender, "&aPlayer added to whitelist.");
								}
								else if(args[1].equals("rem")){
									chunk.getPlayerWhitelist().remove(gp.getId());
									chunk.save();
									Print.chat(sender, "&aPlayer removed from whitelist.");
								}
								else{
									Print.chat(sender, "&c&oHow could this happen?! Report this issue immediatelly.");
								}
							}
							return;
						}
						case "clear":{
							chunk.getPlayerWhitelist().clear();
							chunk.getCompanyWhitelist().clear();
							chunk.save();
							Print.chat(sender, "Whitelist cleared.");
							return;
						}
						case "view":{
							Print.chat(sender, "&2Whitelist of " + chunk.xCoord() + "x, " + chunk.zCoord() + "z &0:");
							if(chunk.getPlayerWhitelist().size() <= 0){
								Print.chat(sender, "&eNo players are whitelisted.");
							}
							else{
								chunk.getPlayerWhitelist().forEach(uuid -> Print.chat(sender, "&5-> &7" + Static.getPlayerNameByUUID(uuid)));
							}
							if(chunk.getCompanyWhitelist().size() <= 0){
								Print.chat(sender, "&eNo companies are whitelisted.");
							}
							else{
								//TODO companies
								chunk.getCompanyWhitelist().forEach(id -> Print.chat(sender, "&9-> &7Company (" + id + ");"));
							}
							return;
						}
						default:{
							Print.chat(sender, "&cInvalid Argument.");
							return;
						}
					}
				}
				return;
			}
			case "types":{
				Print.chat(sender, "&9Existing chunk types:");
				for(ChunkType type : ChunkType.values()){
					Print.chat(sender, "&2-> &3" + type.name().toLowerCase());
				}
				return;
			}
			default:{
				Print.chat(sender, "//TODO");
				return;
			}
		}
	}

	private boolean isPermitted(Chunk chunk, EntityPlayer player){
		if(chunk.getLink() != null){
			int[] link = chunk.getLink();
			Print.chat(player, "&7Chunk is linked to a chunk at &2" + link[0] + "x&7, &2" + link[1] + "z&7.");
			Print.chat(player, "&7Please make changes to that chunk, they will be copied to this one.");
			Print.chat(player, "&7Alternatively unlink this chunk.");
			return false;
		}
		if(isAdmin(player)){
			Print.chat(player, "&7&oAdmin bypass.");
			return true;
		}
		boolean result = false;
		UUID uuid = player.getGameProfile().getId();
		boolean isco = chunk.getOwner().equals(uuid.toString());
		boolean ismn = chunk.getDistrict().getManager() != null && chunk.getDistrict().getManager().equals(uuid);
		boolean ismy = chunk.getDistrict().getMunicipality().getMayor() != null && chunk.getDistrict().getMunicipality().getMayor().equals(uuid);
		boolean isst = chunk.getDistrict().getMunicipality().getState().getCouncil().contains(uuid) || (chunk.getDistrict().getMunicipality().getState().getLeader() != null && chunk.getDistrict().getMunicipality().getState().getLeader().equals(uuid));
		boolean iscm = false;//TODO companies
		Print.debug(isco, ismn, ismy, isst, iscm);
		switch(chunk.getType()){
			case COMPANY: result = iscm || isst; break;
			case DISTRICT: result = ismn || ismy || isst; break;
			case MUNICIPAL: result = ismy || isst; break;
			case NORMAL: result = ismn || ismy || isst; break;
			case PRIVATE: result = isco || ismy || isst; break;
			case PUBLIC: result = ismn || ismy || isst; break;
			case STATEOWNED: result = isst; break;
			default: result = false; break;
		}
		if(!result){
			Print.chat(player, "&7No Permission.");
		}
		return result;
	}
	
	private boolean isOwner(Chunk chunk, EntityPlayer player){
		if(chunk.getLink() != null){
			int[] link = chunk.getLink();
			Print.chat(player, "&7Chunk is linked to a chunk at &2" + link[0] + "x&7, &2" + link[1] + "z&7.");
			Print.chat(player, "&7Please make changes to that chunk, they will be copied to this one.");
			Print.chat(player, "&7Alternatively unlink this chunk.");
			return false;
		}
		if(isAdmin(player)){
			Print.chat(player, "&7&oAdmin bypass.");
			return true;
		}
		boolean result = false;
		UUID uuid = player.getGameProfile().getId();
		boolean isco = chunk.getOwner().equals(uuid.toString());
		boolean ismn = chunk.getDistrict().getManager() != null && chunk.getDistrict().getManager().equals(uuid);
		boolean ismy = chunk.getDistrict().getMunicipality().getMayor() != null && chunk.getDistrict().getMunicipality().getMayor().equals(uuid);
		boolean isst = chunk.getDistrict().getMunicipality().getState().getCouncil().contains(uuid) || (chunk.getDistrict().getMunicipality().getState().getLeader() != null && chunk.getDistrict().getMunicipality().getState().getLeader().equals(uuid));
		boolean iscm = false;//TODO companies
		Print.debug(isco, ismn, ismy, isst, iscm);
		switch(chunk.getType()){
			case COMPANY: result = iscm; break;
			case DISTRICT: result = ismn || ismy || isst; break;
			case MUNICIPAL: result = ismy || isst; break;
			case NORMAL: result = ismn || ismy || isst; break;
			case PRIVATE: result = isco || ismy; break;
			case PUBLIC: result = false; break;
			case STATEOWNED: result = isst; break;
			default: result = false;
		}
		if(!result){
			Print.chat(player, "&7No Permission.");
		}
		return result;
	}

	public static final boolean isAdmin(EntityPlayer player){
		return PermManager.getPlayerPerms(player).hasPermission(States.ADMIN_PERM);
	}

}
