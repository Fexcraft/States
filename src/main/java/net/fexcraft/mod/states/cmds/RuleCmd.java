package net.fexcraft.mod.states.cmds;

import java.util.HashMap;
import java.util.Map;

import net.fexcraft.lib.mc.api.registry.fCommand;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.states.data.Chunk;
import net.fexcraft.mod.states.data.Rule;
import net.fexcraft.mod.states.data.capabilities.PlayerCapability;
import net.fexcraft.mod.states.data.capabilities.StatesCapabilities;
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

	@SuppressWarnings("unused") @Override
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
				Print.chat(sender, "&e====-====-====-====-====-====&4[&bStates&4]");
				//TOOD
				return;
			}
			case "types":{
				Print.chat(sender, "&9Available types:");
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
