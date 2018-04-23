package net.fexcraft.mod.states.util.chunk;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class ChunkCapabilityUtil implements ICapabilitySerializable<NBTBase>{
	
	@CapabilityInject(ChunkCapability.class)
	public static final Capability<ChunkCapability> CHUNK_CAPABILITY = null;
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation("states:chunk");
	private ChunkCapability instance;
	
	public ChunkCapabilityUtil(Chunk chunk){
		instance = CHUNK_CAPABILITY.getDefaultInstance();
		instance.setChunk(chunk);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing){
		return capability == CHUNK_CAPABILITY;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing){
		return capability == CHUNK_CAPABILITY ? CHUNK_CAPABILITY.<T>cast(this.instance) : null;
	}

	@Override
	public NBTBase serializeNBT(){
		return CHUNK_CAPABILITY.getStorage().writeNBT(CHUNK_CAPABILITY, instance, null);
	}

	@Override
	public void deserializeNBT(NBTBase nbt){
		CHUNK_CAPABILITY.getStorage().readNBT(CHUNK_CAPABILITY, instance, null, nbt);
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
			return new ChunkCap();
		}
		
	}

}
