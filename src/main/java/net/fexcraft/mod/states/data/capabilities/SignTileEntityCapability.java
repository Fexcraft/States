package net.fexcraft.mod.states.data.capabilities;

import javax.annotation.Nullable;

import net.fexcraft.mod.states.data.Chunk;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public interface SignTileEntityCapability {

	public void setup(Chunk chunk);

	public void setTileEntity(TileEntitySign tileentity);
	
	public TileEntitySign getTileEntity();
	
	public void update(Chunk chunk, @Nullable String task, boolean sendupdatepacket);

	public NBTBase writeToNBT(Capability<SignTileEntityCapability> capability, EnumFacing side);

	public void readNBT(Capability<SignTileEntityCapability> capability, EnumFacing side, NBTBase nbt);

	public boolean isStatesSign();

	public void onPlayerInteract(Chunk chunk, EntityPlayer entityPlayer);

}
