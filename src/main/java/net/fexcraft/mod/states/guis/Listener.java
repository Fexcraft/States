package net.fexcraft.mod.states.guis;

import java.awt.image.BufferedImage;

import net.fexcraft.mod.lib.api.network.IPacketListener;
import net.fexcraft.mod.lib.network.PacketHandler;
import net.fexcraft.mod.lib.network.packet.PacketNBTTagCompound;
import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.packets.ImagePacket;
import net.fexcraft.mod.states.util.ImageCache;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Listener implements IPacketListener<PacketNBTTagCompound> {

	@Override
	public String getId(){
		return "states:gui";
	}

	@Override
	public void process(PacketNBTTagCompound packet, Object[] objs){
		Print.debug(packet.nbt.toString(), objs);
		EntityPlayerMP player = (EntityPlayerMP)objs[0];
		World world = player.world;
		BlockPos pos = player.getPosition();
		switch(packet.nbt.getInteger("from")){
			case 0:{
				player.openGui(States.INSTANCE, packet.nbt.getInteger("button") + 1, world, pos.getX(), pos.getY(), pos.getZ());
				return;
			}
			case 1:{
				BufferedImage image = ImageCache.getImage(packet.nbt.getInteger("chunk_x"), packet.nbt.getInteger("chunk_z"), packet.nbt.getString("view"), true);
				PacketHandler.getInstance().sendTo(new ImagePacket("area_view", image), player);
			}
			default: return;
		}
	}

	/*private void sendBack(NBTTagCompound nbt, EntityPlayerMP player){
		PacketHandler.getInstance().sendTo(new PacketNBTTagCompound(nbt), player);
	}*/

}
