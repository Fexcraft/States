package net.fexcraft.mod.states.util;

import java.util.TreeMap;

import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.states.data.Chunk;
import net.fexcraft.mod.states.data.District;
import net.fexcraft.mod.states.data.Municipality;
import net.fexcraft.mod.states.data.State;
import net.fexcraft.mod.states.data.capabilities.PlayerCapability;
import net.fexcraft.mod.states.data.capabilities.StatesCapabilities;
import net.minecraft.entity.player.EntityPlayer;

public class StatesPermissions {
	
	public static final TreeMap<String, Permission> PERMISSIONS = new TreeMap<String, Permission>();
	
	public static final boolean hasPermission(EntityPlayer entity, String permission, Object obj){
		if(obj == null){ obj = StateUtil.getChunk(entity); }
		PlayerCapability player = entity.getCapability(StatesCapabilities.PLAYER, null);
		Permission perm = PERMISSIONS.get(permission);
		if(perm == null){
			Print.log("Permission with ID '" + permission + "' doesn't exists.");
			return false;
		}
		boolean group = false, admin = false;
		if(perm.group != null && perm.group != PermissionGroup.SKIP){
			switch(perm.group){
				case ADMIN: group = StateUtil.isAdmin(entity) || isOperator(entity); admin = true; break;
				case DISTRICT: group = player.isDistrictManagerOf(district(obj)) || player.isMayorOf(district(obj).getMunicipality()); break;
				case MUNICIPALITY: group = player.isMayorOf(municipality(obj)) || player.isStateLeaderOf(municipality(obj).getState()); break;
				case NONE: group = true; break;
				case STATE: group = player.isStateLeaderOf(state(obj)); break;
				default: break;
			}
		}
		Print.debug("Perm Check Start;");
		Print.debug(entity.getName() + " | " + permission + " | GR: " + group);
		Print.debug(perm.toString());
		if(!group){
			boolean exit = false;
			for(PermissionLevel level : perm.levels){
				if(exit){ break; }
				switch(level){
					case ADMIN: exit = StateUtil.isAdmin(entity); break;
					case DIS_MANAGER: exit = player.isDistrictManagerOf(district(obj)); break;
					case MUNICIPALITY_COUNCIL: exit = municipality(obj).getCouncil().contains(player.getUUID()); break;
					case MUNICIPALITY_MAYOR: exit = player.isMayorOf(municipality(obj)); break;
					case MUNICIPALITY_MEMBER: exit = player.getMunicipality().getId() == municipality(obj).getId(); break;
					case NONE: exit = true; break;
					case OPERATOR: exit = isOperator(entity); break;
					case SKIP: break;
					case STATE_COUNCIL: exit = state(obj).getCouncil().contains(player.getUUID()); break;
					case STATE_LEADER: exit = player.isStateLeaderOf(state(obj)); break;
					case STATE_MEMBER: exit = player.getMunicipality().getState().getId() == state(obj).getId(); break;
					case OWNER: exit = chunk(obj).getOwner().equals(player.getUUID().toString()); break;
					default: break;
				}
			}
			group = exit;
		}
		Print.debug(entity.getName() + " | " + permission + " | PS: " + group);
		Print.debug("Perm Check End;");
		if(admin){ Print.chat(entity, "&7&oAdmin mode."); }
		return group;
	}
	
	private static Chunk chunk(Object obj){
		return (Chunk)obj;
	}
	
	private static District district(Object obj){
		return obj instanceof Chunk ? ((Chunk)obj).getDistrict() : (District)obj;
	}
	
	private static Municipality municipality(Object obj){
		return obj instanceof Chunk ? ((Chunk)obj).getMunicipality() : obj instanceof District ? ((District)obj).getMunicipality() :(Municipality)obj;
	}

	private static State state(Object obj){
		return obj instanceof Chunk ? ((Chunk)obj).getState() : obj instanceof District ? ((District)obj).getState() : obj instanceof Municipality ? ((Municipality)obj).getState() : (State)obj;
	}

	private static boolean isOperator(EntityPlayer entity){
		return Static.getServer().getPlayerList().getOppedPlayers().getPermissionLevel(entity.getGameProfile()) > 0;
	}

	public static class Permission {
		
		private PermissionGroup group;
		private PermissionLevel[] levels;
		
		public Permission(PermissionGroup group, PermissionLevel... levels){
			this.group = group;
			this.levels = levels;
		}
		
		@Override
		public String toString(){
			String str = "[ " + (group == null ? "null" : group.name()) + " | ";
			for(int i = 0; i < levels.length; i++){
				str += levels[i].name() + (i + 1 >= levels.length ? "" : ", ");
			}
			return str + " ]";
		}
		
	}
	
	private static enum PermissionLevel {
		NONE, SKIP, ADMIN, OPERATOR, OWNER, DIS_MANAGER, MUNICIPALITY_MEMBER, MUNICIPALITY_COUNCIL, MUNICIPALITY_MAYOR, STATE_MEMBER, STATE_COUNCIL, STATE_LEADER;
	}
	
	private static enum PermissionGroup {
		NONE, SKIP, ADMIN, DISTRICT, MUNICIPALITY, STATE;
	}
	
	public static final void init(){
		PERMISSIONS.put("admin", new Permission(PermissionGroup.ADMIN, PermissionLevel.ADMIN));
		PERMISSIONS.put("chunk.claim", new Permission(PermissionGroup.DISTRICT, PermissionLevel.ADMIN, PermissionLevel.STATE_LEADER));
		PERMISSIONS.put("district.set.type", new Permission(PermissionGroup.DISTRICT, PermissionLevel.ADMIN));
		PERMISSIONS.put("district.set.name", new Permission(PermissionGroup.DISTRICT, PermissionLevel.ADMIN, PermissionLevel.MUNICIPALITY_COUNCIL));
		PERMISSIONS.put("district.set.price", new Permission(PermissionGroup.SKIP, PermissionLevel.ADMIN, PermissionLevel.STATE_LEADER, PermissionLevel.STATE_COUNCIL, PermissionLevel.MUNICIPALITY_MAYOR));
		PERMISSIONS.put("district.set.manager", new Permission(PermissionGroup.SKIP, PermissionLevel.ADMIN, PermissionLevel.STATE_LEADER, PermissionLevel.STATE_COUNCIL, PermissionLevel.MUNICIPALITY_COUNCIL,  PermissionLevel.MUNICIPALITY_MAYOR));
		PERMISSIONS.put("district.set.color", new Permission(PermissionGroup.DISTRICT, PermissionLevel.MUNICIPALITY_COUNCIL, PermissionLevel.ADMIN));
		PERMISSIONS.put("district.set.cfs", new Permission(PermissionGroup.DISTRICT, PermissionLevel.MUNICIPALITY_COUNCIL, PermissionLevel.ADMIN));
		//PERMISSIONS.put("municipality.set.name", new Permission(PermissionGroup.SKIP, PermissionLevel.MUNICIPALITY_MAYOR, PermissionLevel.ADMIN));
		//PERMISSIONS.put("municipality.set.price", new Permission(PermissionGroup.SKIP, PermissionLevel.MUNICIPALITY_MAYOR, PermissionLevel.STATE_COUNCIL, PermissionLevel.STATE_LEADER, PermissionLevel.ADMIN));
		PERMISSIONS.put("municipality.set.color", new Permission(PermissionGroup.SKIP, PermissionLevel.MUNICIPALITY_MAYOR, PermissionLevel.MUNICIPALITY_COUNCIL, PermissionLevel.ADMIN));
		PERMISSIONS.put("municipality.set.open", new Permission(PermissionGroup.SKIP, PermissionLevel.MUNICIPALITY_MAYOR, PermissionLevel.ADMIN));
		PERMISSIONS.put("municipality.blacklist.edit", new Permission(PermissionGroup.SKIP, PermissionLevel.MUNICIPALITY_COUNCIL));
		PERMISSIONS.put("municipality.kick", new Permission(PermissionGroup.SKIP, PermissionLevel.MUNICIPALITY_COUNCIL));
		PERMISSIONS.put("municipality.invite", new Permission(PermissionGroup.SKIP, PermissionLevel.MUNICIPALITY_COUNCIL));
		PERMISSIONS.put("state.set.name", new Permission(PermissionGroup.STATE, PermissionLevel.ADMIN));
		PERMISSIONS.put("state.set.price", new Permission(PermissionGroup.STATE, PermissionLevel.ADMIN));
		PERMISSIONS.put("state.set.color", new Permission(PermissionGroup.STATE, PermissionLevel.STATE_COUNCIL, PermissionLevel.ADMIN));
		PERMISSIONS.put("municipality.buy", new Permission(PermissionGroup.STATE, PermissionLevel.STATE_COUNCIL));
		PERMISSIONS.put("state.set.capital", new Permission(PermissionGroup.STATE, PermissionLevel.NONE));
		PERMISSIONS.put("municipality.council.kick", new Permission(PermissionGroup.MUNICIPALITY, PermissionLevel.MUNICIPALITY_COUNCIL, PermissionLevel.ADMIN));
		PERMISSIONS.put("municipality.council.invite", new Permission(PermissionGroup.MUNICIPALITY, PermissionLevel.MUNICIPALITY_COUNCIL, PermissionLevel.ADMIN));
		PERMISSIONS.put("municipality.council.vote", new Permission(PermissionGroup.SKIP, PermissionLevel.MUNICIPALITY_COUNCIL, PermissionLevel.MUNICIPALITY_MAYOR));
		PERMISSIONS.put("municipality.set.icon", new Permission(PermissionGroup.MUNICIPALITY, PermissionLevel.MUNICIPALITY_COUNCIL, PermissionLevel.ADMIN));
		PERMISSIONS.put("district.set.icon", new Permission(PermissionGroup.DISTRICT, PermissionLevel.MUNICIPALITY_COUNCIL, PermissionLevel.ADMIN));
		PERMISSIONS.put("state.set.icon", new Permission(PermissionGroup.STATE, PermissionLevel.STATE_COUNCIL, PermissionLevel.ADMIN));
		PERMISSIONS.put("state.create", new Permission(PermissionGroup.SKIP, PermissionLevel.MUNICIPALITY_MAYOR, PermissionLevel.ADMIN));
		PERMISSIONS.put("chunk.update", new Permission(PermissionGroup.SKIP, PermissionLevel.OWNER, PermissionLevel.DIS_MANAGER, PermissionLevel.MUNICIPALITY_MAYOR, PermissionLevel.STATE_LEADER, PermissionLevel.ADMIN));
		PERMISSIONS.put("shop.create.municipality", new Permission(PermissionGroup.MUNICIPALITY, PermissionLevel.MUNICIPALITY_COUNCIL, PermissionLevel.ADMIN, PermissionLevel.OPERATOR));
		PERMISSIONS.put("shop.create.state", new Permission(PermissionGroup.STATE, PermissionLevel.STATE_COUNCIL, PermissionLevel.ADMIN, PermissionLevel.OPERATOR));
		PERMISSIONS.put("shop.create.server", new Permission(PermissionGroup.ADMIN, PermissionLevel.SKIP));
		PERMISSIONS.put("district.create", new Permission(PermissionGroup.ADMIN, PermissionLevel.MUNICIPALITY_MAYOR));
		PERMISSIONS.put("nickname.change.self", new Permission(PermissionGroup.SKIP, PermissionLevel.NONE));
		PERMISSIONS.put("nickname.change.others", new Permission(PermissionGroup.ADMIN, PermissionLevel.SKIP));
		PERMISSIONS.put("chunk.set.forceloaded", new Permission(PermissionGroup.MUNICIPALITY, PermissionLevel.STATE_COUNCIL, PermissionLevel.ADMIN));
		PERMISSIONS.put("chunk.set.customtax", new Permission(PermissionGroup.MUNICIPALITY, PermissionLevel.DIS_MANAGER, PermissionLevel.ADMIN));
		PERMISSIONS.put("district.set.chunktax", new Permission(PermissionGroup.MUNICIPALITY, PermissionLevel.DIS_MANAGER, PermissionLevel.MUNICIPALITY_COUNCIL, PermissionLevel.STATE_COUNCIL, PermissionLevel.ADMIN));
		PERMISSIONS.put("state.set.chunk-tax-percentage", new Permission(PermissionGroup.STATE, PermissionLevel.ADMIN));
		PERMISSIONS.put("district.set.unclaim-if-bankrupt", new Permission(PermissionGroup.DISTRICT, PermissionLevel.MUNICIPALITY_COUNCIL, PermissionLevel.ADMIN));
		PERMISSIONS.put("municipality.set.citizentax", new Permission(PermissionGroup.MUNICIPALITY, PermissionLevel.MUNICIPALITY_COUNCIL, PermissionLevel.STATE_COUNCIL, PermissionLevel.ADMIN));
		PERMISSIONS.put("municipality.set.kick-if-bankrupt", new Permission(PermissionGroup.SKIP, PermissionLevel.MUNICIPALITY_MAYOR, PermissionLevel.MUNICIPALITY_COUNCIL, PermissionLevel.ADMIN));
		PERMISSIONS.put("state.set.citizen-tax-percentage", new Permission(PermissionGroup.STATE, PermissionLevel.ADMIN));
		//PERMISSIONS.put("municipality.set.mayor", new Permission(PermissionGroup.SKIP, PermissionLevel.MUNICIPALITY_MAYOR, PermissionLevel.ADMIN));
		PERMISSIONS.put("state.set.leader", new Permission(PermissionGroup.SKIP, PermissionLevel.STATE_LEADER, PermissionLevel.ADMIN));
		PERMISSIONS.put("state.set.mailbox", new Permission(PermissionGroup.STATE, PermissionLevel.STATE_COUNCIL, PermissionLevel.ADMIN));
		PERMISSIONS.put("municipality.set.mailbox", new Permission(PermissionGroup.MUNICIPALITY, PermissionLevel.MUNICIPALITY_COUNCIL, PermissionLevel.ADMIN));
		PERMISSIONS.put("district.set.mailbox", new Permission(PermissionGroup.DISTRICT, PermissionLevel.MUNICIPALITY_COUNCIL, PermissionLevel.ADMIN));
		PERMISSIONS.put("state.open.mailbox", new Permission(PermissionGroup.STATE, PermissionLevel.STATE_COUNCIL, PermissionLevel.ADMIN));
		PERMISSIONS.put("municipality.open.mailbox", new Permission(PermissionGroup.MUNICIPALITY, PermissionLevel.MUNICIPALITY_COUNCIL, PermissionLevel.ADMIN));
		PERMISSIONS.put("district.open.mailbox", new Permission(PermissionGroup.DISTRICT, PermissionLevel.MUNICIPALITY_COUNCIL, PermissionLevel.ADMIN));
		
	}

}