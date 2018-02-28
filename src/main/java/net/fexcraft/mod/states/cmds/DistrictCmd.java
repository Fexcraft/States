package net.fexcraft.mod.states.cmds;

import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.lib.api.common.fCommand;
import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.lib.util.common.Static;
import net.fexcraft.mod.lib.util.math.Time;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.api.District;
import net.fexcraft.mod.states.api.DistrictAttribute;
import net.fexcraft.mod.states.api.DistrictType;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

@fCommand
public class DistrictCmd extends CommandBase {
	
	@Override
	public String getName(){
		return "dis";
	}

	@Override
	public String getUsage(ICommandSender sender){
		return "/dis";
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
			Print.chat(sender, "&7/dis info");
			Print.chat(sender, "&7/dis types");
			Print.chat(sender, "&7/dis attributes");
			Print.chat(sender, "&7/dis set <option> <value>");
			return;
		}
		EntityPlayer player = (EntityPlayer)sender.getCommandSenderEntity();
		Chunk chunk = StateUtil.getChunk(player);
		District dis = chunk.getDistrict();
		switch(args[0]){
			case "info":{
				Print.chat(sender, "&e====-====-====-====-====-====&0[&2States&0]");
				Print.chat(sender, "&6Info of district &7" + dis.getName() + " (" + dis.getId() + ")&2:");
				Print.chat(sender, "&9Municipality: &7" + dis.getMunicipality().getName() + " (" + dis.getMunicipality().getId() + ")");
				Print.chat(sender, "&9Manager: &7" + (dis.getManager() == null ? "no one" : Static.getPlayerNameByUUID(dis.getManager())));
				Print.chat(sender, "&9Price: &7" + (dis.getPrice() > 0 ? Config.getWorthAsString(dis.getPrice()) : "not for sale"));
				Print.chat(sender, "&9Type: &7" + dis.getType().name().toLowerCase());
				Print.chat(sender, "&9Color: &7" + dis.getColor());
				Print.chat(sender, "&9Last change: &7" + Time.getAsString(dis.getChanged()));
				Print.chat(sender, "&9Neighbors: &7" + dis.getNeighbors().size());
				dis.getNeighbors().forEach(var -> {
					District district = StateUtil.getDistrict(var);
					Print.chat(sender, "&c-> &9" + district.getName() + " &7(" + district.getId() + ");");
				});
				Print.chat(sender, "&2Created by &7" + Static.getPlayerNameByUUID(dis.getCreator()) + "&2 at &8" + Time.getAsString(dis.getCreated()));
				return;
			}
			case "types":{
				Print.chat(sender, "&9Existing district types:");
				for(DistrictType type : DistrictType.values()){
					Print.chat(sender, "&2-> &3 " + type.toDetailedString());
				}
				Print.chat(sender, "&9While the &7#&9 signs in order mean:");
				Print.chat(sender, "&7housing, commerce, industry, cultivation, exploitation");
				return;
			}
			case "attributes":{
				Print.chat(sender, "&9Existing district attributes:");
				for(DistrictAttribute type : DistrictAttribute.values()){
					Print.chat(sender, "&2-> &3 " + type.name().toLowerCase());
				}
				Print.chat(sender, "&9Each district type may have a different set of attributes.");
				return;
			}
			case "set":{
				
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
