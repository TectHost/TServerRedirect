package minealex.tserverredirect;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Event;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.config.Configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Random;

@SuppressWarnings("unused")
public class TServerRedirect extends Plugin implements Listener {

    private List<String> lobbyServers;
    private List<String> modalityServers;

    @Override
    public void onEnable() {
        loadConfig();
        getProxy().getPluginManager().registerListener(this, this);
    }
    
    public void onServerConnect(ServerConnectEvent event) {
        if (event.getPlayer().getServer() != null && event.getTarget().getName().equals(event.getPlayer().getServer().getInfo().getName())) {
            event.setCancelled(true);
        }
    }

    private void loadConfig() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        File configFile = new File(getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, configFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
            lobbyServers = getLobbyServers(config);
            modalityServers = getModalityServers(config);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerLogin(PostLoginEvent event) {
        // Redirigir al jugador a un servidor de lobby aleatorio al ingresar
        if (lobbyServers.size() > 0) {
            ServerInfo lobbyServer = getProxy().getServerInfo(lobbyServers.get(new Random().nextInt(lobbyServers.size())));
            event.getPlayer().connect(lobbyServer);
        }
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        // Redirigir al jugador a un servidor de lobby si estaba en una modalidad
        if (!lobbyServers.contains(event.getPlayer().getServer().getInfo().getName())) {
            if (lobbyServers.size() > 0) {
                ServerInfo lobbyServer = getProxy().getServerInfo(lobbyServers.get(new Random().nextInt(lobbyServers.size())));
                event.getPlayer().connect(lobbyServer);
            }
        }
    }

    // Método de utilidad para verificar si un servidor es de modalidad
	private boolean isModalityServer(String serverName) {
        return modalityServers.contains(serverName);
    }
    
    private List<String> getLobbyServers(Configuration config) {
        return config.getStringList("lobby-servers");
    }

    private List<String> getModalityServers(Configuration config) {
        return config.getStringList("modality-servers");
    }

}
