package net.fexcraft.mod.states.cmds;

import static net.fexcraft.mod.states.guis.GuiHandler.MANAGER_MUNICIPALITY;
import static net.fexcraft.mod.states.guis.GuiHandler.RULE_EDITOR;
import static net.fexcraft.mod.states.guis.GuiHandler.openGui;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.api.registry.fCommand;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.api.Bank;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.data.*;
import net.fexcraft.mod.states.data.Vote.VoteType;
import net.fexcraft.mod.states.data.capabilities.PlayerCapability;
import net.fexcraft.mod.states.data.capabilities.StatesCapabilities;
import net.fexcraft.mod.states.data.root.AnnounceLevel;
import net.fexcraft.mod.states.data.root.Mailbox.MailType;
import net.fexcraft.mod.states.data.root.Mailbox.RecipientType;
import net.fexcraft.mod.states.guis.ManagerContainer;
import net.fexcraft.mod.states.util.MailUtil;
import net.fexcraft.mod.states.util.Perms;
import net.fexcraft.mod.states.util.StConfig;
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

	@SuppressWarnings("unused") @Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(args.length == 0){
			Print.chat(sender, "&7/mun info");
			Print.chat(sender, "&7/mun rules");
			Print.chat(sender, "&7/mun types");
			Print.chat(sender, "&7/mun buy");
			Print.chat(sender, "&7/mun council <args...>");
			Print.chat(sender, "&7/mun citizen");
			Print.chat(sender, "&7/mun join");
			Print.chat(sender, "&7/mun leave");
			Print.chat(sender, "&7/mun kick <player>");
			Print.chat(sender, "&7/mun invite <player>");
			Print.chat(sender, "&8- &6- &8- - - - - -");
			Print.chat(sender, "&7/mun create <name...>");
			Print.chat(sender, "&7/mun abandon");
			Print.chat(sender, "&7/mun claim");
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
				openGui(player, MANAGER_MUNICIPALITY, ManagerContainer.Mode.INFO.ordinal(), mun.getId(), 0);
				return;
			}
			case "rules":{
				openGui(player, RULE_EDITOR, 2, 0, 0);
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
				if(true){//TODO permission for this will be stored in state ruleset //hasPerm("municipality.buy", player, mun)){
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
						mun.setHead(null);
						mun.setPrice(0);
						mun.save();
						mun.getState().save();
						StateUtil.announce(server, AnnounceLevel.MUNICIPALITY_ALL, "&6Municipality has been bought!", ply.getMunicipality().getId());
						StateUtil.announce(server, AnnounceLevel.MUNICIPALITY_ALL, "&9Buyer: " + playerstate.getName() + " (" + playerstate.getId() + ");", ply.getMunicipality().getId());
						Print.log(StateLogger.player(player) + " bought " + StateLogger.municipality(mun) + ", it is now part of " + StateLogger.state(mun.getState()) + ".");
					}
				}
				else{
					Print.chat(sender, "&6No permission.");
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
						if(!mun.isAuthorized(mun.r_VOTE_MAYOR.id, ply.getUUID()).isTrue() && !StateUtil.bypass(player)){
							Print.chat(sender, "&4No permission.");
							return;
						}
						if(mun.getHead() != null){
							Print.chat(sender, "&aA vote for a new mayor can be only started when there is no mayor!");
							return;
						}
						if(args.length < 3){
							Print.chat(sender, "&7/mun council vote &e>>playername<<"); return;
						}
						GameProfile gp = Static.getServer().getPlayerProfileCache().getGameProfileForUsername(args[2]);
						if(gp == null || gp.getId() == null){
							Print.chat(sender, "&cPlayer not found in Cache.");
							break;
						}
						if(Vote.exists(mun, VoteType.ASSIGNMENT, null)){
							Print.chat(sender, "&bThere is already an assignment vote ongoing!");
							return;
						}
						int newid = sender.getEntityWorld().getCapability(StatesCapabilities.WORLD, null).getNewVoteId();
						Vote newvote = new Vote(newid, null, ply.getUUID(), Time.getDate(), Time.getDate() + (Time.DAY_MS * 7),
							mun, VoteType.ASSIGNMENT, !mun.r_VOTE_MAYOR.setter.isCitizenVote(), null, null);
						if(newvote.getVoteFile().exists()){
							new Exception("Tried to create new Vote with ID '" + newvote.id + "', but savefile already exists."); return;
						}
						newvote.save(); newvote.vote(sender, ply.getUUID(), gp.getId()); States.VOTES.put(newvote.id, newvote);
						StateUtil.announce(null, AnnounceLevel.MUNICIPALITY_ALL, "A new vote to choose a Mayor started!", 0);
						for(UUID member : newvote.council ? mun.getCouncil() : mun.getCitizen()){
							MailUtil.send(null, RecipientType.PLAYER, member, null, "&7A new vote to choose a Mayor started!\n&7Detailed info via &e/st-vote status " + newvote.id, MailType.SYSTEM);
						}
						return;
					}
					case "kick":{
						if(!(mun.isAuthorized(mun.r_COUNCIL_KICK.id, ply.getUUID()).isTrue() || StateUtil.bypass(player))){
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
						Print.log(StateLogger.player(player) + " removed " + StateLogger.player(gp) + " from the council of " + StateLogger.municipality(mun) + ".");
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
						Print.log(StateLogger.player(player) + " left of the council of " + StateLogger.municipality(mun) + ".");
					}
					case "invite":{
						if(!(mun.isAuthorized(mun.r_COUNCIL_INVITE.id, ply.getUUID()).isTrue() || StateUtil.bypass(player))){
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
						String invmsg = "You have been invited become a Municipality Council Member " + mun.getName() + " (" + mun.getId() + ")!" + (msg == null ? "" : " MSG: " + msg);
						NBTTagCompound compound = new NBTTagCompound();
						compound.setString("type", "municipality_council");
						compound.setInteger("id", mun.getId());
						compound.setString("from", player.getGameProfile().getId().toString());
						compound.setLong("at", Time.getDate());
						MailUtil.send(sender, RecipientType.PLAYER, gp.getId().toString(), player.getGameProfile().getId().toString(), invmsg, MailType.INVITE, Time.DAY_MS * 5, compound);
						Print.chat(sender, "&7&oInvite sent! (Will be valid for 5 days.)");
						Print.log(StateLogger.player(player) + " invited " + StateLogger.player(gp) + " to the council of " + StateLogger.municipality(mun) + ".");
						return;
					}
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
				if(!(mun.isAuthorized(mun.r_KICK.id, ply.getUUID()).isTrue() || StateUtil.bypass(player))){
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
				Print.log(StateLogger.player(player) + " kicked " + StateLogger.player(gp) + " from " + StateLogger.municipality(mun) + ".");
				return;
			}
			case "join":{
				if(!mun.r_OPEN.get()){
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
				Print.log(StateLogger.player(player) + " left " + StateLogger.municipality(ply.getMunicipality()) + ".");
				ply.setMunicipality(mun);
				MailUtil.send(sender, RecipientType.MUNICIPALITY, ply.getMunicipality().getId(), sender.getName(), player.getGameProfile().getName() + " joined the Municipality. At " + Time.getAsString(-1), MailType.SYSTEM);
				StateUtil.announce(server, AnnounceLevel.MUNICIPALITY, "&o" + ply.getFormattedNickname() + " &2&ojoined the Municipality!", ply.getMunicipality().getId());
				Print.log(StateLogger.player(player) + " joined " + StateLogger.municipality(ply.getMunicipality()) + ".");
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
				Print.log(StateLogger.player(player) + " left " + StateLogger.municipality(mun) + ".");
				ply.setMunicipality(StateUtil.getMunicipality(-1));
				return;
			}
			case "invite":{
				if(!(mun.isAuthorized(mun.r_INVITE.id, ply.getUUID()).isTrue() || StateUtil.bypass(player))){
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
				Print.log(StateLogger.player(player) + " invited " + StateLogger.player(gp) + " to join "+ StateLogger.municipality(mun) + ".");
				return;
			}
			case "create":{
				long price = StConfig.MUNICIPALITY_CREATION_PRICE;
				if(!Perms.CREATE_MUNICIPALITY.has(player)){
					Print.chat(sender, "&cNo permission.");
					return;
				}
				if(ply.getState().getId() > 0 && !ply.getState().isAuthorized(ply.getState().r_CREATE_MUNICIPALITY.id, ply.getUUID()).isTrue()){
					Print.chat(sender, "&cYour State does not allow you to create a new Municipality.");
					return;
				}
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
					Municipality newmun = new Municipality(sender.getEntityWorld().getCapability(StatesCapabilities.WORLD, null).getNewMunicipalityId());
					if(newmun.getMunicipalityFile().exists() || StateUtil.getMunicipality(newmun.getId()).getId() >= 0){
						throw new Exception("Tried to create new Municipality with ID '" + newmun.getId() + "', but savefile already exists.");
					}
					else{
						newmun.setCreator(ply.getUUID());
						newmun.setName(name);
						newmun.setHead(ply.getUUID());
						newmun.r_OPEN.set(false);
						newmun.setPrice(0);
						newmun.getCitizen().add(ply.getUUID());
						newmun.setChanged(Time.getDate());
                                                newmun.getCouncil().add(ply.getUUID());
						//
						District newdis = new District(sender.getEntityWorld().getCapability(StatesCapabilities.WORLD, null).getNewDistrictId());
						if(newdis.getDistrictFile().exists() || StateUtil.getDistrict(newdis.getId()).getId() >= 0){
							throw new Exception("Tried to create new District with ID '" + newmun.getId() + "', but savefile already exists.");
						}
						else{
							newdis.setCreator(ply.getUUID());
							newdis.setName("Center");
							newdis.setHead(ply.getUUID());
							newdis.r_CFS.set(false);
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
								Print.log(StateLogger.player(player) + " created " + StateLogger.municipality(newmun) + " at " + StateLogger.chunk(chunk) + ".");
								Print.log(StateLogger.player(player) + " created " + StateLogger.district(newdis) + " at " + StateLogger.chunk(chunk) + ".");
								Print.log(StateLogger.player(player) + " created a Municipality at " + StateLogger.chunk(chunk) + ".");
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
			case "abandon":{
				if(mun.getId() < 1){
					Print.chat(player, "&cYou cannot abandon system municipalities.");
					return;
				}
				Rule.Result res = mun.isAuthorized(mun.r_ABANDON.id, ply.getUUID());
				boolean pass = StateUtil.bypass(player) || isLastCitizen(mun, ply);
				if(!res.isFalse() || pass){
					Account munacc = mun.getAccount();
					if(munacc.getBalance() < StConfig.MUNICIPALITY_ABANDONMENT_PRICE){
						Print.chat(player, "&cMunicipality does not have enought money to pay the abandonement server fee.");
					}
					if(mun.isCapital()){
						Print.chat(player, "&cYou cannot abandon the capital!");
						Print.chat(player, "&7Try instead:");
						Print.chat(player, "&3-> setting a new state capital");
						Print.chat(player, "&3-> abandoning the state");
					}
					if(!pass && mun.isAuthorized(mun.r_ABANDON.id, ply.getUUID()).isVote()){
						
					}
					else{
						mun.setAbandoned(player.getGameProfile().getId());
						StateUtil.announce(server, "&9A Municipality became abandoned!");
						StateUtil.announce(server, "&9Name&0: &7" + mun.getName() + " &3(&6" + mun.getId() + "&3)");
						StateUtil.announce(server, "&9By " + ply.getFormattedNickname());
					}
				}
				else{
					Print.chat(player, "&cNo permission to abandon this municipality.");
					Print.chat(player, "&7Conditions: (at least one must apply)");
					Print.chat(player, "&3-> authorized to execute rule 'abandon'");
					Print.chat(player, "&3-> has admin permission / operator");
					Print.chat(player, "&3-> is last citizen in the municipality");
				}
				return;
			}
			case "claim":{
				
				return;
			}
			default:{
				Print.chat(sender, "&cInvalid Argument.");
				return;
			}
		}
	}

	private boolean isLastCitizen(Municipality mun, PlayerCapability ply){
		return mun.getCitizen().size() == 1 && mun.getCitizen().contains(ply.getUUID());
	}
	
}
