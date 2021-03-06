package net.fexcraft.mod.states.cmds;

import static net.fexcraft.mod.states.guis.GuiHandler.CLAIM_MAP;
import static net.fexcraft.mod.states.guis.GuiHandler.MANAGER_CHUNK;
import static net.fexcraft.mod.states.guis.GuiHandler.openGui;
import static net.fexcraft.mod.states.util.StateTranslator.send;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.math.NumberUtils;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.api.Bank;
import net.fexcraft.mod.fsmm.api.Bank.Action;
import net.fexcraft.mod.fsmm.api.FSMMCapabilities;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.data.Chunk;
import net.fexcraft.mod.states.data.ChunkPos;
import net.fexcraft.mod.states.data.ChunkType;
import net.fexcraft.mod.states.data.capabilities.PlayerCapability;
import net.fexcraft.mod.states.data.capabilities.StatesCapabilities;
import net.fexcraft.mod.states.data.root.MunCt;
import net.fexcraft.mod.states.guis.ManagerContainer;
import net.fexcraft.mod.states.util.AliasLoader;
import net.fexcraft.mod.states.util.StConfig;
import net.fexcraft.mod.states.util.StateLogger;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class ChunkCmd extends CommandBase {

	@Override
	public String getName(){
		return AliasLoader.getOverride("ck");
	}

	@Override
	public String getUsage(ICommandSender sender){
		return "/" + getName();
	}
	
	@Override
	public List<String> getAliases(){
		return AliasLoader.getAlias("ck");
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
			boolean admin = StateUtil.isAdmin(player);
			if(admin)
				send(sender, "cmd.chunk.help.normal-cmds");
			send(sender, "cmd.chunk.help.claim");
			send(sender, "cmd.chunk.help.reclaim");
			send(sender, "cmd.chunk.help.tempclaim");
			if(StConfig.ALLOW_CHUNK_UNCLAIM)
				send(sender, "cmd.chunk.help.unclaim");
			send(sender, "cmd.chunk.help.info");
			send(sender, "cmd.chunk.help.map");
			send(sender, "cmd.chunk.help.buy");
			send(sender, "cmd.chunk.help.force-load");
			send(sender, "cmd.chunk.help.types");
			if(admin){
				send(sender, "cmd.chunk.help.admin-cmds");
				if(!StConfig.ALLOW_CHUNK_UNCLAIM)
					send(sender, "cmd.chunk.help.unclaim");
				send(sender, "cmd.chunk.help.set-district");
			}
			return;
		}
		PlayerCapability playerdata = player.getCapability(StatesCapabilities.PLAYER, null);
		Chunk chunk = StateUtil.getChunk(player);
		switch(args[0]){
			case "claim": case "reclaim":{
				if(args.length == 1){
					send(sender, "cmd.chunk.claim.missing_id");
					return;
				}
				if(!NumberUtils.isCreatable(args[1])){
					send(sender, "cmd.chunk.claim.invalid_id");
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
				send(sender, "cmd.chunk.map.legend");
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
			case "unclaim":{
				boolean admin = StateUtil.isAdmin(player);
				if(admin || (StConfig.ALLOW_CHUNK_UNCLAIM && (playerdata.isMayorOf(chunk.getMunicipality()) || playerdata.isCountyManagerOf(chunk.getCounty())))){
					MunCt asmc = admin ? null : chunk.getMunCt();
					int range = args.length > 1 ? Integer.parseInt(args[1]) : 0;
					if(range <= 0){
						if(asmc != null && StConfig.UNCLAIM_CHUNK_PRICE > 0){
							Bank bank = asmc.getAccountHolder().getBank();
							if(asmc.getAccountHolder().getAccount().getBalance() < StConfig.UNCLAIM_CHUNK_PRICE){
								send(sender, "cmd.chunk.unclaim.not_enough_money_for_fee." + asmc.trid());
								return;
							}
							if(!bank.processAction(Action.TRANSFER, player, asmc.getAccountHolder().getAccount(), StConfig.UNCLAIM_CHUNK_PRICE, States.SERVERACCOUNT)){
								return;
							}
						}
						chunk.created.setClaimer(player.getGameProfile().getId());
						chunk.setDistrict(StateUtil.getDistrict(-1));
						chunk.setType(ChunkType.NORMAL);
						chunk.price.reset();
						chunk.save();
						send(sender, "cmd.chunk.unclaim.success.single");
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
								if(asmc != null){
									if(!ck.getMunCt().equals(asmc)) continue;
									if(StConfig.UNCLAIM_CHUNK_PRICE > 0){
										Bank bank = asmc.getAccountHolder().getBank();
										if(asmc.getAccountHolder().getAccount().getBalance() < StConfig.UNCLAIM_CHUNK_PRICE){
											send(sender, "cmd.chunk.unclaim.not_enough_money_for_fee." + asmc.trid());
											break;
										}
										if(!bank.processAction(Action.TRANSFER, player, asmc.getAccountHolder().getAccount(), StConfig.UNCLAIM_CHUNK_PRICE, States.SERVERACCOUNT)){
											break;
										}
									}
								}
								ck.created.setClaimer(player.getGameProfile().getId());
								ck.setDistrict(StateUtil.getDistrict(-1));
								ck.setType(ChunkType.NORMAL);
								ck.price.reset();
								ck.save();
								c++;
							}
						}
						send(sender, "cmd.chunk.unclaim.success.multiple", c);
						Print.log(StateLogger.player(player) + " unclaimed " + c + " chunks, with the center being " + StateLogger.chunk(chunk) + ".");
					}
				}
				return;
			}
			case "buy":{
				if(chunk.getDistrict().getId() < 0){
					send(sender, "cmd.chunk.buy.not_claimed");
					return;
				}
				if(chunk.getType() == ChunkType.PRIVATE && chunk.getOwner().equals(playerdata.getUUIDAsString())){
					send(sender, "cmd.chunk.buy.already_owned");
					return;
				}
				if(args.length >= 2 && args[1].equals("company")){
					//TODO
					return;
				}
				if(chunk.getState().getId() != playerdata.getState().getId() && chunk.getDistrict().r_OCCB.get()){
					send(sender, "cmd.chunk.buy.not_citizen0");
					send(sender, "cmd.chunk.buy.not_citizen1");
					return;
				}
				if(chunk.getDistrict().getMunCt().mun){
					if(chunk.getMunicipality().getPlayerBlacklist().contains(player.getGameProfile().getId())){
						send(sender, "cmd.chunk.buy.banned.mun");
						return;
					}
				}
				if(chunk.getState().getBlacklist().contains(playerdata.getMunicipality().getState().getId())){
					send(sender, "cmd.chunk.buy.banned.state");
					return;
				}
				if(!chunk.price.forSale()){
					send(sender, "cmd.chunk.buy.not_for_sale");
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
						case PUBLIC:
						case NORMAL:{
							receiver = chunk.getMunCt().getAccountHolder().getAccount();
							break;
						}
						case PRIVATE:{
							receiver = player.world.getCapability(FSMMCapabilities.WORLD, null).getAccount("player:" + chunk.getOwner(), true, true);
							break;
						}
						case STATEPUBLIC:
						case STATEOWNED:{
							receiver = chunk.getState().getAccount();
							break;
						}
						case COUNTYOWNED:{
							receiver = chunk.getCounty().getAccount();
							break;
						}
						default:{
							send(sender, "cmd.chunk.buy.invalid_type");
							return;
						}
					}
					Account ac_sender = playerdata.getAccount();
					if(!playerdata.getBank().processAction(Bank.Action.TRANSFER, sender, ac_sender, chunk.price.get(), receiver)){
						return;
					}
					long time = Time.getDate();
					chunk.setOwner(player.getGameProfile().getId().toString());
					chunk.price.reset();
					chunk.setType(ChunkType.PRIVATE);
					chunk.created.update(time);
					chunk.save();
					send(sender, "cmd.chunk.buy.success");
					Print.log(StateLogger.player(player) + " bought the " + StateLogger.chunk(chunk) + "!");
					if(chunk.getLinkedChunks().size() > 0){
						for(int[] ckpos : chunk.getLinkedChunks()){
							Chunk ck = StateUtil.getTempChunk(ckpos);
							ck.setOwner(player.getGameProfile().getId().toString());
							ck.price.reset();
							ck.setType(ChunkType.PRIVATE);
							ck.created.update(time);
							ck.save();
							Print.log(StateLogger.player(player) + " received the " + StateLogger.chunk(ck) + " which was linked to " + StateLogger.chunk(chunk) + "!");
						}
						send(sender, "cmd.chunk.buy.success.linked", chunk.getLinkedChunks().size());
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
			case "types":{
				send(sender, "cmd.chunk.types");
				for(ChunkType type : ChunkType.values()){
					send(sender, "cmd.chunk.types." + type.name().toLowerCase());
				}
				return;
			}
			default:{
				send(sender, "cmd.unknown_argument");
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
		boolean ismn = chunk.getDistrict().manage.getHead() != null && chunk.getDistrict().manage.getHead().equals(uuid);
		boolean ismy = chunk.getMunicipality().manage.getHead() != null && chunk.getMunicipality().manage.getHead().equals(uuid);
		boolean isst = chunk.getState().manage.isInCouncil(uuid) || (chunk.getState().manage.getHead() != null && chunk.getState().manage.getHead().equals(uuid));
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
			case STATEPUBLIC: result = isst; break;
			default: result = false; break;
		}
		if(!result){
			Print.chat(player, "&7No Permission.");
		}
		return result;
	}

}
