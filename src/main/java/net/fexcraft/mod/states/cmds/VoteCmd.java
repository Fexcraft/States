package net.fexcraft.mod.states.cmds;

import net.fexcraft.lib.mc.api.registry.fCommand;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.data.Chunk;
import net.fexcraft.mod.states.data.Vote;
import net.fexcraft.mod.states.data.capabilities.PlayerCapability;
import net.fexcraft.mod.states.data.capabilities.StatesCapabilities;
import net.fexcraft.mod.states.data.root.Ruleable;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

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

	@SuppressWarnings("unused") @Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(args.length == 0){
			Print.chat(sender, "&7/st-vote all");
			Print.chat(sender, "&7/st-vote all <layer>");
			Print.chat(sender, "&7/st-vote status <id>");
			Print.chat(sender, "&7/st-rule layers");
			Print.chat(sender, "&7/st-rule types");
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
					if(vote.target != ruleable)
					Print.chat(sender, "&7- &a" + vote.id + ", &e" + (vote.type.assignment() ? "assignment" : "rule change") + ", &bof " + vote.targetAsString());
				}
				return;
			}
			case "status":{
				if(args.length < 2){
					Print.chat(player, "Please specify a vote ID!");
					return;
				}
				Vote vote = StateUtil.getVote(Integer.parseInt(args[1]));
				if(vote == null){
					Print.chat(sender, "Vote [" + args[1] + "] not found.");
					return;
				}
				Print.chat(sender, "&9Vote Target: &7" + vote.targetAsString());
				vote.summary(sender); return;
			}
			default:{
				Print.chat(sender, "&cInvalid Argument.");
				return;
			}
		}
	}
	
}
