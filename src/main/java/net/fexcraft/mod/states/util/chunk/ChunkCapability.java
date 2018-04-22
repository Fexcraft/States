package net.fexcraft.mod.states.util.chunk;

import net.minecraft.world.chunk.Chunk;

public interface ChunkCapability {

	public void setChunk(Chunk chunk);
	
	public net.fexcraft.mod.states.api.Chunk getStatesChunk();

}
