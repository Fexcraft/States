package net.fexcraft.mod.states.cmds;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.api.registry.fCommand;
import net.fexcraft.lib.mc.utils.Formatter;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.data.Chunk;
import net.fexcraft.mod.states.data.Municipality;
import net.fexcraft.mod.states.data.Vote;
import net.fexcraft.mod.states.data.Vote.VoteType;
import net.fexcraft.mod.states.data.capabilities.PlayerCapability;
import net.fexcraft.mod.states.data.capabilities.StatesCapabilities;
import net.fexcraft.mod.states.data.root.Ruleable;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;

@fCommand
public class VoteCmd extends CommandBase {
	
	@Override
	public String getName(){
		return "st-vote";
	}

	@Override
	public String getUsage(ICommandSender sender){
		return "/st-vote";
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
			Print.chat(sender, "&7/st-vote all");
			Print.chat(sender, "&7/st-vote all <layer>");
			Print.chat(sender, "&7/st-vote status <id>");
			Print.chat(sender, "&7/st-vote vote <id> <yes/true/accept/confirm>");
			Print.chat(sender, "&7/st-vote vote <id> <no/false/deny/cancel>");
			Print.chat(sender, "&7/st-vote vote-for <id> <playername>");
			Print.chat(sender, "&7/st-rule layers");
			Print.chat(sender, "&7/st-rule types");
			Print.chat(sender, "&7/st-vote relevant");
			Print.chat(sender, "&7/st-vote my-votes");
			return;
		}
		EntityPlayer player = (EntityPlayer)sender.getCommandSenderEntity();
		PlayerCapability ply = player.getCapability(StatesCapabilities.PLAYER, null);
		if(ply == null){
			Print.chat(sender, "&o&4There was an error loading your Playerdata.");
			return;
		}
		Chunk chunk = StateUtil.getChunk(player);
		switch(args[0]){
			case "all":{
				if(args.length < 2){
					if(States.VOTES.isEmpty()){
						Print.chat(sender, "&6No Active Votes on this Server.");
						return;
					}
					Print.chat(sender, "&6All current votes:");
					for(Vote vote : States.VOTES.values()){
						Print.chat(sender, "&7- &a" + vote.id + ", &e" + (vote.type.assignment() ? "assignment" : "rule change") + ", &bof " + vote.targetAsString());
					}
					return;
				}
				Ruleable ruleable = null; String of;
				switch(args[1]){
					case "dis": case "district":{
						ruleable = chunk.getDistrict(); of = "&9Dis.: &7" + chunk.getDistrict().getName(); break;
					}
					case "mun": case "municipality":{
						ruleable = chunk.getMunicipality(); of = "&9Mun.: &7" + chunk.getMunicipality().getName(); break;
					}
					case "st": case "state":{
						ruleable = chunk.getState(); of = "&9State: &7" + chunk.getState().getName(); break;
					}
					default: Print.chat(sender, "&cInvalid Layer specified."); return;
				}
				Print.chat(sender, "&6All current votes of\n" + of);
				for(Vote vote : States.VOTES.values()){
					if(vote.holder != ruleable) continue;
					Print.chat(sender, "&7- &a" + vote.id + ", &e" + (vote.type.assignment() ? "assignment" : "rule change") + ", &bof " + vote.targetAsString());
				}
				return;
			}
			case "relevant": case "my-votes":{
				Print.chat(sender, "&7Welcome! &aYou are requisted to\n&aparttake in these votes:");
				for(Vote vote : ply.getRelevantVotes()){
					Print.chat(sender, "&7- &a" + vote.id + ", &e" + (vote.type.assignment() ? "assignment" : "rule change") + ", &bof " + vote.targetAsString());
				}
				Print.chat(sender, "&7Use &a/st-vote status <id> &7to see vote details.");
				return;
			}
			case "status":{
				if(args.length < 2){
					Print.chat(player, "Please specify a vote ID!");
					return;
				}
				Vote vote = StateUtil.getVote(null, Integer.parseInt(args[1]));
				if(vote == null){
					Print.chat(sender, "Vote [" + args[1] + "] not found.");
					return;
				}
				Print.chat(sender, "&9Vote Target: &7" + vote.targetAsString());
				vote.summary(sender);
				boolean canshow = vote.council ? vote.holder.getCouncil().contains(ply.getUUID()) : ((Municipality)vote.holder).getCitizen().contains(ply.getUUID());
				if(canshow){
					if(!vote.votes.containsKey(ply.getUUID().toString())){
						if(!vote.type.assignment()){
							TextComponentString text = new TextComponentString(Formatter.format("&a&l[ACCEPT] "));
							text.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/st-vote vote " + vote.id + " true"));
							TextComponentString text2 = new TextComponentString(Formatter.format(" &c&l[DENY]"));
							text2.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/st-vote vote " + vote.id + " false"));
							sender.sendMessage(text.appendSibling(text2));
						}
						else{
							Print.chat(player, "&7Use &a'/st-vote vote-for " + vote.id + " <playername>' &7to vote!");
						}
					}
					else{
						if(vote.type.assignment()){
							Print.chat(player, "&7You voted for: &a" + Static.getPlayerNameByUUID((UUID)vote.votes.get(ply.getUUID().toString())));
						}
						else{
							Print.chat(player, "&7Your vote: &a" + vote.votes.get(ply.getUUID().toString()));
						}
					}
				}
				return;
			}
			case "vote":{
				if(args.length < 2){
					Print.chat(player, "Please specify a vote ID!");
					return;
				}
				if(args.length < 3){
					Print.chat(player, "Please specify a vote response!");
					return;
				}
				Vote vote = StateUtil.getVote(null, Integer.parseInt(args[1]));
				if(vote == null){
					Print.chat(sender, "Vote [" + args[1] + "] not found.");
					return;
				}
				if(vote.type.assignment()){
					Print.chat(sender, "&7This is not a rule change vote.");
					Print.chat(sender, "&7Use &a/st-vote vote-for " + vote.id + " <playername> &7instead!");
					return;
				}
				String[] agree = { "yes", "true", "accept", "confirm" };
				String[] disagree = { "no", "false", "deny", "cancel" };
				Boolean bool = null;
				String vot = args[2].toLowerCase();
				for(String str : agree) if(vot.equals(str)) bool = true;
				for(String str : disagree) if(vot.equals(str)) bool = false;
				if(bool == null){
					Print.chat(sender, "Invalid Vote Response."); return;
				}
				vote.vote(sender, ply.getUUID(), bool);
				return;
			}
			case "vote-for":{
				if(args.length < 2){
					Print.chat(player, "Please specify a vote ID!");
					return;
				}
				if(args.length < 3){
					Print.chat(player, "Please specify a vote response!");
					return;
				}
				Vote vote = StateUtil.getVote(null, Integer.parseInt(args[1]));
				if(vote == null){
					Print.chat(sender, "Vote [" + args[1] + "] not found.");
					return;
				}
				if(!vote.type.assignment()){
					Print.chat(sender, "&7This is not an assignment vote.");
					Print.chat(sender, "&7Use &a/st-vote vote " + vote.id + " <true/false> &7instead!");
					return;
				}
				GameProfile gp = Static.getServer().getPlayerProfileCache().getGameProfileForUsername(args[2]);
				if(gp == null || gp.getId() == null){
					Print.chat(sender, "&cPlayer not found in Cache.");
					return;
				}
				vote.vote(sender, ply.getUUID(), gp.getId());
				return;
			}
			case "test":{
				if(!Static.dev()){ Print.chat(sender, "Only applicable in a developement workspace."); return; }
				//since this is a dev. call for testing we don't need a duplication check, at least for now.
				int dis = args.length > 1 ? Integer.parseInt(args[1]) : -1;
				Vote newvote = new Vote(sender.getEntityWorld().getCapability(StatesCapabilities.WORLD, null).getNewVoteId(),
					"allow.explosions", ply.getUUID(), Time.getDate(), Time.getDate() + (Time.MIN_MS / 2),
					StateUtil.getDistrict(dis), VoteType.CHANGE_VALUE, true, null, true);
				if(newvote.getVoteFile().exists()){
					new Exception("Tried to create new Vote with ID '" + newvote.id + "', but savefile already exists."); return;
				}
				States.VOTES.put(newvote.id, newvote); newvote.save(); Print.chat(sender, "Test Vote Added! [" + newvote.id + "]"); return;
			}
			default:{
				Print.chat(sender, "&cInvalid Argument.");
				return;
			}
		}
	}
	
}
