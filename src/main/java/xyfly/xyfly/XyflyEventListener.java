package xyfly.xyfly;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public class XyflyEventListener implements Listener {

    private final Xyfly plugin;

    public XyflyEventListener(Xyfly plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (!player.hasPermission("xyfly.use")) {
            return;
        }

        // 玩家起飞设置
        if (event.isFlying()) {
            if (plugin.getFlyTimeMap().containsKey(playerId)) {
                int timeLeft = plugin.getFlyTimeMap().get(playerId);

                if (timeLeft <= 0) {
                    plugin.stopFlying(player);
                    player.sendMessage(plugin.getMessage("fly_time_expired"));
                    return;
                }

                // 启动倒计时任务
                if (!plugin.getFlyTaskMap().containsKey(playerId)) {
                    BukkitTask task = new BukkitRunnable() {
                        @Override
                        public void run() {
                            int newTimeLeft = plugin.getFlyTimeMap().get(playerId) - 1;
                            plugin.getFlyTimeMap().put(playerId, newTimeLeft);

                            // 使用 Spigot API 发送 Action Bar 消息
                            player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, new net.md_5.bungee.api.chat.TextComponent(ChatColor.GREEN + plugin.getConfig().getString("flyTimeMessage").replace("{time}", String.valueOf(newTimeLeft))));

                            if (newTimeLeft <= 0) {
                                plugin.stopFlying(player);
                                player.sendMessage(plugin.getMessage("fly_time_expired"));
                                cancel();
                            }
                        }
                    }.runTaskTimer(plugin, 20, 20);
                    plugin.getFlyTaskMap().put(playerId, task);
                }
            }
        } else {
            // 玩家降落
            if (plugin.getFlyTaskMap().containsKey(playerId)) {
                plugin.getFlyTaskMap().get(playerId).cancel();
                plugin.getFlyTaskMap().remove(playerId);
                player.sendMessage(plugin.getMessage("fly_time_paused"));
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // 玩家退出时停止飞行任务
        if (plugin.getFlyTaskMap().containsKey(playerId)) {
            plugin.getFlyTaskMap().get(playerId).cancel();
            plugin.getFlyTaskMap().remove(playerId);
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        // 检查事件中的实体是否是玩家
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            // 检查配置项是否启用了战斗状态下关闭飞行
            if (plugin.isDisableFlyInCombat() && player.isFlying()) {
                // 关闭玩家的飞行模式
                player.setFlying(false);
                player.setAllowFlight(false);

                // 发送消息通知玩家
                player.sendMessage("你进入了战斗状态，飞行模式已关闭！");
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            UUID playerId = player.getUniqueId();

            if (event.getCause() == EntityDamageEvent.DamageCause.FALL && plugin.noFallDamageMap.containsKey(playerId)) {
                event.setCancelled(true);
                plugin.noFallDamageMap.remove(playerId);
            }
        }
    }
}