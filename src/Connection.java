import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class Connection {

    private final ArrayList<Route> connection;

    public Connection(ArrayList<Route> routes) {
        this.connection = new ArrayList<>(routes);
    }

    public ArrayList<Route> getConnection() { return connection; }


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
    
    //Handle cases where total duration exceeds 24 hours
    int hours = (int)(minutes / 60);
    int mins = (int)(minutes % 60);
    

    if (hours >= 24) {
        return LocalTime.of(hours % 24, mins);
    }
    
    return LocalTime.of(hours, mins);
}


public float totalCost() {
    float sum = 0f;
    for (Route r : connection) sum += r.getSecondClassTicket();
    return sum;
}

//To be fixed
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
