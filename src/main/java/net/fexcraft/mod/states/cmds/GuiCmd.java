package net.fexcraft.mod.states.cmds;

import static net.fexcraft.mod.states.guis.GuiHandler.WELCOME;
import static net.fexcraft.mod.states.guis.GuiHandler.openGui;

import java.util.List;

import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.states.util.AliasLoader;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class GuiCmd extends CommandBase {

	@Override
	public String getName(){
		return AliasLoader.getOverride("st-gui");
	}

	@Override
	public String getUsage(ICommandSender sender){
		return "/" + getName() + " <args>";
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
	public List<String> getAliases(){
		return AliasLoader.getAlias("st-gui");
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(sender instanceof EntityPlayer == false){
			Print.chat(sender, "&7Only available Ingame.");
			return;
		}
		openGui(((EntityPlayer)sender), WELCOME, sender.getPosition());
	}

}
