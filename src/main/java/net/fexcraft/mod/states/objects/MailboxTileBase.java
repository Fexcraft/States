package net.fexcraft.mod.states.objects;

import java.util.ArrayList;

import javax.annotation.Nullable;

import net.fexcraft.mod.lib.api.network.IPacketReceiver;
import net.fexcraft.mod.lib.network.packet.PacketTileEntityUpdate;
import net.fexcraft.mod.lib.util.common.ApiUtil;
import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.states.api.Mailbox;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;

public abstract class MailboxTileBase extends TileEntity implements Mailbox, IPacketReceiver<PacketTileEntityUpdate> {
	
	protected NonNullList<ItemStack> mails;
	protected ItemStackHandler handler;
	protected ArrayList<String> recipients;
	
	public MailboxTileBase(int size){
		mails = NonNullList.<ItemStack>withSize(4096, ItemStack.EMPTY);
		recipients = new ArrayList<String>();
	}
	
    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing){
        return (facing != null && facing.getAxis().isVertical() && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) ? true : super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked") @Override @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing){
        if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && this.hasCapability(capability, facing)){
            return (T)(handler == null ? handler = new ItemStackHandler(null) : handler);
        } return super.getCapability(capability, facing);
    }
    
    //
    
    public SPacketUpdateTileEntity getUpdatePacket(){
        return new SPacketUpdateTileEntity(this.getPos(), this.getBlockMetadata(), this.getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag(){
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt){
        this.readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound){
        super.writeToNBT(compound);
        //ItemStackHelper.saveAllItems(compound, mails);
        compound.setInteger("Items", mails.size());
        for(int i = 0; i < mails.size(); i++){
        	compound.setTag("Item" + i, mails.get(i).writeToNBT(new NBTTagCompound()));
        }
        if(!recipients.isEmpty()){
        	compound.setInteger("Recipients", recipients.size());
        	for(int i = 0; i < recipients.size(); i++){
        		compound.setString("Recipient" + i, recipients.get(i));
        	}
        }
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound){
        super.readFromNBT(compound);
        //ItemStackHelper.loadAllItems(compound, mails);
        int length = compound.getInteger("Items");
        mails = mails == null ? NonNullList.<ItemStack>withSize(length, ItemStack.EMPTY) : mails;
        for(int i = 0; i < length; i++){ if(i >= mails.size()) break;
        	mails.set(i, new ItemStack(compound.getCompoundTag("Item" + i)));
        }
        int recs = compound.getInteger("Recipients");
        for(int i = 0; i < recs; i++){
        	recipients.add(compound.getString("Recipient" + i));
        }
    }
    
    @Override
	public void processClientPacket(PacketTileEntityUpdate packet){
		this.readFromNBT(packet.nbt);
	}
    
    public void updateRemote(){ ApiUtil.sendTileEntityUpdatePacket(this, this.writeToNBT(new NBTTagCompound()), 256); }

	@Override
	public void insert(RecipientType rectype, String receiver, String sender, String message, MailType type, long expiry, NBTTagCompound compound){
		Print.debug(rectype, receiver, sender, message, type, expiry, compound);
	}
	
}