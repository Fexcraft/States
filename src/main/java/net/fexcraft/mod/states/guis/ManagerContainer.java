package net.fexcraft.mod.states.guis;

import static net.fexcraft.mod.states.util.StateUtil.bypass;
import static net.fexcraft.mod.states.util.StateUtil.translate;

import java.awt.Color;
import java.util.ArrayList;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.mojang.authlib.GameProfile;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.gui.GenericContainer;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.states.data.Chunk;
import net.fexcraft.mod.states.data.ChunkPos;
import net.fexcraft.mod.states.data.ChunkType;
import net.fexcraft.mod.states.data.District;
import net.fexcraft.mod.states.data.DistrictType;
import net.fexcraft.mod.states.data.Municipality;
import net.fexcraft.mod.states.data.MunicipalityType;
import net.fexcraft.mod.states.data.State;
import net.fexcraft.mod.states.data.capabilities.PlayerCapability;
import net.fexcraft.mod.states.data.capabilities.StatesCapabilities;
import net.fexcraft.mod.states.data.root.AnnounceLevel;
import net.fexcraft.mod.states.data.root.Mailbox.MailType;
import net.fexcraft.mod.states.data.root.Mailbox.RecipientType;
import net.fexcraft.mod.states.util.MailUtil;
import net.fexcraft.mod.states.util.Perms;
import net.fexcraft.mod.states.util.StConfig;
import net.fexcraft.mod.states.util.StateLogger;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;

public class ManagerContainer extends GenericContainer {
	
	protected static final String NOTAX = "no_tax";
	protected static final String NOMAILBOX = "no_mailbox";
	protected static final String NOONE = "no_one";
	protected static final String NOTFORSALE = "not_for_sale";
	protected static final String NONE = "none";
	protected static final String UNKNOWN = "unknown";
	protected static final String NOTHING = "nothing";
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
	protected Object[] list_values;
	protected String[] view_values;
	protected ViewMode[] view_modes;

	public ManagerContainer(EntityPlayer player, int layerid, int x, int y, int z){
		super(player);
		mode = Mode.values()[x];
		layer = Layer.values()[layerid];
		Print.debug("CREATED " + player.world.isRemote + " " + layer + "/" + layerid + " " + mode + "/" + x);
		if(player.world.isRemote){
			if(layer.isChunk() && mode == Mode.CKINFO) mode = Mode.INFO;
			return;
		}
		chunk = StateUtil.getChunk(player);
		cap = player.getCapability(StatesCapabilities.PLAYER, null);
		switch(layer){
			case CHUNK:
				if(mode != Mode.INFO){
					if(mode == Mode.CKINFO) mode = Mode.INFO;
					chunk = StateUtil.getTempChunk(y, z);
				}
				break;
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
		packet.setString("layer_title", getLayerInfoTitle());
		NBTTagList list = new NBTTagList();
		switch(layer){
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
				addKey(list, "blacklist", mun.getPlayerBlacklist().size(), ViewMode.LIST);
				addKey(list, "icon", mun.getIcon(), ViewMode.EDIT);
				if(!mun.isAbandoned()){
					addKey(list, "abandoned", mun.isAbandoned(), ViewMode.NONE);
				}
				break;
			case STATE:
				addKey(list, "id", state.getId(), ViewMode.NONE);
				addKey(list, "name", state.getName(), ViewMode.EDIT);
				addKey(list, "capital", StateUtil.getMunicipality(state.getCapitalId()).getName() + " (" + state.getCapitalId() + ")", ViewMode.EDIT);
				addKey(list, "leader", state.getHead() == null ? NOONE : Static.getPlayerNameByUUID(state.getHead()), ViewMode.EDIT);
				addKey(list, "price", state.getPrice() > 0 ? ggas(state.getPrice()) : NOTFORSALE, ViewMode.EDIT);
				addKey(list, "color", state.getColor(), ViewMode.EDIT);
				addKey(list, "citizen", getCitizens(state).size(), ViewMode.LIST);
				addKey(list, "balance", ggas(state.getAccount().getBalance()), ViewMode.GOTO);
				addKey(list, "chunk_tax", state.getChunkTaxPercentage() + "%", ViewMode.EDIT);
				addKey(list, "citizen_tax", state.getCitizenTaxPercentage() + "%", ViewMode.EDIT);
				addKey(list, "last_edited", time(state.getChanged()), ViewMode.NONE);
				addKey(list, "council", state.getCouncil().size(), ViewMode.LIST);
				addKey(list, "municipalities", state.getMunicipalities().size(), ViewMode.LIST);
				addKey(list, "neighbors", state.getNeighbors().size(), ViewMode.LIST);
				addKey(list, "creator", Static.getPlayerNameByUUID(state.getCreator()), ViewMode.NONE);
				addKey(list, "created", time(state.getCreated()), ViewMode.NONE);
				addKey(list, "ruleset", state.getRulesetTitle(), ViewMode.EDIT);
				addKey(list, "mailbox", state.getMailbox() == null ? NOMAILBOX : state.getMailbox().toString(), ViewMode.RESET);
				addKey(list, "blacklist", state.getBlacklist().size(), ViewMode.LIST);
				addKey(list, "icon", state.getIcon(), ViewMode.EDIT);
				break;
			case UNION:
				break;
			case COMPANY:
				//TODO
				break;
			case PLAYERDATA:
				addKey(list, "uuid", cap.getUUIDAsString(), ViewMode.NONE);
				addKey(list, "nick", cap.getRawNickname() == null ? NONE : cap.getRawNickname(), ViewMode.EDIT);
				addKey(list, "chatcolor", cap.getNicknameColor(), ViewMode.EDIT);
				addKey(list, "municipality", cap.getMunicipality().getName() + " (" + cap.getMunicipality().getId() + ")", ViewMode.GOTO);
				addKey(list, "custom_tax", cap.getCustomTax() > 0 ? ggas(cap.getCustomTax()) : NONE, ViewMode.RESET);
				addKey(list, "balance", ggas(cap.getAccount().getBalance()), ViewMode.GOTO);
				addKey(list, "bank", cap.getBank().getName(), ViewMode.GOTO);
				addKey(list, "mailbox", cap.getMailbox() == null ? NOMAILBOX : cap.getMailbox().toString(), ViewMode.RESET);
				break;
			case CHUNK:
				addKey(list, "coords", chunk.xCoord() + ", " + chunk.zCoord(), ViewMode.NONE);
				addKey(list, "district", chunk.getDistrict().getName() + " (" + chunk.getDistrict().getId() + ")", ViewMode.GOTO);
				addKey(list, "owner", chunk.getType() == ChunkType.PRIVATE ? Static.getPlayerNameByUUID(chunk.getOwner()) : chunk.getOwner(), chunk.getType() == ChunkType.PRIVATE ? ViewMode.NONE : ViewMode.GOTO);
				addKey(list, "price", chunk.getPrice() > 0 ? ggas(chunk.getPrice()) : NOTFORSALE, ViewMode.EDIT);
				addKey(list, "tax", chunk.getCustomTax() > 0 ? ggas(chunk.getCustomTax()) + "c" : chunk.getDistrict().getChunkTax() > 0 ? ggas(chunk.getDistrict().getChunkTax()) + "d" : NOTAX, ViewMode.EDIT);
				addKey(list, "type", chunk.getType().name().toLowerCase(), ViewMode.EDIT);
				addKey(list, "last_edited", time(chunk.getChanged()), ViewMode.NONE);
				addKey(list, "last_taxcoll", time(chunk.lastTaxCollection()), ViewMode.NONE);
				addKey(list, "linked_chunks", chunk.getLinkedChunks().size() > 0 ? chunk.getLinkedChunks().size() : NONE, ViewMode.LIST);
				addKey(list, "linked_to", chunk.getLink() == null ? NOTHING : chunk.getLink().x + ", " + chunk.getLink().z, ViewMode.GOTO);
				addKey(list, "whitelist", chunk.getPlayerWhitelist().size(), ViewMode.LIST);
				addKey(list, "claimed_by", Static.getPlayerNameByUUID(chunk.getClaimer()), ViewMode.NONE);
				addKey(list, "claimed_at", time(chunk.getCreated()), ViewMode.NONE);
				if(chunk.getDistrict().getId() == -2){
					addKey(list, "transit", time(chunk.getChanged() + Time.DAY_MS), ViewMode.NONE);
				}
				if(chunk.isForceLoaded()){
					addKey(list, "forceloaded", "true", ViewMode.NONE);
				}
				break;
			case PROPERTY:
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
	
	public void sendListData(){
		NBTTagCompound packet = new NBTTagCompound();
		packet.setString("cargo", "init");
		packet.setString("layer_title", getLayerListTitle());
		NBTTagList list = new NBTTagList();
		switch(mode){
			case LIST_BWLIST:
				if(layer.isChunk()){
					initListValues(chunk.getPlayerWhitelist().size());
					for(UUID uuid : chunk.getPlayerWhitelist()) addKey(list, uuid, uuid);
					//TODO company white list
				}
				if(layer.isMunicipality()){
					initListValues(mun.getPlayerBlacklist().size());
					for(UUID uuid : mun.getPlayerBlacklist()) addKey(list, uuid, uuid);
				}
				if(layer.isState()){
					initListValues(state.getBlacklist().size());
					for(int id : state.getBlacklist()) addKey(list, id, "company:" + id);
				}
				break;
			case LIST_CITIZENS:
				if(layer.isMunicipality()){
					initListValues(mun.getCitizen().size());
					for(UUID uuid : mun.getCitizen()) addKey(list, uuid, uuid);
				}
				if(layer.isState()){
					ArrayList<UUID> citizen = getCitizens(state);
					initListValues(citizen.size());
					for(UUID uuid : citizen) addKey(list, uuid, uuid);
				}
				break;
			case LIST_COMPONENTS:
				if(layer.isChunk()){
					initListValues(chunk.getLinkedChunks().size());
					for(ResourceLocation pos : chunk.getLinkedChunks()){
						addKey(list, pos, pos.getNamespace() + ", " + pos.getPath());
					}
				}
				if(layer.isMunicipality()){
					initListValues(mun.getDistricts().size());
					for(int id : mun.getDistricts()){
						addKey(list, id, StateUtil.getDistrictName(id));
					}
				}
				if(layer.isState()){
					initListValues(state.getMunicipalities().size());
					for(int id : state.getMunicipalities()){
						addKey(list, id, StateUtil.getMunicipalityName(id));
					}
				}
				break;
			case LIST_COUNCIL:
				if(layer.isMunicipality()){
					initListValues(mun.getCouncil().size());
					for(UUID uuid : mun.getCouncil()) addKey(list, uuid, uuid);
				}
				if(layer.isState()){
					initListValues(state.getCouncil().size());
					for(UUID uuid : state.getCouncil()) addKey(list, uuid, uuid);
				}
				break;
			case LIST_NEIGHBORS:
				if(layer.isDistrict()){
					initListValues(dis.getNeighbors().size());
					for(int id : dis.getNeighbors()){
						addKey(list, id, StateUtil.getDistrictName(id));
					}
				}
				if(layer.isMunicipality()){
					initListValues(mun.getNeighbors().size());
					for(int id : mun.getNeighbors()){
						addKey(list, id, StateUtil.getMunicipalityName(id));
					}
				}
				if(layer.isState()){
					initListValues(state.getNeighbors().size());
					for(int id : state.getNeighbors()){
						addKey(list, id, StateUtil.getStateName(id));
					}
				}
				break;
			default: return;
		}
		readInit(list);
		packet.setTag("keys", list);
		this.send(Side.CLIENT, packet);
	}
	
	private int listinitindex = 0;
	
	private void initListValues(int amount){
		list_values = new Object[amount];
		listinitindex = 0;
	}

	private void addKey(NBTTagList list, Object rawval, Object key){
		list_values[listinitindex++] = rawval;
		if(key instanceof UUID){
			key = Static.getPlayerNameByUUID((UUID)key);
		}
		list.appendTag(new NBTTagString(key.toString()));
	}
	
	public static final String ggas(long value){
		return Config.getWorthAsString(value, false);
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
					if(mode.isInfo()){
						sendViewData();
					}
					else{
						sendListData();
					}
					break;
				}
				case "view_mode_click":{
					if(mode != Mode.INFO) return;
					String value = packet.hasKey("value") ? packet.getString("value") : null;
					switch(layer){
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
											Print.log(StateLogger.player(player) + " set the 'chunk-tax' of " + StateLogger.district(dis) + " to " + dis.getChunkTax());
										}
										else if(NumberUtils.isCreatable(value)){
											dis.setChunkTax(Long.parseLong(value));
											dis.setChanged(Time.getDate());
											dis.save();
											sendViewData();
											Print.log(StateLogger.player(player) + " set the 'chunk-tax' of " + StateLogger.district(dis) + " to " + dis.getChunkTax());
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
											Print.log(StateLogger.player(player) + " set the 'citizen-tax' of " + StateLogger.municipality(mun) + " to " + mun.getCitizenTax());
										}
										else if(NumberUtils.isCreatable(value)){
											mun.setCitizenTax(Long.parseLong(value));
											mun.setChanged(Time.getDate());
											mun.save();
											sendViewData();
											Print.log(StateLogger.player(player) + " set the 'citizen-tax' of " + StateLogger.municipality(mun) + " to " + mun.getCitizenTax());
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
								case "blacklist":{
									openGui(Layer.MUNICIPALITY, Mode.LIST_BWLIST, mun.getId());
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
						case STATE:
							switch(keys[packet.getInteger("button")]){
								case "name":{
									if(state.isAuthorized(state.r_SET_NAME.id, cap.getUUID()).isTrue() || bypass(player)){
										if(value.replace(" ", "").length() < 3){
											sendStatus("states.manager_gui.view.name_short");
											break;
										}
										state.setName(value);
										state.setChanged(Time.getDate());
										state.save();
										sendViewData();
										Print.log(StateLogger.player(player) + " set the name of " + StateLogger.state(state) + " to " + state.getName());
									}
									else sendStatus(null);
									break;
								}
								case "capital":{
									if(state.isAuthorized(state.r_SET_CAPITAL.id, cap.getUUID()).isTrue() || bypass(player)){
										Municipality mun = StateUtil.getMunicipality(Integer.parseInt(value));
										if(mun.getId() <= 0 || mun.getState().getId() != state.getId()){
											sendStatus("states.manager_gui.view.municipality_not_in_state");
											break;
										}
										state.setCapitalId(mun.getId());
										state.setChanged(Time.getDate());
										state.save();
										sendViewData();
										StateUtil.announce(null, AnnounceLevel.STATE_ALL, translate("states.announce.state.new_capital", mun.getId()), state.getId());
										Print.log(StateLogger.player(player) + " set the capital of " + StateLogger.state(state) + " to " + StateLogger.municipality(mun));
										return;
									}
									else sendStatus(null);
									break;
								}
								case "leader":{
									if(state.isAuthorized(state.r_SET_LEADER.id, cap.getUUID()).isTrue() || bypass(player)){
										if(value.equals("null") || value.equals("reset")){
											if(state.isAuthorized(state.r_RESET_HEAD.id, cap.getUUID()).isFalse() && !bypass(player)){
												sendStatus(null);
												return;
											}
											state.setHead(null);
											state.save();
											sendViewData();
											StateUtil.announce(null, AnnounceLevel.MUNICIPALITY_ALL, translate("states.announce.state.leader_reset"), state.getId());
											Print.log(StateLogger.player(player) + " reset head of " + StateLogger.state(state) + ".");
											return;
										}
										GameProfile gp = Static.getServer().getPlayerProfileCache().getGameProfileForUsername(value);
										if(gp == null || gp.getId() == null){
											sendStatus("states.manager_gui.view.player_not_found_cache");
											break;
										}
										if(!state.getCouncil().contains(gp.getId()) && !getCitizens(state).contains(gp.getId())){
											sendStatus("states.manager_gui.view.player_not_council_or_citizen");
											break;
										}
										state.setHead(gp.getId());
										state.setChanged(Time.getDate());
										state.save();
										sendViewData();
										Print.log(StateLogger.player(player) + " changed leader of " + StateLogger.state(state) + " to " + StateLogger.player(gp) + ".");
									}
									else sendStatus(null);
									break;
								}
								case "price":{
									if(state.isAuthorized(state.r_SET_PRICE.id, cap.getUUID()).isTrue() || bypass(player)){
										try{
											Long price = Long.parseLong(value);
											if(price < 0){ price = 0l; }
											state.setPrice(price);
											state.setChanged(Time.getDate());
											state.save();
											sendViewData();
											Print.log(StateLogger.player(player) + " set the price of " + StateLogger.state(state) + " to " + state.getPrice());
										}
										catch(Exception e){
											sendStatus("&cError: &7" + e.getMessage());
										}
									}
									else sendStatus(null);
									break;
								}
								case "color":{
									if(state.isAuthorized(state.r_SET_COLOR.id, cap.getUUID()).isTrue() || bypass(player)){
										try{
											String str = value;
											if(str.replace("#", "").length() != 6){
												sendStatus("states.manager_gui.view.invalid_hex");
												break;
											}
											str = str.startsWith("#") ? str : "#" + str;
											Color.decode(str);
											state.setColor(str);
											state.setChanged(Time.getDate());
											state.save();
											sendViewData();
											Print.log(StateLogger.player(player) + " set the color of " + StateLogger.state(state) + " to " + state.getColor());
										}
										catch(Exception e){
											sendStatus("&2Error: &7" + e.getMessage());
										}
									}
									else sendStatus(null);
									break;
								}
								case "citizen":{
									openGui(Layer.STATE, Mode.LIST_CITIZENS, state.getId());
									break;
								}
								case "chunk_tax":{
									if(state.isAuthorized(state.r_SET_CHUNK_TAX_PERCENT.id, cap.getUUID()).isTrue() || bypass(player)){
										if(value.equals("reset") || value.equals("disable")){
											state.setChunkTaxPercentage((byte)0);
											state.setChanged(Time.getDate());
											state.save();
											sendViewData();
											Print.log(StateLogger.player(player) + " set the 'chunk-tax-percentage' of " + StateLogger.state(state) + " to " + state.getChunkTaxPercentage());
										}
										else if(NumberUtils.isCreatable(value)){
											byte byt = Byte.parseByte(value);
											if(byt > 100){ byt = 100; } if(byt < 0){ byt = 0; }
											state.setChunkTaxPercentage(byt);
											state.setChanged(Time.getDate());
											state.save();
											sendViewData();
											Print.log(StateLogger.player(player) + " set the 'chunk-tax-percentage' of " + StateLogger.state(state) + " to " + state.getChunkTaxPercentage());
										}
										else{
											sendStatus("states.manager_gui.view.not_number");
										}
									}
									else sendStatus(null);
									break;
								}
								case "citizen_tax":{
									if(state.isAuthorized(state.r_SET_CITIZEN_TAX_PERCENT.id, cap.getUUID()).isTrue() || bypass(player)){
										if(value.equals("reset") || value.equals("disable")){
											state.setCitizenTaxPercentage((byte)0);
											state.setChanged(Time.getDate());
											state.save();
											sendViewData();Print.log(StateLogger.player(player) + " set the 'citizen-tax-percentage' of " + StateLogger.state(state) + " to " + state.getChunkTaxPercentage());
										}
										else if(NumberUtils.isCreatable(value)){
											byte byt = Byte.parseByte(value);
											if(byt > 100){ byt = 100; } if(byt < 0){ byt = 0; }
											state.setCitizenTaxPercentage(byt);
											state.setChanged(Time.getDate());
											state.save();
											sendViewData();
											Print.log(StateLogger.player(player) + " set the 'citizen-tax-percentage' of " + StateLogger.state(state) + " to " + state.getChunkTaxPercentage());
										}
										else{
											sendStatus("states.manager_gui.view.not_number");
										}
									}
									else sendStatus(null);
									break;
								}
								case "council":{
									openGui(Layer.STATE, Mode.LIST_COUNCIL, state.getId());
									break;
								}
								case "municipalities":{
									openGui(Layer.STATE, Mode.LIST_COMPONENTS, state.getId());
									break;
								}
								case "neighbors":{
									openGui(Layer.STATE, Mode.LIST_NEIGHBORS, state.getId());
									break;
								}
								case "ruleset":{
									if(state.isAuthorized(state.r_SET_RULESET.id, cap.getUUID()).isTrue() || bypass(player)){
										if(value.replace(" ", "").length() < 3){
											sendStatus("states.manager_gui.view.name_short");
											break;
										}
										state.setRulesetTitle(value);
										state.setChanged(Time.getDate());
										state.save();
										sendViewData();
										Print.log(StateLogger.player(player) + " set the ruleset name of " + StateLogger.state(state) + " to " + state.getName());
									}
									else sendStatus(null);
									break;
								}
								case "mailbox":{
									if(state.isAuthorized(state.r_SET_MAILBOX.id, cap.getUUID()).isTrue() || bypass(player)){
										state.setMailbox(null);
										state.setChanged(Time.getDate());
										state.save();
										sendViewData();
										Print.log(StateLogger.player(player) + " reset mailbox location of" + StateLogger.state(state) + ".");
									}
									else sendStatus(null);
									break;
								}
								case "blacklist":{
									openGui(Layer.STATE, Mode.LIST_BWLIST, state.getId());
									break;
								}
								case "icon":{
									if(state.isAuthorized(state.r_SET_ICON.id, cap.getUUID()).isTrue() || bypass(player)){
										try{
											state.setIcon(value);
											state.setChanged(Time.getDate());
											state.save();
											sendViewData();
											Print.log(StateLogger.player(player) + " set the icon of " + StateLogger.state(state) + " to " + state.getIcon());
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
						case UNION:
							break;
						case COMPANY:
							break;
						case PLAYERDATA:
							switch(keys[packet.getInteger("button")]){
								case "nick":{
									if(!Perms.NICKNAME_CHANGE_SELF.has(player)){
										sendStatus(null);
										return;
									}
									if(value.length() > StConfig.NICKNAME_LENGTH){
										sendStatus("states.manager_gui.view.nick_too_long");
									}
									else if(value.length() < 1){
										sendStatus("states.manager_gui.view.nick_too_short");
									}
									else{
										cap.setRawNickname(value);
										cap.save();
										sendStatus("states.manager_gui.view.change_applied");
									}
									break;
								}
								case "chatcolor":{
									if(!Perms.NICKNAME_CHANGE_SELF.has(player)){
										sendStatus(null);
										return;
									}
									cap.setNicknameColor(Integer.parseInt(value, StringUtils.indexOfAny(value, new String[]{"a", "b", "c", "d", "e", "f"}) >= 0 ? 16 : 10));
									cap.save();
									sendStatus("states.manager_gui.view.change_applied");
									break;
								}
								case "municipality":{
									openGui(Layer.MUNICIPALITY, Mode.INFO, cap.getMunicipality().getId());
									break;
								}
								case "mailbox":{
									cap.setMailbox(null);
									cap.save();
									sendViewData();
									break;
								}
							}
							break;
						case CHUNK:
							switch(keys[packet.getInteger("button")]){
								case "district":{
									openGui(Layer.DISTRICT, Mode.INFO, chunk.getDistrict().getId());
									break;
								}
								case "owner":{
									switch(chunk.getType()){
										case COMPANY:
											//TODO
											break;
										case DISTRICT:
											openGui(Layer.DISTRICT, Mode.INFO, chunk.getDistrict().getId());
											break;
										case MUNICIPAL:
											openGui(Layer.MUNICIPALITY, Mode.INFO, chunk.getMunicipality().getId());
											break;
										case STATEOWNED:
											openGui(Layer.STATE, Mode.INFO, chunk.getState().getId());
											break;
										default: return;
									}
									break;
								}
								case "price":{
									if(isPermitted(chunk, player)){
										try{
											Long price = Long.parseLong(value);
											chunk.setPrice(price);
											chunk.setChanged(Time.getDate());
											chunk.save();
											sendViewData();
											Print.log(StateLogger.player(player) + " set the price of the " + StateLogger.chunk(chunk) + " to " + chunk.getPrice() + ".");
										}
										catch(Exception e){
											sendStatus("&9Error: &7" + e.getMessage());
										}
									}
									break;
								}
								case "tax":{
									if(chunk.getDistrict().isAuthorized(chunk.getDistrict().r_SET_CUSTOM_CHUNKTAX.id, cap.getUUID()).isTrue()){
										if(value.equals("reset") || value.equals("disable")){
											chunk.setCustomTax(0);
											chunk.save();
											sendViewData();
											Print.log(StateLogger.player(player) + " reset the custom-tax of " + StateLogger.chunk(chunk) + ".");
										}
										else if(NumberUtils.isCreatable(value)){
											chunk.setCustomTax(Long.parseLong(value));
											chunk.save();
											sendViewData();
											Print.log(StateLogger.player(player) + " set the custom-tax of " + StateLogger.chunk(chunk) + " to " + chunk.getCustomTax() + ".");
										}
										else{
											sendStatus("states.manager_gui.view.not_number");
										}
									}
									break;
								}
								case "type":{
									if(isPermitted(chunk, player)){
										ChunkType type = ChunkType.get(value.toUpperCase());
										if(type == null){
											sendStatus("states.manager_gui.view_chunk.type.not_found0");
											Print.chat(player, translate("states.manager_gui.view_chunk.type.not_found1"));
										}
										else{
											long time = Time.getDate();
											switch(type){
												case COMPANY:{
													sendStatus("states.manager_gui.view_chunk.type.sale_only");
													break;
												}
												case STATEOWNED:
												case MUNICIPAL:
												case DISTRICT:
												case NORMAL:{
													String to = type == ChunkType.NORMAL || type == ChunkType.DISTRICT ? "District" : type == ChunkType.MUNICIPAL ? "Municipality" : type == ChunkType.STATEOWNED ? "State" : "ERROR";
													chunk.setType(type);
													chunk.setOwner(null);
													chunk.setPrice(0);
													chunk.setChanged(time);
													chunk.getLinkedChunks().forEach(link -> {
														Chunk ck = StateUtil.getTempChunk(link);
														ck.setType(type);
														ck.setOwner(null);
														ck.setPrice(0);
														ck.setChanged(time);
														ck.save();
														Print.log(StateLogger.player(player) + " gave the linked " + StateLogger.chunk(ck) + " to the " + to + ".");
													});
													chunk.save();
													sendViewData();
													Print.log(StateLogger.player(player) + " gave the  " + StateLogger.chunk(chunk) + " to the " + to + ".");
													break;
												}
												case PRIVATE:{
													sendStatus("states.manager_gui.view_chunk.type.sale_only");
													break;
												}
												case PUBLIC:{
													chunk.setType(type);
													chunk.setChanged(time);
													chunk.getLinkedChunks().forEach(link -> {
														Chunk ck = StateUtil.getTempChunk(link);
														ck.setType(type);
														ck.setChanged(time);
														ck.save();
														Print.log(StateLogger.player(player) + " set the type of linked " + StateLogger.chunk(ck) + " to PUBLIC.");
													});
													chunk.save();
													sendViewData();
													Print.chat(player, translate("states.manager_gui.view_chunk.type.public0"));
													Print.chat(player, translate("states.manager_gui.view_chunk.type.public1"));
													Print.log(StateLogger.player(player) + " set the type of " + StateLogger.chunk(chunk) + " to PUBLIC.");
													break;
												}
												default:{
													sendStatus("ERROR:INVALID_REQUEST_TYPE");
													break;
												}
											}
										}
									}
									break;
								}
								case "linked_chunks":{
									openGui(Layer.CHUNK, Mode.LIST_COMPONENTS, chunk.xCoord(), chunk.zCoord());
									break;
								}
								case "linked_to":{
									if(chunk.getLink() == null) return;
									openGui(Layer.CHUNK, Mode.CKINFO, chunk.getLink().x, chunk.getLink().z);
									break;
								}
								case "whitelist":{
									openGui(Layer.CHUNK, Mode.LIST_BWLIST, chunk.xCoord(), chunk.zCoord());
									break;
								}
							}
							break;
						case PROPERTY:
							break;
						default:
							break;
					}
					break;
				}
				case "list_mode_click":{
					switch(mode){
						case LIST_BWLIST:
							//do nothing for now
							break;
						case LIST_CITIZENS:
							//also nothing yet
							break;
						case LIST_COMPONENTS:
							if(layer.isChunk()){
								ResourceLocation resloc = chunk.getLinkedChunks().get(packet.getInteger("button"));
								openGui(Layer.CHUNK, Mode.CKINFO, Integer.parseInt(resloc.getNamespace()), Integer.parseInt(resloc.getPath()));
								return;
							}
							if(layer.isMunicipality()){
								openGui(Layer.DISTRICT, Mode.INFO, mun.getDistricts().get(packet.getInteger("button")));
								return;
							}
							if(layer.isState()){
								openGui(Layer.MUNICIPALITY, Mode.INFO, state.getMunicipalities().get(packet.getInteger("button")));
								return;
							}
							//TODO check if the component is loaded in memory?
							break;
						case LIST_COUNCIL:
							//nothing also yet
							break;
						case LIST_NEIGHBORS:
							if(layer.isDistrict()){
								openGui(Layer.DISTRICT, Mode.INFO, dis.getNeighbors().get(packet.getInteger("button")));
								return;
							}
							if(layer.isMunicipality()){
								openGui(Layer.MUNICIPALITY, Mode.INFO, mun.getNeighbors().get(packet.getInteger("button")));
								return;
							}
							if(layer.isState()){
								openGui(Layer.STATE, Mode.INFO, state.getNeighbors().get(packet.getInteger("button")));
								return;
							}
							//TODO check if the component is loaded in memory?
							break;
						default: return;
					}
					break;
				}
				case "list_mode_remove":{
					int index = packet.getInteger("button");
					switch(mode){
						case LIST_BWLIST:
							if(layer.isChunk()){
								if(isOwner(chunk, player)){
									chunk.getPlayerWhitelist().remove(list_values[index]);
									chunk.save();
									sendListData();
									return;
								}
								//TODO handling of companies
								return;
							}
							if(layer.isMunicipality()){
								if(mun.isAuthorized(mun.r_EDIT_BL.id, cap.getUUID()).isTrue() || bypass(player)){
									mun.getPlayerBlacklist().remove(list_values[index]);
									mun.save();
									sendListData();
									Print.log(StateLogger.player(player) + " removed " + (UUID)list_values[index] + " from the blacklist of " + StateLogger.municipality(mun) + ".");
								}
								else sendStatus(null);
								//TODO handling of companies
								return;
							}
							if(layer.isState()){
								if(state.isAuthorized(state.r_EDIT_BL.id, cap.getUUID()).isTrue() || bypass(player)){
									state.getBlacklist().remove(list_values[index]);
									state.save();
									sendListData();
									Print.log(StateLogger.player(player) + " removed " + list_values[index] + " from the blacklist of " + StateLogger.state(state) + ".");
								}
								else sendStatus(null);
								return;
							}
							break;
						case LIST_CITIZENS:
							if(layer.isMunicipality()){
								if(!(mun.isAuthorized(mun.r_KICK.id, cap.getUUID()).isTrue() || bypass(player))){
									sendStatus(null);
									return;
								}
								if(mun.getCouncil().contains(list_values[index])){
									Print.chat(player, "states.manager_gui.list.cannot_kick_council0");
									Print.chat(player, "states.manager_gui.list.cannot_kick_council1");
									sendStatus(null);
									return;
								}
								if(!mun.getCitizen().contains(list_values[index])){
									sendStatus("states.manager_gui.list.not_a_citizen");
									return;
								}
								mun.getCitizen().remove(list_values[index]);
								String kickmsg = translate("states.manager_gui.list_citizens_municipality.rem_msg", mun.getId());
								PlayerCapability playr = StateUtil.getPlayer((UUID)list_values[index], false);
								if(playr != null){ playr.setMunicipality(StateUtil.getMunicipality(-1)); }
								MailUtil.send(player, RecipientType.PLAYER, list_values[index].toString(), player.getGameProfile().getId().toString(), kickmsg, MailType.SYSTEM, Time.DAY_MS * 64);
								Print.log(StateLogger.player(player) + " kicked " + list_values[index].toString() + " from " + StateLogger.municipality(mun) + ".");
								sendListData();
								return;
							}
							break;
						case LIST_COMPONENTS:
							if(layer.isChunk()){
								if(!isOwner(chunk, player)){
									sendStatus(null);
									return;
								}
								Chunk linked = StateUtil.getTempChunk((ResourceLocation)list_values[index]);
								if(linked.getLink().x == chunk.xCoord() && linked.getChunkPos().z == chunk.zCoord()){
									linked.setLink(null);
									linked.save();
									Print.log(StateLogger.player(player) + " unlinked the " + StateLogger.chunk(chunk) + ".");
									sendListData();
								}
								else sendStatus("ERROR:[01]");
								return;
							}
							break;
						case LIST_COUNCIL:
							break;
						case LIST_NEIGHBORS: return;
						default: return;
					}
					break;
				}
				case "list_mode_add":{
					String input = packet.getString("input");
					switch(mode){
						case LIST_BWLIST:
							if(layer.isChunk()){
								if(isOwner(chunk, player)){
									GameProfile prof = Static.getServer().getPlayerProfileCache().getGameProfileForUsername(input);
									if(prof == null){
										sendStatus("states.manager_gui.view.player_not_found_cache");
										return;
									}
									if(chunk.getPlayerWhitelist().contains(prof.getId())){
										sendStatus("states.manager_gui.view.duplicate");
										return;
									}
									chunk.getPlayerWhitelist().add(prof.getId());
									chunk.save();
									sendListData();
									return;
								}
								return;
							}
							if(layer.isMunicipality()){
								if(mun.isAuthorized(mun.r_EDIT_BL.id, cap.getUUID()).isTrue() || bypass(player)){
									GameProfile prof = Static.getServer().getPlayerProfileCache().getGameProfileForUsername(input);
									if(prof == null){
										sendStatus("states.manager_gui.view.player_not_found_cache");
										return;
									}
									if(mun.getCouncil().contains(prof.getId())){
										Print.chat(player, "states.manager_gui.list.cannot_blacklist_council0");
										Print.chat(player, "states.manager_gui.list.cannot_blacklist_council1");
										sendStatus(null);
										return;
									}
									if(mun.getPlayerBlacklist().contains(prof.getId())){
										sendStatus("states.manager_gui.view.duplicate");
										return;
									}
									mun.getPlayerBlacklist().add(prof.getId());
									mun.save();
									sendListData();
									Print.log(StateLogger.player(player) + " added " + StateLogger.player(prof) + " to the blacklist of " + StateLogger.municipality(mun) + ".");
								}
								else sendStatus(null);
								//TODO handling of companies
								return;
							}
							if(layer.isState()){
								if(state.isAuthorized(state.r_EDIT_BL.id, cap.getUUID()).isTrue() || bypass(player)){
									//TODO company id validation
									if(!NumberUtils.isCreatable(input)){
										sendStatus("states.manager_gui.view.not_number");
										return;
									}
									state.getBlacklist().add(Integer.parseInt(input));
									state.save();
									sendListData();
									Print.log(StateLogger.player(player) + " added company" + input + " to the blacklist of " + StateLogger.state(state) + ".");
								}
								else sendStatus(null);
								return;
							}
							break;
						case LIST_CITIZENS:
							if(layer.isMunicipality()){
								if(!(mun.isAuthorized(mun.r_INVITE.id, cap.getUUID()).isTrue() || bypass(player))){
									sendStatus(null);
									return;
								}
								GameProfile gp = Static.getServer().getPlayerProfileCache().getGameProfileForUsername(input);
								if(gp == null || gp.getId() == null){
									sendStatus("states.manager_gui.view.player_not_found_cache");
									return;
								}
								if(mun.getCitizen().contains(gp.getId())){
									sendStatus("states.manager_gui.list.a_citizen");
									return;
								}
								String invmsg = translate("states.manager_gui.list_citizens_municipality.add_msg", mun.getName() + " (" + mun.getId() + ")");
								NBTTagCompound compound = new NBTTagCompound();
								compound.setString("type", "municipality");
								compound.setInteger("id", mun.getId());
								compound.setString("from", player.getGameProfile().getId().toString());
								compound.setLong("at", Time.getDate());
								MailUtil.send(player, RecipientType.PLAYER, gp.getId().toString(), player.getGameProfile().getId().toString(), invmsg, MailType.INVITE, Time.DAY_MS * 2, compound);
								Print.log(StateLogger.player(player) + " invited " + StateLogger.player(gp) + " to join "+ StateLogger.municipality(mun) + ".");
								sendListData();
								return;
							}
							break;
						case LIST_COMPONENTS:
							if(layer.isChunk()){
								String[] split = null;
								if(input.contains(":")) split = input.split(":");
								else if(input.contains(",")) split = input.split(",");
								else if(input.contains(" ")) split = input.split(" ");
								else{
									sendStatus("states.manager_gui.list_components_chunk.invalid_format");
									return;
								}
								int x = Integer.parseInt(split[0].trim());
								int z = Integer.parseInt(split[1].trim());
								Chunk ck = StateUtil.getChunk(x, z);
								if(!isOwner(ck, player)){
									sendStatus("states.manager_gui.list_components_chunk.not_owner");
									return;
								}
								ck.setLink(chunk.getChunkPos());
								ck.setChanged(Time.getDate());
								ck.save();
								chunk.getLinkedChunks().add(new ResourceLocation(x + "", z + ""));
								chunk.setChanged(Time.getDate());
								chunk.save();
								Print.log(StateLogger.player(player) + " linked " + StateLogger.chunk(ck) + " to " + StateLogger.chunk(chunk) + ".");
								sendListData();
								return;
							}
							break;
						case LIST_COUNCIL:
							break;
						case LIST_NEIGHBORS: return;
						default: return;
					}
					break;
				}
				case "list_mode_home":{
					if(layer.isChunk()){
						openGui(Layer.CHUNK, Mode.CKINFO, chunk.xCoord(), chunk.zCoord());
					}
					if(layer.isDistrict()){
						openGui(Layer.DISTRICT, Mode.INFO, dis.getId());
					}
					if(layer.isMunicipality()){
						openGui(Layer.MUNICIPALITY, Mode.INFO, mun.getId());
					}
					if(layer.isState()){
						openGui(Layer.STATE, Mode.INFO, state.getId());
					}
					break;
				}
			}
		}
	}

	private void openGui(Layer layer, Mode mode, int id){
		GuiHandler.openGui(player, layer.ordinal() + 2, mode.ordinal(), id, 0);
	}

	private void openGui(Layer layer, Mode mode, int x, int z){
		GuiHandler.openGui(player, layer.ordinal() + 2, mode.ordinal(), x, z);
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
			if(mode == Mode.INFO){
				String[] arr = list.getStringTagAt(i).split(";");
				keys[i] = arr[0];
				view_values[i] = arr[1];
				view_modes[i] = ViewMode.values()[Integer.parseInt(arr[2])];
			}
			else keys[i] = list.getStringTagAt(i);
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
		LIST_BWLIST,
		CKINFO;//to detect if a local chunk is opened or remote

		public int entries(){
			return this.isInfo() ? 12 : 10;
		}

		boolean isInfo(){
			return this == INFO || this == CKINFO;
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

		boolean isChunk(){
			return this == CHUNK;
		}

		boolean isDistrict(){
			return this == DISTRICT;
		}

		boolean isMunicipality(){
			return this == MUNICIPALITY;
		}

		boolean isState(){
			return this == STATE;
		}

		boolean isPlayer(){
			return this == PLAYERDATA;
		}
		
	}
	
	public static enum ViewMode {
		
		NONE,
		BOOL,
		LIST,
		EDIT,
		RESET,
		GOTO
		
	}

	public String getLayerInfoTitle(){
		switch(layer){
			case CHUNK:
				return chunk.xCoord() + ", " + chunk.zCoord();
			case COMPANY:
				return "//TODO";
			case DISTRICT:
				return dis.getName();
			case MUNICIPALITY:
				return mun.getName();
			case PLAYERDATA:
				return player.getName();
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

	public String getLayerListTitle(){
		switch(mode){
			case LIST_BWLIST:
				if(layer.isChunk()) return translate("states.manager_gui.title_whitelist");
				if(layer.isMunicipality()) return translate("states.manager_gui.title_blacklist");
				if(layer.isState()) return translate("states.manager_gui.title_blacklist");
				break;
			case LIST_CITIZENS:
				return translate("states.manager_gui.title_citizens");
			case LIST_COMPONENTS:
				if(layer.isChunk()) return translate("states.manager_gui.title_links");
				if(layer.isMunicipality()) return translate("states.manager_gui.title_ditricts");
				if(layer.isState()) return translate("states.manager_gui.title_municipalities");
				break;
			case LIST_COUNCIL:
				return translate("states.manager_gui.title_council");
			case LIST_NEIGHBORS:
				return translate("states.manager_gui.title_neighbors");
			default: break;
		}
		return "INVALID_TYPE";
	}

	private ArrayList<UUID> getCitizens(State state){
		ArrayList<UUID> list = new ArrayList<UUID>();
		for(int id : state.getMunicipalities()){
			Municipality mun = StateUtil.getMunicipality(id);
			if(mun.getId() == -1){ continue; }
			list.addAll(mun.getCitizen());
		}
		return list;
	}
	
	private boolean isPermitted(Chunk chunk, EntityPlayer player){
		if(chunk.getLink() != null){
			ChunkPos link = chunk.getLink();
			Print.chat(player, translate("states.manager_gui.perm_chunk.linked0", link.x, link.z));
			Print.chat(player, translate("states.manager_gui.perm_chunk.linked1"));
			Print.chat(player, translate("states.manager_gui.perm_chunk.linked2"));
			return false;
		}
		if(bypass(player)){
			Print.chat(player, translate("states.manager_gui.perm_admin.bypass"));
			return true;
		}
		boolean result = false;
		UUID uuid = player.getGameProfile().getId();
		boolean isco = chunk.getOwner().equals(uuid.toString());
		boolean ismn = chunk.getDistrict().getHead() != null && chunk.getDistrict().getHead().equals(uuid);
		boolean ismy = chunk.getMunicipality().getHead() != null && chunk.getMunicipality().getHead().equals(uuid);
		boolean isst = chunk.getState().getCouncil().contains(uuid) || (chunk.getState().getHead() != null && chunk.getState().getHead().equals(uuid));
		boolean iscm = false;//TODO companies
		Print.debug(isco, ismn, ismy, isst, iscm);
		switch(chunk.getType()){
			case COMPANY: result = iscm || isst; break;
			case DISTRICT: result = ismn || ismy || isst; break;
			case MUNICIPAL: result = ismy || isst; break;
			case NORMAL: result = ismn || ismy || isst; break;
			case PRIVATE: result = isco || ismy || isst; break;
			case PUBLIC: result = ismn || ismy || isst; break;
			case STATEOWNED: result = isst; break;
			default: result = false; break;
		}
		if(!result){
			sendStatus(null);
		}
		return result;
	}

	private boolean isOwner(Chunk chunk2, EntityPlayer player){
		if(chunk.getLink() != null){
			ChunkPos link = chunk.getLink();
			Print.chat(player, translate("states.manager_gui.perm_chunk.linked0", link.x, link.z));
			Print.chat(player, translate("states.manager_gui.perm_chunk.linked1"));
			Print.chat(player, translate("states.manager_gui.perm_chunk.linked2"));
			return false;
		}
		if(bypass(player)){
			Print.chat(player, translate("states.manager_gui.perm_admin.bypass"));
			return true;
		}
		boolean result = false;
		UUID uuid = player.getGameProfile().getId();
		boolean isco = chunk.getOwner().equals(uuid.toString());
		boolean ismn = chunk.getDistrict().getHead() != null && chunk.getDistrict().getHead().equals(uuid);
		boolean ismy = chunk.getMunicipality().getHead() != null && chunk.getMunicipality().getHead().equals(uuid);
		boolean isst = chunk.getState().getCouncil().contains(uuid) || (chunk.getState().getHead() != null && chunk.getState().getHead().equals(uuid));
		boolean iscm = false;//TODO companies
		Print.debug(isco, ismn, ismy, isst, iscm);
		switch(chunk.getType()){
			case COMPANY: result = iscm; break;
			case DISTRICT: result = ismn || ismy || isst; break;
			case MUNICIPAL: result = ismy || isst; break;
			case NORMAL: result = ismn || ismy || isst; break;
			case PRIVATE: result = isco || ismy; break;
			case PUBLIC: result = false; break;
			case STATEOWNED: result = isst; break;
			default: result = false;
		}
		if(!result){
			sendStatus(null);
		}
		return result;
	}

}
