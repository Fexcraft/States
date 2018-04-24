package net.fexcraft.mod.states.cmds;

import java.awt.Color;
import java.util.ArrayList;
import java.util.UUID;

import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.lib.api.common.fCommand;
import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.lib.util.common.Static;
import net.fexcraft.mod.lib.util.math.Time;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.api.Municipality;
import net.fexcraft.mod.states.api.State;
import net.fexcraft.mod.states.api.root.AnnounceLevel;
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
			Print.chat(sender, "&7/st create <name...>");
			Print.chat(sender, "&7/st citizen");
			Print.chat(sender, "&7/st buy");
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
				Print.chat(sender, "&8Citizen: &7" + getCitizens(state).size());
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
			case "set":{
				if(args.length < 2){
					Print.chat(sender, "&7/mun set name <new name>");
					Print.chat(sender, "&7/mun set price <price/0>");
					Print.chat(sender, "&7/mun set color <hex>");
					Print.chat(sender, "&7/mun set capital <municipality id>");
					return;
				}
				switch(args[1]){
					case "name":{
						if(hasPerm("state.set.name", player, state)){
							if(args.length < 3){
								Print.chat(sender, "&9Missing Arguments!");
								break;
							}
							String str = args[2];
							if(args.length > 3){
								for(int i = 3; i < args.length; i++){
									str += " " + args[i];
								}
							}
							if(str.replace(" ", "").length() < 3){
								Print.chat(sender, "&cName is too short!");
								break;
							}
							state.setName(str);
							state.setChanged(Time.getDate());
							state.save();
							Print.chat(sender, "&6Name set to: &7" + state.getName());
						}
						else{
							Print.chat(sender, "&cNo permission.");
						}
						break;
					}
					case "price":{
						if(hasPerm("state.set.price", player, state)){
							if(args.length < 3){
								Print.chat(sender, "&9Missing Argument!");
								Print.chat(sender, "&7Setting the price to \"0\" makes the state not buyable.");
								break;
							}
							try{
								Long price = Long.parseLong(args[2]);
								if(price < 0){ price = 0l; }
								state.setPrice(price);
								state.setChanged(Time.getDate());
								state.save();
								Print.chat(sender, "&2Price set to: &7" + Config.getWorthAsString(state.getPrice()));
							}
							catch(Exception e){
								Print.chat(sender, "&cError: &7" + e.getMessage());
							}
						}
						else{
							Print.chat(sender, "&cNo permission.");
						}
						break;
					}
					case "color":{
						if(hasPerm("state.set.color", player, state)){
							if(args.length < 3){
								Print.chat(sender, "&9Missing Argument!");
								break;
							}
							try{
								String str = args[2];
								if(str.replace("#", "").length() != 6){
									Print.chat(sender, "&cInvalid HEX String.");
									break;
								}
								str = str.startsWith("#") ? str : "#" + str;
								Color color = Color.decode(str);
								state.setColor(str);
								state.setChanged(Time.getDate());
								state.save();
								Print.chat(sender, "&6Color set to &7" + str + "&6! (" + color.getRed() + ", " + color.getGreen() + ", " + color.getBlue() + ");");
							}
							catch(Exception e){
								Print.chat(sender, "&2Error: &7" + e.getMessage());
							}
						}
						else{
							Print.chat(sender, "&cNo permission.");
						}
						break;
					}
					case "capital":{
						if(hasPerm("state.set.capital", player, state)){
							if(args.length < 3){
								Print.chat(sender, "&9Missing Argument!");
								break;
							}
							Municipality mun = StateUtil.getMunicipality(Integer.parseInt(args[2]));
							if(mun.getId() <= 0 || mun.getState().getId() != state.getId()){
								Print.chat(sender, "&cThat Municipality isn't part of our State.");
								break;
							}
							state.setCapitalId(mun.getId());
							state.setChanged(Time.getDate());
							StateUtil.announce(server, AnnounceLevel.STATE_ALL, "&6" + mun.getName() + " &9 is now the new Capital!", state.getId());
							return;
						}
						else{
							Print.chat(sender, "&cNo permission.");
						}
						break;
					}
				}
				return;
			}
			case "council":{
				
				return;
			}
			case "blacklist":{
				Print.chat(sender, "Not available yet.");
				return;
			}
			case "mun": case "municipality":{
				
				return;
			}
			case "create":{
				
				return;
			}
			case "citizen":{
				Print.chat(sender, "&9Citizen: &7" + getCitizens(state).size());
				for(int id : state.getMunicipalities()){
					Municipality mun = StateUtil.getMunicipality(id);
					if(mun != null && mun.getId() >= 0){
						Print.chat(sender, "&6Municipality: &7" + mun.getName() + "&8(" + mun.getId() + ");");
						mun.getCitizen().forEach(uuid -> {
							Print.chat(sender, "&e-> &9" + Static.getPlayerNameByUUID(uuid) + (mun.getCouncil().contains(uuid) ? " &6" + "[CM]" : ""));
						});
					}
				}
				return;
			}
			default:{
				Print.chat(sender, "&cInvalid Argument.");
				return;
			}
		}
	}
	
	private ArrayList<UUID> getCitizens(State state){
		ArrayList<UUID> list = new ArrayList<UUID>();
		for(int id : state.getMunicipalities()){
			Municipality mun = StateUtil.getMunicipality(id);
			if(mun.getId() == -1){ continue; }
			list.addAll(mun.getCitizen());
		}
		return list;
	}

	public static final boolean hasPerm(String perm, EntityPlayer player, Object obj){
		return ChunkCmd.hasPerm(perm, player, obj);
	}
	
}
