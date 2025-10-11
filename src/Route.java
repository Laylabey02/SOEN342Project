import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Locale;

public class Route {

    private final String routeID;
    private final LocalTime departureTime;
    private final LocalTime arrivalTime;
    private final String departureCity;
    private final String arrivalCity;
    private final String trainType;
    private final float firstClassTicket;
    private final float secondClassTicket;
    // Days like: "Mon", "Tue", "Wednesday", etc.
    private final ArrayList<String> daysOfOperation;

    public Route(String routeID,
                 LocalTime departureTime,
                 LocalTime arrivalTime,
                 String departureCity,
                 String arrivalCity,
                 String trainType,
                 float firstClassTicket,
                 float secondClassTicket,
                 ArrayList<String> daysOfOperation) {
        this.routeID = routeID;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.departureCity = departureCity;
        this.arrivalCity = arrivalCity;
        this.trainType = trainType;
        this.firstClassTicket = firstClassTicket;
        this.secondClassTicket = secondClassTicket;
        this.daysOfOperation = daysOfOperation;
    }

    /** Duration in minutes, accounting for overnight arrival. */
    public long durationMinutes() {
        long mins = ChronoUnit.MINUTES.between(departureTime, arrivalTime);
        if (mins < 0) mins += 24 * 60; // next-day arrival
        return mins;
    }

    /** Case-insensitive prefix match: "Mon" matches "Monday", etc. */
    public boolean inOperation(String day) {
        if (day == null) return true;
        String norm = day.toLowerCase(Locale.ROOT).trim();
        for (String d : daysOfOperation) {
            if (d != null && d.toLowerCase(Locale.ROOT).trim().startsWith(norm)) return true;
        }
        return false;
    }

    public String getRouteID() { return routeID; }
    public LocalTime getDepartureTime() { return departureTime; }
    public LocalTime getArrivalTime() { return arrivalTime; }
    public String getDepartureCity() { return departureCity; }
    public String getArrivalCity() { return arrivalCity; }
    public String getTrainType() { return trainType; }
    public float getFirstClassTicket() { return firstClassTicket; }
    public float getSecondClassTicket() { return secondClassTicket; }
    public ArrayList<String> getDaysOfOperation() { return daysOfOperation; }

    @Override
    public String toString() {
        return String.format(
                "Route %s: %s (%s) -> %s (%s) type=%s first=%.2f€ second=%.2f€ days=%s",
                routeID, departureCity, departureTime, arrivalCity, arrivalTime, trainType,
                firstClassTicket, secondClassTicket, daysOfOperation);
    }
}
