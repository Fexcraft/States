package net.fexcraft.mod.states.util;

import static net.fexcraft.mod.states.data.chunk.ChunkCap.CHUNK;

import net.fexcraft.mod.states.data.chunk.ChunkCap;
import net.fexcraft.mod.states.data.chunk.Chunk_;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class ChunkCapabilityUtil implements ICapabilitySerializable<NBTBase>{
    
    private ChunkCap instance;
    
    public ChunkCapabilityUtil(Chunk chunk){
        instance = CHUNK.getDefaultInstance();
        instance.setChunk(chunk);
    }
    
    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing){
        return capability == CHUNK;
    }
    
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing){
        return capability == CHUNK ? CHUNK.cast(this.instance) : null;
    }
    
    @Override
    public NBTBase serializeNBT(){
        return CHUNK.getStorage().writeNBT(CHUNK, instance, null);
    }
    
    @Override
    public void deserializeNBT(NBTBase nbt){
        CHUNK.getStorage().readNBT(CHUNK, instance, null, nbt);
    }
    
    public static class Storage implements IStorage<ChunkCap> {
        
        @Override
        public NBTBase writeNBT(Capability<ChunkCap> capability, ChunkCap instance, EnumFacing side){
            return new NBTTagCompound();
        }
        
        @Override
        public void readNBT(Capability<ChunkCap> capability, ChunkCap instance, EnumFacing side, NBTBase nbt){
            //
        }
    }
    
    public static class Callable implements java.util.concurrent.Callable<ChunkCap>{
        
        @Override
        public ChunkCap call() throws Exception {
            return new Implementation();
        }
    
    }
    
    public static class Implementation implements ChunkCap {
        
        private Chunk chunk;
        private Chunk_ chunk_;
        
        @Override
        public void setChunk(Chunk chunk){
            this.chunk = chunk;
        }
        
        @Override
        public Chunk_ getStatesChunk(){
            return getStatesChunk(false);
        }

        @Override
        public Chunk_ getStatesChunk(boolean allownull){
            if(allownull){ return chunk_; }
            if(chunk_ == null){
                chunk_ = ResManager.getChunk(chunk.x, chunk.z);
            }
            return chunk_;
        }
        
        @Override
        public Chunk getChunk(){
            return chunk;
        }
    
    }
    
}
