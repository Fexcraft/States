package net.fexcraft.mod.states.data.capabilities;

import com.google.common.collect.ImmutableMap;

import net.fexcraft.mod.states.data.District;
import net.fexcraft.mod.states.data.Municipality;
import net.fexcraft.mod.states.data.State;
import net.fexcraft.mod.states.data.Vote;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

public interface WorldCapability {

	public void setWorld(World world);

	public NBTBase writeToNBT(Capability<WorldCapability> capability, EnumFacing side);

	public void readFromNBT(Capability<WorldCapability> capability, EnumFacing side, NBTBase nbt);

	public int getNewMunicipalityId() throws Exception;

	public int getNewDistrictId();

	public int getNewStateId();

	public int getNewVoteId();
	
	public ImmutableMap<Integer, Municipality> getMunicipalities();
	
	public ImmutableMap<Integer, District> getDistricts();
	
	public ImmutableMap<Integer, State> getStates();
	
	public ImmutableMap<Integer, Vote> getVotes();

}
