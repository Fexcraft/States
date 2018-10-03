package net.fexcraft.mod.states.objects;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class ItemStackHandler extends net.minecraftforge.items.ItemStackHandler {

    public ItemStackHandler(NonNullList<ItemStack> inventory){
        super(inventory);
    }

    @Override @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate){
        if(stack.getItem() instanceof MailItem == false) return stack;
        return super.insertItem(slot, stack, simulate);
    }

}