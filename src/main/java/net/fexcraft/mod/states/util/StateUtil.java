package net.fexcraft.mod.states.util;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.fexcraft.mod.lib.util.common.Formatter;
import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.lib.util.common.Static;
import net.fexcraft.mod.lib.util.json.JsonUtil;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.api.District;
import net.fexcraft.mod.states.api.Mail;
import net.fexcraft.mod.states.api.Municipality;
import net.fexcraft.mod.states.api.State;
import net.fexcraft.mod.states.api.capabilities.PlayerCapability;
import net.fexcraft.mod.states.api.capabilities.StatesCapabilities;
import net.fexcraft.mod.states.api.root.AnnounceLevel;
import net.fexcraft.mod.states.impl.GenericChunk;
import net.fexcraft.mod.states.impl.GenericDistrict;
import net.fexcraft.mod.states.impl.GenericMail;
import net.fexcraft.mod.states.impl.GenericMunicipality;
import net.fexcraft.mod.states.impl.GenericPlayer;
import net.fexcraft.mod.states.impl.GenericState;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.server.permission.PermissionAPI;

public class StateUtil {
    
    public static @Nullable Chunk getChunk(int x, int z){
        return States.CHUNKS.get(x, z);
    }
    
    public static @Nullable Chunk getChunk(EntityPlayer player){
        return getChunk(player.getPosition());
    }
    
    public static @Nullable Chunk getChunk(BlockPos pos){
        return getChunk(pos.getX() >> 4, pos.getZ() >> 4);
    }
    
    public static @Nullable Chunk getChunk(net.minecraft.world.chunk.Chunk chunk){
        if(chunk != null){
            return chunk.getCapability(StatesCapabilities.CHUNK, null).getStatesChunk();
        }
        return null;
    }
    
    public static Chunk getTempChunk(int x, int z){
        Chunk chunk = getChunk(x, z);
        return chunk == null ? new GenericChunk(x, z, false) : chunk;
    }
    
    public static Chunk getTempChunk(ResourceLocation ckpos){
        int x = Integer.parseInt(ckpos.getResourceDomain());
        int z = Integer.parseInt(ckpos.getResourcePath());
        return getTempChunk(x, z);
    }
	
	public static District getDistrict(int value){
		return getDistrict(value, true);
	}

	public static District getDistrict(int value, boolean wilderness){
		if(States.DISTRICTS.containsKey(value)){
			return States.DISTRICTS.get(value);
		}
		if(District.getDistrictFile(value).exists()){
			District district = new GenericDistrict(value);
			States.DISTRICTS.put(value, district);
			return district;
		}
		else return wilderness ? States.DISTRICTS.get(-1) : null;
	}
	
	public static Municipality getMunicipality(int value){
		return getMunicipality(value, true);
	}

	public static Municipality getMunicipality(int value, boolean bool){
		if(States.MUNICIPALITIES.containsKey(value)){
			return States.MUNICIPALITIES.get(value);
		}
		if(Municipality.getMunicipalityFile(value).exists()){
			Municipality municipality = new GenericMunicipality(value);
			States.MUNICIPALITIES.put(value, municipality);
			return municipality;
		}
		else return bool ? States.MUNICIPALITIES.get(-1) : null;
	}
	
	public static State getState(int value){
		return getState(value, true);
	}

	public static State getState(int value, boolean bool){
		if(States.STATES.containsKey(value)){
			return States.STATES.get(value);
		}
		if(State.getStateFile(value).exists()){
			State state = new GenericState(value);
			States.STATES.put(value, state);
			return state;
		}
		else return bool ? States.STATES.get(-1) : null;
	}

	public static boolean isUUID(String owner){
		try{
			UUID uuid = UUID.fromString(owner);
			return uuid != null;
		}
		catch(Exception e){
			return false;
		}
	}
	
	@Nullable
	public static PlayerCapability getPlayer(UUID uuid, boolean loadtemp){
		return States.PLAYERS.containsKey(uuid) ? States.PLAYERS.get(uuid) : loadtemp ? GenericPlayer.getOfflineInstance(uuid) : null;
	}

	public static void sendMail(Mail mail){
		if(mail.getRecipientType().equals("player")){
			UUID uuid = UUID.fromString(mail.getRecipient());
			EntityPlayerMP player = Static.getServer().getPlayerList().getPlayerByUUID(uuid);
			if(player != null){
				Print.chat(player, "&0[&eSt&0]&6 You have got new mail!");
			}
		}
		mail.save();
	}

	public static List<Mail> gatherMailOf(String type, String string, boolean read){
		File folder = new File(States.getSaveDirectory(), "mails/" + type + "/" + string + "/");
		ArrayList<Mail> list = new ArrayList<>();
		if(!folder.exists()){ return list; }
		for(File file : folder.listFiles()){
			if(file.getName().endsWith(read ? ".read" : ".unread")){
				try{
					list.add(new GenericMail(file.getName().replace(read ? ".read" : ".unread", ""), type, string));
				}
				catch (Exception e){
					e.printStackTrace();
				}
			}
		}
		return list;
	}

	public static int getUnreadMailsOf(String rectype, String string){
		File folder = new File(States.getSaveDirectory(), "mails/" + rectype + "/" + string + "/");
		int i = 0;
		if(folder.exists()){
			for(String file : folder.list()){
				if(file.endsWith(".unread")){
					i++;
				}
			}
		}
		return i;
	}

	public static void announce(MinecraftServer server, String string){
		announce(server, AnnounceLevel.ALL, string, 0);
		return;
	}
	
	public static void announce(MinecraftServer server, AnnounceLevel level, String string, int range){
		announce(server, level, string, range, null);
	}

	public static void announce(MinecraftServer server, AnnounceLevel level, String string, int range, ICommandSender sender){
		server = server == null ? Static.getServer() : server;
		switch(level){
			case ALL:
				server.getPlayerList().sendMessage(new TextComponentString(Formatter.format(string)), true);
				Sender.sendToWebhook(null, string);
				break;
			case UNION:
				//TODO doesn't exists yet.
				break;
			case STATE:
				server.getPlayerList().getPlayers().forEach(player -> {
					PlayerCapability playerdata;
					if((playerdata = player.getCapability(StatesCapabilities.PLAYER, null)) != null && playerdata.getMunicipality().getState().getId() == range){
						Print.chat(player, string);
					}
				});
				break;
			case STATE_ALL:
				server.getPlayerList().getPlayers().forEach(player -> {
					if(StateUtil.getChunk(player).getDistrict().getMunicipality().getState().getId() == range){
						Print.chat(player, string);
					}
				});
				break;
			case MUNICIPALITY:
				server.getPlayerList().getPlayers().forEach(player -> {
					PlayerCapability playerdata;
					if((playerdata = player.getCapability(StatesCapabilities.PLAYER, null)) != null && playerdata.getMunicipality().getId() == range){
						Print.chat(player, string);
					}
				});
				break;
			case MUNICIPALITY_ALL:
				server.getPlayerList().getPlayers().forEach(player -> {
					if(StateUtil.getChunk(player).getDistrict().getMunicipality().getId() == range){
						Print.chat(player, string);
					}
				});
				break;
			case DISTRICT:
				server.getPlayerList().getPlayers().forEach(player -> {
					if(StateUtil.getChunk(player).getDistrict().getId() == range){
						Print.chat(player, string);
					}
				});
				break;
			case AREAL:
				List<EntityPlayerMP> players = getPlayersInRange(server, sender, range);
				players.forEach(player -> { Print.chat(player, string); });
				break;
			default:
				break;
		}
	}

	private static List<EntityPlayerMP> getPlayersInRange(MinecraftServer server, ICommandSender sender, int range){
		if(sender == null || server == null){ return new ArrayList<EntityPlayerMP>(); }
		List<EntityPlayerMP> list = new ArrayList<EntityPlayerMP>();
		Vec3d position = sender.getCommandSenderEntity().getPositionVector();
        for(EntityPlayerMP player : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()){
            if(player.dimension == sender.getCommandSenderEntity().dimension){
                double d4 = position.x - player.posX;
                double d5 = position.y - player.posY;
                double d6 = position.z - player.posZ;
                if(d4 * d4 + d5 * d5 + d6 * d6 < range * range){
                    list.add(player);
                }
            }
        }
		return list;
	}

    public static @Nullable net.minecraft.world.chunk.Chunk getChunk(Chunk chunk){
        return Static.getServer().worlds[0].getChunkProvider().getLoadedChunk(chunk.xCoord(), chunk.zCoord());
    }

	public static boolean isAdmin(EntityPlayer sender){
		return PermissionAPI.hasPermission(sender, States.ADMIN_PERM);
	}
	
	// --- /// --- //
	
	public static JsonObject getStateJson(int value){
		JsonElement elm = JsonUtil.read(State.getStateFile(value), false);
		if(elm == null){
			InputStream in = StateUtil.class.getClassLoader().getResourceAsStream("/assets/states/defaults/states/" + value + ".json");
			 return in == null ? new JsonObject() : JsonUtil.getObjectFromInputStream(in);
		}
		else return elm.getAsJsonObject();
	}
	
	public static JsonObject getMunicipalityJson(int value){
		JsonElement elm = JsonUtil.read(Municipality.getMunicipalityFile(value), false);
		if(elm == null){
			InputStream in = StateUtil.class.getClassLoader().getResourceAsStream("/assets/states/defaults/municipalities/" + value + ".json");
			 return in == null ? new JsonObject() : JsonUtil.getObjectFromInputStream(in);
		}
		else return elm.getAsJsonObject();
	}
	
	public static JsonObject getDistrictJson(int value){
		JsonElement elm = JsonUtil.read(District.getDistrictFile(value), false);
		if(elm == null){
			InputStream in = StateUtil.class.getClassLoader().getResourceAsStream("/assets/states/defaults/districts/" + value + ".json");
			 return in == null ? new JsonObject() : JsonUtil.getObjectFromInputStream(in);
		}
		else return elm.getAsJsonObject();
	}

}
