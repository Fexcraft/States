package net.fexcraft.mod.states.events;

import net.fexcraft.mod.states.data.State;
import net.minecraftforge.fml.common.eventhandler.Event;

public class StateEvent extends Event {
	
	private State state;

	public StateEvent(State state){
		this.state = state;
	}
	
	public State getState(){
		return state;
	}
	
	/** Called when all internal State variables been loaded, include e.g. custom rules here. */
	public static class Load extends StateEvent {

		public Load(State state){
			super(state);
		}
		
	}
	
}