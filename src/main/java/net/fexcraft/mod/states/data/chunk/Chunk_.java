package net.fexcraft.mod.states.data.chunk;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.states.data.Layer;
import net.fexcraft.mod.states.data.Layers;
import net.fexcraft.mod.states.data.Saveable;
import net.fexcraft.mod.states.data.Sellable;
import net.fexcraft.mod.states.util.ResManager;
import net.minecraft.world.World;

public class Chunk_ implements Saveable, Layer {
	
	public ChunkKey key;
	public long updated, claimed;
	public String claimer, owner;
	public ChunkType type = ChunkType.NORMAL;
	public Sellable sell = new Sellable(this);
	public ChunkLink link = null;

	public Chunk_(World world, int x, int z){
		key = new ChunkKey(x, z);
	}

	@Override
	public void save(JsonMap map){
		map.add("id", key.toString());
		map.add("updated", updated);
		map.add("claimed", claimed);
		map.add("claimer", claimer);
		map.add("owner", owner);
		map.add("type", type.l1());
		sell.save(map);
		if(link != null) link.save(map);
	}

	@Override
	public void load(JsonMap map){
		updated = map.getLongTime("updated");
		claimed = map.getLongTime("claimed");
		claimer = map.getString("claimer", ResManager.CONSOLE_UUID);
		owner = map.getString(owner, null);
		type = ChunkType.l1(map.getString("type", "N"));
		sell.load(map);
		if(map.has(true, "link", "linked")){
			link = new ChunkLink(this);
			link.load(map);
		}
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

}
