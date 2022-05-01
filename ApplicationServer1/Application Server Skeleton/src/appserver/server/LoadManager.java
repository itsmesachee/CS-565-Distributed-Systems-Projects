package appserver.server;

import java.util.ArrayList;

/**
 *
 * @author Dr.-Ing. Wolf-Dieter Otte
 */
public class LoadManager {

    static ArrayList satellites = null;
    static int lastSatelliteIndex = -1;

    public LoadManager() {
        satellites = new ArrayList<String>();
    }

    public void satelliteAdded(String satelliteName) {
        // add satellite
        // ...
         if (!satellites.contains(satelliteName)) {
            satellites.add(satelliteName);
            if (lastSatelliteIndex == -1) {
                lastSatelliteIndex = 0;
            }
            System.out.println("[LoadManager.satelliteAdded] " + satelliteName + " satellite registered.");
        } else {
            System.out.println("[LoadManager.satelliteAdded] " + satelliteName + "satellite is already registered.");
        }
    }


    public String nextSatellite() throws Exception {
        
        int numberSatellites;
        numberSatellites = satellites.size();
        String nextSat = null;
        
        synchronized (satellites) {
            // implement policy that returns the satellite name according to a round robin methodology
            // ...
            if (numberSatellites > 0) {
//                lastSatelliteIndex ++;
//                lastSatelliteIndex %= numberSatellites;
                lastSatelliteIndex = (lastSatelliteIndex + 1) % numberSatellites;
                nextSat = (String) satellites.get(lastSatelliteIndex);
            } else {
                System.err.println("There are no satellites registered.");
                throw new Exception();
            }
        }

        //return // ... name of satellite who is supposed to take job;
        return nextSat;
    }
}
