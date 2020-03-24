package net.fexcraft.mod.states.util;

import com.mojang.authlib.GameProfile;

import net.fexcraft.mod.states.data.Chunk;
import net.fexcraft.mod.states.data.ChunkPos;
import net.fexcraft.mod.states.data.District;
import net.fexcraft.mod.states.data.Municipality;
import net.fexcraft.mod.states.data.State;
import net.fexcraft.mod.states.data.capabilities.PlayerCapability;
import net.minecraft.entity.player.EntityPlayer;

public class StateLogger {

	public static String player(EntityPlayer player){
		return player(player.getGameProfile());
	}

	public static String player(GameProfile gp){
		return gp.getId().toString() + "(" + gp.getName() + ")";
	}

	public static String player(PlayerCapability cap){
		return cap.getUUIDAsString() + "(" + (cap.getEntityPlayer() == null ? "off-line" : cap.getName()) + ")";
	}

	public static String chunk(Chunk chunk){
		return "Chunk(" + chunk.xCoord() + ", " + chunk.zCoord() + ")";
	}

	public static String chunk(ChunkPos pos){
		return "Chunk(" + pos.x + ", " + pos.z + ")";
	}

	public static String district(int i){
		District dis = StateUtil.getDistrict(i, false);
		if(dis == null){
			return "INVALID-DISTRICT(" + i + ")";
		}
		return district(dis);
	}

	public static String district(District district){
		return district.getName() + "(" + district.getId() + ")";
	}
	
	public static String municipality(int i){
		Municipality mun = StateUtil.getMunicipality(i, false);
		if(mun == null){
			return "INVALID-MUNICIPALITY(" + i + ")";
		}
		return municipality(mun);
	}

	public static String municipality(Municipality mun){
		return mun.getName() + "(" + mun.getId() + ")";
	}
	
	public static String state(int i){
		State state = StateUtil.getState(i, false);
		if(state == null){
			return "INVALID-STATE(" + i + ")";
		}
		return state(state);
	}

	public static String state(State state){
		return state.getName() + "(" + state.getId() + ")";
	}
	
}