package net.fexcraft.mod.states.impl.capabilities;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class TESStorage implements IStorage<TESCapability> {

	@Override
	public NBTBase writeNBT(Capability<TESCapability> capability, TESCapability instance, EnumFacing side){
		return instance.writeToNBT(capability, side);
	}

	@Override
	public void readNBT(Capability<TESCapability> capability, TESCapability instance, EnumFacing side, NBTBase nbt){
		instance.readNBT(capability, side, nbt);
	}

}
