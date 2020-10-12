package net.fexcraft.mod.states.events;

import net.fexcraft.mod.states.data.Municipality;
import net.minecraftforge.fml.common.eventhandler.Event;

public class MunicipalityEvent extends Event {
	
	private Municipality muninipality;

	public MunicipalityEvent(Municipality mun){
		this.muninipality = mun;
	}
	
	public Municipality getMunicipality(){
		return muninipality;
	}
	
	/** Called when all internal Municipality variables been loaded, include e.g. custom rules here. */
	public static class Load extends MunicipalityEvent {

		public Load(Municipality mun){
			super(mun);
		}
		
	}
	
}