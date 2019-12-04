package net.fexcraft.mod.states.util;

import net.fexcraft.mod.states.States;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;

public class Perms {
	
	public static final Perm NICKNAME_CHANGE_SELF = new Perm("states.nickname.change.self");
	public static final Perm NICKNAME_CHANGE_OTHERS = new Perm("states.nickname.change.others");
	public static final Perm CREATE_SERVER_SIGN_SHOPS = new Perm("states.create-server-sign-shops");
	public static final Perm MAIL_READ_ANY = new Perm("states.mail.read.any");
	public static final Perm CREATE_STATE = new Perm("states.create.state");
	public static final Perm CREATE_MUNICIPALITY = new Perm("states.create.municipality");

	public static void init(){
		PermissionAPI.registerNode(States.ADMIN_PERM, DefaultPermissionLevel.OP, "States Admin Permission");
		PermissionAPI.registerNode(NICKNAME_CHANGE_SELF.get(), DefaultPermissionLevel.ALL, "NickName Management - Self");
		PermissionAPI.registerNode(NICKNAME_CHANGE_OTHERS.get(), DefaultPermissionLevel.OP, "NickName Management - Others");
		PermissionAPI.registerNode(CREATE_SERVER_SIGN_SHOPS.get(), DefaultPermissionLevel.OP, "Creation of States (SERVER) Sign Shops");
		PermissionAPI.registerNode(MAIL_READ_ANY.get(), DefaultPermissionLevel.OP, "Permission to open ANY kind of mail.");
		PermissionAPI.registerNode(CREATE_STATE.get(), DefaultPermissionLevel.OP, "Creation of a new State");
		PermissionAPI.registerNode(CREATE_MUNICIPALITY.get(), DefaultPermissionLevel.OP, "Creation of a new Municipality");
	}
	
	public static class Perm {
		
		private final String node;
		
		private Perm(String node){
			this.node = node;
		}
		
		public String get(){
			return node;
		}

		public boolean has(EntityPlayer player){
			return PermissionAPI.hasPermission(player, node);
		}
		
	}

}
