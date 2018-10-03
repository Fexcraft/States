package net.fexcraft.mod.states.objects;

import net.fexcraft.mod.lib.api.block.fBlock;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@fBlock(modid = States.MODID, name = "general_mailbox", tileentity = GeneralMailbox.Entity.class)
public class GeneralMailbox extends MailboxBase {

	@Override
	public TileEntity createNewTileEntity(World world, int meta){
		return new Entity();
	}
	
	public static class Entity extends MailboxTileBase {

		public Entity(){ super(4096); }

		@Override
		public boolean accepts(RecipientType rectype, String receiver){
			return rectype == RecipientType.STATE ? StateUtil.getChunk(pos).getMunicipality().isCapital() : true;
		}
		
	}

	@Override
	protected void setupMailbox(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack){
		//
	}

}