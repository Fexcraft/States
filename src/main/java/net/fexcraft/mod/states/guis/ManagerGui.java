package net.fexcraft.mod.states.guis;

import net.fexcraft.lib.mc.gui.GenericGui;
import net.fexcraft.lib.mc.utils.Formatter;
import net.fexcraft.mod.states.guis.ManagerContainer.Layer;
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
	private boolean normallist, confirmopen, confirmmode;
	private int scroll;

	public ManagerGui(EntityPlayer player, int layer, int x, int y, int z){
		super(STONE, new ManagerContainer(player, layer, x, y, z), player);
		//this.defbackground = false;
		this.deftexrect = true;
		container.set(this);
	}

	@Override
	protected void init(){
		if(container.mode.isInfo()){
			xSize = 256;
			ySize = 217;
			texloc = VIEW;
		}
		else{
			normallist = container.mode == Mode.LIST_NEIGHBORS;
			if(container.mode == Mode.LIST_COMPONENTS && container.layer == Layer.MUNICIPALITY) normallist = true;
			if(container.mode == Mode.LIST_CITIZENS && container.layer == Layer.STATE) normallist = true;
			xSize = 224;
			ySize = normallist ? 184 : 200;
			texloc = normallist ? LIST : EDITLIST;
		}
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
        buttons.clear();
        texts.clear();
        fields.clear();
        //
        texts.put("title", new BasicText(guiLeft + 8, guiTop + 8, 196, texcol, ""));
        if(container.mode.isInfo()){
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
        }
        else{
			buttons.put("su", new BasicButton("scroll_up", guiLeft + 204, guiTop + 36, 204, 36, 14, 14, true));
			buttons.put("sd", new BasicButton("scroll_down", guiLeft + 204, guiTop + (normallist ? 164 : 180), 204, (normallist ? 164 : 180), 14, 14, true));
			buttons.put(normallist ? "home" : "add", new BasicButton(normallist ? "home" : "add", guiLeft + 204, guiTop + 20, 204, 20, 14, 14, true));
			int texoff = normallist ? 20 : 36;
			for(int i = 0; i < 10; i++){
				texts.put("field" + i, new BasicText(guiLeft + 10, guiTop + 3 + texoff + (i * 16), 188, texcol, ""));
				buttons.put("row" + i, new BasicButton("row" + i, guiLeft + 6, guiTop + texoff + (i * 16), 6, texoff, 196, 14, true));
				/*TextField field = new TextField(i, fontRenderer, guiLeft + 10, guiTop + texoff + (i * 16), 188, 8);
				field.setEnableBackgroundDrawing(false);
				field.setEnabled(false);
				field.setTextColor(texcol);
				field.setDisabledTextColour(texcol);
				fields.put("field" + i, field);*/
			}
			if(!normallist){
				TextField field = new TextField(10, fontRenderer, guiLeft + 10, guiTop + 23, 188, 8);
				field.setEnableBackgroundDrawing(false);
				field.setEnabled(true);
				field.setTextColor(texcol);
				fields.put("add", field);
				//
				for(int i = 0; i < 10; i++){
					buttons.put("rem" + i, new BasicButton("rem" + i, guiLeft + 191, guiTop + 39 + (i * 16), 191, 39, 8, 8, true));
				}
				buttons.put("cancel", new BasicButton("cancel", guiLeft + 6, guiTop + 90, 6, 236, 70, 14, true));
				buttons.put("confirm", new BasicButton("confirm", guiLeft + 77, guiTop + 90, 77, 236, 70, 14, true));
				buttons.get("cancel").visible = buttons.get("confirm").visible = false;
				texts.put("confirm0", new BasicText(guiLeft + 8, guiTop + 64, 208, texcol, ""));
				texts.put("confirm1", new BasicText(guiLeft + 8, guiTop + 78, 208, texcol, ""));
				texts.get("confirm0").visible = texts.get("confirm1").visible = false;
				texts.put("cancel", new BasicText(guiLeft + 10, guiTop + 93, 62, texcol, "Cancel"));
				texts.put("confirm", new BasicText(guiLeft + 81, guiTop + 93, 62, texcol, "Confirm"));
				texts.get("cancel").visible = texts.get("confirm").visible = false;
        	}
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
		if(confirmopen){
			drawTexturedModalRect(guiLeft, guiTop + 56, 0, 202, 224, 54);
		}
	}

	@Override
	protected boolean buttonClicked(int mouseX, int mouseY, int mouseButton, String key, BasicButton button){
		if(key.startsWith("mode")){
			NBTTagCompound packet = new NBTTagCompound();
			packet.setString("cargo", "view_mode_click");
			int buttonid = Integer.parseInt(key.replace("mode", ""));
			if(scroll + buttonid >= container.keys.length) return true;
			packet.setInteger("button", scroll + buttonid);
			if(container.view_modes[scroll + buttonid] == ViewMode.EDIT){
				packet.setString("value", fields.get("field" + buttonid).getText());
			}
			container.send(Side.SERVER, packet);
			return true;
		}
		if(key.startsWith("row")){
			NBTTagCompound packet = new NBTTagCompound();
			packet.setString("cargo", "list_mode_click");
			int buttonid = Integer.parseInt(key.replace("row", ""));
			if(scroll + buttonid >= container.keys.length) return true;
			packet.setInteger("button", scroll + buttonid);
			container.send(Side.SERVER, packet);
			return true;
		}
		if(key.startsWith("rem")){
			int buttonid = Integer.parseInt(key.replace("rem", ""));
			if(scroll + buttonid >= container.keys.length) return true;
			openConfirm(false, container.keys[buttonid]);
			return true;
		}
		if(key.equals("home")){
			NBTTagCompound packet = new NBTTagCompound();
			packet.setString("cargo", "list_mode_home");
			container.send(Side.SERVER, packet);
			return true;
		}
		if(key.equals("add")){
			openConfirm(true, fields.get("add").getText());
			return true;
		}
		if(key.equals("cancel")){
			this.refreshKeys();
			return true;
		}
		if(key.equals("confirm")){
			NBTTagCompound packet = new NBTTagCompound();
			packet.setString("cargo", "list_mode_" + (confirmmode ? "add" : "remove"));
			if(!confirmmode){
				int buttonid = Integer.parseInt(key.replace("rem", ""));
				if(scroll + buttonid >= container.keys.length) return true;
				packet.setInteger("button", scroll + buttonid);
			}
			else{
				packet.setString("input", fields.get("add").getText());
			}
			container.send(Side.SERVER, packet);
			return true;
		}
		switch(key){
			case "su": scroll(-1); break;
			case "sd": scroll(+1); break;
		}
		//
		return false;
	}

	private void openConfirm(boolean mode, String input){
		this.confirmopen = true;
		this.confirmmode = mode;
		for(int i = 1; i < 5; i++){
			texts.get("field" + i).visible = false;
			buttons.get("row" + i).visible = false;
			buttons.get("rem" + i).visible = false;
		}
		buttons.get("cancel").visible = buttons.get("confirm").visible = true;
		texts.get("confirm0").visible = texts.get("confirm1").visible = true;
		texts.get("cancel").visible = texts.get("confirm").visible = true;
		String midfix = container.mode.name().toLowerCase() + "_" + container.layer.name().toLowerCase();
		String string = "states.manager_gui." + midfix + "." + (confirmmode ? "add" : "rem");
		texts.get("confirm0").string = I18n.hasKey(string + "0") ? Formatter.format(I18n.format(string + "0", input)) : ">";
		texts.get("confirm1").string = I18n.hasKey(string + "1") ? Formatter.format(I18n.format(string + "1", input)) : ">";
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
		if(confirmopen){
			confirmopen = false;
			for(int i = 1; i < 5; i++){
				texts.get("field" + i).visible = true;
				buttons.get("row" + i).visible = true;
				buttons.get("rem" + i).visible = true;
			}
			buttons.get("cancel").visible = buttons.get("confirm").visible = false;
			texts.get("confirm0").visible = texts.get("confirm1").visible = false;
			texts.get("cancel").visible = texts.get("confirm").visible = false;
		}
		for(int j = 0; j < container.mode.entries(); j++){
			int i = j + scroll;
			String keyval = i >= container.keys.length ? "" : container.keys[i];
			if(container.mode.isInfo()){
				texts.get("key" + j).string = I18n.format("states.manager_gui.view_" + layerid + "." + keyval);
			}
			else{
				texts.get("field" + j).string = keyval;
			}
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
