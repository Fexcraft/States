package net.fexcraft.mod.states.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

import net.fexcraft.lib.common.json.JsonUtil;
import net.fexcraft.lib.mc.utils.Formatter;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.states.data.capabilities.PlayerCapability;
import net.fexcraft.mod.states.data.capabilities.StatesCapabilities;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;

public class MessageSender {
    
    public static Receiver RECEIVER;
    
    public static void to(ICommandSender receiver, String message){
        if(receiver == null){ Print.log("RECEIVER NULL || " + message); }
        receiver.sendMessage(new TextComponentString(Formatter.format(message)));
    }
    
    public static void as(EntityPlayer sender, String message){ as(sender, message, true); }
    
    public static void as(EntityPlayer sender, String message, boolean webhook){
    	PlayerCapability cap = null;
    	String name = sender == null ? "&9#&8] &2" + Config.WEBHOOK_BROADCASTER_NAME :
    		"&" + (StateUtil.isAdmin(sender) ? "4" : "6") + "#&8] " + sender.getCapability(StatesCapabilities.PLAYER, null).getFormattedNickname(); 
        Static.getServer().getPlayerList().sendMessage(new TextComponentString(Formatter.format(name + "&0: &7" + message)));
        if(webhook) toWebhook(cap, message);
    }
    
    public static void fromDiscord(JsonObject obj){
        String name = "&5#&8] &2" + obj.get("username").getAsString();
        ITextComponent text = null;
        if(obj.get("content").isJsonArray()){
            text = new TextComponentString(Formatter.format(name + "&0: " + obj.get("content").getAsJsonArray().get(0).getAsString()));
            text.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, obj.get("content").getAsJsonArray().get(1).getAsString()));
        }
        else{
            text = new TextComponentString(Formatter.format(name + "&0: &7" + obj.get("content").getAsString()));
        }
        Static.getServer().getPlayerList().sendMessage(text);
    }
    
    public static void toWebhook(PlayerCapability sender, String message){
        if(RECEIVER == null){ return; }
        try{
            URL url = new URL(Config.WEBHOOK);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setDoOutput(true);
            //
            JsonObject obj = new JsonObject();
            obj.addProperty("username", sender == null ? Config.WEBHOOK_BROADCASTER_NAME : sender.getWebhookNickname());
            obj.addProperty("content", Formatter.clear(message));
            if(sender != null){
                obj.addProperty("avatar_url", "https://crafatar.com/avatars/" + sender.getEntityPlayer().getGameProfile().getId().toString());
            }
            else{
                obj.addProperty("avatar_url", Config.WEBHOOK_ICON);
            }
            //
            //OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream(), "utf-8");
            //wr.write(obj.toString()); wr.flush(); wr.close();
            JsonWriter writer = JsonUtil.getGson().newJsonWriter(new OutputStreamWriter(connection.getOutputStream(), "utf-8"));
            writer.jsonValue(obj.toString());
            writer.flush(); writer.close();
            //
            if(true){//Static.dev()){
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String input; StringBuffer response = new StringBuffer();
                while((input = in.readLine()) != null){ response.append(input); }
                in.close(); input = response.toString();
                if(!input.equals("")){
                    if(sender == null){
                        Print.log("[States-Webhook]: " + input);
                    }
                    else{
                        Print.chat(sender.getEntityPlayer(), input);
                    }
                }
            }
            //
            connection.disconnect();
            return;
        }
        catch(Exception e){
            Print.log(e.getMessage());
            //e.printStackTrace();
        }
    }
    
    public static class Receiver extends Thread {
        
        private boolean running = true;
        
        @Override
        public void run(){
            Print.log("[States-Webhook] Starting Message Listener on port " + Config.BOT_PORT);
            toWebhook(null, "Starting Server Message Receiver...");
            try{
                ServerSocket socket = new ServerSocket(Config.BOT_PORT);
                while(running){
                    try{
                        Socket client = socket.accept();
                        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                        //
                        String input;
                        StringBuffer response = new StringBuffer();
                        while((input = in.readLine()) != null){ response.append(input); }
                        in.close(); client.close();
                        JsonElement obj = JsonUtil.getFromString(response.toString());
                        if(obj != null && valid(obj)){
                            MessageSender.fromDiscord(obj.getAsJsonObject());
                        }
                        else{
                            Print.debug("Received invalid message: " + obj);
                        }
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                }
                socket.close();
            }
            catch(IOException e){
                e.printStackTrace();
            }
            Print.log("[States-Webhook] Stopping Message Listener on port " + Config.BOT_PORT);
            toWebhook(null, "Server Message Receiver stopped.");
        }
        
        private boolean valid(JsonElement obj){
            return obj.isJsonObject() && obj.getAsJsonObject().has("token") && obj.getAsJsonObject().get("token").getAsString().equals(Config.BOT_KEY);
        }
        
        public void halt(){
            toWebhook(null, "Stopping Server Message Receiver...");
            running = false;
        }
    }
    
}