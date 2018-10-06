package net.fexcraft.mod.states.cmds;

import java.util.UUID;

import net.fexcraft.mod.lib.api.common.fCommand;
import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.lib.util.common.Static;
import net.fexcraft.mod.lib.util.math.Time;
import net.fexcraft.mod.states.api.capabilities.PlayerCapability;
import net.fexcraft.mod.states.api.capabilities.StatesCapabilities;
import net.fexcraft.mod.states.util.MailUtil;
import net.fexcraft.mod.states.api.Mailbox.MailType;
import net.fexcraft.mod.states.api.Mailbox.RecipientType;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;

@fCommand
public class MailCmd extends CommandBase {

    @Override
    public String getName(){
        return "mail";
    }

    @Override
    public String getUsage(ICommandSender sender){
	return "/mail";
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
			Print.chat(sender, "&7/mail inbox");
			Print.chat(sender, "&7/mail read");
			Print.chat(sender, "&7/mail send <receiver> <msg...>");
			//Print.chat(sender, "&7/mail accept <args...>");
			//Print.chat(sender, "&7/mail deny <args...>");
			return;
		}
		if(sender instanceof EntityPlayer == false){
			Print.chat(sender, "&7Only available Ingame.");
			return;
		}
		EntityPlayer player = getCommandSenderAsPlayer(sender);
		PlayerCapability cap = player.getCapability(StatesCapabilities.PLAYER, null);
		switch(args[0]){
			case "inbox":{
				if(cap.getMailbox() == null){
					Print.chat(player, "&7You do not have a mailbox set!");
				}
				else{
					Print.chat(player, "&7Your mailbox is at: " + cap.getMailbox().toString());
				}
				return;
			}
			case "read":{
				//TODO check mail item in hand
				return;
			}
			case "temp":{
				String invmsg = "You have been invited to join the Municipality NULL (0)!";
				NBTTagCompound compound = new NBTTagCompound();
				compound.setString("type", "municipality");
				compound.setString("from", player.getGameProfile().getId().toString());
				compound.setLong("at", Time.getDate());
				MailUtil.send(sender, RecipientType.PLAYER, player.getGameProfile().getId().toString(), player.getGameProfile().getId().toString(), invmsg, MailType.INVITE, Time.DAY_MS * 2, compound);
				return;
			}
			case "send":{
				if(args.length < 3){
					Print.chat(sender, "&7/mail send <receiver> <msg...>");
					return;
				}
				try{
					String receiver = args[1]; try{
						UUID uuid = UUID.fromString(receiver);
						receiver = uuid.toString();
					}
					catch(Exception e){
						receiver = Static.getServer().getPlayerProfileCache().getGameProfileForUsername(receiver).getId().toString();
					}
					String msg = args[2];
					if(args.length > 3){
						for(int i = 3; i < args.length; i++){
							msg += " " + args[i];
						}
					}
					MailUtil.send(sender, RecipientType.PLAYER, receiver, player.getGameProfile().getId().toString(), msg, MailType.PRIVATE);
					Print.chat(sender, "&6Mail queued!");
				}
				catch(Exception e){
					e.printStackTrace();
				}
				return;
			}
			/*case "accept": case "deny": {
				if(args.length < 4){
					Print.chat(sender, "Missing arguments.");
					return;
				}
				try{
					/*PlayerCapability cap = player.getCapability(StatesCapabilities.PLAYER, null);
					GenericMail mail = new GenericMail(args[1], args[2], args[3]);
					if(mail.expired() || mail.getData().has("status")){
						Print.chat(sender, "&8&lInvite Expired.");
						return;
					}
					else if(args[0].equals("deny")){
						mail.getData().addProperty("status", "denied");
						mail.archive();
						Print.chat(sender, "&e&lInvite denied.");
					}
					else{
						switch(mail.getData().get("type").getAsString()){
							case "municipality":{
								if(cap.getMunicipality().getId() >= 0){
									Print.chat(sender, "&9You must leave your current Municipality first.");
									return;
								}
								Municipality mun = StateUtil.getMunicipality(mail.getData().get("id").getAsInt(), false);
								if(mun == null || mun.getId() < 0){
									Print.chat(sender, "&9Municipality not found.");
									return;
								}
								if(mun.getPlayerBlacklist().contains(cap.getUUID())){
									Print.chat(sender, "&cYou are blacklisted in this Municipality.");
									return;
								}
								StateUtil.announce(server, AnnounceLevel.MUNICIPALITY, "&o" + cap.getFormattedNickname() + " &e&oleft the Municipality!", cap.getMunicipality().getId());
								StateLogger.log(StateLogger.LoggerType.MUNICIPALITY, StateLogger.player(player) + " left " + StateLogger.municipality(cap.getMunicipality()) + ".");
								cap.setMunicipality(mun);
								cap.save(); mun.save();
								StateUtil.announce(server, AnnounceLevel.MUNICIPALITY, "&o" + cap.getFormattedNickname() + " &2&ojoined the Municipality!", cap.getMunicipality().getId());
								StateLogger.log(StateLogger.LoggerType.MUNICIPALITY, StateLogger.player(player) + " joined " + StateLogger.municipality(cap.getMunicipality()) + ".");
								break;
							}
							case "municipality_council":{
								Municipality mun = StateUtil.getMunicipality(mail.getData().get("id").getAsInt(), false);
								if(mun == null || mun.getId() < 0){
									Print.chat(sender, "&9Municipality not found.");
									return;
								}
								if(cap.getMunicipality().getId() != mun.getId()){
									Print.chat(sender, "&9You are not part of that Municipality.");
									return;
								}
								mun.getCouncil().add(cap.getUUID());
								mun.save();
								StateUtil.announce(server, AnnounceLevel.MUNICIPALITY, cap.getFormattedNickname() + " joined the Coucil of our Municipality!", mun.getId());
								StateLogger.log(StateLogger.LoggerType.MUNICIPALITY, StateLogger.player(player) + " joined to the council of " + StateLogger.municipality(mun) + ".");
								break;
							}
							case "state_council":{
								State state = StateUtil.getState(mail.getData().get("id").getAsInt(), false);
								if(state == null || state.getId() < 0){
									Print.chat(sender, "&6State not found.");
									return;
								}
								if(cap.getState().getId() != state.getId()){
									Print.chat(sender, "&6You are not part of that State.");
									return;
								}
								state.getCouncil().add(cap.getUUID());
								state.save();
								StateUtil.announce(server, AnnounceLevel.STATE, cap.getFormattedNickname() + " joined the Coucil of our State!", state.getId());
								StateLogger.log(StateLogger.LoggerType.STATE, StateLogger.player(player) + " joined to the council of " + StateLogger.state(state) + ".");
								break;
							}
							case "state_municipality":{
								State state = StateUtil.getState(mail.getData().get("id").getAsInt(), false);
								if(state == null || state.getId() < 0){
									Print.chat(sender, "&6State not found.");
									return;
								}
								if(cap.getMunicipality().getState().getId() >= 0){
									Print.chat(sender, "&6You must leave your current State first!");
									return;
								}
								if(!cap.getMunicipality().getMayor().equals(cap.getUUID())){
									Print.chat(sender, "&7No permission.");
									return;
								}
								cap.getMunicipality().setState(state);
								StateUtil.announce(server, AnnounceLevel.STATE, "Municipality of " + cap.getMunicipality().getName() + " joined our State!", state.getId());
								StateLogger.log(StateLogger.LoggerType.STATE, StateLogger.player(player) + " >>> " + StateLogger.municipality(cap.getMunicipality()) + " joined the State of " + StateLogger.state(state));
								break;
							}
						}
						mail.getData().addProperty("status", "accepted");
						mail.archive();
						Print.chat(sender, "&a&lInvite accepted.");
					}
				}
				catch(Exception e){
					e.printStackTrace();
				}
				return;
			}*/
		}
	}
	
	public static final String getSender(String str){
		try{
			UUID uuid = UUID.fromString(str);
			return Static.getPlayerNameByUUID(uuid);
		}
		catch(Exception e){
			return str;
		}
	}

}
