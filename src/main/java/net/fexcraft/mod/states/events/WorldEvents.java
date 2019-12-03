package net.fexcraft.mod.states.events;

import java.util.ArrayList;

import net.fexcraft.mod.states.data.Chunk;
import net.fexcraft.mod.states.impl.capabilities.ChunkCapabilityUtil;
import net.fexcraft.mod.states.impl.capabilities.PlayerCapabilityUtil;
import net.fexcraft.mod.states.impl.capabilities.SignTileEntityCapabilityUtil;
import net.fexcraft.mod.states.impl.capabilities.WorldCapabilityUtil;
import net.fexcraft.mod.states.util.ForcedChunksManager;
import net.fexcraft.mod.states.util.ImageCache;
import net.fexcraft.mod.states.util.StateLogger;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class WorldEvents {
	

	
	@SubscribeEvent
	public static void onWorldLoad(WorldEvent.Load event){
		if(event != null && (event.getWorld().provider.getDimension() != 0 || event.getWorld().isRemote)) return;
		ImageCache.loadQueue();
		ForcedChunksManager.load();
		//event.getWorld().addEventListener(new TestListener());
	}
	
	@SubscribeEvent
	public static void onWorldUnload(WorldEvent.Unload event){
		if(event.getWorld().provider.getDimension() != 0 || event.getWorld().isRemote) return;
		StateUtil.unloadAll(); StateUtil.clearAll(); ChunkEvents.LOADED = false;
		ImageCache.saveQueue();
		StateLogger.log("all", "Unloading World...");
		ForcedChunksManager.unload();
	}
	
	@SubscribeEvent
	public static void onExplosion(ExplosionEvent event){
		if(event.getWorld().provider.getDimension() != 0 || event.getWorld().isRemote) return;
		try{
			ArrayList<BlockPos> torem = new ArrayList<>();
			for(BlockPos pos : event.getExplosion().getAffectedBlockPositions()){
				Chunk chunk = StateUtil.getChunk(pos);
				if(!chunk.getDistrict().r_ALLOW_EXPLOSIONS.get()){ torem.add(pos); }
			}
			event.getExplosion().getAffectedBlockPositions().removeAll(torem);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@SubscribeEvent
	public static void onHostileSpawn(LivingSpawnEvent.CheckSpawn event){
		if(event.getWorld().provider.getDimension() != 0 || event.getWorld().isRemote) return;
		Chunk chunk = StateUtil.getChunk(event.getX(), event.getZ());
		if(!chunk.getDistrict().r_ALLOW_ENT_HOSTILE.get()){
			
		}
	}
	
	//
	
	@SubscribeEvent
	public static void onAttachEventTileEntity(AttachCapabilitiesEvent<net.minecraft.tileentity.TileEntity> event){
		if(event.getObject() instanceof TileEntitySign){
			event.addCapability(new ResourceLocation("states", "sign"), new SignTileEntityCapabilityUtil(event.getObject()));
		}
	}
	
	@SubscribeEvent
	public static void onAttachEventEntityPlayer(AttachCapabilitiesEvent<net.minecraft.entity.Entity> event){
		if(event.getObject() instanceof EntityPlayer && !event.getObject().world.isRemote){
			event.addCapability(PlayerCapabilityUtil.REGISTRY_NAME, new PlayerCapabilityUtil((EntityPlayer)event.getObject()));
		}
	}
	
	@SubscribeEvent
	public static void onAttachEventWorld(AttachCapabilitiesEvent<net.minecraft.world.World> event){
		event.addCapability(WorldCapabilityUtil.REGISTRY_NAME, new WorldCapabilityUtil(event.getObject()));
	}
	
	@SubscribeEvent
	public static void onAttachEventChunk(AttachCapabilitiesEvent<net.minecraft.world.chunk.Chunk> event){
            if(event.getObject().getWorld().provider.getDimension() != 0){ return; }
            event.addCapability(ChunkCapabilityUtil.REGISTRY_NAME, new ChunkCapabilityUtil(event.getObject()));
	}
	
}
