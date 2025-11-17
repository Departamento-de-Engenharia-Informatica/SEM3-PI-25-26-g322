import isep.ipp.pt.g322.Service.StationCsvLoader;
import isep.ipp.pt.g322.model.Station;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StationCsvLoaderTest {

    @TempDir
    Path tmp;

    private Path write(String name, String content) throws Exception {
        Path p = tmp.resolve(name);
        Files.writeString(p, content);
        return p;
    }

    @Test
    void loadsValidCsv_withSemicolons() throws Exception {
        String csv = String.join("\n",
                "station;lat;lon;country;tz;tzGroup;isCity;isMain;isAirport",
                "Porto São Bento;41.1456;-8.6109;PT;Europe/Lisbon;PT;true;true;false",
                "Lisboa Oriente;38.7678;-9.0999;PT;Europe/Lisbon;PT;true;true;false"
        );
        Path file = write("stations_sc.csv", csv);

        StationCsvLoader loader = new StationCsvLoader();
        List<Station> list = loader.load(file.toString());

        assertEquals(2, list.size());
        assertTrue(list.stream().anyMatch(s -> s.getStation().contains("Porto")));
        assertTrue(list.stream().anyMatch(s -> s.getStation().contains("Lisboa")));
    }

    @Test
    void loadsValidCsv_withCommas() throws Exception {
        String csv = String.join("\n",
                "station,lat,lon,country,tz,tzGroup,isCity,isMain,isAirport",
                "Madrid Puerta de Atocha,40.4086,-3.6922,ES,Europe/Madrid,ES,false,true,false",
                "Barcelona Sants,41.3800,2.1400,ES,Europe/Madrid,ES,true,true,false"
        );
        Path file = write("stations_comma.csv", csv);

        StationCsvLoader loader = new StationCsvLoader();
        List<Station> list = loader.load(file.toString());

        assertEquals(2, list.size());
        assertTrue(list.stream().anyMatch(s -> s.getCountry().equals("ES")));
    }

    @Test
    void ignoresInvalidLines_andTrims() throws Exception {
        String csv = String.join("\n",
                "station;lat;lon;country;tz;tzGroup;isCity;isMain;isAirport",
                " Good ;41.0;-8.0;PT;Europe/Lisbon;PT;true;false;false",
                " BadLat ;999;-8.0;PT;Europe/Lisbon;PT;true;false;false",      // inválido
                " Missing ;41.0;;PT;Europe/Lisbon;PT;true;false;false"        // inválido
        );
        Path file = write("bad_lines.csv", csv);

        StationCsvLoader loader = new StationCsvLoader();
        List<Station> list = loader.load(file.toString());

        assertEquals(1, list.size());
        assertEquals("Good", list.get(0).getStation().trim());
    }

    @Test
    void parsesBooleansCorrectly() throws Exception {
        String csv = String.join("\n",
                "station;lat;lon;country;tz;tzGroup;isCity;isMain;isAirport",
                "AirportX;40.0;-8.0;PT;Europe/Lisbon;PT;false;false;true"
        );
        Path file = write("bools.csv", csv);

        StationCsvLoader loader = new StationCsvLoader();
        List<Station> list = loader.load(file.toString());

        Station s = list.get(0);
        assertFalse(s.isCity());
        assertFalse(s.isMainStation());
        assertTrue(s.isAirport());
    }
}
