package net.fexcraft.mod.states.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.states.data.sub.IconHolder;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class StConfig {
	
	public static File CONFIG_PATH;
	//CHUNK
	public static boolean ALLOW_CHUNK_UNCLAIM, ALLOW_CHUNK_OVERCLAIM;
	public static long DEFAULT_CHUNK_PRICE, UNCLAIM_CHUNK_PRICE, OVERCLAIM_CHUNK_PRICE;
	//CREATE/CLAIM/ABANDON
	public static long MUNICIPALITY_CREATION_PRICE, STATE_CREATION_PRICE, DISTRICT_CREATION_PRICE;
	public static long MUNICIPALITY_ABANDONMENT_PRICE, MUNICIPALITY_CLAIM_PRICE;
	//TAX-SYSTEM
	public static long TAX_INTERVAL, LOADED_CHUNKS_TAX;
	public static boolean TAX_OFFLINE_PLAYERS, TAX_ENABLED;
	//WEBHOOK-BOT
	public static String WEBHOOK, BOT_KEY, WEBHOOK_ICON, WEBHOOK_BROADCASTER_NAME;
	public static int BOT_PORT;
	//TRANSIT-ZONES
	public static boolean ALLOW_TRANSIT_ZONES;
	public static int TRANSIT_ZONE_BOTTOM_LIMIT, TRANSIT_ZONE_TOP_LIMIT;
	//LIMITS-OTHER
	public static int MAP_UPDATES_PER_SECOND, CHUNK_PER_CITIZEN, NICKNAME_LENGTH, LOADED_CHUNKS_PER_MUNICIPALITY, CHUNKS_FOR_DISTRICT;
	public static boolean ALLOW_WILDERNESS_ACCESS, STATES_CHAT, ALLOW_MAILBOX_COLLECT;//, FORGE_ADMIN_CHECK;
	public static String SERVER_ICON = "http://fexcraft.net/files/mod_data/states/default_server_icon.png";
	public static String DEFAULT_ICON = "http://fexcraft.net/files/mod_data/states/default_icon.png";
	public static final String BROADCASTER = "http://fexcraft.net/files/mod_data/states/broadcaster_icon.png";
	public static IconHolder SERVER_ICONHOLDER = new IconHolder(SERVER_ICON);
	//CLIENT
	public static boolean SHOW_MINIMAP;
	//
	public static final String DEFAULT_CAT = "Default Settings";
	public static final String CLIENT_CAT = "Client Settings";
	//
	public static ProtectionLevel PROTLVL = ProtectionLevel.BASIC;
	private static Configuration config;
	
	public static void initialize(FMLPreInitializationEvent event){
		CONFIG_PATH = event.getSuggestedConfigurationFile().getParentFile();
		config = new Configuration(event.getSuggestedConfigurationFile(), "1.0", true);
		config.load();
		config.setCategoryRequiresMcRestart(DEFAULT_CAT, false);
		config.setCategoryRequiresWorldRestart(DEFAULT_CAT, true);
		config.setCategoryComment(DEFAULT_CAT, "General State Settings.");
		config.setCategoryRequiresMcRestart(CLIENT_CAT, false);
		config.setCategoryRequiresWorldRestart(CLIENT_CAT, false);
		config.setCategoryComment(CLIENT_CAT, "Client Settings.");
		refresh();
		config.save();
	}

	public static List<IConfigElement> getList(){
		List<IConfigElement> list = new ArrayList<IConfigElement>();
		list.add(new ConfigElement(StConfig.getConfig().getCategory(DEFAULT_CAT)));
		return list;
	}

	public static final Configuration getConfig(){
		return config;
	}
	
	private static void refresh(){
		//CHUNK PRICES
		ALLOW_CHUNK_UNCLAIM = config.getBoolean("allow_chunk_unclaim", DEFAULT_CAT, false, "If Mayors should have permission to unclaim chunks, usually only Admins are allowed to do so.");
		ALLOW_CHUNK_OVERCLAIM = config.getBoolean("allow_chunk_overclaim", DEFAULT_CAT, false, "If claiming over the Municipality's chunk limit should be allowed, with a separate fee for such.");
		DEFAULT_CHUNK_PRICE = config.getInt("default_chunk_price", DEFAULT_CAT, 100000, 0, Integer.MAX_VALUE, "Default price for unclaimed chunks. (1000 == 1F$)");
		UNCLAIM_CHUNK_PRICE = config.getInt("unclaim_chunk_price", DEFAULT_CAT, 1000000, 0, Integer.MAX_VALUE, "Price for unclaiming chunks (if unclaiming is allowed for mayors). (1000 == 1F$)");
		OVERCLAIM_CHUNK_PRICE = config.getInt("overclaim_chunk_price", DEFAULT_CAT, 1000000, 0, Integer.MAX_VALUE, "Price for claiming chunks over the Municipality limit. (1000 == 1F$)");
		//CREATE/CLAIM/ABANDON
		DISTRICT_CREATION_PRICE = config.getInt("district_creation_price", DEFAULT_CAT, 1200000, 0, Integer.MAX_VALUE, "Amount of Money Needded to create a district. (1000 == 1F$)");
		MUNICIPALITY_CREATION_PRICE = config.getInt("municipality_creation_price", DEFAULT_CAT, 2500000, 0, Integer.MAX_VALUE, "Amount of Money needed to create a municipality. (1000 == 1F$)");
		MUNICIPALITY_ABANDONMENT_PRICE = config.getInt("municipality_abandonment_price", DEFAULT_CAT, 10000, 0, Integer.MAX_VALUE, "Amount of Money needed to abandon a municipality. (1000 == 1F$)");
		MUNICIPALITY_CLAIM_PRICE = config.getInt("municipality_claim_price", DEFAULT_CAT, 1000000, 0, Integer.MAX_VALUE, "Amount of Money needed to claim anabandoned municipality. (1000 == 1F$)");
		STATE_CREATION_PRICE = config.getInt("state_creation_price", DEFAULT_CAT, 52000000, 0, Integer.MAX_VALUE, "Amount of Money needed to create a state. (1000 == 1F$)");
		//TAX-SYSTEM
		TAX_ENABLED = config.getBoolean("tax_enabled", DEFAULT_CAT, true, "If the Tax System should be enabled.");
		TAX_INTERVAL = config.getInt("tax_interval", DEFAULT_CAT, (int)Time.DAY_MS, 1000, Integer.MAX_VALUE, "Intervals between tax collection cycles, 1000 = 1 second.");
		TAX_OFFLINE_PLAYERS = config.getBoolean("tax_offline_players", DEFAULT_CAT, false, "If offline (inactive since more than one interval) players should be taxed as well.");
		LOADED_CHUNKS_TAX = config.getInt("loaded_chunks_tax", DEFAULT_CAT, 25000, 0, Integer.MAX_VALUE, "Tax a Municipality has to pay the Server for having force-loaded chunks.");
		//WEBHOOK-BOT
		WEBHOOK = config.getString("discord_webhook", DEFAULT_CAT, "null", "Discord Webhook, set to 'null' to disable!");
		BOT_KEY = config.getString("discord_botkey", DEFAULT_CAT, UUID.randomUUID().toString().replace("-", ""), "A key/token so only an authorized bot can send messages to this server. Can be changed as wanted.");
		BOT_PORT = config.getInt("discord_botport", DEFAULT_CAT, 9910, 8000, Integer.MAX_VALUE, "Port for receiving messages from the bot, set to -1 to disable.");
		WEBHOOK_BROADCASTER_NAME = config.getString("discord_webhook_broadcaster_name", DEFAULT_CAT, "States Broadcaster", "The \"Server's\" name when sending messages to the webhook which aren't from a player, e.g. on server start/stop.");
		WEBHOOK_ICON = config.getString("discord_webhook_icon", DEFAULT_CAT, BROADCASTER, "Icon for the Server Broadcaster, in discord.");
		//TRANSIT-ZONES
		ALLOW_TRANSIT_ZONES = config.getBoolean("allow_temporary_districts", DEFAULT_CAT, true, "If players should be able to claim chunks temporarily.");
		TRANSIT_ZONE_BOTTOM_LIMIT = config.getInt("temporary_district_bottom_limit", DEFAULT_CAT, 50, 0, 127, "Min Height value to which blocks in Temporary claimed Chunks can be accessed.");
		TRANSIT_ZONE_TOP_LIMIT = config.getInt("temporary_district_top_limit", DEFAULT_CAT, 80, 127, 255, "Max Height value to which blocks in Temporary claimed Chunks can be accessed.");
		//LIMITS-OTHER
		PROTLVL = ProtectionLevel.fromString(config.getString("protection_level", DEFAULT_CAT, "basic", "Available: " + ProtectionLevel.allToString()));
		//FORGE_ADMIN_CHECK = config.getBoolean("forge_admin_check", DEFAULT_CAT, true, "If the Forge PermissionsAPI should be used for admin permission checks, when 'false', use '/st-admin toggle' instead!");
		ALLOW_WILDERNESS_ACCESS = config.getBoolean("allow_wilderness_access", DEFAULT_CAT, false, "Should players be able to break, place or interact with blocks in Wilderness? (District:-1)");
		CHUNK_PER_CITIZEN = config.getInt("chunk_per_citizen", DEFAULT_CAT, 16, 1, 4096, "How many chunks a Municipality can CLAIM per citizen inhabiting it.");
		LOADED_CHUNKS_PER_MUNICIPALITY = config.getInt("loaded_chunks_per_municipality", DEFAULT_CAT, 0, 0, 128, "Amount of max (force) loaded chunks a Municipality can have.");
		STATES_CHAT = config.getBoolean("states_chat", DEFAULT_CAT, true, "States chat override.");
		NICKNAME_LENGTH = config.getInt("nickname_length", DEFAULT_CAT, 40, 3, 128, "Max length of Player Nicknames (/nick).");
		ALLOW_MAILBOX_COLLECT = config.getBoolean("allow_mailbox_collect", DEFAULT_CAT, true, "Should players be allowed to use '/mail collect <box-type>'? Allows to collect player mail from non-player mailboxes without being directly nearby or needing permission for the mailbox.");
		MAP_UPDATES_PER_SECOND = config.getInt("map_updates_per_second", DEFAULT_CAT, 20, 0, 128, "Max amount of Map updates per second.");
		CHUNKS_FOR_DISTRICT = config.getInt("chunks_for_district", DEFAULT_CAT, 64, 1, Integer.MAX_VALUE, "Multiplier of chunk claims required to unlock more districts.");
		SERVER_ICON = config.getString("server_icon", DEFAULT_CAT, SERVER_ICON, "Server Icon to be shown in the Location Update GUI.");
		SERVER_ICONHOLDER.set(SERVER_ICON);
		DEFAULT_ICON = config.getString("default_icon", DEFAULT_CAT, DEFAULT_ICON, "Default Dis/Mun/Cou/State Icon to be shown in the Location Update GUI.");
		//CLIENT
		SHOW_MINIMAP = config.getBoolean("show_minimap", CLIENT_CAT, true, "If the States Minimap should be shown.");
		updateWebHook();
	}

	public static void updateWebHook(){
		if(WEBHOOK != null && WEBHOOK.equals("null")){
			WEBHOOK = null;
		}
		if(MessageSender.RECEIVER != null){
			MessageSender.RECEIVER.halt();
		}
		if(WEBHOOK != null && BOT_PORT != -1 /*&& Static.getServer() != null*/){
			MessageSender.RECEIVER = new MessageSender.Receiver();
			MessageSender.RECEIVER.start();
		}
	}
	
	public static enum ProtectionLevel {
		
		BASIC, ADVANCED, ABSOLUTE;
		
		public static ProtectionLevel fromString(String string){
			if(string.equals(ABSOLUTE.name().toLowerCase())) return ABSOLUTE;
			else if(string.equals(ADVANCED.name().toLowerCase())) return ADVANCED;
			else if(string.equals(BASIC.name().toLowerCase())) return BASIC;
			else return ProtectionLevel.BASIC;
		}

		public static String allToString(){
			String str = new String();
			for(ProtectionLevel level : values()){
				str += level.name().toLowerCase() + ", ";
			} return str;
		}
		
	}
	
}