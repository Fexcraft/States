package net.fexcraft.mod.states.data.chunk;

import java.util.UUID;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.states.data.Saveable;

/**
 * For Chunks and Properties
 * 
 * @author Ferdnand Calo' (FEX___96)
 *
 */
public class ChunkOwner implements Saveable {
	
	public boolean unowned;
	public boolean player;
	public UUID owner;
	public int company;

	@Override
	public void save(JsonMap map){
		if(!unowned) map.add("owner", player ? owner.toString() : "com:" + company);
	}

	@Override
	public void load(JsonMap map){
		String val = map.getString("owner", null);
		if(!(unowned = val == null)){
			if(val.startsWith("com:")){
				player = false;
				company = Integer.parseInt(val.substring(4));
			}
			else{
				player = true;
				owner = UUID.fromString(val);
			}
		}
		
	}

}
