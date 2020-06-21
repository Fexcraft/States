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
	 * 6 Chunk Manager
	 * 7 Companies
	 * 8 Player Data
	 * 9 Rules
	 * 10 Chunk claiming.
	 * 20 Mailbox
	 * */
	
	public static final int WELCOME = 0;
	public static final int REGION_VIEW = 1;
	public static final int RULE_EDITOR = 9;
	public static final int CLAIM_MAP = 10;
	public static final int MAILBOX = 20;

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z){
		return ID == 9 ? new RulesUIC(player, world, x, y, z) : ID == 20 ? new MailboxUIC(player, world, x, y, z) : new PlaceholderContainer();
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z){
		//return new GeneralGui(ID, player, world, x, y, z);
		switch(ID){
			case WELCOME: return new WelcomeGui(player, world, x, y, z);
			case REGION_VIEW: return new AreaView(player, world, x, y, z);
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
