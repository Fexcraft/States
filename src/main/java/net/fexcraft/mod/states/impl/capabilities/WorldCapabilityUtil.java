package net.fexcraft.mod.states.impl.capabilities;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

import com.google.common.collect.ImmutableMap;

import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.data.County;
import net.fexcraft.mod.states.data.District;
import net.fexcraft.mod.states.data.Municipality;
import net.fexcraft.mod.states.data.State;
import net.fexcraft.mod.states.data.Vote;
import net.fexcraft.mod.states.data.capabilities.StatesCapabilities;
import net.fexcraft.mod.states.data.capabilities.WorldCapability;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class WorldCapabilityUtil implements ICapabilitySerializable<NBTBase>{
	
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation("states:world");
	private WorldCapability instance;
	
	public WorldCapabilityUtil(net.minecraft.world.World world){
		instance = StatesCapabilities.WORLD.getDefaultInstance();
		instance.setWorld(world);
		StateUtil.CURRENT = instance;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing){
		return capability == StatesCapabilities.WORLD;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing){
		return capability == StatesCapabilities.WORLD ? StatesCapabilities.WORLD.<T>cast(this.instance) : null;
	}

	@Override
	public NBTBase serializeNBT(){
		return StatesCapabilities.WORLD.getStorage().writeNBT(StatesCapabilities.WORLD, instance, null);
	}

	@Override
	public void deserializeNBT(NBTBase nbt){
		StatesCapabilities.WORLD.getStorage().readNBT(StatesCapabilities.WORLD, instance, null, nbt);
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
	
	public static class Callable implements java.util.concurrent.Callable<WorldCapability> {

		@Override
		public WorldCapability call() throws Exception {
			return new Implementation();
		}
		
	}
	
	//
	
	public static class Implementation implements WorldCapability {
		
		//private World world;

		@Override
		public void setWorld(World world){
			//this.world = world;
		}

		@Override
		public NBTBase writeToNBT(Capability<WorldCapability> capability, EnumFacing side){
			return new NBTTagCompound();
		}

		@Override
		public void readFromNBT(Capability<WorldCapability> capability, EnumFacing side, NBTBase nbt){
			//
		}

		@Override
		public int getNewMunicipalityId() throws Exception {
			File folder = new File(States.getSaveDirectory(), "municipalitites/");
			if(!folder.exists()) folder.mkdirs();
			int newid = -1;
			for(File file : folder.listFiles()){
				if(FilenameUtils.isExtension(file.getName(), "json")){ newid++; }
			}
			while(Municipality.getMunicipalityFile(newid).exists()) newid++;
			return newid;
		}

		@Override
		public int getNewDistrictId(){
			File folder = new File(States.getSaveDirectory(), "districts/");
			if(!folder.exists()) folder.mkdirs();
			int newid = -2;
			for(File file : folder.listFiles()){
				if(FilenameUtils.isExtension(file.getName(), "json")){ newid++; }
			}
			while(District.getDistrictFile(newid).exists()) newid++;
			return newid;
		}

		@Override
		public int getNewStateId(){
			File folder = new File(States.getSaveDirectory(), "states/");
			if(!folder.exists()) folder.mkdirs();
			int newid = -1;
			for(File file : folder.listFiles()){
				if(FilenameUtils.isExtension(file.getName(), "json")){ newid++; }
			}
			while(State.getStateFile(newid).exists()) newid++;
			return newid;
		}

		@Override
		public int getNewVoteId(){
			File folder = new File(States.getSaveDirectory(), "votes/");
			if(!folder.exists()) folder.mkdirs();
			int newid = 0;
			for(File file : folder.listFiles()){
				if(FilenameUtils.isExtension(file.getName(), "json")){ newid++; }
			}
			while(Vote.getVoteFile(newid).exists()) newid++;
			return newid;
		}

		@Override
		public int newCountyId(){
			File folder = new File(States.getSaveDirectory(), "counties/");
			if(!folder.exists()) folder.mkdirs();
			int newid = -1;
			for(File file : folder.listFiles()){
				if(FilenameUtils.isExtension(file.getName(), "json")){ newid++; }
			}
			while(County.getCountyFile(newid).exists()) newid++;
			return newid;
		}

		@Override
		public ImmutableMap<Integer, Municipality> getMunicipalities(){
			return ImmutableMap.copyOf(States.MUNICIPALITIES);
		}

		@Override
		public ImmutableMap<Integer, District> getDistricts(){
			return ImmutableMap.copyOf(States.DISTRICTS);
		}

		@Override
		public ImmutableMap<Integer, State> getStates(){
			return ImmutableMap.copyOf(States.STATES);
		}

		@Override
		public ImmutableMap<Integer, Vote> getVotes(){
			return ImmutableMap.copyOf(States.VOTES);
		}
		
	}

}
