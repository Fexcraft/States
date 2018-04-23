package net.fexcraft.mod.states.util.world;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import net.fexcraft.mod.states.States;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

public class WorldCap implements WorldCapability {
	
	private World world;
	private int municipalities = -1, districts = -1;

	@Override
	public void setWorld(World world){
		this.world = world;
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
			districts = i - 2;//recompensate for wilderness & "spawn"
		}
	}
	
}
