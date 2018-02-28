package net.fexcraft.mod.states.impl.capabilities;

import net.fexcraft.mod.states.api.Chunk;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public interface TESCapability {

	@CapabilityInject(TESCapability.class)
	public static final Capability<TESCapability> CAPINJ = null;

	public void setup(Chunk chunk);

	public void setTileEntity(TileEntitySign tileentity);
	
	public TileEntitySign getTileEntity();
	
	public void update(Chunk chunk, boolean sendupdatepacket);

	public NBTBase writeToNBT(Capability<TESCapability> capability, EnumFacing side);

	public void readNBT(Capability<TESCapability> capability, EnumFacing side, NBTBase nbt);

	public boolean isStatesSign();

	public void onPlayerInteract(Chunk chunk, EntityPlayer entityPlayer);

}
