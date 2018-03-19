package net.fexcraft.mod.states.events;

import net.fexcraft.mod.lib.api.common.LockableObject;
import net.fexcraft.mod.lib.perms.PermManager;
import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.api.Player;
import net.fexcraft.mod.states.impl.GenericPlayer;
import net.fexcraft.mod.states.impl.capabilities.TESCapability;
import net.fexcraft.mod.states.util.Config;
import net.fexcraft.mod.states.util.StateUtil;
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
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;

@Mod.EventBusSubscriber
public class PlayerEvents {
	
	@SubscribeEvent
	public static void onLogin(PlayerLoggedInEvent event){
		Player player = PermManager.getPlayerPerms(event.player).getAdditionalData(GenericPlayer.class);
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
	}
	
	@SubscribeEvent
	public static void onLogout(PlayerLoggedOutEvent event){
		States.PLAYERS.remove(event.player.getGameProfile().getId());
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
			TESCapability cap = te_sign.getCapability(TESCapability.CAPINJ, null);
			if(te_sign.signText[0].getUnformattedText().equalsIgnoreCase("[States]")){
				if(cap != null){ cap.setup(chunk); }
			}
			else if(cap != null && cap.isStatesSign()){
				cap.onPlayerInteract(chunk, event.getEntityPlayer());
			}
			else return;
		}
		else if(state.getBlock() instanceof LockableObject || (state.getBlock() instanceof ITileEntityProvider && event.getWorld().getTileEntity(event.getPos()) instanceof LockableObject)){
			/*LockableObject obj = state.getBlock() instanceof LockableObject ? (LockableObject)state.getBlock() : (LockableObject)te;
			if(event.getItemStack().getItem() instanceof KeyItem){
				if(obj.isLocked()){
					obj.unlock(event.getWorld(), event.getEntityPlayer(), event.getItemStack(), (KeyItem)event.getItemStack().getItem());
				}
				else{
					obj.lock(event.getWorld(), event.getEntityPlayer(), event.getItemStack(), (KeyItem)event.getItemStack().getItem());
				}
			}
			else{
				if(obj.isLocked()){
					event.setCanceled(true);
					Print.chat(event.getEntityPlayer(), "This Block is locked.");
				}
			}*/
			//Actually this should be obsolete as the Item should check this.
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
	
	//@SubscribeEvent
	public static void onBlockBreak(BlockEvent.BreakEvent event){
		if(!checkAccess(event.getWorld(), event.getPos(), event.getState(), event.getPlayer())){
			Print.chat(event.getPlayer(), "No permission to break blocks here.");
			event.setCanceled(true);
		}
		return;
	}
	
	private static boolean checkAccess(World world, BlockPos pos, IBlockState state, EntityPlayer player){
		Player pl = StateUtil.getPlayer(player);
		if(pl.getPermissions().hasPermission(States.ADMIN_PERM)){
			return false;
		}
		Chunk chunk = StateUtil.getChunk(world, pos);
		if(chunk.getDistrict().getId() == -1){
			return Config.ALLOW_WILDERNESS_ACCESS;
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

	//@SubscribeEvent
	public static void onBlockPlace(BlockEvent.PlaceEvent event){
		if(!checkAccess(event.getWorld(), event.getPos(), event.getState(), event.getPlayer())){
			Print.chat(event.getPlayer(), "No permission to place blocks here.");
			event.setCanceled(true);
		}
		return;
	}

}
