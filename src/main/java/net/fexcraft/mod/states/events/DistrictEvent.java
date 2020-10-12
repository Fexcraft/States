package net.fexcraft.mod.states.events;

import net.fexcraft.mod.states.data.District;
import net.minecraftforge.fml.common.eventhandler.Event;

public class DistrictEvent extends Event {
	
	private District district;

	public DistrictEvent(District dis){
		this.district = dis;
	}
	
	public District getDistrict(){
		return district;
	}
	
	/** Called when all internal District variables been loaded, include e.g. custom rules here. */
	public static class Load extends DistrictEvent {

		public Load(District dis){
			super(dis);
		}
		
	}
	
}