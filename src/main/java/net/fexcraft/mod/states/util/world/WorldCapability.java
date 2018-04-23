package net.fexcraft.mod.states.util.world;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

public interface WorldCapability {

	public void setWorld(World world);

	public int getNewMunicipalityId() throws Exception;

	public NBTBase writeToNBT(Capability<WorldCapability> capability, EnumFacing side);

	public void readFromNBT(Capability<WorldCapability> capability, EnumFacing side, NBTBase nbt);

	public int getNewDistrictId() throws Exception;

}
