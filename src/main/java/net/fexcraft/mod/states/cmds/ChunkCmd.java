package net.fexcraft.mod.states.cmds;

import static net.fexcraft.mod.states.guis.GuiHandler.CLAIM_MAP;
import static net.fexcraft.mod.states.guis.GuiHandler.MANAGER_CHUNK;
import static net.fexcraft.mod.states.guis.GuiHandler.openGui;

import java.util.UUID;

import org.apache.commons.lang3.math.NumberUtils;

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
import net.fexcraft.mod.states.util.StConfig;
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
		if(sender.getCommandSenderEntity() instanceof EntityPlayer == false){
			Print.chat(sender, "&7Only available Ingame.");
			return;
		}
		EntityPlayer player = (EntityPlayer)sender.getCommandSenderEntity();
		if(args.length == 0){
			Print.chat(sender, "&7/ck claim <args>");
			Print.chat(sender, "&7/ck reclaim <args>");
			Print.chat(sender, "&7/ck tempclaim");
			Print.chat(sender, "&7/ck info");
			Print.chat(sender, "&7/ck map");
			Print.chat(sender, "&7/ck buy");
			//Print.chat(sender, "&7/ck link <args>");
			Print.chat(sender, "&7/ck force-load <true/false>");
			Print.chat(sender, "&7/ck queue");
			Print.chat(sender, "&7/ck types");
			if(StateUtil.isAdmin(player)){
				Print.chat(sender, "&cAdmin CMDs");
				Print.chat(sender, "&a/ck unclaim");
				Print.chat(sender, "&a/ck update <option:range>");
				Print.chat(sender, "&a/ck set-district <id>");
			}
			return;
		}
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
				openGui(player, MANAGER_CHUNK, ManagerContainer.Mode.CKINFO.ordinal(), chunk.xCoord(), chunk.zCoord());
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
				Print.chat(sender, "&9Current Config allows for &3" + StConfig.MAP_UPDATES_PER_SECOND + "&9 map updates per second.");
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
						chunk.setPrice(StConfig.DEFAULT_CHUNK_PRICE);
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
								ck.setPrice(StConfig.DEFAULT_CHUNK_PRICE);
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
			case "set-district":{
				if(isPermitted(chunk, player)){
					if(args.length < 2){
						Print.chat(sender, "&9Missing argument.");
						Print.chat(sender, "&7/ck set-district <id>");
						return;
					}
					if(StateUtil.isAdmin(player)){
						try{
							chunk.setDistrict(StateUtil.getDistrict(Integer.parseInt(args[1])));
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
				return;
			}
			case "force-load":{
				if(!isPermitted(chunk, player)) return;
				if(args.length < 2){
					Print.chat(sender, "&9Missing argument.");
					Print.chat(sender, "&7/ck force-load true");
					Print.chat(sender, "&7/ck force-load false");
					return;
				}
				if(chunk.getMunicipality().r_FORCE_LOAD_CHUNKS.isAuthorized(chunk.getMunicipality(), playerdata.getUUID()).isTrue()){
					if(chunk.getMunicipality().getAccount().getBalance() < StConfig.LOADED_CHUNKS_TAX){
						Print.chat(sender, "Not enough money to pay the tax.");
						return;
					}
					boolean bool = Boolean.parseBoolean(args[1]);
					chunk.getMunicipality().modifyForceloadedChunk(player, chunk.getChunkPos(), bool);
					Print.log(StateLogger.player(player) + " " + (bool ? "enabled" : "disabled") + " chunk force-loading at " + StateLogger.chunk(chunk) + ", in the District of " + StateLogger.district(chunk.getDistrict()) + ", which is in " + StateLogger.municipality(chunk.getMunicipality()) + ".");
					//
					Bank bank = chunk.getMunicipality().getBank();
					bank.processAction(Bank.Action.TRANSFER, Static.getServer(), chunk.getMunicipality().getAccount(), StConfig.LOADED_CHUNKS_TAX, States.SERVERACCOUNT);
					return;
				}
				break;
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
