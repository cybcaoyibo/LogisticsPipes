package logisticspipes.modules;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;

import logisticspipes.gui.hud.modules.HUDModBasedItemSink;
import logisticspipes.interfaces.IClientInformationProvider;
import logisticspipes.interfaces.IHUDModuleHandler;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.interfaces.IModuleWatchReciver;
import logisticspipes.modules.abstractmodules.LogisticsGuiModule;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import logisticspipes.network.guis.module.inhand.ModBasedItemSinkModuleGuiInHand;
import logisticspipes.network.guis.module.inpipe.ModBasedItemSinkModuleGuiSlot;
import logisticspipes.network.packets.hud.HUDStartModuleWatchingPacket;
import logisticspipes.network.packets.hud.HUDStopModuleWatchingPacket;
import logisticspipes.network.packets.module.ModuleBasedItemSinkList;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import logisticspipes.utils.item.ItemIdentifier;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ModuleModBasedItemSink extends LogisticsGuiModule implements IClientInformationProvider, IHUDModuleHandler, IModuleWatchReciver {
	
	public final List<String> modList = new LinkedList<String>();
	private BitSet modIdSet;

	private IHUDModuleRenderer HUD = new HUDModBasedItemSink(this);
	
	private final PlayerCollectionList localModeWatchers = new PlayerCollectionList();
	
	private static final SinkReply _sinkReply = new SinkReply(FixedPriority.ModBasedItemSink, 0, true, false, 5, 0);
	@Override
	public SinkReply sinksItem(ItemIdentifier item, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit) {
		if(bestPriority > _sinkReply.fixedPriority.ordinal() || (bestPriority == _sinkReply.fixedPriority.ordinal() && bestCustomPriority >= _sinkReply.customPriority)) return null;
		if(modIdSet == null) {
			buildModIdSet();
		}
		if(modIdSet.get(item.getModId())) {
			if(_service.canUseEnergy(5)) {
				return _sinkReply;
			}
		}
		return null;
	}

	@Override
	protected ModuleCoordinatesGuiProvider getPipeGuiProvider() {
		NBTTagCompound nbt = new NBTTagCompound();
		this.writeToNBT(nbt);
		return NewGuiHandler.getGui(ModBasedItemSinkModuleGuiSlot.class).setNbt(nbt);
	}

	@Override
	protected ModuleInHandGuiProvider getInHandGuiProvider() {
		return NewGuiHandler.getGui(ModBasedItemSinkModuleGuiInHand.class);
	}
	
	@Override
	public LogisticsModule getSubModule(int slot) {return null;}

	private void buildModIdSet() {
		modIdSet = new BitSet();
		for(String modname : modList) {
			int modid = ItemIdentifier.getModIdForName(modname);
			modIdSet.set(modid);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		modList.clear();
		int limit = nbttagcompound.getInteger("listSize");
		for(int i = 0; i < limit; i++) {
			modList.add(nbttagcompound.getString("Mod" + i));
		}
		modIdSet = null;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setInteger("listSize", modList.size());
		for(int i = 0; i < modList.size(); i++) {
			nbttagcompound.setString("Mod" + i, modList.get(i));
		}
	}

	@Override
	public void tick() {}

	@Override
	public List<String> getClientInformation() {
		List<String> list = new ArrayList<String>();
		list.add("Mods: ");
		list.addAll(modList);
		return list;
	}

	@Override
	public void startHUDWatching() {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStartModuleWatchingPacket.class).setModulePos(this));
	}

	@Override
	public void stopHUDWatching() {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStopModuleWatchingPacket.class).setModulePos(this));
	}

	@Override
	public void startWatching(EntityPlayer player) {
		localModeWatchers.add(player);
		NBTTagCompound nbt = new NBTTagCompound();
		writeToNBT(nbt);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ModuleBasedItemSinkList.class).setNbt(nbt).setModulePos(this), (Player)player);
	}

	@Override
	public void stopWatching(EntityPlayer player) {
		localModeWatchers.remove(player);
	}
	
	public void ModListChanged() {
		if(MainProxy.isServer(_world.getWorld())) {
			NBTTagCompound nbt = new NBTTagCompound();
			writeToNBT(nbt);
			MainProxy.sendToPlayerList(PacketHandler.getPacket(ModuleBasedItemSinkList.class).setNbt(nbt).setModulePos(this), localModeWatchers);
		} else {
			NBTTagCompound nbt = new NBTTagCompound();
			writeToNBT(nbt);
			MainProxy.sendPacketToServer(PacketHandler.getPacket(ModuleBasedItemSinkList.class).setNbt(nbt).setModulePos(this));
		}
	}

	@Override
	public IHUDModuleRenderer getHUDRenderer() {
		return HUD;
	}
	@Override
	public boolean hasGenericInterests() {
		return true;
	}

	@Override
	public List<ItemIdentifier> getSpecificInterests() {
		return null;
	}

	@Override
	public boolean interestedInAttachedInventory() {		
		return false;
	}

	@Override
	public boolean interestedInUndamagedID() {
		return false;
	}

	@Override
	public boolean recievePassive() {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIconTexture(IconRegister register) {
		return register.registerIcon("logisticspipes:itemModule/ModuleModBasedItemSink");
	}
}
