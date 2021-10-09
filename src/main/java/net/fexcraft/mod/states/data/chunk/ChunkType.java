package net.fexcraft.mod.states.data.chunk;

public enum ChunkType {
	
	PRIVATE,
	NORMAL,
	RESTRICTED,
	PUBLIC;

	public String l1(){
		switch(this){
			case PRIVATE: return "V";
			case NORMAL: return "N";
			case RESTRICTED: return "R";
			case PUBLIC: return "P";
		}
		return "N";
	}

	public static ChunkType l1(String string){
		switch(string){
			case "V": return PRIVATE;
			case "N": return NORMAL;
			case "R": return RESTRICTED;
			case "P": return PUBLIC;
		}
		return NORMAL;
	}

}
