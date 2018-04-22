package net.fexcraft.mod.states.api.root;

import java.awt.Color;

import net.fexcraft.mod.lib.util.common.Print;
import net.minecraft.command.ICommandSender;

public interface ColorHolder {
	
	public String getColor();
	
	public void setColor(String newcolor);
	
	public default boolean validateColor(ICommandSender sender, String newcolor){
		if(newcolor.replace("#", "").length() != 6){
			Print.chat(sender, "Invalid colour string!");
			return false;
		}
		if(!newcolor.contains("#")){
			newcolor = "#" + newcolor;
		}
		try{
			Color.decode(newcolor);
		}
		catch(Exception e){
			Print.chat(sender, "Parse Error: " + e.getMessage());
		}
		return true;
	}
	
}
