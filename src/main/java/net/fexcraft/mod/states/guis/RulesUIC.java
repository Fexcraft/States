package net.fexcraft.mod.states.guis;

import static net.fexcraft.mod.states.guis.GuiHandler.RULE_EDITOR;

import java.util.UUID;

import net.fexcraft.lib.mc.gui.GenericContainer;
import net.fexcraft.lib.mc.gui.GenericGui;
import net.fexcraft.lib.mc.utils.Formatter;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.states.data.Chunk;
import net.fexcraft.mod.states.data.Rule;
import net.fexcraft.mod.states.data.root.Initiator;
import net.fexcraft.mod.states.data.sub.Manageable;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;

public class RulesUIC extends GenericContainer {
	
	protected GenericGui<RulesUIC> gui;
	protected Rule[] rules = new Rule[16];
	protected Manageable manage;
	protected int page, layer;
	protected String ruleset;

	public RulesUIC(EntityPlayer player, World world, int x, int y, int z){
		super(player); page = y < 0 ? 0 : y; layer = x;
		if(world.isRemote) return;
		Chunk chunk = StateUtil.getChunk(player.getPosition());
		switch(layer){
			//case 0: holder = chunk; break;
			case 1: manage = chunk.getDistrict().manage; break;
			case 2: manage = chunk.getMunicipality().manage; break;
			case 3: manage = chunk.getState().manage; break;
			default: {
				Print.log("Invalid Layer for Rules GUI"); player.closeScreen(); break;
			}
		}
		Rule[] arr = manage.getRuleHolder().getMap().values().toArray(new Rule[0]); int j = 0;
		for(int i = 0; i < 16; i++){
			if((j = i + (page * 16)) >= manage.getRuleHolder().getMap().size()) break;
			rules[i] = arr[j]; continue;
		}
	}

	@Override
	protected void packet(Side side, NBTTagCompound packet, EntityPlayer player){
		if(!packet.hasKey("cargo")) return;
		if(side.isServer()){
			switch(packet.getString("cargo")){
				case "open": {
					GuiHandler.openGui(player, RULE_EDITOR, packet.getIntArray("arr"));
					return;
				}
				case "init": {
					NBTTagCompound compound = new NBTTagCompound(); compound.setString("cargo", "init");
					for(int i = 0; i < 16; i++){
						if(rules[i] == null) break;
						compound.setString("rule" + i, rules[i].id + "," + rules[i].save());
					}
					compound.setString("ruleset", manage.getRulesetTitle());
					send(Side.CLIENT, compound);
					return;
				}
				case "val": {
					int i = packet.getInteger("rule");
					if(rules[i].get() == null){
						sendStatus("Rule cannot be set - valueless.");
						return;
					}
					boolean passed = false; UUID uuid = player.getGameProfile().getId();
					if(StateUtil.isAdmin(player)){
						passed = true;
					}
					else{
						Rule.Result res = manage.isAuthorized(rules[i].id, uuid);
						if(res.isVote()){
							sendStatus("&bThis needs a vote to change! &7/st-rule");
							passed = false;
						}
						else passed = res.isTrue();
					}
					if(!passed){
						sendStatus("&bNo permission to set this rule.");
					}
					else{
						rules[i].set(Boolean.parseBoolean(packet.getString("value")));
						sendStatus("&aRule updated to: " + rules[i].get());
					}
					return;
				}
				case "rev": case "set": {
					int i = packet.getInteger("rule"); boolean set = packet.getString("cargo").equals("set");
					UUID uuid = player.getGameProfile().getId();
					boolean vote, pass;
					Rule.Result res;
					if(set){
						res = manage.isAuthorized(rules[i].id, uuid);
					}
					else{
						res = manage.canRevise(rules[i].id, uuid);
					}
					vote = res.isVote();
					pass = res.isTrue();
					if(pass || StateUtil.isAdmin(player)){
						try{
							Initiator init = Initiator.valueOf(packet.getString("value").toUpperCase());
							if(set && !init.isValidAsSetter(rules[i].isVotable())){
								sendStatus("&bInvalid Initiator.");
							}
							else{
								if(set) rules[i].setter = init; else rules[i].reviser = init;
								sendStatus("&aRule " + (set ? "SET" : "REV") + " updated to: " + init.name());
							}
						}
						catch(Exception e){
							sendStatus("&bError parsing Initiator.");
						}
					}
					else if(vote){
						sendStatus("&bThis needs a vote to change! &7/st-rule");
					}
					else{
						sendStatus("&bNo permission to revise this.");
					}
					return;
				}
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
				((RulesUI)gui).initFromContainer(); return;
			}
			else if(packet.getString("cargo").equals("status")){
				((RulesUI)gui).status.string = Formatter.format(packet.getString("status"));
			}
		}
	}
	
	private void sendStatus(String status){
		NBTTagCompound compound = new NBTTagCompound(); compound.setString("cargo", "status");
		compound.setString("status", status);send(Side.CLIENT, compound);
	}

}
