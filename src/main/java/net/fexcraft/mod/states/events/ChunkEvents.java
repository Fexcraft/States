package net.fexcraft.mod.states.events;

import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.fsmm.FSMM;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.data.ChunkCap;
import net.fexcraft.mod.states.data.Chunk_;
import net.fexcraft.mod.states.util.ChunkCapabilityUtil;
import net.fexcraft.mod.states.util.ResManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class ChunkEvents {
    
    @SubscribeEvent
    public static void onLoad(ChunkEvent.Load event){
        if(event.getWorld().provider.getDimension() != 0) return;
    	if(!ResManager.LOADED){
    		ResManager.LOADED = true;
    		if(event.getWorld().isRemote) return;
    		if(!FSMM.isDataManagerLoaded()) FSMM.loadDataManager();
    		States.updateSaveDirectory(event.getWorld());
			Print.log("Initializing States default World Data...");
			//TODO load states, counties, muns, districts
    	}
    	Chunk_ chunk = new Chunk_(event.getWorld(), event.getChunk().x, event.getChunk().z);
        if(ResManager.CHUNKS.containsKey(chunk.key)) return;
        ResManager.CHUNKS.put(chunk.key, chunk);
    }
    
    @SubscribeEvent
    public static void onUnload(ChunkEvent.Unload event){
        if(event.getWorld().provider.getDimension() != 0) return;
        Chunk_ chunk = ResManager.getChunk(event.getChunk());
        if(chunk != null){
        	ResManager.remChunk(event.getChunk());
        	FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> chunk.save());
        }
    }
    
	@SubscribeEvent
	public static void onAttachEventChunk(AttachCapabilitiesEvent<net.minecraft.world.chunk.Chunk> event){
		if(event.getObject().getWorld().provider.getDimension() != 0) return;
		event.addCapability(ChunkCap.REGNAME, new ChunkCapabilityUtil(event.getObject()));
	}

}
