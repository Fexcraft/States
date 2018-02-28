package net.fexcraft.mod.states.events;

import java.io.File;

import net.fexcraft.mod.lib.util.json.JsonUtil;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.impl.GenericChunk;
import net.fexcraft.mod.states.impl.capabilities.TESSerialisable;
import net.fexcraft.mod.states.util.ImageCache;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class ChunkEvents {
	
	@SubscribeEvent
	public static void onLoad(ChunkEvent.Load event){
		if(event.getWorld().isRemote || event.getWorld().provider.getDimension() != 0){
			return;
		}
		int x = event.getChunk().x, z = event.getChunk().z;
		if(!States.CHUNKS.contains(x, z)){
			States.CHUNKS.put(x, z, new GenericChunk(x, z));
		}
	}
	
	@SubscribeEvent
	public static void onUnload(ChunkEvent.Unload event){
		if(event.getWorld().isRemote || event.getWorld().provider.getDimension() != 0){
			return;
		}
		int x = event.getChunk().x, z = event.getChunk().z;
		if(States.CHUNKS.contains(x, z)){
			Chunk chunk = States.CHUNKS.remove(x, z);
			if(!(chunk == null)){
				File file = chunk.getChunkFile();
				boolean matches = file.exists() && JsonUtil.get(file).get("changed").getAsLong() == chunk.getChanged();
				chunk.save();
				if(!matches){
					ImageCache.update(event.getWorld(), event.getChunk(), "unload", "all");
				}
			}
		}
		event.getChunk().getTileEntityMap().keySet().forEach(key -> {
			TESSerialisable.TILEENTITIES.remove(key);
		});
	}

}
