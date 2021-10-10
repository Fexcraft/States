package net.fexcraft.mod.states.data;

import net.fexcraft.app.json.JsonMap;
import net.minecraft.util.math.BlockPos;

public class MailData implements Saveable {

	public static final String NOMAILBOX = "no_mailbox";
	private BlockPos mailbox;

	@Override
	public void load(JsonMap map){
		mailbox = map.has("mailbox") ? BlockPos.fromLong(map.get("mailbox").long_value()) : null;
	}

	@Override
	public void save(JsonMap map){
		if(mailbox != null) map.add("mailbox", mailbox.toLong());
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
