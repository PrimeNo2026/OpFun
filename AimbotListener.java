package opfun;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class AimbotListener implements Listener {

    private final OpFun plugin;
    private final Set<UUID> active = Collections.synchronizedSet(new HashSet<>());

    public AimbotListener(OpFun plugin) {
        this.plugin = plugin;
    }

    public boolean toggle(Player p) {
        if (active.remove(p.getUniqueId())) return false;
        active.add(p.getUniqueId());
        return true;
    }

    public boolean isActive(Player p) {
        return active.contains(p.getUniqueId());
    }

    public void clearAll() {
        active.clear();
    }

    @EventHandler
    public void onShoot(ProjectileLaunchEvent e) {
        if (!(e.getEntity() instanceof Arrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player shooter)) return;
        if (!active.contains(shooter.getUniqueId())) return;

        LivingEntity target = findTarget(shooter);
        if (target == null) return;

        // Pfeil Richtung zum Ziel berechnen (Bogen-Optik: Partikel-Trail)
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                ticks++;
                if (!arrow.isValid() || arrow.isInBlock() || ticks > 120) {
                    cancel();
                    return;
                }
                if (!target.isValid() || target.isDead()) {
                    cancel();
                    return;
                }

                // Jedes Tick den Pfeil sanft auf das Ziel lenken
                Location arrowLoc = arrow.getLocation();
                Location targetLoc = target.getLocation().add(0, target.getHeight() / 2.0, 0);
                Vector toTarget = targetLoc.toVector().subtract(arrowLoc.toVector());
                double dist = toTarget.length();

                if (dist < 0.5) {
                    cancel();
                    return;
                }

                // Geschwindigkeit beibehalten, Richtung anpassen
                double speed = arrow.getVelocity().length();
                // Sanfte Lenkung: 30% zum Ziel, 70% aktuelle Richtung
                Vector newDir = arrow.getVelocity().multiply(0.7)
                        .add(toTarget.normalize().multiply(speed * 0.3));
                if (newDir.lengthSquared() > 0) {
                    newDir = newDir.normalize().multiply(speed);
                }
                arrow.setVelocity(newDir);

                // Partikel-Trail
                arrow.getWorld().spawnParticle(
                        Particle.CRIT,
                        arrowLoc, 2, 0.05, 0.05, 0.05, 0.01);
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    /** Findet den nächsten Spieler oder Mob in bis zu 60 Blöcken Entfernung. */
    private LivingEntity findTarget(Player shooter) {
        Location eye = shooter.getEyeLocation();
        LivingEntity best = null;
        double bestScore = Double.MAX_VALUE;

        for (Entity en : shooter.getWorld().getNearbyEntities(eye, 60, 60, 60)) {
            if (en.equals(shooter)) continue;
            if (!(en instanceof LivingEntity le)) continue;
            if (le.isDead()) continue;
            if (en instanceof Player p && (p.getGameMode() == org.bukkit.GameMode.SPECTATOR
                    || p.getGameMode() == org.bukkit.GameMode.CREATIVE)) continue;

            Location targetLoc = le.getLocation().add(0, le.getHeight() / 2.0, 0);
            Vector toTarget = targetLoc.toVector().subtract(eye.toVector());
            double dist = toTarget.length();
            if (dist < 0.5) continue;

            // Winkel zwischen Blickrichtung und Ziel
            double dot = eye.getDirection().dot(toTarget.normalize());
            // Score: bevorzuge nah + in Blickrichtung (dot nahe 1.0 = direkt davor)
            double score = dist * (2.0 - dot);

            if (score < bestScore) {
                bestScore = score;
                best = le;
            }
        }
        return best;
    }
}
