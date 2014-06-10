package logisticspipes.modules;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import logisticspipes.gui.hud.modules.HUDCCBasedQuickSort;
import logisticspipes.interfaces.IClientInformationProvider;
import logisticspipes.interfaces.IHUDModuleHandler;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.IModuleWatchReciver;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.hud.HUDStartModuleWatchingPacket;
import logisticspipes.network.packets.modules.CCBasedQuickSortMode;
import logisticspipes.network.packets.modules.CCBasedQuickSortSinkSize;
import logisticspipes.pipes.basic.CoreRoutedPipe.ItemSendMode;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.cc.CCSinkResponder;
import logisticspipes.proxy.specialinventoryhandler.SpecialInventoryHandler;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.PipeRoutingConnectionType;
import logisticspipes.routing.ServerRouter;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.Pair;
import logisticspipes.utils.tuples.Triplet;
import lombok.Getter;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ModuleCCBasedQuickSort extends ModuleQuickSort implements IClientInformationProvider, IHUDModuleHandler, IModuleWatchReciver {

	private Map<Integer, Pair<Integer, List<CCSinkResponder>>> sinkResponses = new HashMap<Integer, Pair<Integer, List<CCSinkResponder>>>();

	@Getter
	private int timeout = 100;

	@Getter
	private int sinkSize = 0;

	private final PlayerCollectionList localModeWatchers = new PlayerCollectionList();

	private IHUDModuleRenderer HUD = new HUDCCBasedQuickSort(this);

	private void createSinkMessage(int slot, ItemIdentifierStack stack) {
		List<CCSinkResponder> respones = new ArrayList<CCSinkResponder>();
		IRouter sourceRouter = this._itemSender.getRouter();
		if (sourceRouter == null) return;
		BitSet routersIndex = ServerRouter.getRoutersInterestedIn(null); // get only pipes with generic interest
		List<ExitRoute> validDestinations = new ArrayList<ExitRoute>(); // get the routing table 
		for (int i = routersIndex.nextSetBit(0); i >= 0; i = routersIndex.nextSetBit(i + 1)) {
			IRouter r = SimpleServiceLocator.routerManager.getRouterUnsafe(i, false);
			List<ExitRoute> exits = sourceRouter.getDistanceTo(r);
			if (exits != null) {
				for (ExitRoute e : exits) {
					if (e.containsFlag(PipeRoutingConnectionType.canRouteTo)) validDestinations.add(e);
				}
			}
		}
		Collections.sort(validDestinations);

		outer:
		for (ExitRoute candidateRouter : validDestinations) {
			if (candidateRouter.destination.getId().equals(sourceRouter.getId())) continue;

			for (IFilter filter : candidateRouter.filters) {
				if (filter.blockRouting() || (filter.isBlocked() == filter.isFilteredItem(stack.getItem()))) continue outer;
			}
			if (candidateRouter.destination != null && candidateRouter.destination.getLogisticsModule() != null) {
				respones.addAll(candidateRouter.destination.getLogisticsModule().queueCCSinkEvent(stack));
			}
		}
		sinkResponses.put(slot, new Pair<Integer, List<CCSinkResponder>>(0, respones));
	}

	@Override
	public void tick() {
		IInventoryUtil invUtil = _invProvider.getPointedInventory(true);
		if (invUtil == null) return;
		handleSinkResponses(invUtil);
		if (--currentTick > 0) return;
		if (stalled) currentTick = stalledDelay;
		else currentTick = normalDelay;

		//Extract Item

		if (!_power.canUseEnergy(500)) {
			stalled = true;
			return;
		}

		if ((!(invUtil instanceof SpecialInventoryHandler) && invUtil.getSizeInventory() == 0) || !_power.canUseEnergy(500)) {
			stalled = true;
			return;
		}

		if (lastSuceededStack >= invUtil.getSizeInventory()) lastSuceededStack = 0;

		//incremented at the end of the previous loop.
		if (lastStackLookedAt >= invUtil.getSizeInventory()) lastStackLookedAt = 0;

		ItemStack slot = invUtil.getStackInSlot(lastStackLookedAt);

		while (slot == null) {
			lastStackLookedAt++;
			if (lastStackLookedAt >= invUtil.getSizeInventory()) lastStackLookedAt = 0;
			slot = invUtil.getStackInSlot(lastStackLookedAt);
			if (lastStackLookedAt == lastSuceededStack) {
				stalled = true;
				send();
				return; // then we have been around the list without sending, halt for now
			}
		}
		send();

		if (!sinkResponses.containsKey(lastStackLookedAt)) {
			createSinkMessage(lastStackLookedAt, ItemIdentifierStack.getFromStack(slot));
		}

		lastStackLookedAt++;
		checkSize();
	}

	private void handleSinkResponses(IInventoryUtil invUtil) {
		boolean changed = false;
		Iterator<Entry<Integer, Pair<Integer, List<CCSinkResponder>>>> iter = sinkResponses.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Integer, Pair<Integer, List<CCSinkResponder>>> pair = iter.next();
			pair.getValue().setValue1(pair.getValue().getValue1() + 1);
			boolean canBeHandled = true;
			for (CCSinkResponder response : pair.getValue().getValue2()) {
				if (!response.isDone()) {
					canBeHandled = false;
					break;
				}
			}
			if (canBeHandled || pair.getValue().getValue1() > timeout) {
				if (handle(invUtil, pair.getKey(), pair.getValue().getValue2())) {
					this.stalled = false;
					this.lastSuceededStack = pair.getKey();
				}
				iter.remove();
				changed = true;
			}
		}
		if (changed) {
			checkSize();
		}
	}

	private boolean handle(IInventoryUtil invUtil, int slot, List<CCSinkResponder> list) {
		if (list.isEmpty()) return false;
		ItemIdentifier ident = list.get(0).getStack().getItem();
		ItemStack stack = invUtil.getStackInSlot(slot);
		if (stack == null || ItemIdentifier.get(stack) != ident) return false;
		final IRouter source = this._itemSender.getRouter();
		List<Triplet<Integer, Integer, CCSinkResponder>> posibilities = new ArrayList<Triplet<Integer, Integer, CCSinkResponder>>();
		for (CCSinkResponder sink : list) {
			if (!sink.isDone()) continue;
			if (sink.getCanSink() < 1) continue;
			IRouter r = SimpleServiceLocator.routerManager.getRouter(sink.getRouterId());
			if (r == null) continue;
			List<ExitRoute> ways = source.getDistanceTo(r);
			int minDistance = Integer.MAX_VALUE;
			outer:
			for (ExitRoute route : ways) {
				for (IFilter filter : route.filters) {
					if (filter.blockRouting() || filter.isFilteredItem(ident) == filter.isBlocked()) continue outer;
				}
				minDistance = Math.min(route.distanceToDestination, minDistance);
			}
			if (minDistance != Integer.MAX_VALUE) {
				posibilities.add(new Triplet<Integer, Integer, CCSinkResponder>(sink.getPriority(), minDistance, sink));
			}
		}
		if (posibilities.isEmpty()) return false;
		Collections.sort(posibilities, new Comparator<Triplet<Integer, Integer, CCSinkResponder>>() {

			@Override
			public int compare(Triplet<Integer, Integer, CCSinkResponder> o1, Triplet<Integer, Integer, CCSinkResponder> o2) {
				int c = o2.getValue1() - o1.getValue1();
				if (c == 0) c = o1.getValue2() - o2.getValue2();
				return c;
			}
		});
		boolean sended = false;
		for (Triplet<Integer, Integer, CCSinkResponder> triple : posibilities) {
			CCSinkResponder sink = triple.getValue3();
			if (sink.getCanSink() < 0) continue;
			stack = invUtil.getStackInSlot(slot);
			if (stack == null || stack.stackSize <= 0) continue;
			int amount = Math.min(stack.stackSize, sink.getCanSink());
			ItemStack extracted = invUtil.decrStackSize(slot, amount);
			_itemSender.sendStack(extracted, sink.getRouterId(), ItemSendMode.Fast);
			sended = true;
		}
		return sended;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIconTexture(IconRegister register) {
		return register.registerIcon("logisticspipes:itemModule/ModuleCCQuickSort");
	}

	@Override
	public final int getX() {
		if (_slot >= 0) return this._power.getX();
		else return 0;
	}

	@Override
	public final int getY() {
		if (_slot >= 0) return this._power.getY();
		else return -1;
	}

	@Override
	public final int getZ() {
		if (_slot >= 0) return this._power.getZ();
		else return -1 - _slot;
	}

	@Override
	public int getGuiHandlerID() {
		return GuiIDs.GUI_Module_CC_Based_QuickSort_ID;
	}

	@Override
	public boolean hasGui() {
		return true;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		timeout = nbttagcompound.getInteger("Timeout");
		if (timeout == 0) timeout = 100;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		nbttagcompound.setInteger("Timeout", timeout);
	}

	@Override
	public List<String> getClientInformation() {
		List<String> list = new ArrayList<String>(5);
		list.add("Timeout: " + timeout);
		return list;
	}

	private void checkSize() {
		if (sinkSize != sinkResponses.size()) {
			sinkSize = sinkResponses.size();
			MainProxy.sendToPlayerList(PacketHandler.getPacket(CCBasedQuickSortSinkSize.class).setInteger2(_slot).setInteger(sinkSize).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), localModeWatchers);
		}
	}

	@Override
	public void startWatching() {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStartModuleWatchingPacket.class).setInteger(_slot).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
	}

	@Override
	public void stopWatching() {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStartModuleWatchingPacket.class).setInteger(_slot).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
	}

	@Override
	public void startWatching(EntityPlayer player) {
		localModeWatchers.add(player);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(CCBasedQuickSortMode.class).setInteger2(_slot).setInteger(timeout).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), (Player) player);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(CCBasedQuickSortSinkSize.class).setInteger2(_slot).setInteger(sinkSize).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), (Player) player);
	}

	@Override
	public void stopWatching(EntityPlayer player) {
		localModeWatchers.remove(player);
	}

	@Override
	public IHUDModuleRenderer getRenderer() {
		return HUD;
	}

	public void setTimeout(int time) {
		this.timeout = time;
		MainProxy.sendToPlayerList(PacketHandler.getPacket(CCBasedQuickSortMode.class).setInteger2(_slot).setInteger(timeout).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), localModeWatchers);
	}

	public void setSinkSize(int integer) {
		if (MainProxy.isClient(this._world.getWorld())) {
			this.sinkSize = integer;
		}
	}
}
