package net.fexcraft.mod.states.cmds;

import static net.fexcraft.mod.states.guis.GuiHandler.WELCOME;
import static net.fexcraft.mod.states.guis.GuiHandler.openGui;

import java.util.ArrayList;
import java.util.List;

import net.fexcraft.lib.mc.api.registry.fCommand;
import net.fexcraft.lib.mc.utils.Print;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

@fCommand
public class GuiCmd extends CommandBase {
	
	private static final ArrayList<String> aliases = new ArrayList<>();
	static {
		aliases.add("/states");
		aliases.add("stgui");
		aliases.add("stui");
	}

	@Override
	public String getName(){
		return "st-gui";
	}

	@Override
	public String getUsage(ICommandSender sender){
		return "/st-gui <args>";
	}
	
	@Override
	public List<String> getAliases(){
		return aliases;
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
