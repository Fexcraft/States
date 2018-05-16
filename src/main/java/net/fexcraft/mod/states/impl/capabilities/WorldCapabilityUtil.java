package net.fexcraft.mod.states.impl.capabilities;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

import com.google.common.collect.ImmutableMap;

import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.District;
import net.fexcraft.mod.states.api.Municipality;
import net.fexcraft.mod.states.api.State;
import net.fexcraft.mod.states.api.capabilities.StatesCapabilities;
import net.fexcraft.mod.states.api.capabilities.WorldCapability;
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
		private int municipalities = -1, districts = -1, states = -1;

		@Override
		public void setWorld(World world){
			//this.world = world;
		}

		@Override
		public int getNewMunicipalityId() throws Exception {
			checkMunicipalityAmount();
			return municipalities + 1;
		}

		private void checkMunicipalityAmount() throws Exception {
			if(municipalities <= 2){
				File folder = new File(States.getSaveDirectory(), "municipalitites/");
				if(!folder.exists()){
					//this bad...
					throw new Exception("Missing Municipalities Save Location for this World.");
				}
				if(!folder.isDirectory()){
					throw new Exception("Municipalities File is not Directory.");
				}
				int i = 0;
				for(File file : folder.listFiles()){
					if(FilenameUtils.isExtension(file.getName(), "json")){
						i++;
					}
					else{
						throw new Exception("Found file in Municipalities Directory which shouldn't be there.");
					}
				}
				municipalities = i - 2;//recompensate for wilderness & "spawn"
			}
		}

		@Override
		public NBTBase writeToNBT(Capability<WorldCapability> capability, EnumFacing side){
			NBTTagCompound compound = new NBTTagCompound();
			return compound;
		}

		@Override
		public void readFromNBT(Capability<WorldCapability> capability, EnumFacing side, NBTBase nbt){
			if(nbt == null || nbt instanceof NBTTagCompound == false){
				try{
					checkStatesAmount();
					checkMunicipalityAmount();
					checkDistrictAmount();
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
			else{
				NBTTagCompound compound = (NBTTagCompound) nbt;
				municipalities = compound.getInteger("Municipalities");
			}
		}

		private void checkStatesAmount() throws Exception {
			if(states <= 2){
				File folder = new File(States.getSaveDirectory(), "states/");
				if(!folder.exists()){
					//this bad...
					throw new Exception("Missing States Save Location for this World.");
				}
				if(!folder.isDirectory()){
					throw new Exception("States File is not Directory.");
				}
				int i = 0;
				for(File file : folder.listFiles()){
					if(FilenameUtils.isExtension(file.getName(), "json")){
						i++;
					}
					else{
						throw new Exception("Found file in States Directory which shouldn't be there.");
					}
				}
				states = i - 2;//recompensate for neutral territory & "spawn"
			}
		}

		@Override
		public int getNewStateId() throws Exception {
			checkStatesAmount();
			return states + 1;
		}

		@Override
		public int getNewDistrictId() throws Exception {
			checkDistrictAmount();
			return districts + 1;
		}

		private void checkDistrictAmount() throws Exception {
			if(districts <= 2){
				File folder = new File(States.getSaveDirectory(), "districts/");
				if(!folder.exists()){
					//this bad...
					throw new Exception("Missing Districts Save Location for this World.");
				}
				if(!folder.isDirectory()){
					throw new Exception("Districts File is not Directory.");
				}
				int i = 0;
				for(File file : folder.listFiles()){
					if(FilenameUtils.isExtension(file.getName(), "json")){
						i++;
					}
					else{
						throw new Exception("Found file in Districts Directory which shouldn't be there.");
					}
				}
				districts = i - 3;//recompensate for wilderness, "spawn" & transit zone
			}
		}

		@Override
		public ImmutableMap<Integer, District> getDistricts(){
			return ImmutableMap.copyOf(States.DISTRICTS);
		}

		@Override
		public ImmutableMap<Integer, Municipality> getMunicipalities(){
			return ImmutableMap.copyOf(States.MUNICIPALITIES);
		}

		@Override
		public ImmutableMap<Integer, State> getStates(){
			return ImmutableMap.copyOf(States.STATES);
		}
		
	}

}
