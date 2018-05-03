package net.fexcraft.mod.states.events;

import com.google.gson.JsonObject;

import net.fexcraft.mod.lib.util.json.JsonUtil;
import net.fexcraft.mod.lib.util.math.Time;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.District;
import net.fexcraft.mod.states.api.DistrictType;
import net.fexcraft.mod.states.api.Municipality;
import net.fexcraft.mod.states.api.State;
import net.fexcraft.mod.states.impl.GenericDistrict;
import net.fexcraft.mod.states.impl.GenericMunicipality;
import net.fexcraft.mod.states.impl.GenericState;
import net.fexcraft.mod.states.impl.capabilities.ChunkCapabilityUtil;
import net.fexcraft.mod.states.impl.capabilities.PlayerCapabilityUtil;
import net.fexcraft.mod.states.impl.capabilities.SignTileEntityCapabilityUtil;
import net.fexcraft.mod.states.impl.capabilities.WorldCapabilityUtil;
import net.fexcraft.mod.states.util.ImageCache;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class WorldEvents {
	
	@SubscribeEvent
	public static void onWorldLoad(WorldEvent.Load event){
		if(event.getWorld().provider.getDimension() != 0 || event.getWorld().isRemote){
			return;
		}
		if(!States.STATES.containsKey(-1)){
			if(!State.getStateFile(-1).exists()){
				JsonObject object = new JsonObject();
				object.addProperty("id", -1);
				object.addProperty("created", Time.getDate());
				object.addProperty("creator", States.CONSOLE_UUID);
				object.addProperty("changed", Time.getDate());
				object.addProperty("name", "Neutral Territory");
				object.addProperty("leader", States.CONSOLE_UUID);
				object.addProperty("capital", -1);
				object.addProperty("color", "#003500");
				JsonUtil.write(State.getStateFile(-1), object);
			}
			States.STATES.put(-1, new GenericState(-1));
		}
		if(!States.STATES.containsKey(0)){
			if(!State.getStateFile(0).exists()){
				JsonObject object = new JsonObject();
				object.addProperty("id", 0);
				object.addProperty("created", Time.getDate());
				object.addProperty("creator", States.DEF_UUID);
				object.addProperty("changed", Time.getDate());
				object.addProperty("name", "Testaria");
				object.addProperty("leader", States.DEF_UUID);
				object.addProperty("capital", 0);
				object.addProperty("color", "#FF6600");
				JsonUtil.write(State.getStateFile(0), object);
			}
			States.STATES.put(0, new GenericState(0));
		}
		if(!States.MUNICIPALITIES.containsKey(-1)){
			if(!Municipality.getMunicipalityFile(-1).exists()){
				JsonObject object = new JsonObject();
				object.addProperty("id", -1);
				object.addProperty("created", Time.getDate());
				object.addProperty("creator", States.CONSOLE_UUID);
				object.addProperty("changed", Time.getDate());
				object.addProperty("name", "Wilderness");
				object.addProperty("state", -1);
				object.addProperty("color", "#003500");
				JsonUtil.write(Municipality.getMunicipalityFile(-1), object);
			}
			States.MUNICIPALITIES.put(-1, new GenericMunicipality(-1));
		}
		if(!States.MUNICIPALITIES.containsKey(0)){
			if(!Municipality.getMunicipalityFile(0).exists()){
				JsonObject object = new JsonObject();
				object.addProperty("id", 0);
				object.addProperty("created", Time.getDate());
				object.addProperty("creator", States.DEF_UUID);
				object.addProperty("changed", Time.getDate());
				object.addProperty("mayor", States.DEF_UUID);
				object.addProperty("name", "Spawn");
				object.addProperty("state", 0);
				object.addProperty("color", "#FF6600");
				JsonUtil.write(Municipality.getMunicipalityFile(0), object);
			}
			States.MUNICIPALITIES.put(0, new GenericMunicipality(0));
		}
		if(!States.DISTRICTS.containsKey(-1)){
			if(!District.getDistrictFile(-1).exists()){
				JsonObject object = new JsonObject();
				object.addProperty("id", -1);
				object.addProperty("type", DistrictType.WILDERNESS.name());
				object.addProperty("created", Time.getDate());
				object.addProperty("creator", States.CONSOLE_UUID);
				object.addProperty("changed", Time.getDate());
				object.addProperty("name", "Wilderness");
				object.addProperty("municipality", -1);
				object.addProperty("color", "#003500");
				JsonUtil.write(District.getDistrictFile(-1), object);
			}
			States.DISTRICTS.put(-1, new GenericDistrict(-1));
		}
		if(!States.DISTRICTS.containsKey(0)){
			if(!District.getDistrictFile(0).exists()){
				JsonObject object = new JsonObject();
				object.addProperty("id", 0);
				object.addProperty("type", DistrictType.MUNICIPIAL.name());
				object.addProperty("created", Time.getDate());
				object.addProperty("creator", States.DEF_UUID);
				object.addProperty("changed", Time.getDate());
				object.addProperty("name", "Safezone");
				object.addProperty("municipality", 0);
				object.addProperty("color", "#FF6600");
				JsonUtil.write(District.getDistrictFile(0), object);
			}
			States.DISTRICTS.put(0, new GenericDistrict(0));
		}
		ImageCache.loadQueue();
		//event.getWorld().addEventListener(new TestListener());
	}
	
	@SubscribeEvent
	public static void onWorldUnload(WorldEvent.Unload event){
		if(event.getWorld().provider.getDimension() != 0 || event.getWorld().isRemote){
			return;
		}
		States.DISTRICTS.values().forEach(elm -> { elm.save(); });
		States.MUNICIPALITIES.values().forEach(elm -> { elm.save(); });
		States.STATES.values().forEach(elm -> { elm.save(); });
		ImageCache.saveQueue();
	}
	
	@SubscribeEvent
	public static void onExplosion(ExplosionEvent event){
		/*for(BlockPos pos : event.getExplosion().getAffectedBlockPositions()){
			if(StateUtil.getChunk(event.getWorld(), pos).getDistrict().getId() >= 0){
				event.getExplosion().getAffectedBlockPositions().remove(pos);
			}
		}*/
		event.setCanceled(true);
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
		if(event.getObject() instanceof EntityPlayer){
			event.addCapability(PlayerCapabilityUtil.REGISTRY_NAME, new PlayerCapabilityUtil((EntityPlayer)event.getObject()));
		}
	}
	
	@SubscribeEvent
	public static void onAttachEventWorld(AttachCapabilitiesEvent<net.minecraft.world.World> event){
		event.addCapability(WorldCapabilityUtil.REGISTRY_NAME, new WorldCapabilityUtil(event.getObject()));
	}
	
	@SubscribeEvent
	public static void onAttachEventChunk(AttachCapabilitiesEvent<net.minecraft.world.chunk.Chunk> event){
		event.addCapability(ChunkCapabilityUtil.REGISTRY_NAME, new ChunkCapabilityUtil(event.getObject()));
	}
	
}
