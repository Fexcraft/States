package net.fexcraft.mod.states.data.sub;

import java.util.UUID;

import com.google.gson.JsonObject;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.data.root.Loadable;

public class Createable implements Loadable {
	
	private long created, changed;
	private UUID creator;

	@Override
	public void load(JsonObject obj){
		created = obj.has("created") ? obj.get("created").getAsLong() : Time.getDate();
		creator = UUID.fromString(obj.has("creator") ? obj.get("creator").getAsString() : States.CONSOLE_UUID);
		changed = obj.has("changed") ? obj.get("changed").getAsLong() : Time.getDate();
	}

	@Override
	public void save(JsonObject obj){
		obj.addProperty("created", created);
		obj.addProperty("creator", creator.toString());
		obj.addProperty("changed", changed);
	}
	
	public long getCreated(){
		return created;
	}
	
	public UUID getCreator(){
		return creator;
	}
	
	public long getChanged(){
		return created;
	}
	
	public void update(Long time){
		changed = time == null ? Time.getDate() : time;
	}

	public void update(){
		update(null);
	}

	/** Only to be used with CHUNKS. Updates the "changed" value too. */
	public void setClaimer(UUID uuid){
		creator = uuid;
		update(null);
	}

	public UUID getClaimer(){
		return creator;
	}

	/** Only to be used on creation of NEW Layer instances, e.g. via commands. */
	public void create(UUID uuid){
		creator = uuid;
		created = changed = Time.getDate();
	}

}
