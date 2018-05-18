package net.fexcraft.mod.states.impl.capabilities;

import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.capabilities.ChunkCapability;
import net.fexcraft.mod.states.api.capabilities.StatesCapabilities;
import net.fexcraft.mod.states.impl.GenericChunk;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class ChunkCapabilityUtil implements ICapabilitySerializable<NBTBase>{
    
    public static final ResourceLocation REGISTRY_NAME = new ResourceLocation("states:chunk");
    private ChunkCapability instance;
    
    public ChunkCapabilityUtil(Chunk chunk){
        instance = StatesCapabilities.CHUNK.getDefaultInstance();
        instance.setChunk(chunk);
    }
    
    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing){
        return capability == StatesCapabilities.CHUNK;
    }
    
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing){
        return capability == StatesCapabilities.CHUNK ? StatesCapabilities.CHUNK.<T>cast(this.instance) : null;
    }
    
    @Override
    public NBTBase serializeNBT(){
        return StatesCapabilities.CHUNK.getStorage().writeNBT(StatesCapabilities.CHUNK, instance, null);
    }
    
    @Override
    public void deserializeNBT(NBTBase nbt){
        StatesCapabilities.CHUNK.getStorage().readNBT(StatesCapabilities.CHUNK, instance, null, nbt);
    }

    //
    
    public static class Storage implements IStorage<ChunkCapability> {
        
        @Override
        public NBTBase writeNBT(Capability<ChunkCapability> capability, ChunkCapability instance, EnumFacing side){
            return new NBTTagString(instance == null ? "" : instance.getStatesChunk() == null ? instance.getChunk().x + "_" + instance.getChunk().z : instance.getStatesChunk().toString());
            //I know this is nonsense, but else chunks kept getting errors and didn't save.
        }
        
        @Override
        public void readNBT(Capability<ChunkCapability> capability, ChunkCapability instance, EnumFacing side, NBTBase nbt){
            //
        }
    }

    //
    
    public static class Callable implements java.util.concurrent.Callable<ChunkCapability>{
        
        @Override
        public ChunkCapability call() throws Exception {
            return new Implementation();
        }
    
    }
    
    public static class Implementation implements ChunkCapability {
        
        private Chunk chunk;
        private net.fexcraft.mod.states.api.Chunk statechunk;
        
        @Override
        public void setChunk(Chunk chunk){
            this.chunk = chunk;
        }
        
        @Override
        public net.fexcraft.mod.states.api.Chunk getStatesChunk(){
            return getStatesChunk(false);
        }

        @Override
        public net.fexcraft.mod.states.api.Chunk getStatesChunk(boolean allownull){
            if(allownull){ return statechunk; }
            if(statechunk == null){
                statechunk = States.CHUNKS.get(chunk.x, chunk.z);
            }
            return statechunk;
        }
        
        @Override
        public Chunk getChunk(){
            return chunk;
        }
    
    }
    
}
