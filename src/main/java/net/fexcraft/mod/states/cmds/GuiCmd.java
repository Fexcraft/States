package net.fexcraft.mod.states.cmds;

import net.fexcraft.mod.lib.api.common.fCommand;
import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.states.States;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

@fCommand
public class GuiCmd extends CommandBase {

	@Override
	public String getName(){
		return "st-gui";
	}

	@Override
	public String getUsage(ICommandSender sender){
		return "/st-gui <args>";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(sender instanceof EntityPlayer == false){
			Print.chat(sender, "&7Only available Ingame.");
			return;
		}
		((EntityPlayer)sender).openGui(States.INSTANCE, 0, sender.getEntityWorld(), sender.getPosition().getX(), sender.getPosition().getY(), sender.getPosition().getZ());
	}

}
