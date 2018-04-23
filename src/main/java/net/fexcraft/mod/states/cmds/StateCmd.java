package net.fexcraft.mod.states.cmds;

import net.fexcraft.mod.lib.api.common.fCommand;
import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.api.State;
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
			Print.chat(sender, "&7//TODO");
			return;
		}
		EntityPlayer player = (EntityPlayer)sender.getCommandSenderEntity();
		Chunk chunk = StateUtil.getChunk(player);
		State state = chunk.getDistrict().getMunicipality().getState();
		switch(args[0]){
			case "info":{
				//TODO
				return;
			}
			default:{
				Print.chat(sender, "&cInvalid Argument.");
				return;
			}
		}
	}
	
	private static boolean isAdmin(EntityPlayer player){
		return ChunkCmd.isAdmin(player);
	}
	
}
