package net.fexcraft.mod.states.cmds;

import org.apache.commons.lang3.math.NumberUtils;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.api.registry.fCommand;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.states.api.District;
import net.fexcraft.mod.states.data.Chunk;
import net.fexcraft.mod.states.guis.Listener;
import net.fexcraft.mod.states.util.ImageCache;
import net.fexcraft.mod.states.util.StateLogger;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

@fCommand
public class AdminCmd extends CommandBase {

    @Override
    public String getName(){
        return "st-admin";
    }

    @Override
    public String getUsage(ICommandSender sender){
	return "/st-admin <args>";
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
			Print.chat(sender, "&7/st-admin forceclaim <dis-id>");
			Print.chat(sender, "&7/st-admin ...");
			Print.chat(sender, "&7/st-admin ...");
			return;
		}
		if(sender instanceof EntityPlayer == false){
			Print.chat(sender, "&7Only available Ingame."); return;
		}
		EntityPlayer player = getCommandSenderAsPlayer(sender);
		//PlayerCapability cap = player.getCapability(StatesCapabilities.PLAYER, null);
		Chunk chunk = StateUtil.getChunk(player);
		if(!ChunkCmd.hasPerm("admin", player, null)){
			Print.chat(player, "&7No Permission."); return;
		}
		switch(args[0]){
			case "forceclaim":{
				if(args.length == 1 || !NumberUtils.isCreatable(args[1])){
					Print.chat(player, "&7Please enter the numerical district ID as second argument!");
				}
				else{
					District dis = StateUtil.getDistrict(Integer.parseInt(args[1]), false);
					if(dis == null){
						Print.chat(sender, "&c&oSpecified District not found."); return;
					}
					if(dis.getId() < 0){
						Print.chat(sender, "&4&lCannot force claim into IDs bellow 0!"); return;
					}
					chunk.setDistrict(dis);
					chunk.setClaimer(player.getGameProfile().getId());
					chunk.setChanged(Time.getDate());
					chunk.setPrice(0); chunk.save();
					Print.chat(sender, String.format("&2&lChunk Claimed. [ %s, %s ]", chunk.xCoord(), chunk.zCoord()));
					Listener.updateNeighbors(chunk);
					StateLogger.log(StateLogger.LoggerType.CHUNK, StateLogger.player(player) + " (force-)claimed " + StateLogger.chunk(chunk) + ", it is now part of " + StateLogger.district(dis) + ".");
					ImageCache.update(player.world, player.world.getChunkFromChunkCoords(chunk.xCoord(), chunk.zCoord()));
				}
				return;
			}
		}
	}

}
