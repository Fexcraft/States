package net.fexcraft.mod.states.cmds;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.states.data.capabilities.PlayerCapability;
import net.fexcraft.mod.states.data.capabilities.StatesCapabilities;
import net.fexcraft.mod.states.util.AliasLoader;
import net.fexcraft.mod.states.util.Perms;
import net.fexcraft.mod.states.util.StConfig;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class NickCmd extends CommandBase {

	@Override
	public String getName(){
		return AliasLoader.getOverride("st-nick");
	}

	@Override
	public String getUsage(ICommandSender sender){
		return "/" + getName();
	}
	
	@Override
	public List<String> getAliases(){
		return AliasLoader.getAlias("st-nick");
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
			Print.chat(sender, "&7/st-nick name <nickname>");
			Print.chat(sender, "&7/st-nick color <code>");
			Print.chat(sender, "&7/st-nick reset");
			Print.chat(sender, "&8 - - - - - - -");
			Print.chat(sender, "&7/st-nick setname <player> <nickname>");
			Print.chat(sender, "&7/st-nick setcolor <player> <code>");
			Print.chat(sender, "&7/st-nick reset <player>");
			return;
		}
		if(sender.getCommandSenderEntity() instanceof EntityPlayer == false){
			Print.chat(sender, "&7Only available Ingame.");
			return;
		}
		EntityPlayer player = (EntityPlayer)sender.getCommandSenderEntity();
		PlayerCapability cap = player.getCapability(StatesCapabilities.PLAYER, null);
		if(args.length < 2 && !args[0].equals("reset")){
			Print.chat(sender, "Missing Arguments.");
			return;
		}
		switch(args[0]){
			case "name":{
				if(!Perms.NICKNAME_CHANGE_SELF.has(player)){
					Print.chat(sender, "&eNo Permission.");
					return;
				}
				String name = args[1];
				for(int i = 2; i < args.length; i++){
					name += " " + args[i];
				}
				if(name.length() > StConfig.NICKNAME_LENGTH){
					Print.chat(sender, "Nickname is too long.");
				}
				else{
					cap.setRawNickname(name);
					Print.chat(sender, "&7Nickname updated " + cap.getFormattedNickname() + "&7!");
				}
				return;
			}
			case "color":{
				if(!Perms.NICKNAME_CHANGE_SELF.has(player)){
					Print.chat(sender, "&eNo Permission.");
					return;
				}
				cap.setNicknameColor(Integer.parseInt(args[1], StringUtils.indexOfAny(args[1], new String[]{"a", "b", "c", "d", "e", "f"}) >= 0 ? 16 : 10));
				Print.chat(sender, "&7Nickname color updated " + cap.getFormattedNickname() + "&7!");
				return;
			}
			case "reset":{
				if(args.length == 1){
					if(!Perms.NICKNAME_CHANGE_SELF.has(player)){
						Print.chat(sender, "&eNo Permission.");
						return;
					}
					cap.setRawNickname(null); cap.setNicknameColor(2);
					Print.chat(sender, "&7Nickname reset " + cap.getFormattedNickname() + "&7!");
				}
				else{
					if(!Perms.NICKNAME_CHANGE_OTHERS.has(player)){
						Print.chat(sender, "&cNo Permission.");
						return;
					}
					PlayerCapability ocap = getOtherPlayer(server, cap, args[1]);
					if(ocap != null){
						ocap.setRawNickname(null); ocap.setNicknameColor(2);
						Print.chat(ocap, "&7Your Nickname was resetted by " + cap.getFormattedNickname());
						Print.chat(cap, "&7Nickname was reset! >> " + ocap.getFormattedNickname());
					}
				}
				return;
			}
			case "setname":{
				if(!Perms.NICKNAME_CHANGE_OTHERS.has(player)){
					Print.chat(sender, "&eNo Permission.");
					return;
				}
				PlayerCapability ocap = getOtherPlayer(server, cap, args[1]);
				if(ocap != null){
					String name = args[2];
					for(int i = 3; i < args.length; i++){
						name += " " + args[i];
					}
					if(name.length() > StConfig.NICKNAME_LENGTH){
						Print.chat(sender, "Nickname is too long.");
					}
					else{
						cap.setRawNickname(name);
						Print.chat(ocap, "&7Your Nickname was changed by " + cap.getFormattedNickname());
						Print.chat(cap, "&7Nickname changed ! >> " + ocap.getFormattedNickname());
					}
				}
				return;
			}
			case "setcolor":{
				if(!Perms.NICKNAME_CHANGE_OTHERS.has(player)){
					Print.chat(sender, "&eNo Permission.");
					return;
				}
				PlayerCapability ocap = getOtherPlayer(server, cap, args[1]);
				if(ocap != null){
					ocap.setNicknameColor(Integer.parseInt(args[2], StringUtils.indexOfAny(args[2], new String[]{"a", "b", "c", "d", "e", "f"}) >= 0 ? 16 : 10));
					Print.chat(ocap, "&7Your Nickname color was changed by " + cap.getFormattedNickname());
					Print.chat(cap, "&7Nickname color changed! >> " + ocap.getFormattedNickname());
				}
				return;
			}
			default:{
				Print.chat(sender, "Invalid Argument.");
				return;
			}
		}
	}

	private PlayerCapability  getOtherPlayer(MinecraftServer server, PlayerCapability cap, String string){
		EntityPlayer othrplyr = server.getPlayerList().getPlayerByUsername(string);
		if(othrplyr == null){
			Print.chat(cap, "&9Player not found.");
		}
		else{
			PlayerCapability ocap = othrplyr.getCapability(StatesCapabilities.PLAYER, null);
			if(ocap == null){
				Print.chat(cap, "Wasn't able to get data of the selected player.");
			}
			else return ocap;
		}
		return null;
	}
	
}
