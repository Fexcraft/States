package net.fexcraft.mod.states.cmds;

import java.awt.Color;

import com.mojang.authlib.GameProfile;

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
				Print.chat(sender, "&7Can Foreigners Settle: " + dis.canForeignersSettle());
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
				if(args.length < 2){
					Print.chat(sender, "&7/dis set type <type>");
					Print.chat(sender, "&7/dis set name <new name>");
					Print.chat(sender, "&7/dis set price <price/0>");
					Print.chat(sender, "&7/dis set manager <playername>");
					Print.chat(sender, "&7/dis set color <hex>");
					Print.chat(sender, "&7/dis set can-foreigners-settle <true/false>");
					return;
				}
				boolean can0 = (dis.getManager() != null && dis.getManager().equals(player.getGameProfile().getId())) || (dis.getMunicipality().getMayor() != null && dis.getMunicipality().getMayor().equals(player.getGameProfile().getId())) || isAdmin(player);
				boolean can1 = (dis.getMunicipality().getMayor() != null && dis.getMunicipality().getMayor().equals(player.getGameProfile().getId())) || dis.getMunicipality().getState().getCouncil().contains(player.getGameProfile().getId()) || (dis.getMunicipality().getState().getLeader() != null && dis.getMunicipality().getState().getLeader().equals(player.getGameProfile().getId())) || isAdmin(player);
				boolean can2 = dis.getMunicipality().getCouncil().contains(player.getGameProfile().getId());
				switch(args[1]){
					case "type":{
						if(can1){
							if(args.length < 3){
								Print.chat(sender, "&9Missing Argument!");
								Print.chat(sender, "&2You can see available types using &7/dis types&2!");
								break;
							}
							try{
								DistrictType type = DistrictType.valueOf(args[2].toUpperCase());
								if(type != null){
									dis.setType(type);
									dis.setChanged(Time.getDate());
									dis.save();
								}
								Print.chat(sender, "&9District type set to &7" + type.name().toLowerCase() + "&9!");
							}
							catch(Exception e){
								Print.chat(sender, "&9Error: &7" + e.getMessage());
							}
						}
						else{
							Print.chat(sender, "&cNo permission.");
						}
						break;
					}
					case "name":{
						if(can0 || can2){
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
							dis.setName(str);
							dis.setChanged(Time.getDate());
							dis.save();
							Print.chat(sender, "&6Name set to: &7" + dis.getName());
						}
						else{
							Print.chat(sender, "&cNo permission.");
						}
						break;
					}
					case "price":{
						if(can1){
							if(args.length < 3){
								Print.chat(sender, "&9Missing Argument!");
								Print.chat(sender, "&7Setting the price to \"0\" makes the district not buyable.");
								break;
							}
							try{
								Long price = Long.parseLong(args[2]);
								if(price < 0){ price = 0l; }
								dis.setPrice(price);
								dis.setChanged(Time.getDate());
								dis.save();
								Print.chat(sender, "&2Price set to: &7" + Config.getWorthAsString(dis.getPrice()));
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
					case "manager":{
						if(can1 || can2){
							if(args.length < 3){
								Print.chat(sender, "&9Missing Argument!");
								break;
							}
							GameProfile gp = Static.getServer().getPlayerProfileCache().getGameProfileForUsername(args[2]);
							if(gp == null || gp.getId() == null){
								Print.chat(sender, "&cPlayer not found in Cache.");
								break;
							}
							dis.setManager(gp.getId());
							dis.setChanged(Time.getDate());
							dis.save();
							Print.chat(sender, "&2Set &7" + gp.getName() + "&2 to new District Manager!");
						}
						else{
							Print.chat(sender, "&cNo permission.");
						}
						break;
					}
					case "color":{
						if(can0 || can2){
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
								dis.setColor(str);
								dis.setChanged(Time.getDate());
								dis.save();
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
					case "can-foreigners-settle":{
						if(can0 || can2){
							if(args.length < 3){
								Print.chat(sender, "&9Missing Argument!");
								break;
							}
							dis.setForeignersSettle(Boolean.parseBoolean(args[2]));
							dis.setChanged(Time.getDate());
							dis.save();
							Print.chat(sender, "&2FCS: &7" + dis.canForeignersSettle());
						}
						else{
							Print.chat(sender, "&cNo permission.");
						}
						break;
					}
					default:{
						Print.chat(sender, "&9Invalid Argument.");
						break;
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
	
	private static boolean isAdmin(EntityPlayer player){
		return ChunkCmd.isAdmin(player);
	}
	
}
