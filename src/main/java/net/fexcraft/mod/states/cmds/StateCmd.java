package net.fexcraft.mod.states.cmds;

import java.awt.Color;
import java.util.ArrayList;
import java.util.UUID;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;

import net.fexcraft.mod.fsmm.api.Bank;
import net.fexcraft.mod.fsmm.util.AccountManager;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.lib.api.common.fCommand;
import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.lib.util.common.Static;
import net.fexcraft.mod.lib.util.math.Time;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.api.Mail;
import net.fexcraft.mod.states.api.MailType;
import net.fexcraft.mod.states.api.Municipality;
import net.fexcraft.mod.states.api.State;
import net.fexcraft.mod.states.api.capabilities.PlayerCapability;
import net.fexcraft.mod.states.api.capabilities.StatesCapabilities;
import net.fexcraft.mod.states.api.root.AnnounceLevel;
import net.fexcraft.mod.states.impl.GenericMail;
import net.fexcraft.mod.states.impl.GenericState;
import net.fexcraft.mod.states.util.StateLogger;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

@fCommand
public class StateCmd extends CommandBase {
	
	@Override
	public String getName(){
		return "st";
	}

	@Override
	public String getUsage(ICommandSender sender){
		return "/st";
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
			Print.chat(sender, "&7/st info");
			Print.chat(sender, "&7/st set <option> <value>");
			Print.chat(sender, "&7/st council <args...>");
			Print.chat(sender, "&7/st blacklist <args...>");
			Print.chat(sender, "&7/st mun <option> <args>");
			Print.chat(sender, "&7/st create <name...>");
			Print.chat(sender, "&7/st citizen");
			Print.chat(sender, "&7/st buy");
			return;
		}
		EntityPlayer player = (EntityPlayer)sender.getCommandSenderEntity();
		PlayerCapability ply = player.getCapability(StatesCapabilities.PLAYER, null);
		if(ply == null){
			Print.chat(sender, "&o&4There was an error loading your Playerdata.");
			return;
		}
		Chunk chunk = StateUtil.getChunk(player);
		State state = chunk.getDistrict().getMunicipality().getState();
		switch(args[0]){
			case "info":{
				Print.chat(sender, "&e====-====-====-====-====-====&0[&2States&0]");
				Print.chat(sender, "&6Info of State &7" + state.getName() + " (" + state.getId() + ")&2:");
				Print.chat(sender, "&9Capital: &7" + StateUtil.getMunicipality(state.getCapitalId()).getName() + " (" + state.getCapitalId() + ")");
				Print.chat(sender, "&9Leader: &7" + (state.getLeader() == null ? "no one" : Static.getPlayerNameByUUID(state.getLeader())));
				Print.chat(sender, "&9Price: &7" + (state.getPrice() > 0 ? Config.getWorthAsString(state.getPrice()) : "not for sale"));
				Print.chat(sender, "&6Color: &7" + state.getColor());
				Print.chat(sender, "&8Citizen: &7" + getCitizens(state).size());
				Print.chat(sender, "&9Balance: &7" + Config.getWorthAsString(state.getAccount().getBalance()));
				Print.chat(sender, "&9Last change: &7" + Time.getAsString(state.getChanged()));
				Print.chat(sender, "&9Council Members: &7" + state.getCouncil().size());
				state.getCouncil().forEach(uuid -> {
					Print.chat(sender, "&c-> &9" + Static.getPlayerNameByUUID(uuid));
				});
				Print.chat(sender, "&9Municipalities: &7" + state.getMunicipalities().size());
				state.getMunicipalities().forEach(var -> {
					Municipality municipality = StateUtil.getMunicipality(var);
					Print.chat(sender, "&c-> &9" + municipality.getName() + " &7(" + municipality.getId() + ");");
				});
				Print.chat(sender, "&9Neighbors: &7" + state.getNeighbors().size());
				state.getNeighbors().forEach(var -> {
					State st = StateUtil.getState(var);
					Print.chat(sender, "&c-> &9" + st.getName() + " &7(" + st.getId() + ");");
				});
				Print.chat(sender, "&2Created by &7" + Static.getPlayerNameByUUID(state.getCreator()) + "&2 at &8" + Time.getAsString(state.getCreated()));
				return;
			}
			case "set":{
				if(args.length < 2){
					Print.chat(sender, "&7/st set name <new name>");
					Print.chat(sender, "&7/st set price <price/0>");
					Print.chat(sender, "&7/st set color <hex>");
					Print.chat(sender, "&7/st set capital <municipality id>");
					Print.chat(sender, "&7/st set icon <url>");
					return;
				}
				switch(args[1]){
					case "name":{
						if(hasPerm("state.set.name", player, state)){
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
							state.setName(str);
							state.setChanged(Time.getDate());
							state.save();
							Print.chat(sender, "&6Name set to: &7" + state.getName());
							StateLogger.log(StateLogger.LoggerType.STATE, StateLogger.player(player) + " set the name of " + StateLogger.state(state) + " to " + state.getName());
						}
						else{
							Print.chat(sender, "&cNo permission.");
						}
						break;
					}
					case "price":{
						if(hasPerm("state.set.price", player, state)){
							if(args.length < 3){
								Print.chat(sender, "&9Missing Argument!");
								Print.chat(sender, "&7Setting the price to \"0\" makes the state not buyable.");
								break;
							}
							try{
								Long price = Long.parseLong(args[2]);
								if(price < 0){ price = 0l; }
								state.setPrice(price);
								state.setChanged(Time.getDate());
								state.save();
								Print.chat(sender, "&2Price set to: &7" + Config.getWorthAsString(state.getPrice()));
								StateLogger.log(StateLogger.LoggerType.STATE, StateLogger.player(player) + " set the price of " + StateLogger.state(state) + " to " + state.getPrice());
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
					case "color":{
						if(hasPerm("state.set.color", player, state)){
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
								state.setColor(str);
								state.setChanged(Time.getDate());
								state.save();
								Print.chat(sender, "&6Color set to &7" + str + "&6! (" + color.getRed() + ", " + color.getGreen() + ", " + color.getBlue() + ");");
								StateLogger.log(StateLogger.LoggerType.STATE, StateLogger.player(player) + " set the color of " + StateLogger.state(state) + " to " + state.getColor());
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
					case "capital":{
						if(hasPerm("state.set.capital", player, state)){
							if(args.length < 3){
								Print.chat(sender, "&9Missing Argument!");
								break;
							}
							Municipality mun = StateUtil.getMunicipality(Integer.parseInt(args[2]));
							if(mun.getId() <= 0 || mun.getState().getId() != state.getId()){
								Print.chat(sender, "&cThat Municipality isn't part of our State.");
								break;
							}
							state.setCapitalId(mun.getId());
							state.setChanged(Time.getDate());
							StateUtil.announce(server, AnnounceLevel.STATE_ALL, "&6" + mun.getName() + " &9 is now the new Capital!", state.getId());
							StateLogger.log(StateLogger.LoggerType.STATE, StateLogger.player(player) + " set the capital of " + StateLogger.state(state) + " to " + StateLogger.municipality(mun));
							return;
						}
						else{
							Print.chat(sender, "&cNo permission.");
						}
						break;
					}
					case "icon":{
						if(hasPerm("state.set.icon", player, state)){
							if(args.length < 3){
								Print.chat(sender, "&9Missing Argument!");
								break;
							}
							try{
								state.setIcon(args[2]);
								state.setChanged(Time.getDate());
								state.save();
								Print.chat(sender, "&6Icon set to &7" + args[2] + "&6!");
								StateLogger.log(StateLogger.LoggerType.STATE, StateLogger.player(player) + " set the icon of " + StateLogger.state(state) + " to " + state.getIcon());
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
				}
				return;
			}
			case "council":{
				if(args.length == 1){
                                    Print.chat(sender, "&7/st council vote <playername> (for leader)");
                                    Print.chat(sender, "&7/st council kick <playername>");
                                    Print.chat(sender, "&7/st council invite <playername>");
                                    Print.chat(sender, "&7/st council leave");
                                    return;
				}
				switch(args[1]){
					case "vote":{
						Print.chat(sender, "Not available yet.");
						if(!hasPerm("state.council.vote", player, state)){
							Print.chat(sender, "&4No permission.");
							return;
						}
						break;
					}
					case "kick":{
						if(!hasPerm("state.council.kick", player, state)){
							Print.chat(sender, "&4No permission.");
							return;
						}
						if(args.length < 3){
							Print.chat(sender, "&9Missing Argument.");
							return;
						}
						GameProfile gp = Static.getServer().getPlayerProfileCache().getGameProfileForUsername(args[2]);
						if(gp == null){
							Print.chat(sender, "&eGameProfile not found.");
							return;
						}
						if(!state.getCouncil().contains(gp.getId())){
							Print.chat(sender, "Player isn't part of the council.");
							return;
						}
						state.getCouncil().remove(gp.getId());
						state.save();
						StateUtil.announce(server, AnnounceLevel.MUNICIPALITY, gp.getName() + " &9was removed from the State Council!", state.getId());
						StateLogger.log(StateLogger.LoggerType.STATE, StateLogger.player(player) + " removed " + StateLogger.player(gp) + " from the council of " + StateLogger.state(state) + ".");
						break;
					}
					case "leave":{
						if(state.getCouncil().size() < 2){
							Print.chat(sender, "&9You cannot leave while being the last council member.");
							return;
						}
						state.getCouncil().remove(ply.getUUID());
						state.save();
						StateUtil.announce(server, AnnounceLevel.MUNICIPALITY, ply.getFormattedNickname(sender) + " &9left the State Council!", state.getId());
						StateLogger.log(StateLogger.LoggerType.STATE, StateLogger.player(player) + " left the council of " + StateLogger.state(state) + ".");
					}
					case "invite":{
						if(!hasPerm("state.council.invite", player, state)){
							Print.chat(sender, "&4No permission.");
							return;
						}
						if(args.length < 3){
							Print.chat(sender, "&7/mun council invite <playername> <optional:message>");
							return;
						}
						GameProfile gp = server.getPlayerProfileCache().getGameProfileForUsername(args[2]);
						if(gp == null || gp.getId() == null){
							Print.chat(sender, "&cPlayer not found.");
							return;
						}
						if(state.getCouncil().contains(gp.getId())){
							Print.chat(sender, "That player is already a Council member.");
							return;
						}
						String msg = null;
						if(args.length > 3){
							msg = args[3];
							if(args.length >= 4){
								for(int i = 4; i < args.length; i++){
									msg += " " + args[i];
								}
							}
						}
						String invmsg = "You have been invited become a State Countil Member " + state.getName() + " (" + state.getId() + ")!" + (msg == null ? "" : " MSG: " + msg);
						JsonObject obj = new JsonObject();
						obj.addProperty("type", "state_council");
						obj.addProperty("id", state.getId());
						obj.addProperty("from", player.getGameProfile().getId().toString());
						obj.addProperty("at", Time.getDate());
						obj.addProperty("valid", Time.DAY_MS * 5);
						Mail mail = new GenericMail("player", gp.getId().toString(), player.getGameProfile().getId().toString(), invmsg, MailType.INVITE, obj);
						StateUtil.sendMail(mail);
						Print.chat(sender, "&7&oInvite sent! (Will be valid for 5 days.)");
						StateLogger.log(StateLogger.LoggerType.STATE, StateLogger.player(player) + " invited " + StateLogger.player(gp) + " to the council of " + StateLogger.state(state) + ".");
						return;
					}
				}
				return;
			}
			case "blacklist":{
				Print.chat(sender, "Not available yet.");
				return;
			}
			case "mun": case "municipality":{
				if(args.length == 1){
                                    Print.chat(sender, "&7/st mun list");
                                    Print.chat(sender, "&7/st mun invite <municipality id>");
                                    Print.chat(sender, "&7/st mun kick/remove");
                                    return;
				}
				switch(args[1]){
					case "list":{
						Print.chat(sender, "&9Municipalities: &7" + state.getMunicipalities().size());
						state.getMunicipalities().forEach(var -> {
							Municipality municipality = StateUtil.getMunicipality(var);
							Print.chat(sender, "&c-> &9" + municipality.getName() + " &7(" + municipality.getId() + ");");
						});
						return;
					}
					case "invite":{
						if(args.length < 3){
							Print.chat(sender, "Missing Municipality Id.");
							return;
						}
						if(!hasPerm("state.municipality.invite", player, state)){
							Municipality mun = StateUtil.getMunicipality(Integer.parseInt(args[2]));
							if(mun == null || mun.getId() <= 0){
								Print.chat(sender, "&6Municipality not found.");
								return;
							}
							if(mun.getMayor() == null){
								Print.chat(sender, "&7Municipality has no Mayor.");
								return;
							}
							String invmsg = "Your Municipality was invited to join the State of " + state.getName() + " (" + state.getId() + ")!";
							JsonObject obj = new JsonObject();
							obj.addProperty("type", "state_municipality");
							obj.addProperty("id", state.getId());
							obj.addProperty("from", player.getGameProfile().getId().toString());
							obj.addProperty("at", Time.getDate());
							obj.addProperty("valid", Time.DAY_MS * 12);
							Mail mail = new GenericMail("player", mun.getMayor().toString(), player.getGameProfile().getId().toString(), invmsg, MailType.INVITE, obj);
							StateUtil.sendMail(mail);
							Print.chat(sender, "&7&oInvite sent! (Will be valid for 12 days.)");

							StateLogger.log(StateLogger.LoggerType.MUNICIPALITY, StateLogger.player(player) + " invited " + StateLogger.municipality(mun) + " to join the State of " + StateLogger.state(state));
							return;
						}
						else{
							Print.chat(sender, "&6No Permission.");
						}
						return;
					}
					case "kick": case "remove":{
						if(args.length < 3){
							Print.chat(sender, "Missing Municipality Id.");
							return;
						}
						if(!hasPerm("state.municipality.kick", player, state)){
							Municipality mun = StateUtil.getMunicipality(Integer.parseInt(args[2]));
							if(mun == null || mun.getId() <= 0){
								Print.chat(sender, "&6Municipality not found.");
								return;
							}
							mun.setState(StateUtil.getState(-1));
							mun.setChanged(Time.getDate());
							mun.save();
							StateUtil.announce(server, AnnounceLevel.STATE, "Municipality of " + mun.getName() + " was removed from our State!", state.getId());
							StateLogger.log(StateLogger.LoggerType.MUNICIPALITY, StateLogger.player(player) + " kicked " + StateLogger.municipality(mun) + " from the State of " + StateLogger.state(state));
						}
						else{
							Print.chat(sender, "&6No Permission.");
						}
						return;
					}
				}
				return;
			}
			case "create":{
				long price = net.fexcraft.mod.states.util.Config.STATE_CREATION_PRICE;
				if(!hasPerm("state.create", player, chunk)){
					Print.chat(sender, "&cNo permission.");
					return;
				}
				if(ply.getMunicipality().getState().getId() >= 0){
					Print.chat(sender, "&cYour Municipality must leave your current state to create a new one.");
					return;
				}
				if(ply.getMunicipality().getAccount().getBalance() < price){
					Print.chat(sender, "&cMunicipality does not have enough money to create a State.");
					Print.chat(sender, "(needed: " + Config.getWorthAsString(price, false) + "; available: " + Config.getWorthAsString(ply.getMunicipality().getAccount().getBalance(), false) + ")");
					return;
				}
				if(args.length < 2){
					Print.chat(sender, "&9No name for new State Specified.");
					return;
				}
				Bank bank = AccountManager.INSTANCE.getBank(ply.getMunicipality().getAccount().getBankId());
				if(bank == null){
					Print.chat(sender, "&9Your Municipality Bank couldn't be found.");
					return;
				}
				//TODO permissions check
				try{
					String name = args[1];
					if(args.length > 2){
						for(int i = 2; i < args.length; i++){
							name += " " + args[i];
						}
					}
					GenericState newstate = new GenericState(sender.getEntityWorld().getCapability(StatesCapabilities.WORLD, null).getNewStateId());
					if(newstate.getStateFile().exists() || StateUtil.getState(newstate.getId()).getId() >= 0){
						throw new Exception("Tried to create new State with ID '" + newstate.getId() + "', but savefile already exists.");
					}
					else{
						newstate.setCreator(ply.getUUID());
						newstate.setName(name);
						newstate.setLeader(ply.getUUID());
						newstate.setCapitalId(ply.getMunicipality().getId());
						newstate.setPrice(0);
						newstate.getCouncil().add(ply.getUUID());
						newstate.setChanged(Time.getDate());
						//
						//Now let's save stuff.
						long halfprice = price / 2;
						if(halfprice == 0 || bank.processTransfer(sender, ply.getMunicipality().getAccount(), halfprice, States.SERVERACCOUNT)){
							bank.processTransfer(null, ply.getMunicipality().getAccount(), halfprice, newstate.getAccount());
							newstate.save(); States.STATES.put(newstate.getId(), newstate);
							ply.getMunicipality().setState(newstate);
							ply.getMunicipality().setChanged(Time.getDate());
							ply.getMunicipality().save();
							StateUtil.announce(server, "&9New State was created!");
							StateUtil.announce(server, "&9Created by " + ply.getFormattedNickname(sender));
							StateUtil.announce(server, "&9Name&0: &7" + newstate.getName());
							StateLogger.log(StateLogger.LoggerType.STATE, StateLogger.player(player) + " created " + StateLogger.state(newstate) + ".");
						}
					}
				}
				catch(Exception e){
					Print.chat(sender, "Error: " + e.getMessage());
					Print.chat(sender, e);
					Print.debug(e);
				}
				return;
			}
			case "citizen":{
				Print.chat(sender, "&9Citizen: &7" + getCitizens(state).size());
				for(int id : state.getMunicipalities()){
					Municipality mun = StateUtil.getMunicipality(id);
					if(mun != null && mun.getId() >= 0){
						Print.chat(sender, "&6Municipality: &7" + mun.getName() + " &8(" + mun.getId() + ");");
						mun.getCitizen().forEach(uuid -> {
							Print.chat(sender, "&e-> &9" + Static.getPlayerNameByUUID(uuid) + (mun.getCouncil().contains(uuid) ? " &6" + "[CM]" : ""));
						});
					}
				}
				return;
			}
			default:{
				Print.chat(sender, "&cInvalid Argument.");
				return;
			}
		}
	}
	
	private ArrayList<UUID> getCitizens(State state){
		ArrayList<UUID> list = new ArrayList<UUID>();
		for(int id : state.getMunicipalities()){
			Municipality mun = StateUtil.getMunicipality(id);
			if(mun.getId() == -1){ continue; }
			list.addAll(mun.getCitizen());
		}
		return list;
	}

	public static final boolean hasPerm(String perm, EntityPlayer player, Object obj){
		return ChunkCmd.hasPerm(perm, player, obj);
	}
	
}
