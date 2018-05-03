package net.fexcraft.mod.states.guis;

import java.awt.Color;
import java.awt.image.BufferedImage;

import net.fexcraft.mod.fsmm.util.AccountManager;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.lib.api.network.IPacketListener;
import net.fexcraft.mod.lib.network.PacketHandler;
import net.fexcraft.mod.lib.network.packet.PacketNBTTagCompound;
import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.lib.util.math.Time;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.api.District;
import net.fexcraft.mod.states.api.capabilities.StatesCapabilities;
import net.fexcraft.mod.states.packets.ImagePacket;
import net.fexcraft.mod.states.util.ImageCache;
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
				BufferedImage image = ImageCache.getImage(packet.nbt.getInteger("chunk_x"), packet.nbt.getInteger("chunk_z"), packet.nbt.getString("view"), true);
				PacketHandler.getInstance().sendTo(new ImagePacket("area_view", image), player);
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
		if(!StatesPermissions.hasPermission(player, "chunk.claim", dis)){
			compound.setString("result", "No permission. (0)");
			return compound;
		}
		if(mode == 1 && !StatesPermissions.hasPermission(player, "chunk.claim", ck.getDistrict())){
			compound.setString("result", "No permission. (1)");
			return compound;
		}
		if(mode == 0 && dis.getMunicipality().getAccount().getBalance() < ck.getPrice()){
			Print.chat(player, "&7Municipality does not have enough money to claim this chunk.");
			Print.chat(player, "&7Required: &9" + Config.getWorthAsString(ck.getPrice()) + " &8|| &7Available: &9" + Config.getWorthAsString(dis.getMunicipality().getAccount().getBalance()));
			return null;
		}
		if(mode == 0){
			if(ck.getDistrict().getId() < 0){
				if(!nearbyChunkSame(ck, dis)){
					compound.setString("result", "No nearby/connected chunks are of the selected district.");
					return compound;
				}
				else{
					if(ck.getPrice() > 0 && !AccountManager.INSTANCE.getBank(dis.getMunicipality().getAccount().getBankId()).processTransfer(player, dis.getMunicipality().getAccount(), ck.getPrice(), States.SERVERACCOUNT)){
						return null;
					}
					ck.setDistrict(dis);
					ck.setClaimer(player.getGameProfile().getId());
					ck.setChanged(Time.getDate());
					ck.setPrice(0);
					ck.save();
					ImageCache.update(world, ch, "claim", "all");
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
			ImageCache.update(world, ch, "claim", "all");
			if(ck.getLinkedChunks().size() > 0){
				for(ResourceLocation loc : ck.getLinkedChunks()){
					Chunk chunk = StateUtil.getTempChunk(loc);
					if(chunk != null){
						chunk.setDistrict(dis);
						chunk.setClaimer(player.getGameProfile().getId());
						chunk.setChanged(Time.getDate());
						chunk.save();
						ImageCache.update(world, world.getChunkFromChunkCoords(chunk.xCoord(), chunk.zCoord()), "claim", "all");
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

	private boolean nearbyChunkSame(Chunk ck, District dis){
		Chunk chunk = null;
		if((chunk = StateUtil.getChunk(ck.xCoord() + 1, ck.zCoord())) != null && chunk.getDistrict().getId() == dis.getId()){
			return true;
		}
		if((chunk = StateUtil.getChunk(ck.xCoord() - 1, ck.zCoord())) != null && chunk.getDistrict().getId() == dis.getId()){
			return true;
		}
		if((chunk = StateUtil.getChunk(ck.xCoord(), ck.zCoord() + 1)) != null && chunk.getDistrict().getId() == dis.getId()){
			return true;
		}
		if((chunk = StateUtil.getChunk(ck.xCoord(), ck.zCoord() - 1)) != null && chunk.getDistrict().getId() == dis.getId()){
			return true;
		}
		return false;
	}

	/*private void sendBack(NBTTagCompound nbt, EntityPlayerMP player){
		PacketHandler.getInstance().sendTo(new PacketNBTTagCompound(nbt), player);
	}*/

}
