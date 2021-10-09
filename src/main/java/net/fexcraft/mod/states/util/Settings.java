package net.fexcraft.mod.states.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.fexcraft.mod.states.data.IconHolder;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class Settings {
	
	public static File CONFIG_PATH;
	//
	public static String SERVER_ICON = "http://fexcraft.net/files/mod_data/states/default_server_icon.png";
	public static String DEFAULT_ICON = "http://fexcraft.net/files/mod_data/states/default_icon.png";
	public static final String BROADCASTER = "http://fexcraft.net/files/mod_data/states/broadcaster_icon.png";
	public static IconHolder SERVER_ICONHOLDER = new IconHolder(SERVER_ICON);
	public static boolean SAVE_SPACED_JSON;
	//
	public static final String DEFAULT_CAT = "default_settings";
	public static final String CLIENT_CAT = "client_settings";
	//
	private static Configuration config;
	
	public static void initialize(FMLPreInitializationEvent event){
		CONFIG_PATH = event.getSuggestedConfigurationFile().getParentFile();
		config = new Configuration(event.getSuggestedConfigurationFile(), "1.0", true);
		config.load();
		config.setCategoryRequiresMcRestart(DEFAULT_CAT, false);
		config.setCategoryRequiresWorldRestart(DEFAULT_CAT, true);
		config.setCategoryComment(DEFAULT_CAT, "General Mod Settings.");
		config.setCategoryRequiresMcRestart(CLIENT_CAT, false);
		config.setCategoryRequiresWorldRestart(CLIENT_CAT, false);
		config.setCategoryComment(CLIENT_CAT, "Client Settings.");
		refresh();
		config.save();
	}

	public static List<IConfigElement> getList(){
		List<IConfigElement> list = new ArrayList<IConfigElement>();
		list.add(new ConfigElement(Settings.getConfig().getCategory(DEFAULT_CAT)));
		list.add(new ConfigElement(Settings.getConfig().getCategory(CLIENT_CAT)));
		return list;
	}

	public static final Configuration getConfig(){
		return config;
	}
	
	private static void refresh(){
		SERVER_ICON = config.getString("server_icon", DEFAULT_CAT, SERVER_ICON, "Server Icon to be shown in the Location Update GUI.");
		SERVER_ICONHOLDER.set(SERVER_ICON);
		DEFAULT_ICON = config.getString("default_icon", DEFAULT_CAT, DEFAULT_ICON, "Default Dis/Mun/Cou/State Icon to be shown in the Location Update GUI.");
		SAVE_SPACED_JSON = config.getBoolean("save_spaced_json", DEFAULT_CAT, false, "If true, the JSON will be formatted to be easily readable, otherwise if false it will not have any spacing, to save on disk and load time.");
	}
	
}