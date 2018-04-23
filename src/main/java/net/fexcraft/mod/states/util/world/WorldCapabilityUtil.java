package net.fexcraft.mod.states.util.world;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class WorldCapabilityUtil implements ICapabilitySerializable<NBTBase>{
	
	@CapabilityInject(WorldCapability.class)
	public static final Capability<WorldCapability> WORLD_CAPABILITY = null;
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation("states:world");
	private WorldCapability instance;
	
	public WorldCapabilityUtil(net.minecraft.world.World world){
		instance = WORLD_CAPABILITY.getDefaultInstance();
		instance.setWorld(world);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing){
		return capability == WORLD_CAPABILITY;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing){
		return capability == WORLD_CAPABILITY ? WORLD_CAPABILITY.<T>cast(this.instance) : null;
	}

	@Override
	public NBTBase serializeNBT(){
		return WORLD_CAPABILITY.getStorage().writeNBT(WORLD_CAPABILITY, instance, null);
	}

	@Override
	public void deserializeNBT(NBTBase nbt){
		WORLD_CAPABILITY.getStorage().readNBT(WORLD_CAPABILITY, instance, null, nbt);
	}
	
	//
	
	public static class Storage implements IStorage<WorldCapability> {

		@Override
		public NBTBase writeNBT(Capability<WorldCapability> capability, WorldCapability instance, EnumFacing side){
			return instance.writeToNBT(capability, side);
		}

		@Override
		public void readNBT(Capability<WorldCapability> capability, WorldCapability instance, EnumFacing side, NBTBase nbt){
			instance.readFromNBT(capability, side, nbt);
		}
		
	}
	
	//
	
	public static class Callable implements java.util.concurrent.Callable<WorldCapability>{

		@Override
		public WorldCapability call() throws Exception {
			return new WorldCap();
		}
		
	}

}
