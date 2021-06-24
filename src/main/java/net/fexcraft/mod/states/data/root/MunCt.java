package net.fexcraft.mod.states.data.root;

import com.google.gson.JsonObject;

import net.fexcraft.lib.common.json.JsonUtil;
import net.fexcraft.mod.states.data.County;
import net.fexcraft.mod.states.data.District;
import net.fexcraft.mod.states.data.Municipality;
import net.fexcraft.mod.states.util.StateUtil;

/**
 * "Municipality-County"
 * 
 * @author Ferdinand Calo' (FEX___96)
 *
 */
public class MunCt implements Loadable {
	
	public District district;
	public Municipality municipality;
	public County county;
	public boolean mun;
	
	public MunCt(District dis){
		district = dis;
	}

	public String trid(){
		return mun ? "municipality" : "county";
	}

	public void set(Municipality mun){
		municipality = mun;
		this.mun = true;
		county = null;
	}

	public void set(County county){
		this.county = county;
		this.mun = false;
		municipality = null;
	}

	public County getCounty(){
		return mun ? municipality.getCounty() : county;
	}

	public Layer getParent(){
		return mun ? municipality : county;
	}

	public AccountHolder getAccountHolder(){
		return mun ? municipality : county;
	}
	
	@Override
	public boolean equals(Object other){
		if(other instanceof MunCt == false) return false;
		MunCt munct = (MunCt)other;
		return mun == munct.mun && (mun ? municipality.getId() == munct.municipality.getId() : county.getId() == munct.county.getId());
	}

	public void load(JsonObject obj){
		municipality = obj.has("municipality") ? StateUtil.getMunicipality(JsonUtil.getIfExists(obj, "municipality", -1).intValue()) : null;
		county = obj.has("county") ?  StateUtil.getCounty(JsonUtil.getIfExists(obj, "county", -1).intValue()) : null;
		if(county == null && municipality == null) municipality = StateUtil.getMunicipality(-1);
		if(county != null) municipality = null;
		if(municipality != null) mun = true;
	}

	@Override
	public void save(JsonObject obj){
		if(mun) obj.addProperty("municipality", municipality.getId());
		else obj.addProperty("county", county.getId());
	}

}
