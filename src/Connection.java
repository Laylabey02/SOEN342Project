import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class Connection {

    private final ArrayList<Route> routesCon;

    public Connection(ArrayList<Route> routes) {
        this.routesCon = new ArrayList<>(routes);
    }

    public ArrayList<Route> getRoutesCon() { return routesCon; }

    // duration of ride + wait
    public long totalDurationMinutes(int transferBufferMinutes){
        if (routesCon.isEmpty()) return 0;
        long total = 0;
        for (int i = 0; i< routesCon.size(); i++) {
            Route r = routesCon.get(i);
            total += r.durationMinutes();
            if (i < routesCon.size() - 1) {
                Route next = routesCon.get(i+1);
                long waitTime = ChronoUnit.MINUTES.between(r.getArrivalTime(), next.getDepartureTime());
                if (waitTime < 0) waitTime += 24*60;
                total += transferBufferMinutes + Math.max(0, waitTime);
            }
        }
        return total;
    }
    // total cost either first or second. no mixing
    public float totalCost(boolean firstClass){
        float sum = 0f;
        for (Route r : routesCon) sum += firstClass ? r.getFirstClassTicket() : r.getSecondClassTicket();
        return sum;
    }

    //display info to user regarding this connection
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i< routesCon.size(); i++) {
            Route r = routesCon.get(i);
            sb.append(String.format("%s %s %s to %s %s-%s",
                    r.getTrainType(), r.getRouteID(), r.getDepartureCity(), r.getArrivalCity(),
                    r.getDepartureTime(), r.getArrivalTime()));
            if (i < routesCon.size()-1) sb.append(" | ");
        }
        return sb.toString();
    }
}
