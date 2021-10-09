package net.fexcraft.mod.states.data.chunk;

public class ChunkKey implements Comparable<ChunkKey> {
	
	public int x, z;
	
	public ChunkKey(int x, int z){
		this.x = x;
		this.z = z;
	}
	
	public ChunkKey(String string){
		String[] split = string.split("_");
		x = Integer.parseInt(split[0]);
		z = Integer.parseInt(split[0]);
	}

	@Override
	public String toString(){
		return x + "_" + z;
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof int[]){
			int[] a = (int[]) o;
			return a.length > 1 && x == a[0] && z == a[1];
		}
		else if(o instanceof ChunkKey){
			ChunkKey c = (ChunkKey)o;
			return x == c.x && z == c.z;
		}
		else return false;
	}

	@Override
	public int compareTo(ChunkKey c){
		if(c.x < x) return -1;
		if(c.x > x){
			if(c.z < z) return -1;
			if(c.z > z) return 1;
		}
		return 0;
	}
	
}
