package com.namelessmc.spigot.commands;

import java.util.Optional;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.spigot.Config;
import com.namelessmc.spigot.Message;
import com.namelessmc.spigot.NamelessPlugin;
import com.namelessmc.spigot.Permission;

/**
 * Command used to submit a code to validate a user's NamelessMC account
 */
public class ValidateCommand extends Command {

	public ValidateCommand() {
		super(Config.COMMANDS.getConfig().getString("validate"),
				Message.COMMAND_VALIDATE_DESCRIPTION.getMessage(),
				Message.COMMAND_VALIDATE_USAGE.getMessage("command", Config.COMMANDS.getConfig().getString("validate")),
				Permission.COMMAND_VALIDATE);
	}

	@Override
	public boolean execute(final CommandSender sender, final String[] args) {
		if (args.length != 1) {
			return false;
		}

		if (!(sender instanceof Player)) {
			sender.sendMessage(Message.COMMAND_NOTAPLAYER.getMessage());
			return true;
		}

		final Player player = (Player) sender;

		NamelessPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(NamelessPlugin.getInstance(), () -> {
			try {
				final Optional<NamelessUser> user = NamelessPlugin.getApi().getUser(player.getUniqueId());
				if (!user.isPresent()) {
					Message.PLAYER_SELF_NOTREGISTERED.send(sender);
					return;
				}
				
				final String code = args[0];
				if (user.get().verifyMinecraft(code)) {
					Message.COMMAND_VALIDATE_OUTPUT_SUCCESS.send(sender);
				} else {
					Message.COMMAND_VALIDATE_OUTPUT_FAIL_INVALIDCODE.send(sender);
				}
			} catch (final NamelessException e) {
				Message.COMMAND_VALIDATE_OUTPUT_FAIL_GENERIC.send(sender);
				return;
			}
		});

		return true;
	}

}
