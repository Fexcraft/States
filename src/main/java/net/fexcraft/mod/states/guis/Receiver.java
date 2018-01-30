package net.fexcraft.mod.states.guis;

import net.fexcraft.mod.lib.api.network.IPacketListener;
import net.fexcraft.mod.lib.network.packet.PacketNBTTagCompound;
import net.fexcraft.mod.lib.util.common.Print;

public class Receiver implements IPacketListener<PacketNBTTagCompound> {

	@Override
	public String getId(){
		return "states:gui";
	}

	@Override
	public void process(PacketNBTTagCompound packet, Object[] objs){
		Print.debug(packet.nbt.toString(), objs);
		//
	}

}
