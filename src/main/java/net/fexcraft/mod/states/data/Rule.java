package net.fexcraft.mod.states.data;

import java.util.UUID;

import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.states.data.root.Initiator;
import net.fexcraft.mod.states.data.root.Layer;
import net.fexcraft.mod.states.data.root.Populated;
import net.fexcraft.mod.states.data.sub.Manageable;

public class Rule {
	
	public final String id, name;
	private Boolean value;
	private boolean votable_set, external;
	public Initiator reviser;
	public Initiator setter;
	
	public Rule(String id, Boolean def, boolean votable, Initiator rev, Initiator setter, String name){
		this.id = id; value = def; reviser = rev; this.setter = setter; this.votable_set = votable; this.name = name;
		if(!setter.isValidAsSetter(votable)){
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
		return new Rule(id, value, votable_set, reviser, setter, name);
	}

	/** Call to see if this player can SET/APPLY this rule. */
	public Result isAuthorized(Layer layer, UUID uuid){
		if(layer instanceof Manageable == false) return Result.FALSE;
		else return isAuthorized((Manageable)layer, uuid);
	}

	/** Call to see if this player can SET/APPLY this rule. */
	public Result isAuthorized(Manageable manage, UUID uuid){
		switch(setter){
			case NONE: return Result.FALSE;
			case CITIZEN_ANY:{
				if(manage.getLayer() instanceof Populated){
					return Result.bool(((Populated)manage.getLayer()).isCitizen(uuid));
				}
				else return Result.FALSE;//it shouldn't get this far
			}
			case CITIZEN_VOTE:{
				if(votable_set){
					if(manage.getLayer() instanceof Populated){
						if(((Populated)manage.getLayer()).isCitizen(uuid)) return Result.VOTE;
						return Result.FALSE;
					}
					else return Result.FALSE;//it shouldn't get this far
				}
				else{
					Print.log("INVALID STATE OF RULE " + id + " FROM " + manage);
					Print.log("SETTER IS VOTE TYPE BUT RULE IS NOT A VOTE TYPE");
					return Result.FALSE;
				}
			}
			case COUNCIL_ANY: return Result.bool(manage.isInCouncil(uuid));
			case COUNCIL_VOTE:{
				if(votable_set){
					if(manage.getCouncil().size() == 1) return Result.bool(manage.getCouncil().get(0).equals(uuid));
					if(manage.isInCouncil(uuid)) return Result.VOTE;
					return Result.FALSE;
				}
				else{
					Print.log("INVALID STATE OF RULE " + id + " FROM " + manage);
					Print.log("SETTER IS VOTE TYPE BUT RULE IS NOT A VOTE TYPE");
					return Result.FALSE;
				}
			}
			case INCHARGE: return Result.bool(manage.isHead(uuid));
			case HIGHERINCHARGE:
				return Result.bool(manage.hasHigherInstance() ? manage.getHigherInstance().isHead(uuid) : manage.isHead(uuid));
			default: return Result.FALSE;
		}
	}
	
	/** Call to see if this player can REVISE/MODIFY this rule. */
	public Result canRevise(Layer layer, UUID uuid){
		if(layer instanceof Manageable == false) return Result.FALSE;
		else return canRevise((Manageable)layer, uuid);
	}
	
	/** Call to see if this player can REVISE/MODIFY this rule. */
	public Result canRevise(Manageable manage, UUID uuid){
		switch(reviser){
			case NONE: return Result.FALSE;
			case CITIZEN_ANY:{
				if(manage.getLayer() instanceof Populated){
					return Result.bool(((Populated)manage.getLayer()).isCitizen(uuid));
				}
				else return Result.FALSE;
			}
			case CITIZEN_VOTE:{
				if(manage.getLayer() instanceof Populated){
					if(((Populated)manage.getLayer()).isCitizen(uuid)) return Result.VOTE;
					return Result.FALSE;
				}
				else return Result.FALSE;
			}
			case COUNCIL_ANY: return Result.bool(manage.getCouncil().contains(uuid));
			case COUNCIL_VOTE:{
				if(manage.getCouncil().size() == 1) return Result.bool(manage.getCouncil().get(0).equals(uuid));
				if(manage.isInCouncil(uuid)) return Result.VOTE;
				return Result.FALSE;
			}
			case INCHARGE: return Result.bool(manage.isHead(uuid));
			case HIGHERINCHARGE:
				return Result.bool(manage.hasHigherInstance() ? manage.getHigherInstance().isHead(uuid) : manage.isHead(uuid));
			default: return Result.FALSE;
		}
	}
	
	public static enum Result {
		
		TRUE, VOTE, FALSE;
		
		public static final Result bool(boolean bool){
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
	
	public boolean isVotable(){
		return votable_set;
	}

	public Rule setExternal(){
		external = true;
		return this;
	}
	
	public boolean isExternal(){
		return external;
	}

}
