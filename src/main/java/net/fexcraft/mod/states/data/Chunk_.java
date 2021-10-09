package net.fexcraft.mod.states.data;

import net.fexcraft.app.json.JsonMap;
import net.minecraft.world.World;

public class Chunk_ implements Saveable {
	
	public ChunkKey key;

	public Chunk_(World world, int x, int z){
		key = new ChunkKey(x, z);
	}

	@Override
	public void save(JsonMap map){
		map.add("id", key.toString());
	}

	@Override
	public void load(JsonMap map){
		//
	}
	
	@Override
	public String saveId(){
		return key.toString();
	}
	
	@Override
	public String saveTable(){
		return "chunks";
	}
	
	public static class ChunkKey implements Comparable<ChunkKey> {
		
		public int x, z;
		
		public ChunkKey(int x, int z){
			this.x = x;
			this.z = z;
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

}
