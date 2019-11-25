package net.fexcraft.mod.states.guis.mail;

import net.fexcraft.lib.mc.gui.GenericGui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class MailboxUI extends GenericGui<MailboxUIC> {
	
	private static final ResourceLocation texture = new ResourceLocation("states:textures/gui/mail_view.png");

	public MailboxUI(EntityPlayer player, World world, int x, int y, int z){
		super(texture, new MailboxUIC(player, world, x, y, z), player);
		this.defbackground = true; this.deftexrect = true; container.gui = this;
		this.xSize = 248; this.ySize = 207;
	}

	@Override
	protected void init(){
		//
	}

	@Override
	protected void predraw(float pticks, int mouseX, int mouseY){
		//
	}

	@Override
	protected void drawbackground(float pticks, int mouseX, int mouseY){
		//
	}

	@Override
	protected boolean buttonClicked(int mouseX, int mouseY, int mouseButton, String key, BasicButton button){
		return false;
	}

	@Override
	protected void scrollwheel(int am, int x, int y){
		//
	}
	
}

