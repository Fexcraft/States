package net.fexcraft.mod.states.guis.mail;

import net.fexcraft.lib.mc.capabilities.FCLCapabilities;
import net.fexcraft.lib.mc.gui.GenericContainer;
import net.fexcraft.lib.mc.gui.GenericGui;
import net.fexcraft.mod.states.impl.SignMailbox;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;

public class MailboxUIC extends GenericContainer {
	
	protected GenericGui<MailboxUIC> gui;
	protected TileEntity sign;
	protected SignMailbox box;
	protected MailboxUII inv;
	protected int slots;

	public MailboxUIC(EntityPlayer player, World world, int x, int y, int z){
		super(player); sign = world.getTileEntity(new BlockPos(x, y, z));
		box = sign.getCapability(FCLCapabilities.SIGN_CAPABILITY, null).getListener(SignMailbox.class, SignMailbox.RESLOC);
    	inv = new MailboxUII(box); slots = inv.getSizeInventory();
        for(int row = 0; row < 6; row++){
            for(int col = 0; col < 12; col++){ if(col + row * 12 >= slots) break;
                addSlotToContainer(new MailboxUII.MailboxSlot(inv, col + row * 12, 11 + col * 18, 11 + row * 18));
            }
        }
        for(int row = 0; row < 3; row++){
            for(int col = 0; col < 9; col++){
                addSlotToContainer(new Slot(player.inventory, col + row * 9 + 9, 11 + col * 18, 124 + row * 18));
            }
        }
        for(int col = 0; col < 9; col++){
            addSlotToContainer(new Slot(player.inventory, col, 11 + col * 18, 180));
        }
	}

	@Override
	protected void packet(Side side, NBTTagCompound packet, EntityPlayer player){
		if(!packet.hasKey("cargo")) return;
	}

    @Override
    public boolean canInteractWith(EntityPlayer player){
        return true;
    }
	
    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index){
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if(slot != null && slot.getHasStack()){
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if(index < slots){
                if(!this.mergeItemStack(itemstack1, slots, this.inventorySlots.size(), true)){
                    return ItemStack.EMPTY;
                }
            }
            else if(!this.mergeItemStack(itemstack1, 0, slots, false)){ return ItemStack.EMPTY; }
            if(itemstack1.isEmpty()){ slot.putStack(ItemStack.EMPTY); } else{ slot.onSlotChanged(); }
        } return itemstack;
    }

    @Override
    public void onContainerClosed(EntityPlayer player){
        super.onContainerClosed(player);
        if(!player.world.isRemote) box.updateSize(sign, box.getMails().size(), true);
    }
    
    @Override
    public void detectAndSendChanges(){
        super.detectAndSendChanges();
    }

}
