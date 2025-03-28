package net.fexcraft.mod.states.data.root;

import net.fexcraft.mod.fsmm.data.Account;

public interface AccountHolder {
	
	public void unload();
	
	public Account getAccount();
	
}