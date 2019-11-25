package net.fexcraft.mod.states.data.capabilities;

import net.minecraft.world.chunk.Chunk;

public interface ChunkCapability {
    
    public Chunk getChunk();
    
    public void setChunk(Chunk chunk);
    
    public net.fexcraft.mod.states.data.Chunk getStatesChunk();

    public net.fexcraft.mod.states.data.Chunk getStatesChunk(boolean allownull);

}
