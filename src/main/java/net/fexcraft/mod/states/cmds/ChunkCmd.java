package net.fexcraft.mod.states.cmds;

import static net.fexcraft.mod.states.guis.GuiHandler.CLAIM_MAP;
import static net.fexcraft.mod.states.guis.GuiHandler.MANAGER_CHUNK;
import static net.fexcraft.mod.states.guis.GuiHandler.openGui;

import java.util.UUID;

import org.apache.commons.lang3.math.NumberUtils;

import com.mojang.authlib.GameProfile;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.api.registry.fCommand;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.api.Bank;
import net.fexcraft.mod.fsmm.api.FSMMCapabilities;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.data.Chunk;
import net.fexcraft.mod.states.data.ChunkPos;
import net.fexcraft.mod.states.data.ChunkType;
import net.fexcraft.mod.states.data.capabilities.PlayerCapability;
import net.fexcraft.mod.states.data.capabilities.StatesCapabilities;
import net.fexcraft.mod.states.guis.ManagerContainer;
import net.fexcraft.mod.states.util.ImageCache;
import net.fexcraft.mod.states.util.StateLogger;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
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
	
	public static String ggas(long value){
		return Config.getWorthAsString(value);
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(args.length == 0){
			Print.chat(sender, "&7/ck claim <args>");
			Print.chat(sender, "&7/ck reclaim <args>");
			Print.chat(sender, "&7/ck tempclaim");
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
		PlayerCapability playerdata = player.getCapability(StatesCapabilities.PLAYER, null);
		Chunk chunk = StateUtil.getChunk(player);
		switch(args[0]){
			case "claim": case "reclaim":{
				if(args.length == 1){
					Print.chat(sender, "&7/ck claim <district>");
					return;
				}
				if(!NumberUtils.isCreatable(args[1])){
					Print.chat(sender, "&7Please enter the numerical district ID as second argument!");
					return;
				}
				openGui(player, CLAIM_MAP, Integer.parseInt(args[1]), args[0].equals("claim") ? 0 : 1, 0);
				return;
			}
			case "tempclaim":{
				openGui(player, CLAIM_MAP, -2, 0, 0);
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
				//openGui(player, REGION_VIEW, 0, 0, 0);
				return;
			}
			case "info":{
				if(args.length >= 3){
					int x = Integer.parseInt(args[1]);
					int z = Integer.parseInt(args[2]);
					chunk = StateUtil.getTempChunk(x, z);
				}
				openGui(player, MANAGER_CHUNK, chunk.xCoord(), ManagerContainer.Mode.CKINFO.ordinal(), chunk.zCoord());
				return;
			}
			case "update":{
				/*if(hasPerm("chunk.update", player, chunk)){
					int range = args.length > 1 ? Integer.parseInt(args[1]) : 0;
					if(range <= 0){
						ImageCache.update(player.world, player.world.getChunkFromChunkCoords(chunk.xCoord(), chunk.zCoord()));
						Print.chat(sender, "&9Queued for map update.");
						Print.log(Print.loggerType.CHUNK, StateLogger.player(player) + " queued " + StateLogger.chunk(chunk) + " for map update.");
					}
					else{
						if(range > 3 && !hasPerm("admin", player, chunk)){
							Print.chat(sender, "&cNo permission for larger update requests.");
							return;
						}
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
								ImageCache.update(player.world, player.world.getChunkFromChunkCoords(x, z));
							}
						}
						Print.chat(sender, "&2" + c + " &9chunks queued for map update.");
						Print.chat(sender, "&9There is &21 &9map mode activated.");
						Print.log(Print.loggerType.CHUNK, StateLogger.player(player) + " queued " + c + " chunks for map update, with the center being " + StateLogger.chunk(chunk) + ".");
					}
				}
				else{
					Print.chat(sender, "&cNo Permission.");
				}*/
				Print.debug("&bCurrently disabled.");
				return;
			}
			case "queue":{
				Print.chat(sender, "&9There are &2" + ImageCache.getQueue().size() + "&9 chunk map updates queued.");
				Print.chat(sender, "&9Current Config allows for &3" + net.fexcraft.mod.states.util.StConfig.MAP_UPDATES_PER_SECOND + "&9 map updates per second.");
				return;
			}
			case "unclaim":{
				if(StateUtil.isAdmin(player)){
					int range = args.length > 1 ? Integer.parseInt(args[1]) : 0;
					if(range <= 0){
						chunk.setClaimer(player.getGameProfile().getId());
						chunk.setDistrict(StateUtil.getDistrict(-1));
						chunk.setChanged(Time.getDate());
						chunk.setType(ChunkType.NORMAL);
						chunk.setPrice(net.fexcraft.mod.states.util.StConfig.DEFAULT_CHUNK_PRICE);
						chunk.save();
						ImageCache.update(player.world, player.world.getChunk(chunk.xCoord(), chunk.zCoord()));
						Print.chat(sender, "&9Chunk unclaimed and resseted.");
						Print.log(StateLogger.player(player) + " unclaimed " + StateLogger.chunk(chunk) + ".");
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
								ck.setPrice(net.fexcraft.mod.states.util.StConfig.DEFAULT_CHUNK_PRICE);
								ck.save();
								c++;
								ImageCache.update(player.world, player.world.getChunk(x, z));
							}
						}
						Print.chat(sender, "&2" + c + " &9chunks have been resseted.");
						Print.log(StateLogger.player(player) + " unclaimed " + c + " chunks, with the center being " + StateLogger.chunk(chunk) + ".");
					}
				}
				return;
			}
			case "buy":{
				//TODO add check for companies, based on their type
				if(args.length >= 3 && args[1].equals("via-sign")){
					try{
						chunk = StateUtil.getChunk(BlockPos.fromLong(Long.parseLong(args[2])));
					}
					catch(Exception e){
						Print.chat(sender, "&9Error: &7" + e.getMessage());
						return;
					}
					if(chunk == null){
						Print.chat(sender, "Chunk couldn't be found, maybe it isn't loaded?");
						return;
					}
				}
				if(chunk.getDistrict().getId() < 0){
					Print.chat(sender, "Only claimed chunks can be bought.");
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
				/*if(playerdata.getMunicipality().getState().getId() < 0){
					Print.chat(sender, "&7You must be citizen of a State to be able to buy chunks.");
					return;
				}*/
				if(chunk.getDistrict().getMunicipality().getId() != playerdata.getMunicipality().getId() && !chunk.getDistrict().r_CFS.get()){
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
				if(chunk.getPrice() <= 0){
					Print.chat(sender, "&cChunk isn't for sale.");
				}
				else{
					Account receiver = null;
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
							receiver = player.world.getCapability(FSMMCapabilities.WORLD, null).getAccount("player:" + chunk.getOwner(), true, true);
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
					Account ac_sender = playerdata.getAccount();
					if(!playerdata.getBank().processAction(Bank.Action.TRANSFER, sender, ac_sender, chunk.getPrice(), receiver)){
						return;
					}
					long time = Time.getDate();
					chunk.setOwner(player.getGameProfile().getId().toString());
					chunk.setPrice(0);
					chunk.setType(ChunkType.PRIVATE);
					chunk.setChanged(time);
					chunk.save();
					Print.chat(sender, "&aChunk bought!");
					Print.log(StateLogger.player(player) + " bought the " + StateLogger.chunk(chunk) + "!");
					if(chunk.getLinkedChunks().size() > 0){
						for(ResourceLocation ckpos : chunk.getLinkedChunks()){
							Chunk ck = StateUtil.getTempChunk(ckpos);
							ck.setOwner(player.getGameProfile().getId().toString());
							ck.setPrice(0);
							ck.setType(ChunkType.PRIVATE);
							ck.setChanged(time);
							ck.save();
							Print.log(StateLogger.player(player) + " received the " + StateLogger.chunk(ck) + " which was linked to " + StateLogger.chunk(chunk) + "!");
						}
						Print.chat(sender, "&7" + chunk.getLinkedChunks().size() + "&a linked chunks bought!");
					}
				}
				return;
			}
			case "set_for_sale":
			case "set-for-sale":
			case "setforsale":
			case "sell":
			case "sfs":{
				if(isPermitted(chunk, player)){
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
						Print.log(StateLogger.player(player) + " set the price of the " + StateLogger.chunk(chunk) + " to " + chunk.getPrice() + ".");
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
							if(StateUtil.isAdmin(player)){
								try{
									chunk.setDistrict(StateUtil.getDistrict(Integer.parseInt(args[2])));
									Print.chat(sender, "&2District set to: " + chunk.getDistrict().getName() + " (" + chunk.getDistrict().getId() + ");");
									Print.log(StateLogger.player(player) + " set the district of " + StateLogger.chunk(chunk) + " to " + StateLogger.district(chunk.getDistrict()) + ".");
								}
								catch(Exception e){
									Print.chat(sender, "&9Error: &7" + e.getMessage());
								}
							}
							else{
								Print.chat(sender, "&2Try &7/ck reclaim&2!");
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
										String to = type == ChunkType.NORMAL || type == ChunkType.DISTRICT ? "District" : type == ChunkType.MUNICIPAL ? "Municipality" : type == ChunkType.STATEOWNED ? "State" : "ERROR";
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
											Print.log(StateLogger.player(player) + " gave the linked " + StateLogger.chunk(ck) + " to the " + to + ".");
										});
										chunk.save();
										Print.chat(sender, "&9Chunk given to the &2" + to + "&9!");
										Print.log(StateLogger.player(player) + " gave the  " + StateLogger.chunk(chunk) + " to the " + to + ".");
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
											Print.log(StateLogger.player(player) + " set the type of linked " + StateLogger.chunk(ck) + " to PUBLIC.");
										});
										chunk.save();
										Print.chat(sender, "&2Chunk set to &cPUBLIC&2!");
										Print.chat(sender, "&2It is still yours, but anyone can edit blocks.");
										Print.log(StateLogger.player(player) + " set the type of " + StateLogger.chunk(chunk) + " to PUBLIC.");
										break;
									}
									default:{
										Print.chat(sender, "&4Invalid chunk type, this actually shouldn't happen.");
										break;
									}
								}
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
						case "force-loaded":{
							if(args.length < 3){
								Print.chat(sender, "&9Missing argument.");
								Print.chat(sender, "&7/ck set force-loaded true");
								Print.chat(sender, "&7/ck set force-loaded false");
								return;
							}
							if(chunk.getMunicipality().r_FORCE_LOAD_CHUNKS.isAuthorized(chunk.getMunicipality(), playerdata.getUUID()).isTrue()){
								if(chunk.getMunicipality().getAccount().getBalance() < net.fexcraft.mod.states.util.StConfig.LOADED_CHUNKS_TAX){
									Print.chat(sender, "Not enough money to pay the tax.");
									return;
								}
								boolean bool = Boolean.parseBoolean(args[2]);
								chunk.getMunicipality().modifyForceloadedChunk(player, chunk.getChunkPos(), bool);
								Print.log(StateLogger.player(player) + " " + (bool ? "enabled" : "disabled") + " chunk force-loading at " + StateLogger.chunk(chunk) + ", in the District of " + StateLogger.district(chunk.getDistrict()) + ", which is in " + StateLogger.municipality(chunk.getMunicipality()) + ".");
								//
								Bank bank = chunk.getMunicipality().getBank();
								bank.processAction(Bank.Action.TRANSFER, Static.getServer(), chunk.getMunicipality().getAccount(), net.fexcraft.mod.states.util.StConfig.LOADED_CHUNKS_TAX, States.SERVERACCOUNT);
								return;
							}
							break;
						}
						case "custom-tax":{
							if(args.length < 3){
								Print.chat(sender, "&9Missing argument!");
								Print.chat(sender, "&7/ck set custom-tax <amount>");
								Print.chat(sender, "&7/ck set custom-tax reset/disable");
								return;
							}
							if(chunk.getDistrict().isAuthorized(chunk.getDistrict().r_SET_CUSTOM_CHUNKTAX.id, playerdata.getUUID()).isTrue()){
								if(args[2].equals("reset") || args[2].equals("disable")){
									chunk.setCustomTax(0); chunk.save();
									Print.chat(sender, "&9Chunk's Custom Tax was reset!");
								}
								else if(NumberUtils.isCreatable(args[2])){
									chunk.setCustomTax(Long.parseLong(args[2])); chunk.save();
									Print.chat(sender, "&9Chunk's Custom Tax was set! (" + ggas(chunk.getCustomTax()) + ")");
								}
								else{
									Print.chat(sender, "Not a (valid) number.");
								}
							}
							break;
						}
						case "help":
						default:{
							Print.chat(sender, "&9Available options:");
							Print.chat(sender, "&7district, price, link, type, owner, whitelist");
							Print.chat(sender, "&7force-loaded, custom-tax");
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
						chunk.setLink(null);
						Print.chat(sender, "&6Chunk unlinked.");
						Print.log(StateLogger.player(player) + " unliked the " + StateLogger.chunk(chunk) + ".");
					}
					else{
						try{
							int x = Integer.parseInt(args[1]);
							int z = Integer.parseInt(args[2]);
							Chunk ck = StateUtil.getTempChunk(x, z);
							if(!isOwner(ck, player)){
								Print.chat(sender, "&cYou must be the owner of the Linked chunk aswel!");
								return;
							}
							chunk.setLink(new ChunkPos(x, z));
							Print.chat(sender, "&6Chunk linked. ( " + x + " | " + z + " );");
							Print.log(StateLogger.player(player) + " linked " + StateLogger.chunk(chunk) + " to (" + x + ", " + z + ").");
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
									Print.log(StateLogger.player(player) + " added " + StateLogger.player(gp) + " to the whitelist of " + StateLogger.chunk(chunk) + ".");
								}
								else if(args[1].equals("rem")){
									chunk.getPlayerWhitelist().remove(gp.getId());
									chunk.save();
									Print.chat(sender, "&aPlayer removed from whitelist.");
									Print.log(StateLogger.player(player) + " removed " + StateLogger.player(gp) + " from the whitelist of " + StateLogger.chunk(chunk) + ".");
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
							Print.log(StateLogger.player(player) + " cleared the whitelist of " + StateLogger.chunk(chunk) + ".");
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
				Print.chat(sender, "Unknown Argument.");
				return;
			}
		}
	}

	private boolean isPermitted(Chunk chunk, EntityPlayer player){
		if(chunk.getLink() != null){
			ChunkPos link = chunk.getLink();
			Print.chat(player, "&7Chunk is linked to a chunk at &2" + link.x + "x&7, &2" + link.z + "z&7.");
			Print.chat(player, "&7Please make changes to that chunk, they will be copied to this one.");
			Print.chat(player, "&7Alternatively unlink this chunk.");
			return false;
		}
		if(StateUtil.isAdmin(player)){
			Print.chat(player, "&7&oAdmin bypass.");
			return true;
		}
		boolean result = false;
		UUID uuid = player.getGameProfile().getId();
		boolean isco = chunk.getOwner().equals(uuid.toString());
		boolean ismn = chunk.getDistrict().getHead() != null && chunk.getDistrict().getHead().equals(uuid);
		boolean ismy = chunk.getMunicipality().getHead() != null && chunk.getMunicipality().getHead().equals(uuid);
		boolean isst = chunk.getState().getCouncil().contains(uuid) || (chunk.getState().getHead() != null && chunk.getState().getHead().equals(uuid));
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
			ChunkPos link = chunk.getLink();
			Print.chat(player, "&7Chunk is linked to a chunk at &2" + link.x + "x&7, &2" + link.z + "z&7.");
			Print.chat(player, "&7Please make changes to that chunk, they will be copied to this one.");
			Print.chat(player, "&7Alternatively unlink this chunk.");
			return false;
		}
		if(StateUtil.isAdmin(player)){
			Print.chat(player, "&7&oAdmin bypass.");
			return true;
		}
		boolean result = false;
		UUID uuid = player.getGameProfile().getId();
		boolean isco = chunk.getOwner().equals(uuid.toString());
		boolean ismn = chunk.getDistrict().getHead() != null && chunk.getDistrict().getHead().equals(uuid);
		boolean ismy = chunk.getMunicipality().getHead() != null && chunk.getMunicipality().getHead().equals(uuid);
		boolean isst = chunk.getState().getCouncil().contains(uuid) || (chunk.getState().getHead() != null && chunk.getState().getHead().equals(uuid));
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

}
