package net.fexcraft.mod.states.util;

import java.util.concurrent.ConcurrentHashMap;

import net.fexcraft.mod.states.data.chunk.ChunkKey;
import net.fexcraft.mod.states.data.chunk.Chunk_;
import net.minecraft.world.chunk.Chunk;

/**
 * Resource Manager / Data Manager
 * 
 * @author Ferdnand Calo' (FEX___96)
 *
 */
public class ResManager {
	
	public static boolean LOADED;
	public static final String CONSOLE_UUID = "f78a4d8d-d51b-4b39-98a3-230f2de0c670";
	public static ConcurrentHashMap<ChunkKey, Chunk_> CHUNKS = new ConcurrentHashMap<>();

	public static Chunk_ getChunk(int x, int z){
		for(Chunk_ ck : CHUNKS.values()){
			if(ck.key.x == x && ck.key.z == z) return ck;
		}
		return null;
	}

	public static Chunk_ getChunk(Chunk chunk){
		for(Chunk_ ck : CHUNKS.values()){
			if(ck.key.x == chunk.x && ck.key.z == chunk.z) return ck;
		}
		return null;
	}

	public static void remChunk(Chunk chunk){
		CHUNKS.remove(new ChunkKey(chunk.x, chunk.z));
	}

	public static void unload(){
		//
	}

	public static void clear(){
		//
	}

}
