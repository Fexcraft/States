package net.fexcraft.mod.states.util;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.fexcraft.mod.lib.util.common.Static;
import net.fexcraft.mod.states.States;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class ImageCache {
	
	private static final int IMGSIZE = 512;

	public static void update(World world, Chunk chunk, String event, String type){
		if(!type.contains("surface")){
			return;
		}
		Static.getServer().addScheduledTask(new Runnable(){
			@Override
			public void run(){
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
				switch(type){
					case "surface_states":{
						
						break;
					}
				}
				saveImage(img, chunk.x, chunk.z, type);
				return;
			}
		});
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
	
}