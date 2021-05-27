package net.fexcraft.mod.states.data.root;

public enum Layers {
	
	DISTRICT,
	MUNICIPALITY,
	COUNTY,
	STATE,
	UNION,
	COMPANY,
	PLAYERDATA,
	CHUNK,
	PROPERTY;

	public boolean isChunk(){
		return this == CHUNK;
	}

	public boolean isDistrict(){
		return this == DISTRICT;
	}

	public boolean isMunicipality(){
		return this == MUNICIPALITY;
	}
	
	public boolean isCounty(){
		return this == COUNTY;
	}

	public boolean isState(){
		return this == STATE;
	}

	public boolean isPlayer(){
		return this == PLAYERDATA;
	}
	
}