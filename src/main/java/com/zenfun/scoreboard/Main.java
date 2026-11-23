package com.zenfun.scoreboard;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Main extends JavaPlugin {
    private Economy econ = null;
    private boolean usePAPI = false;

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().warning("Vault Economy not found!");
        }
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            usePAPI = true;
        }
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                updateScoreboard(p);
            }
        }, 0L, 20L);
        getLogger().info("DarkScoreboard Enabled!");
    }

    private boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        econ = rsp.getProvider();
        return econ != null;
    }

    public String formatMoney(double money) {
        if (money >= 1000000000) return String.format("%.2fB", money / 1000000000.0);
        if (money >= 1000000) return String.format("%.2fM", money / 1000000.0);
        if (money >= 1000) return String.format("%.2fK", money / 1000.0);
        return String.format("%.0f", money);
    }

    public String getPAPI(Player p, String ph) {
        if (!usePAPI) return "0";
        String out = PlaceholderAPI.setPlaceholders(p, ph);
        if (out == null || out.contains("%") || out.equalsIgnoreCase("null")) return "0";
        return out;
    }

    public void updateScoreboard(Player p) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("dark", "dummy", Component.text("ZENFUN MC").color(NamedTextColor.RED).decorate(TextDecoration.BOLD));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        String date = new SimpleDateFormat("MMM dd, yyyy").format(new Date());
        double money = econ != null ? econ.getBalance(p) : 0;

        String credits = getPAPI(p, "%player_credits%");
        String gems = getPAPI(p, "%gems%");
        String team = getPAPI(p, "%ultimateteams_team_name%");
        if (team.equals("0") || team.isEmpty()) team = "N/A";
        String keyall = getPAPI(p, "%keyall_time%");
        if (keyall.equals("0")) keyall = "No Keyall";

        int kills = p.getStatistic(Statistic.PLAYER_KILLS);
        int deaths = p.getStatistic(Statistic.DEATHS);
        int online = Bukkit.getOnlinePlayers().size();

        // Score must be unique, use descending scores
        setLine(board, obj, 11, Component.text("🕒 " + date).color(NamedTextColor.GRAY));
        setLine(board, obj, 10, Component.text("👤 " + p.getName()).color(NamedTextColor.GREEN));
        setLine(board, obj, 9, Component.text(""));
        setLine(board, obj, 8, Component.text("💚 Money: $" + formatMoney(money)).color(NamedTextColor.GREEN));
        setLine(board, obj, 7, Component.text("🧡 Credits: " + credits).color(NamedTextColor.GOLD));
        setLine(board, obj, 6, Component.text("💰 Gems: " + gems).color(NamedTextColor.LIGHT_PURPLE));
        setLine(board, obj, 5, Component.text("⚔️ Kills: " + kills).color(NamedTextColor.RED));
        setLine(board, obj, 4, Component.text("🔥 Deaths: " + deaths).color(NamedTextColor.GOLD));
        setLine(board, obj, 3, Component.text("👥 Team: " + team).color(NamedTextColor.AQUA));
        setLine(board, obj, 2, Component.text("🗝️ Keyall: " + keyall).color(NamedTextColor.YELLOW));
        setLine(board, obj, 1, Component.text("👥 Online: " + online).color(NamedTextColor.LIGHT_PURPLE));
        setLine(board, obj, 0, Component.text("WWW.ZENFUNMC.SITE").color(NamedTextColor.YELLOW));

        p.setScoreboard(board);
    }

    private void setLine(Scoreboard board, Objective obj, int score, Component text) {
        String entry = "§" + score + "§r";
        // Use unique entry to avoid duplicates
        String unique = org.bukkit.ChatColor.values()[score].toString() + org.bukkit.ChatColor.RESET;
        obj.getScore(unique).setScore(score);
        // Paper 1.26.2 - use Team for prefix
        org.bukkit.scoreboard.Team team = board.getTeam("line" + score);
        if (team == null) team = board.registerNewTeam("line" + score);
        team.addEntry(unique);
        team.prefix(text);
    }
                }
