package net.fexcraft.mod.states.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.fexcraft.mod.lib.util.common.Static;
import net.fexcraft.mod.lib.util.json.JsonUtil;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.MunicipalityType;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class Config {
	
	public static File CONFIG_PATH;
	public static long DEFAULT_CHUNK_PRICE, MUNICIPALITY_CREATION_PRICE, STATE_CREATION_PRICE;
	public static int MAP_UPDATES_PER_TICK, BOT_PORT, TRANSIT_ZONE_BOTTOM_LIMIT, TRANSIT_ZONE_TOP_LIMIT, CHUNK_PER_CITIZEN;
	public static boolean ALLOW_WILDERNESS_ACCESS, ALLOW_TRANSIT_ZONES;
	public static String WEBHOOK, BOT_KEY, WEBHOOK_ICON;
	//
	public static final String DEFAULT_CAT = "Default Settings";
	//
	private static Configuration config;
	
	public static void initialize(FMLPreInitializationEvent event){
		CONFIG_PATH = event.getSuggestedConfigurationFile().getParentFile();
		config = new Configuration(event.getSuggestedConfigurationFile(), "1.0", true);
		config.load();
		config.setCategoryRequiresMcRestart(DEFAULT_CAT, true);
		config.setCategoryRequiresWorldRestart(DEFAULT_CAT, true);
		config.setCategoryComment(DEFAULT_CAT, "General State Settings.");
		refresh();
		config.save();
	}

	public static List<IConfigElement> getList(){
		List<IConfigElement> list = new ArrayList<IConfigElement>();
		list.add(new ConfigElement(Config.getConfig().getCategory(DEFAULT_CAT)));
		return list;
	}

	public static final Configuration getConfig(){
		return config;
	}
	
	private static void refresh(){
		DEFAULT_CHUNK_PRICE = config.getInt("default_chunk_price", DEFAULT_CAT, 100000, 0, Integer.MAX_VALUE, "Default price for unclaimed chunks. (1000 == 1F$)");
		{
			String[] arr = config.getStringList("municipality_types", DEFAULT_CAT, MunicipalityType.DEFAULTS, "Municipality Sizes");
			MunicipalityType.clearEntries();
			for(String str : arr){ new MunicipalityType(JsonUtil.getFromString(str).getAsJsonObject()); }
		}
		MAP_UPDATES_PER_TICK = config.getInt("map_updates_per_tick", DEFAULT_CAT, 1, 0, 128, "Max amount of Map updates per Server Tick.");
		ALLOW_WILDERNESS_ACCESS = config.getBoolean("allow_wilderness_access", DEFAULT_CAT, false, "Should players be able to break, place or interact with blocks in Wilderness? (District:-1)");
		MUNICIPALITY_CREATION_PRICE = config.getInt("municipality_creation_price", DEFAULT_CAT, 2500000, 0, Integer.MAX_VALUE, "Amount of Money needed to create a municipality. (1000 == 1F$)");
		STATE_CREATION_PRICE = config.getInt("state_creation_price", DEFAULT_CAT, 52000000, 0, Integer.MAX_VALUE, "Amount of Money needed to create a state. (1000 == 1F$)");
		WEBHOOK = config.getString("discord_webhook", DEFAULT_CAT, "null", "Discord Webhook, set to 'null' to disable!");
		BOT_KEY = config.getString("discord_botkey", DEFAULT_CAT, UUID.randomUUID().toString().replace("-", ""), "A key/token so only an authorized bot can send messages to this server. Can be changed as wanted.");
		BOT_PORT = config.getInt("discord_botport", DEFAULT_CAT, 9910, 8000, Integer.MAX_VALUE, "Port for receiving messages from the bot, set to -1 to disable.");
		WEBHOOK_ICON = config.getString("discord_webhook_icon", DEFAULT_CAT, States.DEFAULT_ICON, "Icon for the Server Broadcaster, in discord.");
		TRANSIT_ZONE_BOTTOM_LIMIT = config.getInt("temporary_district_bottom_limit", DEFAULT_CAT, 50, 0, 127, "Min Height value to which blocks in Temporary claimed Chunks can be accessed.");
		TRANSIT_ZONE_TOP_LIMIT = config.getInt("temporary_district_top_limit", DEFAULT_CAT, 80, 127, 255, "Max Height value to which blocks in Temporary claimed Chunks can be accessed.");
		ALLOW_TRANSIT_ZONES = config.getBoolean("allow_temporary_districts", DEFAULT_CAT, true, "If players should be able to claim chunks temporarily.");
		CHUNK_PER_CITIZEN = config.getInt("chunk_per_citizen", DEFAULT_CAT, 16, 1, 128, "How many chunks a Municipality can CLAIM per citizen inhabiting it.");
		updateWebHook();
	}

	public static void updateWebHook(){
		if(WEBHOOK != null && WEBHOOK.equals("null")){
			WEBHOOK = null;
		}
		if(Sender.RECEIVER != null){
			Sender.RECEIVER.halt();
		}
		if(WEBHOOK != null && BOT_PORT != -1 && Static.getServer() != null){
			Sender.RECEIVER = new Sender.Receiver();
			Sender.RECEIVER.start();
		}
	}
	
}