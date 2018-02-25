package net.fexcraft.mod.states.util;

import java.io.File;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.fexcraft.mod.lib.perms.PermManager;
import net.fexcraft.mod.lib.util.json.JsonUtil;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.api.District;
import net.fexcraft.mod.states.api.Municipality;
import net.fexcraft.mod.states.api.Player;
import net.fexcraft.mod.states.api.State;
import net.fexcraft.mod.states.impl.GenericChunk;
import net.fexcraft.mod.states.impl.GenericDistrict;
import net.fexcraft.mod.states.impl.GenericMunicipality;
import net.fexcraft.mod.states.impl.GenericPlayer;
import net.fexcraft.mod.states.impl.GenericState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StateUtil {

	public static Chunk getChunk(EntityPlayer player){
		net.minecraft.world.chunk.Chunk chunk = player.world.getChunkFromBlockCoords(player.getPosition());
		return States.CHUNKS.get(chunk.x, chunk.z);
	}

	public static Chunk getChunk(int x, int z){
		return  States.CHUNKS.get(x, z);
	}

	public static Chunk getChunk(World world, BlockPos pos){
		net.minecraft.world.chunk.Chunk chunk = world.getChunkFromBlockCoords(pos);
		return States.CHUNKS.get(chunk.x, chunk.z);
	}

	public static District getDistrict(int value){
		if(States.DISTRICTS.containsKey(value)){
			return States.DISTRICTS.get(value);
		}
		if(District.getDistrictFile(value).exists()){
			District district = new GenericDistrict(value);
			States.DISTRICTS.put(value, district);
			return district;
		}
		else return States.DISTRICTS.get(-1);
	}

	public static Municipality getMunicipality(int value){
		if(States.MUNICIPALITIES.containsKey(value)){
			return States.MUNICIPALITIES.get(value);
		}
		if(Municipality.getMunicipalityFile(value).exists()){
			Municipality municipality = new GenericMunicipality(value);
			States.MUNICIPALITIES.put(value, municipality);
			return municipality;
		}
		else return States.MUNICIPALITIES.get(-1);
	}

	public static State getState(int value){
		if(States.STATES.containsKey(value)){
			return States.STATES.get(value);
		}
		if(State.getStateFile(value).exists()){
			State state = new GenericState(value);
			States.STATES.put(value, state);
			return state;
		}
		else return States.STATES.get(-1);
	}

	public static Chunk getTempChunk(int x, int z){
		Chunk chunk = getChunk(x, z);
		return chunk == null ? new GenericChunk(x, z, false) : chunk;
	}

	public static Chunk getTempChunk(ResourceLocation ckpos){
		int x = Integer.parseInt(ckpos.getResourceDomain());
		int z = Integer.parseInt(ckpos.getResourcePath());
		return getTempChunk(x, z);
	}

	public static boolean isUUID(String owner){
		try{
			UUID uuid = UUID.fromString(owner);
			return uuid != null;
		}
		catch(Exception e){
			return false;
		}
	}
	
	@Nullable
	public static Player getPlayer(UUID uuid, boolean loadtemp){
		return States.PLAYERS.containsKey(uuid) ? States.PLAYERS.get(uuid) : loadtemp ? getOfflinePlayer(uuid) : null;
	}

	private static Player getOfflinePlayer(UUID uuid){
		JsonElement elm = JsonUtil.read(new File(PermManager.userDir, "/" + uuid.toString() + ".perm"), false);
		if(elm == null){
			return null;
		}
		else{
			JsonObject obj = elm.getAsJsonObject();
			if(!obj.has("AttachedData") || !obj.get("AttachedData").getAsJsonObject().has(States.PLAYER_DATA)){
				return null;
			}
			return GenericPlayer.getOfflineInstance(uuid, obj.get("AttachedData").getAsJsonObject().get(States.PLAYER_DATA).getAsJsonObject());
		}
	}

	public static Player getPlayer(EntityPlayer player){
		return PermManager.getPlayerPerms(player).getAdditionalData(GenericPlayer.class);
	}

}
