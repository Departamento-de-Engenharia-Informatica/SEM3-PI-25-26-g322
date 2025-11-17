package isep.ipp.pt.g322.Service;

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
            if (header == null) {
                return stations;
            }


            String delimiter = header.contains(";") ? ";" : ",";

            String[] cols = header.split(delimiter, -1);

            int idxCountry   = indexOf(cols, "country");
            int idxStation   = indexOf(cols, "station");
            int idxLat       = indexOf(cols, "latitude", "lat");
            int idxLon       = indexOf(cols, "longitude", "lon");
            int idxTimeZone  = indexOf(cols, "time_zone", "tz");
            int idxTzGroup   = indexOf(cols, "time_zone_group", "tzGroup");
            int idxIsCity    = indexOf(cols, "is_city", "isCity");
            int idxIsMain    = indexOf(cols, "is_main_station", "isMain", "is_main");
            int idxIsAirport = indexOf(cols, "is_airport", "airport", "isAirport");



            if (idxCountry < 0 || idxStation < 0 || idxLat < 0 || idxLon < 0) {
                throw new IOException("Missing mandatory columns in header");
            }

            String line;
            int lineNo = 1;

            while ((line = br.readLine()) != null) {
                lineNo++;

                if (line.isBlank()) continue;


                line = line.replace("\"('", "")
                        .replace("',)\"", "");

                String[] f = line.split(delimiter, -1);

                String name      = getField(f, idxStation);
                String country   = getField(f, idxCountry);
                String latStr    = getField(f, idxLat);
                String lonStr    = getField(f, idxLon);
                String timeZone  = idxTimeZone >= 0 ? getField(f, idxTimeZone) : "";
                String tzGroup   = idxTzGroup >= 0 ? getField(f, idxTzGroup) : "";


                if (name.isBlank() || country.isBlank()
                        || latStr.isBlank() || lonStr.isBlank()) {
                    System.out.println("Ignoring line " + lineNo + ": mandatory field empty");
                    continue;
                }

                double lat, lon;
                try {
                    lat = Double.parseDouble(latStr);
                    lon = Double.parseDouble(lonStr);
                } catch (NumberFormatException e) {
                    System.out.println("Ignoring line " + lineNo + ": bad lat/lon");
                    continue;
                }

                boolean isCity    = idxIsCity    >= 0 && parseBool(getField(f, idxIsCity));
                boolean isMain    = idxIsMain    >= 0 && parseBool(getField(f, idxIsMain));
                boolean isAirport = idxIsAirport >= 0 && parseBool(getField(f, idxIsAirport));

                Station s = new Station(
                        name,
                        lat,
                        lon,
                        country,
                        timeZone,
                        tzGroup,
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



    private static int indexOf(String[] cols, String... names) {
        for (int i = 0; i < cols.length; i++) {
            String col = cols[i].trim().toLowerCase();
            for (String n : names) {
                if (col.equals(n.toLowerCase())) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static String getField(String[] f, int idx) {
        if (idx < 0 || idx >= f.length) return "";
        return f[idx].trim();
    }

    private static boolean parseBool(String s) {
        String v = s.trim().toLowerCase();
        return v.equals("true") || v.equals("t") || v.equals("1");
    }
}
