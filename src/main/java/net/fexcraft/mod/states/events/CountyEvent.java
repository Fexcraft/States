package net.fexcraft.mod.states.events;

import net.fexcraft.mod.states.data.County;
import net.minecraftforge.fml.common.eventhandler.Event;

public class CountyEvent extends Event {
	
	private County county;

	public CountyEvent(County cou){
		this.county = cou;
	}
	
	public County getCounty(){
		return county;
	}
	
	/** Called when all internal County variables been loaded, include e.g. custom rules here. */
	public static class Load extends CountyEvent {

		public Load(County mun){
			super(mun);
		}
		
	}
	
}