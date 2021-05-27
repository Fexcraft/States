package net.fexcraft.mod.states.guis;

import static net.fexcraft.mod.states.States.INSTANCE;

import net.fexcraft.mod.states.guis.mail.MailboxUI;
import net.fexcraft.mod.states.guis.mail.MailboxUIC;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler{
	
	/**
	 * 0 Welcome Screen
	 * 1 Chunk/Area View
	 * 2 Districts
	 * 3 Municipalities
	 * 4 States
	 * 5 Unions
	 * 6 Companies
	 * 7 Player Data
	 * 8 Chunk Manager
	 * 9 Property Manager
	 * 10 Chunk claiming.
	 * 20 Mailbox
	 * 30 Rules
	 * 40 Vote
	 * */
	
	public static final int WELCOME = 0;
	public static final int RULE_EDITOR = 30;
	public static final int CLAIM_MAP = 10;
	public static final int MAILBOX = 20;
	//
	public static final int MANAGER_DISTRICT = 1;
	public static final int MANAGER_MUNICIPALITY = 2;
	public static final int MANAGER_COUNTY = 3;
	public static final int MANAGER_STATE = 4;
	public static final int MANAGER_COMPANY = 6;
	public static final int MANAGER_PLAYERDATA = 7;
	public static final int MANAGER_CHUNK = 8;
	public static final int MANAGER_PROPERTY = 9;

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z){
		if(ID >= 1 && ID < 10) return new ManagerContainer(player, ID - 1, x, y, z);
		switch(ID){
			case RULE_EDITOR: return new RulesUIC(player, world, x, y, z);
			case MAILBOX: return new MailboxUIC(player, world, x, y, z);
			//
			default: return new PlaceholderContainer();
		}
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z){
		if(ID >= 1 && ID < 10) return new ManagerGui(player, ID - 1, x, y, z);
		switch(ID){
			case WELCOME: return new WelcomeGui(player, world, x, y, z);
			//case REGION_VIEW: return new AreaView(player, world, x, y, z);
			case RULE_EDITOR: return new RulesUI(player, world, x, y, z);
			//
			case CLAIM_MAP: return new ClaimMap(player, world, x, y, z);
			case MAILBOX: return new MailboxUI(player, world, x, y, z);
		}
		return null;
	}

	public static final void openGui(EntityPlayer player, int guiid, BlockPos position){
		player.openGui(INSTANCE, guiid, player.world, position.getX(), position.getY(), position.getZ());
	}

	public static final void openGui(EntityPlayer player, int guiid, int x, int y, int z){
		player.openGui(INSTANCE, guiid, player.world, x, y, z);
	}

	public static final void openGui(EntityPlayer player, int guiid, int[] array){
		player.openGui(INSTANCE, guiid, player.world, array[0], array[1], array[2]);
	}

}
