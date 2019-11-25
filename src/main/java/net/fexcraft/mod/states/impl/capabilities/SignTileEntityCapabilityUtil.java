package net.fexcraft.mod.states.impl.capabilities;

import java.util.TreeMap;

import net.fexcraft.mod.states.data.capabilities.SignTileEntityCapability;
import net.fexcraft.mod.states.data.capabilities.StatesCapabilities;
import net.minecraft.nbt.NBTBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class SignTileEntityCapabilityUtil implements ICapabilitySerializable<NBTBase> {

	public static final TreeMap<BlockPos, TileEntity> TILEENTITIES = new TreeMap<>();
	private final SignTileEntityCapability instance;
	private TileEntitySign tileentity;

	public SignTileEntityCapabilityUtil(TileEntity object){
		tileentity = (TileEntitySign)object;
		instance = StatesCapabilities.SIGN_TE.getDefaultInstance();
		instance.setTileEntity(tileentity);
		TILEENTITIES.put(tileentity.getPos(), tileentity);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing){
		return capability == StatesCapabilities.SIGN_TE;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing){
		return capability == StatesCapabilities.SIGN_TE ? StatesCapabilities.SIGN_TE.<T>cast(this.instance) : null;
	}

	@Override
	public NBTBase serializeNBT(){
		return StatesCapabilities.SIGN_TE.getStorage().writeNBT(StatesCapabilities.SIGN_TE, instance, null);
	}

	@Override
	public void deserializeNBT(NBTBase nbt){
		StatesCapabilities.SIGN_TE.getStorage().readNBT(StatesCapabilities.SIGN_TE, instance, null, nbt);
	}

	public static void processChunkChange(net.minecraft.world.chunk.Chunk chunk, String string){
            if(chunk == null){ return; }
		chunk.getTileEntityMap().values().forEach(te -> {
			if(te instanceof TileEntitySign){
				SignTileEntityCapability cap = te.getCapability(StatesCapabilities.SIGN_TE, null);
				cap.update(chunk.getCapability(StatesCapabilities.CHUNK, null).getStatesChunk(), string, true);
			}
		});
	}
	
	//
	
	public static class Callable implements java.util.concurrent.Callable<SignTileEntityCapability> {

		@Override
		public SignTileEntityCapability call() throws Exception {
			return new SignTileEntityImplementation();
		}

	}
	
	//
	
	public static class Storage implements IStorage<SignTileEntityCapability> {

		@Override
		public NBTBase writeNBT(Capability<SignTileEntityCapability> capability, SignTileEntityCapability instance, EnumFacing side){
			return instance.writeToNBT(capability, side);
		}

		@Override
		public void readNBT(Capability<SignTileEntityCapability> capability, SignTileEntityCapability instance, EnumFacing side, NBTBase nbt){
			instance.readNBT(capability, side, nbt);
		}

	}

}
