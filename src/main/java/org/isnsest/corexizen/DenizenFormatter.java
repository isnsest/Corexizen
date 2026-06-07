package org.isnsest.corexizen;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.tags.TagManager;
import dev.corexinc.corex.api.tags.AbstractFormatter;
import dev.corexinc.corex.api.tags.AbstractTag;
import dev.corexinc.corex.api.tags.Attribute;
import dev.corexinc.corex.engine.utils.PlayerIdentity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

/* @doc formatter
 *
 * @Name denizen
 * @ArgRequired
 * @Description
 * This tag lets you use any Denizen tag directly inside your Corex scripts.
 * It’s like a bridge that brings all of Denizen's information into Corex.
 *
 * It automatically knows which player is running the script. This means you can
 * use tags like <player.name> or <player.health> without doing any extra work.
 *
 * @Usage
 * // Show the server version using a Denizen tag.
 * - narrate "Server version: <denizen[<server.version>]>"
 *
 * // Show the name of the player who is running the Corex script.
 * - narrate "Hello, <denizen[<player.name>]>!"
 *
 * // Get the player's money balance from Denizen.
 * - narrate "Balance: $<denizen[<player.money>]>"
 */

public class DenizenFormatter implements AbstractFormatter {

    @Override
    public @NotNull String getName() {
        return "denizen";
    }

    @Override
    public @Nullable AbstractTag parse(@NotNull Attribute attribute) {
        if (!attribute.hasParam()) return null;

        var queue = attribute.getQueue();
        PlayerIdentity identity = queue != null ? queue.getPlayer() : null;

        BukkitTagContext context = new BukkitTagContext(identity == null ? null : new PlayerTag((Player) identity), null, null);
        return Utils.parseCorexTagObject(TagManager.tag(attribute.getRawParam(), context));
    }
}
