package net.fexcraft.mod.states.impl.capabilities;

import java.util.UUID;

import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.lib.util.common.Formatter;
import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.lib.util.common.Static;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.api.ChunkType;
import net.fexcraft.mod.states.util.ImageCache;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.capabilities.Capability;

public class TESImplementation implements TESCapability {
	
	private TileEntitySign tileentity;
	private boolean isStatesSign = false;
	private String mode;
	private long lastupdate = 0;

	@Override
	public void setup(Chunk chunk){
		isStatesSign = true;
		mode = tileentity.signText[1].getUnformattedText().toLowerCase();
		update(chunk, null, true);
	}

	@Override
	public void setTileEntity(TileEntitySign tileentity){
		this.tileentity = tileentity;
	}

	@Override
	public TileEntitySign getTileEntity(){
		return tileentity;
	}

	@Override
	public void update(Chunk chunk, String task, boolean send){
		if(!isStatesSign || chunk == null || (task != null && !task.equals(mode))){
			return;
		}
		//
		switch(mode){
			case "chunk":{
				if(chunk.getPrice() > 0){
					tileentity.signText[1] = new TextComponentString(Formatter.format("&2For Sale!"));
					tileentity.signText[2] = new TextComponentString(Config.getWorthAsString(chunk.getPrice()));
					//tileentity.signText[3] = new TextComponentString(Formatter.format("&2" + (chunk.getType() == ChunkType.PRIVATE ? Static.getPlayerNameByUUID(UUID.fromString(chunk.getOwner())) : chunk.getOwner())));
				}
				else{
					switch(chunk.getType()){
						case PRIVATE:{
							tileentity.signText[1] = new TextComponentString(Formatter.format("&cPrivate Property"));
							tileentity.signText[2] = new TextComponentString(Formatter.format("&c- - - -"));
							break;
						}
						case PUBLIC:{
							tileentity.signText[1] = new TextComponentString(Formatter.format("&cPublic Access"));
							tileentity.signText[2] = new TextComponentString(Formatter.format("&cProperty"));
							break;
						}
						default:
							tileentity.signText[1] = new TextComponentString(Formatter.format("&9Managed"));
							tileentity.signText[2] = new TextComponentString(Formatter.format("&9Property"));
							break;
					}
				}
				if(chunk.getDistrict().getMunicipality().getState().getId() >= 0){
					tileentity.signText[3] = new TextComponentString(Formatter.format("&2" + (chunk.getType() == ChunkType.PRIVATE ? Static.getPlayerNameByUUID(UUID.fromString(chunk.getOwner())) : chunk.getOwner())));
				}
				else{
					tileentity.signText[3] = new TextComponentString(Formatter.format("&2Wilderness"));
				}
				tileentity.signText[0] = new TextComponentString(Formatter.format("&0[&9States&0]&2> &8Chunk"));
				break;
			}
			case "district":{
				tileentity.signText[1] = new TextComponentString(Formatter.format("&9" + chunk.getDistrict().getName()));
				tileentity.signText[2] = new TextComponentString(Formatter.format("&6" + chunk.getDistrict().getType().name().toLowerCase()));
				tileentity.signText[3] = new TextComponentString(Formatter.format(chunk.getDistrict().getManager() == null ? "&cno manager" : "&2" + Static.getPlayerNameByUUID(chunk.getDistrict().getManager())));
				//
				tileentity.signText[0] = new TextComponentString(Formatter.format("&0[&9States&0]&2> &8District"));
				break;
			}
			case "municipality":{
				tileentity.signText[1] = new TextComponentString(Formatter.format("&9" + chunk.getDistrict().getMunicipality().getName()));
				tileentity.signText[2] = new TextComponentString(Formatter.format("&6" + chunk.getDistrict().getMunicipality().getType().getTitle()));
				tileentity.signText[3] = new TextComponentString(Formatter.format(chunk.getDistrict().getMunicipality().getMayor() == null ? "&cno mayor" : "&2" + Static.getPlayerNameByUUID(chunk.getDistrict().getMunicipality().getMayor())));
				//
				tileentity.signText[0] = new TextComponentString(Formatter.format("&0[&9St&0]&2> &8Municipality"));
				break;
			}
			case "state":{
				tileentity.signText[1] = new TextComponentString(Formatter.format("&9" + chunk.getDistrict().getMunicipality().getState().getName()));
				tileentity.signText[2] = new TextComponentString(Formatter.format("&6 - - - "));
				tileentity.signText[3] = new TextComponentString(Formatter.format(chunk.getDistrict().getMunicipality().getState().getLeader() == null ? "&cno mayor" : "&2" + Static.getPlayerNameByUUID(chunk.getDistrict().getMunicipality().getState().getLeader())));
				//
				tileentity.signText[0] = new TextComponentString(Formatter.format("&0[&9States&0]&2> &8State"));
				break;
			}
			case "map":{
				tileentity.signText[0] = new TextComponentString(Formatter.format("&0[&9States&0]&2> &8Map"));
				String str = tileentity.signText[2].getUnformattedText().toLowerCase();
				boolean found = false;
				str = str.replace("s.", "surface_");
				for(String string : ImageCache.TYPES){
					if(str.equals(string)){
						found = true;
						str = string.replace("surface_", "s.");
						break;
					}
				}
				if(!found){
					str = "surface";
				}
				tileentity.signText[1] = new TextComponentString(str);
				tileentity.signText[2] = new TextComponentString(Formatter.format("&7" + chunk.xCoord() + "x"));
				tileentity.signText[3] = new TextComponentString(Formatter.format("&7" + chunk.zCoord() + "z"));
				break;
			}
			default:{
				//Invalid mode, thus let's unmark this;
				this.isStatesSign = false;
				break;
			}
		}
		lastupdate = chunk.getChanged();
		//
		if(send){
			Static.getServer().getPlayerList().getPlayers().forEach(player -> {
				player.connection.sendPacket(tileentity.getUpdatePacket());
			});
		}
	}

	@Override
	public NBTBase writeToNBT(Capability<TESCapability> capability, EnumFacing side){
		NBTTagCompound compound = new NBTTagCompound();
		compound.setBoolean("StatesSign", isStatesSign);
		if(isStatesSign){
			compound.setString("Mode", mode);
			compound.setLong("LastUpdate", lastupdate);
		}
		Print.debug("W: " + compound);
		return compound;
	}

	@Override
	public void readNBT(Capability<TESCapability> capability, EnumFacing side, NBTBase nbt){
		NBTTagCompound compound = (NBTTagCompound)nbt;
		if(isStatesSign = compound.getBoolean("StatesSign")){
			mode = compound.getString("Mode");
			lastupdate = compound.getLong("LastUpdate");
		}
		Print.debug("R:" + compound);
	}

	@Override
	public boolean isStatesSign(){
		return isStatesSign;
	}

	@Override
	public void onPlayerInteract(Chunk chunk, EntityPlayer player){
		if(chunk.getChanged() != this.lastupdate){
			Print.chat(player, "&7Sign Updating...");
			update(chunk, null, true);
			return;
		}
		Print.debug(player, chunk.toJsonObject());
		switch(mode){
			case "chunk":{
				Static.getServer().commandManager.executeCommand(player, chunk.getPrice() > 0 ? ("ck buy via-sign " + tileentity.getPos().toLong()) : "ck info");
				break;
			}
			case "district":{
				Static.getServer().commandManager.executeCommand(player, "dis info");
				break;
			}
			case "municipality":{
				Static.getServer().commandManager.executeCommand(player, "mun info");
				break;
			}
			case "state":{
				Static.getServer().commandManager.executeCommand(player, "st info");
				break;
			}
			case "map":{
				String str = tileentity.signText[1].getUnformattedText().toLowerCase().replace("s.", "surface_");
				int j = -1;
				for(int i = 0; i < ImageCache.TYPES.length; i++){
					if(ImageCache.TYPES[i].equals(str)){
						j = i;
						break;
					}
				}
				if(j < 0){ j = 0; }
				player.openGui(States.INSTANCE, 1, player.world, j, 0, 0);
				break;
			}
			default:{
				//Invalid mode, thus let's unmark this;
				this.isStatesSign = false;
				break;
			}
		}
	}

}
