package net.fexcraft.mod.states.data;

import net.fexcraft.app.json.JsonMap;

public class District implements Saveable, Layer {
	
	public final int id;
	public Createable created = new Createable();
	public Sellable sell = new Sellable(this);
	public IconHolder icon = new IconHolder();
	public ColorData color = new ColorData();
	public NeighborData neighbors = new NeighborData();
	public MailData mail = new MailData();
	//TODO manageable
	//TODO ruleholder
	public long chunks;
	
	public District(int id){
		this.id = id;
	}

	@Override
	public void save(JsonMap map){
		map.add("id", id);
		created.save(map);
		sell.save(map);
		icon.save(map);
		color.save(map);
		neighbors.save(map);
		mail.save(map);
		//
		map.add("chunks", chunks);
	}

	@Override
	public void load(JsonMap map){
		created.load(map);
		sell.load(map);
		icon.load(map);
		color.load(map);
		neighbors.load(map);
		mail.load(map);
		//
		chunks = map.getLong("chunks", 0);
	}
	
	@Override
	public String saveId(){
		return id + "";
	}
	
	@Override
	public String saveTable(){
		return "districts";
	}

	@Override
	public Layers getLayer(){
		return Layers.DISTRICT;
	}

	@Override
	public Layers getParentLayer(){
		return Layers.MUNICIPALITY;
	}

}
