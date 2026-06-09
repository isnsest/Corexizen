package org.isnsest.corexizen.commands.denizen;


import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.BracedCommand;
import com.denizenscript.denizencore.scripts.commands.generator.*;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import dev.corexinc.corex.engine.compiler.Instruction;
import dev.corexinc.corex.engine.compiler.ScriptCompiler;
import dev.corexinc.corex.engine.queue.ScriptQueue;
import dev.corexinc.corex.environment.tags.player.PlayerTag;

import java.util.ArrayList;
import java.util.List;

public class CorexCommand extends BracedCommand {

    // <--[command]
    // @Name Corex
    // @Syntax corex [<commands>]
    // @Required 0
    // @Maximum 0
    // @Short Executes Corex script commands directly inside Denizen.
    // @Group Corexizen
    // @Plugin Corex
    //
    // @Description
    // The 'corex' command lets you write and run Corex script commands directly inside your Denizen scripts.
    //
    // If the Denizen script has a player linked to it (like from an event), that player will automatically
    // be the target for the Corex commands.
    //
    // @Usage
    // Use to send a message using Corex's narrate command directly from Denizen.
    // - corex:
    //     - narrate "Hello from Corex!"
    //
    // @Usage
    // Use to run a Corex repeat loop.
    // - corex:
    //     - repeat 3 as:x:
    //         - narrate "This is loop number <[x]>"
    // -->

    public CorexCommand() {
        setName("corex");
        setSyntax("corex [<commands>]");
        setRequiredArguments(0, 0);
        autoCompile();
    }

    public static void autoExecute(ScriptEntry scriptEntry) {
        List<ScriptEntry> bracedCommandsList = getBracedCommandsDirect(scriptEntry, scriptEntry);
        if (bracedCommandsList == null || bracedCommandsList.isEmpty()) {
            Debug.echoError(scriptEntry, "Empty subsection - did you forget a ':'?");
            return;
        }

        Instruction[] instructionsArray = compileEntries(bracedCommandsList, scriptEntry);

        var player = Utilities.getEntryPlayer(scriptEntry);

        ScriptQueue scriptQueue = new ScriptQueue(
                "Corex_" + System.currentTimeMillis(),
                instructionsArray,
                false,
                player == null ? null : new PlayerTag(player.getPlayerEntity())
        );
        scriptQueue.setSilent(true);
        scriptQueue.start();
    }

    private static Instruction[] compileEntries(List<ScriptEntry> entries, ScriptEntry owner) {
        if (entries == null) {
            return new Instruction[0];
        }

        List<Instruction> instructions = new ArrayList<>();

        for (ScriptEntry entry : entries) {
            List<BracedData> bracedDataList = entry.getBracedSet();

            if (bracedDataList != null && !bracedDataList.isEmpty()) {
                List<ScriptEntry> subEntries = getBracedCommandsDirect(entry, owner);

                Instruction[] innerBlock = compileEntries(subEntries, owner);

                String rawLine = cleanCommandLine(entry.internal.originalLine);

                Instruction instruction = ScriptCompiler.compile(rawLine, innerBlock);
                if (instruction != null) {
                    instructions.add(instruction);
                }
            } else {
                String rawLine = cleanCommandLine(entry.internal.originalLine);
                Instruction instruction = ScriptCompiler.compile(rawLine);
                if (instruction != null) {
                    instructions.add(instruction);
                }
            }
        }

        return instructions.toArray(new Instruction[0]);
    }

    private static String cleanCommandLine(String line) {
        if (line == null) {
            return "";
        }
        String trimmed = line.trim();
        if (trimmed.endsWith(":")) {
            return trimmed.substring(0, trimmed.length() - 1).trim();
        }
        return trimmed;
    }
}