package net.fexcraft.mod.states.impl;

import java.io.File;

import com.google.gson.JsonObject;

import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.lib.util.json.JsonUtil;
import net.fexcraft.mod.lib.util.math.Time;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.Mail;
import net.fexcraft.mod.states.api.MailType;
import net.minecraft.command.ICommandSender;

public class GenericMail implements Mail {
	
	private String id, rectype, recipient, sender, message;
	private JsonObject data;
	private MailType type;
	private boolean read;
	
	/** Load from storage. 
	 * @throws Exception if mail file not found*/
	public GenericMail(String id, String type, String receiver) throws Exception {
		this.id = id;
		rectype = type;
		recipient = receiver;
		File file = new File(States.getSaveDirectory(), "mails/" + type + "/" + receiver + "/" + id + ".unread");
		if(!file.exists()){
			throw new Exception("Mail not found!");
		}
		JsonObject object = JsonUtil.get(file);
		this.type = MailType.valueOf(JsonUtil.getIfExists(object, "mailtype", MailType.SYSTEM.name()));
		sender = JsonUtil.getIfExists(object, "sender", States.DEF_UUID);
		message = JsonUtil.getIfExists(object, "message", "no message content");
		data = object.has("data") ? object.get("data").getAsJsonObject() : null;
		read = JsonUtil.getIfExists(object, "read", false);
	}
	
	/** Create new. */
	public GenericMail(String rectype, String receiver, String sender, String message, MailType type, JsonObject data){
		this.id = Time.getDate() + "";
		this.rectype = rectype;
		this.recipient = receiver;
		this.sender = sender;
		this.message = message;
		this.type = type;
		this.data = data;
		this.read = false;
	}

	@Override
	public String getId(){
		return id;
	}

	@Override
	public MailType getType(){
		return type;
	}

	@Override
	public String getRecipientType(){
		return rectype;
	}

	@Override
	public String getRecipient(){
		return recipient;
	}

	@Override
	public String getMessage(){
		return message;
	}

	@Override
	public String getSender(){
		return sender;
	}

	@Override
	public JsonObject getData(){
		return data;
	}

	@Override
	public void read(ICommandSender sender){
		//TODO
		Print.chat(sender, message);
		Print.chat(sender, data);
	}
	
	@Override
	public boolean isRead(){
		return read;
	}

	@Override
	public JsonObject toJsonObject(){
		JsonObject object = new JsonObject();
		object.addProperty("id", id);
		object.addProperty("type", type.name());
		object.addProperty("rec-type", rectype);
		object.addProperty("receiver", recipient);
		object.addProperty("sender", sender);
		object.addProperty("message", message);
		object.addProperty("read", read);
		object.add("data", data);
		return object;
	}

	@Override
	public void archive(){
		File file = new File(States.getSaveDirectory(), "mails/" + rectype + "/" + recipient + "/" + id + ".unread");
		if(file.exists()){
			file.delete();
		}
		file = new File(States.getSaveDirectory(), "mails/" + rectype + "/" + recipient + "/" + id + ".read");
		JsonUtil.write(file, toJsonObject(), true);
	}
	
	@Override
	public void save(){
		File file = new File(States.getSaveDirectory(), "mails/" + rectype + "/" + recipient + "/" + id + ".unread");
		JsonUtil.write(file, toJsonObject(), true);
	}

}
