package net.fexcraft.mod.states.api;

public enum DistrictType {

	WILDERNESS(new boolean[]{false, false, false, false, false}),
	AGRICULTURAL(new boolean[]{false, false, false, true, false}),
	MINERAL   (new boolean[]{false, false, true , false, true}),
	VILLAGE   (new boolean[]{true , false, false, true , false}),
	RESIDENTAL(new boolean[]{true , true , false, false, false}),
	COMMERCIAL(new boolean[]{false, true , false, true , false}),
	INDUSTRIAL(new boolean[]{false, false, true , false, false}),
	WASTELAND (new boolean[]{false, false, false, false, true }),
	MUNICIPIAL(new boolean[]{true, true, false, false, false});
	
	public boolean housing, commerce, industry, cultivation, exploit;
	
	private DistrictType(boolean... bools){
		housing = bools[0];
		commerce = bools[1];
		industry = bools[2];
		cultivation = bools[3];
		exploit = bools[4];
	}
	
	/** "can be used as" */
	public boolean hasAttribute(DistrictAttribute attr){
		switch(attr){
			case COMMERCE: return commerce;
			case CULTIVATION: return cultivation;
			case EXPLOITATION: return exploit;
			case HOUSING: return housing;
			case INDUSTRY: return industry;
			default: return false;
		}
	}
	
}
