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
import net.fexcraft.mod.states.api.Chunk;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class ImageCache {
	
	private static final int IMGSIZE = 512;
	public static final String[] TYPES = new String[]{"surface", "surface_states", "states", "surface_municipalities", "municipalities", "surface_districts", "districts", "surface_commercial", "chunk_types"};
	
	private static BufferedImage NOIMG;
	private static final Queue<Object[]> QUEUE = new LinkedList<>();
	
	@Mod.EventBusSubscriber
	public static class TickHandler {
		
		@SubscribeEvent
		public static void onTick(TickEvent.ServerTickEvent event) throws Exception {
			if(event.phase == Phase.END){
				for(int i = 0; i < Config.MAP_UPDATES_PER_TICK; i++){
					Object[] objs = QUEUE.poll();
					if(objs != null){
						updateImage((World)objs[0], (net.minecraft.world.chunk.Chunk)objs[1], (String)objs[2], (String)objs[3]);
					}
				}
			}
		}
		
	}
	
	public static void update(World world, net.minecraft.world.chunk.Chunk chunk, String event, String type){
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
	
	private static final void updateImage(World world, net.minecraft.world.chunk.Chunk chunk, String event, String type){
		BufferedImage img = getImage(chunk.x, chunk.z, type, false);
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
		Chunk st_chunk = StateUtil.getChunk(chunk.x, chunk.z);
		String color = getColor(type, st_chunk);//"#afafaf";
		if(type.contains("surface") && !type.equals("surface")){
			for(int k = 0; k < 16; k++){
				for(int l = 0; l < 16; l++){
					Color cor = Color.decode(color);
					Color col = new Color(img.getRGB(x + k, z + l));
					Color clr = new Color((cor.getRed() + col.getRed()) / 2, (cor.getGreen() + col.getGreen()) / 2, (cor.getBlue() + col.getBlue()) / 2);
					img.setRGB(x + k, z + l, clr.getRGB());
				}
			}
			for(int i = -1; i < 2; i++){
				for(int j = -1; j < 2; j++){
					if(i == 0 && j == 0){
						continue;
					}
					if(borders(type, StateUtil.getTempChunk(chunk.x + i, chunk.z + j), st_chunk)){
						//paintBorder(img, i == 0 ? j == -1 ? "left" : "right" : i == 1 ? "down" : "up", Integer.toHexString(Color.BLUE.getRGB()).substring(2), x, z);
						paintBorder(img, sideCorner(i, j), 4, color, x, z);
					}
					else continue;
				}
			}
		}
		else if(type.equals("states") || type.equals("municipalities") || type.equals("districts")){
			Color clr = Color.decode(color);
			for(int m = 0; m < 16; m++){
				for(int n = 0; n < 16; n++){
					img.setRGB(x + m, z + n, clr.getRGB());
				}
			}
			for(int i = -1; i < 2; i++){
				for(int j = -1; j < 2; j++){
					if(i == 0 && j == 0){
						continue;
					}
					if(borders(type, StateUtil.getTempChunk(chunk.x + i, chunk.z + j), st_chunk)){
						paintBorder(img, sideCorner(i, j), 2, "#000000", x, z);
					}
					else continue;
				}
			}
			//TODO markers
		}
		else if(type.equals("chunk_types")){
			Color clr = Color.decode(color);
			for(int m = 0; m < 16; m++){
				for(int n = 0; n < 16; n++){
					img.setRGB(x + m, z + n, clr.getRGB());
				}
			}
			for(int i = -1; i < 2; i++){
				for(int j = -1; j < 2; j++){
					if(i == 0 && j == 0){
						continue;
					}
					if(StateUtil.getTempChunk(chunk.x + i, chunk.z + j).getType() != st_chunk.getType()){
						paintBorder(img, sideCorner(i, j), 2, "#000000", x, z);
					}
					else continue;
				}
			}
		}
		if(type.equals("surface_commercial")){
			//TODO
		}
		saveImage(img, chunk.x, chunk.z, type);
		return;
	}
	
	private static boolean borders(String type, Chunk ck, Chunk st_chunk) {
		switch(type){
			case "surface_states":
			case "states":{
				return ck.getDistrict().getMunicipality().getState().getId() != st_chunk.getDistrict().getMunicipality().getState().getId();
			}
			case "surface_municipalities":
			case "municipalities":{
				return ck.getDistrict().getMunicipality().getId() != st_chunk.getDistrict().getMunicipality().getId();
			}
			case "surface_districts":
			case "districts":{
				return ck.getDistrict().getId() != st_chunk.getDistrict().getId();
			}
			case "chunk_types":{
				if(ck.getType() != st_chunk.getType()){
					return true;
				}
				switch(st_chunk.getType()){
					case DISTRICT: return st_chunk.getDistrict().getId() != ck.getDistrict().getId();
					case MUNICIPAL: return st_chunk.getDistrict().getMunicipality().getId() != ck.getDistrict().getMunicipality().getId();
					case STATEOWNED: return st_chunk.getDistrict().getMunicipality().getState().getId() != ck.getDistrict().getMunicipality().getState().getId();
					case PUBLIC:
					case NORMAL:
					case PRIVATE:
					case COMPANY: return st_chunk.getOwner() != null && ck.getOwner() != null && !ck.getOwner().equals(st_chunk.getOwner());
					default: break;
				}
			}
			case "surface_commercial":{
				//TODO
			}
		}
		return false;
	}

	private static String getColor(String type, net.fexcraft.mod.states.api.Chunk st_chunk) {
		switch(type){
			case "surface_states":
			case "states":{
				return st_chunk.getDistrict().getMunicipality().getState().getColor();
			}
			case "surface_municipalities":
			case "municipalities":{
				return st_chunk.getDistrict().getMunicipality().getColor();
			}
			case "surface_districts":
			case "districts":{
				return st_chunk.getDistrict().getColor();
			}
			case "chunk_types":{
				return st_chunk.getType().getColor();
			}
			case "surface_commercial":{
				//TODO
			}
		}
		return "#afafaf";
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
	
	private static void paintBorder(BufferedImage image, String side, int thickness, String color_hex, int... coords){
		if(!color_hex.contains("#")){
			color_hex = "#" + color_hex;
		}
		Color color = Color.decode(color_hex);
		//
		int i, j, k, l;
		switch(side){
			case "left" :{ i = 0; j = 16; k = 0; l = thickness; break; }
			case "right":{ i = 0; j = 16; k = 16 - thickness; l = 16; break; }
			case "up"   :{ i = 0; j = thickness; k = 0; l = 16; break; }
			case "down" :{ i = 16 - thickness; j = 16; k = 0; l = 16; break; }
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

	public static final int[] getRegion(int x, int z){
		return new int[]{(int)Math.floor(x / 32.0), (int)Math.floor(z / 32.0)};
	}

	public static final String getChunkRegion(int x, int z){
		return (int)Math.floor(x / 32.0) + "_" + (int)Math.floor(z / 32.0);
	}
	
	private static final BufferedImage emptyImage(String type){
		File file = new File(States.getSaveDirectory(), "image_cache/defaults/" + type + ".png");
		if(file.exists()){
			try{
				return ImageIO.read(file);
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		if(NOIMG == null){
			NOIMG = new BufferedImage(IMGSIZE, IMGSIZE, BufferedImage.TYPE_INT_ARGB);
			boolean t = false;
			int black = Color.BLACK.getRGB(), gray = Color.BLUE.getRGB();
			for(int i = 0; i < IMGSIZE; i++){
				for(int j = 0; j < IMGSIZE; j++){
					NOIMG.setRGB(i, j, (t = !t) ? black : gray);
				}
			}
		}
		return NOIMG;
	}
	
	public static final BufferedImage getImage(int x, int z, String type, boolean regcooords){
		File file = new File(States.getSaveDirectory(), "image_cache/" + type + "/" + (regcooords ? x + "_" + z : getChunkRegion(x, z)) + ".png");
		if(!file.exists()){
			return emptyImage(type);
		}
		else{
			try{
				return ImageIO.read(file);
			}
			catch(Exception e){
				Print.debug(file.toPath().toString());
				e.printStackTrace();
				return emptyImage(type);
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
			net.minecraft.world.chunk.Chunk chunk = (net.minecraft.world.chunk.Chunk) entry[1];
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
				net.minecraft.world.chunk.Chunk chunk = world.getChunkFromChunkCoords(Integer.parseInt(ckarr[0]), Integer.parseInt(ckarr[1]));
				String event = object.get("event").getAsString();
				String type = object.get("type").getAsString();
				QUEUE.add(new Object[]{world, chunk, event, type});
			});
		}
		
	}

	public static Queue<Object[]> getQueue(){
		return QUEUE;
	}
	
}