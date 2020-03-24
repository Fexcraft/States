package net.fexcraft.mod.states.data.root;

import java.util.UUID;

public interface Abandonable {

	public boolean isAbandoned();
	
	/** Unclaim command. */
	public void setAbandoned(UUID by);
	
	public long getAbandonedSince();
	
	public UUID getAbandonedBy();
	
	/** Claim command. */
	public void getAbandoned(UUID by);
	
}
