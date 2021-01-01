package net.fexcraft.mod.states.util;

import net.minecraftforge.fml.common.ICrashCallable;

public class CrashHook implements ICrashCallable {

	@Override
	public String call() throws Exception{
        MessageSender.toWebhook(null, "Server crashed! Check log for details.");
		return MessageSender.RECEIVER == null ? "No webhook active." : "Notified webhook about crash.";
	}

	@Override
	public String getLabel(){
		return "States Crash Hook";
	}

}
