package net.fexcraft.mod.states.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import org.apache.commons.lang3.math.NumberUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.fexcraft.lib.common.json.JsonUtil;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.LoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

public class ForcedChunksManager implements LoadingCallback {
	
	//TODO someday add a way to bypass force's chunk limit per ticket.
	private static ArrayList<Ticket> tickets = new ArrayList<>();
	
	public static void check(){
		if(Config.LOADED_CHUNKS_PER_MUNICIPALITY <= 0){ return; }
		//if(tickets.isEmpty()){ return; }
		States.LOADED_CHUNKS.entrySet().forEach(entry -> {
			entry.getValue().forEach(pos -> {
				if(getTicketForChunk(pos) == null){
					ForgeChunkManager.forceChunk(getFreeTicket(), pos);
				}
			});
		});
		for(Ticket ticket : tickets){
			for(net.minecraft.util.math.ChunkPos pos : ticket.getChunkList()){
				boolean found = false;
				for(Collection<ChunkPos> cks : States.LOADED_CHUNKS.values()){
					for(ChunkPos ckp : cks){ if(ckp.equals(pos)){ found = true; break; } }
				}
				if(!found){
					ForgeChunkManager.unforceChunk(ticket, pos);
				}
			}
		}
		for(Ticket ticket : tickets){
			if(ticket.getChunkList().size() == 0){
				ForgeChunkManager.releaseTicket(ticket);
			}
		}
	}

	public static boolean isChunkLoaded(ChunkPos pos){
		return getTicketForChunk(pos) != null;
	}

	public static void load(){
		if(Config.LOADED_CHUNKS_PER_MUNICIPALITY <= 0){ return; }
		JsonObject obj = JsonUtil.get(new File(States.getSaveDirectory(), "forced_chunks.json"));
		if(obj == null || obj.entrySet().isEmpty()){
			States.LOADED_CHUNKS.clear();
		}
		else{
			States.LOADED_CHUNKS.clear();
			obj.entrySet().forEach(entry -> {
				if(entry.getValue().isJsonArray() && NumberUtils.isCreatable(entry.getKey())){
					int i = Integer.parseInt(entry.getKey());
					ArrayList<ChunkPos> list = new ArrayList<>();
					for(JsonElement elm : entry.getValue().getAsJsonArray()){
						try{
							int x = elm.getAsJsonObject().get("x").getAsInt();
							int z = elm.getAsJsonObject().get("z").getAsInt();
							list.add(new ChunkPos(x, z));
						}
						catch(Exception e){ e.printStackTrace(); }
					}
					States.LOADED_CHUNKS.put(i, list);
				}
			});
		}
	}

	public static void unload(){
		if(Config.LOADED_CHUNKS_PER_MUNICIPALITY <= 0){ return; }
		//if(States.LOADED_CHUNKS.size() <= 0){ return; }
		JsonObject obj = new JsonObject();
		for(Entry<Integer, List<ChunkPos>> entry : States.LOADED_CHUNKS.entrySet()){
			JsonArray array = new JsonArray();
			for(ChunkPos pos : entry.getValue()){
				JsonObject jsn = new JsonObject(); jsn.addProperty("x", pos.x); jsn.addProperty("z", pos.z); array.add(jsn);
			}
			obj.add(entry.getKey() + "", array);
		}
		JsonUtil.write(new File(States.getSaveDirectory(), "forced_chunks.json"), obj, true);
	}

	@Override
	public void ticketsLoaded(List<Ticket> tickets, World world){
		//
	}
	
	private static @Nullable Ticket requestTicket(){
		Ticket ticket = ForgeChunkManager.requestTicket(States.INSTANCE, Static.getServer().worlds[0], ForgeChunkManager.Type.NORMAL);
		if(ticket != null){ tickets.add(ticket); } return ticket;
	}
	
	public static @Nullable Ticket getTicketForChunk(ChunkPos pos){
		for(Ticket ticket : tickets){
			for(net.minecraft.util.math.ChunkPos ckp : ticket.getChunkList()){
				if(pos.equals(ckp)){ return ticket; }
			}
		}
		return null;
	}
	
	public static @Nullable Ticket getFreeTicket(){
		if(tickets.isEmpty()){
			return requestTicket();
		}
		int i = chunksPerTicket();
		for(Ticket ticket : tickets){
			if(ticket.getChunkList().size() < i){
				return ticket;
			}
		}
		return requestTicket();
	}
	
	public static long getLoadedChunksInTickets(){
		long l = 0; for(Ticket ticket : tickets){ l += ticket.getChunkList().size(); }
		return l;
	}

	public static Collection<Ticket> getTickets(){
		return tickets;
	}
	
	public static int chunksPerTicket(){
		return ForgeChunkManager.getMaxChunkDepthFor(States.MODID);
	}
	
	public static int maxTickets(){
		return ForgeChunkManager.getMaxTicketLengthFor(States.MODID);
	}

	/** Warning! Does not remove from the "States" loaded-chunk list as of now!*/
	public static void requestUnload(ChunkPos pos){
		Ticket ticket = getTicketForChunk(pos);
		if(ticket != null){ ForgeChunkManager.unforceChunk(ticket, pos); }
		return;
	}
	
}