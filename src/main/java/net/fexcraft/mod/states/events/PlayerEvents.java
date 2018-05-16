package net.fexcraft.mod.states.events;

import java.util.Arrays;
import java.util.List;

import net.fexcraft.mod.lib.network.PacketHandler;
import net.fexcraft.mod.lib.network.packet.PacketNBTTagCompound;
import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.lib.util.math.Time;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.api.capabilities.PlayerCapability;
import net.fexcraft.mod.states.api.capabilities.SignTileEntityCapability;
import net.fexcraft.mod.states.api.capabilities.StatesCapabilities;
import net.fexcraft.mod.states.util.Config;
import net.fexcraft.mod.states.util.Sender;
import net.fexcraft.mod.states.util.StateUtil;
import net.fexcraft.mod.states.util.UpdateHandler;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockDropper;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.BlockHopper;
import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockPressurePlate;
import net.minecraft.block.BlockRedstoneComparator;
import net.minecraft.block.BlockRedstoneRepeater;
import net.minecraft.block.BlockSign;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
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
		States.PLAYERS.put(event.player.getGameProfile().getId(), player);
		Print.chat(event.player, "&e====-====-====-====-====-====&0[&2States&0]");
		Print.chat(event.player, "&6Welcome back " + player.getFormattedNickname(event.player) + "&6!");//TODO replace with nick
		if(event.player.dimension != 0){
			Print.chat(event.player, "&2You are currently in &7DIM($0)&0.".replace("$0", event.player.dimension + ""));
		}
		else{
			Chunk chunk = StateUtil.getChunk(event.player);
			Print.chat(event.player, "&2You are currently in a district called &7$0&2.".replace("$0", chunk.getDistrict().getName()));
			Print.chat(event.player, "&2Which is part of the &6$1 &7$0&2.".replace("$0", chunk.getDistrict().getMunicipality().getName()).replace("$1", chunk.getDistrict().getMunicipality().getType().getTitle()));
			Print.chat(event.player, "&2In the State of &7$0&2.".replace("$0", chunk.getDistrict().getMunicipality().getState().getName()));
		}
		int i = StateUtil.getUnreadMailsOf("player", event.player.getGameProfile().getId().toString());
		Print.chat(event.player, "&6You have &7" + (i <= 0 ? "no" : i) + "&6 new mail" + (i == 1 ? "" : "s") + ".");
		Print.chat(event.player, "&e====-====-====-====-====-====&0[&2States&0]");
		sendLocationUpdate(event.player, null, "&6Welcome back " + player.getFormattedNickname(event.player) + "&6!", "", "", 3);
		if(UpdateHandler.STATE != null){
			Print.chat(event.player, UpdateHandler.STATE);
		}
		//
		Sender.sendToWebhook(null, event.player.getGameProfile().getName() + " joined.");
	}
	
	@SubscribeEvent
	public static void onLogout(PlayerLoggedOutEvent event){
		States.PLAYERS.remove(event.player.getGameProfile().getId());
		Sender.sendToWebhook(null, event.player.getGameProfile().getName() + " left.");
	}
	
	@SubscribeEvent
	public static void onInteract(PlayerInteractEvent event){
		if(event.getWorld().isRemote || event.getEntityPlayer().getActiveHand() == EnumHand.OFF_HAND){
			return;
		}
		IBlockState state = event.getWorld().getBlockState(event.getPos());
		if(state.getBlock() instanceof BlockSign){
			//IBlockState state = event.getWorld().getBlockState(event.getPos());
			TileEntitySign te_sign = (TileEntitySign)event.getWorld().getTileEntity(event.getPos());
			if(te_sign == null || te_sign.signText == null || te_sign.signText[0] == null){
				return;
			}
			Chunk chunk = StateUtil.getChunk(event.getWorld(), event.getPos());
			SignTileEntityCapability cap = te_sign.getCapability(StatesCapabilities.SIGN_TE, null);
			if(te_sign.signText[0].getUnformattedText().equalsIgnoreCase("[States]")){
				if(cap != null){ cap.setup(chunk); }
			}
			else if(cap != null && cap.isStatesSign()){
				cap.onPlayerInteract(chunk, event.getEntityPlayer());
			}
			else return;
		}
		else if(state.getBlock() instanceof BlockChest || state.getBlock() instanceof BlockFurnace
				|| state.getBlock() instanceof BlockHopper || state.getBlock() instanceof BlockDispenser
				|| state.getBlock() instanceof BlockDropper || state.getBlock() instanceof BlockLever
				|| state.getBlock() instanceof BlockButton || state.getBlock() instanceof BlockPressurePlate
				|| state.getBlock() instanceof BlockRedstoneRepeater || state.getBlock() instanceof BlockRedstoneComparator){
			if(!checkAccess(event.getWorld(), event.getPos(), state, event.getEntityPlayer())){
				Print.chat(event.getEntityPlayer(), "No permission to interact with these blocks here.");
				event.setCanceled(true);
				return;
			}
		}
		else return;
	}
	
	@SubscribeEvent
	public static void onBlockBreak(BlockEvent.BreakEvent event){
		if(!checkAccess(event.getWorld(), event.getPos(), event.getState(), event.getPlayer())){
			Print.bar(event.getPlayer(), "No permission to break blocks here.");
			event.setCanceled(true);
		}
		return;
	}

	@SubscribeEvent
	public static void onBlockPlace(BlockEvent.PlaceEvent event){
		if(!checkAccess(event.getWorld(), event.getPos(), event.getState(), event.getPlayer())){
			Print.bar(event.getPlayer(), "No permission to place blocks here.");
			event.setCanceled(true);
		}
		return;
	}
	
	public static boolean checkAccess(World world, BlockPos pos, IBlockState state, EntityPlayer player){
            if(world.provider.getDimension() != 0){ return true; }
            PlayerCapability pl = player.getCapability(StatesCapabilities.PLAYER, null);
            if(pl.getPermissions().hasPermission(States.ADMIN_PERM)){
		return true;
            }
            Chunk chunk = world.getChunkFromBlockCoords(pos).getCapability(StatesCapabilities.CHUNK, null).getStatesChunk();
            if(chunk.getDistrict().getId() < 0){
                if(chunk.getDistrict().getId() == -1){
                    return Config.ALLOW_WILDERNESS_ACCESS;
                }
                else if(chunk.getDistrict().getId() == -2){
                    if(chunk.getChanged() + Time.DAY_MS < Time.getDate()){
                        chunk.setDistrict(StateUtil.getDistrict(-1));
                        //TODO log
                        chunk.save();
                        Print.chat(player, "Updating chunk...");
                            return false;
                        }
                        return pos.getY() > Config.TRANSIT_ZONE_BOTTOM_LIMIT && pos.getY() < Config.TRANSIT_ZONE_TOP_LIMIT;
                }
                else{
                    Print.chat(player, "Unknown district type.");
                    return false;
                }
            }
            //TODO company check
            switch(chunk.getType()){
                case PRIVATE:{
                    return chunk.getOwner().equals(pl.getUUIDAsString()) || chunk.getPlayerWhitelist().contains(pl.getUUID()) || pl.isMayorOf(chunk.getDistrict().getMunicipality()) || pl.isStateLeaderOf(chunk.getDistrict().getMunicipality().getState());
                }
                case NORMAL:{
                    return pl.getMunicipality().getId() == chunk.getDistrict().getMunicipality().getId();
                }
                case DISTRICT:{
                    return pl.isDistrictManagerOf(chunk.getDistrict()) || pl.isMayorOf(chunk.getDistrict().getMunicipality()) || pl.isStateLeaderOf(chunk.getDistrict().getMunicipality().getState());
                }
                case MUNICIPAL:{
                    return pl.isMayorOf(chunk.getDistrict().getMunicipality()) || pl.isStateLeaderOf(chunk.getDistrict().getMunicipality().getState());
                }
                case STATEOWNED:{
                    return pl.isStateLeaderOf(chunk.getDistrict().getMunicipality().getState());
                }
                case COMPANY: return false;//TODO
                case PUBLIC: return true;
                default:{
                    return false;
                }
            }
	}
	
	@SubscribeEvent
	public static void onMessage(ServerChatEvent event){
            event.setCanceled(true);
            Sender.sendAs(event.getPlayer(), event.getMessage());
	}
	
	@SubscribeEvent
	public static void onTick(TickEvent.PlayerTickEvent event){
		if(event.player.world.isRemote){ return; }
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
