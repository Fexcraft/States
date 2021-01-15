package net.fexcraft.mod.states.cmds;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.capabilities.FCLCapabilities;
import net.fexcraft.lib.mc.utils.Formatter;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.data.Municipality;
import net.fexcraft.mod.states.data.State;
import net.fexcraft.mod.states.data.capabilities.PlayerCapability;
import net.fexcraft.mod.states.data.capabilities.StatesCapabilities;
import net.fexcraft.mod.states.data.root.AnnounceLevel;
import net.fexcraft.mod.states.data.root.Mailbox.MailType;
import net.fexcraft.mod.states.data.root.Mailbox.RecipientType;
import net.fexcraft.mod.states.impl.SignMailbox;
import net.fexcraft.mod.states.objects.MailItem;
import net.fexcraft.mod.states.util.AliasLoader;
import net.fexcraft.mod.states.util.MailUtil;
import net.fexcraft.mod.states.util.Perms;
import net.fexcraft.mod.states.util.StConfig;
import net.fexcraft.mod.states.util.StateLogger;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;

public class MailCmd extends CommandBase {

    @Override
    public String getName(){
        return AliasLoader.getOverride("st-mail");
    }

    @Override
    public String getUsage(ICommandSender sender){
    	return "/" + getName();
    }
	
	@Override
	public List<String> getAliases(){
		return AliasLoader.getAlias("st-mail");
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
			Print.chat(sender, "&7/st-mail inbox");
			Print.chat(sender, "&7/st-mail read");
			Print.chat(sender, "&7/st-mail send <receiver> <msg...>");
			Print.chat(sender, "&7/st-mail collect <box-type>");
			//Print.chat(sender, "&7/st-mail accept <args...>");
			//Print.chat(sender, "&7/st-mail deny <args...>");
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
					Print.chat(player, "&7Your mail will be redirected to State or Municipality Mailbox!");
				}
				else{
					Print.chat(player, "&7Your mailbox is at: " + cap.getMailbox().toString());
				}
				return;
			}
			case "read":{
				if(!player.getHeldItemMainhand().isEmpty()){
					ItemStack stack = player.getHeldItemMainhand();
					if(stack.getItem() instanceof MailItem == false){
						Print.chat(player, "Not a valid Mail Item in hand."); return;
					}
					else if(stack.getTagCompound() == null || stack.getMetadata() < 2){
						if(stack.getMetadata() == 1){
							Print.chat(player, "Mail expired, it cannot be read anymore."); return;
						}
						Print.chat(player, "Item has no data to read."); return;
					}
					if(!StateUtil.isAdmin(player) && !Perms.MAIL_READ_ANY.has(player)){
						ResourceLocation loc = new ResourceLocation(stack.getTagCompound().getString("Receiver"));
						switch(loc.getNamespace()){
							case "player":{
								if(!stack.getTagCompound().getString("Receiver").replace("player:", "").equals(player.getGameProfile().getId().toString())){
									Print.chat(player, "Receiver and your UUID do not match."); return;
								} break;
							}
							case "district":{
								if(!cap.isDistrictManagerOf(StateUtil.getDistrict(Integer.parseInt(loc.getPath())))
									&& !cap.isMayorOf(StateUtil.getDistrict(Integer.parseInt(loc.getPath())).getMunicipality())){
									Print.chat(player, "Your are not District Manager or Mayor to read this."); return;
								} break;
							}
							case "municipality":{
								if(!cap.isMayorOf(StateUtil.getMunicipality(Integer.parseInt(loc.getPath())))
									&& !StateUtil.getMunicipality(Integer.parseInt(loc.getPath())).getCouncil().contains(player.getGameProfile().getId())){
									Print.chat(player, "Your are not Mayor or Council Member to read this."); return;
								} break;
							}
							case "state":{
								if(!cap.isStateLeaderOf(StateUtil.getState(Integer.parseInt(loc.getPath())))
									&& !StateUtil.getState(Integer.parseInt(loc.getPath())).getCouncil().contains(player.getGameProfile().getId())){
									Print.chat(player, "Your are not State Leader or Council Member to read this."); return;
								} break;
							}
						}
					}
					Print.chat(player, "&e====-====-====-====-====-====" + States.PREFIX);
					String str = stack.getTagCompound().getString("Type");
					Print.chat(player, "&9From: &7" + Static.getPlayerNameByUUID(stack.getTagCompound().getString("Sender")));
					Print.chat(player, "&9Type: &7" + str);
					Print.chat(player, stack.getTagCompound().getString("Content"));
					Print.chat(player, "&cExpires: &7" + Time.getAsString(stack.getTagCompound().getLong("Expiry")));
					if(str.toLowerCase().equals("invite") && stack.getTagCompound().hasKey("StatesData")){
						TextComponentString text = new TextComponentString(Formatter.format("&a&l[ACCEPT] "));
						text.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/st-mail accept"));
						TextComponentString text2 = new TextComponentString(Formatter.format(" &c&l[DENY]"));
						text2.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/st-mail deny"));
						sender.sendMessage(text.appendSibling(text2));
					}
					Print.chat(player, "&e====-====-====-====-====-====" + States.PREFIX);
				}
				else{
					Print.chat(player, "No mail item in hand.");
				}
				return;
			}
			case "temp":{
				String invmsg = "You have been invited to join the Municipality NULL (0)!";
				NBTTagCompound compound = new NBTTagCompound();
				compound.setString("type", "municipality");
				compound.setString("from", player.getGameProfile().getId().toString());
				compound.setLong("at", Time.getDate());
				compound.setInteger("id", 0);
				MailUtil.send(sender, RecipientType.PLAYER, player.getGameProfile().getId().toString(), player.getGameProfile().getId().toString(), invmsg, MailType.INVITE, Time.DAY_MS * 2, compound);
				return;
			}
			case "send":{
				if(args.length < 3){
					Print.chat(sender, "&7/st-mail send <receiver> <msg...>");
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
					if(MailUtil.send(sender, RecipientType.PLAYER, receiver, player.getGameProfile().getId().toString(), msg, MailType.PRIVATE)){
						Print.chat(sender, "&6Mail sent!");
					}
				}
				catch(Exception e){
					e.printStackTrace();
				}
				return;
			}
			case "accept": case "deny": {
				try{
					if(player.getHeldItemMainhand().isEmpty()){
						Print.chat(player, "No mail item in hand.");
					}
					ItemStack stack = player.getHeldItemMainhand();
					if(stack.getItem() instanceof MailItem == false){
						Print.chat(player, "Not a valid Mail Item in hand."); return;
					}
					else if(stack.getTagCompound() == null || stack.getMetadata() != 3){
						Print.chat(player, "Item in hand is not an Invite Mail."); return;
					}
					else if(!stack.getTagCompound().getString("Receiver").replace("player:", "").equals(player.getGameProfile().getId().toString())){
						Print.chat(player, "Receiver and your UUID do not match.");
						if(Static.dev()){
							Print.chat(player, stack.getTagCompound().getString("Receiver"));
							Print.chat(player, player.getGameProfile().getId().toString());
						}
						return;
					}
					if(Time.getDate() >= stack.getTagCompound().getLong("Expiry")){
						Print.chat(player, "&cInvite Expired."); return;
					}
					NBTTagCompound invdata = stack.getTagCompound().getCompoundTag("StatesData");
					if(invdata == null){
						Print.chat(player, "&cNo invite data in Item stored."); return;
					}
					if(invdata.hasKey("status")){
						Print.chat(player, "&aYou replied to this mail already."); return;
					}
					//
					if(args[0].equals("deny")){
						invdata.setString("status", "denied");
						Print.chat(sender, "&e&lInvite denied.");
					}
					else{
						switch(invdata.getString("type")){
							case "municipality":{
								if(cap.getMunicipality().getId() >= 0){
									Print.chat(sender, "&9You must leave your current Municipality first.");
									return;
								}
								Municipality mun = StateUtil.getMunicipality(invdata.getInteger("id"), false);
								if(mun == null || mun.getId() < 0){
									Print.chat(sender, "&9Municipality not found.");
									return;
								}
								if(mun.getPlayerBlacklist().contains(cap.getUUID())){
									Print.chat(sender, "&cYou are blacklisted in this Municipality.");
									return;
								}
								StateUtil.announce(server, AnnounceLevel.MUNICIPALITY, "&o" + cap.getFormattedNickname() + " &e&oleft the Municipality!", cap.getMunicipality().getId());
								Print.log(StateLogger.player(player) + " left " + StateLogger.municipality(cap.getMunicipality()) + ".");
								cap.setMunicipality(mun);
								cap.save(); mun.save();
								StateUtil.announce(server, AnnounceLevel.MUNICIPALITY, "&o" + cap.getFormattedNickname() + " &2&ojoined the Municipality!", cap.getMunicipality().getId());
								Print.log(StateLogger.player(player) + " joined " + StateLogger.municipality(cap.getMunicipality()) + ".");
								break;
							}
							case "municipality_council":{
								Municipality mun = StateUtil.getMunicipality(invdata.getInteger("id"), false);
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
								Print.log(StateLogger.player(player) + " joined to the council of " + StateLogger.municipality(mun) + ".");
								break;
							}
							case "state_council":{
								State state = StateUtil.getState(invdata.getInteger("id"), false);
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
								Print.log(StateLogger.player(player) + " joined to the council of " + StateLogger.state(state) + ".");
								break;
							}
							case "state_municipality":{
								State state = StateUtil.getState(invdata.getInteger("id"), false);
								if(state == null || state.getId() < 0){
									Print.chat(sender, "&6State not found.");
									return;
								}
								if(cap.getMunicipality().getState().getId() >= 0){
									Print.chat(sender, "&6You must leave your current State first!");
									return;
								}
								if(!cap.getMunicipality().getHead().equals(cap.getUUID())){
									Print.chat(sender, "&7No permission.");
									return;
								}
								cap.getMunicipality().setState(state);
								StateUtil.announce(server, AnnounceLevel.STATE, "Municipality of " + cap.getMunicipality().getName() + " joined our State!", state.getId());
								Print.log(StateLogger.player(player) + " >>> " + StateLogger.municipality(cap.getMunicipality()) + " joined the State of " + StateLogger.state(state));
								break;
							}
						}
						invdata.setString("status", "accepted");
						Print.chat(sender, "&a&lInvite accepted.");
					}
				}
				catch(Exception e){
					e.printStackTrace();
				}
				return;
			}
			case "collect":{
				if(!StConfig.ALLOW_MAILBOX_COLLECT){
					Print.chat(sender, "&c&lFeature is disabled on this server.");
				}
				if(args.length < 2){
					Print.chat(sender, "&9Missing argument, try the following:");
					Print.chat(sender, "&7fallback/central, &6state, &5municipality");
					return;
				}
				BlockPos mailbox = null;
				switch(args[1]){
					case "fallback":
					case "central":
					case "state":{
						State state = args[1].equals("state") ? cap.getState() : StateUtil.getState(-1);
						if(state.getMailbox() == null){
							if(args[1].equals("state")){
								Print.chat(player, "&c&oState has no Mailbox set!");
							}
							else{
								Print.chat(player, "&c&oServer has no Fallback/Central Mailbox set!");
							}
							return;
						}
						mailbox = state.getMailbox();
						break;
					}
					case "municipality":{
						if(cap.getMunicipality().getMailbox() == null){
							Print.chat(player, "&c&oMunicipality has no Mailbox set!");
							return;
						}
						mailbox = cap.getMunicipality().getMailbox();
						break;
					}
					default:{
						Print.chat(player, "&7Invalid argument.");
						return;
					}
				}
				if(!player.world.isBlockLoaded(mailbox)){
					Print.chat(player, "&bSelected Mailbox's Chunk is not loaded.");
				}
				TileEntity tile = player.world.getTileEntity(mailbox);
				SignMailbox sign = tile.getCapability(FCLCapabilities.SIGN_CAPABILITY, null).getListener(SignMailbox.class, SignMailbox.RESLOC);
				if(sign.getMails().isEmpty()){
					Print.chat(player, "&bSelected Mailbox is empty.");
				}
				List<ItemStack> mails = sign.getMails().stream().filter(stack -> {
					if(!stack.hasTagCompound()) return false;
					ResourceLocation loc = new ResourceLocation(stack.getTagCompound().getString("Receiver"));
					if(!loc.getNamespace().equals("player")) return false;
					if(!loc.getPath().equals(player.getGameProfile().getId().toString().toLowerCase())) return false;
					return true;
				}).collect(Collectors.toList());
				if(mails.isEmpty()){
					Print.chat(player, "&bYou got no new mail in the selected Mailbox.");
				}
				else{
					sign.getMails().removeAll(mails);
					sign.updateSize(tile, true);
				}
				for(ItemStack stack : mails){
					if(!player.inventory.addItemStackToInventory(stack)){
						EntityItem item = new EntityItem(player.world, player.posX, player.posY + 1, player.posZ, stack);
						player.world.spawnEntity(item);
					}
				}
				Print.chat(player, "&b&oMail collected.");
				return;
			}
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
