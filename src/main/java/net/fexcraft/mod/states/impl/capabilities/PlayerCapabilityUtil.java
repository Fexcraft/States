package net.fexcraft.mod.states.impl.capabilities;

import net.fexcraft.mod.states.api.Municipality;
import net.fexcraft.mod.states.data.capabilities.PlayerCapability;
import net.fexcraft.mod.states.data.capabilities.StatesCapabilities;
import net.fexcraft.mod.states.impl.GenericPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class PlayerCapabilityUtil implements ICapabilitySerializable<NBTBase>{
	
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation("states:player");
	private PlayerCapability instance;
	
	public PlayerCapabilityUtil(EntityPlayer player){
		instance = StatesCapabilities.PLAYER.getDefaultInstance();
		instance.setEntityPlayer(player); //instance.load();
		//States.PLAYERS.put(player.getGameProfile().getId(), instance);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing){
		return capability == StatesCapabilities.PLAYER;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing){
		return capability == StatesCapabilities.PLAYER ? StatesCapabilities.PLAYER.<T>cast(this.instance) : null;
	}

	@Override
	public NBTBase serializeNBT(){
		return StatesCapabilities.PLAYER.getStorage().writeNBT(StatesCapabilities.PLAYER, instance, null);
	}

	@Override
	public void deserializeNBT(NBTBase nbt){
		StatesCapabilities.PLAYER.getStorage().readNBT(StatesCapabilities.PLAYER, instance, null, nbt);
	}
	
	//
	
	public static class Storage implements IStorage<PlayerCapability> {

		@Override
		public NBTBase writeNBT(Capability<PlayerCapability> capability, PlayerCapability instance, EnumFacing side){
			instance.save();
			return new NBTTagInt(instance == null ? -1 : instance.getMunicipality() == null ? -1 : instance.getMunicipality().getId());
		}

		@Override
		public void readNBT(Capability<PlayerCapability> capability, PlayerCapability instance, EnumFacing side, NBTBase nbt){
			instance.load();
			if(instance.getMunicipality().getId() == -1){ //Just in case something else broke.
				Municipality mun = instance.getEntityPlayer().getEntityWorld().getCapability(StatesCapabilities.WORLD, null).getMunicipalities().get(((NBTTagInt)nbt).getInt());
				if(mun != null && mun.getId() != -1){
					instance.setMunicipality(mun);
				}
			}
		}
		
	}
	
	//
	
	public static class Callable implements java.util.concurrent.Callable<PlayerCapability>{

		@Override
		public PlayerCapability call() throws Exception {
			return new GenericPlayer();
		}
		
	}

}
