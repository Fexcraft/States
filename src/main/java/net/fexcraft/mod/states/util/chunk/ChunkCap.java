package net.fexcraft.mod.states.util.chunk;

import net.fexcraft.mod.states.util.StateUtil;
import net.minecraft.world.chunk.Chunk;

public class ChunkCap implements ChunkCapability {
	
	//This is a complete Memory and Processing waste, but hey, it gives compatibility, doesn't it?
	
	private Chunk chunk;
	private net.fexcraft.mod.states.api.Chunk statechunk;

	@Override
	public void setChunk(Chunk chunk){
		this.chunk = chunk;
	}
	
	@Override
	public net.fexcraft.mod.states.api.Chunk getStatesChunk(){
		return statechunk == null ? statechunk = StateUtil.getChunk(chunk.x, chunk.z) : statechunk;
	}
	
}
