package net.fexcraft.mod.states.util;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ImageUtil {

	public static ResourceLocation getTempChunkImage(World world, int i, int j){
		ResourceLocation rs = new ResourceLocation("states:temp/geo_chunk_" + i + "_" + j);
		ITextureObject object = Minecraft.getMinecraft().renderEngine.getTexture(rs);
		if(object == null){
        	Minecraft.getMinecraft().renderEngine.loadTexture(rs, object = new TempChunkTexture(world, rs, i, j));
		}
		return rs;
	}
	
	public static class TempChunkTexture extends SimpleTexture {

		private World world;
		private int x, z;
	    @Nullable
	    private BufferedImage bufferedImage;

		public TempChunkTexture(World world, ResourceLocation rs, int i, int j){
			super(rs);
			this.world = world;
			this.x = i; this.z = j;
			this.bufferedImage = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		}
		
		@Override
		public void loadTexture(IResourceManager resourceManager) throws IOException {
	        if(this.bufferedImage == null && this.textureLocation != null){
	            super.loadTexture(resourceManager);
	        }
	        for(int i = 0; i < 16; i++){
				for(int j = 0; j < 16; j++){
					BlockPos pos = getPos(world, i + (x * 16), j + (z * 16));
					IBlockState state = world.getBlockState(pos);
					bufferedImage.setRGB(i, j, new Color(state.getMapColor(world, pos).colorValue).getRGB());
				}
			}
	        if(this.bufferedImage != null){
                if(this.textureLocation != null){
                    this.deleteGlTexture();
                }
                TextureUtil.uploadTextureImage(super.getGlTextureId(), this.bufferedImage);
            }
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

	    /*private void checkTextureUploaded(){
	        if(!this.textureUploaded){
	            if(this.bufferedImage != null){
	                if(this.textureLocation != null){
	                    this.deleteGlTexture();
	                }
	                TextureUtil.uploadTextureImage(super.getGlTextureId(), this.bufferedImage);
	                this.textureUploaded = true;
	            }
	        }
	    }*/

	    public int getGlTextureId(){
	        //this.checkTextureUploaded();
	        return super.getGlTextureId();
	    }
		
	}

}
