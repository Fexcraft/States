package net.fexcraft.mod.states.guis;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.gui.GenericContainer;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.states.data.Chunk;
import net.fexcraft.mod.states.data.District;
import net.fexcraft.mod.states.data.Municipality;
import net.fexcraft.mod.states.data.State;
import net.fexcraft.mod.states.data.capabilities.PlayerCapability;
import net.fexcraft.mod.states.data.capabilities.StatesCapabilities;
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
		Print.debug("CREATED " + player.world.isRemote);
		mode = Mode.values()[x];
		layer = Layer.values()[layerid];
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
				addKey(list, "canforsettle", dis.r_CFS.get(), ViewMode.NONE);
				addKey(list, "unifbank", dis.r_ONBANKRUPT.get(), ViewMode.NONE);
				addKey(list, "creator", Static.getPlayerNameByUUID(dis.getCreator()), ViewMode.NONE);
				addKey(list, "created", time(dis.getCreated()), ViewMode.NONE);
				addKey(list, "ruleset", dis.getRulesetTitle(), ViewMode.GOTO);
				addKey(list, "mailbox", dis.getMailbox() == null ? NOMAILBOX : dis.getMailbox().toString(), ViewMode.RESET);
				addKey(list, "icon", dis.getIcon(), ViewMode.EDIT);
				break;
			case MUNICIPALITY:
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
						case LIST:
							//
							break;
						default:
							break;
					}
					break;
				}
				case "view_mode_click":{
					if(mode != Mode.INFO) return;
					boolean bypass = StateUtil.bypass(player);
					switch(layer){
						case CHUNK:
							break;
						case COMPANY:
							break;
						case DISTRICT:
							switch(keys[packet.getInteger("button")]){
								case "name":
									if(bypass || dis.isAuthorized(dis.r_SET_NAME.id, cap.getUUID()).isTrue()){
										dis.setName(packet.getString("value"));
										dis.setChanged(Time.getDate());
										dis.save();
										sendViewData();
										Print.log(StateLogger.player(player) + " changed name of " + StateLogger.district(dis) + " to " + dis.getName() + ".");
									}
									else sendStatus(null);
									break;
							}
							break;
						case MUNICIPALITY:
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
		LIST;

		public int entries(){
			return this == LIST ? 10 : 12;
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
