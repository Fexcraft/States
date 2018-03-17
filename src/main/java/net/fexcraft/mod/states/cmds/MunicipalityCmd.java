package net.fexcraft.mod.states.cmds;

import java.awt.Color;

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
			Print.chat(sender, "&7/mun council <args...>");
			Print.chat(sender, "&7/mun blacklist <args...>");
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
			case "set":{
				if(args.length < 2){
					Print.chat(sender, "&7/dis set open <true/false>");
					Print.chat(sender, "&7/dis set name <new name>");
					Print.chat(sender, "&7/dis set price <price/0>");
					Print.chat(sender, "&7/dis set color <hex>");
					return;
				}
				boolean can0 = (mun.getMayor() != null && mun.getMayor().equals(player.getGameProfile().getId())) || isAdmin(player);
				boolean can1 = (mun.getMayor() != null && mun.getMayor().equals(player.getGameProfile().getId())) || mun.getState().getCouncil().contains(player.getGameProfile().getId()) || (mun.getState().getLeader() != null && mun.getState().getLeader().equals(player.getGameProfile().getId())) || isAdmin(player);
				boolean can2 = mun.getCouncil().contains(player.getGameProfile().getId());
				switch(args[1]){
					case "name":{
						if(can0){
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
							mun.setName(str);
							mun.setChanged(Time.getDate());
							mun.save();
							Print.chat(sender, "&6Name set to: &7" + mun.getName());
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
								mun.setPrice(price);
								mun.setChanged(Time.getDate());
								mun.save();
								Print.chat(sender, "&2Price set to: &7" + Config.getWorthAsString(mun.getPrice()));
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
								mun.setColor(str);
								mun.setChanged(Time.getDate());
								mun.save();
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
					case "open":{
						if(can0){
							if(args.length < 3){
								Print.chat(sender, "&9Missing Argument!");
								break;
							}
							mun.setOpen(Boolean.parseBoolean(args[2]));
							mun.setChanged(Time.getDate());
							mun.save();
							Print.chat(sender, "&2Open: &7" + mun.isOpen());
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
			case "council":{
				
				return;
			}
			case "blacklist":{
				
				return;
			}
			case "kick":{
				
				return;
			}
			case "invite":{
				
				return;
			}
			case "create":{
				
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
