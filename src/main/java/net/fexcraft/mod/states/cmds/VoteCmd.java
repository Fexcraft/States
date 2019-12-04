package net.fexcraft.mod.states.cmds;

import net.fexcraft.lib.mc.api.registry.fCommand;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.states.data.Chunk;
import net.fexcraft.mod.states.data.capabilities.PlayerCapability;
import net.fexcraft.mod.states.data.capabilities.StatesCapabilities;
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
				//TODO
				return;
			}
			default:{
				Print.chat(sender, "&cInvalid Argument.");
				return;
			}
		}
	}
	
}
