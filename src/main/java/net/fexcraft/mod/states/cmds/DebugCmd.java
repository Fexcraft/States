package net.fexcraft.mod.states.cmds;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import net.fexcraft.lib.common.json.JsonUtil;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.api.registry.fCommand;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.data.Chunk;
import net.fexcraft.mod.states.data.capabilities.StatesCapabilities;
import net.fexcraft.mod.states.util.ForcedChunksManager;
import net.fexcraft.mod.states.util.StateUtil;
import net.fexcraft.mod.states.util.UpdateHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

@fCommand
public class DebugCmd extends CommandBase {
    
    @Override
    public String getName(){
        return "st-debug";
    }
    
    @Override
    public String getUsage(ICommandSender sender){
        return "/st-debug";
    }
    
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos){
        return args.length == 1 ? Arrays.asList(new String[]{ "chunk", "chunks", "district", "districts", "municipality", "municipalities", "state", "states", "serveraccount", "permission", "permissions", "self", "totals", "check-for-updates", "loadedchunks"}) : Collections.<String>emptyList();
    }
    
    private long lastcheck;
    
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if(args.length == 0){
            Print.chat(sender, "&7/st-debug chunk");
            Print.chat(sender, "&7/st-debug chunks");
            Print.chat(sender, "&7/st-debug district");
            Print.chat(sender, "&7/st-debug districts");
            Print.chat(sender, "&7/st-debug municipality");
            Print.chat(sender, "&7/st-debug municipalities");
            Print.chat(sender, "&7/st-debug state");
            Print.chat(sender, "&7/st-debug states");
            Print.chat(sender, "&7/st-debug serveraccount");
            Print.chat(sender, "&7/st-debug self");
            Print.chat(sender, "&7/st-debug totals");
            Print.chat(sender, "&7/st-debug check-for-updates");
            Print.chat(sender, "&7/st-debug loadedchunks");
            return;
        }
        if(sender instanceof EntityPlayer == false){
            Print.chat(sender, "&7Only available Ingame.");
            return;
        }
        Chunk chunk = StateUtil.getChunk((EntityPlayer)sender);
        switch(args[0]){
            case "chunks":{
                if(Static.dev()){
                    Static.getServer().worlds[0].getChunkProvider().getLoadedChunks().forEach(ck -> {
                        Chunk chk = ck.getCapability(StatesCapabilities.CHUNK, null).getStatesChunk(true);
                        if(chk != null){
                            Print.log(chk.toJsonObject().toString());
                        }
                    });
                    Print.chat(sender, "Chunk JSON's printed into console.");
                }
                Print.chat(sender, "&7Chunks (World0) loaded: &a" + Static.getServer().worlds[0].getChunkProvider().getLoadedChunkCount());
                Print.chat(sender, "&7Chunks (States) loaded: &a" + States.CHUNKS.size());
                return;
            }
            case "ck": case "chunk":{
                Print.chat(sender, JsonUtil.setPrettyPrinting(chunk.toJsonObject()));
                return;
            }
            case "district":{
                Print.chat(sender, JsonUtil.setPrettyPrinting(chunk.getDistrict().toJsonObject()));
                return;
            }
            case "districts":{
                if(Static.dev()){
                    States.DISTRICTS.values().forEach((elm) -> {
                        Print.log(JsonUtil.setPrettyPrinting(elm.toJsonObject()));
                    });
                    Print.chat(sender, "Districts JSON's printed into console.");
                }
                Print.chat(sender, "&9Districts loaded: &a" + States.DISTRICTS.size());
                Print.chat(sender, "&9Districts indata: &a" + States.DISTRICTS.firstEntry().getValue().getDistrictFile().getParentFile().listFiles().length);
                return;
            }
            case "municipality":{
                Print.chat(sender, JsonUtil.setPrettyPrinting(chunk.getDistrict().getMunicipality().toJsonObject()));
                return;
            }
            case "municipalities":{
                if(Static.dev()){
                    States.MUNICIPALITIES.values().forEach((elm) -> {
                        Print.log(JsonUtil.setPrettyPrinting(elm.toJsonObject()));
                    });
                    Print.chat(sender, "Municipality JSON's printed into console.");
                }
                Print.chat(sender, "&9Municipalities loaded: &a" + States.MUNICIPALITIES.size());
                Print.chat(sender, "&9Municipalities indata: &a" + States.MUNICIPALITIES.firstEntry().getValue().getMunicipalityFile().getParentFile().listFiles().length);
                Print.chat(sender, "Municipality JSON's printed into console.");
                return;
            }
            case "state":{
                Print.chat(sender, JsonUtil.setPrettyPrinting(chunk.getDistrict().getMunicipality().getState().toJsonObject()));
                return;
            }
            case "states":{
                if(Static.dev()){
                    States.STATES.values().forEach((elm) -> {
                        Print.log(JsonUtil.setPrettyPrinting(elm.toJsonObject()));
                    });
                    Print.chat(sender, "State JSON's printed into console.");
                }
                Print.chat(sender, "&9States loaded: &a" + States.STATES.size());
                Print.chat(sender, "&9States indata: &a" + States.STATES.firstEntry().getValue().getStateFile().getParentFile().listFiles().length);
                return;
            }
            case "serveraccount":{
                Print.chat(sender, "&9Server Account Balance: &7" + Config.getWorthAsString(States.SERVERACCOUNT.getBalance()));
                return;
            }
            case "self":{
                Print.chat(sender, sender.getCommandSenderEntity().getCapability(StatesCapabilities.PLAYER, null).toJsonObject());
                return;
            }
            case "check-for-updates":{
                if(lastcheck + (Time.MIN_MS + 15) < Time.getDate()){
                    Print.chat(sender, "Requesting update data...");
                    UpdateHandler.getDataFromServer();
                    lastcheck = Time.getDate();
                }
                else{
                    Print.chat(sender, "Please wait at least 15 minutes between requests.");
                }
                return;
            }
            case "totals": case "total":{
                Print.chat(sender, "&e====-====-====-====-====-====&0[&2States&0]");
                Print.chat(sender, "&9States: &7" + States.STATES.size() + "&8/&3" + chunk.getState().getStateFile().getParentFile().listFiles().length);
                Print.chat(sender, "&9Municipalities: &7" + States.MUNICIPALITIES.size() + "&8/&3" + chunk.getMunicipality().getMunicipalityFile().getParentFile().listFiles().length);
                Print.chat(sender, "&9Districts: &7" + States.DISTRICTS.size() + "&8/&3" + chunk.getDistrict().getDistrictFile().getParentFile().listFiles().length);
                Print.chat(sender, "&7Chunks: &a" + Static.getServer().worlds[0].getChunkProvider().getLoadedChunkCount() + "&8/&3" + States.CHUNKS.size());
                Print.chat(sender, "&6RAM: &5" + MB(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())+ "&8/&7" + MB(Runtime.getRuntime().totalMemory()));
                return;
            }
            case "loadedchunks":{
            	long l = 0; for(Collection<?> coll : States.LOADED_CHUNKS.values()){ l += coll.size(); }
            	long loaded = ForcedChunksManager.getLoadedChunksInTickets();
            	long tickets = ForcedChunksManager.getTickets().size();
                Print.chat(sender, "&7Chunks (World0) force-loaded: &a" + (tickets == 0 ? "no tickets loaded" : loaded));
                Print.chat(sender, "&7Chunks (States) force-loaded: &a" + l);
                Print.chat(sender, "&7Chunks (Forge) per ticket: &a" + ForcedChunksManager.chunksPerTicket());
                Print.chat(sender, "&7Tickets (Forge) available: &a" + ForcedChunksManager.maxTickets());
                Print.chat(sender, "&7Tickets in use: &a" + tickets + " &7| &9ratio: &3" + Static.divide(loaded, tickets) + "&7 per ticket");
                if(ForcedChunksManager.getTickets().isEmpty() && l > 0){
                	Print.chat(sender, "&cThere seems to be an error, chunks are registered as loaded, but there are no (forge-chunk-loader) tickets!");
                }
            	return;
            }
        }
    }
    
    private long MB(long byt){
        return (byt / 1024) / 1024;
    }
    
}
