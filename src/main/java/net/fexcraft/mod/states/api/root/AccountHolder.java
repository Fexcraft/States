package net.fexcraft.mod.states.api.root;

import javax.annotation.Nullable;

import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.api.Bank;

public interface AccountHolder {
	
	public void unload();
	
	public Account getAccount();
	
	public @Nullable Bank getBank();
	
}