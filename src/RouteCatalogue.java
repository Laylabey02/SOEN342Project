import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalTime;
import java.util.ArrayList;

public final class RouteCatalogue {
    private static RouteCatalogue instance;
    private final ArrayList<Route> routes;

    private RouteCatalogue() {
        this.routes = new ArrayList<>();
    }
    public static RouteCatalogue getInstance() {
        if(instance == null) instance = new RouteCatalogue();
        return instance;
    }

    public Route create(String routeID, String departureCity, String arrivalCity,
                        LocalTime departureTime, LocalTime arrivalTime,
                        String trainType, float firstClass, float secondClass,
                        ArrayList<String> days) {
        Route r = new Route(routeID, departureTime, arrivalTime, departureCity, arrivalCity,
                trainType, firstClass, secondClass, days);
        routes.add(r);
        return r;
    }

    public ArrayList<Route> getRoutes() { return routes; }

    /** Load routes from CSV per the assignment spec. */
    public void loadFromCsv(String path) throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                // RouteID,DepCity,ArrCity,DepTime,ArrTime,TrainType,Days,First,Second
                String[] parts = line.split("\\s*,\\s*");
                if (parts.length < 9) continue; // skip malformed
                String id = parts[0];
                String depCity = parts[1];
                String arrCity = parts[2];
                LocalTime depT = LocalTime.parse(parts[3]);
                LocalTime arrT = LocalTime.parse(parts[4]);
                String type = parts[5];
                String daysRaw = parts[6];
                float first = Float.parseFloat(parts[7]);
                float second = Float.parseFloat(parts[8]);

                ArrayList<String> days = new ArrayList<>();
                for (String d : daysRaw.split("/|;|\\s+")) {
                    if (!d.isBlank()) days.add(d.trim());
                }
                create(id, depCity, arrCity, depT, arrT, type, first, second, days);
            }
        }
    }
}
