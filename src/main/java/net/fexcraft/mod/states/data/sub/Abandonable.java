package net.fexcraft.mod.states.data.sub;

import java.util.UUID;
import java.util.function.Consumer;

import com.google.gson.JsonObject;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.states.data.capabilities.PlayerCapability;
import net.fexcraft.mod.states.data.root.ChildLayer;
import net.fexcraft.mod.states.data.root.Loadable;

public class Abandonable implements Loadable {
	
	private long at;
	private UUID by;
	private boolean abandoned;
	private Consumer<ChildLayer> abandon;
	private Consumer<PlayerCapability> claim;
	private ChildLayer child;
	
	public Abandonable(ChildLayer layer, Consumer<ChildLayer> aban, Consumer<PlayerCapability> clai){
		abandon = aban;
		claim = clai;
		child = layer;
	}

	@Override
	public void load(JsonObject obj){
		abandoned = obj.has("abandoned") && obj.get("abandoned").getAsBoolean();
		by = obj.has("abandoned_by") ? UUID.fromString(obj.get("abandoned_by").getAsString()) : null;
		at = obj.has("abandoned_at") ? obj.get("abandoned_at").getAsLong() : 0;
	}

	@Override
	public void save(JsonObject obj){
		obj.addProperty("abandoned", abandoned);
		if(by != null) obj.addProperty("abandoned_by", by.toString());
		if(at > 0) obj.addProperty("abandoned_at", at);
	}

	public boolean isAbandoned(){
		return abandoned;
	}
	
	/** Unclaim command. */
	public void abandon(UUID by){
		this.by = by;
		at = Time.getDate();
		abandoned = true;
		abandon.accept(child);
	}
	
	public long getSince(){
		return at;
	}
	
	public UUID getBy(){
		return by;
	}
	
	/** Claim command. */
	public void claim(PlayerCapability by){
		by = null;
		at = Time.getDate();
		abandoned = false;
		claim.accept(by);
	}
	
}
