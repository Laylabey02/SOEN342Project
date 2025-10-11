import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.time.LocalTime;
import java.util.ArrayList;


public final class RouteCatalogue {
    private static RouteCatalogue instance;
    private final ArrayList<Route> routes;

    private RouteCatalogue() {
        this.routes = new ArrayList<>();
    }

    public static RouteCatalogue getInstance() {
        if (instance == null) instance = new RouteCatalogue();
        return instance;
    }

    public ArrayList<Route> getRoutes() { return routes; }

    public Route create(String routeID,
                        LocalTime departureTime,
                        LocalTime arrivalTime,
                        String departureCity,
                        String arrivalCity,
                        String trainType,
                        float firstClassTicket,
                        float secondClassTicket,
                        ArrayList<String> daysOfOperation) {
        Route r = new Route(routeID, departureTime, arrivalTime, departureCity, arrivalCity,
                trainType, firstClassTicket, secondClassTicket, daysOfOperation);
        routes.add(r);
        return r;
    }
    public void loadFromCsv(String path) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(","); // simple, fixed format
            String id = parts[0];
            String depCity = parts[1];
            String arrCity = parts[2];
            LocalTime dep = LocalTime.parse(parts[3]);
            LocalTime arr = LocalTime.parse(parts[4]);
            String train = parts[5];
            ArrayList<String> days = new ArrayList<>(Arrays.asList(parts[6].split(" ")));
            float first = Float.parseFloat(parts[7]);
            float second = Float.parseFloat(parts[8]);
            create(id, dep, arr, depCity, arrCity, train, first, second, days);
        }
        br.close();
    }


}
