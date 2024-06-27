package xyfly.xyfly;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class XyflyCommandExecutor implements CommandExecutor, TabCompleter {
    private final Xyfly plugin;

    public XyflyCommandExecutor(Xyfly plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 如果没有参数，显示用法信息
        if (args.length == 0) {
            sender.sendMessage(plugin.getMessage("usage"));
            return false;
        }

        // 处理 "on" 命令，开启飞行模式
        if (args[0].equalsIgnoreCase("on")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.getMessage("only_players"));
                return true;
            }
            Player player = (Player) sender;
            if (!player.hasPermission("xyfly.on")) {
                player.sendMessage(plugin.getMessage("no_permission"));
                return true;
            }
            plugin.handleFlyOn(player);
        }
        // 处理 "off" 命令，关闭飞行模式
        else if (args[0].equalsIgnoreCase("off")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.getMessage("only_players"));
                return true;
            }
            Player player = (Player) sender;
            if (!player.hasPermission("xyfly.off")) {
                player.sendMessage(plugin.getMessage("no_permission"));
                return true;
            }
            plugin.stopFlying(player);
            player.sendMessage(plugin.getMessage("fly_off"));
        }
        // 处理 "settime" 命令，设置飞行时间
        else if (args[0].equalsIgnoreCase("settime")) {
            if (args.length < 2 || args.length > 3) {
                sender.sendMessage(plugin.getMessage("usage"));
                return false;
            }

            Player target;
            int timeIndex;

            if (args.length == 2) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(plugin.getMessage("only_players"));
                    return true;
                }
                target = (Player) sender;
                timeIndex = 1;
            } else {
                target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(plugin.getMessage("player_not_online").replace("{player}", args[1]));
                    return true;
                }
                timeIndex = 2;
            }

            int time;
            try {
                time = Integer.parseInt(args[timeIndex]);
            } catch (NumberFormatException e) {
                sender.sendMessage(plugin.getMessage("invalid_time"));
                return true;
            }

            plugin.getFlyTimeMap().put(target.getUniqueId(), time);
            sender.sendMessage(plugin.getMessage("fly_time_set").replace("{player}", target.getName()).replace("{time}", String.valueOf(time)));
            target.sendMessage(plugin.getMessage("fly_time_set_target").replace("{time}", String.valueOf(time)));
        }
        // 处理 "gettime" 命令，获取剩余飞行时间
        else if (args[0].equalsIgnoreCase("gettime")) {
            if (args.length < 2) {
                sender.sendMessage(plugin.getMessage("usage"));
                return false;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(plugin.getMessage("player_not_online").replace("{player}", args[1]));
                return true;
            }

            int remainingTime = plugin.getRemainingFlyTime(target);
            sender.sendMessage(plugin.getMessage("remaining_fly_time").replace("{player}", target.getName()).replace("{time}", String.valueOf(remainingTime)));
        }
        // 处理 "remove" 命令，移除飞行时间
        else if (args[0].equalsIgnoreCase("remove")) {
            if (args.length < 2) {
                sender.sendMessage(plugin.getMessage("usage"));
                return false;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(plugin.getMessage("player_not_online").replace("{player}", args[1]));
                return true;
            }

            int removedTime = plugin.getFlyTimeMap().remove(target.getUniqueId());
            sender.sendMessage(plugin.getMessage("fly_time_removed").replace("{player}", target.getName()).replace("{time}", String.valueOf(removedTime)));
            target.sendMessage(plugin.getMessage("fly_time_removed_target").replace("{time}", String.valueOf(removedTime)));
        }
        // 处理 "addtime" 命令，增加飞行时间
        else if (args[0].equalsIgnoreCase("addtime")) {
            if (args.length < 2 || args.length > 3) {
                sender.sendMessage(plugin.getMessage("usage"));
                return false;
            }

            Player target;
            int timeIndex;

            if (args.length == 2) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(plugin.getMessage("only_players"));
                    return true;
                }
                target = (Player) sender;
                timeIndex = 1;
            } else {
                target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(plugin.getMessage("player_not_online").replace("{player}", args[1]));
                    return true;
                }
                timeIndex = 2;
            }

            int additionalTime;
            try {
                additionalTime = Integer.parseInt(args[timeIndex]);
            } catch (NumberFormatException e) {
                sender.sendMessage(plugin.getMessage("invalid_time"));
                return true;
            }

            int currentTime = plugin.getFlyTimeMap().getOrDefault(target.getUniqueId(), 0);
            int newTime = currentTime + additionalTime;

            plugin.getFlyTimeMap().put(target.getUniqueId(), newTime);
            sender.sendMessage(plugin.getMessage("fly_time_added").replace("{player}", target.getName()).replace("{time}", String.valueOf(additionalTime)));
            target.sendMessage(plugin.getMessage("fly_time_added_target").replace("{time}", String.valueOf(additionalTime)));
        }
        // 处理 "reload" 命令，重新加载配置文件
        else if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("xyfly.reload")) {
                sender.sendMessage(plugin.getMessage("no_permission"));
                return true;
            }

            plugin.reloadConfig();
            plugin.publicLoadMessagesConfig();  // 使用公共方法调用 loadMessagesConfig()
            sender.sendMessage(plugin.getMessage("config_reloaded"));
        }
        // 处理未知命令，显示用法信息
        else {
            sender.sendMessage(plugin.getMessage("usage"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // 自动补全命令参数
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            completions.add("on");
            completions.add("off");
            completions.add("settime");
            completions.add("gettime");
            completions.add("remove");
            completions.add("addtime");
            completions.add("reload");
            return completions;
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("settime") || args[0].equalsIgnoreCase("gettime") || args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("addtime"))) {
            List<String> playerNames = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                playerNames.add(player.getName());
            }
            return playerNames;
        }
        return null;
    }
}