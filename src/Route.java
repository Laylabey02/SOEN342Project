import java.time.LocalTime;

public class Route {

    private String RouteID;
    public LocalTime departureTime;
    public LocalTime arrivalTime;
    public String departureCity;
    public String arrivalCity;
    public String trainType;
    public float firstClassTicket;
    public float secondClassTicket;
    public String daysofOperation;

    public Route(String routeID, LocalTime departureTime, LocalTime arrivalTime, String departureCity, String arrivalCity, String trainType, float firstClassTicket, float secondClassTicket, String daysofOperation) {
        RouteID = routeID;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.departureCity = departureCity;
        this.arrivalCity = arrivalCity;
        this.trainType = trainType;
        this.firstClassTicket = firstClassTicket;
        this.secondClassTicket = secondClassTicket;
        this.daysofOperation = daysofOperation;
    }
    public LocalTime duration(){

    }
    public bool InOperation(String day){

    }

}
