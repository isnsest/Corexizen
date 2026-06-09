package org.isnsest.corexizen;

import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.events.ScriptEvent;
import com.denizenscript.denizencore.events.core.ScriptGeneratesErrorScriptEvent;
import dev.corexinc.corex.api.tags.AbstractTag;
import dev.corexinc.corex.engine.compiler.Instruction;
import dev.corexinc.corex.engine.compiler.ScriptCompiler;
import dev.corexinc.corex.engine.queue.ScriptQueue;
import dev.corexinc.corex.engine.registry.CommandMetadata;
import dev.corexinc.corex.engine.registry.ScriptCommandRegistry;
import dev.corexinc.corex.engine.tags.ObjectFetcher;
import dev.corexinc.corex.environment.tags.player.PlayerTag;

public class Utils {

    public static ScriptGeneratesErrorScriptEvent err;

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

    public static void registerEventHooks() {
        err = ScriptGeneratesErrorScriptEvent.instance;
        ScriptGeneratesErrorScriptEvent.instance = new ScriptGeneratesErrorScriptEvent() {

            @Override
            public ScriptEvent fire() {
                if (this.message.contains("Unknown command")) {
                    boolean cancel = false;

                    outerLoop:
                    for (CommandMetadata metadata : ScriptCommandRegistry.getCommands().values()) {
                        for (String alias : metadata.command.getAlias()) {
                            if (this.message.contains(alias)) {
                                cancel = true;
                                break outerLoop;
                            }
                        }
                        if (this.message.contains(metadata.command.getName())) {
                            cancel = true;
                            break;
                        }
                    }
                    if (cancel) {
                        this.cancelled = true;
                        this.cancellationChanged();
                        return null;
                    }
                }
                ScriptEvent result = null;
                if (err != null) {
                    try {
                        err.message = this.message;
                        err.queue = this.queue;
                        err.script = this.script;
                        err.line = this.line;
                        result = err.fire();
                    } catch (Throwable ignored) {
                    }
                }
                return result;
            }
        };
    }

}
