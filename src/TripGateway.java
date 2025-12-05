import java.sql.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
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


    public ArrayList<Trip> getTripsByClient(String clientLastName, String identificationNumber) {
        ArrayList<Trip> out = new ArrayList<>();
        String sql = "SELECT tripId, clientId, bookingDate, isCompleted, connection FROM Trip WHERE clientId = ?";
        try (java.sql.Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, identificationNumber);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String tripId = rs.getString("tripId");
                    Trip t = new Trip(tripId, clientLastName, rs.getString("clientId"));

                    String connectionStr = rs.getString("connection");
                    ArrayList<Route> routes = loadRoutesFromConnection(connectionStr);

                    loadReservationsForTrip(t, routes);
                    
                    out.add(t);
                }
            }
        } catch (SQLException e) {
            System.out.println("getTripsByClient failed: " + e.getMessage());
        }
        return out;
    }
    
    private void loadReservationsForTrip(Trip trip, ArrayList<Route> routes) {
        String sql = "SELECT reservationId FROM Reservation WHERE tripId = ?";
        try (java.sql.Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trip.getTripId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String reservationId = rs.getString("reservationId");

                    Ticket ticket = loadTicket(reservationId);
                    if (ticket == null) continue;
                    
                    // Create reservation with ticket and shared routes
                    Reservation reservation = new Reservation(
                            reservationId,
                            ticket.getTravelerName(),
                            ticket.getAge(),
                            ticket.getIdentificationNumber(),
                            ticket,
                            routes  
                    );
                    
                    trip.addReservation(reservation);
                }
            }
        } catch (SQLException e) {
            System.out.println("loadReservationsForTrip failed: " + e.getMessage());
        }
    }
    
    private Ticket loadTicket(String reservationId) {
        String sql = "SELECT ticketNumber, travelerName, age, identificationNumber, isFirstClass, price FROM Ticket WHERE reservationId = ?";
        try (java.sql.Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Ticket(
                            rs.getString("ticketNumber"),
                            rs.getString("travelerName"),
                            rs.getInt("age"),
                            rs.getString("identificationNumber"),
                            rs.getInt("isFirstClass") == 1,
                            rs.getFloat("price")
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("loadTicket failed: " + e.getMessage());
        }
        return null;
    }
    

    private ArrayList<Route> loadRoutesFromConnection(String connectionStr) {
        ArrayList<Route> routes = new ArrayList<>();
        if (connectionStr == null || connectionStr.isEmpty()) {
            return routes;
        }
        
        String[] routeIDs = connectionStr.split("\\|");
        for (String routeID : routeIDs) {
            Route route = loadRouteById(routeID.trim());
            if (route != null) {
                routes.add(route);
            }
        }
        return routes;
    }

    private Route loadRouteById(String routeID) {
        String sql = "SELECT routeID, departureTime, arrivalTime, departureCity, arrivalCity, trainType, firstClassTicket, secondClassTicket, daysOfOperation FROM Route WHERE routeID = ?";
        try (java.sql.Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, routeID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LocalTime departureTime = LocalTime.parse(rs.getString("departureTime"));
                    LocalTime arrivalTime = LocalTime.parse(rs.getString("arrivalTime"));
                    String departureCity = rs.getString("departureCity");
                    String arrivalCity = rs.getString("arrivalCity");
                    String trainType = rs.getString("trainType");
                    float firstClassTicket = (float) rs.getDouble("firstClassTicket");
                    float secondClassTicket = (float) rs.getDouble("secondClassTicket");

                    String daysStr = rs.getString("daysOfOperation");
                    ArrayList<String> daysOfOperation = new ArrayList<>();
                    if (daysStr != null && !daysStr.isEmpty()) {
                        String[] days = daysStr.split(",");
                        for (String day : days) {
                            daysOfOperation.add(day.trim());
                        }
                    }
                    
                    return new Route(
                            routeID,
                            departureTime,
                            arrivalTime,
                            departureCity,
                            arrivalCity,
                            trainType,
                            firstClassTicket,
                            secondClassTicket,
                            daysOfOperation
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("loadRouteById failed for " + routeID + ": " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error parsing route data for " + routeID + ": " + e.getMessage());
        }
        return null;
    }
}
