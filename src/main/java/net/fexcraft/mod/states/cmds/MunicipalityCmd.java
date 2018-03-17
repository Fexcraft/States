package net.fexcraft.mod.states.cmds;

import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.lib.api.common.fCommand;
import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.lib.util.common.Static;
import net.fexcraft.mod.lib.util.math.Time;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.api.Municipality;
import net.fexcraft.mod.states.api.MunicipalityType;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

@fCommand
public class MunicipalityCmd extends CommandBase {
	
	@Override
	public String getName(){
		return "mun";
	}

	@Override
	public String getUsage(ICommandSender sender){
		return "/mun";
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
			Print.chat(sender, "&7/mun info");
			Print.chat(sender, "&7/mun types");
			Print.chat(sender, "&7/mun set <option> <value>");
			Print.chat(sender, "&7/mun kick <player>");
			Print.chat(sender, "&7/mun invite <player>");
			Print.chat(sender, "&7/mun create <name...>");
			return;
		}
		EntityPlayer player = (EntityPlayer)sender.getCommandSenderEntity();
		Chunk chunk = StateUtil.getChunk(player);
		Municipality mun = chunk.getDistrict().getMunicipality();
		switch(args[0]){
			case "info":{
				Print.chat(sender, "&e====-====-====-====-====-====&0[&2States&0]");
				Print.chat(sender, "&6Info of Municipality &7" + mun.getName() + " (" + mun.getId() + ")&2:");
				Print.chat(sender, "&9State: &7" + mun.getState().getName() + " (" + mun.getState().getId() + ")");
				Print.chat(sender, "&9Mayor: &7" + (mun.getMayor() == null ? "no one" : Static.getPlayerNameByUUID(mun.getMayor())));
				Print.chat(sender, "&9Price: &7" + (mun.getPrice() > 0 ? Config.getWorthAsString(mun.getPrice()) : "not for sale"));
				Print.chat(sender, "&9Type: &7" + mun.getType().getTitle());
				Print.chat(sender, "&6Color: &7" + mun.getColor());
				Print.chat(sender, "&9Balance: &7" + Config.getWorthAsString(mun.getAccount().getBalance()));
				Print.chat(sender, "&9Last change: &7" + Time.getAsString(mun.getChanged()));
				Print.chat(sender, "&9Council Members: &7" + mun.getCouncil().size());
				mun.getCouncil().forEach(uuid -> {
					Print.chat(sender, "&c-> &9" + Static.getPlayerNameByUUID(uuid));
				});
				Print.chat(sender, "&9Districts: &7" + mun.getDistricts().size());
				Print.chat(sender, "&9Neighbors: &7" + mun.getNeighbors().size());
				mun.getNeighbors().forEach(var -> {
					Municipality municipality = StateUtil.getMunicipality(var);
					Print.chat(sender, "&c-> &9" + municipality.getName() + " &7(" + municipality.getId() + ");");
				});
				Print.chat(sender, "&3Open to join: " + mun.isOpen());
				Print.chat(sender, "&2Created by &7" + Static.getPlayerNameByUUID(mun.getCreator()) + "&2 at &8" + Time.getAsString(mun.getCreated()));
				return;
			}
			case "types":{
				Print.chat(sender, "&9Existing municipality types:");
				for(MunicipalityType type : MunicipalityType.values()){
					Print.chat(sender, "&2-> &3 " + type.toDetailedString());
				}
				Print.chat(sender, "&9While the numbers mean: &71. required citizen | 2. district limit");
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
