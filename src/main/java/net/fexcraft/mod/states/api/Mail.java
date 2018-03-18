package net.fexcraft.mod.states.api;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;

import net.minecraft.command.ICommandSender;

public interface Mail {
	
	public String getId();
	
	public MailType getType();
	
	public String getRecipientType();
	
	public String getRecipient();
	
	public String getMessage();
	
	public String getSender();
	
	@Nullable
	public JsonObject getData();
	
	public void read(ICommandSender sender);
	
	public JsonObject toJsonObject();
	
	public boolean isRead();
	
	public void archive();
	
	public void save();

}
