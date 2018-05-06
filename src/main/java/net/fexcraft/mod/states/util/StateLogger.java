package net.fexcraft.mod.states.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import net.fexcraft.mod.lib.util.common.Print;

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
	
	public static final void log(String logger, Object obj){
		LoggerType type = LoggerType.valueOf(logger.toUpperCase());
		if(type != null){
			log(type, Level.INFO, obj);
		}
		else{
			Print.log("[StateLogger] Invalid logger type supplied '" + logger + "'!");
		}
	}
	
}