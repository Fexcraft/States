package net.fexcraft.mod.states.packets;

import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import net.fexcraft.lib.mc.api.packet.IPacket;
import net.fexcraft.mod.states.util.ImageCache;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class ImagePacket implements IPacket, IMessage {
	
	public BufferedImage image;
	public String target;
	
	public ImagePacket(){}
	
	public ImagePacket(String target, BufferedImage image){
		this.image = image;
		this.target = target;
	}

	@Override
	public void toBytes(ByteBuf buf){
		if(image == null){ image = ImageCache.emptyImage(); }
		int x = image.getWidth(), y = image.getHeight();
		buf.writeInt(x); buf.writeInt(y);
		buf.writeInt(target.length());
		buf.writeCharSequence(target, StandardCharsets.UTF_8);
		//
		StringBuilder str = new StringBuilder();
		for(int i = 0; i < x; i++){
			for(int j = 0; j < y; j++){
				str.append(image.getRGB(i, j) + (j == (y - 1) ? "_" : ","));
			}
		}
		buf.writeInt(str.length());
		buf.writeCharSequence(str, StandardCharsets.UTF_8);
	}

	@Override
	public void fromBytes(ByteBuf buf){
		int x = buf.readInt(), y = buf.readInt();
		image = new BufferedImage(x, y, BufferedImage.TYPE_INT_ARGB);
		int tl = buf.readInt();
		target = buf.readCharSequence(tl, StandardCharsets.UTF_8).toString();
		//
		int l = buf.readInt();
		String str = buf.readCharSequence(l, StandardCharsets.UTF_8).toString();
		//Print.debug(Arrays.asList(str.split("_")));
		String[] arr0 = str.split("_");
		for(int i = 0; i < x; i++){
			String[] arr1 = arr0[i].split(",");
			for(int j = 0; j < y; j++){
				image.setRGB(i, j, Integer.parseInt(arr1[j]));
			}
		}
	}
	
}