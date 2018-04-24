package net.fexcraft.mod.states.guis;

import net.fexcraft.mod.lib.api.network.IPacketListener;
import net.fexcraft.mod.lib.network.packet.PacketNBTTagCompound;
import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.lib.util.math.Time;
import net.fexcraft.mod.lib.util.render.ExternalTextureHelper;

public class Receiver implements IPacketListener<PacketNBTTagCompound> {

	@Override
	public String getId(){
		return "states:gui";
	}

	@Override
	public void process(PacketNBTTagCompound packet, Object[] objs){
		Print.debug(packet.nbt.toString(), objs);
		if(!packet.nbt.hasKey("task")){
			return;
		}
		//EntityPlayer player = (EntityPlayer)objs[0];
		switch(packet.nbt.getString("task")){
			case "show_location_update":{
				int t = packet.nbt.hasKey("time") ? packet.nbt.getInteger("time") : 5;
				LocationUpdate.till = Time.getDate() + (t * 2000);
				//
				LocationUpdate.lines[0] = packet.nbt.getString("line0");
				LocationUpdate.lines[1] = packet.nbt.getString("line1");
				LocationUpdate.lines[2] = packet.nbt.getString("line2");
				for(int i = 0; i < 3; i++){
					LocationUpdate.icon[i] = packet.nbt.hasKey("icon_" + i) ? ExternalTextureHelper.get(packet.nbt.getString("icon_" + i)) : null;
					LocationUpdate.x[i] = packet.nbt.hasKey("x_" + i) ? packet.nbt.getInteger("x_" + i) : 0;
					LocationUpdate.y[i] = packet.nbt.hasKey("y_"+ i) ? packet.nbt.getInteger("y_" + i) : 0;
					if(packet.nbt.hasKey("color_" + i)){
						switch(packet.nbt.getString("color_" + i)){
							case "green":{
								LocationUpdate.x[i] =  0; LocationUpdate.y[i] = 224;
								break;
							}
							case "yellow":{
								LocationUpdate.x[i] = 32; LocationUpdate.y[i] = 224;
								break;
							}
							case "red":{
								LocationUpdate.x[i] = 64; LocationUpdate.y[i] = 224;
								break;
							}
							case "blue":{
								LocationUpdate.x[i] = 96; LocationUpdate.y[i] = 224;
								break;
							}
						}
					}
				}
				break;
			}
		}
	}

}
