package net.fexcraft.mod.states.guis;

import java.awt.Color;
import java.awt.image.BufferedImage;

import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.util.AccountManager;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.lib.api.network.IPacketListener;
import net.fexcraft.mod.lib.network.PacketHandler;
import net.fexcraft.mod.lib.network.packet.PacketNBTTagCompound;
import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.lib.util.common.Static;
import net.fexcraft.mod.lib.util.math.Time;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.api.District;
import net.fexcraft.mod.states.api.MunicipalityType;
import net.fexcraft.mod.states.api.capabilities.StatesCapabilities;
import net.fexcraft.mod.states.packets.ImagePacket;
import net.fexcraft.mod.states.util.ImageCache;
import net.fexcraft.mod.states.util.StateLogger;
import net.fexcraft.mod.states.util.StateUtil;
import net.fexcraft.mod.states.util.StatesPermissions;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Listener implements IPacketListener<PacketNBTTagCompound> {

	@Override
	public String getId(){
		return "states:gui";
	}
	
	public static final String[] MAP_VIEW_MODES = new String[]{ "none", "districts", "municipalities", "states", "chunk_types", "chunk_properties"};

	@Override
	public void process(PacketNBTTagCompound packet, Object[] objs){
		Print.debug(packet.nbt.toString(), objs);
		EntityPlayerMP player = (EntityPlayerMP)objs[0];
		World world = player.world;
		BlockPos pos = player.getPosition();
		switch(packet.nbt.getInteger("from")){
			case 0:{
				player.openGui(States.INSTANCE, packet.nbt.getInteger("button") + 1, world, pos.getX(), pos.getY(), pos.getZ());
				return;
			}
			case 1:{
				if(packet.nbt.hasKey("terrain")){
					BufferedImage image = ImageCache.getImage(packet.nbt.getInteger("x"), packet.nbt.getInteger("z"), true);
					PacketHandler.getInstance().sendTo(new ImagePacket("area_view", image), player);
				}
				if(packet.nbt.hasKey("cmd")){
					Static.getServer().commandManager.executeCommand(player, "/ck info " + packet.nbt.getInteger("x") + " " + packet.nbt.getInteger("z"));
					return;
				}
				NBTTagList list = new NBTTagList();
				NBTTagCompound namelist = new NBTTagCompound();
				int x = packet.nbt.getInteger("x"); int z = packet.nbt.getInteger("z");
				int mode = packet.nbt.getInteger("mode");
				if(mode > 0 && mode < MAP_VIEW_MODES.length){
					for(int i = 0; i < 32; i++){
						for(int j = 0; j < 32; j++){
							NBTTagCompound compound = new NBTTagCompound();
							Chunk chunk = StateUtil.getTempChunk(i + (x * 32), j + (z * 32));
							if(chunk != null){
								switch(mode){
									case 0: { break; }
									case 1: {
										compound.setInteger("color", Color.decode(chunk.getDistrict().getColor()).getRGB());
										compound.setInteger("district", chunk.getDistrict().getId());
										if(!namelist.hasKey("district:" + chunk.getDistrict().getId())){
											namelist.setString("district:" + chunk.getDistrict().getId(), chunk.getDistrict().getName());
										}
										break;
									}
									case 2:{
										compound.setInteger("color", Color.decode(chunk.getDistrict().getMunicipality().getColor()).getRGB());
										compound.setInteger("municipality", chunk.getDistrict().getMunicipality().getId());
										if(!namelist.hasKey("municipality:" + chunk.getDistrict().getMunicipality().getId())){
											namelist.setString("municipality:" + chunk.getDistrict().getMunicipality().getId(), chunk.getDistrict().getMunicipality().getName());
										}
										break;
									}
									case 3:{
										compound.setInteger("color", Color.decode(chunk.getDistrict().getMunicipality().getState().getColor()).getRGB());
										compound.setInteger("state", chunk.getDistrict().getMunicipality().getState().getId());
										if(!namelist.hasKey("state:" + chunk.getDistrict().getMunicipality().getState().getId())){
											namelist.setString("state:" + chunk.getDistrict().getMunicipality().getState().getId(), chunk.getDistrict().getMunicipality().getState().getName());
										}
										break;
									}
									case 4:{
										compound.setInteger("color", Color.decode(chunk.getType().getColor()).getRGB());
										compound.setString("type", chunk.getType().name().toLowerCase());
										break;
									}
									case 5:{
										compound.setInteger("color", Color.white.getRGB());
										break;
									}
								}
								compound.setInteger("x", chunk.xCoord());
								compound.setInteger("z", chunk.zCoord());
								compound.setBoolean("linked", chunk.getLink() != null);
								if(compound.getBoolean("linked")){
									compound.setIntArray("link", chunk.getLink());
								}
								compound.setBoolean("owned", chunk.getOwner() != null && !chunk.getOwner().equals("null"));
								if(compound.getBoolean("owned")){
									compound.setString("owner", chunk.getOwner());
								}
								compound.setLong("price", chunk.getPrice());
							}
							else{
								compound.setInteger("color", Color.BLACK.getRGB());
							}
							list.appendTag(compound);
						}
					}
				}
				NBTTagCompound compound = new NBTTagCompound();
				compound.setString("target_listener", "states:gui");
				compound.setString("task", "area_view_list");
				compound.setTag("list", list);
				compound.setTag("namelist", namelist);
				PacketHandler.getInstance().sendTo(new PacketNBTTagCompound(compound), player);
				return;
			}
			case 10:{
				switch(packet.nbt.getString("request")){
					case "get_map":{
						Chunk chunk = world.getChunkFromBlockCoords(pos).getCapability(StatesCapabilities.CHUNK, null).getStatesChunk();
						if(chunk == null){ return; }
						NBTTagList list = new NBTTagList();
						for(int i = -5; i < 6; i++){
							for(int j = -5; j < 6; j++){
								net.minecraft.world.chunk.Chunk ch = world.getChunkFromChunkCoords(chunk.xCoord() + i, chunk.zCoord() + j);
								Chunk ck = ch == null ? null : ch.getCapability(StatesCapabilities.CHUNK, null).getStatesChunk();
								Print.debug(ch, ck);
								NBTTagCompound compound = new NBTTagCompound();
								if(ck == null){
									compound.setInteger("color", Color.BLACK.getRGB());
									compound.setBoolean("claimable", false);
									compound.setInteger("district", -1);
									compound.setInteger("x", chunk.xCoord() + i);
									compound.setInteger("z", chunk.zCoord() + j);
									compound.setBoolean("linked", false);
									compound.setBoolean("owned", false);
									compound.setLong("price", 0l);
								}
								else{
									compound.setInteger("color", Color.decode(ck.getDistrict().getColor()).getRGB());
									compound.setBoolean("claimable", ck.getDistrict().getId() < 0);
									compound.setInteger("district", ck.getDistrict().getId());
									compound.setInteger("x", ck.xCoord());
									compound.setInteger("z", ck.zCoord());
									compound.setBoolean("linked", ck.getLink() != null);
									if(compound.getBoolean("linked")){
										compound.setIntArray("link", ck.getLink());
									}
									compound.setBoolean("owned", ck.getOwner() != null && !ck.getOwner().equals("null"));
									if(compound.getBoolean("owned")){
										compound.setString("owner", ck.getOwner());
									}
									compound.setLong("price", ck.getPrice());
								}
								list.appendTag(compound);
							}
						}
						NBTTagCompound compound = new NBTTagCompound();
						compound.setTag("array", list);
						compound.setString("target_listener", "states:gui");
						compound.setString("task", "get_map_return");
						//compound.setBoolean("perm", StatesPermissions.hasPermission(player, "chunk.claim", chunk));
						PacketHandler.getInstance().sendTo(new PacketNBTTagCompound(compound), player);
						break;
					}
					case "claim":{
						District dis = StateUtil.getDistrict(packet.nbt.getIntArray("data")[0]);
						NBTTagCompound compound = tryClaim(player, world, dis, packet.nbt, new NBTTagCompound());
						if(compound == null){ return; }
						compound.setInteger("x", packet.nbt.getIntArray("data")[1]);
						compound.setInteger("z", packet.nbt.getIntArray("data")[2]);
						compound.setString("target_listener", "states:gui");
						compound.setString("task", "claim_return");
						PacketHandler.getInstance().sendTo(new PacketNBTTagCompound(compound), player);
						break;
					}
				}
				return;
			}
			default: return;
		}
	}

	private NBTTagCompound tryClaim(EntityPlayerMP player, World world, District dis, NBTTagCompound nbt, NBTTagCompound compound){
		int mode = nbt.getIntArray("data")[3];
		if(dis == null || dis.getId() == -1){
			compound.setString("result", "District not found.");
			return compound;
		}
		if(dis.getId() == -2 && !net.fexcraft.mod.states.util.Config.ALLOW_TRANSIT_ZONES){
			compound.setString("result", "Transit Zones (-2) are disabled.");
			return compound;
		}
		if(dis.getMunicipality().getClaimedChunks() + 1 > MunicipalityType.getChunkLimitFor(dis.getMunicipality())){
			compound.setString("result", "Municipality reached the Chunk Limit.");
			return compound;
		}
		net.minecraft.world.chunk.Chunk ch = world.getChunkFromChunkCoords(nbt.getIntArray("data")[1], nbt.getIntArray("data")[2]);
		if(ch == null){
			compound.setString("result", "Chunk isn't loaded.");
			return compound;
		}
		Chunk ck = ch.getCapability(StatesCapabilities.CHUNK, null).getStatesChunk();
		if(ck == null){
			compound.setString("result", "Chunk data not found.");
			return compound;
		}
		if(dis.getId() != -2 && !StatesPermissions.hasPermission(player, "chunk.claim", dis)){
			compound.setString("result", "No permission. (0)");
			return compound;
		}
		if(mode == 1 && !StatesPermissions.hasPermission(player, "chunk.claim", ck.getDistrict())){
			compound.setString("result", "No permission. (1)");
			return compound;
		}
		if(mode == 0 && dis.getId() != -2 && dis.getMunicipality().getAccount().getBalance() < ck.getPrice()){
			Print.chat(player, "&7Municipality does not have enough money to claim this chunk.");
			Print.chat(player, "&7Required: &9" + Config.getWorthAsString(ck.getPrice()) + " &8|| &7Available: &9" + Config.getWorthAsString(dis.getMunicipality().getAccount().getBalance()));
			return null;
		}
		if(mode == 0){
			if(ck.getDistrict().getId() < 0){
				if(dis.getId() != -2 && !nearbyChunkSame(ck, dis)){
					compound.setString("result", "No nearby/connected chunks are of the selected district.");
					return compound;
				}
				else{
					if(ck.getPrice() > 0){
						if(dis.getId() != -2){
							if(!AccountManager.INSTANCE.getBank(dis.getMunicipality().getAccount().getBankId()).processTransfer(player, dis.getMunicipality().getAccount(), ck.getPrice(), States.SERVERACCOUNT)){
								return null;
							}
						}
						else{
							Account playeracc = player.getCapability(StatesCapabilities.PLAYER, null).getAccount();
							if(!AccountManager.INSTANCE.getBank(playeracc.getBankId()).processTransfer(player, playeracc, ck.getPrice() / 10, States.SERVERACCOUNT)){
								return null;
							}
						}
					}
					ck.setDistrict(dis);
					ck.setClaimer(player.getGameProfile().getId());
					ck.setChanged(Time.getDate());
					if(dis.getId() != -2){
						ck.setPrice(0);
					}
					ck.save();
					updateNeighbors(ck);
					//
					StateLogger.log(StateLogger.LoggerType.CHUNK, StateLogger.player(player) + " claimed " + StateLogger.chunk(ck) + ", it is now part of " + StateLogger.district(dis) + ".");
					ImageCache.update(world, ch);
					compound.setString("result", "Chunk Claimed. (" + dis.getId() + ");");
					compound.setBoolean("claimed", true);
					compound.setInteger("color", Color.decode(ck.getDistrict().getColor()).getRGB());
					compound.setBoolean("owned", ck.getOwner() != null && !ck.getOwner().equals("null"));
					if(compound.getBoolean("owned")){
						compound.setString("owner", ck.getOwner());
					}
					compound.setLong("price", ck.getPrice());
				}
			}
			else{
				compound.setString("result", "Chunk is already claimed.");
			}
			return compound;
		}
		if(mode == 1){
			if(ck.getDistrict().getMunicipality().getId() != dis.getMunicipality().getId()){
				compound.setString("result", "Districts are not of the same Municipality.");
				return compound;
			}
			if(!nearbyChunkSame(ck, dis)){
				compound.setString("result", "No nearby/connected chunks are of the selected district.");
				return compound;
			}
			if(ck.getLink() != null){
				compound.setString("result", "Chunk is linked to another chunk! Claim the main-chunk instead.");
				return compound;
			}
			ck.setDistrict(dis);
			ck.setClaimer(player.getGameProfile().getId());
			ck.setChanged(Time.getDate());
			ck.save();
			StateLogger.log(StateLogger.LoggerType.CHUNK, StateLogger.player(player) + " re-claimed " + StateLogger.chunk(ck) + ", it is now part of " + StateLogger.district(dis) + ".");
			ImageCache.update(world, ch);
			if(ck.getLinkedChunks().size() > 0){
				for(ResourceLocation loc : ck.getLinkedChunks()){
					Chunk chunk = StateUtil.getTempChunk(loc);
					if(chunk != null){
						chunk.setDistrict(dis);
						chunk.setClaimer(player.getGameProfile().getId());
						chunk.setChanged(Time.getDate());
						chunk.save();
						ImageCache.update(world, world.getChunkFromChunkCoords(chunk.xCoord(), chunk.zCoord()));
					}
				}
			}
			compound.setString("result", "Chunk Reclaimed. (" + dis.getId() + ");");
			compound.setBoolean("claimed", true);
			compound.setInteger("color", Color.decode(ck.getDistrict().getColor()).getRGB());
			compound.setBoolean("owned", ck.getOwner() != null && !ck.getOwner().equals("null"));
			if(compound.getBoolean("owned")){
				compound.setString("owner", ck.getOwner());
			}
			compound.setLong("price", ck.getPrice());
			return compound;
		}
		return null;
	}
	
	private static final int[][] coords = new int[4][2];
	static {
		coords[0] = new int[]{  1,  0 };
		coords[1] = new int[]{ -1,  0 };
		coords[2] = new int[]{  0,  1 };
		coords[3] = new int[]{  0, -1 };
	}

	private void updateNeighbors(Chunk ck){
		Chunk chunk = null;
		for(int[] cor : coords){
			chunk = StateUtil.getChunk(ck.xCoord() + cor[0], ck.zCoord() + cor[1]);
			if(chunk != null && chunk.getDistrict().getId() >= 0 && chunk.getDistrict().getId() != ck.getDistrict().getId()){
				if(!ck.getDistrict().getNeighbors().contains(chunk.getDistrict().getId())){
					ck.getDistrict().getNeighbors().add(chunk.getDistrict().getId());
					ck.getDistrict().save();
					StateLogger.log(StateLogger.LoggerType.DISRICT, "Added " + StateLogger.district(chunk.getDistrict()) + " to NeighborList of " + StateLogger.district(ck.getDistrict()) + ".");
				}
				if(!chunk.getDistrict().getNeighbors().contains(ck.getDistrict().getId())){
					chunk.getDistrict().getNeighbors().add(ck.getDistrict().getId());
					chunk.getDistrict().save();
					StateLogger.log(StateLogger.LoggerType.DISRICT, "Added " + StateLogger.district(ck.getDistrict()) + " to NeighborList of " + StateLogger.district(chunk.getDistrict()) + ".");
				}
				if(chunk.getMunicipality().getId() >= 0 && chunk.getMunicipality().getId() != ck.getMunicipality().getId()){
					if(!ck.getMunicipality().getNeighbors().contains(chunk.getMunicipality().getId())){
						ck.getMunicipality().getNeighbors().add(chunk.getMunicipality().getId());
						ck.getMunicipality().save();
						StateLogger.log(StateLogger.LoggerType.MUNICIPALITY, "Added " + StateLogger.municipality(chunk.getMunicipality()) + " to NeighborList of " + StateLogger.municipality(ck.getMunicipality()) + ".");
					}
					if(!chunk.getMunicipality().getNeighbors().contains(ck.getMunicipality().getId())){
						chunk.getMunicipality().getNeighbors().add(ck.getMunicipality().getId());
						chunk.getMunicipality().save();
						StateLogger.log(StateLogger.LoggerType.MUNICIPALITY, "Added " + StateLogger.municipality(ck.getMunicipality()) + " to NeighborList of " + StateLogger.municipality(chunk.getMunicipality()) + ".");
					}
					if(chunk.getDistrict().getMunicipality().getState().getId() >= 0 && chunk.getDistrict().getMunicipality().getState().getId() != ck.getDistrict().getMunicipality().getState().getId()){
						if(!ck.getState().getNeighbors().contains(chunk.getState().getId())){
							ck.getState().getNeighbors().add(chunk.getState().getId());
							ck.getState().save();
							StateLogger.log(StateLogger.LoggerType.STATE, "Added " + StateLogger.state(chunk.getState()) + " to NeighborList of " + StateLogger.state(ck.getState()) + ".");
						}
						if(!chunk.getState().getNeighbors().contains(ck.getState().getId())){
							chunk.getState().getNeighbors().add(ck.getState().getId());
							chunk.getState().save();
							StateLogger.log(StateLogger.LoggerType.STATE, "Added " + StateLogger.state(ck.getState()) + " to NeighborList of " + StateLogger.state(chunk.getState()) + ".");
						}
					}
				}
			}
		}
	}

	private boolean nearbyChunkSame(Chunk ck, District dis){
		Chunk chunk = null;
		for(int[] cor : coords){
			chunk = StateUtil.getChunk(ck.xCoord() + cor[0], ck.zCoord() + cor[1]);
			if(chunk != null && chunk.getDistrict().getId() == dis.getId()){
				return true;
			}
		}
		return false;
	}

	/*private void sendBack(NBTTagCompound nbt, EntityPlayerMP player){
		PacketHandler.getInstance().sendTo(new PacketNBTTagCompound(nbt), player);
	}*/

}
