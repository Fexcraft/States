package net.fexcraft.mod.states.guis;

import net.fexcraft.lib.mc.gui.GenericGui;
import net.minecraft.block.material.MapColor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;

public class RulesUI extends GenericGui<RulesUIC> {
	
	private static final ResourceLocation texture = new ResourceLocation("states:textures/gui/rules.png");
	private BasicButton[] rev = new BasicButton[16], set = new BasicButton[16];
	private TextField[] tfields = new TextField[16];
	private BasicText title, status;

	public RulesUI(EntityPlayer player, World world, int x, int y, int z){
		super(texture, new RulesUIC(player, world, x, y, z), player);
		this.defbackground = true; this.deftexrect = true; container.gui = this;
		this.xSize = 248; this.ySize = 240;
	}

	@Override
	protected void init(){
		NBTTagCompound compound = new NBTTagCompound(); compound.setString("cargo", "init");
		container.send(Side.SERVER, compound);
		texts.put("title", title = new BasicText(guiLeft + 10, guiTop + 8, 216, MapColor.SNOW.colorValue, "waiting for server response..."));
		texts.put("status", status = new BasicText(guiLeft + 10, guiTop + 225, 216, MapColor.SNOW.colorValue, "waiting for server response..."));
	}
	
	protected void initFromContainer(){
		for(int i = 0; i < 16; i++){
			buttons.put("set" + i, set[i] = new BasicButton("set" + i, guiLeft + 215, guiTop + 25 + (i * 12), 215, 25, 10, 10, container.rules[i] != null && container.rules[i].get() != null));
			buttons.put("rev" + i, rev[i] = new BasicButton("rev" + i, guiLeft + 204, guiTop + 25 + (i * 12), 204, 25, 10, 10, true));
			if(i != 15) fields.put("entry" + i, tfields[i] = new TextField(i, fontRenderer, guiLeft + 150, guiTop + 25 + (i * 12), 53, 10, false));
			else{
				fields.put("entry" + i, tfields[i] = new TextField(i, fontRenderer, guiLeft + 150, guiTop + 25 + (i * 12), 53, 10, false){
					@Override
					public void drawTextBox(){
						super.drawTextBox(); if(set[0] == null) return;
						for(int i = 0; i < 16; i++){
							if(set[i].hovered) drawHoveringText("Set Value of this Rule.", set[i].x, set[i].y);
							if(rev[i].hovered) drawHoveringText("Revise this Rule.", rev[i].x, rev[i].y);
						};
					}
				});
			}
			texts.put("rule" + i, new BasicText(guiLeft + 12, guiTop + 26 + (i * 12), 134, MapColor.SNOW.colorValue, container.rules[i] == null ? "" : container.rules[i].id));
			tfields[i].setText(container.rules[i] == null ? "" : "sel. mode"); tfields[i].setEnabled(container.rules[i] != null);
		}
		buttons.put("next", new BasicButton("next", guiLeft + 230, guiTop + 63, 230, 63, 14, 14, true));
		buttons.put("prev", new BasicButton("prev", guiLeft + 230, guiTop + 49, 230, 49, 14, 14, container.page > 0));
		switch(container.layer){
			case 0: title.string = "Chunk Rules"; break;
			case 1: title.string = "Dis.: " + container.ruleset; break;
			case 2: title.string = "Mun.: " + container.ruleset; break;
			case 4: title.string = "State: " + container.ruleset; break;
			default: title.string = "INVALID LAYER"; break;
		} status.string = "";
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
		if(button.name.equals("prev") || button.name.equals("next")){
			NBTTagCompound compound = new NBTTagCompound(); compound.setString("cargo", "open");
			compound.setIntArray("arr", new int[]{ container.layer, container.page + (button.name.equals("next") ? 1 : -1), 0 });
			this.container.send(Side.SERVER, compound); return true;
		}
		if(button.name.startsWith("set") && button.enabled){
			//TODO check perm
		}
		if(button.name.startsWith("rev")){
			//TODO check perm
		}
		return false;
	}

	@Override
	protected void scrollwheel(int am, int x, int y){
		//
	}
	
}

