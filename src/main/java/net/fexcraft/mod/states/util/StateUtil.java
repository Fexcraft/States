package net.fexcraft.mod.states.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.fexcraft.mod.lib.perms.PermManager;
import net.fexcraft.mod.lib.util.common.Formatter;
import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.lib.util.common.Static;
import net.fexcraft.mod.lib.util.json.JsonUtil;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.api.District;
import net.fexcraft.mod.states.api.Mail;
import net.fexcraft.mod.states.api.Municipality;
import net.fexcraft.mod.states.api.Player;
import net.fexcraft.mod.states.api.State;
import net.fexcraft.mod.states.api.root.AnnounceLevel;
import net.fexcraft.mod.states.impl.GenericChunk;
import net.fexcraft.mod.states.impl.GenericDistrict;
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
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class StateUtil {

	public static Chunk getChunk(EntityPlayer player){
		net.minecraft.world.chunk.Chunk chunk = player.world.getChunkFromBlockCoords(player.getPosition());
		return States.CHUNKS.get(chunk.x, chunk.z);
	}

	public static Chunk getChunk(int x, int z){
		return  States.CHUNKS.get(x, z);
	}

	public static Chunk getChunk(World world, BlockPos pos){
		return States.CHUNKS.get(pos.getX() >> 4, pos.getZ() >> 4);
	}

	public static District getDistrict(int value){
		if(States.DISTRICTS.containsKey(value)){
			return States.DISTRICTS.get(value);
		}
		if(District.getDistrictFile(value).exists()){
			District district = new GenericDistrict(value);
			States.DISTRICTS.put(value, district);
			return district;
		}
		else return States.DISTRICTS.get(-1);
	}

	public static Municipality getMunicipality(int value){
		if(States.MUNICIPALITIES.containsKey(value)){
			return States.MUNICIPALITIES.get(value);
		}
		if(Municipality.getMunicipalityFile(value).exists()){
			Municipality municipality = new GenericMunicipality(value);
			States.MUNICIPALITIES.put(value, municipality);
			return municipality;
		}
		else return States.MUNICIPALITIES.get(-1);
	}

	public static State getState(int value){
		if(States.STATES.containsKey(value)){
			return States.STATES.get(value);
		}
		if(State.getStateFile(value).exists()){
			State state = new GenericState(value);
			States.STATES.put(value, state);
			return state;
		}
		else return States.STATES.get(-1);
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
	public static Player getPlayer(UUID uuid, boolean loadtemp){
		return States.PLAYERS.containsKey(uuid) ? States.PLAYERS.get(uuid) : loadtemp ? getOfflinePlayer(uuid) : null;
	}

	private static Player getOfflinePlayer(UUID uuid){
		JsonElement elm = JsonUtil.read(new File(PermManager.userDir, "/" + uuid.toString() + ".perm"), false);
		if(elm == null){
			return null;
		}
		else{
			JsonObject obj = elm.getAsJsonObject();
			if(!obj.has("AttachedData") || !obj.get("AttachedData").getAsJsonObject().has(States.PLAYER_DATA)){
				return null;
			}
			return GenericPlayer.getOfflineInstance(uuid, obj.get("AttachedData").getAsJsonObject().get(States.PLAYER_DATA).getAsJsonObject());
		}
	}

	public static Player getPlayer(EntityPlayer player){
		return PermManager.getPlayerPerms(player).getAdditionalData(GenericPlayer.class);
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

	public static List<Mail> gatherMailOf(String type, String string){
		// TODO Auto-generated method stub
		return null;
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
					Player playerdata;
					if((playerdata = StateUtil.getPlayer(player)) != null && playerdata.getMunicipality().getState().getId() == range){
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
					Player playerdata;
					if((playerdata = StateUtil.getPlayer(player)) != null && playerdata.getMunicipality().getId() == range){
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

}
