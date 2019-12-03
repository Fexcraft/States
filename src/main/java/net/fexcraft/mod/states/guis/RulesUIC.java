package net.fexcraft.mod.states.guis;

import net.fexcraft.lib.mc.gui.GenericContainer;
import net.fexcraft.lib.mc.gui.GenericGui;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.data.Chunk;
import net.fexcraft.mod.states.data.Rule;
import net.fexcraft.mod.states.data.root.Initiator;
import net.fexcraft.mod.states.data.root.RuleHolder;
import net.fexcraft.mod.states.data.root.Ruleable;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;

public class RulesUIC extends GenericContainer {
	
	protected GenericGui<RulesUIC> gui;
	protected Rule[] rules = new Rule[16];
	protected RuleHolder holder;
	protected int page, layer;
	protected String ruleset;

	public RulesUIC(EntityPlayer player, World world, int x, int y, int z){
		super(player); page = y < 0 ? 0 : y; layer = x;
		if(world.isRemote) return;
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
	protected void packet(Side side, NBTTagCompound packet, EntityPlayer player){
		if(!packet.hasKey("cargo")) return;
		if(side.isServer()){
			if(packet.getString("cargo").equals("open")){
				int[] arr = packet.getIntArray("arr");
				player.openGui(States.MODID, 9, player.world, arr[0], arr[1], arr[2]);
			}
			if(packet.getString("cargo").equals("init")){
				NBTTagCompound compound = new NBTTagCompound(); compound.setString("cargo", "init");
				for(int i = 0; i < 16; i++){
					if(rules[i] == null) break;
					compound.setString("rule" + i, rules[i].id + "," + rules[i].save());
				}
				if(holder instanceof Ruleable) compound.setString("ruleset", ((Ruleable)holder).getRulesetTitle());
				send(Side.CLIENT, compound);
			}
		}
		else{
			if(packet.getString("cargo").equals("init")){
				rules = new Rule[16];
				for(int i = 0; i < 16; i++){
					if(!packet.hasKey("rule" + i)) break;
					String[] arr = packet.getString("rule" + i).split(",");
					Boolean bool = arr.length > 3 ? Boolean.parseBoolean(arr[3]) : null;
					rules[i] = new Rule(arr[0], bool, true, Initiator.valueOf(arr[1]), Initiator.valueOf(arr[2]));
				}
				if(packet.hasKey("ruleset")) ruleset = packet.getString("ruleset");
				((RulesUI)gui).initFromContainer();
			}
		}
	}

}
