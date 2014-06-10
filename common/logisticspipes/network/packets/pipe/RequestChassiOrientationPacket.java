package logisticspipes.network.packets.pipe;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.network.Player;

public class RequestChassiOrientationPacket extends CoordinatesPacket {

	public RequestChassiOrientationPacket(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsTileGenericPipe pipe = this.getPipe(player.worldObj);
		if (pipe == null || !(pipe.pipe instanceof PipeLogisticsChassi)) return;
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ChassiOrientationPacket.class).setDir(((PipeLogisticsChassi) pipe.pipe).getOrientation()).setPosX(getPosX()).setPosY(getPosY()).setPosZ(getPosZ()), (Player) player);
	}

	@Override
	public ModernPacket template() {
		return new RequestChassiOrientationPacket(getId());
	}
}
