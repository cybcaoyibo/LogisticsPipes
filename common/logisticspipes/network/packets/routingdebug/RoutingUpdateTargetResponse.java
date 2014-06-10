package logisticspipes.network.packets.routingdebug;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.concurrent.Callable;

import logisticspipes.commands.chathelper.LPChatListener;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.packets.gui.OpenChatGui;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.routing.ServerRouter;
import logisticspipes.routing.debug.DebugController;
import logisticspipes.utils.string.ChatColor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import cpw.mods.fml.common.network.Player;

@Accessors(chain = true)
public class RoutingUpdateTargetResponse extends ModernPacket {

	public RoutingUpdateTargetResponse(int id) {
		super(id);
	}

	public enum TargetMode {
		Block, Entity, None;
	}

	@Getter
	@Setter
	private TargetMode mode;

	@Getter
	@Setter
	private Object[] additions = new Object[0];

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		mode = TargetMode.values()[data.readByte()];
		int size = data.readInt();
		additions = new Object[size];
		for (int i = 0; i < size; i++) {
			int arraySize = data.readInt();
			byte[] bytes = new byte[arraySize];
			data.read(bytes);
			ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
			ObjectInput in = null;
			in = new ObjectInputStream(bis);
			try {
				Object o = in.readObject();
				additions[i] = o;
			} catch (ClassNotFoundException e) {
				throw new UnsupportedOperationException(e);
			}
		}
	}

	@Override
	public void processPacket(final EntityPlayer player) {
		if (mode == TargetMode.None) {
			player.sendChatToPlayer(ChatMessageComponent.createFromText(ChatColor.RED + "No Target Found"));
		} else if (mode == TargetMode.Block) {
			int x = (Integer) additions[0];
			int y = (Integer) additions[1];
			int z = (Integer) additions[2];
			player.sendChatToPlayer(ChatMessageComponent.createFromText("Checking Block at: x:" + x + " y:" + y + " z:" + z));
			int id = player.worldObj.getBlockId(x, y, z);
			player.sendChatToPlayer(ChatMessageComponent.createFromText("Found Block with Id: " + id));
			final TileEntity tile = player.worldObj.getBlockTileEntity(x, y, z);
			if (tile == null) {
				player.sendChatToPlayer(ChatMessageComponent.createFromText(ChatColor.RED + "No TileEntity found"));
			} else if (!(tile instanceof LogisticsTileGenericPipe)) {
				player.sendChatToPlayer(ChatMessageComponent.createFromText(ChatColor.RED + "No LogisticsTileGenericPipe found"));
			} else if (!(((LogisticsTileGenericPipe) tile).pipe instanceof CoreRoutedPipe)) {
				player.sendChatToPlayer(ChatMessageComponent.createFromText(ChatColor.RED + "No CoreRoutedPipe found"));
			} else {
				LPChatListener.addTask(new Callable<Boolean>() {

					@Override
					public Boolean call() throws Exception {
						player.sendChatToPlayer(ChatMessageComponent.createFromText(ChatColor.GREEN + "Starting RoutingTable debug update."));
						DebugController.instance(player).debug(((ServerRouter) ((CoreRoutedPipe) ((LogisticsTileGenericPipe) tile).pipe).getRouter()));
						MainProxy.sendPacketToPlayer(PacketHandler.getPacket(OpenChatGui.class), (Player) player);
						return true;
					}
				}, player);
				player.sendChatToPlayer(ChatMessageComponent.createFromText(ChatColor.AQUA + "Start RoutingTable debug update ? " + ChatColor.RESET + "<" + ChatColor.GREEN + "yes" + ChatColor.RESET + "/" + ChatColor.RED + "no" + ChatColor.RESET + ">"));
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(OpenChatGui.class), (Player) player);
			}
		} else if (mode == TargetMode.Entity) {
			player.sendChatToPlayer(ChatMessageComponent.createFromText(ChatColor.RED + "Entity not allowed"));
		}
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		data.writeByte(mode.ordinal());
		data.writeInt(additions.length);
		for (int i = 0; i < additions.length; i++) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutput out = null;
			out = new ObjectOutputStream(bos);
			out.writeObject(additions[i]);
			byte[] bytes = bos.toByteArray();
			data.writeInt(bytes.length);
			data.write(bytes);
		}
	}

	@Override
	public ModernPacket template() {
		return new RoutingUpdateTargetResponse(getId());
	}

	@Override
	public boolean isCompressable() {
		return true;
	}
}
