package net.fexcraft.mod.states.util;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.TimerTask;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.fexcraft.lib.common.json.JsonUtil;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.states.States;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class ImageCache extends TimerTask {
	
	private static final int IMGSIZE = 512;
	//private static BufferedImage NOIMG;
	private static final Queue<QueueObj> QUEUE = new LinkedList<>();
	private static final TreeMap<String, TempImg> LOADED_CACHE = new TreeMap<>();
	
	@Override
	public void run(){
		try{
			if(QUEUE.size() == 0){ return; }
			for(int i = 0; i < Config.MAP_UPDATES_PER_SECOND; i++){
				QueueObj obj = QUEUE.poll();
				if(obj != null){
					updateImage(obj.world, obj.chunk);
				}
			}
			if(LOADED_CACHE.size() == 0){ return; }
			for(int i = 0; i < Config.MAP_UPDATES_PER_SECOND; i++){
				Entry<String, TempImg> entry = LOADED_CACHE.firstEntry();
				if(entry != null){
					if(entry.getValue().last_access + (Time.MIN_MS) >= Time.getDate()){
						saveImage(entry.getValue().image, entry.getKey());
						LOADED_CACHE.remove(entry.getKey());
					}
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void update(World world, Chunk chunk){
		if(Config.MAP_UPDATES_PER_SECOND == 0 || chunk == null){
			return;
		}
		QUEUE.add(new QueueObj(world, chunk));
		return;
	}
	
	private static final void updateImage(World world, Chunk chunk){
		BufferedImage img = getImage(chunk.x, chunk.z, false);
		if(img == null){ return; }
		int rx = (int)Math.floor(chunk.x / 32.0), rz = (int)Math.floor(chunk.z / 32.0);
		int x = (chunk.x * 16) - (rx * 512), z = (chunk.z * 16) - (rz * 512);
		for(int i = 0; i < 16; i++){
			for(int j = 0; j < 16; j++){
				BlockPos pos = getPos(world, i + (chunk.x * 16), j + (chunk.z * 16));
				IBlockState state = world.getBlockState(pos);
				img.setRGB(x + i, z + j, new Color(state.getMapColor(world, pos).colorValue).getRGB());
			}
		}
		//Save
		String regaddr = getChunkRegion(chunk.x, chunk.z);
		if(LOADED_CACHE.containsKey(regaddr)){
			TempImg temp = LOADED_CACHE.get(regaddr);
			temp.image = img;
			temp.last_access = Time.getDate();
		}
		else{
			LOADED_CACHE.put(regaddr, new TempImg(img));
		}
		return;
	}

	public static final int[] getRegion(int x, int z){
		return new int[]{(int)Math.floor(x / 32.0), (int)Math.floor(z / 32.0)};
	}

	public static final String getChunkRegion(int x, int z){
		return (int)Math.floor(x / 32.0) + "_" + (int)Math.floor(z / 32.0);
	}
	
	public static final BufferedImage emptyImage(){
		File file = new File(States.getSaveDirectory(), "image_cache/default.png");
		if(file.exists()){
			try{ return ImageIO.read(file); }
			catch(Exception e){ e.printStackTrace(); }
		}
		BufferedImage img = new BufferedImage(IMGSIZE, IMGSIZE, BufferedImage.TYPE_INT_ARGB);
		boolean t = false;
		int black = Color.BLACK.getRGB(), gray = Color.GRAY.getRGB();
		for(int i = 0; i < IMGSIZE; i++){
			for(int j = 0; j < IMGSIZE; j++){
				img.setRGB(i, j, (t = j == 255 ? t : !t) ? black : gray);
			}
		}
		return img;
	}
	
	public static final BufferedImage getImage(int x, int z, boolean regcooords){
		String regaddr = regcooords ? x + "_" + z : getChunkRegion(x, z);
		if(LOADED_CACHE.containsKey(regaddr)){
			return LOADED_CACHE.get(regaddr).image;
		}
		File file = new File(States.getSaveDirectory(), "image_cache/" + regaddr + ".png");
		if(!file.exists()){ return emptyImage(); }
		else{
			try{ return ImageIO.read(file); }
			catch(Exception e){
				Print.debug(file.toPath().toString());
				e.printStackTrace();
				return emptyImage();
			}
		}
	}
	
	public static final void saveImage(BufferedImage image, String reg){
		File file = new File(States.getSaveDirectory(), "image_cache/" + reg + ".png");
		if(!file.getParentFile().exists()){ file.getParentFile().mkdirs(); }
		try{ ImageIO.write(image, "png", file); } catch(Exception e){ e.printStackTrace(); }
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
		QueueObj entry = null;
		while((entry = QUEUE.poll()) != null){
			JsonObject obj = new JsonObject();
			obj.addProperty("dimension", entry.world.provider.getDimension());
			obj.addProperty("chunk", entry.chunk.x + ":" + entry.chunk.z);
			array.add(obj);
		}
		JsonObject obj = new JsonObject();
		obj.addProperty("last_save", Time.getDate());
		obj.addProperty("last_save_string", Time.getAsString(obj.get("last_save").getAsLong()));
		obj.add("queue", array);
		JsonUtil.write(new File(States.getSaveDirectory(), "image_cache/queue.json"), obj, true);
		//Force-process anything.
		if(LOADED_CACHE.size() > 0){
			for(Entry<String, TempImg> entr : LOADED_CACHE.entrySet()){
				saveImage(entr.getValue().image, entr.getKey());
			}
		}
	}

	public static void loadQueue(){
		JsonObject obj = JsonUtil.get(new File(States.getSaveDirectory(), "image_cache/queue.json"));
		if(obj.has("queue")){
			JsonArray array = obj.get("queue").getAsJsonArray();
			array.forEach(elm -> {
				JsonObject object = elm.getAsJsonObject();
				World world = Static.getServer().getWorld(object.get("dimension").getAsInt());
				String[] ckarr = object.get("chunk").getAsString().split(":");
				Chunk chunk = world.getChunk(Integer.parseInt(ckarr[0]), Integer.parseInt(ckarr[1]));
				QUEUE.add(new QueueObj(world, chunk));
			});
		}
	}

	public static Queue<QueueObj> getQueue(){
		return QUEUE;
	}
	
	private static class TempImg {
		
		public TempImg(BufferedImage img){
			this.image = img;
			this.last_access = Time.getDate();
		}
		
		private BufferedImage image;
		private long last_access;
		
	}
	
	private static class QueueObj {
		
		public QueueObj(World world, Chunk chunk){
			this.world = world; this.chunk = chunk;
		}
		
		private World world;
		private Chunk chunk;
		
	}
	
}