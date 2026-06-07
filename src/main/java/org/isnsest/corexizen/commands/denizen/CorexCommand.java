package org.isnsest.corexizen.commands.denizen;


import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.generator.*;
import dev.corexinc.corex.engine.scripts.ScriptManager;
import dev.corexinc.corex.environment.containers.TaskContainer;
import org.isnsest.corexizen.Utils;

import java.util.stream.Collectors;

public class CorexCommand extends AbstractCommand {

    // <--[command]
    // @Name Corex
    // @Syntax corex [<task>] (def:<element|...>) (path:<name>) (id:<name>)
    // @Required 1
    // @Maximum 4
    // @Short Runs a Corex task script.
    // @Group Corexizen
    // @Plugin Corex
    //
    // @Description
    // The 'corex' command allows you to execute Corex engine task scripts directly from within Denizen.
    // This is part of the Corexizen addon, providing seamless cross-engine script integration.
    //
    // You must specify the name of the Corex task you wish to run.
    //
    // Optionally, use the "path:" argument to choose a specific sub-path within the Corex task.
    // If no path is specified, it defaults to "script".
    //
    // Optionally, use the "def:" argument to pass a list of definitions to the resulting Corex queue.
    //
    // Optionally, use the "id:" argument to specify a custom queue ID for the Corex execution.
    // If none is specified, a unique ID will be automatically generated (formatted as Corex_<timestamp>).
    //
    // @Usage
    // Use to run a Corex task script named 'GlobalBroadcast'.
    // - corex GlobalBroadcast
    //
    // @Usage
    // Use to run a Corex task named 'HealPlayer' and pass definition values to it.
    // - corex HealPlayer def:20|true
    //
    // @Usage
    // Use to run 'MainTask' at the specific sub-path 'SubSection'.
    // - corex MainTask path:SubSection
    //
    // @Usage
    // Use to run a task with a manually specified Corex queue ID.
    // - corex MyTask id:VeryImportantQueue
    // -->

    public CorexCommand() {
        setName("corex");
        setSyntax("corex [<task>] (def:<element|...>) (path:<name>) (id:<name>)");
        setRequiredArguments(2, 4);
        autoCompile();
    }

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        tab.add(
                ScriptManager.getContainersByType(TaskContainer.class)
                        .stream()
                        .map(TaskContainer::getName)
                        .collect(Collectors.toList())
        );
    }

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgName("task") @ArgLinear ElementTag script,
                                   @ArgName("definitions") @ArgPrefixed @ArgDefaultNull ElementTag definitions,
                                   @ArgName("path") @ArgPrefixed @ArgDefaultText("script") ElementTag path,
                                   @ArgName("id") @ArgPrefixed @ArgDefaultNull ElementTag id) {
        String idStr = id != null ? id.asString() : null;
        if (id == null) {
            idStr = "Corex_" + System.currentTimeMillis();
        }

        Utils.runCorexTask(
                scriptEntry,
                script.asString(),
                path.asString(),
                definitions == null ? null : definitions.asString(),
                idStr
        );
    }
}