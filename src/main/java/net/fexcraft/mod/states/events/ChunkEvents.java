package net.fexcraft.mod.states.events;

import java.io.File;

import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.lib.util.json.JsonUtil;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.api.ChunkPos;
import net.fexcraft.mod.states.impl.GenericChunk;
import net.fexcraft.mod.states.impl.capabilities.SignTileEntityCapabilityUtil;
import net.fexcraft.mod.states.util.ImageCache;
import net.fexcraft.mod.states.util.StateLogger;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class ChunkEvents {
    
    @SubscribeEvent
    public static void onLoad(ChunkEvent.Load event){
        if(event.getWorld().provider.getDimension() != 0) return;
        if(event.getWorld().isRemote){
    		//net.fexcraft.mod.states.guis.Minimap.loadChunk(event.getChunk());
    		return;
        } /*if(!WorldEvents.LOADED){ WorldEvents.onWorldLoad(null); WorldEvents.LOADED = true; }*/
        ChunkPos pos = new ChunkPos(event.getChunk().x, event.getChunk().z);
        if(!States.CHUNKS.containsKey(pos)){
            States.CHUNKS.put(pos, new GenericChunk(pos));
        }
    }
    
    @SubscribeEvent
    public static void onUnload(ChunkEvent.Unload event){
        if(event.getWorld().provider.getDimension() != 0) return;
    	if(event.getWorld().isRemote){
    		//net.fexcraft.mod.states.guis.Minimap.unloadChunk(event.getChunk());
    		return;
    	}
        Chunk chunk = StateUtil.getChunk(event.getChunk());
        //Chunk chunk = event.getChunk().getCapability(StatesCapabilities.CHUNK, null).getStatesChunk(true);
        if(!(chunk == null)){
        	if(!States.CHUNKS.entrySet().removeIf(pre -> pre.getValue().xCoord() == chunk.xCoord() && pre.getValue().zCoord() == chunk.zCoord())){
        		Print.log("Error while trying to remove chunkdata for " + StateLogger.chunk(chunk) + " from main storaging Map.");
        	}
            File file = chunk.getChunkFile();
            boolean matches = file.exists() && JsonUtil.get(file).get("changed").getAsLong() == chunk.getChanged();
            chunk.save();
            if(!matches || chunk.getEdited() > chunk.getChanged()){
            	ImageCache.update(event.getWorld(), event.getChunk());
            }
        }
        event.getChunk().getTileEntityMap().keySet().forEach(key -> {
            SignTileEntityCapabilityUtil.TILEENTITIES.remove(key);
        });
    }

}
