package net.fexcraft.mod.states.data.root;

import net.fexcraft.mod.states.data.County;
import net.fexcraft.mod.states.data.District;
import net.fexcraft.mod.states.data.Municipality;

/**
 * "Municipality-County"
 * 
 * @author Ferdinand Calo' (FEX___96)
 *
 */
public class MunCt {
	
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

}
