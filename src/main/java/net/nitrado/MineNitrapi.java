package net.nitrado;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;

public class MineNitrapi extends JavaPlugin implements Listener {

    public static MineNitrapi instance;

    public void loadConfiguration() {
        this.getConfig().addDefault("APP_ID","Enter APP_ID here");
        this.getConfig().addDefault("APP_SECRET","Enter APP_SECRET here");
        this.getConfig().addDefault("IP","Enter IP here");
        this.getConfig().addDefault("PORT",8080);
        this.getConfig().addDefault("SCOPES","user_info service service_order ssh_keys");
        this.getConfig().addDefault("CREDENTIALS_PATH","/ftproot/minecraftbukkit/plugins/Nitrapi-Minecraft/");
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
    }

    public String getAppId(){
        return getConfig().getString("APP_ID");
    }
    public String getAppSecret(){
        return getConfig().getString("APP_SECRET");
    }
    public String getIp(){
        return getConfig().getString("IP");
    }
    public String getScopes(){
        return getConfig().getString("SCOPES");
    }
    public String getCredentialsPath(){
        return getConfig().getString("CREDENTIALS_PATH");
    }
    public int getPort(){
        return getConfig().getInt("PORT");
    }

    @Override
    public void onLoad(){
        instance = this;
    }

    @Override
    public void onEnable(){
        loadConfiguration();
        /* Disable the plugin if the config is not valid */
        if (!NitrapiCommandExecutor.isConfigValid()){
            System.out.println("-------------------------------------------------------------------------");
            System.out.println("ERROR: PLEASE ENTER CONFIG VALUES in plugins/Nitrapi-Minecraft/config.yml");
            System.out.println("                ERROR: THE PLUGIN WILL NOW DISABLE ITSELF                ");
            System.out.println("-------------------------------------------------------------------------");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        } else {
            try {
                Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
                    try {
                        System.out.println("Check if auth is already done");
                        System.out.println(NitrapiAuth.ping());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Please execute /nitrapi auth");
            }
        }
        /* register events and the nitrapi command */
        getServer().getPluginManager().registerEvents(this, this);
        this.getCommand("nitrapi").setExecutor(new NitrapiCommandExecutor(this));
    }

    /* useless but polite */
    @Override
    public void onDisable(){
        this.getLogger().info("MineNitrapi says goodbye");
    }

    /* Telling great news */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        p.sendMessage("§6MineNitrapi Admin Interface");
        p.sendMessage("§6start with /nitrapi auth");
    }

}
