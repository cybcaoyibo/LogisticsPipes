package logisticspipes.network.packets.pipe;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.tuples.LPPosition;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;
import cpw.mods.fml.client.FMLClientHandler;

@Accessors(chain = true)
public class PipeDebugAskForTarget extends ModernPacket {

	@Setter
	@Getter
	private boolean isServer;

	public PipeDebugAskForTarget(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		isServer = data.readBoolean();
	}

	@Override
	public void processPacket(EntityPlayer player) {
		MovingObjectPosition box = FMLClientHandler.instance().getClient().objectMouseOver;
		if (box != null && box.typeOfHit == EnumMovingObjectType.TILE) {
			if (!isServer) {
				TileEntity tile = new LPPosition(box.blockX, box.blockY, box.blockZ).getTileEntity(player.getEntityWorld());
				if (tile instanceof LogisticsTileGenericPipe) {
					((CoreRoutedPipe) ((LogisticsTileGenericPipe) tile).pipe).debug.debugThisPipe = !((CoreRoutedPipe) ((LogisticsTileGenericPipe) tile).pipe).debug.debugThisPipe;
					if (((CoreRoutedPipe) ((LogisticsTileGenericPipe) tile).pipe).debug.debugThisPipe) {
						player.sendChatToPlayer(ChatMessageComponent.createFromText("Debug enabled On Client"));
					} else {
						player.sendChatToPlayer(ChatMessageComponent.createFromText("Debug disabled On Client"));
					}
				}
			} else {
				MainProxy.sendPacketToServer(PacketHandler.getPacket(PipeDebugResponse.class).setPosX(box.blockX).setPosY(box.blockY).setPosZ(box.blockZ));
			}
		}
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		data.writeBoolean(isServer);
	}

	@Override
	public ModernPacket template() {
		return new PipeDebugAskForTarget(getId());
	}

	@Override
	public boolean isCompressable() {
		return true;
	}
}
