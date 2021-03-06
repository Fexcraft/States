package net.fexcraft.mod.states.events;

import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.data.Chunk;
import net.fexcraft.mod.states.data.ChunkPos;
import net.fexcraft.mod.states.data.District;
import net.fexcraft.mod.states.data.Municipality;
import net.fexcraft.mod.states.data.State;
import net.fexcraft.mod.states.util.StateLogger;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class ChunkEvents {
	
	private static int[] def_st = new int[]{ -1, 0 };
	private static int[] def_mun = new int[]{ -1, 0 };
	private static int[] def_dis = new int[]{ -2, -1, 0 };
	public static boolean LOADED = false;
    
    @SubscribeEvent
    public static void onLoad(ChunkEvent.Load event){
		if(!LOADED){
			if(event.getWorld().isRemote) return;
			if(!event.getWorld().isRemote && !FSMM.isDataManagerLoaded()){
				FSMM.loadDataManager();
			}
			States.updateSaveDirectory(event.getWorld());
			Print.log("Loading World...");
			for(int i : def_st){
				if(!States.STATES.containsKey(i)){
					States.STATES.put(i, new State(i));
					States.STATES.get(i).save();
				}
			}
			for(int i : def_mun){
				if(!States.MUNICIPALITIES.containsKey(i)){
					States.MUNICIPALITIES.put(i, new Municipality(i));
					States.MUNICIPALITIES.get(i).save();
				}
			}
			for(int i : def_dis){
				if(!States.DISTRICTS.containsKey(i)){
					States.DISTRICTS.put(i, new District(i));
					States.DISTRICTS.get(i).save();
				}
			} LOADED = true;
		}
        if(event.getWorld().provider.getDimension() != 0) return;
        if(event.getWorld().isRemote){
    		//net.fexcraft.mod.states.guis.Minimap.loadChunk(event.getChunk());
    		return;
        } /*if(!WorldEvents.LOADED){ WorldEvents.onWorldLoad(null); WorldEvents.LOADED = true; }*/
        ChunkPos pos = new ChunkPos(event.getChunk().x, event.getChunk().z);
        if(!States.CHUNKS.containsKey(pos)){
            States.CHUNKS.put(pos, new Chunk(event.getWorld(), pos));
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
        if(chunk != null){
        	if(!States.CHUNKS.entrySet().removeIf(pre -> pre.getValue().xCoord() == chunk.xCoord() && pre.getValue().zCoord() == chunk.zCoord())){
        		Print.log("Error while trying to remove chunkdata for " + StateLogger.chunk(chunk) + " from main storaging Map.");
        	}
            //File file = chunk.getChunkFile();
            //boolean matches = file.exists() && JsonUtil.get(file).get("changed").getAsLong() == chunk.getChanged();
        	FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> chunk.save());
            /*if(!matches || chunk.getEdited() > chunk.getChanged()){
            	ImageCache.update(event.getWorld(), event.getChunk());
            }*/
        }
    }

}
