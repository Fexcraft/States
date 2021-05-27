package net.fexcraft.mod.states.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map.Entry;
import java.util.TimerTask;
import java.util.UUID;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;

import net.fexcraft.lib.common.json.JsonUtil;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.api.Bank;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.data.Chunk;
import net.fexcraft.mod.states.data.ChunkPos;
import net.fexcraft.mod.states.data.ChunkType;
import net.fexcraft.mod.states.data.Municipality;
import net.fexcraft.mod.states.data.PlayerImpl;
import net.fexcraft.mod.states.data.capabilities.PlayerCapability;
import net.fexcraft.mod.states.data.capabilities.StatesCapabilities;
import net.fexcraft.mod.states.data.root.Mailbox.MailType;
import net.fexcraft.mod.states.data.root.Mailbox.RecipientType;
import net.minecraft.entity.player.EntityPlayerMP;

public class TaxSystem extends TimerTask {
	
	private static long last_interval;
	private static boolean loaded;
	
	public static final String COLLECTOR = "TaxCollector";
	public static final MailType SYSTEMMAIL = MailType.SYSTEM;

	@Override
	public void run(){
		try{
			load();
			long date = Time.getDate();
			if(invalidInterval(last_interval, date)){
				Print.log("Tried to process TAX data, but it is not time for that yet.");
				Print.log("LTAX: " + last_interval + " + INTERVAL: " + StConfig.TAX_INTERVAL + " > NOW: " + date);
				return;
			}
			MessageSender.as(null, "Starting regular tax collection.");
			Print.log("Collecting tax from loaded chunks...");
			ImmutableMap<ChunkPos, Chunk> map = ImmutableMap.copyOf(States.CHUNKS);
			for(Chunk chunk  : map.values()){
				TaxSystem.processChunkTax(date, chunk);
			}
			Print.log("Collecting tax from online players...");
			ArrayList<EntityPlayerMP> players = new ArrayList<>(Static.getServer().getPlayerList().getPlayers());
			for(EntityPlayerMP player : players){
				TaxSystem.processPlayerTax(date, player.getCapability(StatesCapabilities.PLAYER, null));
			}
			if(StConfig.TAX_OFFLINE_PLAYERS){
				Print.log("Collecting tax from offline players...");
				TaxSystem.processOfflinePlayerTax(players, date);
			}
			//
			Print.log("Collecting tax from force-loaded chunks...");
			ImmutableMap<Integer, List<ChunkPos>> maplc = ImmutableMap.copyOf(States.LOADED_CHUNKS);
			for(Entry<Integer, List<ChunkPos>> entry : maplc.entrySet()){
				TaxSystem.processLoadedChunkTax(date, entry.getKey(), entry.getValue());
			}
			MessageSender.as(null, "Finished collecting tax.");
			last_interval = date; save();
		}
		catch(Exception e){
			MessageSender.as(null, "TAX COLLECTION ERRORED!");
			Print.log("An error occured while collecting tax, further collection is halted for this interval.");
			e.printStackTrace();
		}
	}

	private static boolean invalidInterval(long last, long date){
		return last + StConfig.TAX_INTERVAL - 100 > date;
	}

	public static void processLoadedChunkTax(long date, Integer key, List<ChunkPos> value){
		if(!loaded){ return; }
		//if(value.lastTaxCollection() + Config.TAX_INTERVAL > date){ return; }
		long conf = StConfig.LOADED_CHUNKS_TAX;
		if(conf > 0){
			Municipality mun = StateUtil.getMunicipality(key, false);
			if(mun == null){ return; }//TODO maybe remove the collection?
			for(int i = 0; i < value.size(); i++){
				if(mun.getAccount().getBalance() < conf){
					ChunkPos pos = States.LOADED_CHUNKS.get(key).remove(i);
					MailUtil.send(null, RecipientType.MUNICIPALITY, getMayor(mun), COLLECTOR, "Municipality didn't have enough money to pay the tax for force-loading the " + StateLogger.chunk(pos) + "! As such, force-loading got disabled.", SYSTEMMAIL);
					Print.log("Municipality didn't have enough money to pay the tax for force-loading the " + StateLogger.chunk(pos) + "! As such, force-loading got disabled.");
					ForcedChunksManager.requestUnload(pos);
				}
				else{
					mun.getBank().processAction(Bank.Action.TRANSFER, Static.getServer(), mun.getAccount(), conf, States.SERVERACCOUNT);
				}
			}
		}
	}

	private static void processOfflinePlayerTax(ArrayList<EntityPlayerMP> players, long date){
		File folder = new File(States.getSaveDirectory(), "players/");
		if(folder == null || !folder.exists()){ return; }
		for(File file : folder.listFiles()){
			try{
				UUID uuid = UUID.fromString(file.getName().replace(".json", ""));
				if(isOnline(players, uuid)){ continue; }
				PlayerImpl player = new PlayerImpl(uuid);
				processPlayerTax(date, player);
				player.save();
				player.unload();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	private static boolean isOnline(ArrayList<EntityPlayerMP> players, UUID uuid){
		for(EntityPlayerMP player : players){
			if(player.getGameProfile().getId().equals(uuid)){
				return true;
			}
		}
		return false;
	}

	public static void processPlayerTax(long date, PlayerCapability cap){
		if(!loaded || cap == null){ return; }
		if(cap.lastTaxCollection() + StConfig.TAX_INTERVAL > date){ return; }
		//if(cap.getMunicipality().getId() <= -1){ return; }
		long tax = cap.getCustomTax() > 0 ? cap.getCustomTax() : cap.getMunicipality().getCitizenTax();
		if(tax > 0){
			Account account = cap.getAccount();
			Account receiver = cap.getMunicipality().getAccount();
			Account state = cap.getState().getAccount();
			Bank bank = cap.getBank();
			if(account.getBalance() < tax){
				if((account.getBalance() <= 0 || bank.isNull()) && cap.getMunicipality().r_KIB.get()){
					MailUtil.send(null, RecipientType.PLAYER, cap.getUUIDAsString(), COLLECTOR, "You didn't have enough money to pay your citizen tax, as such, you got kicked.", SYSTEMMAIL);
					MailUtil.send(null, RecipientType.MUNICIPALITY, cap.getMunicipality().getId(), COLLECTOR, StateLogger.player(cap) + " did not have enough money to pay the tax, following the Municipality's settings, that player got kicked.", SYSTEMMAIL);
					cap.setMunicipality(StateUtil.getMunicipality(-1));
					cap.onTaxCollected(date);
					return;
				}
				else if(account.getBalance() > 0 && bank != null && cap.getMunicipality().r_KIB.get()){
					bank.processAction(Bank.Action.TRANSFER, Static.getServer(), account, account.getBalance(), cap.getMunicipality().getAccount());
					MailUtil.send(null, RecipientType.PLAYER, cap.getUUIDAsString(), COLLECTOR, "WARNING! You didn't have enough money to pay your tax, next tax collection cycle you may get kicked!", SYSTEMMAIL);
					MailUtil.send(null, RecipientType.MUNICIPALITY, cap.getMunicipality().getId(), COLLECTOR, StateLogger.player(cap) + " did not have enough money to pay the full tax.", SYSTEMMAIL);
					cap.onTaxCollected(date);
					return;
				}
				else if(account.getBalance() > 0 && bank != null && !cap.getMunicipality().r_KIB.get()){
					bank.processAction(Bank.Action.TRANSFER, Static.getServer(), account, account.getBalance(), cap.getMunicipality().getAccount());
					MailUtil.send(null, RecipientType.PLAYER, cap.getUUIDAsString(), COLLECTOR, StateLogger.player(cap) + " did not have enough money to pay the full tax.", SYSTEMMAIL);
					MailUtil.send(null, RecipientType.MUNICIPALITY, cap.getMunicipality().getId(), COLLECTOR, StateLogger.player(cap) + " did not have enough money to pay the full tax.", SYSTEMMAIL);
					cap.onTaxCollected(date);
				}
				if(bank.isNull()){
					Print.log("Tax collection for " + StateLogger.player(cap) + " could not be completed as the player's bank is NULL, additionally, the player didn't have enough money to pay the tax.");
				}
			}
			else if(bank.isNull()){
				Print.log("Tax collection for " + StateLogger.player(cap.getEntityPlayer()) + " could not be completed as the player's bank is NULL.");
			}
			long statetax = tax > 1000 ? getPercentage(tax, cap.getState().getCitizenTaxPercentage()) : 0;
			long muntax = tax > 1000 ? tax - statetax : tax;
			bank.processAction(Bank.Action.TRANSFER, Static.getServer(), account, muntax, receiver);
			if(state != null && statetax > 0){
				bank.processAction(Bank.Action.TRANSFER, Static.getServer(), account, statetax, state);
			}
		}
		else cap.onTaxCollected(date);
	}

	private static String getMayor(Municipality mun){
		return mun.manage.getHead() == null ? mun.manage.getCouncil().size() <= 0 ? States.CONSOLE_UUID : mun.manage.getCouncil().get(0).toString() : mun.manage.getHead().toString();
	}

	public static void processChunkTax(long date, Chunk value){
		if(!loaded){ return; }
		if(invalidInterval(value.lastTaxCollection(), date)){ return; }
		if(value.getLink() != null){
			Chunk chunk = StateUtil.getChunk(value.getLink());
			if(chunk == null ? (chunk = StateUtil.getTempChunk(value.getLink())) != null : true){
				processChunkTax(date, chunk);
			}
			return;
		}
		if(value.getDistrict().getId() <= -1){ return; }
		long tax = 0;
		if(value.getCustomTax() > 0){
			tax = value.getCustomTax();
		}
		else{
			switch(value.getType()){
				case PRIVATE: case COMPANY: case NORMAL: tax = value.getDistrict().getChunkTax(); break;
				case MUNICIPAL: case DISTRICT: case PUBLIC: case STATEOWNED: default: return;
			}
		}
		if(tax > 0){
			Account account = null;
			Account receiver = value.getMunicipality().getAccount();
			Account state = value.getState().getAccount();
			switch(value.getType()){
				case PRIVATE:{
					account = DataManager.getAccount("player:" + value.getOwner(), true, false);
					break;
				}
				case COMPANY: //TODO
				default: return;
			}
			if(account == null || receiver == null){ return; }
			Bank bank = DataManager.getBank(account.getBankId(), true, false);
			if(account.getBalance() < tax){
				if((account.getBalance() <= 0 || bank.isNull()) && value.getDistrict().r_ONBANKRUPT.get()){
					MailUtil.send(null, RecipientType.PLAYER, value.getOwner(), COLLECTOR, "You didn't have enough money to pay for your Property at " + StateLogger.chunk(value) + ", as such, it was unclaimed.", SYSTEMMAIL);
					MailUtil.send(null, RecipientType.MUNICIPALITY, value.getMunicipality().getId(), COLLECTOR, "Owner of the Property at " + StateLogger.chunk(value) + " did not have enough money to pay the tax, following the District's settings, the property was taken from that player/company.", SYSTEMMAIL);
					//value.getDistrict().getManager() == null ? getMayor(value.getMunicipality()) : value.getDistrict().getManager().toString()
					value.setOwner(null); value.setType(ChunkType.DISTRICT);
					value.onTaxCollected(date);
					return;
				}
				else if(account.getBalance() > 0 && bank != null && value.getDistrict().r_ONBANKRUPT.get()){
					bank.processAction(Bank.Action.TRANSFER, Static.getServer(), account, account.getBalance(), value.getMunicipality().getAccount());
					MailUtil.send(null, RecipientType.PLAYER, value.getOwner(), COLLECTOR, "WARNING! You didn't have enough money to pay for your Property at " + StateLogger.chunk(value) + ", next tax collection cycle it will be unclaimed!", SYSTEMMAIL);
					MailUtil.send(null, RecipientType.MUNICIPALITY, value.getMunicipality().getId(), COLLECTOR, "Owner of the Property at " + StateLogger.chunk(value) + " did not have enough money to pay the full tax.", SYSTEMMAIL);
					//value.getDistrict().getManager() == null ? getMayor(value.getMunicipality()) : value.getDistrict().getManager().toString()
					value.onTaxCollected(date);
					return;
				}
				else if(account.getBalance() > 0 && bank != null && !value.getDistrict().r_ONBANKRUPT.get()){
					bank.processAction(Bank.Action.TRANSFER, Static.getServer(), account, account.getBalance(), value.getMunicipality().getAccount());
					MailUtil.send(null, RecipientType.PLAYER, value.getOwner(), COLLECTOR, "WARNING! You didn't have enough money to pay for your Property at " + StateLogger.chunk(value) + "!", SYSTEMMAIL);
					MailUtil.send(null, RecipientType.MUNICIPALITY, value.getMunicipality().getId(), COLLECTOR, "Owner of the Property at " + StateLogger.chunk(value) + " did not have enough money to pay the full tax.", SYSTEMMAIL);
					//value.getDistrict().getManager() == null ? getMayor(value.getMunicipality()) : value.getDistrict().getManager().toString()
					value.onTaxCollected(date);
				}
				if(bank.isNull()){
					Print.log("Tax collection for " + StateLogger.chunk(value) + " could not be completed as the owner's bank is NULL, additionally, the owner didn't have enough money to pay the tax.");
				}
				return;
			}
			else if(bank.isNull()){
				Print.log("Tax collection for " + StateLogger.chunk(value) + " could not be completed as the owner's bank is NULL.");
				return;
			}
			long statetax = tax > 1000 ? getPercentage(tax, value.getState().getChunkTaxPercentage()) : 0;
			long muntax = tax > 1000 ? tax - statetax : tax;
			bank.processAction(Bank.Action.TRANSFER, Static.getServer(), account, muntax, receiver);
			if(state != null && statetax > 0){
				bank.processAction(Bank.Action.TRANSFER, Static.getServer(), account, statetax, state);
			}
		}
		else value.onTaxCollected(date);
	}

	public static long getPercentage(long tax, byte percent){
		return percent > 0 ? (tax / 100) * percent : 0;
	}

	private void load(){
		JsonObject obj = JsonUtil.get(new File(States.getSaveDirectory(), "tax_interval.json"));
		last_interval = JsonUtil.getIfExists(obj, "last_interval", 0).longValue(); loaded = true;
	}
	
	private void save(){
		JsonObject obj = new JsonObject();
		obj.addProperty("last_interval", last_interval);
		//
		JsonUtil.write(new File(States.getSaveDirectory(), "tax_interval.json"), obj, true);
	}

	public static long getProbableSchedule(){
		return last_interval;
	}
	
	private static Calendar calendar;
	
	public static Calendar getCalendar(){
		if(calendar == null){
			calendar = Calendar.getInstance();
	        calendar.set(Calendar.HOUR_OF_DAY, 12);
	        calendar.set(Calendar.MINUTE, 0);
	        calendar.set(Calendar.SECOND, 0);
	        calendar.set(Calendar.MILLISECOND, 0);
		}
		return calendar;
	}
	
}