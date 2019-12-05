package net.fexcraft.mod.states.data;

import java.util.UUID;

import net.fexcraft.mod.states.data.root.Initiator;
import net.fexcraft.mod.states.data.root.Ruleable;

public class Rule {
	
	public final String id, name;
	private Boolean value;
	private boolean votable;
	public Initiator reviser;
	public Initiator setter;
	
	public Rule(String id, Boolean def, boolean votable, Initiator rev, Initiator setter, String name){
		this.id = id; value = def; reviser = rev; this.setter = setter; this.votable = votable; this.name = name;
		if(!setter.isValidAsSetter()){
			if(setter == Initiator.CITIZEN_VOTE) setter = Initiator.COUNCIL_ANY;
			else if(setter == Initiator.COUNCIL_VOTE) setter = Initiator.INCHARGE;
		}
	}
	
	public Rule(String id, Boolean def, boolean votable, Initiator rev, Initiator setter){
		this(id, def, votable, rev, setter, id);
	}
	
	public Rule set(Boolean val){
		value = val; return this;
	}
	
	public Boolean get(){
		return value;
	}
	
	public String save(){
		String string = reviser.name() + "," + setter.name();
		if(value != null) string += "," + value; return string;
	}
	
	public Rule load(String string){
		String[] arr = string.split(",");
		reviser = Initiator.valueOf(arr[0]);
		setter = Initiator.valueOf(arr[1]);
		if(arr.length > 2) value = Boolean.parseBoolean(arr[2]);
		return this;
	}
	
	public Rule copy(){
		return new Rule(id, value, votable, reviser, setter, name);
	}

	/** Call to see if this player can SET/APPLY this rule. */
	public boolean isAuthorized(Ruleable holder, UUID uuid){
		switch(setter){
			case CITIZEN_ANY:{
				if(holder instanceof Municipality){
					return ((Municipality)holder).getCitizen().contains(uuid);
				} else return false;//it shouldn't get this far
			}
			case COUNCIL_ANY: return holder.getCouncil().contains(uuid);
			case INCHARGE: return holder.isHead(uuid);
			case HIGHERINCHARGE:
				return holder.hasHigherInstance() ? holder.getHigherInstance().isHead(uuid) : holder.isHead(uuid);
			default: return false;
		}
	}
	
	/** Call to see if this player can REVISE/MODIFY this rule. */
	public Result canRevise(Ruleable holder, UUID uuid){
		switch(reviser){
			case CITIZEN_ANY:{
				if(holder instanceof Municipality){
					return Result.bool(((Municipality)holder).getCitizen().contains(uuid));
				} else return Result.FALSE;
			}
			case CITIZEN_VOTE:{
				if(holder instanceof Municipality){
					if(((Municipality)holder).getCitizen().contains(uuid)) return Result.VOTE;
					return Result.FALSE;
				} else return Result.FALSE;
			}
			case COUNCIL_ANY: return Result.bool(holder.getCouncil().contains(uuid));
			case COUNCIL_VOTE:{
				if(holder.getCouncil().size() == 1) return Result.bool(holder.getCouncil().get(0).equals(uuid));
				if(holder.getCouncil().contains(uuid)) return Result.VOTE;
				return Result.FALSE;
			}
			case INCHARGE: return Result.bool(holder.isHead(uuid));
			case HIGHERINCHARGE:
				return Result.bool(holder.hasHigherInstance() ? holder.getHigherInstance().isHead(uuid) : holder.isHead(uuid));
			default: return Result.FALSE;
		}
	}
	
	public static enum Result {
		
		TRUE, VOTE, FALSE;
		
		public static Result bool(boolean bool){
			return bool ? TRUE : FALSE;
		}
		
		/*public boolean bool(){
			return this == TRUE;
		}*/

		public boolean isFalse(){
			return this == FALSE;
		}

		public boolean isTrue(){
			return this == TRUE;
		}

		public boolean isVote(){
			return this == VOTE;
		}
		
	}

}
