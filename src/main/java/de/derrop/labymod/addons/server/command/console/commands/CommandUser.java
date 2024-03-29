package de.derrop.labymod.addons.server.command.console.commands;
/*
 * Created by derrop on 11.10.2019
 */

import de.derrop.labymod.addons.server.GommeStatsServer;
import de.derrop.labymod.addons.server.command.Command;
import de.derrop.labymod.addons.server.command.CommandSender;
import de.derrop.labymod.addons.server.util.Utility;

import java.util.*;
import java.util.stream.Collectors;

public class CommandUser extends Command {

    private GommeStatsServer statsServer;

    public CommandUser(GommeStatsServer statsServer) {
        super("user", "u");
        this.statsServer = statsServer;
    }

    @Override
    public void execute(CommandSender sender, String label, String line, String[] args) {
        if (args.length >= 2 && args[0].equalsIgnoreCase("add")) {
            UUID uniqueId = this.parseUUID(args[1]);
            if (uniqueId == null) {
                sender.sendMessage("This is not a valid uuid, example for a valid uuid: " + UUID.randomUUID().toString());
                return;
            }
            String token = args.length >= 3 ? args[2] : Utility.generateRandomString(32, 64);
            boolean success = this.statsServer.getDatabaseProvider().getUserAuthenticator().addUser(token, uniqueId);
            if (success) {
                sender.sendMessage("Successfully created a new user for the uuid " + uniqueId + " with the token: " + token);
            } else {
                sender.sendMessage("A user with the uuid " + uniqueId + " already exists");
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            UUID uniqueId = this.parseUUID(args[1]);
            if (uniqueId == null) {
                sender.sendMessage("This is not a valid uuid, example for a valid uuid: " + UUID.randomUUID().toString());
                return;
            }
            boolean success = this.statsServer.getDatabaseProvider().getUserAuthenticator().deleteUser(uniqueId);
            if (success) {
                sender.sendMessage("Successfully deleted the user for the uuid " + uniqueId);
            } else {
                sender.sendMessage("A user with the uuid " + uniqueId + " does not exist");
            }
        } else if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
            Collection<UUID> userIds = this.statsServer.getDatabaseProvider().getUserAuthenticator().listUsers();
            if (userIds.isEmpty()) {
                sender.sendMessage("No users found");
            } else {
                Map<UUID, String> users = new HashMap<>();
                for (UUID uniqueId : userIds) {
                    users.put(uniqueId, this.statsServer.getDatabaseProvider().getName(uniqueId));
                }
                sender.sendMessage("Users registered in the database: \n" +
                        users.entrySet().stream()
                                .map(entry -> entry.getValue() != null ? (entry.getKey() + "#" + entry.getValue()) : entry.getKey().toString())
                                .collect(Collectors.joining("\n"))
                );
            }
        } else {
            sender.sendMessage(
                    "user add <uuid> [token]",
                    "user remove <uuid>",
                    "user list"
            );
        }
    }

    private UUID parseUUID(String input) {
        try {
            return UUID.fromString(input);
        } catch (IllegalArgumentException exception) {
            return this.statsServer.getDatabaseProvider().getUniqueId(input);
        }
    }
}
