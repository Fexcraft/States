package net.fexcraft.mod.states.events;

import net.fexcraft.mod.fsmm.api.AccountPermission;
import net.fexcraft.mod.fsmm.events.ATMEvent;
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

}
