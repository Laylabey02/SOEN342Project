import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class Route {
    private String routeID;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private String arrivalCity;
    private String departureCity;
    private String trainType;
    private float firstClassTicket;
    private float secondClassTicket;
    private ArrayList<String> daysOfOperation;


    private boolean overnightArrival = false;

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
        if (overnightArrival || mins < 0) mins += 24 * 60;
        return LocalTime.ofSecondOfDay(mins * 60);
    }


    public String getRouteID() { return routeID; }
    public LocalTime getDepartureTime() { return departureTime; }
    public LocalTime getArrivalTime() { return arrivalTime; }
    public String getArrivalCity() { return arrivalCity; }
    public String getDepartureCity() { return departureCity; }
    public String getTrainType() { return trainType; }
    public float getFirstClassTicket() { return firstClassTicket; }
    public float getSecondClassTicket() { return secondClassTicket; }
    public ArrayList<String> getDaysOfOperation() { return daysOfOperation; }


    public void setOvernight(boolean v) { this.overnightArrival = v; }


    public boolean InOperation(String day) {
        return daysOfOperation != null && daysOfOperation.contains(day);
    }

    @Override
    public String toString() {
        return String.format("%s %s %s→%s %s-%s (1st:%.2f 2nd:%.2f)",
                trainType, routeID, departureCity, arrivalCity,
                departureTime, arrivalTime, firstClassTicket, secondClassTicket);
    }
}
