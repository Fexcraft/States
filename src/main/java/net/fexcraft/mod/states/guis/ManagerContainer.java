package net.fexcraft.mod.states.guis;

import static net.fexcraft.mod.states.util.StateUtil.bypass;
import static net.fexcraft.mod.states.util.StateUtil.translate;

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
import net.fexcraft.mod.states.data.State;
import net.fexcraft.mod.states.data.capabilities.PlayerCapability;
import net.fexcraft.mod.states.data.capabilities.StatesCapabilities;
import net.fexcraft.mod.states.data.root.AnnounceLevel;
import net.fexcraft.mod.states.data.root.Layers;
import net.fexcraft.mod.states.data.root.Mailbox.MailType;
import net.fexcraft.mod.states.data.root.Mailbox.RecipientType;
import net.fexcraft.mod.states.data.root.Populated;
import net.fexcraft.mod.states.data.sub.ColorData;
import net.fexcraft.mod.states.data.sub.Manageable;
import net.fexcraft.mod.states.util.MailUtil;
import net.fexcraft.mod.states.util.Perms;
import net.fexcraft.mod.states.util.StConfig;
import net.fexcraft.mod.states.util.StateLogger;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.fml.relauncher.Side;

public class ManagerContainer extends GenericContainer {
	
	protected static final String NOTAX = "no_tax";
	protected static final String NOONE = "no_one";
	protected static final String NONE = "none";
	protected static final String UNKNOWN = "unknown";
	protected static final String NOTHING = "nothing";
	//
	private ManagerGui gui;
	protected Mode mode;
	protected Layers layer;
	protected Chunk chunk;
	protected District dis;
	protected Municipality mun;
	protected State state;
	protected Populated pop;
	protected Manageable manage;
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
		layer = Layers.values()[layerid];
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
			case PROPERTY:
				//
				break;
			case DISTRICT:
				dis = y < -5 ? chunk.getDistrict() : StateUtil.getDistrict(y);
				break;
			case MUNICIPALITY:
				mun = y < -5 ? chunk.getMunicipality() : StateUtil.getMunicipality(y);
				pop = mun;
				manage = mun.manage;
				break;
			case STATE:
				state = y < -5 ? chunk.getState() : StateUtil.getState(y);
				pop = state;
				manage = state.manage;
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
				addKey(list, "manager", dis.manage.getHead() == null ? NOONE : Static.getPlayerNameByUUID(dis.manage.getHead()), ViewMode.EDIT);
				addKey(list, "price", dis.price.asString(), ViewMode.EDIT);
				addKey(list, "type", dis.getType().name().toLowerCase(), ViewMode.EDIT);
				addKey(list, "color", dis.color.getString(), ViewMode.EDIT);
				addKey(list, "chunk_tax", dis.getChunkTax() > 0 ? ggas(dis.getChunkTax()) : NOTAX, ViewMode.EDIT);
				addKey(list, "last_edited", time(dis.created.getChanged()), ViewMode.NONE);
				addKey(list, "neighbors", dis.neighbors.size(), ViewMode.LIST);
				addKey(list, "chunks", dis.getClaimedChunks(), ViewMode.NONE);
				addKey(list, "canforsettle", dis.r_CFS.get(), ViewMode.BOOL);
				addKey(list, "unifbank", dis.r_ONBANKRUPT.get(), ViewMode.BOOL);
				addKey(list, "creator", Static.getPlayerNameByUUID(dis.created.getCreator()), ViewMode.NONE);
				addKey(list, "created", time(dis.created.getCreated()), ViewMode.NONE);
				addKey(list, "ruleset", dis.manage.getRulesetTitle(), ViewMode.EDIT);
				addKey(list, "mailbox", dis.mailbox.asString(), ViewMode.RESET);
				addKey(list, "icon", dis.icon.getn(), ViewMode.EDIT);
				addKey(list, "explosion", dis.r_ALLOW_EXPLOSIONS.get(), ViewMode.BOOL);
				break;
			case MUNICIPALITY:
				addKey(list, "id", mun.getId(), ViewMode.NONE);
				addKey(list, "name", mun.getName(), ViewMode.EDIT);
				if(mun.abandon.isAbandoned()){
					addKey(list, "abandoned", true, ViewMode.NONE);
					addKey(list, "abandoned_since", mun.abandon.getSince() == 0 ? UNKNOWN : time(mun.abandon.getSince()), ViewMode.NONE);
					addKey(list, "abandoned_by", mun.abandon.getBy() == null ? UNKNOWN : Static.getPlayerNameByUUID(mun.abandon.getBy()), ViewMode.NONE);
				}
				addKey(list, "state", mun.getState().getName() + " (" + mun.getState().getId() + ")", ViewMode.GOTO);
				addKey(list, "mayor", mun.manage.getHead() == null ? NOONE : Static.getPlayerNameByUUID(mun.manage.getHead()), ViewMode.EDIT);
				addKey(list, "price", mun.price.asString(), ViewMode.EDIT);
				addKey(list, "title", mun.getTitle(), ViewMode.EDIT);
				addKey(list, "color", mun.color.getString(), ViewMode.EDIT);
				addKey(list, "citizen", mun.getResidentCount(), ViewMode.LIST);
				addKey(list, "balance", ggas(mun.getAccount().getBalance()), ViewMode.GOTO);
				addKey(list, "citizen_tax", mun.getCitizenTax() > 0 ? ggas(mun.getCitizenTax()) : NOTAX, ViewMode.EDIT);
				addKey(list, "last_edited", time(mun.created.getChanged()), ViewMode.NONE);
				addKey(list, "council", mun.manage.getCouncil().size(), ViewMode.LIST);
				addKey(list, "districts", mun.getDistricts().size() + " / " + mun.getDistrictLimit(), ViewMode.LIST);
				addKey(list, "neighbors", mun.neighbors.size(), ViewMode.LIST);
				addKey(list, "opentojoin", mun.r_OPEN.get(), ViewMode.BOOL);
				addKey(list, "kickbankrupt", mun.r_KIB.get(), ViewMode.BOOL);
				addKey(list, "chunks", mun.getClaimedChunks() + "/" + mun.getChunkLimit(), ViewMode.NONE);
				addKey(list, "creator", Static.getPlayerNameByUUID(mun.created.getCreator()), ViewMode.NONE);
				addKey(list, "created", time(mun.created.getCreated()), ViewMode.NONE);
				addKey(list, "forcechunks", mun.getForceLoadedChunks() == null ? NONE : mun.getForceLoadedChunks().size(), ViewMode.NONE);
				addKey(list, "ruleset", mun.manage.getRulesetTitle(), ViewMode.EDIT);
				addKey(list, "mailbox", mun.mailbox.asString(), ViewMode.RESET);
				addKey(list, "blacklist", mun.getPlayerBlacklist().size(), ViewMode.LIST);
				addKey(list, "icon", mun.icon.getn(), ViewMode.EDIT);
				if(!mun.abandon.isAbandoned()){
					addKey(list, "abandoned", false, ViewMode.NONE);
				}
				break;
			case COUNTY:
				//
				break;
			case STATE:
				addKey(list, "id", state.getId(), ViewMode.NONE);
				addKey(list, "name", state.getName(), ViewMode.EDIT);
				addKey(list, "capital", StateUtil.getMunicipality(state.getCapitalId()).getName() + " (" + state.getCapitalId() + ")", ViewMode.EDIT);
				addKey(list, "leader", state.manage.getHead() == null ? NOONE : Static.getPlayerNameByUUID(state.manage.getHead()), ViewMode.EDIT);
				addKey(list, "price", state.price.asString(), ViewMode.EDIT);
				addKey(list, "color", state.color.getString(), ViewMode.EDIT);
				addKey(list, "citizen", state.getAllResidentCount(), ViewMode.LIST);
				addKey(list, "balance", ggas(state.getAccount().getBalance()), ViewMode.GOTO);
				addKey(list, "chunk_tax", state.getChunkTaxPercentage() + "%", ViewMode.EDIT);
				addKey(list, "citizen_tax", state.getCitizenTaxPercentage() + "%", ViewMode.EDIT);
				addKey(list, "last_edited", time(state.created.getChanged()), ViewMode.NONE);
				addKey(list, "council", state.manage.getCouncil().size(), ViewMode.LIST);
				addKey(list, "municipalities", state.getMunicipalities().size(), ViewMode.LIST);
				addKey(list, "neighbors", state.neighbors.size(), ViewMode.LIST);
				addKey(list, "creator", Static.getPlayerNameByUUID(state.created.getCreator()), ViewMode.NONE);
				addKey(list, "created", time(state.created.getCreated()), ViewMode.NONE);
				addKey(list, "ruleset", state.manage.getRulesetTitle(), ViewMode.EDIT);
				addKey(list, "mailbox", state.mailbox.asString(), ViewMode.RESET);
				addKey(list, "blacklist", state.getBlacklist().size(), ViewMode.LIST);
				addKey(list, "icon", state.icon.getn(), ViewMode.EDIT);
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
				addKey(list, "mailbox", cap.getMailbox().asString(), ViewMode.RESET);
				break;
			case CHUNK:
				addKey(list, "coords", chunk.xCoord() + ", " + chunk.zCoord(), ViewMode.NONE);
				addKey(list, "district", chunk.getDistrict().getName() + " (" + chunk.getDistrict().getId() + ")", ViewMode.GOTO);
				addKey(list, "owner", chunk.getType() == ChunkType.PRIVATE ? Static.getPlayerNameByUUID(chunk.getOwner()) : chunk.getOwner(), chunk.getType() == ChunkType.PRIVATE ? ViewMode.NONE : ViewMode.GOTO);
				addKey(list, "price", chunk.price.asString(), ViewMode.EDIT);
				addKey(list, "tax", chunk.getCustomTax() > 0 ? ggas(chunk.getCustomTax()) + "c" : chunk.getDistrict().getChunkTax() > 0 ? ggas(chunk.getDistrict().getChunkTax()) + "d" : NOTAX, ViewMode.EDIT);
				addKey(list, "type", chunk.getType().name().toLowerCase(), ViewMode.EDIT);
				addKey(list, "last_edited", time(chunk.created.getChanged()), ViewMode.NONE);
				addKey(list, "last_taxcoll", time(chunk.lastTaxCollection()), ViewMode.NONE);
				addKey(list, "linked_chunks", chunk.getLinkedChunks().size() > 0 ? chunk.getLinkedChunks().size() : NONE, ViewMode.LIST);
				addKey(list, "linked_to", chunk.getLink() == null ? NOTHING : chunk.getLink().x + ", " + chunk.getLink().z, ViewMode.GOTO);
				addKey(list, "whitelist", chunk.getPlayerWhitelist().size(), ViewMode.LIST);
				addKey(list, chunk.getType().interactPrefix() + "_interact", chunk.interact(), ViewMode.BOOL);
				addKey(list, "claimed_by", Static.getPlayerNameByUUID(chunk.created.getClaimer()), ViewMode.NONE);
				addKey(list, "claimed_at", time(chunk.created.getCreated()), ViewMode.NONE);
				if(chunk.getDistrict().getId() == -2){
					addKey(list, "transit", time(chunk.created.getChanged() + Time.DAY_MS), ViewMode.NONE);
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
				if(layer.isPopulated()){
					initListValues(pop.getAllResidentCount());
					for(UUID uuid : pop.getAllResidents()) addKey(list, uuid, uuid);
				}
				break;
			case LIST_COMPONENTS:
				if(layer.isChunk()){
					initListValues(chunk.getLinkedChunks().size());
					for(int[] pos : chunk.getLinkedChunks()){
						addKey(list, pos, pos[0] + ", " + pos[1]);
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
				if(layer.isManageable()){
					initListValues(manage.getCouncil().size());
					for(UUID uuid : manage.getCouncil()) addKey(list, uuid, uuid);
				}
				break;
			case LIST_NEIGHBORS:
				if(layer.isDistrict()){
					initListValues(dis.neighbors.size());
					for(int id : dis.neighbors.get()){
						addKey(list, id, StateUtil.getDistrictName(id));
					}
				}
				if(layer.isMunicipality()){
					initListValues(mun.neighbors.size());
					for(int id : mun.neighbors.get()){
						addKey(list, id, StateUtil.getMunicipalityName(id));
					}
				}
				if(layer.isState()){
					initListValues(state.neighbors.size());
					for(int id : state.neighbors.get()){
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
									if(dis.manage.isAuthorized(dis.r_SET_NAME.id, cap.getUUID()).isTrue() || bypass(player)){
										value = value.trim();
										if(value.replace(" ", "").length() < 3){
											sendStatus("states.manager_gui.view.name_short");
											break;
										}
										dis.setName(value);
										dis.created.update();
										dis.save();
										sendViewData();
										Print.log(StateLogger.player(player) + " changed name of " + StateLogger.district(dis) + " to " + dis.getName() + ".");
									}
									else sendStatus(null);
									break;
								}
								case "municipality":{
									openGui(Layers.MUNICIPALITY, Mode.INFO, dis.getMunicipality().getId());
									break;
								}
								case "manager":{
									if(dis.manage.isAuthorized(dis.r_SET_MANAGER.id, cap.getUUID()).isTrue() || bypass(player)){
										GameProfile gp = Static.getServer().getPlayerProfileCache().getGameProfileForUsername(value);
										if(gp == null || gp.getId() == null){
											sendStatus("states.manager_gui.view.player_not_found_cache");
											break;
										}
										dis.manage.setHead(gp.getId());
										dis.created.update();
										dis.save();
										sendViewData();
										Print.log(StateLogger.player(player) + " changed manager of " + StateLogger.district(dis) + " to " + StateLogger.player(gp) + ".");
									}
									else sendStatus(null);
									break;
								}
								case "price":{
									if(dis.manage.isAuthorized(dis.r_SET_PRICE.id, cap.getUUID()).isTrue() || bypass(player)){
										try{
											Long price = Long.parseLong(value);
											if(price < 0){ price = 0l; }
											dis.price.set(price);
											dis.created.update();
											dis.save();
											sendViewData();
											Print.log(StateLogger.player(player) + " changed price of " + StateLogger.district(dis) + " to " + dis.price.asWorth() + ".");
										}
										catch(Exception e){
											sendStatus("&cError: &7" + e.getMessage());
										}
									}
									else sendStatus(null);
									break;
								}
								case "type":{
									if(dis.manage.isAuthorized(dis.r_SET_TYPE.id, cap.getUUID()).isTrue() || bypass(player)){
										try{
											DistrictType type = DistrictType.valueOf(value.toUpperCase());
											if(type != null){
												dis.setType(type);
												dis.created.update();
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
									if(dis.manage.isAuthorized(dis.r_SET_COLOR.id, cap.getUUID()).isTrue() || bypass(player)){
										try{
											String str = value;
											if(!ColorData.validString(null, str)){
												sendStatus("states.manager_gui.view.invalid_hex");
												break;
											}
											dis.color.set(str);
											dis.created.update();
											dis.save();
											sendViewData();
											Print.log(StateLogger.player(player) + " changed color of " + StateLogger.district(dis) + " to " + dis.color.getString() + ".");
										}
										catch(Exception e){
											sendStatus("&2Error: &7" + e.getMessage());
										}
									}
									else sendStatus(null);
									break;
								}
								case "chunk_tax":{
									if(dis.manage.isAuthorized(dis.r_SET_CHUNKTAX.id, cap.getUUID()).isTrue() || bypass(player)){
										if(value.equals("reset") || value.equals("disable")){
											dis.setChunkTax(0);
											dis.created.update();
											dis.save();
											sendViewData();
											Print.log(StateLogger.player(player) + " set the 'chunk-tax' of " + StateLogger.district(dis) + " to " + dis.getChunkTax());
										}
										else if(NumberUtils.isCreatable(value)){
											dis.setChunkTax(Long.parseLong(value));
											dis.created.update();
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
									openGui(Layers.DISTRICT, Mode.LIST_NEIGHBORS, dis.getId());
									break;
								}
								case "canforsettle":{
									if(dis.manage.isAuthorized(dis.r_CFS.id, cap.getUUID()).isTrue() || bypass(player)){
										dis.r_CFS.set(!dis.r_CFS.get());
										dis.created.update();
										dis.save();
										this.sendViewData();
										Print.log(StateLogger.player(player) + " changed 'can-foreigners-settle' of " + StateLogger.district(dis) + " to " + dis.r_CFS.get() + ".");
									}
									else sendStatus(null);
									break;
								}
								case "unifbank":{
									if(dis.manage.isAuthorized(dis.r_ONBANKRUPT.id, cap.getUUID()).isTrue() || bypass(player)){
										dis.r_ONBANKRUPT.set(!dis.r_ONBANKRUPT.get());
										dis.created.update();
										dis.save();
										this.sendViewData();
										Print.log(StateLogger.player(player) + " changed 'unclaim-if-brankrupt' of " + StateLogger.district(dis) + " to " + dis.r_ONBANKRUPT.get() + ".");
									}
									else sendStatus(null);
									break;
								}
								case "ruleset":{
									if(dis.manage.isAuthorized(dis.r_SET_RULESET.id, cap.getUUID()).isTrue() || bypass(player)){
										value = value.trim();
										if(value.replace(" ", "").length() < 3){
											sendStatus("states.manager_gui.view.name_short");
											break;
										}
										dis.manage.setRulesetTitle(value);
										dis.created.update();
										dis.save();
										sendViewData();
										Print.log(StateLogger.player(player) + " changed ruleset name of " + StateLogger.district(dis) + " to " + dis.getName() + ".");
									}
									else sendStatus(null);
									break;
								}
								case "mailbox":{
									if(dis.manage.isAuthorized(dis.r_SET_MAILBOX.id, cap.getUUID()).isTrue() || bypass(player)){
										dis.mailbox.reset();
										dis.created.update();
										dis.save();
										sendViewData();
										Print.log(StateLogger.player(player) + " reset mailbox location of" + StateLogger.district(dis) + ".");
									}
									else sendStatus(null);
									break;
								}
								case "icon":{
									if(dis.manage.isAuthorized(dis.r_SET_ICON.id, cap.getUUID()).isTrue() || bypass(player)){
										try{
											dis.icon.set(value);
											dis.created.update();
											dis.save();
											sendViewData();
											Print.log(StateLogger.player(player) + " changed icon of " + StateLogger.district(dis) + " to " + dis.icon.getn() + ".");
										}
										catch(Exception e){
											sendStatus("&2Error: &7" + e.getMessage());
										}
									}
									else sendStatus(null);
									break;
								}
								case "explosion":{
									if(dis.manage.isAuthorized(dis.r_ALLOW_EXPLOSIONS.id, cap.getUUID()).isTrue() || bypass(player)){
										dis.r_ALLOW_EXPLOSIONS.set(!dis.r_ALLOW_EXPLOSIONS.get());
										dis.created.update();
										dis.save();
										this.sendViewData();
										Print.log(StateLogger.player(player) + " changed 'allow-explosions' of " + StateLogger.district(dis) + " to " + dis.r_ALLOW_EXPLOSIONS.get() + ".");
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
									if(mun.manage.isAuthorized(mun.r_SET_NAME.id, cap.getUUID()).isTrue() || bypass(player)){
										value = value.trim();
										if(value.replace(" ", "").length() < 3){
											sendStatus("states.manager_gui.view.name_short");
											break;
										}
										mun.setName(value);
										mun.created.update();
										mun.save();
										sendViewData();
										Print.log(StateLogger.player(player) + " changed name of " + StateLogger.municipality(mun) + " to " + mun.getName() + ".");
									}
									else sendStatus(null);
									break;
								}
								case "state":{
									openGui(Layers.STATE, Mode.INFO, mun.getId());
									break;
								}
								case "mayor":{
									if(mun.manage.isAuthorized(mun.r_SET_MAYOR.id, cap.getUUID()).isTrue() || bypass(player)){
										if(value.equals("null") || value.equals("reset")){
											if(mun.manage.isAuthorized(mun.r_RESET_MAYOR.id, cap.getUUID()).isFalse() && !bypass(player)){
												sendStatus(null);
												return;
											}
											mun.manage.setHead(null);
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
										if(!mun.manage.isInCouncil(gp.getId()) && !mun.isCitizen(gp.getId())){
											sendStatus("states.manager_gui.view.player_not_council_or_citizen");
											break;
										}
										mun.manage.setHead(gp.getId());
										mun.created.update();
										mun.save();
										sendViewData();
										Print.log(StateLogger.player(player) + " changed mayor of " + StateLogger.municipality(mun) + " to " + StateLogger.player(gp) + ".");
									}
									else sendStatus(null);
									break;
								}
								case "price":{
									if(mun.manage.isAuthorized(mun.r_SET_PRICE.id, cap.getUUID()).isTrue() || bypass(player)){
										try{
											Long price = Long.parseLong(value);
											if(price < 0){ price = 0l; }
											mun.price.set(price);
											mun.created.update();
											mun.save();
											sendViewData();
											Print.log(StateLogger.player(player) + " changed price of " + StateLogger.municipality(mun) + " to " + mun.price.asWorth() + ".");
										}
										catch(Exception e){
											sendStatus("&cError: &7" + e.getMessage());
										}
									}
									else sendStatus(null);
									break;
								}
								case "title":{
									if(mun.manage.isAuthorized(mun.r_SET_TITLE.id, cap.getUUID()).isTrue() || bypass(player)){
										try{
											if(value.replaceAll("(&.)", "").length() > 16){
												sendStatus("states.manager_gui.view.title_too_long");
												break;
											}
											mun.setTitle(value);
											mun.created.update();
											mun.save();
											sendViewData();
											Print.log(StateLogger.player(player) + " changed title of " + StateLogger.municipality(mun) + " to " + mun.getTitle() + ".");
										}
										catch(Exception e){
											sendStatus("&2Error: &7" + e.getMessage());
										}
									}
									else sendStatus(null);
									break;
								}
								case "color":{
									if(mun.manage.isAuthorized(mun.r_COLOR.id, cap.getUUID()).isTrue() || bypass(player)){
										try{
											String str = value;
											if(!ColorData.validString(null, str)){
												sendStatus("states.manager_gui.view.invalid_hex");
												break;
											}
											mun.color.set(str);
											mun.created.update();
											mun.save();
											sendViewData();
											Print.log(StateLogger.player(player) + " changed color of " + StateLogger.municipality(mun) + " to " + mun.color.getString() + ".");
										}
										catch(Exception e){
											sendStatus("&2Error: &7" + e.getMessage());
										}
									}
									else sendStatus(null);
									break;
								}
								case "citizen":{
									openGui(Layers.MUNICIPALITY, Mode.LIST_CITIZENS, mun.getId());
									break;
								}
								case "citizen_tax":{
									if(mun.manage.isAuthorized(mun.r_SET_CITIZENTAX.id, cap.getUUID()).isTrue() || bypass(player)){
										if(value.equals("reset") || value.equals("disable")){
											mun.setCitizenTax(0);
											mun.created.update();
											mun.save();
											sendViewData();
											Print.log(StateLogger.player(player) + " set the 'citizen-tax' of " + StateLogger.municipality(mun) + " to " + mun.getCitizenTax());
										}
										else if(NumberUtils.isCreatable(value)){
											mun.setCitizenTax(Long.parseLong(value));
											mun.created.update();
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
									openGui(Layers.MUNICIPALITY, Mode.LIST_COUNCIL, mun.getId());
									break;
								}
								case "districts":{
									openGui(Layers.MUNICIPALITY, Mode.LIST_COMPONENTS, mun.getId());
									break;
								}
								case "neighbors":{
									openGui(Layers.MUNICIPALITY, Mode.LIST_NEIGHBORS, mun.getId());
									break;
								}
								case "opentojoin":{
									if(mun.manage.isAuthorized(mun.r_OPEN.id, cap.getUUID()).isTrue() || bypass(player)){
										mun.r_OPEN.set(!mun.r_OPEN.get());
										mun.created.update();
										mun.save();
										sendViewData();
										Print.log(StateLogger.player(player) + " set " + StateLogger.municipality(mun) + " to " + (mun.r_OPEN.get() ? "OPEN" : "CLOSED") + ".");
									}
									else sendStatus(null);
									break;
								}
								case "kickbankrupt":{
									if(mun.manage.isAuthorized(mun.r_KIB.id, cap.getUUID()).isTrue() || bypass(player)){
										mun.r_KIB.set(!mun.r_KIB.get());
										mun.created.update();
										mun.save();
										sendViewData();
										Print.log(StateLogger.player(player) + " changed 'kick-if-brankrupt' of " + StateLogger.municipality(mun) + " to " + mun.r_OPEN.get() + ".");
									}
									else sendStatus(null);
									break;
								}
								case "ruleset":{
									if(mun.manage.isAuthorized(mun.r_SET_RULESET.id, cap.getUUID()).isTrue() || bypass(player)){
										value = value.trim();
										if(value.replace(" ", "").length() < 3){
											sendStatus("states.manager_gui.view.name_short");
											break;
										}
										mun.manage.setRulesetTitle(value);
										mun.created.update();
										mun.save();
										sendViewData();
										Print.log(StateLogger.player(player) + " changed ruleset name of " + StateLogger.municipality(mun) + " to " + mun.getName() + ".");
									}
									else sendStatus(null);
									break;
								}
								case "mailbox":{
									if(mun.manage.isAuthorized(mun.r_SET_MAILBOX.id, cap.getUUID()).isTrue() || bypass(player)){
										mun.mailbox.reset();
										mun.created.update();
										mun.save();
										sendViewData();
										Print.log(StateLogger.player(player) + " reset mailbox location of" + StateLogger.municipality(mun) + ".");
									}
									else sendStatus(null);
									break;
								}
								case "blacklist":{
									openGui(Layers.MUNICIPALITY, Mode.LIST_BWLIST, mun.getId());
									break;
								}
								case "icon":{
									if(mun.manage.isAuthorized(mun.r_ICON.id, cap.getUUID()).isTrue() || bypass(player)){
										try{
											mun.icon.set(value);
											mun.created.update();
											mun.save();
											sendViewData();
											Print.log(StateLogger.player(player) + " changed icon of " + StateLogger.municipality(mun) + " to " + mun.icon.getn() + ".");
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
									if(state.manage.isAuthorized(state.r_SET_NAME.id, cap.getUUID()).isTrue() || bypass(player)){
										if(value.replace(" ", "").length() < 3){
											sendStatus("states.manager_gui.view.name_short");
											break;
										}
										state.setName(value);
										state.created.update();
										state.save();
										sendViewData();
										Print.log(StateLogger.player(player) + " set the name of " + StateLogger.state(state) + " to " + state.getName());
									}
									else sendStatus(null);
									break;
								}
								case "capital":{
									if(state.manage.isAuthorized(state.r_SET_CAPITAL.id, cap.getUUID()).isTrue() || bypass(player)){
										Municipality mun = StateUtil.getMunicipality(Integer.parseInt(value));
										if(mun.getId() <= 0 || mun.getState().getId() != state.getId()){
											sendStatus("states.manager_gui.view.municipality_not_in_state");
											break;
										}
										state.setCapitalId(mun.getId());
										state.created.update();
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
									if(state.manage.isAuthorized(state.r_SET_LEADER.id, cap.getUUID()).isTrue() || bypass(player)){
										if(value.equals("null") || value.equals("reset")){
											if(state.manage.isAuthorized(state.r_RESET_HEAD.id, cap.getUUID()).isFalse() && !bypass(player)){
												sendStatus(null);
												return;
											}
											state.manage.setHead(null);
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
										if(!state.manage.isInCouncil(gp.getId()) && !state.isCitizen(gp.getId())){
											sendStatus("states.manager_gui.view.player_not_council_or_citizen");
											break;
										}
										state.manage.setHead(gp.getId());
										state.created.update();
										state.save();
										sendViewData();
										Print.log(StateLogger.player(player) + " changed leader of " + StateLogger.state(state) + " to " + StateLogger.player(gp) + ".");
									}
									else sendStatus(null);
									break;
								}
								case "price":{
									if(state.manage.isAuthorized(state.r_SET_PRICE.id, cap.getUUID()).isTrue() || bypass(player)){
										try{
											Long price = Long.parseLong(value);
											if(price < 0){ price = 0l; }
											state.price.set(price);
											state.created.update();
											state.save();
											sendViewData();
											Print.log(StateLogger.player(player) + " set the price of " + StateLogger.state(state) + " to " + state.price.asWorth());
										}
										catch(Exception e){
											sendStatus("&cError: &7" + e.getMessage());
										}
									}
									else sendStatus(null);
									break;
								}
								case "color":{
									if(state.manage.isAuthorized(state.r_SET_COLOR.id, cap.getUUID()).isTrue() || bypass(player)){
										try{
											String str = value;
											if(!ColorData.validString(null, str)){
												sendStatus("states.manager_gui.view.invalid_hex");
												break;
											}
											state.color.set(str);
											state.created.update();
											state.save();
											sendViewData();
											Print.log(StateLogger.player(player) + " set the color of " + StateLogger.state(state) + " to " + state.color.getString());
										}
										catch(Exception e){
											sendStatus("&2Error: &7" + e.getMessage());
										}
									}
									else sendStatus(null);
									break;
								}
								case "citizen":{
									openGui(Layers.STATE, Mode.LIST_CITIZENS, state.getId());
									break;
								}
								case "chunk_tax":{
									if(state.manage.isAuthorized(state.r_SET_CHUNK_TAX_PERCENT.id, cap.getUUID()).isTrue() || bypass(player)){
										if(value.equals("reset") || value.equals("disable")){
											state.setChunkTaxPercentage((byte)0);
											state.created.update();
											state.save();
											sendViewData();
											Print.log(StateLogger.player(player) + " set the 'chunk-tax-percentage' of " + StateLogger.state(state) + " to " + state.getChunkTaxPercentage());
										}
										else if(NumberUtils.isCreatable(value)){
											byte byt = Byte.parseByte(value);
											if(byt > 100){ byt = 100; } if(byt < 0){ byt = 0; }
											state.setChunkTaxPercentage(byt);
											state.created.update();
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
									if(state.manage.isAuthorized(state.r_SET_CITIZEN_TAX_PERCENT.id, cap.getUUID()).isTrue() || bypass(player)){
										if(value.equals("reset") || value.equals("disable")){
											state.setCitizenTaxPercentage((byte)0);
											state.created.update();
											state.save();
											sendViewData();Print.log(StateLogger.player(player) + " set the 'citizen-tax-percentage' of " + StateLogger.state(state) + " to " + state.getChunkTaxPercentage());
										}
										else if(NumberUtils.isCreatable(value)){
											byte byt = Byte.parseByte(value);
											if(byt > 100){ byt = 100; } if(byt < 0){ byt = 0; }
											state.setCitizenTaxPercentage(byt);
											state.created.update();
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
									openGui(Layers.STATE, Mode.LIST_COUNCIL, state.getId());
									break;
								}
								case "municipalities":{
									openGui(Layers.STATE, Mode.LIST_COMPONENTS, state.getId());
									break;
								}
								case "neighbors":{
									openGui(Layers.STATE, Mode.LIST_NEIGHBORS, state.getId());
									break;
								}
								case "ruleset":{
									if(state.manage.isAuthorized(state.r_SET_RULESET.id, cap.getUUID()).isTrue() || bypass(player)){
										if(value.replace(" ", "").length() < 3){
											sendStatus("states.manager_gui.view.name_short");
											break;
										}
										state.manage.setRulesetTitle(value);
										state.created.update();
										state.save();
										sendViewData();
										Print.log(StateLogger.player(player) + " set the ruleset name of " + StateLogger.state(state) + " to " + state.getName());
									}
									else sendStatus(null);
									break;
								}
								case "mailbox":{
									if(state.manage.isAuthorized(state.r_SET_MAILBOX.id, cap.getUUID()).isTrue() || bypass(player)){
										state.mailbox.reset();
										state.created.update();
										state.save();
										sendViewData();
										Print.log(StateLogger.player(player) + " reset mailbox location of" + StateLogger.state(state) + ".");
									}
									else sendStatus(null);
									break;
								}
								case "blacklist":{
									openGui(Layers.STATE, Mode.LIST_BWLIST, state.getId());
									break;
								}
								case "icon":{
									if(state.manage.isAuthorized(state.r_SET_ICON.id, cap.getUUID()).isTrue() || bypass(player)){
										try{
											state.icon.set(value);
											state.created.update();
											state.save();
											sendViewData();
											Print.log(StateLogger.player(player) + " set the icon of " + StateLogger.state(state) + " to " + state.icon.getn());
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
									openGui(Layers.MUNICIPALITY, Mode.INFO, cap.getMunicipality().getId());
									break;
								}
								case "mailbox":{
									cap.getMailbox().reset();
									cap.save();
									sendViewData();
									break;
								}
							}
							break;
						case CHUNK:
							switch(keys[packet.getInteger("button")]){
								case "district":{
									openGui(Layers.DISTRICT, Mode.INFO, chunk.getDistrict().getId());
									break;
								}
								case "owner":{
									switch(chunk.getType()){
										case COMPANY:
											//TODO
											break;
										case DISTRICT:
											openGui(Layers.DISTRICT, Mode.INFO, chunk.getDistrict().getId());
											break;
										case MUNICIPAL:
										case NORMAL:
										case PUBLIC:
											openGui(Layers.MUNICIPALITY, Mode.INFO, chunk.getMunicipality().getId());
											break;
										case STATEOWNED:
										case STATEPUBLIC:
											openGui(Layers.STATE, Mode.INFO, chunk.getState().getId());
											break;
										case PRIVATE:
										default: return;
									}
									break;
								}
								case "price":{
									if(isPermitted(chunk, player)){
										try{
											Long price = Long.parseLong(value);
											chunk.price.set(price);
											chunk.created.update();
											chunk.save();
											sendViewData();
											Print.log(StateLogger.player(player) + " set the price of the " + StateLogger.chunk(chunk) + " to " + chunk.price.asWorth() + ".");
										}
										catch(Exception e){
											sendStatus("&9Error: &7" + e.getMessage());
										}
									}
									break;
								}
								case "tax":{
									if(chunk.getDistrict().manage.isAuthorized(chunk.getDistrict().r_SET_CUSTOM_CHUNKTAX.id, cap.getUUID()).isTrue()){
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
												case STATEPUBLIC:
												case STATEOWNED:
												case MUNICIPAL:
												case DISTRICT:
												case NORMAL:{
													String to = type == ChunkType.NORMAL || type == ChunkType.DISTRICT ? "District" : type == ChunkType.MUNICIPAL ? "Municipality" : type == ChunkType.STATEOWNED ? "State" : "ERROR";
													chunk.setType(type);
													chunk.setOwner(null);
													chunk.price.set(0);
													chunk.created.update(time);
													chunk.getLinkedChunks().forEach(link -> {
														Chunk ck = StateUtil.getTempChunk(link);
														ck.setType(type);
														ck.setOwner(null);
														ck.price.set(0);
														ck.created.update(time);
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
													chunk.created.update(time);
													chunk.getLinkedChunks().forEach(link -> {
														Chunk ck = StateUtil.getTempChunk(link);
														ck.setType(type);
														ck.created.update(time);
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
									openGui(Layers.CHUNK, Mode.LIST_COMPONENTS, chunk.xCoord(), chunk.zCoord());
									break;
								}
								case "linked_to":{
									if(chunk.getLink() == null) return;
									openGui(Layers.CHUNK, Mode.CKINFO, chunk.getLink().x, chunk.getLink().z);
									break;
								}
								case "whitelist":{
									openGui(Layers.CHUNK, Mode.LIST_BWLIST, chunk.xCoord(), chunk.zCoord());
									break;
								}
								case "allow_interact":
								case "only_interact":
								case "interact":{
									if(isPermitted(chunk, player, true)){
										chunk.interact(!chunk.interact());
										chunk.created.update();
										chunk.save();
										sendViewData();
										Print.log(StateLogger.player(player) + " set '" + chunk.getType().interactPrefix() + "_interact' of the " + StateLogger.chunk(chunk) + " to " + chunk.interact() + ".");
									}
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
								int[] resloc = chunk.getLinkedChunks().get(packet.getInteger("button"));
								openGui(Layers.CHUNK, Mode.CKINFO, resloc[0], resloc[1]);
								return;
							}
							if(layer.isMunicipality()){
								openGui(Layers.DISTRICT, Mode.INFO, mun.getDistricts().get(packet.getInteger("button")));
								return;
							}
							if(layer.isState()){
								openGui(Layers.MUNICIPALITY, Mode.INFO, state.getMunicipalities().get(packet.getInteger("button")));
								return;
							}
							//TODO check if the component is loaded in memory?
							break;
						case LIST_COUNCIL:
							//nothing also yet
							break;
						case LIST_NEIGHBORS:
							if(layer.isDistrict()){
								openGui(Layers.DISTRICT, Mode.INFO, dis.neighbors.get(packet.getInteger("button")));
								return;
							}
							if(layer.isMunicipality()){
								openGui(Layers.MUNICIPALITY, Mode.INFO, mun.neighbors.get(packet.getInteger("button")));
								return;
							}
							if(layer.isState()){
								openGui(Layers.STATE, Mode.INFO, state.neighbors.get(packet.getInteger("button")));
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
								if(mun.manage.isAuthorized(mun.r_EDIT_BL.id, cap.getUUID()).isTrue() || bypass(player)){
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
								if(state.manage.isAuthorized(state.r_EDIT_BL.id, cap.getUUID()).isTrue() || bypass(player)){
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
								if(!(mun.manage.isAuthorized(mun.r_KICK.id, cap.getUUID()).isTrue() || bypass(player))){
									sendStatus(null);
									return;
								}
								if(mun.manage.isInCouncil((UUID)list_values[index])){
									Print.chat(player, "states.manager_gui.list.cannot_kick_council0");
									Print.chat(player, "states.manager_gui.list.cannot_kick_council1");
									sendStatus(null);
									return;
								}
								if(!mun.isCitizen((UUID)list_values[index])){
									sendStatus("states.manager_gui.list.not_a_citizen");
									return;
								}
								mun.getResidents().remove(list_values[index]);
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
								Chunk linked = StateUtil.getTempChunk((int[])list_values[index]);
								if(linked.getLink().x == chunk.xCoord() && linked.getChunkPos().z == chunk.zCoord()){
									chunk.getLinkedChunks().removeIf(pre -> pre[0] == linked.xCoord() && pre[1] == linked.zCoord());
									chunk.setEdited(Time.getDate());
									chunk.save();
									linked.setLink(null);
									linked.setEdited(Time.getDate());
									linked.save();
									Print.log(StateLogger.player(player) + " unlinked the " + StateLogger.chunk(chunk) + ".");
									sendListData();
								}
								else sendStatus("ERROR:[01]");
								//TODO property checks
								return;
							}
							if(layer.isState()){
								if(!state.manage.isAuthorized(state.r_MUN_KICK.id, cap.getUUID()).isTrue() || !bypass(player)){
									sendStatus(null);
									return;
								}
								Municipality kicktar = StateUtil.getMunicipality((int)list_values[index]);
								if(kicktar == null || kicktar.getId() <= 0){
									sendStatus("states.manager_gui.list_components_state.mun_not_found");
									return;
								}
								if(kicktar.getId() == state.getCapitalId()){
									sendStatus("states.manager_gui.list_components_state.cannot_kick_capital");
									return;
								}
								kicktar.setState(StateUtil.getState(-1));
								kicktar.created.update();
								kicktar.save();
								state.getMunicipalities().removeIf(val -> val == kicktar.getId());
								state.created.update();
								state.save();
								StateUtil.announce(null, AnnounceLevel.STATE, "Municipality of " + kicktar.getName() + " was removed from our State!", state.getId());
								Print.log(StateLogger.player(player) + " kicked " + StateLogger.municipality(kicktar) + " from the State of " + StateLogger.state(state));
								sendListData();
								return;
							}
							break;
						case LIST_COUNCIL:
							if(layer.isMunicipality()){
								if(!(mun.manage.isAuthorized(mun.r_COUNCIL_KICK.id, cap.getUUID()).isTrue() || bypass(player))){
									sendStatus(null);
									return;
								}
								if(!mun.manage.isInCouncil((UUID)list_values[index])){
									sendStatus("states.manager_gui.list.not_council");
									return;
								}
								mun.manage.getCouncil().remove((UUID)list_values[index]);
								mun.created.update();
								mun.save();
								String name = Static.getPlayerNameByUUID((UUID)list_values[index]);
								StateUtil.announce(null, AnnounceLevel.MUNICIPALITY, name + " &9was removed from the Municipality Council!", mun.getId());
								Print.log(StateLogger.player(player) + " removed " + list_values[index].toString() + " from the council of " + StateLogger.municipality(mun) + ".");
								//
								String kickmsg = translate("states.manager_gui.list_council_municipality.rem_msg", mun.getName() + "(" + mun.getId() + ")");
								MailUtil.send(player, RecipientType.PLAYER, list_values[index].toString(), player.getGameProfile().getId().toString(), kickmsg, MailType.SYSTEM, Time.DAY_MS * 14);
								return;
							}
							if(layer.isState()){
								if(!state.manage.isAuthorized(state.r_COUNCIL_KICK.id, cap.getUUID()).isTrue() && !bypass(player)){
									sendStatus(null);
									return;
								}
								if(!state.manage.isInCouncil((UUID)list_values[index])){
									sendStatus("states.manager_gui.list.not_council");
									return;
								}
								state.manage.getCouncil().remove((UUID)list_values[index]);
								state.created.update();
								state.save();
								String name = Static.getPlayerNameByUUID((UUID)list_values[index]);
								StateUtil.announce(null, AnnounceLevel.MUNICIPALITY, name + " &9was removed from the State Council!", state.getId());
								Print.log(StateLogger.player(player) + " removed " + list_values[index].toString() + " from the council of " + StateLogger.state(state) + ".");
								//
								String kickmsg = translate("states.manager_gui.list_council_state.rem_msg", state.getName() + "(" + state.getId() + ")");
								MailUtil.send(player, RecipientType.PLAYER, list_values[index].toString(), player.getGameProfile().getId().toString(), kickmsg, MailType.SYSTEM, Time.DAY_MS * 14);
								return;
							}
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
								if(mun.manage.isAuthorized(mun.r_EDIT_BL.id, cap.getUUID()).isTrue() || bypass(player)){
									GameProfile prof = Static.getServer().getPlayerProfileCache().getGameProfileForUsername(input);
									if(prof == null){
										sendStatus("states.manager_gui.view.player_not_found_cache");
										return;
									}
									if(mun.manage.isInCouncil(prof.getId())){
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
								if(state.manage.isAuthorized(state.r_EDIT_BL.id, cap.getUUID()).isTrue() || bypass(player)){
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
								if(!(mun.manage.isAuthorized(mun.r_INVITE.id, cap.getUUID()).isTrue() || bypass(player))){
									sendStatus(null);
									return;
								}
								GameProfile gp = Static.getServer().getPlayerProfileCache().getGameProfileForUsername(input);
								if(gp == null || gp.getId() == null){
									sendStatus("states.manager_gui.view.player_not_found_cache");
									return;
								}
								if(mun.isCitizen(gp.getId())){
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
								Print.chat(player, translate("states.manager_gui.list_citizens_municipality.add_done"));
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
								if(x == chunk.xCoord() && z == chunk.zCoord()){
									sendStatus("states.manager_gui.list_components_chunk.same_chunk");
									return;
								}
								Chunk ck = StateUtil.getChunk(x, z);
								if(!isOwner(ck, player)){
									sendStatus("states.manager_gui.list_components_chunk.not_owner");
									return;
								}
								ck.setLink(chunk.getChunkPos());
								ck.created.update();
								ck.save();
								chunk.getLinkedChunks().add(new int[]{ x, z });
								chunk.created.update();
								chunk.save();
								Print.log(StateLogger.player(player) + " linked " + StateLogger.chunk(ck) + " to " + StateLogger.chunk(chunk) + ".");
								sendListData();
								//TODO property checks
								return;
							}
							if(layer.isState()){
								if(!state.manage.isAuthorized(state.r_MUN_INVITE.id, cap.getUUID()).isTrue() && !bypass(player)){
									sendStatus(null);
									return;
								}
								Municipality invtar = null;
								if(NumberUtils.isCreatable(input)){
									invtar = StateUtil.getMunicipality(Integer.parseInt(input));
								}
								else{
									invtar = StateUtil.getMunicipalityByName(input);
								}
								if(invtar == null || invtar.getId() <= 0){
									sendStatus("states.manager_gui.list_components_state.mun_not_found");
									return;
								}
								if(invtar.manage.getHead() == null){
									sendStatus("states.manager_gui.list_components_state.mun_no_mayor");
									return;
								}
								String invmsg = translate("states.manager_gui.list_components_state.add_msg", state.getName() + " (" + state.getId() + ")");
								NBTTagCompound compound = new NBTTagCompound();
								compound.setString("type", "state_municipality");
								compound.setInteger("id", state.getId());
								compound.setString("from", player.getGameProfile().getId().toString());
								compound.setLong("at", Time.getDate());
								MailUtil.send(player, RecipientType.MUNICIPALITY, invtar.getId(), player.getGameProfile().getId().toString(), invmsg, MailType.INVITE, Time.DAY_MS * 12, compound);
								Print.log(StateLogger.player(player) + " invited " + StateLogger.municipality(invtar) + " to join the State of " + StateLogger.state(state));
								Print.chat(player, translate("states.manager_gui.list_components_state.add_done"));
								sendListData();
								return;
							}
							break;
						case LIST_COUNCIL:
							if(layer.isMunicipality()){
								if(!(mun.manage.isAuthorized(mun.r_COUNCIL_INVITE.id, cap.getUUID()).isTrue() || bypass(player))){
									sendStatus(null);
									return;
								}
								GameProfile gp = Static.getServer().getPlayerProfileCache().getGameProfileForUsername(input);
								if(gp == null || gp.getId() == null){
									sendStatus("states.manager_gui.view.player_not_found_cache");
									return;
								}
								if(mun.manage.isInCouncil(gp.getId())){
									sendStatus("states.manager_gui.list.is_council");
									return;
								}
								if(!mun.isCitizen(gp.getId())){
									sendStatus("states.manager_gui.list.not_a_citizen");
									return;
								}
								String invmsg = translate("states.manager_gui.list_council_municipality.add_msg", mun.getName() + "(" + mun.getId() + ")");
								NBTTagCompound compound = new NBTTagCompound();
								compound.setString("type", "municipality_council");
								compound.setInteger("id", mun.getId());
								compound.setString("from", player.getGameProfile().getId().toString());
								compound.setLong("at", Time.getDate());
								MailUtil.send(player, RecipientType.PLAYER, gp.getId().toString(), player.getGameProfile().getId().toString(), invmsg, MailType.INVITE, Time.DAY_MS * 5, compound);
								Print.log(StateLogger.player(player) + " invited " + StateLogger.player(gp) + " to the council of " + StateLogger.municipality(mun) + ".");
								Print.chat(player, translate("states.manager_gui.list_council_municipality.add_done"));
								sendListData();
								return;
							}
							if(layer.isState()){
								if(!state.manage.isAuthorized(state.r_COUNCIL_INVITE.id, cap.getUUID()).isTrue() && !bypass(player)){
									sendStatus(null);
									return;
								}
								GameProfile gp = Static.getServer().getPlayerProfileCache().getGameProfileForUsername(input);
								if(gp == null || gp.getId() == null){
									sendStatus("states.manager_gui.view.player_not_found_cache");
									return;
								}
								if(state.manage.isInCouncil(gp.getId())){
									sendStatus("states.manager_gui.list.is_council");
									return;
								}
								if(!state.isCitizen(gp.getId())){
									sendStatus("states.manager_gui.list.not_a_citizen");
									return;
								}
								String invmsg = translate("states.manager_gui.list_council_state.add_msg", state.getName() + " (" + state.getId() + ")");
								NBTTagCompound compound = new NBTTagCompound();
								compound.setString("type", "state_council");
								compound.setInteger("id", state.getId());
								compound.setString("from", player.getGameProfile().getId().toString());
								compound.setLong("at", Time.getDate());
								MailUtil.send(player, RecipientType.PLAYER, gp.getId().toString(), player.getGameProfile().getId().toString(), invmsg, MailType.INVITE, Time.DAY_MS * 5, compound);
								Print.chat(player, translate("states.manager_gui.list_council_state.add_done"));
								Print.log(StateLogger.player(player) + " invited " + StateLogger.player(gp) + " to the council of " + StateLogger.state(state) + ".");
								return;
							}
							break;
						case LIST_NEIGHBORS: return;
						default: return;
					}
					break;
				}
				case "list_mode_home":{
					if(layer.isChunk()){
						openGui(Layers.CHUNK, Mode.CKINFO, chunk.xCoord(), chunk.zCoord());
					}
					if(layer.isDistrict()){
						openGui(Layers.DISTRICT, Mode.INFO, dis.getId());
					}
					if(layer.isMunicipality()){
						openGui(Layers.MUNICIPALITY, Mode.INFO, mun.getId());
					}
					if(layer.isState()){
						openGui(Layers.STATE, Mode.INFO, state.getId());
					}
					break;
				}
			}
		}
	}

	private void openGui(Layers layer, Mode mode, int id){
		GuiHandler.openGui(player, layer.ordinal() + 2, mode.ordinal(), id, 0);
	}

	private void openGui(Layers layer, Mode mode, int x, int z){
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

	private boolean isPermitted(Chunk chunk, EntityPlayer player){
		return isPermitted(chunk, player, false);
	}
	
	private boolean isPermitted(Chunk chunk, EntityPlayer player, boolean individual){
		if(chunk.getLink() != null && !individual){
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
		boolean ismn = chunk.getDistrict().manage.getHead() != null && chunk.getDistrict().manage.getHead().equals(uuid);
		boolean ismy = chunk.getMunicipality().manage.getHead() != null && chunk.getMunicipality().manage.getHead().equals(uuid);
		boolean isst = chunk.getState().manage.isInCouncil(uuid) || (chunk.getState().manage.getHead() != null && chunk.getState().manage.getHead().equals(uuid));
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
			case STATEPUBLIC: result = isst; break;
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
		boolean ismn = chunk.getDistrict().manage.getHead() != null && chunk.getDistrict().manage.getHead().equals(uuid);
		boolean ismy = chunk.getMunicipality().manage.getHead() != null && chunk.getMunicipality().manage.getHead().equals(uuid);
		boolean isst = chunk.getState().manage.isInCouncil(uuid) || (chunk.getState().manage.getHead() != null && chunk.getState().manage.getHead().equals(uuid));
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
			case STATEPUBLIC: result = isst; break;
			default: result = false;
		}
		if(!result){
			sendStatus(null);
		}
		return result;
	}

}
