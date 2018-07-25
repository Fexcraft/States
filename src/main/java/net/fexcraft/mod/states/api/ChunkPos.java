package net.fexcraft.mod.states.api;

import net.minecraft.util.math.BlockPos;

public class ChunkPos extends net.minecraft.util.math.ChunkPos implements Comparable<net.minecraft.util.math.ChunkPos> {

	public ChunkPos(BlockPos pos){ super(pos); }
	
	public ChunkPos(int x, int z){ super(x, z); }
	
	@Override
	public int compareTo(net.minecraft.util.math.ChunkPos o){
		if(this.equals(o)){ return 0; }
        int result = ((this.x - o.x) * (this.x + o.x)) + ((this.z - o.z) * (this.z + o.z));
        if(result != 0){ return result; }
        return this.x < 0 ? (o.x < 0 ? o.z - this.z : -1) : (o.x < 0 ? 1 : this.z - o.z);
	}
	
}