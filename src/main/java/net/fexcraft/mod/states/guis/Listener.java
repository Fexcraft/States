package net.fexcraft.mod.states.guis;

import net.fexcraft.mod.lib.api.network.IPacketListener;
import net.fexcraft.mod.lib.network.packet.PacketNBTTagCompound;
import net.fexcraft.mod.lib.util.common.Print;

public class Listener implements IPacketListener<PacketNBTTagCompound> {

	@Override
	public String getId(){
		return "states:gui";
	}

	@Override
	public void process(PacketNBTTagCompound packet, Object[] objs){
		Print.debug(packet.nbt.toString(), objs);
		//EntityPlayerMP player = (EntityPlayerMP)objs[0];
		switch(packet.nbt.getInteger("from")){
			case 0:{
				switch(packet.nbt.getInteger("button")){
					case 0:{
						//Chunk View
						
						return;
					}
					case 1:{
						//Districts
						
						return;
					}
					case 2:{
						//Municipalities
						
						return;
					}
					case 3:{
						//States
						
						return;
					}
					case 4:{
						//Unions
						
						return;
					}
					case 5:{
						//empty
						
						return;
					}
					case 6:{
						//Companies
						
						return;
					}
					case 7:{
						//Player Settings
						
						return;
					}
					case 8:{
						//empty
						return;
					}
				}
			}
			default: return;
		}
	}

	/*private void sendBack(NBTTagCompound nbt, EntityPlayerMP player){
		PacketHandler.getInstance().sendTo(new PacketNBTTagCompound(nbt), player);
	}*/

}
