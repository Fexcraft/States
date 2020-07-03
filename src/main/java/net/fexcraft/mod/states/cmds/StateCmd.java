package net.fexcraft.mod.states.cmds;

import static net.fexcraft.mod.states.guis.GuiHandler.MANAGER_STATE;
import static net.fexcraft.mod.states.guis.GuiHandler.RULE_EDITOR;
import static net.fexcraft.mod.states.guis.GuiHandler.openGui;

import java.util.ArrayList;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.api.registry.fCommand;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.fsmm.api.Bank;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.data.Chunk;
import net.fexcraft.mod.states.data.Municipality;
import net.fexcraft.mod.states.data.State;
import net.fexcraft.mod.states.data.Vote;
import net.fexcraft.mod.states.data.Vote.VoteType;
import net.fexcraft.mod.states.data.capabilities.PlayerCapability;
import net.fexcraft.mod.states.data.capabilities.StatesCapabilities;
import net.fexcraft.mod.states.data.root.AnnounceLevel;
import net.fexcraft.mod.states.data.root.Mailbox.MailType;
import net.fexcraft.mod.states.data.root.Mailbox.RecipientType;
import net.fexcraft.mod.states.guis.ManagerContainer;
import net.fexcraft.mod.states.util.MailUtil;
import net.fexcraft.mod.states.util.Perms;
import net.fexcraft.mod.states.util.StateLogger;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
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
			Print.chat(sender, "&7/st rules");
			Print.chat(sender, "&7/st council <args...>");
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
				openGui(player, MANAGER_STATE, ManagerContainer.Mode.INFO.ordinal(), state.getId(), 0);
				return;
			}
			case "rules":{
				openGui(player, RULE_EDITOR, 3, 0, 0);
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
						if(!state.isAuthorized(state.r_VOTE_LEADER.id, ply.getUUID()).isTrue() && !StateUtil.bypass(player)){
							Print.chat(sender, "&4No permission.");
							return;
						}
						if(state.getHead() != null){
							Print.chat(sender, "&aA vote for a new leader can be only started when there is no leader!");
							return;
						}
						if(args.length < 3){
							Print.chat(sender, "&7/st council vote &e>>playername<<"); return;
						}
						GameProfile gp = Static.getServer().getPlayerProfileCache().getGameProfileForUsername(args[2]);
						if(gp == null || gp.getId() == null){
							Print.chat(sender, "&cPlayer not found in Cache.");
							break;
						}
						if(Vote.exists(state, VoteType.ASSIGNMENT, null)){
							Print.chat(sender, "&bThere is already an assignment vote ongoing!");
							return;
						}
						int newid = sender.getEntityWorld().getCapability(StatesCapabilities.WORLD, null).getNewVoteId();
						Vote newvote = new Vote(newid, null, ply.getUUID(), Time.getDate(), Time.getDate() + (Time.DAY_MS * 7),
							state, VoteType.ASSIGNMENT, true, null, null);
						if(newvote.getVoteFile().exists()){
							new Exception("Tried to create new Vote with ID '" + newvote.id + "', but savefile already exists."); return;
						}
						newvote.save(); newvote.vote(sender, ply.getUUID(), gp.getId()); States.VOTES.put(newvote.id, newvote);
						StateUtil.announce(null, AnnounceLevel.STATE_ALL, "A new vote to choose a State Leader started!", 0);
						for(UUID member : state.getCouncil()){
							MailUtil.send(null, RecipientType.PLAYER, member, null, "&7A new vote to choose a Head of State started!\n&7Detailed info via &e/st-vote status " + newvote.id, MailType.SYSTEM);
						}
						return;
					}
					case "kick":{
						if(!state.isAuthorized(state.r_COUNCIL_KICK.id, ply.getUUID()).isTrue()){
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
						Print.log(StateLogger.player(player) + " removed " + StateLogger.player(gp) + " from the council of " + StateLogger.state(state) + ".");
						break;
					}
					case "leave":{
						if(state.getCouncil().size() < 2){
							Print.chat(sender, "&9You cannot leave while being the last council member.");
							return;
						}
						state.getCouncil().remove(ply.getUUID());
						state.save();
						StateUtil.announce(server, AnnounceLevel.MUNICIPALITY, ply.getFormattedNickname() + " &9left the State Council!", state.getId());
						Print.log(StateLogger.player(player) + " left the council of " + StateLogger.state(state) + ".");
					}
					case "invite":{
						if(!state.isAuthorized(state.r_COUNCIL_INVITE.id, ply.getUUID()).isTrue()){
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
						NBTTagCompound compound = new NBTTagCompound();
						compound.setString("type", "state_council");
						compound.setInteger("id", state.getId());
						compound.setString("from", player.getGameProfile().getId().toString());
						compound.setLong("at", Time.getDate());
						MailUtil.send(sender, RecipientType.PLAYER, gp.getId().toString(), player.getGameProfile().getId().toString(), invmsg, MailType.INVITE, Time.DAY_MS * 5, compound);
						Print.chat(sender, "&7&oInvite sent! (Will be valid for 5 days.)");
						Print.log(StateLogger.player(player) + " invited " + StateLogger.player(gp) + " to the council of " + StateLogger.state(state) + ".");
						return;
					}
				}
				return;
			}
			case "create":{
				long price = net.fexcraft.mod.states.util.StConfig.STATE_CREATION_PRICE;
				if(!Perms.CREATE_STATE.has(player)){
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
				Bank bank = ply.getMunicipality().getBank();
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
					State newstate = new State(sender.getEntityWorld().getCapability(StatesCapabilities.WORLD, null).getNewStateId());
					if(newstate.getStateFile().exists() || StateUtil.getState(newstate.getId()).getId() >= 0){
						throw new Exception("Tried to create new State with ID '" + newstate.getId() + "', but savefile already exists.");
					}
					else{
						newstate.setCreator(ply.getUUID());
						newstate.setName(name);
						newstate.setHead(ply.getUUID());
						newstate.setCapitalId(ply.getMunicipality().getId());
						newstate.setPrice(0);
						newstate.getCouncil().add(ply.getUUID());
						newstate.setChanged(Time.getDate());
						//
						//Now let's save stuff.
						long halfprice = price / 2;
						if(halfprice == 0 || bank.processAction(Bank.Action.TRANSFER, sender, ply.getMunicipality().getAccount(), halfprice, States.SERVERACCOUNT)){
							bank.processAction(Bank.Action.TRANSFER, null, ply.getMunicipality().getAccount(), halfprice, newstate.getAccount());
							newstate.save(); States.STATES.put(newstate.getId(), newstate);
							ply.getMunicipality().setState(newstate);
							ply.getMunicipality().setChanged(Time.getDate());
							ply.getMunicipality().save();
							StateUtil.announce(server, "&9New State was created!");
							StateUtil.announce(server, "&9Created by " + ply.getFormattedNickname());
							StateUtil.announce(server, "&9Name&0: &7" + newstate.getName());
							Print.log(StateLogger.player(player) + " created " + StateLogger.state(newstate) + ".");
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
	
}
