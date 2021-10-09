package net.fexcraft.mod.states;

import java.io.File;

import net.fexcraft.mod.states.data.chunk.ChunkCap;
import net.fexcraft.mod.states.db.Database;
import net.fexcraft.mod.states.db.JsonFileDB;
import net.fexcraft.mod.states.util.ChunkCapabilityUtil;
import net.fexcraft.mod.states.util.CrashHook;
import net.fexcraft.mod.states.util.Settings;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = States.MODID, name = "States", version = States.VERSION, dependencies = "required-after:fcl", guiFactory = "net.fexcraft.mod.states.util.GuiFactory", acceptedMinecraftVersions = "*", acceptableRemoteVersions = "*")
public class States {

	public static final String VERSION = "@VERSION@";
	public static final String MODID = "states";
	public static final String PREFIX = "&0[&2States&0]";
	@Mod.Instance(MODID)
	public static States INSTANCE;
	public static Database DB = new JsonFileDB();
	public static File STATES_DIR;
	

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event){
		Settings.initialize(event);
		FMLCommonHandler.instance().registerCrashCallable(new CrashHook());
		CapabilityManager.INSTANCE.register(ChunkCap.class, new ChunkCapabilityUtil.Storage(), new ChunkCapabilityUtil.Callable());
	}
	

	@Mod.EventHandler
	public void init(FMLInitializationEvent event){
		
	}
	
	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event){
		
	}
	
	public static final File updateSaveDirectory(World world){
		return STATES_DIR = new File(world.getSaveHandler().getWorldDirectory(), "states/");
	}
	
	public static final File getSaveDirectory(){
		return STATES_DIR;
	}
	
}
