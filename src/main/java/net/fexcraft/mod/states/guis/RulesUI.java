package net.fexcraft.mod.states.guis;

import net.fexcraft.lib.mc.gui.GenericGui;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.states.data.Chunk;
import net.fexcraft.mod.states.data.Rule;
import net.fexcraft.mod.states.data.root.RuleHolder;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraft.block.material.MapColor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;

public class RulesUI extends GenericGui<RulesUIC> {
	
	private static final ResourceLocation texture = new ResourceLocation("states:textures/gui/rules.png");
	private TextField[] tfields = new TextField[16];
	private Rule[] rules = new Rule[16];
	private BasicText title, status;
	private RuleHolder holder;
	private int page, layer;

	public RulesUI(EntityPlayer player, World world, int x, int y, int z){
		super(texture, new RulesUIC(player, world, x, y, z), player);
		this.defbackground = true; this.deftexrect = true; container.gui = this;
		this.xSize = 248; this.ySize = 240; page = y < 0 ? 0 : y; layer = x;
		Chunk chunk = StateUtil.getChunk(player.getPosition());
		switch(layer){
			//case 0: holder = chunk; break;
			case 1: holder = chunk.getDistrict(); break;
			case 2: holder = chunk.getMunicipality(); break;
			//case 3: holder = chunk.getState(); break;
			default: {
				Print.log("Invalid Layer for Rules GUI"); player.closeScreen(); break;
			}
		}
		Rule[] arr = holder.getRules().values().toArray(new Rule[0]); int j = 0;
		for(int i = 0; i < 16; i++){
			if((j = i + (page * 16)) >= holder.getRules().size()) break;
			rules[i] = arr[j]; continue;
		}
	}

	@Override
	protected void init(){
		for(int i = 0; i < 16; i++){
			buttons.put("set" + i, new BasicButton("set" + i, guiLeft + 215, guiTop + 25 + (i * 12), 215, 25, 10, 10, rules[i] != null && rules[i].get() != null));
			buttons.put("rev" + i, new BasicButton("rev" + i, guiLeft + 204, guiTop + 25 + (i * 12), 204, 25, 10, 10, true));
			fields.put("entry" + i, tfields[i] = new TextField(i, fontRenderer, guiLeft + 150, guiTop + 25 + (i * 12), 53, 10, false));
			texts.put("rule" + i, new BasicText(guiLeft + 12, guiTop + 26 + (i * 12), 134, MapColor.SNOW.colorValue, rules[i] == null ? "" : rules[i].id));
			tfields[i].setText("sel. mode");
		}
		buttons.put("next", new BasicButton("next", guiLeft + 230, guiTop + 63, 230, 63, 14, 14, true));
		buttons.put("prev", new BasicButton("prev", guiLeft + 230, guiTop + 49, 230, 49, 14, 14, page > 0));
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
			compound.setIntArray("arr", new int[]{ layer, page + (button.name.equals("next") ? 1 : -1), 0 });
			this.container.send(Side.SERVER, compound); return true;
		}
		return false;
	}

	@Override
	protected void scrollwheel(int am, int x, int y){
		//
	}
	
}

