package opfun;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class OpCommands implements CommandExecutor {

    private final OpFun plugin;
    private final AimbotListener aimbot;

    public OpCommands(OpFun plugin, AimbotListener aimbot) {
        this.plugin = plugin;
        this.aimbot = aimbot;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(Component.text("Nur Spieler können das benutzen.", NamedTextColor.RED));
            return true;
        }
        if (!p.hasPermission("opfun.op")) {
            p.sendMessage(Component.text("Dafür hast du keine Berechtigung.", NamedTextColor.RED));
            return true;
        }

        switch (cmd.getName().toLowerCase()) {
            case "ooooooooo" -> handleAimbot(p);
            case "loool" -> handleArmor(p);
        }
        return true;
    }

    // ------------------------------------------------------------------ Aimbot

    private void handleAimbot(Player p) {
        boolean on = aimbot.toggle(p);
        if (on) {
            p.sendMessage(Component.text("🎯 Aimbot aktiviert! Deine Pfeile verfolgen Ziele.", NamedTextColor.GREEN));
            p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 2f);
        } else {
            p.sendMessage(Component.text("Aimbot deaktiviert.", NamedTextColor.GRAY));
            p.playSound(p.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1f, 0.8f);
        }
    }

    // ------------------------------------------------------------------ Diamant-Ruestung

    private void handleArmor(Player p) {
        record Piece(Material mat, boolean featherFalling) {}
        Piece[] pieces = {
            new Piece(Material.DIAMOND_HELMET,      false),
            new Piece(Material.DIAMOND_CHESTPLATE,  false),
            new Piece(Material.DIAMOND_LEGGINGS,    false),
            new Piece(Material.DIAMOND_BOOTS,       true),
        };

        ItemStack[] armor = new ItemStack[4];
        for (int i = 0; i < pieces.length; i++) {
            ItemStack item = new ItemStack(pieces[i].mat());
            ItemMeta meta = item.getItemMeta();
            meta.addEnchant(Enchantment.PROTECTION,      5, true);
            meta.addEnchant(Enchantment.UNBREAKING,      4, true);
            meta.addEnchant(Enchantment.MENDING,         1, true);
            if (pieces[i].featherFalling()) meta.addEnchant(Enchantment.FEATHER_FALLING, 4, true);
            item.setItemMeta(meta);
            armor[i] = item;
        }

        // Slots: 0 = Helm, 1 = Brust, 2 = Hose, 3 = Schuhe
        p.getInventory().setHelmet(armor[0]);
        p.getInventory().setChestplate(armor[1]);
        p.getInventory().setLeggings(armor[2]);
        p.getInventory().setBoots(armor[3]);

        p.sendMessage(Component.text("💎 Diamant-Rüstung angelegt!", NamedTextColor.AQUA));
        p.playSound(p.getLocation(), org.bukkit.Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1f, 1f);
    }
}
