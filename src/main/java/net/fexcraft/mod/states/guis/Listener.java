package net.fexcraft.mod.states.guis;

import java.awt.Color;
import java.awt.image.BufferedImage;

import net.fexcraft.mod.lib.api.network.IPacketListener;
import net.fexcraft.mod.lib.network.PacketHandler;
import net.fexcraft.mod.lib.network.packet.PacketNBTTagCompound;
import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.packets.ImagePacket;
import net.minecraft.block.material.MapColor;
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
				BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
				for(int i = 0; i < 16; i++){
					for(int j = 0; j < 16; j++){
						boolean k = i % 2 == 0 && j % 2 == 0 ? true : false;
						image.setRGB(i, j, k ? new Color(MapColor.BLACK.colorValue).getRGB() : new Color(MapColor.SNOW.colorValue).getRGB());
					}
				}
				PacketHandler.getInstance().sendTo(new ImagePacket("area_view", image), player);
			}
			default: return;
		}
	}

	/*private void sendBack(NBTTagCompound nbt, EntityPlayerMP player){
		PacketHandler.getInstance().sendTo(new PacketNBTTagCompound(nbt), player);
	}*/

}
