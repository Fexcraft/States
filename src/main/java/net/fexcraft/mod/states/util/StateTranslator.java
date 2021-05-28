package net.fexcraft.mod.states.util;

import net.fexcraft.lib.mc.network.PacketHandler;
import net.fexcraft.lib.mc.network.packet.PacketNBTTagCompound;
import net.fexcraft.lib.mc.utils.Print;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

/**
 * 
 * @author Ferdinand Calo' (FEX___96)
 *
 */
public class StateTranslator {

	/** Server Side */
	public static NBTTagCompound wrap(String msg, Object... args){
		if(msg == null) return null;
		NBTTagCompound compound = new NBTTagCompound();
		compound.setString("target_listener", "states:gui");
		compound.setString("task", "chat_msg");
		compound.setString("message", msg);
		compound.setInteger("args", args.length);
		for(int i = 0; i < args.length; i++){
			compound.setString("arg" + i, args[i] + "");
		}
		return compound;
	}

	/** Client Side */
	public static String unwrap(NBTTagCompound compound){
		String message = compound.getString("message");
		int args = compound.getInteger("args");
		String[] arguments = new String[args];
		for(int i = 0; i < args; i++){
			arguments[i] = compound.getString("arg" + i);
		}
		return translate(message, arguments);
	}
	
	/** Server Side */
	public static void send(ICommandSender sender, String msg, Object... args){
		if(sender instanceof EntityPlayer) return;
		send((EntityPlayer)sender, msg, args);
	}

	/** Server Side */
	public static void send(EntityPlayer player, String msg, Object... args){
		send(player, wrap(msg, args));
	}

	/** Server Side */
	public static void send(EntityPlayer player, NBTTagCompound wrapped){
		PacketHandler.getInstance().sendTo(new PacketNBTTagCompound(wrapped), (EntityPlayerMP)player);
	}

	/** Client Side */
	public static void chat(EntityPlayer player, NBTTagCompound compound){
		Print.chat(player, unwrap(compound));
	}

	/** Client Side */
	public static String translate(String key){
		return net.minecraft.client.resources.I18n.format(key);
	}

	/** Client Side */
	public static String translate(String key, String... format){
		String str = translate(key);
		for(int i = 0; i < format.length; i++){
			str = str.replace("$" + i, format[i]);
		}
		return str;
	}

}
