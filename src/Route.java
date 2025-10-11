import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Locale;

public class Route {

    private String routeID;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private String departureCity;
    private String arrivalCity;
    private String trainType;
    private float firstClassTicket;
    private float secondClassTicket;
    private ArrayList<String> daysOfOperation;

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


public LocalTime duration() {
    long mins = ChronoUnit.MINUTES.between(departureTime, arrivalTime);
    if (mins < 0) mins += 24 * 60; // overnight arrival
    return LocalTime.of((int)(mins / 60), (int)(mins % 60));
}


public boolean InOperation(String day) {
    if (day == null) return true;
    String norm = day.toLowerCase(Locale.ROOT).trim();
    for (String d : daysOfOperation) {
        if (d != null && d.toLowerCase(Locale.ROOT).trim().startsWith(norm)) return true;
    }
    return false;
}

// Getters (needed by other classes)
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
    return String.format("%s %s (%s)->%s (%s)",
            trainType, routeID, departureCity, arrivalCity, arrivalTime);
}
}
