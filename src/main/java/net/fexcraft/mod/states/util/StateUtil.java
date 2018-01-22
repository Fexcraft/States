package net.fexcraft.mod.states.util;

import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.api.District;
import net.fexcraft.mod.states.api.Municipality;
import net.fexcraft.mod.states.api.State;
import net.fexcraft.mod.states.impl.GenericDistrict;
import net.fexcraft.mod.states.impl.GenericMunicipality;
import net.fexcraft.mod.states.impl.GenericState;
import net.minecraft.entity.player.EntityPlayer;

public class StateUtil {

	public static Chunk getChunk(EntityPlayer player){
		net.minecraft.world.chunk.Chunk chunk = player.world.getChunkFromBlockCoords(player.getPosition());
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

}
