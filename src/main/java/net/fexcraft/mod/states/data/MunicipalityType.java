package net.fexcraft.mod.states.data;

import java.util.TreeSet;

import com.google.gson.JsonObject;

import net.fexcraft.lib.common.json.JsonUtil;
import net.fexcraft.mod.states.util.StConfig;

public class MunicipalityType implements Comparable<MunicipalityType>{
	
	private static final TreeSet<MunicipalityType> TYPES = new TreeSet<MunicipalityType>();
	public static final String[] DEFAULTS = new String[]{
		"{'citizen': 0, 'districts': 1, 'title':'Hamlet'}",
		"{'citizen': 8, 'districts': 2, 'title':'Village'}",
		"{'citizen': 16, 'districts': 3, 'title':'Small Town'}",
		"{'citizen': 24, 'districts': 4, 'title':'Town'}",
		"{'citizen': 32, 'districts': 6, 'title':'Large Town'}",
		"{'citizen': 40, 'districts': 8, 'title':'City'}",
		"{'citizen': 60, 'districts': 10, 'title':'Large City'}",
		"{'citizen': 120, 'districts': 12, 'title':'Metropolis'}",
		"{'citizen': 160, 'districts': 16, 'title':'<name pending>'}",
		
	};
	private int requred_citizen, district_limit;
	private boolean obtainable;
	private String title;
	
	public MunicipalityType(String title, int x, int z, boolean normal){
		this.title = title;
		this.requred_citizen = x;
		this.district_limit = z;
		this.obtainable = normal;
		TYPES.add(this);
	}
	
	public MunicipalityType(JsonObject obj){
		this(JsonUtil.getIfExists(obj, "title", "Missing Title"),
				JsonUtil.getIfExists(obj, "citizen", 0).intValue(),
					JsonUtil.getIfExists(obj, "districts", 0).intValue(),
						JsonUtil.getIfExists(obj, "available", true));
	}
	
	public String toDetailedString(){
		return "&0[&7" + requred_citizen + " | " + district_limit + "&0]&2 " + title;
	}

	@Override
	public int compareTo(MunicipalityType o){
		return -(o.requred_citizen > this.requred_citizen ? 1 : o.requred_citizen < this.requred_citizen ? -1 : 0);
	}
	
	public static final void clearEntries(){
		TYPES.clear();
		new MunicipalityType("Invalid", 0, 0, false);
		new MunicipalityType("(Abandoned)", 0, 0, false);
	}
	
	public static final TreeSet<MunicipalityType> values(){
		return TYPES;
	}
	
	public static final MunicipalityType getType(Municipality mun){
		MunicipalityType type = null;
		for(MunicipalityType elm : TYPES){
			type = elm.obtainable ? elm : type;
			if(elm.requred_citizen > mun.getCitizen().size()){
				break;
			}
		}
		//Print.debug(TYPES);
		//Static.stop();
		return type;
	}
	
	public final String getTitle(){
		return title;
	}
	
	public final int getRequiredCitizen(){
		return requred_citizen;
	}
	
	public final int getDistrictLimit(){
		return district_limit;
	}
	
	public final boolean obtainableType(){
		return obtainable;
	}
	
	@Override
	public String toString(){
		return "[citizen:" + requred_citizen + ",districts:" + district_limit + ", title:\"" + title + "\",obtainable:" + obtainable + "]";
	}
	
	public static final int getChunkLimitFor(Municipality mun){
		return StConfig.CHUNK_PER_CITIZEN * mun.getCitizen().size();
	}
	
}
