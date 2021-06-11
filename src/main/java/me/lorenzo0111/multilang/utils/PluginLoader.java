package me.lorenzo0111.multilang.utils;

import me.lorenzo0111.multilang.MultiLangPlugin;
import me.lorenzo0111.multilang.api.IMultiLangAPI;
import me.lorenzo0111.multilang.api.impl.MultiLangAPI;
import me.lorenzo0111.multilang.commands.AdminLangCommand;
import me.lorenzo0111.multilang.commands.MultiLangCommand;
import me.lorenzo0111.multilang.handlers.ConfigManager;
import me.lorenzo0111.pluginslib.command.Customization;
import me.lorenzo0111.pluginslib.config.ConfigExtractor;
import me.lorenzo0111.rocketplaceholders.api.RocketPlaceholdersAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.ServicePriority;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public final class PluginLoader {
    private final MultiLangPlugin plugin;
    private FileConfiguration guiConfig;
    private File guiFile;

    public PluginLoader(MultiLangPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean init() {
        plugin.getLogger().info("Hooking with RocketPlaceholders..");

        RocketPlaceholdersAPI api = Bukkit.getServicesManager().load(RocketPlaceholdersAPI.class);

        if (api != null) {
            plugin.getLogger().info("RocketPlaceholders hooked!");
            plugin.setRocketPlaceholdersAPI(api);
        } else {
            plugin.getLogger().severe("Unable to find RocketPlaceholdersAPI, disabling..");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return false;
        }

        plugin.setConfigManager(new ConfigManager(plugin));
        plugin.getConfigManager().parse();
        plugin.getConfigManager().register();
        return true;
    }

    public void metrics() {

    }

    public void api() {
        plugin.getLogger().info("Initializing api..");
        final MultiLangAPI api = new MultiLangAPI(plugin);
        Bukkit.getServicesManager().register(IMultiLangAPI.class,api,plugin, ServicePriority.Normal);
    }

    public void commands() {
        plugin.getLogger().info("Registering commands..");
        String prefix = plugin.getConfig().getString("prefix");
        final Customization customization = new Customization("&8[&9MultiLang&8] &7Running &9" + plugin.getDescription().getName() + " &7v&9" + plugin.getDescription().getVersion() + " &7by &9" + plugin.getDescription().getAuthors(),prefix + "&cCommand not found. Try to use &8/$cmd help&7.",prefix + "&7Run &8/$cmd help &7for a command list.");
        AdminLangCommand acmd = new AdminLangCommand(plugin,"multilangadmin",customization);
        MultiLangCommand cmd = new MultiLangCommand(plugin,"multilang",customization);
        Objects.requireNonNull(plugin.getCommand("multilang")).setTabCompleter(cmd);
        Objects.requireNonNull(plugin.getCommand("multilangadmin")).setTabCompleter(acmd);
    }

    public void gui() {
        plugin.getLogger().info("Loading gui.yml..");
        guiFile = new File(plugin.getDataFolder(),"gui.yml");
        if (!guiFile.exists()) {
            guiFile = new ConfigExtractor(MultiLangPlugin.class,plugin.getDataFolder(),"gui.yml")
                    .extract();
        }

        this.reloadGui();

    }

    public void reloadGui() {
        guiConfig = new YamlConfiguration();
        try {
            Objects.requireNonNull(guiFile);
            guiConfig.load(guiFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public MultiLangPlugin getPlugin() {
        return plugin;
    }

    public FileConfiguration getGuiConfig() {
        return guiConfig;
    }
}
