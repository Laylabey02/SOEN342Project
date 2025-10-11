import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class Connection {

    private final ArrayList<Route> connection;

    public Connection(ArrayList<Route> routes) {
        this.connection = new ArrayList<>(routes);
    }

    public ArrayList<Route> getLegs() { return connection; }


public LocalTime totalDuration() {
    if (connection.isEmpty()) return LocalTime.of(0,0);

    long minutes = 0;
    for (int i = 0; i < connection.size(); i++) {
        Route r = connection.get(i);

        long ride = ChronoUnit.MINUTES.between(r.getDepartureTime(), r.getArrivalTime());
        if (ride < 0) ride += 24 * 60;
        minutes += ride;


        if (i < connection.size() - 1) {
            Route next = connection.get(i + 1);
            long wait = ChronoUnit.MINUTES.between(r.getArrivalTime(), next.getDepartureTime());
            if (wait < 0) wait += 24 * 60;
            minutes += wait;
        }
    }
    return LocalTime.of((int)(minutes / 60), (int)(minutes % 60));
}


public float totalCost() {
    float sum = 0f;
    for (Route r : connection) sum += r.getSecondClassTicket();
    return sum;
}

// Optional helper if you want first-class totals in Main (doesn’t violate diagram)
public float totalCost(boolean firstClass) {
    if (!firstClass) return totalCost();
    float sum = 0f;
    for (Route r : connection) sum += r.getFirstClassTicket();
    return sum;
}

@Override
public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i=0;i<connection.size();i++) {
        Route r = connection.get(i);
        sb.append(String.format("%s %s %s-%s %s→%s",
                r.getTrainType(), r.getRouteID(),
                r.getDepartureCity(), r.getArrivalCity(),
                r.getDepartureTime(), r.getArrivalTime()));
        if (i < connection.size()-1) sb.append(" | ");
    }
    return sb.toString();
}
}
