package net.fexcraft.mod.states.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.fexcraft.mod.lib.util.json.JsonUtil;
import net.fexcraft.mod.states.api.MunicipalityType;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class Config {
	
	public static File CONFIG_PATH;
	public static long DEFAULT_CHUNK_PRICE;
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
	}
	
}