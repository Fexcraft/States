package net.fexcraft.mod.states.data;

import java.util.UUID;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.states.util.ResManager;

public class Createable implements Saveable {
	
	private long created, updated;
	private UUID creator;

	@Override
	public void load(JsonMap map){
		created = map.getLongTime("created");
		creator = UUID.fromString(map.getString("creator", ResManager.CONSOLE_UUID));
		updated = map.getLongTime("updated");
	}

	@Override
	public void save(JsonMap map){
		map.add("created", created);
		map.add("creator", creator.toString());
		map.add("updated", updated);
	}
	
	public long getCreated(){
		return created;
	}
	
	public UUID getCreator(){
		return creator;
	}
	
	public long getUpdated(){
		return created;
	}
	
	public void update(Long time){
		updated = time == null ? Time.getDate() : time;
	}

	public void update(){
		update(null);
	}

	/** Only to be used with CHUNKS. Updates the "updated" value too. */
	public void setClaimer(UUID uuid){
		creator = uuid;
		update();
	}

	public UUID getClaimer(){
		return creator;
	}

	/** Only to be used on creation of NEW Layer instances, e.g. via commands. */
	public void create(UUID uuid){
		creator = uuid;
		created = updated = Time.getDate();
	}

}
