package org.isnsest.corexizen;

import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.objects.ObjectFetcher;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import dev.corexinc.corex.Corex;
import org.bukkit.plugin.java.JavaPlugin;
import org.isnsest.corexizen.commands.corex.DenizenCommand;
import org.isnsest.corexizen.commands.denizen.CorexCommand;

public final class Corexizen extends JavaPlugin {

    @Override
    public void onEnable() {
        Debug.log("Corexizen", "Loading...");

        Utils.scriptQueue.setKeepAlive(true);
        Utils.scriptQueue.setSilent(true);
        Utils.scriptQueue.start();

        registerForDenizen();
        registerForCorex();

        Debug.log("Corexizen", "Loaded successfully!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static void registerForDenizen() {
        Utils.registerEventHooks();

        DenizenCore.commandRegistry.registerCommand(CorexCommand.class);

        // <--[tag]
        // @attribute <corex[<tag>]>
        // @returns ObjectTag
        // @description
        // Returns the result of a Corex tag parsed through the Corex engine.
        // This allows you to access Corex-specific data and placeholders directly from Denizen.
        // The resulting value is automatically converted into the most appropriate Denizen ObjectTag.
        // -->
        TagManager.registerTagHandler(ObjectTag.class, "corex", (attribute) -> {
            String tag = Utils.parseCorexTag(attribute.getRawParam(), (BukkitTagContext) attribute.context);
            return ObjectFetcher.pickObjectFor(tag, attribute.context);
        });
    }

    public static void registerForCorex() {
        Corex.getInstance().getRegistry().register(
                DenizenCommand.class,
                DenizenFormatter.class
        );
    }
}
