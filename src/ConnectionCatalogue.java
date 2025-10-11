import java.util.ArrayList;
import java.util.Comparator;

public class ConnectionCatalogue {

    private static ConnectionCatalogue instance;
    private final ArrayList<Connection> connections;

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

    public ArrayList<Connection> getConnections() { return connections; }

    public void clear() { connections.clear(); }

    // build connection of 1,2,3 size routes
    public ArrayList<Connection> compute(RouteCatalogue rc, String from, String to, String day) {
        clear();
        ArrayList<Route> rs = rc.getRoutes();

        // direct
        for (Route r : rs) {
            if (r.getDepartureCity().equalsIgnoreCase(from)
                    && r.getArrivalCity().equalsIgnoreCase(to)
                    && r.InOperation(day)) {
                ArrayList<Route> l = new ArrayList<>();
                l.add(r);
                create(l);
            }
        }

        // 1 stop: A->X, X->B
        for (Route r1 : rs) {
            if (!(r1.getDepartureCity().equalsIgnoreCase(from) && r1.InOperation(day))) continue;
            for (Route r2 : rs) {
                if (!(r2.getArrivalCity().equalsIgnoreCase(to) && r2.InOperation(day))) continue;
                if (!r1.getArrivalCity().equalsIgnoreCase(r2.getDepartureCity())) continue;

                ArrayList<Route> l = new ArrayList<>();
                l.add(r1); l.add(r2);
                create(l);
            }
        }

        // 2 stops: A->X, X->Y, Y->B
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
                    create(l);
                }
            }
        }

        return connections;
    }

    public ArrayList<Connection> sort(ArrayList<Connection> list, String key, boolean firstClass) {
        ArrayList<Connection> out = new ArrayList<>(list);
        if ("duration".equalsIgnoreCase(key)) {
            out.sort(Comparator.comparing(Connection::totalDuration)); // compares by LocalTime
        } else {
            out.sort(Comparator.comparingDouble(c -> c.totalCost(firstClass)));
        }
        return out;
    }
}