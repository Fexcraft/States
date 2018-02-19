package net.fexcraft.mod.states.events;

import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.impl.GenericChunk;
import net.fexcraft.mod.states.util.ImageCache;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class ChunkEvents {
	
	@SubscribeEvent
	public static void onLoad(ChunkEvent.Load event){
		if(event.getWorld().provider.getDimension() != 0){
			return;
		}
		int x = event.getChunk().x, z = event.getChunk().z;
		if(!States.CHUNKS.contains(x, z)){
			States.CHUNKS.put(x, z, new GenericChunk(x, z));
		}
	}
	
	@SubscribeEvent
	public static void onUnload(ChunkEvent.Unload event){
		if(event.getWorld().provider.getDimension() != 0){
			return;
		}
		int x = event.getChunk().x, z = event.getChunk().z;
		if(States.CHUNKS.contains(x, z)){
			Chunk chunk = States.CHUNKS.remove(x, z);
			if(!(chunk == null)){
				chunk.save();
			}
		}
		//
		ImageCache.update(event.getWorld(), event.getChunk(), "unload", "surface");
		/*ImageCache.update(event.getWorld(), event.getChunk(), "unload", "surface_states");
		ImageCache.update(event.getWorld(), event.getChunk(), "unload", "states");
		ImageCache.update(event.getWorld(), event.getChunk(), "unload", "surface_municipalities");
		ImageCache.update(event.getWorld(), event.getChunk(), "unload", "municipalities");
		ImageCache.update(event.getWorld(), event.getChunk(), "unload", "surface_districts");
		ImageCache.update(event.getWorld(), event.getChunk(), "unload", "districts");
		ImageCache.update(event.getWorld(), event.getChunk(), "unload", "commercial");
		ImageCache.update(event.getWorld(), event.getChunk(), "unload", "chunk_types");
		ImageCache.update(event.getWorld(), event.getChunk(), "unload", "biomemap");*/
	}

}
