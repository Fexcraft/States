package net.fexcraft.mod.states.events;

import java.util.Map.Entry;

import org.apache.commons.lang3.math.NumberUtils;

import net.fexcraft.mod.fsmm.api.AccountPermission;
import net.fexcraft.mod.fsmm.events.ATMEvent;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.data.Municipality;
import net.fexcraft.mod.states.data.State;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class FSMMEvents {

	@SubscribeEvent()
	public static void onGatherAccounts(ATMEvent.GatherAccounts event){
		event.getAccountsList().add(new AccountPermission(StateUtil.getState(0).getAccount(), true, true, true, false));
		event.getAccountsList().add(new AccountPermission(StateUtil.getState(-1).getAccount(), true, true, true, false));
		event.getAccountsList().add(new AccountPermission(StateUtil.getMunicipality(0).getAccount(), true, true, true, false));
	}
	
	@SubscribeEvent
	public static void onSearchAccounts(ATMEvent.SearchAccounts event){
		if(!event.getSearchedType().equals("state") && !event.getSearchedType().equals("municipality")){
			return;
		}
		boolean state = event.getSearchedType().equals("state");
		if(NumberUtils.isCreatable(event.getSearchedId())){
			int id = Integer.parseInt(event.getSearchedId());
			if(state){
				if(States.STATES.containsKey(id)){
					event.getAccountsMap().put("state:" + id, new AccountPermission(StateUtil.getState(id).getAccount()));
				}
				else if(State.getStateFile(id).exists()){
					event.getAccountsMap().put("state:" + id, new AccountPermission("state:" + id));
				}
			}
			else{
				if(States.MUNICIPALITIES.containsKey(id)){
					event.getAccountsMap().put("municipality:" + id, new AccountPermission(StateUtil.getMunicipality(id).getAccount()));
				}
				else if(Municipality.getMunicipalityFile(id).exists()){
					event.getAccountsMap().put("municipality:" + id, new AccountPermission("municipality:" + id));
				}
			}
		}
		else{
			for(Entry<Integer, String> entry : StateUtil.NAMECACHE_STATE.entrySet()){
				if(entry.getValue().toLowerCase().contains(event.getSearchedId())){
					event.getAccountsMap().put("state:" + entry.getKey(), new AccountPermission("state:" + entry.getKey()));
				}
			}
			for(Entry<Integer, String> entry : StateUtil.NAMECACHE_MUNICIPALITY.entrySet()){
				if(entry.getValue().toLowerCase().contains(event.getSearchedId())){
					event.getAccountsMap().put("municipality:" + entry.getKey(), new AccountPermission("municipality:" + entry.getKey()));
				}
			}
		}
	}

}
