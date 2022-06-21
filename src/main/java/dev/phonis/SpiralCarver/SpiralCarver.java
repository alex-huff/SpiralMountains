package dev.phonis.SpiralCarver;

import dev.phonis.SpiralCarver.commands.CommandCarve;
import dev.phonis.SpiralCarver.commands.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public
class SpiralCarver extends JavaPlugin
{

	@Override
	public
	void onEnable()
	{
		Bukkit.broadcastMessage("Reloaded generator.");
		SubCommand.registerCommand(this, new CommandCarve(this));
	}

	@Override
	public
	void onDisable()
	{
	}

}
