package net.fexcraft.mod.states.cmds;

import java.awt.Color;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;

import net.fexcraft.mod.fsmm.api.Bank;
import net.fexcraft.mod.fsmm.util.AccountManager;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.lib.api.common.fCommand;
import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.lib.util.common.Static;
import net.fexcraft.mod.lib.util.math.Time;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.api.DistrictType;
import net.fexcraft.mod.states.api.Mail;
import net.fexcraft.mod.states.api.MailType;
import net.fexcraft.mod.states.api.Municipality;
import net.fexcraft.mod.states.api.MunicipalityType;
import net.fexcraft.mod.states.api.Player;
import net.fexcraft.mod.states.impl.GenericDistrict;
import net.fexcraft.mod.states.impl.GenericMail;
import net.fexcraft.mod.states.impl.GenericMunicipality;
import net.fexcraft.mod.states.util.StateUtil;
import net.fexcraft.mod.states.util.world.WorldCapabilityUtil;
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
			Print.chat(sender, "&7/mun citizen");
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
				Print.chat(sender, "&8Citizen: &7" + mun.getCitizen().size());
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
					Print.chat(sender, "&7/mun set open <true/false>");
					Print.chat(sender, "&7/mun set name <new name>");
					Print.chat(sender, "&7/mun set price <price/0>");
					Print.chat(sender, "&7/mun set color <hex>");
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
								Print.chat(sender, "&7Setting the price to \"0\" makes the municipality not buyable.");
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
				if(args.length < 2){
					Print.chat(sender, "&7/mun council vote <playername> (for mayor)");
					Print.chat(sender, "&7/mun council kick <playername>");
					Print.chat(sender, "&7/mun council invite <playername>");
					Print.chat(sender, "&7/mun council leave");
					Print.chat(sender, "&7/mun council join");
					return;
				}
				return;
			}
			case "blacklist":{
				if(args.length < 2){
					Print.chat(sender, "&7/mun blacklist add <playername/company:id>");
					Print.chat(sender, "&7/mun blacklist remove <playername/company:id>");
					Print.chat(sender, "&7/mun blacklist view");
					return;
				}
				if(args[1].equals("view")){
					Print.chat(sender, "&9Blacklisted Players: &7" + mun.getPlayerBlacklist().size());
					mun.getPlayerBlacklist().forEach(uuid -> {
						Print.chat(sender, "&c-> &9" + Static.getPlayerNameByUUID(uuid));
					});
					Print.chat(sender, "&9Blacklisted Companies: &7" + mun.getCompanyBlacklist().size());
					mun.getCompanyBlacklist().forEach(id -> {
						Print.chat(sender, "&c-> &9" + id);
					});//TODO
					return;
				}
				if(!mun.getCouncil().contains(player.getGameProfile().getId())){
					Print.chat(sender, "&cNo permission!");
					return;
				}
				if(args[1].equals("add") || args[1].equals("remove") || args[1].equals("rem")){
					if(args.length < 3){
						Print.chat(sender, "&cMissing Argument.");
						return;
					}
					GameProfile gp = Static.getServer().getPlayerProfileCache().getGameProfileForUsername(args[2]);
					if(gp == null || gp.getId() == null){
						Print.chat(sender, "&aPlayer not found.");
						return;
					}
					if(mun.getCouncil().contains(gp.getId())){
						Print.chat(sender, "&9You can not blacklist council members!");
						Print.chat(sender, "&cKick them from the council first!");
						return;
					}
					if(args[1].equals("add")){
						mun.getPlayerBlacklist().add(gp.getId());
						Print.chat(sender, "&9Player &7" + gp.getName() + "&9 added to blacklist!");
						return;
					}
					else{
						mun.getPlayerBlacklist().remove(gp.getId());
						Print.chat(sender, "&9Player &7" + gp.getName() + "&9 removed from blacklist!");
						return;
					}
				}
				else{
					Print.chat(sender, "&9Invalid Argument.");
				}
				return;
			}
			case "citizen":{
				Print.chat(sender, "&9Citizen: &7" + mun.getCouncil().size());
				mun.getCitizen().forEach(uuid -> {
					Print.chat(sender, "&c-> &9" + Static.getPlayerNameByUUID(uuid) + (mun.getCouncil().contains(uuid) ? " &6" + "[CM]" : ""));
				});
				return;
			}
			case "kick":{
				if(!mun.getCouncil().contains(player.getGameProfile().getId())){
					Print.chat(sender, "&cNo permission!");
					return;
				}
				if(args.length < 2){
					Print.chat(sender, "&7/mun kick <playername> <optional:reason>");
					return;
				}
				GameProfile gp = server.getPlayerProfileCache().getGameProfileForUsername(args[1]);
				if(gp == null || gp.getId() == null){
					Print.chat(sender, "&cPlayer not found.");
					return;
				}
				if(mun.getCouncil().contains(gp.getId())){
					Print.chat(sender, "&9You can not kick council members!");
					Print.chat(sender, "&cUse &7/mun council kick &c instead!");
					return;
				}
				if(!mun.getCitizen().contains(gp.getId())){
					Print.chat(sender, "That player isn't a citizen of this Municipality.");
					return;
				}
				String reason = null;
				if(args.length > 2){
					reason = args[2];
					if(args.length >= 3){
						for(int i = 3; i < args.length; i++){
							reason += " " + args[i];
						}
					}
				}
				mun.getCitizen().remove(gp.getId());
				String kickmsg = "You have been kicked from the Municipality (" + mun.getId() + ") for: " + (reason == null ? "No Kick reason given." : reason);
				Mail mail = new GenericMail("player", gp.getId().toString(), player.getGameProfile().getId().toString(), kickmsg, MailType.SYSTEM, null);
				Player playr = StateUtil.getPlayer(gp.getId(), false);
				if(playr != null){ playr.setMunicipality(StateUtil.getMunicipality(-1)); }
				StateUtil.sendMail(mail);
				Print.chat(sender, "&7Player &9" + gp.getName() + "&7 kicked from the Municipality!");
				return;
			}
			case "invite":{
				if(!mun.getCouncil().contains(player.getGameProfile().getId())){
					Print.chat(sender, "&cNo permission!");
					return;
				}
				if(args.length < 2){
					Print.chat(sender, "&7/mun invite <playername> <optional:message>");
					return;
				}
				GameProfile gp = server.getPlayerProfileCache().getGameProfileForUsername(args[1]);
				if(gp == null || gp.getId() == null){
					Print.chat(sender, "&cPlayer not found.");
					return;
				}
				if(mun.getCitizen().contains(gp.getId())){
					Print.chat(sender, "That player is already a citizen of this Municipality.");
					return;
				}
				String msg = null;
				if(args.length > 2){
					msg = args[2];
					if(args.length >= 3){
						for(int i = 3; i < args.length; i++){
							msg += " " + args[i];
						}
					}
				}
				String invmsg = "You have been invited to join the Municipality " + mun.getName() + " (" + mun.getId() + ")!" + (msg == null ? "" : " MSG: " + msg);
				JsonObject obj = new JsonObject();
				obj.addProperty("type", "municipality");
				obj.addProperty("from", player.getGameProfile().getId().toString());
				obj.addProperty("at", Time.getDate());
				obj.addProperty("valid", Time.DAY_MS * 2);
				Mail mail = new GenericMail("player", gp.getId().toString(), player.getGameProfile().getId().toString(), invmsg, MailType.INVITE, obj);
				StateUtil.sendMail(mail);
				Print.chat(sender, "&7&oInvite sent! (Will be valid for 2 days.)");
				return;
			}
			case "create":{
				Player ply = StateUtil.getPlayer(player);
				long price = net.fexcraft.mod.states.util.Config.MUNICIPALITY_CREATION_PRICE;
				if(ply == null){
					Print.chat(sender, "&o&4There was an error loading your Playerdata.");
					return;
				}
				if(ply.getMunicipality().getId() >= 0){
					Print.chat(sender, "&cYou must leave your current municipality to create a new one.");
					return;
				}
				if(chunk.getDistrict().getId() >= 0){
					Print.chat(sender, "&cThis chunk is already part of a municipality.");
					return;
				}
				if(ply.getAccount().getBalance() < price){
					Print.chat(sender, "&cNot enough money to create a municipality.");
					Print.chat(sender, "(needed: " + Config.getWorthAsString(price, false) + "; available: " + Config.getWorthAsString(ply.getAccount().getBalance(), false) + ")");
					return;
				}
				if(args.length < 2){
					Print.chat(sender, "&9No name for new Municipality Specified.");
					return;
				}
				Bank bank = AccountManager.INSTANCE.getBank(ply.getAccount().getBankId());
				if(bank == null){
					Print.chat(sender, "&9Your bank couldn't be found.");
					return;
				}
				//TODO permissions check
				try{
					String name = args[1];
					if(args.length > 2){
						for(int i = 2; i < args.length; i++){
							name += " " + args[i];
						}
					}
					GenericMunicipality newmun = new GenericMunicipality(sender.getEntityWorld().getCapability(WorldCapabilityUtil.WORLD_CAPABILITY, null).getNewMunicipalityId());
					if(newmun.getMunicipalityFile().exists() || StateUtil.getMunicipality(newmun.getId()).getId() >= 0){
						throw new Exception("Tried to create new Municipality with ID '" + newmun.getId() + "', but savefile already exists.");
					}
					else{
						newmun.setCreator(ply.getUUID());
						newmun.setName(name);
						newmun.setMayor(ply.getUUID());
						newmun.setOpen(false);
						newmun.setPrice(0);
						newmun.getCitizen().add(ply.getUUID());
						//
						GenericDistrict newdis = new GenericDistrict(sender.getEntityWorld().getCapability(WorldCapabilityUtil.WORLD_CAPABILITY, null).getNewDistrictId());
						if(newdis.getDistrictFile().exists() || StateUtil.getDistrict(newdis.getId()).getId() >= 0){
							throw new Exception("Tried to create new Municipality with ID '" + newmun.getId() + "', but savefile already exists.");
						}
						else{
							newdis.setCreator(ply.getUUID());
							newdis.setName("Center");
							newdis.setManager(ply.getUUID());
							newdis.setForeignersSettle(false);
							newdis.setMunicipality(newmun);
							newdis.setPrice(0);
							newdis.setType(DistrictType.VILLAGE);
							//
							//Now let's save stuff.
							long halfprice = price / 2;
							if(halfprice == 0 || bank.processTransfer(sender, ply.getAccount(), halfprice, States.SERVERACCOUNT)){
								bank.processTransfer(null, ply.getAccount(), halfprice, newmun.getAccount());
								newmun.save(); States.MUNICIPALITIES.put(newmun.getId(), newmun);
								newdis.save(); States.DISTRICTS.put(newdis.getId(), newdis);
								chunk.setDistrict(newdis); chunk.save();
								ply.setMunicipality(newmun);
								StateUtil.announce(server, "&9New Municipality and District was created!");
								StateUtil.announce(server, "&9Created by " + ply.getFormattedNickname(sender));
								StateUtil.announce(server, "&9Name&0: &7" + newmun.getName());
							}
						}
					}
				}
				catch(Exception e){
					Print.chat(sender, "Error: " + e.getMessage());
					Print.chat(sender, e);
					Print.debug(e);
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
