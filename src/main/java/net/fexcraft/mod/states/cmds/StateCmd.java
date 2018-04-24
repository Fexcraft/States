package net.fexcraft.mod.states.cmds;

import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.lib.api.common.fCommand;
import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.lib.util.common.Static;
import net.fexcraft.mod.lib.util.math.Time;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.api.Municipality;
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
			Print.chat(sender, "&7/st set <option> <value>");
			Print.chat(sender, "&7/st council <args...>");
			Print.chat(sender, "&7/st blacklist <args...>");
			Print.chat(sender, "&7/st mun <option> <args>");
			Print.chat(sender, "&7/st invite <id>");
			Print.chat(sender, "&7/st create <name...>");
			return;
		}
		EntityPlayer player = (EntityPlayer)sender.getCommandSenderEntity();
		Chunk chunk = StateUtil.getChunk(player);
		State state = chunk.getDistrict().getMunicipality().getState();
		switch(args[0]){
			case "info":{
				Print.chat(sender, "&e====-====-====-====-====-====&0[&2States&0]");
				Print.chat(sender, "&6Info of State &7" + state.getName() + " (" + state.getId() + ")&2:");
				Print.chat(sender, "&9Capital: &7" + StateUtil.getMunicipality(state.getCapitalId()).getName() + " (" + state.getCapitalId() + ")");
				Print.chat(sender, "&9Leader: &7" + (state.getLeader() == null ? "no one" : Static.getPlayerNameByUUID(state.getLeader())));
				Print.chat(sender, "&9Price: &7" + (state.getPrice() > 0 ? Config.getWorthAsString(state.getPrice()) : "not for sale"));
				Print.chat(sender, "&6Color: &7" + state.getColor());
				Print.chat(sender, "&8Citizen: &7" + getCitizenAmount(state));
				Print.chat(sender, "&9Balance: &7" + Config.getWorthAsString(state.getAccount().getBalance()));
				Print.chat(sender, "&9Last change: &7" + Time.getAsString(state.getChanged()));
				Print.chat(sender, "&9Council Members: &7" + state.getCouncil().size());
				state.getCouncil().forEach(uuid -> {
					Print.chat(sender, "&c-> &9" + Static.getPlayerNameByUUID(uuid));
				});
				Print.chat(sender, "&9Municipalities: &7" + state.getMunicipalities().size());
				state.getMunicipalities().forEach(var -> {
					Municipality municipality = StateUtil.getMunicipality(var);
					Print.chat(sender, "&c-> &9" + municipality.getName() + " &7(" + municipality.getId() + ");");
				});
				Print.chat(sender, "&9Neighbors: &7" + state.getNeighbors().size());
				state.getNeighbors().forEach(var -> {
					State st = StateUtil.getState(var);
					Print.chat(sender, "&c-> &9" + st.getName() + " &7(" + st.getId() + ");");
				});
				Print.chat(sender, "&2Created by &7" + Static.getPlayerNameByUUID(state.getCreator()) + "&2 at &8" + Time.getAsString(state.getCreated()));
				return;
			}
			default:{
				Print.chat(sender, "&cInvalid Argument.");
				return;
			}
		}
	}
	
	private int getCitizenAmount(State state){
		int citizen = 0;
		for(int id : state.getMunicipalities()){
			Municipality mun = StateUtil.getMunicipality(id);
			if(mun.getId() == -1){ continue; }
			citizen += mun.getCitizen().size();
		}
		return citizen;
	}

	private static boolean isAdmin(EntityPlayer player){
		return ChunkCmd.isAdmin(player);
	}
	
}
