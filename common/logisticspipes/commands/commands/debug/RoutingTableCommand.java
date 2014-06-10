package logisticspipes.commands.commands.debug;

import logisticspipes.commands.abstracts.ICommandHandler;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.routingdebug.RoutingUpdateAskForTarget;
import logisticspipes.proxy.MainProxy;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatMessageComponent;
import cpw.mods.fml.common.network.Player;

public class RoutingTableCommand implements ICommandHandler {

	@Override
	public String[] getNames() {
		return new String[] { "rt", "routing" };
	}

	@Override
	public boolean isCommandUsableBy(ICommandSender sender) {
		return sender instanceof EntityPlayer;
	}

	@Override
	public String[] getDescription() {
		return new String[] { "Starts debugging the Routing Table", "update of the pipe you are currently looking at." };
	}

	@Override
	public void executeCommand(ICommandSender sender, String[] args) {
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(RoutingUpdateAskForTarget.class), (Player) sender);
		sender.sendChatToPlayer(ChatMessageComponent.createFromText("Asking for Target."));
	}
}
