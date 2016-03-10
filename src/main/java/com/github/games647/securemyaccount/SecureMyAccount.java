package com.github.games647.securemyaccount;

import com.github.games647.securemyaccount.commands.EnableCommand;
import com.github.games647.securemyaccount.commands.LoginCommand;
import com.github.games647.securemyaccount.listener.PreventListener;
import com.github.games647.securemyaccount.listener.SessionListener;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SecureMyAccount extends JavaPlugin {

    private final Set<UUID> sessions = Sets.newConcurrentHashSet();
    private final Map<UUID, Account> cache = Maps.newConcurrentMap();

    @Override
    public void onEnable() {
        saveDefaultConfig();

        //register commands
        getCommand(getName().toLowerCase()).setExecutor(new EnableCommand(this));
        getCommand("startsession").setExecutor(new LoginCommand(this));

        //register listeners
        getServer().getPluginManager().registerEvents(new PreventListener(this), this);
        getServer().getPluginManager().registerEvents(new SessionListener(this), this);
    }

    @Override
    public void onDisable() {
        sessions.clear();
        cache.clear();
    }

    //thread-safe
    public void startSession(Player player) {
        sessions.add(player.getUniqueId());
    }

    //thread-safe
    public boolean isInSession(Player player) {
        return sessions.contains(player.getUniqueId());
    }

    //thread-safe
    public void endSession(Player player) {
        sessions.remove(player.getUniqueId());
        cache.remove(player.getUniqueId());
    }

    //todo make async
    public Account getOrLoadAccount(Player player) {
        UUID uniqueId = player.getUniqueId();
        Account account = cache.get(uniqueId);
        if (account == null) {
            File file = new File(getDataFolder(), uniqueId.toString());
            if (file.exists()) {
                try {
                    List<String> lines = Files.readAllLines(file.toPath());
                    String secretCode = lines.get(0);
                    String ip = lines.get(1);

                    account = new Account(uniqueId, secretCode, ip);
                    cache.put(uniqueId, account);
                } catch (IOException ex) {
                    getLogger().log(Level.SEVERE, "Error loading account", ex);
                }
            } else {
                account = new Account(uniqueId);
                cache.put(uniqueId, account);
            }
        }

        return account;
    }

    //todo make async
    public boolean saveAccount(Player player) {
        UUID uniqueId = player.getUniqueId();
        Account account = cache.get(uniqueId);
        if (account != null) {
            File file = new File(getDataFolder(), uniqueId.toString());

            List<String> lines = Lists.newArrayList(account.getSecretCode(), account.getIp());
            try {
                Files.write(file.toPath(), lines);
                return true;
            } catch (IOException ex) {
                getLogger().log(Level.SEVERE, "Error saving account", ex);
            }
        }

        return false;
    }
}
