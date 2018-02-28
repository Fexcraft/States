package net.fexcraft.mod.states.impl.capabilities;

import java.util.TreeMap;

import net.fexcraft.mod.lib.util.common.Static;
import net.fexcraft.mod.states.api.Chunk;
import net.minecraft.nbt.NBTBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class TESSerialisable implements ICapabilitySerializable<NBTBase> {
	
	public static final TreeMap<BlockPos, TileEntity> TILEENTITIES = new TreeMap<>();
	private final TESCapability instance;
	private TileEntitySign tileentity;

	public TESSerialisable(TileEntity object){
		tileentity = (TileEntitySign)object;
		instance = TESCapability.CAPINJ.getDefaultInstance();
		instance.setTileEntity(tileentity);
		TILEENTITIES.put(tileentity.getPos(), tileentity);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing){
		return capability == TESCapability.CAPINJ;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing){
		return capability == TESCapability.CAPINJ ? TESCapability.CAPINJ.<T>cast(this.instance) : null;
	}

	@Override
	public NBTBase serializeNBT(){
		return TESCapability.CAPINJ.getStorage().writeNBT(TESCapability.CAPINJ, instance, null);
	}

	@Override
	public void deserializeNBT(NBTBase nbt){
		TESCapability.CAPINJ.getStorage().readNBT(TESCapability.CAPINJ, instance, null, nbt);
	}

	public static void processChunkChange(Chunk chunk, String string){
		net.minecraft.world.chunk.Chunk ck = Static.getServer().worlds[0].getChunkFromChunkCoords(chunk.xCoord(), chunk.zCoord());
		ck.getTileEntityMap().values().forEach(te -> {
			if(te instanceof TileEntitySign){
				TESCapability cap = te.getCapability(TESCapability.CAPINJ, null);
				cap.update(chunk, string, true);
			}
		});
	}

}
