package net.fexcraft.mod.states.guis;

import net.fexcraft.lib.mc.gui.GenericGui;
import net.fexcraft.lib.mc.utils.Formatter;
import net.fexcraft.mod.states.guis.ManagerContainer.Mode;
import net.fexcraft.mod.states.guis.ManagerContainer.ViewMode;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;

public class ManagerGui extends GenericGui<ManagerContainer> {
	
	public static final ResourceLocation STONE = new ResourceLocation("minecraft:textures/blocks/stone.png");
	public static final ResourceLocation LIST = new ResourceLocation("states:textures/gui/manager_list.png");
	public static final ResourceLocation VIEW = new ResourceLocation("states:textures/gui/manager_view.png");
	public static final ResourceLocation EDITLIST = new ResourceLocation("states:textures/gui/manager_list_editable.png");
	public static int texcol = MapColor.SNOW.colorValue;
	private int scroll;

	public ManagerGui(EntityPlayer player, int layer, int x, int y, int z){
		super(STONE, new ManagerContainer(player, layer, x, y, z), player);
		//this.defbackground = false;
		this.deftexrect = true;
		container.set(this);
	}

	@Override
	protected void init(){
		switch(container.mode){
			case INFO:
			case CKINFO:
				xSize = 256;
				ySize = 217;
				texloc = VIEW;
				break;
			case LIST_COMPONENTS:
			case LIST_NEIGHBORS:
				xSize = 224;
				ySize = 184;
				texloc = LIST;
				break;
			case LIST_CITIZENS:
			case LIST_COUNCIL:
			case LIST_BWLIST:
				xSize = 224;
				ySize = 200;
				texloc = EDITLIST;
				break;
			default:
				xSize = 0;
				ySize = 0;
				texloc = STONE;
				break;
		}
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
        buttons.clear();
        texts.clear();
        fields.clear();
        //
        texts.put("title", new BasicText(guiLeft + 8, guiTop + 8, 196, texcol, ""));
        switch(container.mode){
			case INFO:
				buttons.put("su", new BasicButton("scroll_up", guiLeft + 239, guiTop + 21, 239, 21, 11, 13, true));
				buttons.put("sd", new BasicButton("scroll_down", guiLeft + 239, guiTop + 198, 239, 198, 11, 13, true));
				for(int i = 0; i < container.mode.entries(); i++){
					int offset = i * 16;
					buttons.put("mode" + i, new BasicButton("mode" + i, guiLeft + 225, guiTop + 23 + offset, 225, 23, 10, 10, true));
					texts.put("key" + i, new BasicText(guiLeft + 9, guiTop + 24 + offset, 109, texcol, ""));
					TextField field = new TextField(i, fontRenderer, guiLeft + 125, guiTop + 24 + offset, 96, 8);
					field.setEnableBackgroundDrawing(false);
					field.setEnabled(false);
					field.setTextColor(texcol);
					field.setDisabledTextColour(texcol);
					fields.put("field" + i, field);
				}
				break;
			case LIST_COMPONENTS:
				break;
			case LIST_CITIZENS:
				break;
			case LIST_COUNCIL:
				break;
			default: return;
        }
		NBTTagCompound packet = new NBTTagCompound();
		packet.setString("cargo", "init");
		container.send(Side.SERVER, packet);
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
		if(key.startsWith("mode")){
			NBTTagCompound packet = new NBTTagCompound();
			packet.setString("cargo", "view_mode_click");
			int buttonid = Integer.parseInt(key.replace("mode", ""));
			packet.setInteger("button", scroll + buttonid);
			if(container.view_modes[scroll + buttonid] == ViewMode.EDIT){
				packet.setString("value", fields.get("field" + buttonid).getText());
			}
			container.send(Side.SERVER, packet);
		}
		switch(key){
			case "su": scroll(-1); break;
			case "sd": scroll(+1); break;
		}
		//
		return false;
	}

	@Override
	protected void scrollwheel(int am, int x, int y){
		scroll(am > 0 ? 1 : -1);
	}

	private void scroll(int i){
		scroll += i;
		if(scroll < 0) scroll = 0;
		if(scroll + container.mode.entries() > container.keys.length){
			scroll = container.keys.length - container.mode.entries();
		}
		refreshKeys();
	}

	protected void refreshKeys(){
		String layerid = container.layer.name().toLowerCase();
		setTitle("states.manager_gui.title_" + layerid);
		for(int j = 0; j < container.mode.entries(); j++){
			int i = j + scroll;
			if(i >= container.keys.length) break;
			texts.get("key" + j).string = I18n.format("states.manager_gui.view_" + layerid + "." + container.keys[i]);
			if(container.mode == Mode.INFO){
				TextField field = fields.get("field" + j);
				switch(container.view_values[i]){
					case ManagerContainer.NOONE:{
						field.setText(I18n.format("states.manager_gui.view.no_one"));
						break;
					}
					case ManagerContainer.NOTAX:{
						field.setText(I18n.format("states.manager_gui.view.no_tax"));
						break;
					}
					case ManagerContainer.NOMAILBOX:{
						field.setText(I18n.format("states.manager_gui.view.no_mailbox"));
						break;
					}
					case ManagerContainer.NOTFORSALE:{
						field.setText(I18n.format("states.manager_gui.view.not_for_sale"));
						break;
					}
					case ManagerContainer.NONE:{
						field.setText(I18n.format("states.manager_gui.view.none"));
						break;
					}
					case ManagerContainer.UNKNOWN:{
						field.setText(I18n.format("states.manager_gui.view.unknown"));
						break;
					}
					default:{
						field.setText(container.view_values[i]);
						break;
					}
				}
				if(container.view_values[i].startsWith("#") && container.keys[i].equals("color")){
					field.setTextColor(Integer.parseInt(field.getText().replace("#", ""), 16));
				}
				else field.setTextColor(texcol);
				field.setEnabled(container.view_modes[i] == ViewMode.EDIT);
				BasicButton button = buttons.get("mode" + j);
				button.tx = container.view_modes[i].ordinal() * 10;
				button.ty = 246;
				button.enabled = container.view_modes[i] != ViewMode.NONE;
			}
		}
	}

	protected void setTitle(String string){
		texts.get("title").string = Formatter.format(I18n.format(string, container.layer_title));
	}

}
