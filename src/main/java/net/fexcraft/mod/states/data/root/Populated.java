package net.fexcraft.mod.states.data.root;

import java.util.List;
import java.util.UUID;

public interface Populated {
	
	/** Municipalities will return all their residents, Counties only their direct residents (not including Municipalities in them) and States will return nothing. */
	public List<UUID> getResidents();
	
	/** Will load child layers into memory if necessary to ask for their residents. */
	public List<UUID> getAllResidents();
	
	public default int getResidentCount(){
		return getResidents().size();
	}
	
	public default int getAllResidentCount(){
		return getAllResidents().size();
	}
	
	public boolean isCitizen(UUID uuid);

}
