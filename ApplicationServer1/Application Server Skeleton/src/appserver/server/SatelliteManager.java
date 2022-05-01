package appserver.server;

import appserver.comm.ConnectivityInfo;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 *
 * @author Dr.-Ing. Wolf-Dieter Otte
 */
public class SatelliteManager {

    // (the one) hash table that contains the connectivity information of all satellite servers
    static private Hashtable<String, ConnectivityInfo> satellites = null;

    public SatelliteManager() {
        // ..
        satellites = new Hashtable();
    }

    public void registerSatellite(ConnectivityInfo satelliteInfo) {
        // ...
         String satelliteName = satelliteInfo.getName();
        if (satellites.get(satelliteName) == null) {
            satellites.put(satelliteName, satelliteInfo);
            System.out.println("[SatelliteManager.registerSatellite] " + satelliteName + " satellite is now registered.");
        } else {
            System.out.println("[SatelliteManager.registerSatellite] " + satelliteName + " satellite already registered.");
        }
    }

    public ConnectivityInfo getSatelliteForName(String satelliteName) {
        // ..
        return satellites.get(satelliteName);
    }
}
