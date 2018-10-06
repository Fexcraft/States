package net.fexcraft.mod.states.cmds;

import java.awt.Color;
import java.util.Collection;

import org.apache.commons.lang3.math.NumberUtils;

import com.mojang.authlib.GameProfile;

import net.fexcraft.mod.fsmm.api.Bank;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.lib.api.common.fCommand;
import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.lib.util.common.Static;
import net.fexcraft.mod.lib.util.math.Time;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.api.ChunkType;
import net.fexcraft.mod.states.api.DistrictType;
import net.fexcraft.mod.states.api.Municipality;
import net.fexcraft.mod.states.api.MunicipalityType;
import net.fexcraft.mod.states.api.State;
import net.fexcraft.mod.states.api.capabilities.PlayerCapability;
import net.fexcraft.mod.states.api.capabilities.StatesCapabilities;
import net.fexcraft.mod.states.api.root.AnnounceLevel;
import net.fexcraft.mod.states.impl.GenericDistrict;
import net.fexcraft.mod.states.impl.GenericMunicipality;
import net.fexcraft.mod.states.util.MailUtil;
import net.fexcraft.mod.states.api.Mailbox.MailType;
import net.fexcraft.mod.states.api.Mailbox.RecipientType;
import net.fexcraft.mod.states.util.StateLogger;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;

@fCommand
public class MunicipalityCmd extends CommandBase {
	
	@Override
	public String getName(){
		return "mun";
	}

	@Override
	public String getUsage(ICommandSender sender){
		return "/mun";
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
			Print.chat(sender, "&7/mun info");
			Print.chat(sender, "&7/mun types");
			Print.chat(sender, "&7/mun set <option> <value>");
			Print.chat(sender, "&7/mun buy");
			Print.chat(sender, "&7/mun council <args...>");
			Print.chat(sender, "&7/mun blacklist <args...>");
			Print.chat(sender, "&7/mun citizen");
			Print.chat(sender, "&7/mun join");
			Print.chat(sender, "&7/mun leave");
			Print.chat(sender, "&7/mun kick <player>");
			Print.chat(sender, "&7/mun invite <player>");
			Print.chat(sender, "&7/mun create <name...>");
			return;
		}
		EntityPlayer player = (EntityPlayer)sender.getCommandSenderEntity();
		PlayerCapability ply = player.getCapability(StatesCapabilities.PLAYER, null);
		if(ply == null){
			Print.chat(sender, "&o&4There was an error loading your Playerdata.");
			return;
		}
		Chunk chunk = StateUtil.getChunk(player);
		Municipality mun = chunk.getDistrict().getMunicipality();
		switch(args[0]){
			case "info":{
				Print.chat(sender, "&e====-====-====-====-====-====&0[&2States&0]");
				Print.chat(sender, "&6Info of Municipality &7" + mun.getName() + " (" + mun.getId() + ")&2:");
				Print.chat(sender, "&9State: &7" + mun.getState().getName() + " (" + mun.getState().getId() + ")");
				Print.chat(sender, "&9Mayor: &7" + (mun.getMayor() == null ? "no one" : Static.getPlayerNameByUUID(mun.getMayor())));
				Print.chat(sender, "&9Price: &7" + (mun.getPrice() > 0 ? Config.getWorthAsString(mun.getPrice()) : "not for sale"));
				Print.chat(sender, "&9Type: &7" + mun.getType().getTitle());
				Print.chat(sender, "&6Color: &7" + mun.getColor());
				Print.chat(sender, "&8Citizen: &7" + mun.getCitizen().size());
				Print.chat(sender, "&9Balance: &7" + Config.getWorthAsString(mun.getAccount().getBalance()));
				Print.chat(sender, "&9Citizen Tax: &7" + (mun.getCitizenTax() > 0 ? ggas(mun.getCitizenTax()) : "none"));
				Print.chat(sender, "&9Last change: &7" + Time.getAsString(mun.getChanged()));
				Print.chat(sender, "&9Council Members: &7" + mun.getCouncil().size());
				mun.getCouncil().forEach(uuid -> {
					Print.chat(sender, "&c-> &9" + Static.getPlayerNameByUUID(uuid));
				});
				Print.chat(sender, "&9Districts: &7" + mun.getDistricts().size());
				Print.chat(sender, "&9Neighbors: &7" + mun.getNeighbors().size());
				mun.getNeighbors().forEach(var -> {
					Municipality municipality = StateUtil.getMunicipality(var);
					Print.chat(sender, "&c-> &9" + municipality.getName() + " &7(" + municipality.getId() + ");");
				});
				Print.chat(sender, "&3Open to join: " + mun.isOpen());
				Print.chat(sender, "&8Kick if Bankrupt: " + mun.kickIfBankrupt());
				Print.chat(sender, "&6Chunks: &7" + mun.getClaimedChunks() + "&8/&9" + MunicipalityType.getChunkLimitFor(mun));
				Print.chat(sender, "&2Created by &7" + Static.getPlayerNameByUUID(mun.getCreator()) + "&2 at &8" + Time.getAsString(mun.getCreated()));
				Collection<?> coll = mun.getForceLoadedChunks();
				if(coll != null && coll.size() > 0){
					Print.chat(sender, "&4Force-Loaded Chunks: " + coll.size());
				}
				Print.chat(sender, "&6Mailbox: &7" + (mun.getMailbox() == null ? "No mailbox set." : mun.getMailbox().toString()));
				return;
			}
			case "types":{
				Print.chat(sender, "&9Existing municipality types:");
				for(MunicipalityType type : MunicipalityType.values()){
					Print.chat(sender, "&2-> &3 " + type.toDetailedString());
				}
				Print.chat(sender, "&9While the numbers mean: &71. required citizen | 2. district limit");
				return;
			}
			case "buy":{
				if(hasPerm("municipality.buy", player, mun)){
					if(ply.getMunicipality().getState().getId() < 0){
						Print.chat(sender, "&7You must be part of a State first!");
						return;
					}
					if(mun.getPrice() <= 0){
						Print.chat(sender, "&eMunicipality isn't for sale.");
						return;
					}
					if(mun.getPrice() > ply.getMunicipality().getState().getAccount().getBalance()){
						Print.chat(sender, "&eNot enought money on State Account.");
						return;
					}
					//
					State playerstate = ply.getMunicipality().getState();
					Bank bank = playerstate.getBank();
					if(bank.isNull()){
						Print.chat(sender, "&cState's Bank not found.");
						return;
					}
					if(bank.processAction(Bank.Action.TRANSFER, sender, playerstate.getAccount(), mun.getPrice(), mun.getState().getAccount())){
						if(mun.isCapital()){
							if(mun.getState().getMunicipalities().size() > 0){
								mun.getState().setCapitalId(-1);
								mun.getState().setChanged(Time.getDate());
								mun.getState().save();
							}
							else{
								StateUtil.announce(server, "&7The state of &6" + mun.getState().getName() + " has been disbanned!");
							}
						}
						mun.setState(playerstate);
						mun.getCouncil().clear();
						mun.setChanged(Time.getDate());
						mun.setMayor(null);
						mun.setPrice(0);
						mun.save();
						mun.getState().save();
						StateUtil.announce(server, AnnounceLevel.MUNICIPALITY_ALL, "&6Municipality has been bought!", ply.getMunicipality().getId());
						StateUtil.announce(server, AnnounceLevel.MUNICIPALITY_ALL, "&9Buyer: " + playerstate.getName() + " (" + playerstate.getId() + ");", ply.getMunicipality().getId());
						StateLogger.log(StateLogger.LoggerType.MUNICIPALITY, StateLogger.player(player) + " bought " + StateLogger.municipality(mun) + ", it is now part of " + StateLogger.state(mun.getState()) + ".");
					}
				}
				else{
					Print.chat(sender, "&6No permission.");
				}
				return;
			}
			case "set":{
				if(args.length < 2){
					Print.chat(sender, "&7/mun set open <true/false>");
					Print.chat(sender, "&7/mun set name <new name>");
					Print.chat(sender, "&7/mun set price <price/0>");
					Print.chat(sender, "&7/mun set mayor <playername>");
					Print.chat(sender, "&7/mun set color <hex>");
					Print.chat(sender, "&7/mun set icon <url>");
					Print.chat(sender, "&7/mun set citizen-tax <amount/reset>");
					Print.chat(sender, "&7/mun set kick-if-bankrupt <true/false>");
					return;
				}
				switch(args[1]){
					case "mayor":{
						if(hasPerm("municipality.set.mayor", player, mun)){
							if(args.length < 3){
								Print.chat(sender, "&9Missing Argument!");
								break;
							}
							GameProfile gp = Static.getServer().getPlayerProfileCache().getGameProfileForUsername(args[2]);
							if(gp == null || gp.getId() == null){
								Print.chat(sender, "&cPlayer not found in Cache.");
								break;
							}
							mun.setMayor(gp.getId());
							mun.setChanged(Time.getDate());
							mun.save();
							Print.chat(sender, "&2Set &7" + gp.getName() + "&2 to new Municipality Mayor!");
							StateLogger.log(StateLogger.LoggerType.DISRICT, StateLogger.player(player) + " changed mayor of " + StateLogger.municipality(mun) + " to " + StateLogger.player(gp) + ".");
						}
						else{
							Print.chat(sender, "&cNo permission.");
						}
						break;
					}
					case "name":{
						if(hasPerm("municipality.set.name", player, mun)){
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
							mun.setName(str);
							mun.setChanged(Time.getDate());
							mun.save();
							Print.chat(sender, "&6Name set to: &7" + mun.getName());
							StateLogger.log(StateLogger.LoggerType.MUNICIPALITY, StateLogger.player(player) + " changed name of " + StateLogger.municipality(mun) + " to " + mun.getName() + ".");
						}
						else{
							Print.chat(sender, "&cNo permission.");
						}
						break;
					}
					case "price":{
						if(hasPerm("municipality.set.price", player, mun)){
							if(args.length < 3){
								Print.chat(sender, "&9Missing Argument!");
								Print.chat(sender, "&7Setting the price to \"0\" makes the municipality not buyable.");
								break;
							}
							try{
								Long price = Long.parseLong(args[2]);
								if(price < 0){ price = 0l; }
								mun.setPrice(price);
								mun.setChanged(Time.getDate());
								mun.save();
								Print.chat(sender, "&2Price set to: &7" + Config.getWorthAsString(mun.getPrice()));
								StateLogger.log(StateLogger.LoggerType.MUNICIPALITY, StateLogger.player(player) + " changed price of " + StateLogger.municipality(mun) + " to " + mun.getPrice() + ".");
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
						if(hasPerm("municipality.set.color", player, mun)){
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
								mun.setColor(str);
								mun.setChanged(Time.getDate());
								mun.save();
								Print.chat(sender, "&6Color set to &7" + str + "&6! (" + color.getRed() + ", " + color.getGreen() + ", " + color.getBlue() + ");");
								StateLogger.log(StateLogger.LoggerType.MUNICIPALITY, StateLogger.player(player) + " changed color of " + StateLogger.municipality(mun) + " to " + mun.getColor() + ".");
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
					case "open":{
						if(hasPerm("municipality.set.open", player, mun)){
							if(args.length < 3){
								Print.chat(sender, "&9Missing Argument!");
								break;
							}
							mun.setOpen(Boolean.parseBoolean(args[2]));
							mun.setChanged(Time.getDate());
							mun.save();
							Print.chat(sender, "&2Open: &7" + mun.isOpen());
							StateLogger.log(StateLogger.LoggerType.MUNICIPALITY, StateLogger.player(player) + " set " + StateLogger.municipality(mun) + " to " + (mun.isOpen() ? "OPEN" : "CLOSED") + ".");
						}
						else{
							Print.chat(sender, "&cNo permission.");
						}
						break;
					}
					case "icon":{
						if(hasPerm("municipality.set.icon", player, mun)){
							if(args.length < 3){
								Print.chat(sender, "&9Missing Argument!");
								break;
							}
							try{
								mun.setIcon(args[2]);
								mun.setChanged(Time.getDate());
								mun.save();
								Print.chat(sender, "&6Icon set to &7" + args[2] + "&6!");
								StateLogger.log(StateLogger.LoggerType.MUNICIPALITY, StateLogger.player(player) + " changed icon of " + StateLogger.municipality(mun) + " to " + mun.getIcon() + ".");
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
					case "citizen-tax":{
						if(hasPerm("municipality.set.citizentax", player, mun)){
							if(args[2].equals("reset") || args[2].equals("disable")){
								mun.setCitizenTax(0); mun.save();
								Print.chat(sender, "&9Municipality's Citizen Tax was reset!");
							}
							else if(NumberUtils.isCreatable(args[2])){
								mun.setCitizenTax(Long.parseLong(args[2])); mun.save();
								Print.chat(sender, "&9Municipality's Citizen Tax was set! (" + ggas(mun.getCitizenTax()) + ")");
							}
							else{
								Print.chat(sender, "Not a (valid) number.");
							}
						}
						break;
					}
					case "kick-if-bankrupt":{
						if(hasPerm("municipality.set.kick-if-bankrupt", player, mun)){
							if(args.length < 3){ Print.chat(sender, "&9Missing Argument!"); break; }
							mun.setKickIfBankrupt(Boolean.parseBoolean(args[2]));
							mun.setChanged(Time.getDate()); mun.save();
							Print.chat(sender, "&2KIB: &7" + mun.kickIfBankrupt());
							StateLogger.log(StateLogger.LoggerType.DISRICT, StateLogger.player(player) + " changed 'kick-if-brankrupt' of " + StateLogger.municipality(mun) + " to " + mun.kickIfBankrupt() + ".");
						}
						break;
					}
					default:{
						Print.chat(sender, "&9Invalid Argument.");
						break;
					}
				}
				return;
			}
			case "council":{
				if(args.length < 2){
					Print.chat(sender, "&7/mun council vote <playername> (for mayor)");
					Print.chat(sender, "&7/mun council kick <playername>");
					Print.chat(sender, "&7/mun council invite <playername>");
					Print.chat(sender, "&7/mun council leave");
					return;
				}
				switch(args[1]){
					case "vote":{
						Print.chat(sender, "Not available yet.");
						if(!hasPerm("municipality.council.vote", player, mun)){
							Print.chat(sender, "&4No permission.");
							return;
						}
						break;
					}
					case "kick":{
						if(!hasPerm("municipality.council.kick", player, mun)){
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
						if(!mun.getCouncil().contains(gp.getId())){
							Print.chat(sender, "Player isn't part of the council.");
							return;
						}
						mun.getCouncil().remove(gp.getId());
						mun.save();
						StateUtil.announce(server, AnnounceLevel.MUNICIPALITY, gp.getName() + " &9was removed from the Municipality Council!", mun.getId());
						StateLogger.log(StateLogger.LoggerType.MUNICIPALITY, StateLogger.player(player) + " removed " + StateLogger.player(gp) + " from the council of " + StateLogger.municipality(mun) + ".");
						break;
					}
					case "leave":{
						if(mun.getCouncil().size() < 2){
							Print.chat(sender, "&9You cannot leave while being the last council member.");
							return;
						}
						mun.getCouncil().remove(ply.getUUID());
						mun.save();
						StateUtil.announce(server, AnnounceLevel.MUNICIPALITY, ply.getFormattedNickname() + " &9left the Municipality Council!", mun.getId());
						StateLogger.log(StateLogger.LoggerType.MUNICIPALITY, StateLogger.player(player) + " left of the council of " + StateLogger.municipality(mun) + ".");
					}
					case "invite":{
						if(!hasPerm("municipality.council.invite", player, mun)){
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
						if(mun.getCouncil().contains(gp.getId())){
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
						String invmsg = "You have been invited become a Municipality Countil Member " + mun.getName() + " (" + mun.getId() + ")!" + (msg == null ? "" : " MSG: " + msg);
						NBTTagCompound compound = new NBTTagCompound();
						compound.setString("type", "municipality_council");
						compound.setInteger("id", mun.getId());
						compound.setString("from", player.getGameProfile().getId().toString());
						compound.setLong("at", Time.getDate());
						MailUtil.send(sender, RecipientType.PLAYER, gp.getId().toString(), player.getGameProfile().getId().toString(), invmsg, MailType.INVITE, Time.DAY_MS * 5, compound);
						Print.chat(sender, "&7&oInvite sent! (Will be valid for 5 days.)");
						StateLogger.log(StateLogger.LoggerType.MUNICIPALITY, StateLogger.player(player) + " invited " + StateLogger.player(gp) + " to the council of " + StateLogger.municipality(mun) + ".");
						return;
					}
				}
				return;
			}
			case "blacklist":{
				if(args.length < 2){
					Print.chat(sender, "&7/mun blacklist add <playername/company:id>");
					Print.chat(sender, "&7/mun blacklist remove <playername/company:id>");
					Print.chat(sender, "&7/mun blacklist view");
					return;
				}
				if(args[1].equals("view")){
					Print.chat(sender, "&9Blacklisted Players: &7" + mun.getPlayerBlacklist().size());
					mun.getPlayerBlacklist().forEach(uuid -> {
						Print.chat(sender, "&c-> &9" + Static.getPlayerNameByUUID(uuid));
					});
					Print.chat(sender, "&9Blacklisted Companies: &7" + mun.getCompanyBlacklist().size());
					mun.getCompanyBlacklist().forEach(id -> {
						Print.chat(sender, "&c-> &9" + id);
					});//TODO
					return;
				}
				if(!hasPerm("municipality.blacklist.edit", player, mun)){
					Print.chat(sender, "&cNo permission!");
					return;
				}
				if(args[1].equals("add") || args[1].equals("remove") || args[1].equals("rem")){
					if(args.length < 3){
						Print.chat(sender, "&cMissing Argument.");
						return;
					}
					GameProfile gp = Static.getServer().getPlayerProfileCache().getGameProfileForUsername(args[2]);
					if(gp == null || gp.getId() == null){
						Print.chat(sender, "&aPlayer not found.");
						return;
					}
					if(mun.getCouncil().contains(gp.getId())){
						Print.chat(sender, "&9You can not blacklist council members!");
						Print.chat(sender, "&cKick them from the council first!");
						return;
					}
					if(args[1].equals("add")){
						mun.getPlayerBlacklist().add(gp.getId());
						Print.chat(sender, "&9Player &7" + gp.getName() + "&9 added to blacklist!");

						StateLogger.log(StateLogger.LoggerType.MUNICIPALITY, StateLogger.player(player) + " added " + StateLogger.player(gp) + " to the blacklist of " + StateLogger.municipality(mun) + ".");
						return;
					}
					else{
						mun.getPlayerBlacklist().remove(gp.getId());
						Print.chat(sender, "&9Player &7" + gp.getName() + "&9 removed from blacklist!");
						StateLogger.log(StateLogger.LoggerType.MUNICIPALITY, StateLogger.player(player) + " removed " + StateLogger.player(gp) + " from the blacklist of " + StateLogger.municipality(mun) + ".");
						return;
					}
				}
				else{
					Print.chat(sender, "&9Invalid Argument.");
				}
				return;
			}
			case "citizen":{
				Print.chat(sender, "&9Citizen: &7" + mun.getCitizen().size());
				mun.getCitizen().forEach(uuid -> {
					Print.chat(sender, "&c-> &9" + Static.getPlayerNameByUUID(uuid) + (mun.getCouncil().contains(uuid) ? " &6" + "[CM]" : ""));
				});
				return;
			}
			case "kick":{
				if(!hasPerm("municipality.kick", player, mun)){
					Print.chat(sender, "&cNo permission!");
					return;
				}
				if(args.length < 2){
					Print.chat(sender, "&7/mun kick <playername> <optional:reason>");
					return;
				}
				GameProfile gp = server.getPlayerProfileCache().getGameProfileForUsername(args[1]);
				if(gp == null || gp.getId() == null){
					Print.chat(sender, "&cPlayer not found.");
					return;
				}
				if(mun.getCouncil().contains(gp.getId())){
					Print.chat(sender, "&9You can not kick council members!");
					Print.chat(sender, "&cUse &7/mun council kick &c instead!");
					return;
				}
				if(!mun.getCitizen().contains(gp.getId())){
					Print.chat(sender, "That player isn't a citizen of this Municipality.");
					return;
				}
				String reason = null;
				if(args.length > 2){
					reason = args[2];
					if(args.length >= 3){
						for(int i = 3; i < args.length; i++){
							reason += " " + args[i];
						}
					}
				}
				mun.getCitizen().remove(gp.getId());
				String kickmsg = "You have been kicked from the Municipality (" + mun.getId() + ") for: " + (reason == null ? "No Kick reason given." : reason);
				PlayerCapability playr = StateUtil.getPlayer(gp.getId(), false);
				if(playr != null){ playr.setMunicipality(StateUtil.getMunicipality(-1)); }
				MailUtil.send(sender, RecipientType.PLAYER, gp.getId().toString(), player.getGameProfile().getId().toString(), kickmsg, MailType.SYSTEM, Time.DAY_MS * 64);
				Print.chat(sender, "&7Player &9" + gp.getName() + "&7 kicked from the Municipality!");
				StateLogger.log(StateLogger.LoggerType.MUNICIPALITY, StateLogger.player(player) + " kicked " + StateLogger.player(gp) + " from " + StateLogger.municipality(mun) + ".");
				return;
			}
			case "join":{
				if(!mun.isOpen()){
					Print.chat(sender, "&aYou need an invite to be able to join this Municipality.");
					return;
				}
				if(mun.getPlayerBlacklist().contains(player.getGameProfile().getId())){
					Print.chat(sender, "&eYou are banned from this Municipality.");
					return;
				}
				//TODO company check
				if(mun.getId() == ply.getMunicipality().getId()){
					Print.chat(sender, "You are already part of this municipality.");
					return;
				}
				if(!ply.canLeave(sender)){
					return;
				}
				MailUtil.send(sender, RecipientType.MUNICIPALITY, ply.getMunicipality().getId(), sender.getName(), player.getGameProfile().getName() + " left the Municipality. At " + Time.getAsString(-1), MailType.SYSTEM);
				StateUtil.announce(server, AnnounceLevel.MUNICIPALITY, "&o" + ply.getFormattedNickname() + " &e&oleft the Municipality!", ply.getMunicipality().getId());
				StateLogger.log(StateLogger.LoggerType.MUNICIPALITY, StateLogger.player(player) + " left " + StateLogger.municipality(ply.getMunicipality()) + ".");
				ply.setMunicipality(mun);
				MailUtil.send(sender, RecipientType.MUNICIPALITY, ply.getMunicipality().getId(), sender.getName(), player.getGameProfile().getName() + " joined the Municipality. At " + Time.getAsString(-1), MailType.SYSTEM);
				StateUtil.announce(server, AnnounceLevel.MUNICIPALITY, "&o" + ply.getFormattedNickname() + " &2&ojoined the Municipality!", ply.getMunicipality().getId());
				StateLogger.log(StateLogger.LoggerType.MUNICIPALITY, StateLogger.player(player) + " joined " + StateLogger.municipality(ply.getMunicipality()) + ".");
				return;
			}
			case "leave":{
				if(ply.getMunicipality().getId() == -1){
					Print.chat(sender, "You aren't part of any Municipality.");
					return;
				}
				if(!ply.canLeave(sender)){
					return;
				}
				MailUtil.send(sender, RecipientType.MUNICIPALITY, ply.getMunicipality().getId(), sender.getName(), player.getGameProfile().getName() + " left the Municipality. At " + Time.getAsString(-1), MailType.SYSTEM);
				StateUtil.announce(server, AnnounceLevel.MUNICIPALITY, "&o" + ply.getFormattedNickname() + " &e&oleft the Municipality!", ply.getMunicipality().getId());
				StateLogger.log(StateLogger.LoggerType.MUNICIPALITY, StateLogger.player(player) + " left " + StateLogger.municipality(mun) + ".");
				ply.setMunicipality(StateUtil.getMunicipality(-1));
				return;
			}
			case "invite":{
				if(!hasPerm("municipality.invite", player, mun)){
					Print.chat(sender, "&cNo permission!");
					return;
				}
				if(args.length < 2){
					Print.chat(sender, "&7/mun invite <playername> <optional:message>");
					return;
				}
				GameProfile gp = server.getPlayerProfileCache().getGameProfileForUsername(args[1]);
				if(gp == null || gp.getId() == null){
					Print.chat(sender, "&cPlayer not found.");
					return;
				}
				if(mun.getCitizen().contains(gp.getId())){
					Print.chat(sender, "That player is already a citizen of this Municipality.");
					return;
				}
				String msg = null;
				if(args.length > 2){
					msg = args[2];
					if(args.length >= 3){
						for(int i = 3; i < args.length; i++){
							msg += " " + args[i];
						}
					}
				}
				String invmsg = "You have been invited to join the Municipality " + mun.getName() + " (" + mun.getId() + ")!" + (msg == null ? "" : " MSG: " + msg);
				NBTTagCompound compound = new NBTTagCompound();
				compound.setString("type", "municipality");
				compound.setInteger("id", mun.getId());
				compound.setString("from", player.getGameProfile().getId().toString());
				compound.setLong("at", Time.getDate());
				MailUtil.send(sender, RecipientType.PLAYER, gp.getId().toString(), player.getGameProfile().getId().toString(), invmsg, MailType.INVITE, Time.DAY_MS * 2, compound);
				Print.chat(sender, "&7&oInvite sent! (Will be valid for 2 days.)");
				StateLogger.log(StateLogger.LoggerType.MUNICIPALITY, StateLogger.player(player) + " invited " + StateLogger.player(gp) + " to join "+ StateLogger.municipality(mun) + ".");
				return;
			}
			case "create":{
				long price = net.fexcraft.mod.states.util.Config.MUNICIPALITY_CREATION_PRICE;
				if(ply.getMunicipality().getId() >= 0){
					Print.chat(sender, "&cYou must leave your current municipality to create a new one.");
					return;
				}
				if(chunk.getDistrict().getId() >= 0){
					Print.chat(sender, "&cThis chunk is already part of a municipality.");
					return;
				}
				if(ply.getAccount().getBalance() < price){
					Print.chat(sender, "&cNot enough money to create a municipality.");
					Print.chat(sender, "(needed: " + Config.getWorthAsString(price, false) + "; available: " + Config.getWorthAsString(ply.getAccount().getBalance(), false) + ")");
					return;
				}
				if(args.length < 2){
					Print.chat(sender, "&9No name for new Municipality Specified.");
					return;
				}
				Bank bank = ply.getBank();
				if(bank.isNull()){
					Print.chat(sender, "&9Your bank couldn't be found.");
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
					GenericMunicipality newmun = new GenericMunicipality(sender.getEntityWorld().getCapability(StatesCapabilities.WORLD, null).getNewMunicipalityId());
					if(newmun.getMunicipalityFile().exists() || StateUtil.getMunicipality(newmun.getId()).getId() >= 0){
						throw new Exception("Tried to create new Municipality with ID '" + newmun.getId() + "', but savefile already exists.");
					}
					else{
						newmun.setCreator(ply.getUUID());
						newmun.setName(name);
						newmun.setMayor(ply.getUUID());
						newmun.setOpen(false);
						newmun.setPrice(0);
						newmun.getCitizen().add(ply.getUUID());
						newmun.setChanged(Time.getDate());
                                                newmun.getCouncil().add(ply.getUUID());
						//
						GenericDistrict newdis = new GenericDistrict(sender.getEntityWorld().getCapability(StatesCapabilities.WORLD, null).getNewDistrictId());
						if(newdis.getDistrictFile().exists() || StateUtil.getDistrict(newdis.getId()).getId() >= 0){
							throw new Exception("Tried to create new District with ID '" + newmun.getId() + "', but savefile already exists.");
						}
						else{
							newdis.setCreator(ply.getUUID());
							newdis.setName("Center");
							newdis.setManager(ply.getUUID());
							newdis.setForeignersSettle(false);
							newdis.setMunicipality(newmun);
							newdis.setPrice(0);
							newdis.setType(DistrictType.VILLAGE);
							newdis.setChanged(Time.getDate());
							//
							//Now let's save stuff.
							long halfprice = price / 2;
							if(halfprice == 0 || bank.processAction(Bank.Action.TRANSFER, sender, ply.getAccount(), halfprice, States.SERVERACCOUNT)){
								bank.processAction(Bank.Action.TRANSFER, null, ply.getAccount(), halfprice, newmun.getAccount());
								newmun.save(); States.MUNICIPALITIES.put(newmun.getId(), newmun);
								newdis.save(); States.DISTRICTS.put(newdis.getId(), newdis);
								chunk.setDistrict(newdis); chunk.save();
								chunk.setType(ChunkType.MUNICIPAL);
								chunk.setChanged(Time.getDate());
								chunk.setPrice(0);
								chunk.setClaimer(ply.getUUID());
								ply.setMunicipality(newmun);
								StateUtil.announce(server, "&9New Municipality and District was created!");
								StateUtil.announce(server, "&9Created by " + ply.getFormattedNickname());
								StateUtil.announce(server, "&9Name&0: &7" + newmun.getName());
								StateLogger.log(StateLogger.LoggerType.MUNICIPALITY, StateLogger.player(player) + " created " + StateLogger.municipality(newmun) + " at " + StateLogger.chunk(chunk) + ".");
								StateLogger.log(StateLogger.LoggerType.DISRICT, StateLogger.player(player) + " created " + StateLogger.district(newdis) + " at " + StateLogger.chunk(chunk) + ".");
								StateLogger.log(StateLogger.LoggerType.DISRICT, StateLogger.player(player) + " created a Municipality at " + StateLogger.chunk(chunk) + ".");
							}
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
			default:{
				Print.chat(sender, "&cInvalid Argument.");
				return;
			}
		}
	}

	private String ggas(long citizenTax){
		return ChunkCmd.ggas(citizenTax);
	}

	public static final boolean hasPerm(String perm, EntityPlayer player, Object obj){
		return ChunkCmd.hasPerm(perm, player, obj);
	}
	
}
