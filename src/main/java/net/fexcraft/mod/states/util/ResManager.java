package net.fexcraft.mod.states.util;

import java.util.HashMap;

import net.fexcraft.mod.states.data.Chunk_;
import net.fexcraft.mod.states.data.Chunk_.ChunkKey;
import net.minecraft.world.chunk.Chunk;

/**
 * Resource Manager / Data Manager
 * 
 * @author Ferdnand Calo' (FEX___96)
 *
 */
public class ResManager {
	
	public static boolean LOADED;
	public static HashMap<ChunkKey, Chunk_> CHUNKS = new HashMap<>();

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
