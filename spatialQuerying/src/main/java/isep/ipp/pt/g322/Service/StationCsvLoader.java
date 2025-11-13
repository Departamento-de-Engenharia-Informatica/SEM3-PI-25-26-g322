package isep.ipp.pt.g322.service;

import isep.ipp.pt.g322.model.Station;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StationCsvLoader {


    public List<Station> load(String filename) throws IOException {
        List<Station> stations = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String header = br.readLine(); 
            String line;
            int lineNo = 1;

            while ((line = br.readLine()) != null) {
                lineNo++;


                String[] f = line.split(";", -1);

                if (f.length < 5) {
                    System.out.println("Ignoring line " + lineNo + ": not enough columns");
                    continue;
                }

                String name = f[0].trim();
                String country = f[1].trim();
                String timeZone = f[2].trim();
                String timeZoneGroup = f[3].trim();
                double lat;
                double lon;
                try {
                    lat = Double.parseDouble(f[4].trim());
                    lon = Double.parseDouble(f[5].trim());
                } catch (Exception e) {
                    System.out.println("Ignoring line " + lineNo + ": bad lat/lon");
                    continue;
                }


                boolean isCity = f.length > 6 && f[6].trim().equalsIgnoreCase("true");
                boolean isMain = f.length > 7 && f[7].trim().equalsIgnoreCase("true");
                boolean isAirport = f.length > 8 && f[8].trim().equalsIgnoreCase("true");

                Station s = new Station(
                        name,
                        lat,
                        lon,
                        country,
                        timeZone,
                        timeZoneGroup,
                        isCity,
                        isMain,
                        isAirport
                );

                if (!s.isValid()) {
                    System.out.println("Ignoring line " + lineNo + ": not valid according to model");
                    continue;
                }

                stations.add(s);
            }
        }

        return stations;
    }
}
