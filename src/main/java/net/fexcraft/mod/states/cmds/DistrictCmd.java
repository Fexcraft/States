package net.fexcraft.mod.states.cmds;

import static net.fexcraft.mod.states.guis.GuiHandler.MANAGER_DISTRICT;
import static net.fexcraft.mod.states.guis.GuiHandler.RULE_EDITOR;
import static net.fexcraft.mod.states.guis.GuiHandler.openGui;

import java.util.List;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.fsmm.api.Bank;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.data.Chunk;
import net.fexcraft.mod.states.data.District;
import net.fexcraft.mod.states.data.DistrictAttribute;
import net.fexcraft.mod.states.data.DistrictType;
import net.fexcraft.mod.states.data.Municipality;
import net.fexcraft.mod.states.data.capabilities.PlayerCapability;
import net.fexcraft.mod.states.data.capabilities.StatesCapabilities;
import net.fexcraft.mod.states.guis.Listener;
import net.fexcraft.mod.states.guis.ManagerContainer;
import net.fexcraft.mod.states.util.AliasLoader;
import net.fexcraft.mod.states.util.StateLogger;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class DistrictCmd extends CommandBase {
	
	@Override
	public String getName(){
		return AliasLoader.getOverride("dis");
	}

	@Override
	public String getUsage(ICommandSender sender){
		return "/" + getName();
	}
	
	@Override
	public List<String> getAliases(){
		return AliasLoader.getAlias("dis");
	}
	
	@Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender){
        return sender != null;
    }
	
	@Override
    public int getRequiredPermissionLevel(){
        return 0;
    }

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(args.length == 0){
			Print.chat(sender, "&7/dis info");
			Print.chat(sender, "&7/dis rules");
			Print.chat(sender, "&7/dis types");
			Print.chat(sender, "&7/dis create");
			Print.chat(sender, "&7/dis attributes");
			return;
		}
		EntityPlayer player = (EntityPlayer)sender.getCommandSenderEntity();
		PlayerCapability ply = player.getCapability(StatesCapabilities.PLAYER, null);
		if(ply == null){
			Print.chat(sender, "&4Error loading Playerdata.");
			return;
		}
		Chunk chunk = StateUtil.getChunk(player);
		District dis = chunk.getDistrict();
		switch(args[0]){
			case "info":{
				openGui(player, MANAGER_DISTRICT, ManagerContainer.Mode.INFO.ordinal(), dis.getId(), 0);
				return;
			}
			case "rules":{
				openGui(player, RULE_EDITOR, 1, 0, 0);
				return;
			}
			case "types":{
				Print.chat(sender, "&9Existing district types:");
				for(DistrictType type : DistrictType.values()){
					Print.chat(sender, "&2-> &3 " + type.toDetailedString());
				}
				Print.chat(sender, "&9While the &7#&9 signs in order mean:");
				Print.chat(sender, "&7housing, commerce, industry, cultivation, exploitation");
				return;
			}
			case "attributes":{
				Print.chat(sender, "&9Existing district attributes:");
				for(DistrictAttribute type : DistrictAttribute.values()){
					Print.chat(sender, "&2-> &3 " + type.name().toLowerCase());
				}
				Print.chat(sender, "&9Each district type may have a different set of attributes.");
				return;
			}
			case "create":{
				if(ply.getMunicipality().isAuthorized(ply.getMunicipality().r_CREATE_DISTRICT.id, ply.getUUID()).isTrue() || StateUtil.bypass(player)){
					if(ply.getMunicipality().getDistricts().size() + 1 > ply.getMunicipality().getDistrictLimit()){
						Print.chat(sender, "&aYour Municipality reached the limit of possible Districts.");
						return;
					}
					if(chunk.getDistrict().getId() >= 0){
						Print.chat(sender, "&cThis chunk is already part of a District.");
						return;
					}
					if(!nearbySameMunicipality(chunk, ply.getMunicipality())){
						Print.chat(sender, "No nearby/connected chunks are of the same Municipality.");
						return;
					}
					long price = net.fexcraft.mod.states.util.StConfig.DISTRICT_CREATION_PRICE;
					if(price > ply.getMunicipality().getAccount().getBalance()){
						Print.chat(sender, "&9Not enough money on Municipality Account.");
						return;
					}
					Bank bank = ply.getMunicipality().getBank();
					if(bank.isNull()){
						Print.chat(sender, "&9Your bank couldn't be found.");
						return;
					}
					if(args.length < 2){
						Print.chat(sender, "&9No name for new District Specified.");
						return;
					}
					String name = args[1];
					if(args.length > 2){
						for(int i = 2; i < args.length; i++){
							name += " " + args[i];
						}
					}
					try{
						District newdis = new District(sender.getEntityWorld().getCapability(StatesCapabilities.WORLD, null).getNewDistrictId());
						if(newdis.getDistrictFile().exists() || StateUtil.getDistrict(newdis.getId()).getId() >= 0){
							throw new Exception("Tried to create new District with ID '" + newdis.getId() + "', but savefile already exists.");
						}
						else{
							long halfprice = price / 2;
							if(halfprice == 0 || bank.processAction(Bank.Action.TRANSFER, sender, ply.getMunicipality().getAccount(), halfprice, States.SERVERACCOUNT)){
								bank.processAction(Bank.Action.TRANSFER, null, ply.getMunicipality().getAccount(), halfprice, States.SERVERACCOUNT);
								newdis.setCreator(ply.getUUID());
								newdis.setClaimedChunks(1);
								newdis.setName(name);
								newdis.r_CFS.set(false);
								newdis.setCreated(Time.getDate());
								newdis.setChanged(Time.getDate());
								newdis.setHead(ply.getUUID());
								newdis.setMunicipality(ply.getMunicipality());
								newdis.setPrice(0);
								newdis.setType(DistrictType.WILDERNESS);
								newdis.setIcon(States.DEFAULT_ICON);
								newdis.color.set(0xffffff);
								chunk.setDistrict(newdis);
								newdis.getMunicipality().save();
								newdis.save();
								chunk.save();
								States.DISTRICTS.put(newdis.getId(), newdis);
								StateUtil.announce(server, "&9New District was created!");
								StateUtil.announce(server, "&9Created by " + ply.getFormattedNickname());
								StateUtil.announce(server, "&9Name&0: &7" + newdis.getName());
								Print.log(StateLogger.player(player) + " created " + StateLogger.district(newdis) + " at " + StateLogger.chunk(chunk) + ".");
								return;
							}
						}
					}
					catch(Exception e){
						Print.chat(sender, "Error: " + e.getMessage());
						Print.chat(sender, e);
						Print.debug(e);
						return;
					}
				}
				else{
					Print.chat(sender, "&cNo permission.");
					return;
				}
				return;
			}
			default:{
				Print.chat(sender, "&cInvalid Argument.");
				return;
			}
		}
	}

	private boolean nearbySameMunicipality(Chunk ck, Municipality mun){
		Chunk chunk = null;
		for(int[] cor : Listener.coords){
			chunk = StateUtil.getChunk(ck.xCoord() + cor[0], ck.zCoord() + cor[1]);
			if(chunk != null && chunk.getMunicipality().getId() == mun.getId()){
				return true;
			}
		}
		return false;
	}
	
}
