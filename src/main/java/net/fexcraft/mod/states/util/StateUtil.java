package net.fexcraft.mod.states.util;

import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.Chunk;
import net.minecraft.entity.player.EntityPlayer;

public class StateUtil {

	public static Chunk getChunk(EntityPlayer player){
		net.minecraft.world.chunk.Chunk chunk = player.world.getChunkFromBlockCoords(player.getPosition());
		return States.CHUNKS.get(chunk.x, chunk.z);
	}

}
