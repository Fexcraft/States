package net.fexcraft.mod.states.cmds;

import java.util.HashMap;
import java.util.Map;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.api.registry.fCommand;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.data.Chunk;
import net.fexcraft.mod.states.data.Rule;
import net.fexcraft.mod.states.data.State;
import net.fexcraft.mod.states.data.Vote;
import net.fexcraft.mod.states.data.Vote.VoteType;
import net.fexcraft.mod.states.data.capabilities.PlayerCapability;
import net.fexcraft.mod.states.data.capabilities.StatesCapabilities;
import net.fexcraft.mod.states.data.root.AnnounceLevel;
import net.fexcraft.mod.states.data.root.Initiator;
import net.fexcraft.mod.states.data.root.Ruleable;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

@fCommand
public class RuleCmd extends CommandBase {
	
	@Override
	public String getName(){
		return "st-rule";
	}

	@Override
	public String getUsage(ICommandSender sender){
		return "/st-rule";
	}
	
	@Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender){
        return sender != null;
    }
	
	@Override
    public int getRequiredPermissionLevel(){
        return 0;
    }

	@SuppressWarnings({ "incomplete-switch" }) @Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(args.length == 0){
			Print.chat(sender, "&7/st-rule vote <layer> <rule> rev <type>");
			Print.chat(sender, "&7/st-rule vote <layer> <rule> set <type>");
			Print.chat(sender, "&7/st-rule vote <layer> <rule> value <new value>");
			Print.chat(sender, "&7/st-rule all <layer>");
			Print.chat(sender, "&7/st-rule view <layer> <rule>");
			Print.chat(sender, "&7/st-rule types");
			Print.chat(sender, "&7/st-rule layers");
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
			case "vote":{
				if(args.length < 5){
					Print.chat(sender, "&7/st-rule vote <layer> <rule> <votetype> <value>");
					Print.chat(sender, "&cMissing Arguments."); return;
				}
				Ruleable ruleable = null;
				switch(args[1]){
					case "mun": case "municipality":{
						ruleable = chunk.getMunicipality(); break;
					}
					case "dis": case "district":{
						ruleable = chunk.getDistrict(); break;
					}
					case "st": case "state":{
						ruleable = chunk.getState(); break;
					}
					default:{
						Print.chat(sender, "&cInvalid RuleHolder.");
						Print.chat(sender, "&9Try: &7dis, mun, st&9!");
						return;
					}
				}
				Rule rule = ruleable.getRule(args[2]);
				if(rule == null){
					Print.chat(sender, "&cRule not found."); return;
				}
				VoteType type = args[3].equals("rev") ? VoteType.CHANGE_REVISER : args[3].equals("set") ? VoteType.CHANGE_SETTER : args[3].equals("value") ? VoteType.CHANGE_VALUE : null;
				if(type == null){
					Print.chat(sender, "&cInvalid Vote Type."); return;
				}
				Initiator to = null; boolean value = false; Rule.Result result = Rule.Result.FALSE;
				switch(type){
					case CHANGE_REVISER:
					case CHANGE_SETTER:
						result = ruleable.canRevise(rule.id, ply.getUUID());
						if(result.isFalse()){
							Print.chat(sender, "&cNot Authorized to revise this rule."); return;
						}
						try{
							to = Initiator.valueOf(args[4]);
						}
						catch(Exception e){
							Print.chat(sender, "&cInvalid Initiator Specified.");
							Print.chat(sender, "&9See &7/st-rule types &9for available.");
							return;
						}
						if(type == VoteType.CHANGE_SETTER && !to.isValidAsSetter()){
							Print.chat(sender, "&b'VOTE' Initiator types are not valid as Setter."); return;
						}
						if(ruleable instanceof State && to.isCitizenVote()){
							Print.chat(sender, "&b'CITIZEN' Initiator types are not valid for State level."); return;
						}
						break;
					case CHANGE_VALUE:
						if(!ruleable.isAuthorized(rule.id, ply.getUUID())){
							Print.chat(sender, "&cNot Authorized to set the value of this."); return;
						}
						if(rule.get() == null){
							Print.chat(sender, "&cThis is a value-less rule."); return;
						}
						value = Boolean.parseBoolean(args[4]);
						break;
				}
				Print.chat(sender, "&e====-====-====-====-====-====&4[&bStates&4]");
				if(result.isVote()){
					int newid = sender.getEntityWorld().getCapability(StatesCapabilities.WORLD, null).getNewVoteId();
					boolean council = false;
					switch(type){
						case CHANGE_REVISER:{ council = !rule.reviser.isCitizenVote(); break; }
						case CHANGE_SETTER:{ council = !rule.setter.isCitizenVote(); break; }
						case CHANGE_VALUE:{ council = !rule.setter.isCitizenVote(); break; }
					}
					if(Vote.exists(ruleable, type, rule.id)){
						Print.chat(sender, "&bThere is already a " + type.name() + " vote ongoing for this rule!");
						return;
					}
					Vote newvote = new Vote(newid, rule.id, ply.getUUID(), Time.getDate(), Time.getDate() + Time.DAY_MS + Time.DAY_MS,
						ruleable, type, council, type == VoteType.CHANGE_VALUE ? null : type == VoteType.CHANGE_REVISER, type == VoteType.CHANGE_VALUE ? value : to);
					if(newvote.getVoteFile().exists()){
						new Exception("Tried to create new Vote with ID '" + newvote.id + "', but savefile already exists."); return;
					}
					Print.chat(sender, "&6&oNew Vote to apply your rule change was created.");
					Print.chat(sender, "&9The ID is: &7" + newvote.id);
					newvote.vote(sender, ply.getUUID(), true); newvote.save();
					States.VOTES.put(newvote.id, newvote); String str = "";
					switch(type){
						case CHANGE_REVISER:{ str = "REVISER"; break; }
						case CHANGE_SETTER:{ str = "SETTER";  break; }
						case CHANGE_VALUE:{ str = "VALUE";  break; }
					}
					switch(args[1]){
						case "mun": case "municipality":{
							StateUtil.announce(server, AnnounceLevel.MUNICIPALITY, ply.getFormattedNickname() + "&7 started a vote to revise the rule (mun)&a'" + rule.id +"'&7and set " + str + " to &b" + to.name() + "&7!", chunk.getMunicipality().getId());
							return;
						}
						case "dis": case "district":{
							StateUtil.announce(server, AnnounceLevel.DISTRICT, ply.getFormattedNickname() + "&7 started a vote to revise the rule (dis)&a'" + rule.id +"'&7and set " + str + " to &b" + to.name() + "&7!", chunk.getDistrict().getId());
							return;
						}
						case "st": case "state":{
							StateUtil.announce(server, AnnounceLevel.STATE, ply.getFormattedNickname() + "&7 started a vote to revise the rule (st)&a'" + rule.id +"'&7and set " + str + " to &b" + to.name() + "&7!", chunk.getState().getId());
							return;
						}
					} return;
				}
				else{
					String str = "";
					switch(type){
						case CHANGE_REVISER:{ rule.reviser = to; str = "REVISER"; break; }
						case CHANGE_SETTER:{ rule.setter = to; str = "SETTER";  break; }
						case CHANGE_VALUE:{ rule.set(value); str = "VALUE";  break; }
					}
					Print.chat(sender, "&6&oYour rule change got applied.");
					switch(args[1]){
						case "mun": case "municipality":{
							StateUtil.announce(server, AnnounceLevel.MUNICIPALITY, ply.getFormattedNickname() + "&7 revised the rule (mun)&a'" + rule.id +"'\n&7and set " + str + " to &b" + to.name() + "&7!", 2);
							return;
						}
						case "dis": case "district":{
							StateUtil.announce(server, AnnounceLevel.DISTRICT, ply.getFormattedNickname() + "&7 revised the rule (dis)&a'" + rule.id +"'\n&7and set " + str + " to &b" + to.name() + "&7!", 2);
							return;
						}
						case "st": case "state":{
							StateUtil.announce(server, AnnounceLevel.STATE, ply.getFormattedNickname() + "&7 revised the rule (st)&a'" + rule.id +"'\n&7and set " + str + " to &b" + to.name() + "&7!", 2);
							return;
						}
					}
				}
				return;
			}
			case "types":{
				Print.chat(sender, "&9Available types:");
				Print.chat(sender, "&cNONE&7 - &aliterally no one, be wary of this one");
				Print.chat(sender, "&eCITIZEN_ANY&7 - &aany citizen");
				Print.chat(sender, "&eCITIZEN_VOTE&7 - &aa vote by all citizen or till expiry");
				Print.chat(sender, "&bCOUNCIL_ANY&7 - &aany council member");
				Print.chat(sender, "&bCOUNCIL_VOTE&7 - &aa vote by all council members");
				Print.chat(sender, "&2INCHARGE&7 - &athe head (e.g. mayor in case of a mun)");
				Print.chat(sender, "&6HIGHERINCHARGE&7 - &a(e.g. state leader in case of a mun.)");
				return;
			}
			case "layers":{
				Print.chat(sender, "&9Available layers:");
				Print.chat(sender, "&edis&7 - &adistrict");
				Print.chat(sender, "&bmun&7 - &amunicipality");
				Print.chat(sender, "&2st&7 - &athe state");
				return;
			}
			case "all":{
				if(args.length < 2){
					Print.chat(sender, "&aMissing Argument! Try &7/st-rule layers &aand\n &7/st-rule all <layer> &aafterwards!");return;
				}
				Map<String, Rule> rules = null; String ruleset;
				switch(args[1]){
					case "mun": case "municipality":{
						rules = chunk.getMunicipality().getRules(); ruleset = chunk.getMunicipality().getRulesetTitle(); break;
					}
					case "dis": case "district":{
						rules = chunk.getDistrict().getRules(); ruleset = chunk.getDistrict().getRulesetTitle(); break;
					}
					case "st": case "state":{
						rules = chunk.getState().getRules(); ruleset = chunk.getState().getRulesetTitle(); break;
					}
					default: rules = new HashMap<>(); ruleset = "INVALID LAYER SELECTED"; break;
				}
				Print.chat(sender, "&6Ruleset: &7" + ruleset);
				Print.chat(sender, "&7(in order: rule id, reviser, setter, value)");
				for(Rule rule : rules.values()){
					Print.chat(sender, "&a" + rule.id + " &7- &e" + rule.save());
				}
				return;
			}
			case "view":{
				if(args.length < 3){
					Print.chat(sender, "&aMissing Argument! /st-rule <layer> <rule>");return;
				}
				Rule rule = null;
				switch(args[1]){
					case "mun": case "municipality":{
						rule = chunk.getMunicipality().getRule(args[2]); break;
					}
					case "dis": case "district":{
						rule = chunk.getDistrict().getRule(args[2]); break;
					}
					case "st": case "state":{
						rule = chunk.getState().getRule(args[2]); break;
					}
					default: break;
				}
				if(rule == null){
					Print.chat(sender, "&7&oRule not found.");
				}
				else{
					Print.chat(sender, "&6Rule: &7" + rule.id);
					Print.chat(sender, "&9Reviser/Modifier: " + rule.reviser);
					Print.chat(sender, "&9Setter/Autorized: " + rule.setter);
					Print.chat(sender, "&bValue: " + (rule.get() == null ? "no value" : rule.get()));
				}
				return;
			}
			default:{
				Print.chat(sender, "&cInvalid Argument.");
				return;
			}
		}
	}
	
}
