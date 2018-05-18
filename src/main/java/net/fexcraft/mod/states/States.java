package net.fexcraft.mod.states;

import com.google.common.collect.TreeBasedTable;
import java.io.File;
import java.util.TreeMap;
import java.util.UUID;

import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.util.AccountManager;
import net.fexcraft.mod.lib.capabilities.sign.SignCapabilityUtil;
import net.fexcraft.mod.lib.network.PacketHandler;
import net.fexcraft.mod.lib.network.handlers.NBTTagCompoundPacketHandler;
import net.fexcraft.mod.lib.perms.PermManager;
import net.fexcraft.mod.lib.perms.PermissionNode;
import net.fexcraft.mod.lib.util.common.Static;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.api.District;
import net.fexcraft.mod.states.api.Municipality;
import net.fexcraft.mod.states.api.State;
import net.fexcraft.mod.states.api.capabilities.ChunkCapability;
import net.fexcraft.mod.states.api.capabilities.PlayerCapability;
import net.fexcraft.mod.states.api.capabilities.SignTileEntityCapability;
import net.fexcraft.mod.states.api.capabilities.WorldCapability;
import net.fexcraft.mod.states.guis.GuiHandler;
import net.fexcraft.mod.states.guis.Listener;
import net.fexcraft.mod.states.guis.Receiver;
import net.fexcraft.mod.states.impl.SignShop;
import net.fexcraft.mod.states.impl.capabilities.ChunkCapabilityUtil;
import net.fexcraft.mod.states.impl.capabilities.PlayerCapabilityUtil;
import net.fexcraft.mod.states.impl.capabilities.SignTileEntityCapabilityUtil;
import net.fexcraft.mod.states.impl.capabilities.WorldCapabilityUtil;
import net.fexcraft.mod.states.packets.ImagePacket;
import net.fexcraft.mod.states.packets.ImagePacketHandler;
import net.fexcraft.mod.states.util.Config;
import net.fexcraft.mod.states.util.Sender;
import net.fexcraft.mod.states.util.StatesPermissions;
import net.fexcraft.mod.states.util.UpdateHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = States.MODID, name = "States", version = States.VERSION, dependencies = "required-after:fcl", /*serverSideOnly = true,*/ guiFactory = "net.fexcraft.mod.states.util.GuiFactory", acceptedMinecraftVersions = "*", acceptableRemoteVersions = "*")
public class States {
	
	public static final String VERSION = "1.1";
	public static final String MODID = "states";
	public static String ADMIN_PERM = "states.admin";
	//
	public static final TreeBasedTable<Integer, Integer, Chunk> CHUNKS = TreeBasedTable.create();
	public static final TreeMap<Integer, District> DISTRICTS = new TreeMap<Integer, District>();
	public static final TreeMap<Integer, Municipality> MUNICIPALITIES = new TreeMap<Integer, Municipality>();
	public static final TreeMap<Integer, State> STATES = new TreeMap<Integer, State>();
	public static final TreeMap<UUID, PlayerCapability> PLAYERS = new TreeMap<UUID, PlayerCapability>();
	//
	public static final String DEF_UUID = "66e70cb7-1d96-487c-8255-5c2d7a2b6a0e";
	public static final String CONSOLE_UUID = "f78a4d8d-d51b-4b39-98a3-230f2de0c670";
	public static final String DEFAULT_ICON = "https://i.imgur.com/LwuKE0b.png";
	public static Account SERVERACCOUNT;
	//
	@Mod.Instance(MODID)
	public static States INSTANCE;
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event){
		Config.initialize(event);
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
		PacketHandler.getInstance().registerMessage(ImagePacketHandler.Client.class, ImagePacket.class, 29910, Side.CLIENT);
		PacketHandler.getInstance().registerMessage(ImagePacketHandler.Server.class, ImagePacket.class, 29911, Side.SERVER);
		//
		PermManager.setEnabled(MODID);
		PermManager.add(ADMIN_PERM, PermissionNode.Type.BOOLEAN, false, true);
		//PlayerPerms.addAdditionalData(GenericPlayer.class);
		//
		SERVERACCOUNT = AccountManager.INSTANCE.getAccount("server", "states", true);
		CapabilityManager.INSTANCE.register(SignTileEntityCapability.class, new SignTileEntityCapabilityUtil.Storage(), new SignTileEntityCapabilityUtil.Callable());
		CapabilityManager.INSTANCE.register(ChunkCapability.class, new ChunkCapabilityUtil.Storage(), new ChunkCapabilityUtil.Callable());
		CapabilityManager.INSTANCE.register(WorldCapability.class, new WorldCapabilityUtil.Storage(), new WorldCapabilityUtil.Callable());
		CapabilityManager.INSTANCE.register(PlayerCapability.class, new PlayerCapabilityUtil.Storage(), new PlayerCapabilityUtil.Callable());
		//
		StatesPermissions.init();
		SignCapabilityUtil.addListener(SignShop.class);
		//
		UpdateHandler.initialize();
	}
	
	public static final File getWorldDirectory(){
		return Static.getServer().getEntityWorld().getSaveHandler().getWorldDirectory();
	}
	
	public static final File getSaveDirectory(){
		return new File(Static.getServer().getEntityWorld().getSaveHandler().getWorldDirectory(), "states/");
	}
	
	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event){
		if(Sender.RECEIVER == null){
			Config.updateWebHook();
		}
	}
	
	@Mod.EventHandler
	public void serverStopping(FMLServerStoppingEvent event){
		if(Sender.RECEIVER != null){
			Sender.RECEIVER.halt();
		}
	}

}
