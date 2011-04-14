package net.omnivr.ocd;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;

/**
 *
 * @author Nayruden
 */
public class OCDProtectionInfo {

    static final Map<Location, String> owners = new HashMap<Location, String>();

    static void setKnownOwner(Location at, String who) {
        owners.put(at, who);
    }

    static boolean isOwner(Location at, String who) {
        String owner = owners.get(at);
        if (owner == null || !owner.equalsIgnoreCase(who)) {
            return false;
        }
        return true;
    }

    private OCDProtectionInfo() {
    } // No instances of this class
}
