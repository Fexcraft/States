package net.fexcraft.mod.states.data;

import net.fexcraft.mod.states.data.root.ColorHolder;

public enum ChunkType implements ColorHolder {
	
	NORMAL("#003600"),
	PUBLIC("#703DFF"),
	PRIVATE("#0020D8"),
	COMPANY("#20d400"),//("#008E96"),
	DISTRICT("#007F7F"),
	MUNICIPAL("#C1A100"),
	STATEOWNED("#A01818"),
	STATEPUBLIC("#ed4a4a");
	
	private String name, color;
	
	ChunkType(String color){
		this.name = name().toLowerCase();
		this.color = color;
	}

	@Override
	public String getColor(){
		return color;
	}

	@Override
	public void setColor(String newcolor){
		//haha.. this an enum.
	}
	
	@Override
	public String toString(){
		return name;
	}

	public static ChunkType get(String uppercased){
		for(ChunkType type : values()){
			if(type.name().equals(uppercased)) return type;
		}
		return null;
	}

	public String interactPrefix(){
		switch(this){
			case COMPANY:
			case DISTRICT:
			case MUNICIPAL:
			case PRIVATE:
			case STATEOWNED:
				return "allow";
			case NORMAL:
			case PUBLIC:
			case STATEPUBLIC:
			default:
				return "only";
		}
	}

}
