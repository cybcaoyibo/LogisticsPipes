package logisticspipes.asm.wrapper;

import logisticspipes.LogisticsPipes;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.string.ChatColor;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public abstract class AbstractWrapper {

	@Getter
	@Setter(value = AccessLevel.PACKAGE)
	protected WrapperState state = WrapperState.Enabled;
	@Getter
	@Setter(value = AccessLevel.PACKAGE)
	private Throwable reason;
	@Getter
	@Setter(value = AccessLevel.PACKAGE)
	private String modId;

	public void handleException(Throwable e) {
		e.printStackTrace();
		this.state = WrapperState.Exception;
		this.reason = e;
		String message = "Disabled " + getName() + getTypeName() + (modId != null ? (" for Mod: " + modId) : "") + ". Cause was an Exception";
		LogisticsPipes.log.severe(message);
		MainProxy.proxy.sendBroadCast(ChatColor.RED + message);
	}

	public abstract void onDisable();

	public abstract String getName();

	public abstract String getTypeName();
}
