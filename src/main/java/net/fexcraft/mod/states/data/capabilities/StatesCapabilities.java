package net.fexcraft.mod.states.data.capabilities;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class StatesCapabilities {
	
	@CapabilityInject(ChunkCapability.class)
	public static final Capability<ChunkCapability> CHUNK = null;
	
	@CapabilityInject(WorldCapability.class)
	public static final Capability<WorldCapability> WORLD = null;
	
	@CapabilityInject(PlayerCapability.class)
	public static final Capability<PlayerCapability> PLAYER = null;
	
}