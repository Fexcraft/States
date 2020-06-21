package net.fexcraft.mod.states.cmds;

import static net.fexcraft.mod.states.guis.GuiHandler.RULE_EDITOR;
import static net.fexcraft.mod.states.guis.GuiHandler.openGui;

import java.awt.Color;

import org.apache.commons.lang3.math.NumberUtils;

import com.mojang.authlib.GameProfile;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.api.registry.fCommand;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.fsmm.api.Bank;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.data.Chunk;
import net.fexcraft.mod.states.data.District;
import net.fexcraft.mod.states.data.DistrictAttribute;
import net.fexcraft.mod.states.data.DistrictType;
import net.fexcraft.mod.states.data.Municipality;
import net.fexcraft.mod.states.data.MunicipalityType;
import net.fexcraft.mod.states.data.capabilities.PlayerCapability;
import net.fexcraft.mod.states.data.capabilities.StatesCapabilities;
import net.fexcraft.mod.states.guis.Listener;
import net.fexcraft.mod.states.util.StateLogger;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

@fCommand
public class DistrictCmd extends CommandBase {
	
	@Override
	public String getName(){
		return "dis";
	}

	@Override
	public String getUsage(ICommandSender sender){
		return "/dis";
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
			Print.chat(sender, "&7/dis info");
			Print.chat(sender, "&7/dis rules");
			Print.chat(sender, "&7/dis types");
			Print.chat(sender, "&7/dis create");
			Print.chat(sender, "&7/dis attributes");
			Print.chat(sender, "&7/dis set <option> <value>");
			return;
		}
		EntityPlayer player = (EntityPlayer)sender.getCommandSenderEntity();
		PlayerCapability ply = player.getCapability(StatesCapabilities.PLAYER, null);
		if(ply == null){
			Print.chat(sender, "&4Error loading Playerdata.");
			return;
		}
		Chunk chunk = StateUtil.getChunk(player);
		District dis = chunk.getDistrict();
		switch(args[0]){
			case "info":{
				Print.chat(sender, "&e====-====-====-====-====-====&0[&2States&0]");
				Print.chat(sender, "&6Info of district &7" + dis.getName() + " (" + dis.getId() + ")&2:");
				Print.chat(sender, "&9Municipality: &7" + dis.getMunicipality().getName() + " (" + dis.getMunicipality().getId() + ")");
				Print.chat(sender, "&9Manager: &7" + (dis.getHead() == null ? "no one" : Static.getPlayerNameByUUID(dis.getHead())));
				Print.chat(sender, "&9Price: &7" + (dis.getPrice() > 0 ? Config.getWorthAsString(dis.getPrice()) : "not for sale"));
				Print.chat(sender, "&9Type: &7" + dis.getType().name().toLowerCase());
				Print.chat(sender, "&9Color: &7" + dis.getColor());
				Print.chat(sender, "&9Chunk Tax: &7" + (dis.getChunkTax() > 0 ? ggas(dis.getChunkTax()) : "none"));
				Print.chat(sender, "&9Last change: &7" + Time.getAsString(dis.getChanged()));
				Print.chat(sender, "&9Neighbors: &7" + dis.getNeighbors().size());
				dis.getNeighbors().forEach(var -> {
					District district = StateUtil.getDistrict(var);
					Print.chat(sender, "&c-> &9" + district.getName() + " &7(" + district.getId() + ");");
				});
				Print.chat(sender, "&9Chunks: &7" + dis.getClaimedChunks());
				Print.chat(sender, "&7Can Foreigners Settle: " + dis.r_CFS.get());
				Print.chat(sender, "&8Unclaim if Bankrupt: " + dis.r_ONBANKRUPT.get());
				Print.chat(sender, "&2Created by &7" + Static.getPlayerNameByUUID(dis.getCreator()) + "&2 at &8" + Time.getAsString(dis.getCreated()));
				Print.chat(sender, "&6Mailbox: &7" + (dis.getMailbox() == null ? "No mailbox set." : dis.getMailbox().toString()));
				return;
			}
			case "rules":{
				openGui(player, RULE_EDITOR, 1, 0, 0);
				return;
			}
			case "types":{
				Print.chat(sender, "&9Existing district types:");
				for(DistrictType type : DistrictType.values()){
					Print.chat(sender, "&2-> &3 " + type.toDetailedString());
				}
				Print.chat(sender, "&9While the &7#&9 signs in order mean:");
				Print.chat(sender, "&7housing, commerce, industry, cultivation, exploitation");
				return;
			}
			case "attributes":{
				Print.chat(sender, "&9Existing district attributes:");
				for(DistrictAttribute type : DistrictAttribute.values()){
					Print.chat(sender, "&2-> &3 " + type.name().toLowerCase());
				}
				Print.chat(sender, "&9Each district type may have a different set of attributes.");
				return;
			}
			case "set":{
				if(args.length < 2){
					Print.chat(sender, "&7/dis set type <type>");
					Print.chat(sender, "&7/dis set name <new name>");
					Print.chat(sender, "&7/dis set price <price/0>");
					Print.chat(sender, "&7/dis set manager <playername>");
					Print.chat(sender, "&7/dis set color <hex>");
					Print.chat(sender, "&7/dis set can-foreigners-settle <true/false>");
					Print.chat(sender, "&7/dis set icon <url>");
					Print.chat(sender, "&7/dis set chunk-tax <amount/reset>");
					Print.chat(sender, "&7/dis set unclaim-if-bankrupt <true/false>");
					Print.chat(sender, "&7/dis set ruleset <new name>");
					return;
				}
				switch(args[1]){
					case "type":{
						if(dis.isAuthorized(dis.r_SET_TYPE.id, ply.getUUID()).isTrue() || StateUtil.bypass(player)){
							if(args.length < 3){
								Print.chat(sender, "&9Missing Argument!");
								Print.chat(sender, "&2You can see available types using &7/dis types&2!");
								break;
							}
							try{
								DistrictType type = DistrictType.valueOf(args[2].toUpperCase());
								if(type != null){
									dis.setType(type);
									dis.setChanged(Time.getDate());
									dis.save();
								}
								Print.chat(sender, "&9District type set to &7" + type.name().toLowerCase() + "&9!");
								Print.log(StateLogger.player(player) + " changed type of " + StateLogger.district(dis) + " to " + dis.getType() + ".");
							}
							catch(Exception e){
								Print.chat(sender, "&9Error: &7" + e.getMessage());
							}
						}
						else{
							Print.chat(sender, "&cNo permission.");
						}
						break;
					}
					case "name":{
						if(dis.isAuthorized(dis.r_SET_NAME.id, ply.getUUID()).isTrue() || StateUtil.bypass(player)){
							if(args.length < 3){
								Print.chat(sender, "&9Missing Arguments!");
								break;
							}
							String str = args[2];
							if(args.length > 3){
								for(int i = 3; i < args.length; i++){
									str += " " + args[i];
								}
							}
							if(str.replace(" ", "").length() < 3){
								Print.chat(sender, "&cName is too short!");
								break;
							}
							dis.setName(str);
							dis.setChanged(Time.getDate());
							dis.save();
							Print.chat(sender, "&6Name set to: &7" + dis.getName());
							Print.log(StateLogger.player(player) + " changed name of " + StateLogger.district(dis) + " to " + dis.getName() + ".");
						}
						else{
							Print.chat(sender, "&cNo permission.");
						}
						break;
					}
					case "ruleset":{
						if(dis.isAuthorized(dis.r_SET_RULESET.id, ply.getUUID()).isTrue() || StateUtil.bypass(player)){
							if(args.length < 3){
								Print.chat(sender, "&9Missing Arguments!");
								break;
							}
							String str = args[2];
							if(args.length > 3){
								for(int i = 3; i < args.length; i++){
									str += " " + args[i];
								}
							}
							if(str.replace(" ", "").length() < 3){
								Print.chat(sender, "&cName is too short!");
								break;
							}
							dis.setRulesetTitle(str);
							dis.setChanged(Time.getDate());
							dis.save();
							Print.chat(sender, "&6Ruleset Name set to: &7" + dis.getName());
							Print.log(StateLogger.player(player) + " changed ruleset name of " + StateLogger.district(dis) + " to " + dis.getName() + ".");
						}
						else{
							Print.chat(sender, "&cNo permission.");
						}
						break;
					}
					case "price":{
						if(dis.isAuthorized(dis.r_SET_PRICE.id, ply.getUUID()).isTrue() || StateUtil.bypass(player)){
							if(args.length < 3){
								Print.chat(sender, "&9Missing Argument!");
								Print.chat(sender, "&7Setting the price to \"0\" makes the district not buyable.");
								break;
							}
							try{
								Long price = Long.parseLong(args[2]);
								if(price < 0){ price = 0l; }
								dis.setPrice(price);
								dis.setChanged(Time.getDate());
								dis.save();
								Print.chat(sender, "&2Price set to: &7" + Config.getWorthAsString(dis.getPrice()));
								Print.log(StateLogger.player(player) + " changed price of " + StateLogger.district(dis) + " to " + dis.getPrice() + ".");
							}
							catch(Exception e){
								Print.chat(sender, "&cError: &7" + e.getMessage());
							}
						}
						else{
							Print.chat(sender, "&cNo permission.");
						}
						break;
					}
					case "manager":{
						if(dis.isAuthorized(dis.r_SET_MANAGER.id, ply.getUUID()).isTrue() || StateUtil.bypass(player)){
							if(args.length < 3){
								Print.chat(sender, "&9Missing Argument!");
								break;
							}
							GameProfile gp = Static.getServer().getPlayerProfileCache().getGameProfileForUsername(args[2]);
							if(gp == null || gp.getId() == null){
								Print.chat(sender, "&cPlayer not found in Cache.");
								break;
							}
							dis.setHead(gp.getId());
							dis.setChanged(Time.getDate());
							dis.save();
							Print.chat(sender, "&2Set &7" + gp.getName() + "&2 to new District Manager!");
							Print.log(StateLogger.player(player) + " changed manager of " + StateLogger.district(dis) + " to " + StateLogger.player(gp) + ".");
						}
						else{
							Print.chat(sender, "&cNo permission.");
						}
						break;
					}
					case "color":{
						if(dis.isAuthorized(dis.r_SET_COLOR.id, ply.getUUID()).isTrue() || StateUtil.bypass(player)){
							if(args.length < 3){
								Print.chat(sender, "&9Missing Argument!");
								break;
							}
							try{
								String str = args[2];
								if(str.replace("#", "").length() != 6){
									Print.chat(sender, "&cInvalid HEX String.");
									break;
								}
								str = str.startsWith("#") ? str : "#" + str;
								Color color = Color.decode(str);
								dis.setColor(str);
								dis.setChanged(Time.getDate());
								dis.save();
								Print.chat(sender, "&6Color set to &7" + str + "&6! (" + color.getRed() + ", " + color.getGreen() + ", " + color.getBlue() + ");");
								Print.log(StateLogger.player(player) + " changed color of " + StateLogger.district(dis) + " to " + dis.getColor() + ".");
							}
							catch(Exception e){
								Print.chat(sender, "&2Error: &7" + e.getMessage());
							}
						}
						else{
							Print.chat(sender, "&cNo permission.");
						}
						break;
					}
					case "can-foreigners-settle":{
						if(dis.isAuthorized(dis.r_CFS.id, ply.getUUID()).isTrue() || StateUtil.bypass(player)){
							if(args.length < 3){
								Print.chat(sender, "&9Missing Argument!");
								break;
							}
							dis.r_CFS.set(Boolean.parseBoolean(args[2]));
							dis.setChanged(Time.getDate());
							dis.save();
							Print.chat(sender, "&2FCS: &7" + dis.r_CFS.get());

							Print.log(StateLogger.player(player) + " changed 'can-foreigners-settle' of " + StateLogger.district(dis) + " to " + dis.r_CFS.get() + ".");
						}
						else{
							Print.chat(sender, "&cNo permission.");
						}
						break;
					}
					default:{
						Print.chat(sender, "&9Invalid Argument.");
						break;
					}
					case "icon":{
						if(dis.isAuthorized(dis.r_SET_ICON.id, ply.getUUID()).isTrue() || StateUtil.bypass(player)){
							if(args.length < 3){
								Print.chat(sender, "&9Missing Argument!");
								break;
							}
							try{
								dis.setIcon(args[2]);
								dis.setChanged(Time.getDate());
								dis.save();
								Print.chat(sender, "&6Icon set to &7" + args[2] + "&6!");
								Print.log(StateLogger.player(player) + " changed icon of " + StateLogger.district(dis) + " to " + dis.getIcon() + ".");
							}
							catch(Exception e){
								Print.chat(sender, "&2Error: &7" + e.getMessage());
							}
						}
						else{
							Print.chat(sender, "&cNo permission.");
						}
						break;
					}
					case "chunk-tax":{
						if(dis.isAuthorized(dis.r_SET_CHUNKTAX.id, ply.getUUID()).isTrue() || StateUtil.bypass(player)){
							if(args[2].equals("reset") || args[2].equals("disable")){
								dis.setChunkTax(0); dis.save();
								Print.chat(sender, "&9District's Chunk Tax was reset!");
							}
							else if(NumberUtils.isCreatable(args[2])){
								dis.setChunkTax(Long.parseLong(args[2])); dis.save();
								Print.chat(sender, "&9District's Chunk Tax was set! (" + ggas(dis.getChunkTax()) + ")");
							}
							else{
								Print.chat(sender, "Not a (valid) number.");
							}
						}
						break;
					}
					case "unclaim-if-brankrupt":{
						if(dis.isAuthorized(dis.r_ONBANKRUPT.id, ply.getUUID()).isTrue() || StateUtil.bypass(player)){
							if(args.length < 3){ Print.chat(sender, "&9Missing Argument!"); break; }
							dis.r_ONBANKRUPT.set(Boolean.parseBoolean(args[2]));
							dis.setChanged(Time.getDate()); dis.save();
							Print.chat(sender, "&2UIB: &7" + dis.r_ONBANKRUPT.get());
							Print.log(StateLogger.player(player) + " changed 'unclaim-if-brankrupt' of " + StateLogger.district(dis) + " to " + dis.r_ONBANKRUPT.get() + ".");
						}
						break;
					}
				}
				return;
			}
			case "create":{
				if(ply.getMunicipality().isAuthorized(ply.getMunicipality().r_CREATE_DISTRICT.id, ply.getUUID()).isTrue() || StateUtil.bypass(player)){
					if(ply.getMunicipality().getDistricts().size() + 1 > MunicipalityType.getType(ply.getMunicipality()).getDistrictLimit()){
						Print.chat(sender, "&aYour Municipality reached the limit of possible Districts.");
						return;
					}
					if(chunk.getDistrict().getId() >= 0){
						Print.chat(sender, "&cThis chunk is already part of a District.");
						return;
					}
					if(!nearbySameMunicipality(chunk, ply.getMunicipality())){
						Print.chat(sender, "No nearby/connected chunks are of the same Municipality.");
						return;
					}
					long price = net.fexcraft.mod.states.util.StConfig.DISTRICT_CREATION_PRICE;
					if(price > ply.getMunicipality().getAccount().getBalance()){
						Print.chat(sender, "&9Not enough money on Municipality Account.");
						return;
					}
					Bank bank = ply.getMunicipality().getBank();
					if(bank.isNull()){
						Print.chat(sender, "&9Your bank couldn't be found.");
						return;
					}
					if(args.length < 2){
						Print.chat(sender, "&9No name for new District Specified.");
						return;
					}
					String name = args[1];
					if(args.length > 2){
						for(int i = 2; i < args.length; i++){
							name += " " + args[i];
						}
					}
					try{
						District newdis = new District(sender.getEntityWorld().getCapability(StatesCapabilities.WORLD, null).getNewDistrictId());
						if(newdis.getDistrictFile().exists() || StateUtil.getDistrict(newdis.getId()).getId() >= 0){
							throw new Exception("Tried to create new District with ID '" + newdis.getId() + "', but savefile already exists.");
						}
						else{
							long halfprice = price / 2;
							if(halfprice == 0 || bank.processAction(Bank.Action.TRANSFER, sender, ply.getMunicipality().getAccount(), halfprice, States.SERVERACCOUNT)){
								bank.processAction(Bank.Action.TRANSFER, null, ply.getMunicipality().getAccount(), halfprice, States.SERVERACCOUNT);
								newdis.setCreator(ply.getUUID());
								newdis.setClaimedChunks(1);
								newdis.setName(name);
								newdis.r_CFS.set(false);
								newdis.setCreated(Time.getDate());
								newdis.setChanged(Time.getDate());
								newdis.setHead(ply.getUUID());
								newdis.setMunicipality(ply.getMunicipality());
								newdis.setPrice(0);
								newdis.setType(DistrictType.WILDERNESS);
								newdis.setIcon(States.DEFAULT_ICON);
								newdis.setColor("#ffffff");
								chunk.setDistrict(newdis);
								newdis.getMunicipality().save();
								newdis.save();
								chunk.save();
								States.DISTRICTS.put(newdis.getId(), newdis);
								StateUtil.announce(server, "&9New District was created!");
								StateUtil.announce(server, "&9Created by " + ply.getFormattedNickname());
								StateUtil.announce(server, "&9Name&0: &7" + newdis.getName());
								Print.log(StateLogger.player(player) + " created " + StateLogger.district(newdis) + " at " + StateLogger.chunk(chunk) + ".");
								return;
							}
						}
					}
					catch(Exception e){
						Print.chat(sender, "Error: " + e.getMessage());
						Print.chat(sender, e);
						Print.debug(e);
						return;
					}
				}
				else{
					Print.chat(sender, "&cNo permission.");
					return;
				}
				return;
			}
			default:{
				Print.chat(sender, "&cInvalid Argument.");
				return;
			}
		}
	}
	
	private String ggas(long tax){
		return ChunkCmd.ggas(tax);
	}

	private boolean nearbySameMunicipality(Chunk ck, Municipality mun){
		Chunk chunk = null;
		for(int[] cor : Listener.coords){
			chunk = StateUtil.getChunk(ck.xCoord() + cor[0], ck.zCoord() + cor[1]);
			if(chunk != null && chunk.getMunicipality().getId() == mun.getId()){
				return true;
			}
		}
		return false;
	}
	
}
