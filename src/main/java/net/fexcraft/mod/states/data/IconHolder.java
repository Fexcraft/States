package net.fexcraft.mod.states.data;

import static net.fexcraft.mod.states.util.Settings.DEFAULT_ICON;

import java.util.Arrays;
import java.util.List;

import net.fexcraft.app.json.JsonMap;
import net.minecraft.nbt.NBTTagCompound;

public class IconHolder implements Saveable {
	
	private String icon;

	public IconHolder(){}

	public IconHolder(String string){
		icon = string;
	}

	@Override
	public void load(JsonMap obj){
		icon = obj.has("icon") ? obj.get("icon").string_value() : DEFAULT_ICON;
	}

	@Override
	public void save(JsonMap map){
		if(icon != null && !icon.equals(DEFAULT_ICON)) map.add("icon", icon);
	}
	
	/** Get direct, may be null. */
	public String get(){
		return icon;
	}

	/** For the Manager GUI, returns "none" when null. */
	public String getn(){
		return exists() ? icon : "none";
	}

	/** For the Location GUI, returns DEFAULT_ICON when null. */
	public String getnn(){
		return exists() ? icon : DEFAULT_ICON;
	}
	
	public void set(String url){
		this.icon = url;
	}
	
	public boolean exists(){
		return icon != null && icon.length() > 4;
	}

	public void reset(){
		icon = null;
	}
	
	public static final List<String> PACKET_COLOURS = Arrays.asList(new String[]{ "green", "yellow", "red", "blue" });
	
	public static final void writeLocPacketIcon(NBTTagCompound compound, IconHolder icon, int id, String color){
		if(icon != null){
			compound.setString("icon_" + id, icon.getnn());
		}
		else if(color == null){
			compound.setInteger("x_" + id, 64);
			compound.setInteger("y_" + id, 224);
		}
		else{
			compound.setString("color_" + id, color);
		}
	}
	
}
