import java.time.LocalDateTime;
import java.util.ArrayList;

public class Trip {
    private String tripId;
    private String clientLastName;
    private String clientId;
    private LocalDateTime bookingDate;
    private ArrayList<Reservation> reservations;
    private boolean isCompleted;
    
    public Trip(String tripId, String clientLastName, String clientId) {
        this.tripId = tripId;
        this.clientLastName = clientLastName;
        this.clientId = clientId;
        this.bookingDate = LocalDateTime.now();
        this.reservations = new ArrayList<>();
        this.isCompleted = false;
    }
    
    public void addReservation(Reservation reservation) {
        reservations.add(reservation);
    }
    
    public void completeTrip() {
        this.isCompleted = true;
    }
    

    public String getTripId() { return tripId; }
    public String getClientLastName() { return clientLastName; }
    public String getClientId() { return clientId; }
    public LocalDateTime getBookingDate() { return bookingDate; }
    public ArrayList<Reservation> getReservations() { return reservations; }
    public boolean isCompleted() { return isCompleted; }
    
    public int getNumberOfTravelers() {
        return reservations.size();
    }
    
    public float getTotalCost() {
        float total = 0;
        for (Reservation reservation : reservations) {
            total += reservation.getTicket().getPrice();
        }
        return total;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Trip %s - Client: %s (ID: %s) - Booked: %s - %d travelers - Total: $%.2f\n",
                              tripId, clientLastName, clientId, bookingDate.toString(), 
                              getNumberOfTravelers(), getTotalCost()));
        sb.append("Reservations:\n");
        for (int i = 0; i < reservations.size(); i++) {
            sb.append(String.format("  %d. %s\n", i + 1, reservations.get(i).toString()));
        }
        return sb.toString();
    }
}
