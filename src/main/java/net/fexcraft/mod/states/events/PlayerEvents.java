package net.fexcraft.mod.states.events;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.capabilities.FCLCapabilities;
import net.fexcraft.lib.mc.capabilities.sign.SignCapability;
import net.fexcraft.lib.mc.network.PacketHandler;
import net.fexcraft.lib.mc.network.packet.PacketNBTTagCompound;
import net.fexcraft.lib.mc.utils.Formatter;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.data.Chunk;
import net.fexcraft.mod.states.data.District;
import net.fexcraft.mod.states.data.Municipality;
import net.fexcraft.mod.states.data.State;
import net.fexcraft.mod.states.data.capabilities.PlayerCapability;
import net.fexcraft.mod.states.data.capabilities.StatesCapabilities;
import net.fexcraft.mod.states.impl.SignMailbox;
import net.fexcraft.mod.states.util.MessageSender;
import net.fexcraft.mod.states.util.StConfig;
import net.fexcraft.mod.states.util.StateUtil;
import net.fexcraft.mod.states.util.TaxSystem;
import net.fexcraft.mod.states.util.UpdateHandler;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber
public class PlayerEvents {
	
	@SubscribeEvent
	public static void onLogin(PlayerLoggedInEvent event){
		PlayerCapability player = event.player.getCapability(StatesCapabilities.PLAYER, null);
		if(player == null){
			Print.chat(event.player, "Player data couldn't be loaded.");
			return;
		}
		if(!player.isLoaded()){ player.load(); }
		States.PLAYERS.put(event.player.getGameProfile().getId(), player);
		Print.chat(event.player, "&e====-====-====-====-====-====" + States.PREFIX);
		Print.chat(event.player, StateUtil.translate("states.welcome_msg.welcome_back", player.getFormattedNickname()));
		if(event.player.dimension != 0){
			Print.chat(event.player, StateUtil.translate("states.welcome_msg.other_dim", event.player.dimension));
		}
		else{
			Chunk chunk = StateUtil.getChunk(event.player);
			Print.chat(event.player, StateUtil.translate("states.welcome_msg.district", chunk.getDistrict().getName()));
			Print.chat(event.player, StateUtil.translate("states.welcome_msg.municipality", chunk.getMunicipality().getName(), chunk.getMunicipality().getTitle()));
			Print.chat(event.player, StateUtil.translate("states.welcome_msg.state", chunk.getState().getName()));
		}
		if(player.getMailbox() == null) Print.chat(event.player, StateUtil.translate("states.welcome_msg.no_mailbox"));
		if(player.hasRelevantVotes()) Print.chat(event.player, StateUtil.translate("states.welcome_msg.pending_votes", player.getRelevantVotes().size())); 
		Print.chat(event.player, "&e====-====-====-====-====-====" + States.PREFIX);
		sendLocationUpdate(event.player, null, StateUtil.translate("states.welcome_msg.welcome_back", player.getFormattedNickname()), "", "", 3);
		if(UpdateHandler.STATE != null){
			Print.chat(event.player, UpdateHandler.STATE);
		}
		//
		MessageSender.toWebhook(null, event.player.getGameProfile().getName() + " joined.");
		TaxSystem.processPlayerTax(TaxSystem.getProbableSchedule(), player);
	}
	
	@SubscribeEvent
	public static void onLogout(PlayerLoggedOutEvent event){
		PlayerCapability cap = States.PLAYERS.remove(event.player.getGameProfile().getId());
		MessageSender.toWebhook(null, event.player.getGameProfile().getName() + (cap == null ? " failed to join?" : " left."));
	}
	
	@SubscribeEvent
	public static void onRespawn(PlayerEvent.Clone event){
		event.getEntityPlayer().getCapability(StatesCapabilities.PLAYER, null)
			.copyFromOld(event.getOriginal().getCapability(StatesCapabilities.PLAYER, null));
		States.PLAYERS.put(event.getEntityPlayer().getGameProfile().getId(),
			event.getEntityPlayer().getCapability(StatesCapabilities.PLAYER, null));
		MessageSender.toWebhook(null, event.getEntityPlayer().getGameProfile().getName() + " respawned.");
	}
	
	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event){
		if(event.getWorld().isRemote || event.getEntityPlayer().dimension != 0 || event.getEntityPlayer().getActiveHand() == EnumHand.OFF_HAND){
			return;
		}
		IBlockState state = event.getWorld().getBlockState(event.getPos());
		boolean check = false;
		switch(StConfig.PROTLVL){//TODO temporary, dynamic implementation to be added instead later
			case ABSOLUTE:
				check = true;
				break;
			case ADVANCED:
				check = state.getBlock() instanceof BlockChest || state.getBlock() instanceof BlockFurnace
				|| state.getBlock() instanceof BlockHopper || state.getBlock() instanceof BlockDispenser
				|| state.getBlock() instanceof BlockDropper || state.getBlock() instanceof BlockLever
				|| state.getBlock() instanceof BlockButton || state.getBlock() instanceof BlockPressurePlate
				|| state.getBlock() instanceof BlockRedstoneRepeater || state.getBlock() instanceof BlockRedstoneComparator
				|| state.getBlock() instanceof BlockDoor || state.getBlock() instanceof BlockTrapDoor;
				break;
			case BASIC:
				check = state.getBlock() instanceof BlockChest || state.getBlock() instanceof BlockFurnace
					|| state.getBlock() instanceof BlockHopper || state.getBlock() instanceof BlockDispenser
					|| state.getBlock() instanceof BlockDropper || state.getBlock() instanceof BlockLever
					|| state.getBlock() instanceof BlockButton || state.getBlock() instanceof BlockPressurePlate
					|| state.getBlock() instanceof BlockRedstoneRepeater || state.getBlock() instanceof BlockRedstoneComparator;
				break;
			default:
				break;
		}
		if(check && !checkAccess(event.getWorld(), event.getPos(), state, event.getEntityPlayer(), true)){
			Print.chat(event.getEntityPlayer(), "No permission to interact with these blocks here.");
			event.setCanceled(true);
			return;
		}
		else return;
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onBlockBreak0(BlockEvent.BreakEvent event){
		if(event.getWorld().isRemote || event.getPlayer().dimension != 0){ return; }
		if(!checkAccess(event.getWorld(), event.getPos(), event.getState(), event.getPlayer(), false)){
			Print.bar(event.getPlayer(), "No permission to break blocks here.");
			event.setCanceled(true);
		}
		return;
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onBlockBreak1(BlockEvent.BreakEvent event){
		if(event.getWorld().isRemote || event.getPlayer().dimension != 0){ return; }
		if(event.getState().getBlock() instanceof BlockSign){
			TileEntity tile = event.getWorld().getTileEntity(event.getPos());
			if(tile == null) return;
			SignCapability cap = tile.getCapability(FCLCapabilities.SIGN_CAPABILITY, null);
			if(cap == null) return;
			SignMailbox sign = cap.getListener(SignMailbox.class, SignMailbox.RESLOC);
			Chunk chunk = StateUtil.getChunk(event.getPos());
			try{
				switch(sign.getType()){
					case "state":{
						State state = chunk.getState();
						if(state.getMailbox() != null && state.getMailbox().equals(event.getPos())){
							state.setMailbox(null); state.save();
						}
						return;
					}
					case "municipality":{
						Municipality mun = chunk.getMunicipality();
						if(mun.getMailbox() != null && mun.getMailbox().equals(event.getPos())){
							mun.setMailbox(null); mun.save();
						}
						return;
					}
					case "district":{
						District dis = chunk.getDistrict();
						if(dis.getMailbox() != null && dis.getMailbox().equals(event.getPos())){
							dis.setMailbox(null); dis.save();
						}
						return;
					}
					case "company": break;//TODO
					case "player":{
						UUID uuid = UUID.fromString(sign.getReceiver());
						PlayerCapability playercap = StateUtil.getPlayer(uuid, true);
						if(playercap.getMailbox() != null && playercap.getMailbox().equals(event.getPos())){
							playercap.setMailbox(null); playercap.save();
						}
						return;
					}
					case "central": case "fallback":{
						State state = StateUtil.getState(-1);
						if(state.getMailbox() != null && state.getMailbox().equals(event.getPos())){
							state.setMailbox(null); state.save();
						}
						return;
					}
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		return;
	}

	@SubscribeEvent
	public static void onBlockPlace(BlockEvent.PlaceEvent event){
		if(event.getWorld().isRemote || event.getPlayer().dimension != 0){ return; }
		if(!checkAccess(event.getWorld(), event.getPos(), event.getState(), event.getPlayer(), false)){
			Print.bar(event.getPlayer(), "No permission to place blocks here.");
			event.setCanceled(true);
		}
		return;
	}
	
	public static boolean checkAccess(World world, BlockPos pos, IBlockState state, EntityPlayer player, boolean interact){
		if(StateUtil.isAdmin(player)){ return true; }
		Chunk chunk = StateUtil.getChunk(pos);
		if(chunk.getDistrict().getId() < 0){
			if(chunk.getDistrict().getId() == -1){
				if(StConfig.ALLOW_WILDERNESS_ACCESS){
					chunk.setEdited(Time.getDate());
					return true;
				}
				return false;
			}
			else if(chunk.getDistrict().getId() == -2){
				if(chunk.getChanged() + Time.DAY_MS < Time.getDate()){
					chunk.setDistrict(StateUtil.getDistrict(-1));
					chunk.save();
					Print.chat(player, "Updating chunk...");
					return false;
				}
				if(pos.getY() > StConfig.TRANSIT_ZONE_BOTTOM_LIMIT && pos.getY() < StConfig.TRANSIT_ZONE_TOP_LIMIT){
					chunk.setEdited(Time.getDate());
					return true;
				}
				return false;
			}
			else{
				Print.chat(player, "Unknown district type.");
				return false;
			}
		}
		if(hp(chunk, player, interact)){
			chunk.setEdited(Time.getDate());
			return true;
		}
		return false;
	}
	
	private static boolean hp(Chunk chunk, EntityPlayer player, boolean interact){
		PlayerCapability cap = player.getCapability(StatesCapabilities.PLAYER, null);
		if(cap == null && (cap = StateUtil.getPlayer(player.getUniqueID(), true)) == null){
			return false;
		}
		else if(cap.getUUID() == null || cap.getMunicipality() == null){
			return false;
		}
		switch(chunk.getType()){
			case PRIVATE:{
				if(interact && chunk.interact() && cap.getMunicipality().getId() == chunk.getMunicipality().getId()) return true;
				return chunk.getOwner().equals(cap.getUUIDAsString()) || chunk.getPlayerWhitelist().contains(cap.getUUID()) || cap.isMayorOf(chunk.getMunicipality()) || cap.isStateLeaderOf(chunk.getState());
			}
			case NORMAL:{
				if(!interact && chunk.interact() && cap.getMunicipality().getId() == chunk.getMunicipality().getId()) return false;
				return cap.getMunicipality().getId() == chunk.getMunicipality().getId();
			}
			case DISTRICT:{
				if(interact && chunk.interact() && cap.getMunicipality().getId() == chunk.getMunicipality().getId()) return true;
				return cap.isDistrictManagerOf(chunk.getDistrict()) || cap.isMayorOf(chunk.getMunicipality()) || cap.isStateLeaderOf(chunk.getState());
			}
			case MUNICIPAL:{
				if(interact && chunk.interact() && cap.getMunicipality().getId() == chunk.getMunicipality().getId()) return true;
				return cap.isMayorOf(chunk.getMunicipality()) || cap.isStateLeaderOf(chunk.getState());
			}
			case STATEOWNED:{
				if(interact && chunk.interact() && cap.getState().getId() == chunk.getState().getId()) return true;
				return cap.isStateLeaderOf(chunk.getState());
			}
			case STATEPUBLIC:{
				if(!interact && chunk.interact() && cap.getState().getId() == chunk.getState().getId()) return false;
				return cap.getState().getId() == chunk.getState().getId();
			}
			case COMPANY: return false;//TODO
			case PUBLIC:{
				if(!interact && chunk.interact()) return false;
				return true;
			}
			default:{
				return false;
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onMessage(ServerChatEvent event){
		if(!StConfig.STATES_CHAT){
			MessageSender.toWebhook(event.getPlayer().getCapability(StatesCapabilities.PLAYER, null), event.getMessage());
			return;
		}
		//event.setCanceled(true); Static.getServer().addScheduledTask(() -> { Sender.sendAs(event.getPlayer(), event.getMessage()); });
		PlayerCapability cap = event.getPlayer().getCapability(StatesCapabilities.PLAYER, null);
		if(event.getComponent() instanceof TextComponentTranslation){
			TextComponentTranslation com = (TextComponentTranslation)event.getComponent();
			com.getFormatArgs()[0] = new TextComponentString(Formatter.format("&" + (StateUtil.isAdmin(event.getPlayer()) ? "4" : "6") + "#&8] " + cap.getFormattedNickname() + "&0:"));
			com.getFormatArgs()[1] = new TextComponentString(Formatter.format("&7" + ((ITextComponent)com.getFormatArgs()[1]).getUnformattedText()));
			event.setComponent(new TextComponentTranslation("states.chat.text", com.getFormatArgs()));
			MessageSender.toWebhook(cap, event.getMessage());
		}
		else if(event.getComponent() instanceof TextComponentString){
			TextComponentTranslation com = new TextComponentTranslation("states.chat.text", new Object[]{
				new TextComponentString(Formatter.format("&" + (StateUtil.isAdmin(event.getPlayer()) ? "4" : "6") + "#&8] " + cap.getFormattedNickname() + "&0:")),
				new TextComponentString(Formatter.format("&7" + event.getComponent().getUnformattedText()))
			}); event.setComponent(com); MessageSender.toWebhook(cap, event.getMessage());
		}
	}
	
	@SubscribeEvent
	public static void onTick(TickEvent.PlayerTickEvent event){
		if(event.player.world.isRemote || event.player.dimension != 0){ return; }
		PlayerCapability player = event.player.getCapability(StatesCapabilities.PLAYER, null);
		if(player != null && Time.getDate() > player.getLastPositionUpdate()){
			player.setPositionUpdate(Time.getDate());
			player.setCurrenkChunk(StateUtil.getChunk(event.player));
			//
			if(player.getCurrentChunk() == null || player.getLastChunk() == null){
				return;
			}
			if(player.getCurrentChunk().getDistrict() != player.getLastChunk().getDistrict()){
				Chunk chunk = player.getCurrentChunk();
				sendLocationUpdate(event.player, chunk, chunk.getDistrict().getMunicipality().getState().getName(), chunk.getDistrict().getMunicipality().getName(), chunk.getDistrict().getName(), 0);
			}
		}
	}
	
	public static void sendLocationUpdate(EntityPlayer player, Chunk chunk, String line0, String line1, String line2, int time){
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("target_listener", "states:gui");
		nbt.setString("task", "show_location_update");
		writeIcon(nbt, chunk == null ? "" : chunk.getDistrict().getMunicipality().getState().getIcon(), 0, "red");
		writeIcon(nbt, chunk == null ? "" : chunk.getDistrict().getMunicipality().getIcon(), 1, "green");
		writeIcon(nbt, chunk == null ? "" : chunk.getDistrict().getIcon(), 2, "blue");
		nbt.setString("line0", line0 == null ? " " : line0);
		nbt.setString("line1", line1 == null ? " " : line1);
		nbt.setString("line2", line2 == null ? " " : line2);
		if(time > 0){ nbt.setInteger("time", time); }
		PacketHandler.getInstance().sendTo(new PacketNBTTagCompound(nbt), (EntityPlayerMP)player);
	}
	
	private static final List<String> colours = Arrays.asList(new String[]{"green", "yellow", "red", "blue"});
	
	private static final void writeIcon(NBTTagCompound compound, String icon, int id, String color){
		if(icon != null && !icon.equals("")){
			if(colours.contains(icon)){
				compound.setString("color_" + id, icon);
			}
			else{
				compound.setString("icon_" + id, icon);
			}
		}
		else if(color == null){
			compound.setInteger("x_" + id, 64);
			compound.setInteger("y_" + id, 224);
		}
		else{
			compound.setString("color_" + id, color);
		}
	}

}
