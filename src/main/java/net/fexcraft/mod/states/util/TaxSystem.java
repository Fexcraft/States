package net.fexcraft.mod.states.util;

import java.io.File;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.TimerTask;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;

import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.api.Bank;
import net.fexcraft.mod.fsmm.util.AccountManager;
import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.lib.util.common.Static;
import net.fexcraft.mod.lib.util.json.JsonUtil;
import net.fexcraft.mod.lib.util.math.Time;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.api.ChunkPos;
import net.fexcraft.mod.states.api.Mail;
import net.fexcraft.mod.states.api.MailType;
import net.fexcraft.mod.states.api.Municipality;
import net.fexcraft.mod.states.impl.GenericMail;

public class TaxSystem extends TimerTask {
	
	private long last_interval;

	@Override
	public void run(){
		try{
			load();
			long date = Time.getDate();
			if(last_interval + Config.TAX_INTERVAL > date){
				Print.log("Tried to process TAX data, but it is not time for that yet.");
				Print.log("LTAX: " + last_interval + " + INTERVAL: " + Config.TAX_INTERVAL + " > NOW: " + date);
				return;
			}
			Sender.sendAs(null, "Starting regular tax collection.");
			StateLogger.log(StateLogger.LoggerType.CHUNK, "Collecting tax from loaded chunks...");
			ImmutableMap<ChunkPos, Chunk> map = ImmutableMap.copyOf(States.CHUNKS);
			for(Chunk chunk  : map.values()){
				this.processChunkTax(date, chunk);
			}
			StateLogger.log(StateLogger.LoggerType.MUNICIPALITY, "Collecting tax from loaded municipalities...");
			ImmutableMap<Integer, Municipality> mapm = ImmutableMap.copyOf(States.MUNICIPALITIES);
			for(Municipality mun : mapm.values()){
				this.processMunicipalityTax(date, mun);
			}
			StateLogger.log(StateLogger.LoggerType.MUNICIPALITY, "Collecting tax from force-loaded chunks...");
			ImmutableMap<Integer, Collection<ChunkPos>> maplc = ImmutableMap.copyOf(States.LOADED_CHUNKS);
			for(Entry<Integer, Collection<ChunkPos>> entry : maplc.entrySet()){
				this.processLoadedChunkTax(date, entry.getKey(), entry.getValue());
			}
			Sender.sendAs(null, "Finished collecting tax.");
			last_interval = date; save();
		}
		catch(Exception e){
			Sender.sendAs(null, "TAX COLLECTION ERRORED!");
			Print.log("Error while collecting Tax!");
			StateLogger.log("all", "An error occured while collecting tax, further collection is halted for this interval.");
			e.printStackTrace();
		}
	}

	private void processLoadedChunkTax(long date, Integer key, Collection<ChunkPos> value){
		//if(value.lastTaxCollection() + Config.TAX_INTERVAL > date){ return; }
	}

	private void processMunicipalityTax(long date, Municipality value){
		//if(value.lastTaxCollection() + Config.TAX_INTERVAL > date){ return; }
	}

	private void processChunkTax(long date, Chunk value){
		if(value.lastTaxCollection() + Config.TAX_INTERVAL > date){ return; }
		if(value.getLink() != null){
			Chunk chunk = StateUtil.getChunk(value.getLink());
			if(chunk != null){
				//Chunk exists, it it will either be polled for tax or has been already.
				return;
			}
			else{
				chunk = StateUtil.getTempChunk(value.getLink());
				if(chunk == null){ return; }//In case anything is broken.
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
			boolean wasloaded = true;
			Account account = null;
			Account receiver = value.getMunicipality().getAccount();
			Account state = value.getState().getAccount();
			switch(value.getType()){
				case PRIVATE:{
					account = AccountManager.INSTANCE.getAccount("player", value.getOwner());
					if(account == null){
						wasloaded = false;
						account = AccountManager.INSTANCE.getAccount("player", value.getOwner(), true);
					}
					break;
				}
				case COMPANY: //TODO
				default: return;
			}
			if(account == null || receiver == null){ return; }
			boolean bankloaded = true;
			Bank bank = AccountManager.INSTANCE.getBank(account.getBankId());
			if(bank == null){
				bank = AccountManager.INSTANCE.getBank(account.getBankId(), true);
				bankloaded = false;
			}
			if(account.getBalance() < tax){
				if((account.getBalance() <= 0 || bank == null) && value.getDistrict().unclaimIfBankrupt()){
					Mail mail = new GenericMail("player", value.getOwner(), "TaxCollector",
						"You didn't have enough money to pay for your Property at " + StateLogger.chunk(value) + ", as such, it was unclaimed.", MailType.SYSTEM, null);
					mail.save();
					mail = new GenericMail("player", value.getDistrict().getManager() == null ? value.getMunicipality().getMayor().toString() : value.getDistrict().getManager().toString(), "TaxCollector",
						"Owner of the Property at " + StateLogger.chunk(value) + " did not have enough money to pay the tax, following the District's settings, the property was taken from that player/company.", MailType.SYSTEM, null);
					mail.save();
					value.setOwner(null);
					value.onTaxCollected(date);
					return;
				}
				else if(account.getBalance() > 0 && bank != null && value.getDistrict().unclaimIfBankrupt()){
					bank.processTransfer(Static.getServer(), account, tax, value.getMunicipality().getAccount());
					Mail mail = new GenericMail("player", value.getOwner(), "TaxCollector",
						"WARNING! You didn't have enough money to pay for your Property at " + StateLogger.chunk(value) + ", next tax collection cycle it will be unclaimed!", MailType.SYSTEM, null);
					mail.save();
					mail = new GenericMail("player", value.getDistrict().getManager() == null ? value.getMunicipality().getMayor().toString() : value.getDistrict().getManager().toString(), "TaxCollector",
						"Owner of the Property at " + StateLogger.chunk(value) + " did not have enough money to pay the full tax.", MailType.SYSTEM, null);
					mail.save();
					value.onTaxCollected(date);
					if(!bankloaded){ AccountManager.INSTANCE.unloadBank(bank); }
					if(!wasloaded){ AccountManager.INSTANCE.unloadAccount(account); }
					return;
				}
				else if(account.getBalance() > 0 && bank != null && !value.getDistrict().unclaimIfBankrupt()){
					bank.processTransfer(Static.getServer(), account, tax, value.getMunicipality().getAccount());
					if(!bankloaded){ AccountManager.INSTANCE.unloadBank(bank); }
					if(!wasloaded){ AccountManager.INSTANCE.unloadAccount(account); }
					Mail mail = new GenericMail("player", value.getOwner(), "TaxCollector",
						"WARNING! You didn't have enough money to pay for your Property at " + StateLogger.chunk(value) + "!", MailType.SYSTEM, null);
					mail.save();
					mail = new GenericMail("player", value.getDistrict().getManager() == null ? value.getMunicipality().getMayor().toString() : value.getDistrict().getManager().toString(), "TaxCollector",
						"Owner of the Property at " + StateLogger.chunk(value) + " did not have enough money to pay the full tax.", MailType.SYSTEM, null);
					mail.save();
					value.onTaxCollected(date);
				}
				if(bank == null){
					StateLogger.log(StateLogger.LoggerType.CHUNK, "Tax collection for " + StateLogger.chunk(value) + " could not be completed as the owner's bank is NULL, additionally, the owner didn't have enough money to pay the tax.");
				}
				return;
			}
			if(bank == null){
				StateLogger.log(StateLogger.LoggerType.CHUNK, "Tax collection for " + StateLogger.chunk(value) + " could not be completed as the owner's bank is NULL.");
				return;
			}
			long statetax = tax > 1000 ? getPercentage(tax, value.getState().getChunkTaxPercentage()) : 0;
			long muntax = tax > 1000 ? tax - statetax : tax;
			bank.processTransfer(Static.getServer(), account, muntax, receiver);
			if(state != null && statetax > 0){
				bank.processTransfer(Static.getServer(), account, statetax, state);
			}
			if(!wasloaded){ AccountManager.INSTANCE.unloadAccount(account); }
			if(!bankloaded){ AccountManager.INSTANCE.unloadBank(bank); }
		}
		value.onTaxCollected(date);
	}

	private long getPercentage(long tax, byte percent){
		return percent > 0 ? (tax / 100) * percent : 0;
	}

	private void load(){
		JsonObject obj = JsonUtil.get(new File(States.getSaveDirectory(), "tax_interval.json"));
		last_interval = JsonUtil.getIfExists(obj, "last_interval", 0).longValue();
	}
	
	private void save(){
		JsonObject obj = new JsonObject();
		obj.addProperty("last_interval", last_interval);
		//
		JsonUtil.write(new File(States.getSaveDirectory(), "tax_interval.json"), obj, true);
	}
	
}