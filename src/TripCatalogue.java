import java.util.ArrayList;
import java.util.Random;

public class TripCatalogue {
    private static TripCatalogue instance;
    private ArrayList<Trip> trips;
    private Random random;
    
    private TripCatalogue() {
        this.trips = new ArrayList<>();
        this.random = new Random();
    }
    
    public static TripCatalogue getInstance() {
        if (instance == null) {
            instance = new TripCatalogue();
        }
        return instance;
    }
    
    public String generateTripId() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder tripId = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            tripId.append(chars.charAt(random.nextInt(chars.length())));
        }
        return tripId.toString();
    }
    
    public String generateTicketNumber() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder ticketNumber = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            ticketNumber.append(chars.charAt(random.nextInt(chars.length())));
        }
        return ticketNumber.toString();
    }
    
    public String generateReservationId() {
        return "RES" + String.format("%06d", random.nextInt(1000000));
    }
    
    public Trip create() {
        String tripId = generateTripId();
        Trip trip = new Trip(tripId, "", "");
        trips.add(trip);
        return trip;
    }
    
    public Trip createTrip(String clientLastName, String clientId, ArrayList<Connection> selectedConnections, 
                          ArrayList<String> travelerNames, ArrayList<Integer> ages, 
                          ArrayList<String> identificationNumbers, boolean isFirstClass) {
        
        String tripId = generateTripId();
        Trip trip = new Trip(tripId, clientLastName, clientId);
        
        // Create reservations for each traveler
        for (int i = 0; i < travelerNames.size(); i++) {
            String travelerName = travelerNames.get(i);
            int age = ages.get(i);
            String identificationNumber = identificationNumbers.get(i);
            
            // Calculate price based on connection and class
            float price = 0;
            ArrayList<Route> routes = new ArrayList<>();
            
            for (Connection connection : selectedConnections) {
                for (Route route : connection.getConnection()) {
                    routes.add(route);
                    if (isFirstClass) {
                        price += route.getFirstClassTicket();
                    } else {
                        price += route.getSecondClassTicket();
                    }
                }
            }
            
            // Create ticket
            String ticketNumber = generateTicketNumber();
            Ticket ticket = new Ticket(ticketNumber, travelerName, age, identificationNumber, isFirstClass, price);
            
            // Create reservation
            String reservationId = generateReservationId();
            Reservation reservation = new Reservation(reservationId, travelerName, age, identificationNumber, ticket, routes);
            
            trip.addReservation(reservation);
        }
        
        trips.add(trip);
        return trip;
    }
    
    public ArrayList<Trip> searchTrip(String name, int age, String clientID) {
        ArrayList<Trip> results = new ArrayList<>();
        for (Trip trip : trips) {
            if (trip.getClientId().equals(clientID)) {
                for (Reservation reservation : trip.getReservations()) {
                    if (reservation.getTravelerName().equalsIgnoreCase(name) && 
                        reservation.getAge() == age) {
                        results.add(trip);
                        break;
                    }
                }
            }
        }
        return results;
    }
    
    public ArrayList<Trip> getTripsByClient(String lastName, String identificationNumber) {
        ArrayList<Trip> clientTrips = new ArrayList<>();
        for (Trip trip : trips) {
            if (trip.getClientLastName().equalsIgnoreCase(lastName) && 
                trip.getClientId().equals(identificationNumber)) {
                clientTrips.add(trip);
            }
        }
        return clientTrips;
    }
    
    public ArrayList<Trip> getAllTrips() {
        return new ArrayList<>(trips);
    }
    
    public void addTrip(Trip trip) {
        trips.add(trip);
    }
    
    public void completeTrip(String tripId) {
        for (Trip trip : trips) {
            if (trip.getTripId().equals(tripId)) {
                trip.completeTrip();
                break;
            }
        }
    }
}
