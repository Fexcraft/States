package net.fexcraft.mod.states.api;

public enum ChunkType implements ColorHolder {
	
	NORMAL("#003600"),
	PUBLIC("#703DFF"),
	PRIVATE("#0020D8"),
	COMPANY("#008E96"),
	MUNICIPIAL("#C1A100"),
	STATEOWNED("#A01818");
	
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

}
