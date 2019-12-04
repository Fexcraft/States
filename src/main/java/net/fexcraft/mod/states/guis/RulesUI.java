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
	private BasicButton[] rev = new BasicButton[16], set = new BasicButton[16], val = new BasicButton[16];
	private TextField[] tfields = new TextField[16];
	private byte[] fieldmode = new byte[16];
	protected BasicText title, status;

	public RulesUI(EntityPlayer player, World world, int x, int y, int z){
		super(texture, new RulesUIC(player, world, x, y, z), player);
		this.defbackground = true; this.deftexrect = true; container.gui = this;
		this.xSize = 256; this.ySize = 240;
	}

	@Override
	protected void init(){
		NBTTagCompound compound = new NBTTagCompound(); compound.setString("cargo", "init");
		container.send(Side.SERVER, compound);
		texts.put("title", title = new BasicText(guiLeft + 10, guiTop + 8, 216, MapColor.SNOW.colorValue, "waiting for server response..."));
		texts.put("status", status = new BasicText(guiLeft + 10, guiTop + 225, 236, MapColor.SNOW.colorValue, "waiting for server response..."));
	}
	
	protected void initFromContainer(){
		for(int i = 0; i < 16; i++){
			buttons.put("val" + i, val[i] = new BasicButton("val" + i, guiLeft + 235, guiTop + 25 + (i * 12), 235, 25, 10, 10, container.rules[i] != null && container.rules[i].get() != null));
			buttons.put("set" + i, set[i] = new BasicButton("set" + i, guiLeft + 224, guiTop + 25 + (i * 12), 224, 25, 10, 10, true));
			buttons.put("rev" + i, rev[i] = new BasicButton("rev" + i, guiLeft + 213, guiTop + 25 + (i * 12), 213, 25, 10, 10, true));
			if(i != 15) fields.put("entry" + i, tfields[i] = new TextField(i, fontRenderer, guiLeft + 150, guiTop + 25 + (i * 12), 62, 10, false));
			else{
				fields.put("entry" + i, tfields[i] = new TextField(i, fontRenderer, guiLeft + 150, guiTop + 25 + (i * 12), 62, 10, false){
					@Override
					public void drawTextBox(){
						super.drawTextBox(); if(val[0] == null) return;
						for(int i = 0; i < 16; i++){
							if(val[i].hovered) drawHoveringText("Set Value of this Rule.", val[i].x, val[i].y);
							if(set[i].hovered) drawHoveringText("Set Setter of this Rule.", set[i].x, set[i].y);
							if(rev[i].hovered) drawHoveringText("Set Reviser of this Rule.", rev[i].x, rev[i].y);
						};
					}
				});
			}
			texts.put("rule" + i, new BasicText(guiLeft + 12, guiTop + 26 + (i * 12), 134, MapColor.SNOW.colorValue, container.rules[i] == null ? "" : container.rules[i].id));
			tfields[i].setText(container.rules[i] == null ? "" : "sel. mode"); tfields[i].setEnabled(container.rules[i] != null);
		}
		buttons.put("next", new BasicButton("next", guiLeft + 241, guiTop + 5, 241, 5, 10, 10, true));
		buttons.put("prev", new BasicButton("prev", guiLeft + 230, guiTop + 5, 230, 5, 10, 10, container.page > 0));
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
		if(button.name.startsWith("val") && button.enabled){
			int i = Integer.parseInt(button.name.replace("val", ""));
			if(fieldmode[i] != 3){
				fieldmode[i] = 3; tfields[i].setText(container.rules[i].get() + "");
			}
			else{
				NBTTagCompound compound = new NBTTagCompound(); compound.setString("cargo", "val");
				compound.setInteger("rule", i); compound.setBoolean("value", Boolean.parseBoolean(tfields[i].getText()));
				this.container.send(Side.SERVER, compound); status.string = "sending to server..."; return true;
			}
		}
		if(button.name.startsWith("rev") || button.name.startsWith("set")){
			boolean set = button.name.startsWith("set");
			int i = Integer.parseInt(button.name.replace(set ? "set" : "rev", ""));
			if(fieldmode[i] != (set ? 2 : 1)){
				fieldmode[i] = (byte)(set ? 2 : 1);
				tfields[i].setText((set ? container.rules[i].setter : container.rules[i].reviser) + "");
			}
			else{
				NBTTagCompound compound = new NBTTagCompound(); compound.setString("cargo", set ? "set" : "rev");
				compound.setInteger("rule", i); compound.setString("value", tfields[i].getText());
				this.container.send(Side.SERVER, compound); status.string = "sending to server..."; return true;
			}
		}
		return false;
	}

	@Override
	protected void scrollwheel(int am, int x, int y){
		//
	}
	
}

