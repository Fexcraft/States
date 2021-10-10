package net.fexcraft.mod.states.data.chunk;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.states.data.Createable;
import net.fexcraft.mod.states.data.Layer;
import net.fexcraft.mod.states.data.Layers;
import net.fexcraft.mod.states.data.Saveable;
import net.fexcraft.mod.states.data.Sellable;
import net.fexcraft.mod.states.data.Taxable;
import net.minecraft.world.World;

public class Chunk_ implements Saveable, Layer {
	
	public ChunkKey key;
	public Createable created = new Createable();
	public ChunkType type = ChunkType.NORMAL;
	public Sellable sell = new Sellable(this);
	public ChunkLink link = null;
	public AccessList access = new AccessList();
	public ChunkOwner owner = new ChunkOwner();
	public Taxable tax = new Taxable(this);

	public Chunk_(World world, int x, int z){
		key = new ChunkKey(x, z);
	}

	@Override
	public void save(JsonMap map){
		map.add("id", key.toString());
		created.save(map);
		owner.save(map);
		map.add("type", type.l1());
		sell.save(map);
		if(link != null) link.save(map);
		access.save(map);
		tax.save(map);
	}

	@Override
	public void load(JsonMap map){
		created.load(map);
		owner.load(map);
		type = ChunkType.l1(map.getString("type", "N"));
		sell.load(map);
		if(map.has(true, "link", "linked")){
			link = new ChunkLink(this);
			link.load(map);
		}
		access.load(map);
		tax.load(map);
	}
	
	@Override
	public String saveId(){
		return key.toString();
	}
	
	@Override
	public String saveTable(){
		return "chunks";
	}

	@Override
	public Layers getLayer(){
		return Layers.CHUNK;
	}

	@Override
	public Layers getParentLayer(){
		return Layers.DISTRICT;
	}

}
