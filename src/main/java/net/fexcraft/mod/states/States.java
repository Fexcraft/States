package net.fexcraft.mod.states;

import java.io.File;
import java.util.TreeMap;

import com.google.common.collect.TreeBasedTable;

import net.fexcraft.mod.lib.util.common.Static;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.api.District;
import net.fexcraft.mod.states.util.Config;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = States.MODID, name = "States", version = States.VERSION, dependencies = "required-after:fcl", /*serverSideOnly = true,*/ guiFactory = "net.fexcraft.mod.states.util.GuiFactory", acceptedMinecraftVersions = "*", acceptableRemoteVersions = "*")
public class States {
	
	public static final String VERSION = "1.0";
	public static final String MODID = "states";
	//
	public static final TreeBasedTable<Integer, Integer, Chunk> CHUNKS = TreeBasedTable.create();
	public static final TreeMap<Integer, District> DISTRICTS = new TreeMap<Integer, District>();
	//
	public static final String DEF_UUID = "66e70cb7-1d96-487c-8255-5c2d7a2b6a0e";
	public static final String CONSOLE_UUID = "f78a4d8d-d51b-4b39-98a3-230f2de0c670";
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event){
		Config.initialize(event);
	}
	
	@Mod.EventHandler
	public void properInit(FMLInitializationEvent event){
		//
	}
	
	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event){
		//
	}
	
	public static final File getWorldDirectory(){
		return Static.getServer().getEntityWorld().getSaveHandler().getWorldDirectory();
	}

}
