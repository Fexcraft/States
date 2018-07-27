package net.fexcraft.mod.states.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import com.mojang.authlib.GameProfile;

import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.api.ChunkPos;
import net.fexcraft.mod.states.api.District;
import net.fexcraft.mod.states.api.Municipality;
import net.fexcraft.mod.states.api.State;
import net.fexcraft.mod.states.api.capabilities.PlayerCapability;
import net.minecraft.entity.player.EntityPlayer;

public class StateLogger {
	
	public static final StateLogger INSTANCE = new StateLogger();
	private Logger states, municipalities, districts, chunks, players;
	
	private StateLogger(){
		states = Print.getCustomLogger("st-states", "state", "States", null);
		municipalities = Print.getCustomLogger("st-municipalities", "municipality", "Municipalities", null);
		districts = Print.getCustomLogger("st-districts", "district", "Districts", null);
		chunks = Print.getCustomLogger("st-chunks", "chunk", "Chunks", null);
		players = Print.getCustomLogger("st-players", "player", "Players", null);
	}
	
	public static enum LoggerType {
		
		ALL, STATE, MUNICIPALITY, DISRICT, CHUNK, PLAYER;
		
		public Logger getLogger(){
			switch(this){
				case CHUNK: return INSTANCE.chunks;
				case DISRICT: return INSTANCE.districts;
				case MUNICIPALITY: return INSTANCE.municipalities;
				case PLAYER: return INSTANCE.players;
				case STATE: return INSTANCE.states;
				case ALL: default: return null;
			}
		}
		
	}
	
	public static final void log(LoggerType type, Level level, Object obj){
		if(type != LoggerType.ALL){
			type.getLogger().info(obj == null ? "null" : obj.toString());
			return;
		}
		for(LoggerType typ : LoggerType.values()){
			if(typ == LoggerType.ALL){ continue; }
			typ.getLogger().info(obj == null ? "null" : obj.toString());
		}
	}
	
	public static final void log(LoggerType type, Object obj){
		log(type, Level.INFO, obj);
	}
	
	public static final void log(String logger, Object obj){
		LoggerType type = LoggerType.valueOf(logger.toUpperCase());
		if(type != null){
			log(type, Level.INFO, obj);
		}
		else{
			Print.log("[StateLogger] Invalid logger type supplied '" + logger + "'!");
		}
	}

	public static String player(EntityPlayer player){
		return player(player.getGameProfile());
	}

	public static String player(GameProfile gp){
		return gp.getId().toString() + "(" + gp.getName() + ")";
	}

	public static String player(PlayerCapability cap){
		return player(cap.getEntityPlayer());
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