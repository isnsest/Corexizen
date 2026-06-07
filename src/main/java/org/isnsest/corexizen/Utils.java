package org.isnsest.corexizen;

import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import dev.corexinc.corex.api.containers.AbstractContainer;
import dev.corexinc.corex.api.tags.AbstractTag;
import dev.corexinc.corex.engine.compiler.Instruction;
import dev.corexinc.corex.engine.compiler.ScriptCompiler;
import dev.corexinc.corex.engine.queue.ScriptQueue;
import dev.corexinc.corex.engine.scripts.ScriptManager;
import dev.corexinc.corex.engine.tags.ObjectFetcher;
import dev.corexinc.corex.environment.tags.core.MapTag;
import dev.corexinc.corex.environment.tags.player.PlayerTag;

import java.util.List;

public class Utils {

    public static ScriptQueue scriptQueue = new ScriptQueue(
            "DENIZEN_" + System.currentTimeMillis(),
            new Instruction[0],
            false,
            null
    );

    public static String parseCorexTag(String tag, BukkitTagContext context) {
        scriptQueue.define("__player", null);
        if (context.player != null) {
            scriptQueue.define("__player", new PlayerTag(context.player.getPlayerEntity()));
        }
        return ScriptCompiler.parseArg(tag).evaluate(scriptQueue).identify();
    }

    public static AbstractTag parseCorexTagObject(String tag) {
        return ObjectFetcher.pickObject(tag);
    }

    public static void runCorexTask(ScriptEntry scriptEntry, String scriptName, String path, String definitions, String id) {

        AbstractContainer container = ScriptManager.getContainer(scriptName);
        if (container == null) {
            Debug.echoError(scriptEntry, "Container '" + scriptName + "' not found!");
            return;
        }

        Instruction[] bytecode = container.getScript(path);
        if (bytecode == null) {
            Debug.echoError(scriptEntry, "Path '" + path + "' doesn't contain any commands in " + scriptName);
            return;
        }

        PlayerTag playerTag = null;
        if (Utilities.getEntryPlayer(scriptEntry) != null) {
            playerTag = new PlayerTag(Utilities.getEntryPlayer(scriptEntry).getPlayerEntity());
        }
        ScriptQueue newQueue = new ScriptQueue(id, bytecode, false, playerTag);

        if (definitions != null) {
            AbstractTag defTag = ObjectFetcher.pickObject(definitions);

            if (defTag instanceof MapTag map) {
                for (String key : map.keySet()) {
                    AbstractTag val = map.getObject(key);

                    newQueue.define(key, val);
                }
            } else {
                ListTag list = (defTag instanceof ListTag) ? (ListTag) defTag : new ListTag(defTag.identify());

                List<String> keys = container.getDefinitions();

                for (int i = 0; i < list.size() && i < keys.size(); i++) {
                    AbstractTag val = ObjectFetcher.pickObject(list.get(i));

                    newQueue.define(keys.get(i), val);
                }
            }
        }

        newQueue.start();
    }

}
