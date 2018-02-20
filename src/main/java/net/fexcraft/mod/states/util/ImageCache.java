package net.fexcraft.mod.states.util;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import javax.imageio.ImageIO;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.lib.util.common.Static;
import net.fexcraft.mod.lib.util.json.JsonUtil;
import net.fexcraft.mod.lib.util.math.Time;
import net.fexcraft.mod.states.States;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class ImageCache {
	
	private static final int IMGSIZE = 512;
	public static final String[] TYPES = new String[]{"surface", "surface_states", "states", "surface_municipalities", "municipalities", "surface_districts", "districts", "commercial", "chunk_types", "biomemap"};
	private static final Queue<Object[]> QUEUE = new LinkedList<>();
	
	@Mod.EventBusSubscriber
	public static class TickHandler {
		
		@SubscribeEvent
		public static void onTick(TickEvent.ServerTickEvent event) throws Exception {
			if(event.phase == Phase.END){
				for(int i = 0; i < Config.MAP_UPDATES_PER_TICK; i++){
					Object[] objs = QUEUE.poll();
					if(objs != null){
						updateImage((World)objs[0], (Chunk)objs[1], (String)objs[2], (String)objs[3]);
					}
				}
			}
		}
		
	}
	
	public static void update(World world, Chunk chunk, String event, String type){
		if(Config.MAP_UPDATES_PER_TICK == 0){
			return;
		}
		if(type.equals("all")){
			for(int i = 0; i < TYPES.length; i++){
				QUEUE.add(new Object[]{world, chunk, event, TYPES[i]});
			}
			return;
		}
		QUEUE.add(new Object[]{world, chunk, event, type});
		return;
	}
	
	private static final void updateImage(World world, Chunk chunk, String event, String type){
		BufferedImage img = getImage(chunk.x, chunk.z, type);
		int rx = (int)Math.floor(chunk.x / 32.0), rz = (int)Math.floor(chunk.z / 32.0);
		int x = (chunk.x * 16) - (rx * 512), z = (chunk.z * 16) - (rz * 512);
		if(type.contains("surface")){
			for(int i = 0; i < 16; i++){
				for(int j = 0; j < 16; j++){
					BlockPos pos = getPos(world, i + (chunk.x * 16), j + (chunk.z * 16));
					IBlockState state = world.getBlockState(pos);
					img.setRGB(x + i, z + j, new Color(state.getMapColor(world, pos).colorValue).getRGB());
				}
			}
		}
		net.fexcraft.mod.states.api.Chunk st_chunk = StateUtil.getChunk(chunk.x, chunk.z);
		if(type.contains("surface") && !type.equals("surface")){
			for(int i = -1; i < 2; i++){
				for(int j = -1; j < 2; j++){
					if(i == 0 && j == 0){
						continue;
					}
					net.fexcraft.mod.states.api.Chunk ck = StateUtil.getTempChunk(chunk.x + i, chunk.z + j);
					boolean yes = false;
					switch(type){
						case "surface_states":{
							yes = ck.getDistrict().getMunicipality().getState().getId() != st_chunk.getDistrict().getMunicipality().getState().getId();
							break;
						}
						case "surface_municipalities":{
							yes = ck.getDistrict().getMunicipality().getId() != st_chunk.getDistrict().getMunicipality().getId();
							break;
						}
						case "surface_districts":{
							yes = ck.getDistrict().getId() != st_chunk.getDistrict().getId();
							break;
						}
						default: break;
					}
					if(yes){
						//paintBorder(img, i == 0 ? j == -1 ? "left" : "right" : i == 1 ? "down" : "up", Integer.toHexString(Color.BLUE.getRGB()).substring(2), x, z);
						paintBorder(img, sideCorner(i, j), Integer.toHexString(Color.CYAN.getRGB()).substring(2), x, z);
					}
					else continue;
				}
			}
		}
		saveImage(img, chunk.x, chunk.z, type);
		return;
	}
	
	private static final String sideCorner(int x, int z){
		switch(x){
			case -1:{
				switch(z){
					case -1: return "up_left";
					case  0: return "up";
					case  1: return "up_right";
				}
				break;
			}
			case  0:{
				switch(z){
					case -1: return "left";
					case  0: return "center";
					case  1: return "right";
				}
				break;
			}
			case  1:{
				switch(z){
					case -1: return "down_left";
					case  0: return "down";
					case  1: return "down_right";
				}
				break;
			}
		}
		return "null";
	}
	
	private static void paintBorder(BufferedImage image, String side, String color_hex, int... coords){
		if(!color_hex.contains("#")){
			color_hex = "#" + color_hex;
		}
		Color color = Color.decode(color_hex);
		int i, j, k, l;
		switch(side){
			case "left" :{ i = 0; j = 16; k = 0; l = 4; break; }
			case "right":{ i = 0; j = 16; k = 12; l = 16; break; }
			case "up"   :{ i = 0; j = 4; k = 0; l = 16; break; }
			case "down" :{ i = 12; j = 16; k = 0; l = 16; break; }
			//
			/*case "up_left" :{ i = 0; j = 4; k = 0; l = 4; }
			case "up_right":{ i = 0; j = 4; k = 12; l = 16; }
			case "down_left" :{ i = 12; j = 16; k = 0; l = 4; }
			case "down_right":{ i = 12; j = 16; k = 12; l = 16; }*/
			//
			case "center":{ i = 4; j = 8; k = 4; l = 8; break; }
			default:{ i = j = k = l = 0; break; }
		}
		for(int m = i; m < j; m++){
			for(int n = k; n < l; n++){
				image.setRGB(coords[0] + m, coords[1] + n, color.getRGB());
			}
		}
		return;
	}

	public static final String getChunkRegion(int x, int z){
		return (int)Math.floor(x / 32.0) + "_" + (int)Math.floor(z / 32.0);
	}
	
	public static final BufferedImage getImage(int x, int z, String type){
		File file = new File(States.getSaveDirectory(), "image_cache/" + type + "/" + getChunkRegion(x, z) + ".png");
		if(!file.exists()){
			return new BufferedImage(IMGSIZE, IMGSIZE, BufferedImage.TYPE_INT_ARGB);
		}
		else{
			try{
				return ImageIO.read(file);
			}
			catch(IOException e){
				Print.debug(file.toPath().toString());
				e.printStackTrace();
				return new BufferedImage(IMGSIZE, IMGSIZE, BufferedImage.TYPE_INT_ARGB);
			}
		}
	}
	
	public static final void saveImage(BufferedImage image, int x, int z, String type){
		File file = new File(States.getSaveDirectory(), "image_cache/" + type + "/" + getChunkRegion(x, z) + ".png");
		if(!file.getParentFile().exists()){ file.getParentFile().mkdirs(); }
		try{ ImageIO.write(image, "png", file); } catch(IOException e){ e.printStackTrace(); }
	}

	private static final BlockPos getPos(World world, int x, int z){
		for(int i = 255; i > 0; i--){
			BlockPos pos = new BlockPos(x, i, z);
			if(world.getBlockState(pos).getBlock() != Blocks.AIR){
				return pos;
			}
		}
		return new BlockPos(x, 0, z);
	}

	public static void saveQueue(){
		JsonArray array = new JsonArray();
		Object[] entry = null;
		while((entry = QUEUE.poll()) != null){
			JsonObject obj = new JsonObject();
			obj.addProperty("dimension", ((World)entry[0]).provider.getDimension());
			Chunk chunk = (Chunk) entry[1];
			obj.addProperty("chunk", chunk.x + ":" + chunk.z);
			obj.addProperty("event", (String)entry[2]);
			obj.addProperty("type", (String)entry[3]);
			array.add(obj);
		}
		JsonObject obj = new JsonObject();
		obj.addProperty("last_save", Time.getDate());
		obj.add("queue", array);
		JsonUtil.write(new File(States.getSaveDirectory(), "image_cache/queue.json"), obj);
	}

	public static void loadQueue(){
		JsonObject obj = JsonUtil.get(new File(States.getSaveDirectory(), "image_cache/queue.json"));
		if(obj.has("queue")){
			JsonArray array = obj.get("queue").getAsJsonArray();
			array.forEach(elm -> {
				JsonObject object = elm.getAsJsonObject();
				World world = Static.getServer().getWorld(object.get("dimension").getAsInt());
				String[] ckarr = object.get("chunk").getAsString().split(":");
				Chunk chunk = world.getChunkFromChunkCoords(Integer.parseInt(ckarr[0]), Integer.parseInt(ckarr[1]));
				String event = object.get("event").getAsString();
				String type = object.get("type").getAsString();
				QUEUE.add(new Object[]{world, chunk, event, type});
			});
		}
		
	}
	
}