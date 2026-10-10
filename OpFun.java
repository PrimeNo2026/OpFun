package opfun;

import org.bukkit.plugin.java.JavaPlugin;

public final class OpFun extends JavaPlugin {

    private AimbotListener aimbot;

    @Override
    public void onEnable() {
        aimbot = new AimbotListener(this);
        getServer().getPluginManager().registerEvents(aimbot, this);

        OpCommands cmds = new OpCommands(this, aimbot);
        var ooo = getCommand("ooooooooo");
        if (ooo != null) ooo.setExecutor(cmds);
        var loool = getCommand("loool");
        if (loool != null) loool.setExecutor(cmds);

        getLogger().info("OpFun aktiviert.");
    }

    @Override
    public void onDisable() {
        if (aimbot != null) aimbot.clearAll();
    }
}
