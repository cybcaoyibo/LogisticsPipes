package logisticspipes.commands;

import logisticspipes.commands.abstracts.SubCommandHandler;
import logisticspipes.commands.commands.BypassCommand;
import logisticspipes.commands.commands.ChangelogCommand;
import logisticspipes.commands.commands.ClearCommand;
import logisticspipes.commands.commands.DebugCommand;
import logisticspipes.commands.commands.DummyCommand;
import logisticspipes.commands.commands.DumpCommand;
import logisticspipes.commands.commands.NBTDebugCommand;
import logisticspipes.commands.commands.NameLookupCommand;
import logisticspipes.commands.commands.RoutingThreadCommand;
import logisticspipes.commands.commands.TransferNamesCommand;
import logisticspipes.commands.commands.VersionCommand;
import logisticspipes.commands.commands.WatchCommand;
import logisticspipes.commands.commands.WrapperCommand;
import net.minecraft.command.ICommandSender;

public class MainCommandHandler extends SubCommandHandler {

	@Override
	public String[] getNames() {
		return new String[] { "logisticspipes", "lp", "logipipes" };
	}

	@Override
	public boolean isCommandUsableBy(ICommandSender sender) {
		return true;
	}

	@Override
	public String[] getDescription() {
		return new String[] { "The main LP command" };
	}

	@Override
	public void registerSubCommands() {
		this.registerSubCommand(new DummyCommand());
		this.registerSubCommand(new VersionCommand());
		this.registerSubCommand(new ChangelogCommand());
		this.registerSubCommand(new NBTDebugCommand());
		this.registerSubCommand(new RoutingThreadCommand());
		this.registerSubCommand(new TransferNamesCommand());
		this.registerSubCommand(new NameLookupCommand());
		this.registerSubCommand(new DumpCommand());
		this.registerSubCommand(new BypassCommand());
		this.registerSubCommand(new WatchCommand());
		this.registerSubCommand(new DebugCommand());
		this.registerSubCommand(new WrapperCommand());
		this.registerSubCommand(new ClearCommand());
	}
}
