import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;

public class ConnectionCatalogue {

    private static ConnectionCatalogue instance;
    private final ArrayList<Connection> connections;
    private static final long MAX_LAYOVER_MINUTES = 180; // 3 hours

    private ConnectionCatalogue() {
        this.connections = new ArrayList<>();
    }

    public static ConnectionCatalogue getInstance() {
        if (instance == null) instance = new ConnectionCatalogue();
        return instance;
    }

    public Connection create(ArrayList<Route> routes) {
        Connection c = new Connection(routes);
        connections.add(c);
        return c;
    }

    private boolean isValidLayover(ArrayList<Route> routes) {
        if (routes.size() <= 1) return true; // Direct connections have no layovers
        
        for (int i = 0; i < routes.size() - 1; i++) {
            Route current = routes.get(i);
            Route next = routes.get(i + 1);
            
            long layoverMinutes = ChronoUnit.MINUTES.between(current.getArrivalTime(), next.getDepartureTime());
            if (layoverMinutes < 0) layoverMinutes += 24 * 60; // Handle overnight cases
            
            if (layoverMinutes > MAX_LAYOVER_MINUTES) {
                return false;
            }
        }
        return true;
    }

    public ArrayList<Connection> getConnections() { return connections; }

    public void clear() { connections.clear(); }

    private boolean matchesFilters(Connection c, String arrivalTimeStr, String departureTimeStr, 
                                   String ticketCostStr, String trainType, boolean firstClass) {
        ArrayList<Route> routes = c.getConnection();
        if (routes.isEmpty()) return false;
        
        // Check departure time (first route's departure time)
        if (departureTimeStr != null && !departureTimeStr.isEmpty()) {
            try {
                LocalTime filterDepartureTime = LocalTime.parse(departureTimeStr);
                LocalTime connectionDepartureTime = routes.get(0).getDepartureTime();
                if (!connectionDepartureTime.equals(filterDepartureTime)) {
                    return false;
                }
            } catch (DateTimeParseException e) {
            }
        }
        
        // Check arrival time (last route's arrival time)
        if (arrivalTimeStr != null && !arrivalTimeStr.isEmpty()) {
            try {
                LocalTime filterArrivalTime = LocalTime.parse(arrivalTimeStr);
                LocalTime connectionArrivalTime = routes.get(routes.size() - 1).getArrivalTime();
                if (!connectionArrivalTime.equals(filterArrivalTime)) {
                    return false;
                }
            } catch (DateTimeParseException e) {
            }
        }

        if (trainType != null && !trainType.isEmpty()) {
            for (Route r : routes) {
                if (!r.getTrainType().equalsIgnoreCase(trainType)) {
                    return false;
                }
            }
        }
        
        // Check ticket cost (total cost must be less than or equal to specified)
        if (ticketCostStr != null && !ticketCostStr.isEmpty()) {
            try {
                float maxCost = Float.parseFloat(ticketCostStr);
                float connectionCost = c.totalCost(firstClass);
                if (connectionCost > maxCost) {
                    return false;
                }
            } catch (NumberFormatException e) {
            }
        }
        
        return true;
    }

    //build connection of 1,2,3 size routes
    public ArrayList<Connection> searchForConnections(RouteGateway rg, String from, String to, String day,
                                         String arrivalTime, String departureTime, 
                                         String ticketCost, String trainType, boolean firstClass) {
        clear();
        ArrayList<Route> rs = rg.getRoutes();
        ArrayList<Connection> allConnections = new ArrayList<>();

        //direct
        for (Route r : rs) {
            if (r.getDepartureCity().equalsIgnoreCase(from)
                    && r.getArrivalCity().equalsIgnoreCase(to)
                    && r.InOperation(day)) {
                ArrayList<Route> l = new ArrayList<>();
                l.add(r);
                allConnections.add(create(l));
            }
        }

        //1 stop
        for (Route r1 : rs) {
            if (!(r1.getDepartureCity().equalsIgnoreCase(from) && r1.InOperation(day))) continue;
            for (Route r2 : rs) {
                if (!(r2.getArrivalCity().equalsIgnoreCase(to) && r2.InOperation(day))) continue;
                if (!r1.getArrivalCity().equalsIgnoreCase(r2.getDepartureCity())) continue;

                ArrayList<Route> l = new ArrayList<>();
                l.add(r1); l.add(r2);
                if (isValidLayover(l)) {
                    allConnections.add(create(l));
                }
            }
        }

        //2 stops
        for (Route r1 : rs) {
            if (!(r1.getDepartureCity().equalsIgnoreCase(from) && r1.InOperation(day))) continue;
            for (Route r2 : rs) {
                if (!r2.InOperation(day)) continue;
                if (!r1.getArrivalCity().equalsIgnoreCase(r2.getDepartureCity())) continue;

                for (Route r3 : rs) {
                    if (!(r3.getArrivalCity().equalsIgnoreCase(to) && r3.InOperation(day))) continue;
                    if (!r2.getArrivalCity().equalsIgnoreCase(r3.getDepartureCity())) continue;

                    ArrayList<Route> l = new ArrayList<>();
                    l.add(r1); l.add(r2); l.add(r3);
                    if (isValidLayover(l)) {
                        allConnections.add(create(l));
                    }
                }
            }
        }
        connections.clear();
        for (Connection c : allConnections) {
            if (matchesFilters(c, arrivalTime, departureTime, ticketCost, trainType, firstClass)) {
                connections.add(c);
            }
        }

        return connections;
    }

    public ArrayList<Connection> sort(ArrayList<Connection> list, String key, boolean firstClass) {
        ArrayList<Connection> out = new ArrayList<>(list);
        if ("duration".equalsIgnoreCase(key)) {
            out.sort(Comparator.comparing(Connection::totalDuration));
        } else {
            out.sort(Comparator.comparingDouble(c -> c.totalCost(firstClass)));
        }
        return out;
    }
}