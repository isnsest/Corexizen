package org.isnsest.corexizen.commands.corex;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectFetcher;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.scripts.queues.core.InstantQueue;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import dev.corexinc.corex.api.commands.AbstractCommand;
import dev.corexinc.corex.engine.compiler.CompiledArgument;
import dev.corexinc.corex.engine.compiler.Instruction;
import dev.corexinc.corex.engine.queue.ScriptQueue;
import dev.corexinc.corex.engine.utils.debugging.Debugger;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;

/* @doc command
 *
 * @Name Denizen
 * @Syntax denizen [<script>] (path:<name>) (def:<element>|.../def.<name>:<value>) (id:<name>)
 * @RequiredArgs 1
 * @MaxArgs -1
 * @Aliases d
 * @ShortDescription Runs a Denizen task script.
 *
 * @Description
 * Runs a script in the Denizen engine rather than Corex.
 *
 * You must specify the name of a valid Denizen script object to run.
 *
 * Optionally, use the "path:" argument to choose a specific sub-path within the Denizen script.
 *
 * Optionally, use the "def:" argument to specify definition values to pass to the Denizen script.
 * The definitions will be mapped according to the "definitions:" key on the script being run.
 *
 * Alternately, use "def.<name>:<value>" to define one or more named definitions individually
 * to be passed into the Denizen queue.
 *
 * Optionally, specify the "id:" argument to choose a custom queue ID for the Denizen execution.
 * If none is specified, a randomly generated one will be used by the Denizen engine.
 *
 * @Usage
 * // Use to run a Denizen task script named 'My_Denizen_Task'.
 * - denizen My_Denizen_Task
 *
 * @Usage
 * // Use to run a specific sub-path named 'altPath' within a Denizen script.
 * - denizen My_Denizen_Task path:altPath
 *
 * @Usage
 * // Use to run 'My_Denizen_Task' and pass 3 definitions to the Denizen queue.
 * - denizen My_Denizen_Task def:A|Second_Def|Taco
 *
 * @Usage
 * // Use to run 'My_Denizen_Task' and pass named definitions directly to Denizen.
 * - denizen My_Denizen_Task def.count:5 def.type:Taco def.smell:Tasty
 */
public class DenizenCommand implements AbstractCommand {

    @Override
    public @NonNull String getName() {
        return "denizen";
    }

    @Override
    public @NonNull List<String> getAlias() {
        return List.of("d");
    }

    @Override
    public @NonNull String getSyntax() {
        return "[<script>] (path:<name>) (def:<element>|.../def.<name>:<value>) (id:<name>)";
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return -1;
    }

    @Override
    public boolean isAsyncSafe() {
        return true;
    }

    @Override
    public void run(@NonNull ScriptQueue queue, @NonNull Instruction instruction) {
        String target = instruction.getLinear(0, queue);
        if (target == null) return;

        String scriptName = target;
        String path = "script";

        int dotIndex = target.indexOf('.');
        if (dotIndex > 0) {
            scriptName = target.substring(0, dotIndex);
            path = target.substring(dotIndex + 1);
        }

        String prefixPath = instruction.getPrefix("path", queue);
        if (prefixPath != null) path = prefixPath;

        ScriptContainer container = ScriptRegistry.getScriptContainer(scriptName);
        if (container == null) {
            Debugger.echoError(queue, "Container '" + scriptName + "' not found!");
            return;
        }

        PlayerTag playerTag = queue.getPlayer() == null ? null : new PlayerTag(((dev.corexinc.corex.environment.tags.player.PlayerTag) queue.getPlayer()).getPlayer());
        TagContext context = new BukkitTagContext(playerTag, null, null);

        InstantQueue instantQueue = new InstantQueue("Corex");
        instantQueue.addEntries(
                container.getEntries(
                        new BukkitScriptEntryData(playerTag, null),
                        path
                )
        );

        for (Map.Entry<String, CompiledArgument> entry : instruction.prefixArgs.entrySet()) {
            if (entry.getKey().startsWith("def.")) {
                String defName = entry.getKey().substring(4);
                ObjectTag defValue = ObjectFetcher.pickObjectFor(entry.getValue().getRaw(), context);
                instantQueue.addDefinition(defName, defValue);
            }
        }

        String defRaw = instruction.getPrefix("def", queue);
        Debug.log(defRaw);
        if (defRaw != null) {
            ObjectTag defTag = ObjectFetcher.pickObjectFor(defRaw, context);
            Debug.log(defTag.identify());

            if (defTag instanceof MapTag map) {
                instantQueue.definitions = map;
            } else {

                ListTag list = defTag.asType(ListTag.class, context);
                List<String> definition_names = null;
                if (container.contains("definitions", String.class)) {
                    String str = container.getString("definitions");
                    definition_names = CoreUtilities.split(str, '|');
                }

                int x = 1;
                for (ObjectTag definition : list.objectForms) {
                    String name = definition_names != null && definition_names.size() >= x ? definition_names.getFirst().trim() : String.valueOf(x);
                    int squareBracket = name.indexOf('[');
                    if (squareBracket != -1) {
                        name = name.substring(0, squareBracket).trim();
                    }
                    instantQueue.addDefinition(name, definition);
                    x++;
                }
            }
        }

        instantQueue.start(true);
    }
}