package org.isnsest.corexizen.commands.corex;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.scripts.ScriptBuilder;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.scripts.queues.core.InstantQueue;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.YamlConfiguration;
import dev.corexinc.corex.api.commands.AbstractCommand;
import dev.corexinc.corex.api.commands.DataBlockCommand;
import dev.corexinc.corex.engine.compiler.Instruction;
import dev.corexinc.corex.engine.queue.ScriptQueue;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.List;

 /* @doc command
 *
 * @Name Denizen
 * @Syntax denizen [<commands>]
 * @RequiredArgs 0
 * @MaxArgs -1
 * @Aliases d
 * @ShortDescription Runs Denizen commands directly inside a Corex script.
 *
 * @Description
 * The 'denizen' command (or 'd') allows you to write and run standard Denizen commands
 * directly within your Corex (.cx) scripts.
 *
 * The player currently executing the Corex script is automatically transferred to
 * the Denizen execution context.
 *
 * @Usage
 * // Use to run a block of standard Denizen commands inside a Corex script.
 * - denizen:
 *     - narrate "Hello from Denizen!"
 *     - give diamond quantity:2
 */
public class DenizenCommand implements AbstractCommand, DataBlockCommand {

    public static ScriptContainer container = new ScriptContainer(new YamlConfiguration(), "corex") {
        @Override
        public boolean shouldDebug() {
            return false;
        }
    };

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
        return "[<commands>]";
    }

    @Override
    public int getMinArgs() {
        return 0;
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
    @SuppressWarnings("unchecked")
    public void run(@NonNull ScriptQueue queue, @NonNull Instruction instruction) {
        if (instruction.customData instanceof List<?> rawList) {

            // TagContext
            Player bukkitPlayer = (Player) queue.getPlayer();
            PlayerTag player = bukkitPlayer == null ? null : new PlayerTag(bukkitPlayer);
            TagContext context = new BukkitTagContext(player, null, null);
            //

            InstantQueue instantQueue = new InstantQueue("Corex_" + System.currentTimeMillis());

            List<ScriptEntry> scriptEntries = ScriptBuilder.buildScriptEntries(
                    (List<Object>) rawList,
                    container,
                    context.getScriptEntryData()
            );

            instantQueue.addEntries(scriptEntries);
            instantQueue.start(true);
        }
    }
}