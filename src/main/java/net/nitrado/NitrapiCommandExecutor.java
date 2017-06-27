package net.nitrado;

import com.google.api.client.auth.oauth2.Credential;
import net.nitrado.api.Nitrapi;
import net.nitrado.api.common.exceptions.NitrapiConcurrencyException;
import net.nitrado.api.common.exceptions.NitrapiErrorException;
import net.nitrado.api.common.exceptions.NitrapiHttpException;
import net.nitrado.api.common.exceptions.NitrapiMaintenanceException;
import net.nitrado.api.customer.Customer;
import net.nitrado.api.services.Service;
import net.nitrado.api.services.ServiceDetails;
import net.nitrado.api.services.gameservers.Game;
import net.nitrado.api.services.gameservers.Gameserver;
import net.nitrado.api.services.gameservers.GlobalGameList;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.net.*;

public class NitrapiCommandExecutor implements CommandExecutor{
    private final MineNitrapi plugin;

    public NitrapiCommandExecutor(MineNitrapi plugin){
        this.plugin = plugin;
    }

    public static boolean isConfigValid() {
        if (MineNitrapi.instance.getAppId().startsWith("Enter ") ||
                MineNitrapi.instance.getAppSecret().startsWith("Enter ") ||
                MineNitrapi.instance.getIp().startsWith("Enter ") ||
                MineNitrapi.instance.getCredentialsPath().startsWith("Enter ") ||
                MineNitrapi.instance.getScopes().startsWith("Enter ")) {
            return false;
        }
        return true;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd , String commandLabel, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("§7Not enough arguments!");
            sender.sendMessage("§7/nitrapi <auth|check|whoami|games|services|restart|backup");
            return true;
        }

        /* auth against nitrapi */
        if(args[0].equalsIgnoreCase("auth")) {
            if (!isConfigValid()){
                String line1 = "Please enter config values in plugins/Nitrapi-Minecraft/config.yml\n";
                String line2 = "The plugin will now disable itself";
                sender.sendMessage("§4" + line1 + "§4" + line2);
                Bukkit.getPluginManager().disablePlugin(plugin);
            }
            try {
                System.out.println("Setup Nitrapi Credentials");
                sender.sendMessage("§4Please click the following link for authorization");
                /* send url to the client */
                String url = NitrapiAuth.AUTHORIZATION_SERVER_URL + "?client_id=" + MineNitrapi.instance.getAppId()
                        + "&redirect_uri=http://" + MineNitrapi.instance.getIp() + ":" + MineNitrapi.instance.getPort()
                        + "/Callback&response_type=code&scope=" + URLEncoder.encode(MineNitrapi.instance.getScopes(),"UTF-8") + "&state=asd7wdhw7dwhe92";
                sender.sendMessage(url);
                /* if not async, the server dies */
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    try {
                        System.out.println(NitrapiAuth.ping());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /* global credentials */
        final Credential credential;
        try {
            credential = NitrapiAuth.authorize();
            String access_token = credential.getAccessToken();
            /* new instance of Nitrapi with access_token */
            Nitrapi api = new Nitrapi(access_token);
            switch (args[0]){

                /* Check if Nitrapi is working */
                case "check":
                    if(api.ping()) {
                        sender.sendMessage("§7API is running fine");
                        return true;
                    }
                    break;

                /* get services from Nitrapi */
                case "services":
                    if (args.length > 1) {
                        sender.sendMessage("§7Too many arguments!");
                        return false;
                    }
                    Service[] services = api.getServices();
                    for (Service s: services
                            ) {
                        sender.sendMessage("§b-- Service ID " + s.getId() + " --");
                        sender.sendMessage("§7Type = " + s.getTypeHuman());
                        sender.sendMessage("§7Comment = " + s.getComment());
                        sender.sendMessage("§7Status = " + s.getStatus().name());
                        sender.sendMessage("");
                    }
                    return true;

                /* get games from Nitrapi */
                case "games":
                    GlobalGameList globalGameList = api.getGames();
                    Game[] games = globalGameList.getAllGames();
                    for (Game game: games
                            ) {
                        sender.sendMessage("§7" + game.getName());
                    }
                    return true;

                /* get userid and username for the access_token*/
                case "whoami":
                    Customer customer = api.getCustomer();
                    sender.sendMessage("§7You're ID: §3"+ customer.getUserId() + " §7Name: §3" + customer.getUsername());
                    return true;

                /* restart a service via Nitrapi */
                case "restart":
                    if (args.length < 2) {
                        sender.sendMessage("§7Not enough arguments!");
                        sender.sendMessage("§7/nitrapi restart <id>");
                        return true;
                    }
                    Service[] services1 = api.getServices();
                    try {
                        int id = Integer.parseInt(args[1]);
                        for (Service s:services1
                                ) {
                            if(s.getId() == id){
                                Gameserver this_server = (Gameserver) s;
                                sender.sendMessage("§7MineNitrapi will restart ID: " + id);
                                this_server.doRestart("MineNitrapi restarts this");
                                return true;
                            }
                        }
                        sender.sendMessage("§7This Service does not belong to you, sorry.");
                        sender.sendMessage("§7Try '/nitrapi services' for service ids");
                        return true;
                    } catch (Exception e){
                        sender.sendMessage("§7It seems this was not a Number");
                        sender.sendMessage("§7Usage: /nitrapi restart 123123");
                        return true;
                    }

                /* backups of the players world */
                case "backup":
                    if (args.length < 2) {
                        sender.sendMessage("§7Not enough arguments!");
                        sender.sendMessage("§7/nitrapi backup <create|restore>");
                        return true;
                    }

                    /* create or restore */
                    switch (args[1]){
                        default:
                            sender.sendMessage("§7Wrong argument");
                            sender.sendMessage("/nitrapi backup <create|restore>");
                            return true;

                        /* creates a backup of the players current world*/
                        case "create":
                            Service[] services2 = api.getServices();
                            Player p = (Player) sender;
                            World w = p.getWorld();
                            for (Service s: services2
                                    ) {
                                ServiceDetails d = s.getDetails();
                                String ip = d.getAddress();
                                if(ip != null && !ip.isEmpty()) {
                                    ip = ip.split(":")[0];
                                    if(ip.equals(MineNitrapi.instance.getIp())) {
                                        Gameserver g = (Gameserver) s;
                                        /* world backups need the gamename with / (name of the folder)*/
                                        g.getMinecraft().createBackup("minecraftbukkit/" + w.getName());
                                        sender.sendMessage("§7Backup dieser Welt wird erstellt");
                                    }
                                }
                            }
                            return true;

                        /* restore a backup */
                        case "restore":
                            if (args.length < 3) {
                                sender.sendMessage("§7Not enough arguments!");
                                sender.sendMessage("§7/nitrapi backup restore <list|timestamp>");
                                return true;
                            }
                            Service[] services3 = api.getServices();

                            /* list backups or restore by timestamp */
                            switch (args[2]){
                                default:
                                    /* restore by timestamp */
                                    if (args[2].matches("[0-9]*")){
                                        long timestamp = Long.valueOf(args[2]);
                                        for (Service s: services3
                                                ) {
                                            if (s instanceof  Gameserver){
                                                Gameserver g = (Gameserver) s;
                                                if (g.isMinecraftGame()){
                                                    net.nitrado.api.services.gameservers.minecraft.World[] worlds = g.getMinecraft().getWorldBackups();
                                                    for (net.nitrado.api.services.gameservers.minecraft.World w2: worlds
                                                            ) {
                                                        if (w2.getTimestamp() == timestamp) {
                                                            sender.sendMessage("§7Restoring Backup for " + w2.getGame() + "/" + w2.getWorld() + " Timestamp: " + w2.getTimestamp());
                                                            sender.sendMessage("Server is going down for reboot");
                                                            g.getMinecraft().restoreBackup(timestamp);
                                                            return true;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    sender.sendMessage("§7Wrong argument");

                                /* get a clickable list for restore */
                                case "list":
                                    for (Service s: services3
                                         ) {
                                        if (s instanceof  Gameserver){
                                            Gameserver g = (Gameserver) s;
                                            Player p1 = (Player) sender;
                                            if (g.isMinecraftGame()){
                                                net.nitrado.api.services.gameservers.minecraft.World[] worlds = g.getMinecraft().getWorldBackups();
                                                sender.sendMessage("§7---Available Backups:---");
                                                for (net.nitrado.api.services.gameservers.minecraft.World w2: worlds
                                                        ) {
                                                    String backup = "Service ID:" + s.getId() + " World: " + w2.getGame() + "/" + w2.getWorld() + " \n§bTimestamp: " + w2.getTimestamp();
                                                    /* print backups clickable */
                                                    Bukkit.getServer().dispatchCommand(
                                                            Bukkit.getConsoleSender(),
                                                            "tellraw " + p1.getName() +
                                                                    " {\"text\":\"§7" + backup + "\n\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/nitrapi backup restore " +
                                                                    w2.getTimestamp() + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"Click to restrore backup\"}}");
                                                }
                                            }
                                        }
                                    }
                                    return true;
                            }
                    }

                default:
                    sender.sendMessage("§7Sorry, but this command is unknown");
                    break;
            }
        } catch (NitrapiErrorException e) {
            // There was an error in our request to the api.
            System.out.println("API: Request error = " + e.getMessage());
        } catch (NitrapiHttpException e) {
            // There was an error connecting to the api.
            System.out.println("API: Connection error = " + e.getMessage());
        } catch (NitrapiConcurrencyException e) {
            // The same action is already running.
            System.out.println("API: Concurrent error = " + e.getMessage());
        } catch (NitrapiMaintenanceException e) {
            // The Nitrapi is currently down for maintenance.
            System.out.println("API: maintenance error = " + e.getMessage());
        } catch (RuntimeException ex) {
            System.err.printf("Error executing command %s", args[0]);
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage("§4Error in console");
            sender.sendMessage("§4Please auth first, if you didn't");
        }
        return false;
    }
}
