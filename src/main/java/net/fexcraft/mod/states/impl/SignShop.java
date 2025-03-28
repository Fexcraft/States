package net.fexcraft.mod.states.impl;

import java.util.UUID;

import net.fexcraft.lib.common.utils.Formatter;
import net.fexcraft.lib.mc.capabilities.sign.SignCapability;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.fsmm.data.Account;
import net.fexcraft.mod.fsmm.data.Bank;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.data.Chunk;
import net.fexcraft.mod.states.data.capabilities.PlayerCapability;
import net.fexcraft.mod.states.data.capabilities.StatesCapabilities;
import net.fexcraft.mod.states.events.PlayerEvents;
import net.fexcraft.mod.states.util.Perms;
import net.fexcraft.mod.states.util.StateUtil;
import net.fexcraft.mod.uni.IDL;
import net.minecraft.block.BlockWallSign;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class SignShop implements SignCapability.Listener {
	
	private static final ResourceLocation REGNAME = new ResourceLocation("states:shop");
	private long price;
	private boolean active, server;
	private ItemStack itemtype;
	private String account;

	@Override
	public ResourceLocation getId(){
		return REGNAME;
	}

	@Override
	public boolean isActive(){
		return active;
	}

	@Override
	public boolean onPlayerInteract(SignCapability cap, PlayerInteractEvent event, IBlockState state,TileEntitySign tileentity){
		if(event.getWorld().isRemote){ return false; }
		if(!active){
			if(tileentity.signText[0].getUnformattedText().toLowerCase().equals("[st-shop]")){
				if(!(tileentity.signText[3].getUnformattedText().toLowerCase().equals("buy") || tileentity.signText[3].getUnformattedText().toLowerCase().equals("sell"))){
					Print.chat(event.getEntityPlayer(), "Invalid type on line 4.");
					return false;
				}
				tileentity.signText[0] = new TextComponentString(Formatter.format("&0[&3St&8-&3Shop&0]"));
				TileEntity te = event.getWorld().getTileEntity(getPosAtBack(state, tileentity));
				EnumFacing facing = state.getBlock() instanceof BlockWallSign ? EnumFacing.byIndex(tileentity.getBlockMetadata()) : null;
				if(te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing) && PlayerEvents.checkAccess(te.getWorld(), te.getPos(), te.getWorld().getBlockState(te.getPos()), event.getEntityPlayer(), true)){
					itemtype = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing).getStackInSlot(0).copy();
					if(!tileentity.signText[1].getUnformattedText().equals("")){
						Chunk chunk = StateUtil.getChunk(te.getPos()); UUID uuid = event.getEntityPlayer().getGameProfile().getId();
						switch(tileentity.signText[1].getUnformattedText().toLowerCase()){
							case "district":
							case "municipality":{
								if(chunk.getMunicipality().isAuthorized(chunk.getMunicipality().r_CREATE_SIGN_SHOP.id, uuid).isTrue()){
									account = "municipality:" + chunk.getMunicipality().getId();
								}
								else{
									Print.chat(event.getEntityPlayer(), "&9No permission to Create Municipality Shops.");
									return true;
								}
								break;
							}
							case "state":{
								if(chunk.getState().isAuthorized(chunk.getState().r_CREATE_SIGN_SHOP.id, uuid).isTrue()){
									account = "state:" + chunk.getMunicipality().getId();
								}
								else{
									Print.chat(event.getEntityPlayer(), "&9No permission to Create State Shops.");
									return true;
								}
								break;
							}
							case "admin":
							case "server":{
								if(Perms.CREATE_SERVER_SIGN_SHOPS.has(event.getEntityPlayer())){
									account = States.SERVERACCOUNT.getTypeAndId();
									server = true;
								}
								else{
									Print.chat(event.getEntityPlayer(), "&9No permission to Create Server Shops.");
									return true;
								}
								break;
							}
							case "player":{
								account = null;
								break;
							}
						}
					}
					if(account == null){
						account = event.getEntityPlayer().getCapability(StatesCapabilities.PLAYER, null).getAccount().getTypeAndId();
					}
					tileentity.signText[1] = new TextComponentString(itemtype.getDisplayName());
					try{
						long leng = Long.parseLong(tileentity.signText[2].getUnformattedText());
						tileentity.signText[2] = new TextComponentString(Config.getWorthAsString(leng, true, leng < 10));
						price = leng;
					}
					catch(Exception e){
						e.printStackTrace();
						Print.chat(event.getEntityPlayer(), "Invalid Price. (1000 == 1" + Config.CURRENCY_SIGN + "!)");
						return false;
					}
					cap.setActive();
					active = true;
					this.sendUpdate(tileentity);
					return true;
				}
				else{
					Print.bar(event.getEntityPlayer(), "No ItemStack Container found.");
				}
			}
		}
		else{
			TileEntity te = event.getWorld().getTileEntity(getPosAtBack(state, tileentity));
			EnumFacing facing = state.getBlock() instanceof BlockWallSign ? EnumFacing.byIndex(tileentity.getBlockMetadata()) : null;
			if(te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing)){
				if(event.getEntityPlayer().getHeldItemMainhand().isEmpty()){
					Account shop = DataManager.getAccount(account.toString(), true, false);
					if(shop == null){
						Print.chat(event.getEntityPlayer(), "Shop Account couldn't be loaded.");
						return true;
					}
					PlayerCapability pcap = event.getEntityPlayer().getCapability(StatesCapabilities.PLAYER, null);
					Account playeracc = pcap.getAccount();
					Bank playerbank = playeracc.getBank();
					IItemHandler te_handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing);
					IItemHandler pl_handler = event.getEntityPlayer().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
					if(tileentity.signText[3].getUnformattedText().toLowerCase().startsWith("buy")){
						if(hasStack(event.getEntityPlayer(), te_handler, false)){
							if(playerbank.processAction(Bank.Action.TRANSFER, pcap.wrapper(), playeracc, price, shop)){
								event.getEntityPlayer().addItemStackToInventory(getStackIfPossible(te_handler, false));
								Print.bar(event.getEntityPlayer(), "Items bought.");
							}
						}
					}
					else if(tileentity.signText[3].getUnformattedText().toLowerCase().startsWith("sell")){
						if(hasStack(event.getEntityPlayer(), pl_handler, true) && hasSpace(event.getEntityPlayer(), te_handler)){
							if(shop.getBank().processAction(Bank.Action.TRANSFER, pcap.wrapper(), shop, price, playeracc)){
								addStack(te_handler, getStackIfPossible(pl_handler, true));
								Print.bar(event.getEntityPlayer(), "Items sold.");
							}
						}
					}
					else{
						Print.chat(event.getEntityPlayer(), "Invalid Mode at line 4.");
					}
				}
				else{
					Print.chat(event.getEntityPlayer(), "&9Shop Owner: &7" + account);
					Print.chat(event.getEntityPlayer(), "&9Item: &7" + itemtype.getDisplayName());
					Print.chat(event.getEntityPlayer(), "&9Reg: &7" + itemtype.getItem().getRegistryName().toString());
					if(itemtype.getMetadata() > 0){
						Print.chat(event.getEntityPlayer(), "&9Meta: &8" + itemtype.getMetadata());
					}
					Print.chat(event.getEntityPlayer(), "&9Amount: &6" + itemtype.getCount());
					if(itemtype.hasTagCompound()){
						Print.chat(event.getEntityPlayer(), "&9NBT: &8" + itemtype.getTagCompound().toString());
					}
				}
				return true;
			}
			else{
				Print.bar(event.getEntityPlayer(), "No ItemStack Container linked.");
			}
		}
		return false;
	}

	private void addStack(IItemHandler handler, ItemStack stack){
		if(server){ return; }
		for(int i = 0; i < handler.getSlots(); i++){
			if((stack = handler.insertItem(i, stack, false)).isEmpty()){
				return;
			}
		}
	}

	private boolean hasSpace(EntityPlayer player, IItemHandler handler){
		if(server){ return true; }
		for(int i = 0; i < handler.getSlots(); i++){
			if(handler.getStackInSlot(i).isEmpty() || isEqualOrValid(handler.getStackInSlot(i), true)){
				return true;
			}
		}
		Print.bar(player, "Not enough space in Container.");
		return false;
	}

	private boolean hasStack(EntityPlayer player, IItemHandler handler, boolean plinv){
		if(server && !plinv){ return true; }
		for(int i = 0; i < handler.getSlots(); i++){
			if(isEqualOrValid(handler.getStackInSlot(i), false)){
				return true;
			}
		}
		Print.bar(player, "Not enough Items in " + (plinv ? "Inventory" : "Container") + ".");
		return false;
	}

	private boolean isEqualOrValid(ItemStack stack, boolean reversecheck){
		if(reversecheck ? stack.getCount() + itemtype.getCount() > stack.getMaxStackSize() : stack.getCount() < itemtype.getCount()){
            return false;
        }
        else if(stack.getItem() != itemtype.getItem()){
            return false;
        }
        else if(stack.getItemDamage() != itemtype.getItemDamage()){
            return false;
        }
        else if(stack.getTagCompound() == null && itemtype.getTagCompound() != null){
            return false;
        }
        else{
            return (stack.getTagCompound() == null || stack.getTagCompound().equals(itemtype.getTagCompound())) && stack.areCapsCompatible(itemtype);
        }
	}

	private ItemStack getStackIfPossible(IItemHandler handler, boolean player){
		if(server && !player){
			return itemtype.copy();
		}
		for(int i = 0; i < handler.getSlots(); i++){
			if(isEqualOrValid(handler.getStackInSlot(i), false)){
				return handler.extractItem(i, itemtype.getCount(), false);
			}
		}
		return null;
	}

	@Override
	public NBTBase writeToNBT(Capability<SignCapability> capability, EnumFacing side){
		if(!active){
			return null;
		}
		NBTTagCompound compound = itemtype.writeToNBT(new NBTTagCompound());
		compound.setLong("sign:price", price);
		compound.setBoolean("sign:active", active);
		compound.setString("sign:account", account);
		if(server){
			compound.setBoolean("sign:server", server);
		}
		return compound;
	}

	@Override
	public void readNBT(Capability<SignCapability> capability, EnumFacing side, NBTBase nbt){
		if(nbt == null || !(nbt instanceof NBTTagCompound)){
			active = false;
			return;
		}
		NBTTagCompound compound = (NBTTagCompound)nbt;
		try{
			itemtype = new ItemStack(compound);
			price = compound.getInteger("sign:price");
			active = compound.getBoolean("sign:active");
			account = compound.getString("sign:account");
			server = compound.hasKey("sign:server") && compound.getBoolean("sign:server");
		}
		catch(Exception e){
			e.printStackTrace();
			active = false;
		}
	}
	
}