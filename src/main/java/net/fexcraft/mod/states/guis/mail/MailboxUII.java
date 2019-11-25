package net.fexcraft.mod.states.guis.mail;

import net.fexcraft.mod.states.impl.SignMailbox;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

/** @author Ferdinand Calo' (FEX___96) */
public class MailboxUII implements IInventory {
	
	public static class MailboxSlot extends Slot {

		public MailboxSlot(IInventory inv, int idx, int x, int y){
			super(inv, idx, x, y);
		}
		
	    public boolean isItemValid(ItemStack stack){
	        return false;
	    }

	}

	private final SignMailbox box;

    public MailboxUII(SignMailbox box){
		this.box = box;
	}

	@Override
    public String getName(){
        return "Generic Mailbox Implementation.";
    }

    @Override
    public boolean hasCustomName(){
        return true;
    }

    @Override
    public ITextComponent getDisplayName(){
        return new TextComponentString(getName());
    }

    @Override
    public int getSizeInventory(){
        return box.getMails().size();
    }

    @Override
    public boolean isEmpty(){
        return box.getMails().isEmpty();
    }

    @Override
    public ItemStack getStackInSlot(int index){
        return box.getMails().get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count){
        return !getStackInSlot(index).isEmpty() ? ItemStackHelper.getAndSplit(box.getMails(), index, count) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStackFromSlot(int index){
        return box.getMails().set(index, ItemStack.EMPTY);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack){
    	box.getMails().set(index, stack);
    }

    @Override
    public int getInventoryStackLimit(){
        return 1;
    }

    @Override
    public void markDirty(){
    	//
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player){
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player){
        //
    }

    @Override
    public void closeInventory(EntityPlayer player){
    	//
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack){
        return false;
    }

    @Override
    public int getField(int id){
        return 0;
    }

    @Override
    public void setField(int id, int value){
        //
    }

    @Override
    public int getFieldCount(){
        return 0;
    }

    @Override
    public void clear(){
    	box.getMails().clear();
    }

}
