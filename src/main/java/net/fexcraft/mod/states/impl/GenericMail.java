package net.fexcraft.mod.states.impl;

import java.io.File;

import com.google.gson.JsonObject;

import net.fexcraft.mod.lib.util.common.Formatter;
import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.lib.util.common.Static;
import net.fexcraft.mod.lib.util.json.JsonUtil;
import net.fexcraft.mod.lib.util.math.Time;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.Mail;
import net.fexcraft.mod.states.api.MailType;
import net.fexcraft.mod.states.cmds.MailCmd;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;

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
			file = new File(States.getSaveDirectory(), "mails/" + type + "/" + receiver + "/" + id + ".read");
			if(!file.exists()){
				throw new Exception("Mail not found! (" + type + ", " + receiver + ", " + id + ")");
			}
		}
		JsonObject object = JsonUtil.get(file);
		this.type = MailType.valueOf(JsonUtil.getIfExists(object, "type", MailType.SYSTEM.name()));
		sender = JsonUtil.getIfExists(object, "sender", States.DEF_UUID);
		message = JsonUtil.getIfExists(object, "message", "no message content");
		data = object.has("data") ? object.get("data").getAsJsonObject() : null;
		read = JsonUtil.getIfExists(object, "read", false);
	}
	
	/** Create new. */
	public GenericMail(String rectype, String receiver, String sender, String message, MailType type, JsonObject data){
		this.id = Time.getDate() + "_" + Static.random.nextInt(10000);
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
		Print.chat(sender, "&8[&6" + type.name() + "&8]&7 From: " + MailCmd.getSender(this.sender) + " (" + id + ")");
		Print.chat(sender, message);
		if(this.type == MailType.INVITE){
			if(expired() || data.has("status")){
				Print.chat(sender, "&8&lInvite Expired.");
			}
			else{
				TextComponentString text = new TextComponentString(Formatter.format("&a&l[ACCEPT] "));
				text.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mail accept " + id + " " + rectype + " " + recipient));
				TextComponentString text2 = new TextComponentString(Formatter.format(" &c&l[DENY]"));
				text2.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mail deny " + id + " " + rectype + " " + recipient));
				sender.sendMessage(text.appendSibling(text2));
			}
		}
		this.read = true;
		this.archive();
	}
	
	public boolean expired(){
		long at = data.get("at").getAsLong();
		long valid = data.get("valid").getAsLong();
		return at + valid < Time.getDate();
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
		if(data != null){
			object.add("data", data);
		}
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
