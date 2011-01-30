package net.omnivr.ocd;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.util.Vector;

/**
 *
 * @author Nayruden
 */
public class OCDProtectionInfo {

    static final Map<Vector, String> owners = new HashMap<Vector, String>();

    static void setKnownOwner(Vector at, String who) {
        at = new Vector(at.getBlockX(), at.getBlockY(), at.getBlockZ());
        owners.put(at, who);
    }

    static boolean isOwner(Vector at, String who) {
        at = new Vector(at.getBlockX(), at.getBlockY(), at.getBlockZ());
        String owner = owners.get(at);
        if (owner == null || !owner.equalsIgnoreCase(who)) {
            return false;
        }
        return true;
    }

    private OCDProtectionInfo() {
    } // No instances of this class
}
