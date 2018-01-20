package net.fexcraft.mod.states.util;

import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.api.District;
import net.fexcraft.mod.states.impl.GenericDistrict;
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

}
