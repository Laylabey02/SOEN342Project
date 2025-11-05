import java.util.ArrayList;

public class Reservation {
    private String reservationId;
    private String travelerName;
    private int age;
    private String identificationNumber;
    private Ticket ticket;
    private ArrayList<Route> routes;
    
    public Reservation(String reservationId, String travelerName, int age, String identificationNumber, 
                      Ticket ticket, ArrayList<Route> routes) {
        this.reservationId = reservationId;
        this.travelerName = travelerName;
        this.age = age;
        this.identificationNumber = identificationNumber;
        this.ticket = ticket;
        this.routes = new ArrayList<>(routes);
    }
    

    public String getReservationId() { return reservationId; }
    public String getTravelerName() { return travelerName; }
    public int getAge() { return age; }
    public String getIdentificationNumber() { return identificationNumber; }
    public Ticket getTicket() { return ticket; }
    public ArrayList<Route> getRoutes() { return routes; }
    
    @Override
    public String toString() {
        return String.format("Reservation %s - %s (Age: %d, ID: %s) - %s", 
                           reservationId, travelerName, age, identificationNumber, ticket.toString());
    }
}
