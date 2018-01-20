package net.fexcraft.mod.states.events;

import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.impl.GenericChunk;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class ChunkEvents {
	
	@SubscribeEvent
	public static void onLoad(ChunkEvent.Load event){
		int x = event.getChunk().x, z = event.getChunk().z;
		if(!States.CHUNKS.contains(x, z)){
			States.CHUNKS.put(x, z, new GenericChunk(x, z));
		}
	}
	
	@SubscribeEvent
	public static void onUnload(ChunkEvent.Unload event){
		int x = event.getChunk().x, z = event.getChunk().z;
		if(States.CHUNKS.contains(x, z)){
			Chunk chunk = States.CHUNKS.remove(x, z);
			if(!(chunk == null)){
				chunk.save();
			}
		}
	}

}
