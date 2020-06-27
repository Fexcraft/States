package net.fexcraft.mod.states.guis;

import static net.fexcraft.mod.states.util.StateUtil.bypass;
import static net.fexcraft.mod.states.util.StateUtil.translate;

import java.awt.Color;

import org.apache.commons.lang3.math.NumberUtils;

import com.mojang.authlib.GameProfile;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.gui.GenericContainer;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.states.data.Chunk;
import net.fexcraft.mod.states.data.District;
import net.fexcraft.mod.states.data.DistrictType;
import net.fexcraft.mod.states.data.Municipality;
import net.fexcraft.mod.states.data.MunicipalityType;
import net.fexcraft.mod.states.data.State;
import net.fexcraft.mod.states.data.capabilities.PlayerCapability;
import net.fexcraft.mod.states.data.capabilities.StatesCapabilities;
import net.fexcraft.mod.states.data.root.AnnounceLevel;
import net.fexcraft.mod.states.util.StateLogger;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.fml.relauncher.Side;

public class ManagerContainer extends GenericContainer {
	
	protected static final String NOTAX = "no_tax";
	protected static final String NOMAILBOX = "no_mailbox";
	protected static final String NOONE = "no_one";
	protected static final String NOTFORSALE = "not_for_sale";
	protected static final String NONE = "none";
	protected static final String UNKNOWN = "unknown";
	//
	private ManagerGui gui;
	protected Mode mode;
	protected Layer layer;
	protected Chunk chunk;
	protected District dis;
	protected Municipality mun;
	protected State state;
	protected PlayerCapability cap;
	//
	protected String layer_title;
	protected String[] keys = new String[0];
	//
	protected String[] view_values;
	protected ViewMode[] view_modes;

	public ManagerContainer(EntityPlayer player, int layerid, int x, int y, int z){
		super(player);
		mode = Mode.values()[x];
		layer = Layer.values()[layerid];
		Print.debug("CREATED " + player.world.isRemote + " " + layer + "/" + layerid + " " + mode + "/" + x);
		if(player.world.isRemote) return;
		chunk = StateUtil.getChunk(player);
		cap = player.getCapability(StatesCapabilities.PLAYER, null);
		switch(layer){
			case COMPANY:
				//
				break;
			case DISTRICT:
				dis = y < -5 ? chunk.getDistrict() : StateUtil.getDistrict(y);
				break;
			case MUNICIPALITY:
				mun = y < -5 ? chunk.getMunicipality() : StateUtil.getMunicipality(y);
				break;
			case PROPERTY:
				//
				break;
			case STATE:
				state = y < -5 ? chunk.getState() : StateUtil.getState(y);
				break;
			case UNION:
				//
				break;
			default:
				break;
		}
	}

	private void sendViewData(){
		NBTTagCompound packet = new NBTTagCompound();
		packet.setString("cargo", "init");
		packet.setString("layer_title", getLayerTitle());
		NBTTagList list = new NBTTagList();
		switch(layer){
			case CHUNK:
				//TODO
				break;
			case COMPANY:
				//TODO
				break;
			case DISTRICT:
				addKey(list, "id", dis.getId(), ViewMode.NONE);
				addKey(list, "name", dis.getName(), ViewMode.EDIT);
				addKey(list, "municipality", dis.getMunicipality().getName() + " (" + dis.getMunicipality().getId() + ")", ViewMode.GOTO);
				addKey(list, "manager", dis.getHead() == null ? NOONE : Static.getPlayerNameByUUID(dis.getHead()), ViewMode.EDIT);
				addKey(list, "price", dis.getPrice() == 0 ? NOTFORSALE : ggas(dis.getPrice()), ViewMode.EDIT);
				addKey(list, "type", dis.getType().name().toLowerCase(), ViewMode.EDIT);
				addKey(list, "color", dis.getColor(), ViewMode.EDIT);
				addKey(list, "chunk_tax", dis.getChunkTax() > 0 ? ggas(dis.getChunkTax()) : NOTAX, ViewMode.EDIT);
				addKey(list, "last_edited", time(dis.getChanged()), ViewMode.NONE);
				addKey(list, "neighbors", dis.getNeighbors().size(), ViewMode.LIST);
				addKey(list, "chunks", dis.getClaimedChunks(), ViewMode.NONE);
				addKey(list, "canforsettle", dis.r_CFS.get(), ViewMode.BOOL);
				addKey(list, "unifbank", dis.r_ONBANKRUPT.get(), ViewMode.BOOL);
				addKey(list, "creator", Static.getPlayerNameByUUID(dis.getCreator()), ViewMode.NONE);
				addKey(list, "created", time(dis.getCreated()), ViewMode.NONE);
				addKey(list, "ruleset", dis.getRulesetTitle(), ViewMode.EDIT);
				addKey(list, "mailbox", dis.getMailbox() == null ? NOMAILBOX : dis.getMailbox().toString(), ViewMode.RESET);
				addKey(list, "icon", dis.getIcon(), ViewMode.EDIT);
				break;
			case MUNICIPALITY:
				addKey(list, "id", mun.getId(), ViewMode.NONE);
				addKey(list, "name", mun.getName(), ViewMode.EDIT);
				if(mun.isAbandoned()){
					addKey(list, "abandoned", mun.isAbandoned(), ViewMode.NONE);
					addKey(list, "abandoned_since", mun.getAbandonedSince() == 0 ? UNKNOWN : time(mun.getAbandonedSince()), ViewMode.NONE);
					addKey(list, "abandoned_by", mun.getAbandonedBy() == null ? UNKNOWN : Static.getPlayerNameByUUID(mun.getAbandonedBy()), ViewMode.NONE);
				}
				addKey(list, "state", mun.getState().getName() + " (" + mun.getState().getId() + ")", ViewMode.GOTO);
				addKey(list, "mayor", mun.getHead() == null ? NOONE : Static.getPlayerNameByUUID(mun.getHead()), ViewMode.EDIT);
				addKey(list, "price", mun.getPrice() == 0 ? NOTFORSALE : ggas(mun.getPrice()), ViewMode.EDIT);
				addKey(list, "type", mun.getType().getTitle(), ViewMode.NONE);
				addKey(list, "color", mun.getColor(), ViewMode.EDIT);
				addKey(list, "citizen", mun.getCitizen().size(), ViewMode.LIST);
				addKey(list, "balance", ggas(mun.getAccount().getBalance()), ViewMode.GOTO);
				addKey(list, "citizen_tax", mun.getCitizenTax() > 0 ? ggas(mun.getCitizenTax()) : NOTAX, ViewMode.EDIT);
				addKey(list, "last_edited", time(mun.getChanged()), ViewMode.NONE);
				addKey(list, "council", mun.getCouncil().size(), ViewMode.LIST);
				addKey(list, "districts", mun.getDistricts().size(), ViewMode.LIST);
				addKey(list, "neighbors", mun.getNeighbors().size(), ViewMode.LIST);
				addKey(list, "opentojoin", mun.r_OPEN.get(), ViewMode.BOOL);
				addKey(list, "kickbankrupt", mun.r_KIB.get(), ViewMode.BOOL);
				addKey(list, "chunks", mun.getClaimedChunks() + "/" + MunicipalityType.getChunkLimitFor(mun), ViewMode.NONE);
				addKey(list, "creator", Static.getPlayerNameByUUID(mun.getCreator()), ViewMode.NONE);
				addKey(list, "created", time(mun.getCreated()), ViewMode.NONE);
				addKey(list, "forcechunks", mun.getForceLoadedChunks() == null ? NONE : mun.getForceLoadedChunks().size(), ViewMode.NONE);
				addKey(list, "ruleset", mun.getRulesetTitle(), ViewMode.EDIT);
				addKey(list, "mailbox", mun.getMailbox() == null ? NOMAILBOX : mun.getMailbox().toString(), ViewMode.RESET);
				addKey(list, "icon", mun.getIcon(), ViewMode.EDIT);
				if(!mun.isAbandoned()){
					addKey(list, "abandoned", mun.isAbandoned(), ViewMode.NONE);
				}
				break;
			case PLAYERDATA:
				break;
			case PROPERTY:
				break;
			case STATE:
				break;
			case UNION:
				break;
			default:
				break;
			
		}
		readInit(list);
		packet.setTag("keys", list);
		this.send(Side.CLIENT, packet);
	}

	private void addKey(NBTTagList list, String key, Object value, ViewMode mode){
		list.appendTag(new NBTTagString(key + ";" + value.toString() + ";" + mode.ordinal()));
	}
	
	public static final String ggas(long value){
		return Config.getWorthAsString(value);
	}
	
	public static final String time(long value){
		return Time.getAsString(value);
	}

	@Override
	protected void packet(Side side, NBTTagCompound packet, EntityPlayer player){
		Print.debug(side + " " + packet);
		if(side.isClient()){
			switch(packet.getString("cargo")){
				case "init":{
					layer_title = packet.getString("layer_title");
					readInit((NBTTagList)packet.getTag("keys"));
					gui.refreshKeys();
					break;
				}
				case "status_msg":{
					gui.setTitle(packet.getString("msg"));
					break;
				}
			}
		}
		else{
			switch(packet.getString("cargo")){
				case "init":{
					switch(mode){
						case INFO:
							sendViewData();
							break;
						case LIST_COMPONENTS:
							//
							break;
						case LIST_CITIZENS:
							//
							break;
						case LIST_COUNCIL:
							//
							break;
						default:
							break;
					}
					break;
				}
				case "view_mode_click":{
					if(mode != Mode.INFO) return;
					String value = packet.hasKey("value") ? packet.getString("value") : null;
					switch(layer){
						case CHUNK:
							break;
						case COMPANY:
							break;
						case DISTRICT:
							switch(keys[packet.getInteger("button")]){
								case "name":{
									if(dis.isAuthorized(dis.r_SET_NAME.id, cap.getUUID()).isTrue() || bypass(player)){
										value = value.trim();
										if(value.replace(" ", "").length() < 3){
											sendStatus("states.manager_gui.view.name_short");
											break;
										}
										dis.setName(value);
										dis.setChanged(Time.getDate());
										dis.save();
										sendViewData();
										Print.log(StateLogger.player(player) + " changed name of " + StateLogger.district(dis) + " to " + dis.getName() + ".");
									}
									else sendStatus(null);
									break;
								}
								case "municipality":{
									openGui(Layer.MUNICIPALITY, Mode.INFO, dis.getMunicipality().getId());
									break;
								}
								case "manager":{
									if(dis.isAuthorized(dis.r_SET_MANAGER.id, cap.getUUID()).isTrue() || bypass(player)){
										GameProfile gp = Static.getServer().getPlayerProfileCache().getGameProfileForUsername(value);
										if(gp == null || gp.getId() == null){
											sendStatus("states.manager_gui.view.player_not_found_cache");
											break;
										}
										dis.setHead(gp.getId());
										dis.setChanged(Time.getDate());
										dis.save();
										sendViewData();
										Print.log(StateLogger.player(player) + " changed manager of " + StateLogger.district(dis) + " to " + StateLogger.player(gp) + ".");
									}
									else sendStatus(null);
									break;
								}
								case "price":{
									if(dis.isAuthorized(dis.r_SET_PRICE.id, cap.getUUID()).isTrue() || bypass(player)){
										try{
											Long price = Long.parseLong(value);
											if(price < 0){ price = 0l; }
											dis.setPrice(price);
											dis.setChanged(Time.getDate());
											dis.save();
											sendViewData();
											Print.log(StateLogger.player(player) + " changed price of " + StateLogger.district(dis) + " to " + dis.getPrice() + ".");
										}
										catch(Exception e){
											sendStatus("&cError: &7" + e.getMessage());
										}
									}
									else sendStatus(null);
									break;
								}
								case "type":{
									if(dis.isAuthorized(dis.r_SET_TYPE.id, cap.getUUID()).isTrue() || bypass(player)){
										try{
											DistrictType type = DistrictType.valueOf(value.toUpperCase());
											if(type != null){
												dis.setType(type);
												dis.setChanged(Time.getDate());
												dis.save();
											}
											sendViewData();
											Print.log(StateLogger.player(player) + " changed type of " + StateLogger.district(dis) + " to " + dis.getType() + ".");
										}
										catch(Exception e){
											sendStatus("&9Error: &7" + e.getMessage());
										}
									}
									else sendStatus(null);
									break;
								}
								case "color":{
									if(dis.isAuthorized(dis.r_SET_COLOR.id, cap.getUUID()).isTrue() || bypass(player)){
										try{
											String str = value;
											if(str.replace("#", "").length() != 6){
												sendStatus("states.manager_gui.view.invalid_hex");
												break;
											}
											str = str.startsWith("#") ? str : "#" + str;
											Color.decode(str);
											dis.setColor(str);
											dis.setChanged(Time.getDate());
											dis.save();
											sendViewData();
											Print.log(StateLogger.player(player) + " changed color of " + StateLogger.district(dis) + " to " + dis.getColor() + ".");
										}
										catch(Exception e){
											sendStatus("&2Error: &7" + e.getMessage());
										}
									}
									else sendStatus(null);
									break;
								}
								case "chunk_tax":{
									if(dis.isAuthorized(dis.r_SET_CHUNKTAX.id, cap.getUUID()).isTrue() || bypass(player)){
										if(value.equals("reset") || value.equals("disable")){
											dis.setChunkTax(0);
											dis.setChanged(Time.getDate());
											dis.save();
											sendViewData();
										}
										else if(NumberUtils.isCreatable(value)){
											dis.setChunkTax(Long.parseLong(value));
											dis.setChanged(Time.getDate());
											dis.save();
											sendViewData();
										}
										else{
											sendStatus("states.manager_gui.view.not_number");
										}
									}
									else sendStatus(null);
									break;
								}
								case "neighbors":{
									openGui(Layer.DISTRICT, Mode.LIST_NEIGHBORS, dis.getId());
									break;
								}
								case "canforsettle":{
									if(dis.isAuthorized(dis.r_CFS.id, cap.getUUID()).isTrue() || bypass(player)){
										dis.r_CFS.set(!dis.r_CFS.get());
										dis.setChanged(Time.getDate());
										dis.save();
										this.sendViewData();
										Print.log(StateLogger.player(player) + " changed 'can-foreigners-settle' of " + StateLogger.district(dis) + " to " + dis.r_CFS.get() + ".");
									}
									else sendStatus(null);
									break;
								}
								case "unifbank":{
									if(dis.isAuthorized(dis.r_ONBANKRUPT.id, cap.getUUID()).isTrue() || bypass(player)){
										dis.r_ONBANKRUPT.set(!dis.r_ONBANKRUPT.get());
										dis.setChanged(Time.getDate()); dis.save();
										this.sendViewData();
										Print.log(StateLogger.player(player) + " changed 'unclaim-if-brankrupt' of " + StateLogger.district(dis) + " to " + dis.r_ONBANKRUPT.get() + ".");
									}
									else sendStatus(null);
									break;
								}
								case "ruleset":{
									if(dis.isAuthorized(dis.r_SET_RULESET.id, cap.getUUID()).isTrue() || bypass(player)){
										value = value.trim();
										if(value.replace(" ", "").length() < 3){
											sendStatus("states.manager_gui.view.name_short");
											break;
										}
										dis.setRulesetTitle(value);
										dis.setChanged(Time.getDate());
										dis.save();
										sendViewData();
										Print.log(StateLogger.player(player) + " changed ruleset name of " + StateLogger.district(dis) + " to " + dis.getName() + ".");
									}
									else sendStatus(null);
									break;
								}
								case "mailbox":{
									if(dis.isAuthorized(dis.r_SET_MAILBOX.id, cap.getUUID()).isTrue() || bypass(player)){
										dis.setMailbox(null);
										dis.setChanged(Time.getDate());
										dis.save();
										sendViewData();
										Print.log(StateLogger.player(player) + " reset mailbox location of" + StateLogger.district(dis) + ".");
									}
									else sendStatus(null);
									break;
								}
								case "icon":{
									if(dis.isAuthorized(dis.r_SET_ICON.id, cap.getUUID()).isTrue() || bypass(player)){
										try{
											dis.setIcon(value);
											dis.setChanged(Time.getDate());
											dis.save();
											sendViewData();
											Print.log(StateLogger.player(player) + " changed icon of " + StateLogger.district(dis) + " to " + dis.getIcon() + ".");
										}
										catch(Exception e){
											sendStatus("&2Error: &7" + e.getMessage());
										}
									}
									else sendStatus(null);
									break;
								}
								default: return;
							}
							break;
						case MUNICIPALITY:
							switch(keys[packet.getInteger("button")]){
								case "name":{
									if(mun.isAuthorized(mun.r_SET_NAME.id, cap.getUUID()).isTrue() || bypass(player)){
										value = value.trim();
										if(value.replace(" ", "").length() < 3){
											sendStatus("states.manager_gui.view.name_short");
											break;
										}
										mun.setName(value);
										mun.setChanged(Time.getDate());
										mun.save();
										sendViewData();
										Print.log(StateLogger.player(player) + " changed name of " + StateLogger.municipality(mun) + " to " + mun.getName() + ".");
									}
									else sendStatus(null);
									break;
								}
								case "state":{
									openGui(Layer.STATE, Mode.INFO, mun.getId());
									break;
								}
								case "mayor":{
									if(mun.isAuthorized(mun.r_SET_MAYOR.id, cap.getUUID()).isTrue() || bypass(player)){
										if(value.equals("null") || value.equals("reset")){
											if(mun.isAuthorized(mun.r_RESET_MAYOR.id, cap.getUUID()).isFalse() && !bypass(player)){
												sendStatus(null);
												return;
											}
											mun.setHead(null);
											mun.save();
											sendViewData();
											StateUtil.announce(null, AnnounceLevel.MUNICIPALITY_ALL, translate("states.announce.municipality.mayor_reset"), mun.getId());
											Print.log(StateLogger.player(player) + " reset mayor of " + StateLogger.municipality(mun) + ".");
											return;
										}
										GameProfile gp = Static.getServer().getPlayerProfileCache().getGameProfileForUsername(value);
										if(gp == null || gp.getId() == null){
											sendStatus("states.manager_gui.view.player_not_found_cache");
											break;
										}
										if(!mun.getCouncil().contains(gp.getId()) && !mun.getCitizen().contains(gp.getId())){
											sendStatus("states.manager_gui.view.player_not_council_or_citizen");
											break;
										}
										mun.setHead(gp.getId());
										mun.setChanged(Time.getDate());
										mun.save();
										sendViewData();
										Print.log(StateLogger.player(player) + " changed mayor of " + StateLogger.municipality(mun) + " to " + StateLogger.player(gp) + ".");
									}
									else sendStatus(null);
									break;
								}
								case "price":{
									if(mun.isAuthorized(mun.r_SET_PRICE.id, cap.getUUID()).isTrue() || bypass(player)){
										try{
											Long price = Long.parseLong(value);
											if(price < 0){ price = 0l; }
											mun.setPrice(price);
											mun.setChanged(Time.getDate());
											mun.save();
											sendViewData();
											Print.log(StateLogger.player(player) + " changed price of " + StateLogger.municipality(mun) + " to " + mun.getPrice() + ".");
										}
										catch(Exception e){
											sendStatus("&cError: &7" + e.getMessage());
										}
									}
									else sendStatus(null);
									break;
								}
								case "color":{
									if(mun.isAuthorized(mun.r_COLOR.id, cap.getUUID()).isTrue() || bypass(player)){
										try{
											String str = value;
											if(str.replace("#", "").length() != 6){
												sendStatus("states.manager_gui.view.invalid_hex");
												break;
											}
											str = str.startsWith("#") ? str : "#" + str;
											Color.decode(str);
											mun.setColor(str);
											mun.setChanged(Time.getDate());
											mun.save();
											sendViewData();
											Print.log(StateLogger.player(player) + " changed color of " + StateLogger.municipality(mun) + " to " + mun.getColor() + ".");
										}
										catch(Exception e){
											sendStatus("&2Error: &7" + e.getMessage());
										}
									}
									else sendStatus(null);
									break;
								}
								case "citizen":{
									openGui(Layer.MUNICIPALITY, Mode.LIST_CITIZENS, mun.getId());
									break;
								}
								case "citizen_tax":{
									if(mun.isAuthorized(mun.r_SET_CITIZENTAX.id, cap.getUUID()).isTrue() || bypass(player)){
										if(value.equals("reset") || value.equals("disable")){
											mun.setCitizenTax(0);
											mun.setChanged(Time.getDate());
											mun.save();
											sendViewData();
										}
										else if(NumberUtils.isCreatable(value)){
											mun.setCitizenTax(Long.parseLong(value));
											mun.setChanged(Time.getDate());
											mun.save();
											sendViewData();
										}
										else{
											sendStatus("states.manager_gui.view.not_number");
										}
									}
									else sendStatus(null);
									break;
								}
								case "council":{
									openGui(Layer.MUNICIPALITY, Mode.LIST_COUNCIL, mun.getId());
									break;
								}
								case "districts":{
									openGui(Layer.MUNICIPALITY, Mode.LIST_COMPONENTS, mun.getId());
									break;
								}
								case "neighbors":{
									openGui(Layer.MUNICIPALITY, Mode.LIST_NEIGHBORS, mun.getId());
									break;
								}
								case "opentojoin":{
									if(mun.isAuthorized(mun.r_OPEN.id, cap.getUUID()).isTrue() || bypass(player)){
										mun.r_OPEN.set(!mun.r_OPEN.get());
										mun.setChanged(Time.getDate());
										mun.save();
										sendViewData();
										Print.log(StateLogger.player(player) + " set " + StateLogger.municipality(mun) + " to " + (mun.r_OPEN.get() ? "OPEN" : "CLOSED") + ".");
									}
									else sendStatus(null);
									break;
								}
								case "kickbankrupt":{
									if(mun.isAuthorized(mun.r_KIB.id, cap.getUUID()).isTrue() || bypass(player)){
										mun.r_KIB.set(!mun.r_KIB.get());
										mun.setChanged(Time.getDate());
										mun.save();
										sendViewData();
										Print.log(StateLogger.player(player) + " changed 'kick-if-brankrupt' of " + StateLogger.municipality(mun) + " to " + mun.r_OPEN.get() + ".");
									}
									else sendStatus(null);
									break;
								}
								case "ruleset":{
									if(mun.isAuthorized(mun.r_SET_RULESET.id, cap.getUUID()).isTrue() || bypass(player)){
										value = value.trim();
										if(value.replace(" ", "").length() < 3){
											sendStatus("states.manager_gui.view.name_short");
											break;
										}
										mun.setRulesetTitle(value);
										mun.setChanged(Time.getDate());
										mun.save();
										sendViewData();
										Print.log(StateLogger.player(player) + " changed ruleset name of " + StateLogger.municipality(mun) + " to " + mun.getName() + ".");
									}
									else sendStatus(null);
									break;
								}
								case "mailbox":{
									if(mun.isAuthorized(mun.r_SET_MAILBOX.id, cap.getUUID()).isTrue() || bypass(player)){
										mun.setMailbox(null);
										mun.setChanged(Time.getDate());
										mun.save();
										sendViewData();
										Print.log(StateLogger.player(player) + " reset mailbox location of" + StateLogger.municipality(mun) + ".");
									}
									else sendStatus(null);
									break;
								}
								case "icon":{
									if(mun.isAuthorized(mun.r_ICON.id, cap.getUUID()).isTrue() || bypass(player)){
										try{
											mun.setIcon(value);
											mun.setChanged(Time.getDate());
											mun.save();
											sendViewData();
											Print.log(StateLogger.player(player) + " changed icon of " + StateLogger.municipality(mun) + " to " + mun.getIcon() + ".");
										}
										catch(Exception e){
											sendStatus("&2Error: &7" + e.getMessage());
										}
									}
									else sendStatus(null);
									break;
								}
							}
							break;
						case PLAYERDATA:
							break;
						case PROPERTY:
							break;
						case STATE:
							break;
						case UNION:
							break;
						default:
							break;
					}
					break;
				}
			}
		}
	}

	private void openGui(Layer layer, Mode mode, int id){
		GuiHandler.openGui(player, layer.ordinal() + 2, mode.ordinal(), id, 0);
	}

	private void sendStatus(String reason){
		NBTTagCompound packet = new NBTTagCompound();
		packet.setString("cargo", "status_msg");
		packet.setString("msg", reason == null ? "states.manager_gui.view.no_perm" : reason);
		this.send(Side.CLIENT, packet);
	}

	private void readInit(NBTTagList list){
		keys = new String[list.tagCount()];
		if(mode == Mode.INFO){
			view_values = new String[list.tagCount()];
			view_modes = new ViewMode[list.tagCount()];
		}
		for(int i = 0; i < list.tagCount(); i++){
			String[] arr = list.getStringTagAt(i).split(";");
			keys[i] = arr[0];
			if(mode == Mode.INFO){
				view_values[i] = arr[1];
				view_modes[i] = ViewMode.values()[Integer.parseInt(arr[2])];
			}
		}
	}

	public void set(ManagerGui managerGui){
		this.gui = managerGui;
	}
	
	public static enum Mode {
		
		INFO,
		LIST_COMPONENTS,
		LIST_CITIZENS,
		LIST_COUNCIL,
		LIST_NEIGHBORS,
		LIST_BWLIST;

		public int entries(){
			return this != INFO ? 10 : 12;
		}
		
	}
	
	public static enum Layer {
		
		DISTRICT,
		MUNICIPALITY,
		STATE,
		UNION,
		COMPANY,
		PLAYERDATA,
		CHUNK,
		PROPERTY;
		
	}
	
	public static enum ViewMode {
		
		NONE,
		BOOL,
		LIST,
		EDIT,
		RESET,
		GOTO
		
	}

	public String getLayerTitle(){
		switch(layer){
			case CHUNK:
				return chunk.xCoord() + "x " + chunk.zCoord() + "z";
			case COMPANY:
				return "//TODO";
			case DISTRICT:
				return dis.getName();
			case MUNICIPALITY:
				return mun.getName();
			case PLAYERDATA:
				return cap.getFormattedNickname();
			case PROPERTY:
				return "//TODO";
			case STATE:
				return state.getName();
			case UNION:
				return "//TODO";
			default:
				return "NONE";
		}
	}

}
