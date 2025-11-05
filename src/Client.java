import java.util.ArrayList;

public class Client {
    private String lastName;
    private String firstName;
    private String identificationNumber;
    private ArrayList<Trip> tripHistory;
    
    public Client(String lastName, String firstName, String identificationNumber) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.identificationNumber = identificationNumber;
        this.tripHistory = new ArrayList<>();
    }
    
    public void addTrip(Trip trip) {
        tripHistory.add(trip);
    }
    

    public String getLastName() { return lastName; }
    public String getFirstName() { return firstName; }
    public String getIdentificationNumber() { return identificationNumber; }
    public ArrayList<Trip> getTripHistory() { return tripHistory; }
    
    @Override
    public String toString() {
        return String.format("Client: %s %s (ID: %s)", 
                           firstName, lastName, identificationNumber);
    }
}
