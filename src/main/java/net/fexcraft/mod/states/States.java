package net.fexcraft.mod.states;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.capabilities.sign.SignCapabilitySerializer;
import net.fexcraft.lib.mc.network.PacketHandler;
import net.fexcraft.lib.mc.network.handlers.NBTTagCompoundPacketHandler;
import net.fexcraft.lib.mc.registry.FCLRegistry;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.states.cmds.*;
import net.fexcraft.mod.states.data.Chunk;
import net.fexcraft.mod.states.data.ChunkPos;
import net.fexcraft.mod.states.data.District;
import net.fexcraft.mod.states.data.Municipality;
import net.fexcraft.mod.states.data.State;
import net.fexcraft.mod.states.data.Vote;
import net.fexcraft.mod.states.data.capabilities.ChunkCapability;
import net.fexcraft.mod.states.data.capabilities.PlayerCapability;
import net.fexcraft.mod.states.data.capabilities.WorldCapability;
import net.fexcraft.mod.states.guis.GuiHandler;
import net.fexcraft.mod.states.guis.Listener;
import net.fexcraft.mod.states.guis.Receiver;
import net.fexcraft.mod.states.impl.SignMailbox;
import net.fexcraft.mod.states.impl.SignShop;
import net.fexcraft.mod.states.impl.capabilities.ChunkCapabilityUtil;
import net.fexcraft.mod.states.impl.capabilities.PlayerCapabilityUtil;
import net.fexcraft.mod.states.impl.capabilities.WorldCapabilityUtil;
import net.fexcraft.mod.states.packets.ImagePacket;
import net.fexcraft.mod.states.packets.ImagePacketHandler;
import net.fexcraft.mod.states.util.*;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = States.MODID, name = "States", version = States.VERSION, dependencies = "required-after:fcl", /*serverSideOnly = true,*/ guiFactory = "net.fexcraft.mod.states.util.GuiFactory", acceptedMinecraftVersions = "*", acceptableRemoteVersions = "*")
public class States {
	
	public static final String VERSION = "@VERSION@";
	public static final String MODID = "states";
	public static final String PREFIX = "&0[&2States&0]";
	//
	public static final ConcurrentHashMap<ChunkPos, Chunk> CHUNKS = new ConcurrentHashMap<>();
	public static final TreeMap<Integer, District> DISTRICTS = new TreeMap<>();
	public static final TreeMap<Integer, Municipality> MUNICIPALITIES = new TreeMap<>();
	public static final TreeMap<Integer, State> STATES = new TreeMap<>();
	public static final TreeMap<UUID, PlayerCapability> PLAYERS = new TreeMap<>();
	public static final TreeMap<Integer, List<ChunkPos>> LOADED_CHUNKS = new TreeMap<>();
	public static final TreeMap<Integer, Vote> VOTES = new TreeMap<>();
	//
	public static final String DEF_UUID = "66e70cb7-1d96-487c-8255-5c2d7a2b6a0e";
	public static final String CONSOLE_UUID = "f78a4d8d-d51b-4b39-98a3-230f2de0c670";
	public static final String DEFAULT_ICON = "https://i.imgur.com/LwuKE0b.png";
	public static Account SERVERACCOUNT;
	//
	@Mod.Instance(MODID)
	public static States INSTANCE;
	private static Timer TAX_TIMER, DATA_MANAGER;//, IMG_TIMER;
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event){
		StConfig.initialize(event); 
		FMLCommonHandler.instance().registerCrashCallable(new CrashHook());
		SignCapabilitySerializer.addListener(SignShop.class);
		SignCapabilitySerializer.addListener(SignMailbox.class);
		//CapabilityManager.INSTANCE.register(SignTileEntityCapability.class, new SignTileEntityCapabilityUtil.Storage(), new SignTileEntityCapabilityUtil.Callable());
		CapabilityManager.INSTANCE.register(ChunkCapability.class, new ChunkCapabilityUtil.Storage(), new ChunkCapabilityUtil.Callable());
		CapabilityManager.INSTANCE.register(WorldCapability.class, new WorldCapabilityUtil.Storage(), new WorldCapabilityUtil.Callable());
		CapabilityManager.INSTANCE.register(PlayerCapability.class, new PlayerCapabilityUtil.Storage(), new PlayerCapabilityUtil.Callable());
		ForgeChunkManager.setForcedChunkLoadingCallback(this, new ForcedChunksManager());
		FCLRegistry.newAutoRegistry(MODID);
	}
	
	@Mod.EventHandler
	public void properInit(FMLInitializationEvent event){
		NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, new GuiHandler());
		if(event.getSide().isClient()){
			MinecraftForge.EVENT_BUS.register(new net.fexcraft.mod.states.guis.LocationUpdate());
		}
	}
	
	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event){
		NBTTagCompoundPacketHandler.addListener(Side.SERVER, new Listener());
		NBTTagCompoundPacketHandler.addListener(Side.CLIENT, new Receiver());
		//
		Perms.init(); SERVERACCOUNT = DataManager.getAccount("server:states", false, true);
		//
		PacketHandler.getInstance().registerMessage(ImagePacketHandler.Client.class, ImagePacket.class, 29910, Side.CLIENT);
		PacketHandler.getInstance().registerMessage(ImagePacketHandler.Server.class, ImagePacket.class, 29911, Side.SERVER);
		UpdateHandler.initialize();
	}
	
	private static File statesdir;
	
	public static final File updateSaveDirectory(World world){
		return statesdir = new File(world.getSaveHandler().getWorldDirectory(), "states/");
	}
	
	public static final File getSaveDirectory(){
		return statesdir;
	}
	
	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event){
		StateUtil.loadNameCache();
		if(MessageSender.RECEIVER == null) StConfig.updateWebHook();
		//
		AliasLoader.load();
		event.registerServerCommand(new AdminCmd());
		event.registerServerCommand(new ChunkCmd());
		event.registerServerCommand(new DebugCmd());
		event.registerServerCommand(new DistrictCmd());
		event.registerServerCommand(new GuiCmd());
		event.registerServerCommand(new MailCmd());
		event.registerServerCommand(new MunicipalityCmd());
		event.registerServerCommand(new NickCmd());
		event.registerServerCommand(new RuleCmd());
		event.registerServerCommand(new StateCmd());
		event.registerServerCommand(new VoteCmd());
	}
	
	@Mod.EventHandler
	public void serverStarted(FMLServerStartedEvent event){
		ForcedChunksManager.check();
		//
		LocalDateTime midnight = LocalDateTime.of(LocalDate.now(ZoneOffset.systemDefault()), LocalTime.MIDNIGHT);
		long mid = midnight.toInstant(ZoneOffset.UTC).toEpochMilli(); long date = Time.getDate();
		while((mid += StConfig.TAX_INTERVAL) < date);
		if(TAX_TIMER == null && StConfig.TAX_ENABLED){
			(TAX_TIMER = new Timer()).schedule(new TaxSystem(), new Date(mid), StConfig.TAX_INTERVAL);
		}
		if(DATA_MANAGER == null){
			(DATA_MANAGER = new Timer()).schedule(new StateUtil(), new Date(mid), Static.dev() ? 60000 : Time.MIN_MS * 15);
		}
		//
		/*if(IMG_TIMER == null){
			(IMG_TIMER = new Timer()).schedule(new ImageCache(), new Date(mid), 1000);
			if(event.getSide().isClient()){
				IMG_TIMER.schedule(new ImageUtil(), new Date(mid), 1000);
			}
		}*/
	}
	
	@Mod.EventHandler
	public void serverStopping(FMLServerStoppingEvent event){
		if(MessageSender.RECEIVER != null) MessageSender.RECEIVER.halt();
	}

}
