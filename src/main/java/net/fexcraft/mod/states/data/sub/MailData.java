package net.fexcraft.mod.states.data.sub;

import com.google.gson.JsonObject;

import net.fexcraft.mod.states.data.root.Loadable;
import net.minecraft.util.math.BlockPos;

public class MailData implements Loadable {

	public static final String NOMAILBOX = "no_mailbox";
	private BlockPos mailbox;

	@Override
	public void load(JsonObject obj){
		mailbox = obj.has("mailbox") ? BlockPos.fromLong(obj.get("mailbox").getAsLong()) : null;
	}

	@Override
	public void save(JsonObject obj){
		if(mailbox != null) obj.addProperty("mailbox", mailbox.toLong());
	}
	
	public BlockPos get(){
		return mailbox;
	}
	
	public void set(BlockPos pos){
		mailbox = pos;
	}

	public boolean exists(){
		return mailbox != null;
	}

	public boolean missing(){
		return mailbox == null;
	}

	public void reset(){
		set(null);
	}

	public String asString(){
		return mailbox == null ? NOMAILBOX : mailbox.getX() + ", " + mailbox.getY() + ", " + mailbox.getZ();
	}

}
