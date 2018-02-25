package net.fexcraft.mod.states.util;

import java.util.UUID;

import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.lib.util.common.Formatter;
import net.fexcraft.mod.lib.util.common.Static;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.api.ChunkType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.text.TextComponentString;

public class SignUtil {
	
	public static void updateChunkState(EntityPlayer player, Chunk chunk, TileEntitySign sign){
		if(chunk.getPrice() > 0){
			sign.signText[1] = new TextComponentString(Formatter.format("&2For Sale!"));
			sign.signText[2] = new TextComponentString(Config.getWorthAsString(chunk.getPrice()));
			sign.signText[3] = new TextComponentString(Formatter.format("&2" + (chunk.getType() == ChunkType.PRIVATE ? Static.getPlayerNameByUUID(UUID.fromString(chunk.getOwner())) : chunk.getOwner())));
		}
		else{
			sign.signText[1] = new TextComponentString(Formatter.format(chunk.getType() == ChunkType.PRIVATE ? "&cPrivate Property" : "&2Managed Property"));
			sign.signText[2] = new TextComponentString("");
			sign.signText[3] = new TextComponentString(Formatter.format("&2" + (chunk.getType() == ChunkType.PRIVATE ? Static.getPlayerNameByUUID(UUID.fromString(chunk.getOwner())) : chunk.getOwner())));
		}
		sign.signText[0] = new TextComponentString("[States]> Chunk");
		((EntityPlayerMP)player).connection.sendPacket(sign.getUpdatePacket());
	}

}
