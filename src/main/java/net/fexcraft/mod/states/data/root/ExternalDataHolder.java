package net.fexcraft.mod.states.data.root;

import java.util.Map;

public interface ExternalDataHolder {
	
	public <T extends ExternalData> T getExternalData(String id); 
	
	public ExternalData setExternalData(String id, ExternalData obj);
	
	public Map<String, ExternalData> getExternalObjects();

}
