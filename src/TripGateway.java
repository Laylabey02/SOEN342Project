import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class TripGateway {
    private static final String DB_URL = "jdbc:sqlite:mydatabase.db";
    private static TripGateway instance;
    private final Random random = new Random();

    private TripGateway() {}

    public static TripGateway getInstance() {
        if (instance == null) instance = new TripGateway();
        return instance;
    }

     public String generateTripId() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder tripId = new StringBuilder();
        for (int i = 0; i < 8; i++) tripId.append(chars.charAt(random.nextInt(chars.length())));
        return tripId.toString();
    }
    //good for unique number generation. 
    private static final AtomicLong counter = new AtomicLong();

    public String generateTicketNumber() {
        long timestamp = System.currentTimeMillis(); // 13 digits
        long count = counter.getAndIncrement() % 1000; // 3 digits rollover

        return timestamp + String.format("%03d", count);
    }
    public String generateReservationId() {
        return "RES" + String.format("%06d", random.nextInt(1_000_000));
    }

    // ---- Persist a full booking: Trip + Reservations + Tickets ----
    public Trip createTrip(String clientLastName, String clientId,
                           ArrayList<Connection> selectedConnections,
                           ArrayList<String> travelerNames,
                           ArrayList<Integer> ages,
                           ArrayList<String> identificationNumbers,
                           boolean isFirstClass) {

        String tripId = generateTripId();
        Trip trip = new Trip(tripId, clientLastName, clientId);

        // Serialize connection as routeIDs (e.g., "R1|R5|R9")
        String serializedConnection = serializeConnections(selectedConnections);

        // Insert Trip row first
        String tripSql = "INSERT INTO Trip (tripId, clientId, bookingDate, isCompleted, connection) VALUES (?, ?, ?, ?, ?)";
        try (java.sql.Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(tripSql)) {
            ps.setString(1, tripId);
            ps.setString(2, clientId);
            ps.setString(3, trip.getBookingDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            ps.setInt(4, trip.isCompleted() ? 1 : 0);
            ps.setString(5, serializedConnection);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Trip insert failed: " + e.getMessage());
        }

        // Create reservations & tickets in DB and in-memory Trip
        for (int i = 0; i < travelerNames.size(); i++) {
            String travelerName = travelerNames.get(i);
            int age = ages.get(i);
            String travelerIDNum = identificationNumbers.get(i);

            float price = 0;
            ArrayList<Route> routes = new ArrayList<>();
            for (Connection con : selectedConnections) {
                for (Route r : con.getConnection()) {
                    routes.add(r);
                    price += isFirstClass ? r.getFirstClassTicket() : r.getSecondClassTicket();
                }
            }

            String ticketNumber = generateTicketNumber();
            Ticket ticket = new Ticket(ticketNumber, travelerName, age, travelerIDNum, isFirstClass, price);

            String reservationId = generateReservationId();
            Reservation reservation = new Reservation(reservationId, travelerName, age, travelerIDNum, ticket, routes);

            insertReservation(reservationId, tripId);                 // Reservation row
            insertTicket(reservationId, ticket);                      // Ticket row

            trip.addReservation(reservation);
        }

        return trip;
    }

    private String serializeConnections(ArrayList<Connection> selectedConnections) {
        ArrayList<String> ids = new ArrayList<>();
        for (Connection c : selectedConnections) {
            for (Route r : c.getConnection()) ids.add(r.getRouteID());
        }
        return String.join("|", ids);
    }

    private void insertReservation(String reservationId, String tripId) {
        String sql = "INSERT INTO Reservation (reservationId, tripId) VALUES (?, ?)";
        try (java.sql.Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reservationId);
            ps.setString(2, tripId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Reservation insert failed: " + e.getMessage());
        }
    }

    private void insertTicket(String reservationId, Ticket ticket) {
        String sql = """
            INSERT INTO Ticket (ticketNumber, reservationId, travelerName, age, identificationNumber, isFirstClass, price)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        try (java.sql.Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ticket.getTicketNumber());
            ps.setString(2, reservationId);
            ps.setString(3, ticket.getTravelerName());
            ps.setInt(4, ticket.getAge());
            ps.setString(5, ticket.getIdentificationNumber());
            ps.setInt(6, ticket.isFirstClass() ? 1 : 0);
            ps.setFloat(7, ticket.getPrice());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Ticket insert failed: " + e.getMessage());
        }
    }

    // ------- Useful reads (optional for now) -------
    public ArrayList<Trip> getTripsByClient(String clientLastName, String identificationNumber) {
        ArrayList<Trip> out = new ArrayList<>();
        String sql = "SELECT tripId, clientId, bookingDate, isCompleted, connection FROM Trip WHERE clientId = ?";
        try (java.sql.Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, identificationNumber);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Trip t = new Trip(
                            rs.getString("tripId"),
                            clientLastName,                        // we don’t normalize the last name in DB; you already have it at runtime
                            rs.getString("clientId")
                    );
                    // (Optional) you could parse bookingDate/isCompleted/connection back here if needed
                    out.add(t);
                }
            }
        } catch (SQLException e) {
            System.out.println("getTripsByClient failed: " + e.getMessage());
        }
        return out;
    }
}
