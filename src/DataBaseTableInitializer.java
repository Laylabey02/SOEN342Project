import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DataBaseTableInitializer {
    private static final String DB_URL = "jdbc:sqlite:mydatabase.db";

    public static void initializeDatabase() {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                enableForeignKeys(conn);
                createTables(conn);
                System.out.println("Database initialized successfully.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private static void enableForeignKeys(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
    }

    private static void createTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Client (
                    identificationNumber TEXT PRIMARY KEY,
                    firstName            TEXT NOT NULL,
                    lastName             TEXT NOT NULL
                );
            """);


            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Route (
                    routeID           TEXT PRIMARY KEY,
                    departureTime     TEXT,
                    arrivalTime       TEXT,
                    departureCity     TEXT,
                    arrivalCity       TEXT,
                    trainType         TEXT,
                    firstClassTicket  REAL,
                    secondClassTicket REAL,
                    daysOfOperation   TEXT
                );
            """);


            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Trip (
                    tripId      TEXT PRIMARY KEY,
                    clientId    TEXT NOT NULL,
                    bookingDate TEXT,
                    isCompleted INTEGER,
                    connection  TEXT,
                    FOREIGN KEY (clientId) REFERENCES Client(identificationNumber)
                );
            """);


            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Reservation (
                    reservationId TEXT PRIMARY KEY,
                    tripId        TEXT NOT NULL,
                    FOREIGN KEY (tripId) REFERENCES Trip(tripId)
                );
            """);


            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Ticket (
                    ticketNumber         TEXT PRIMARY KEY,
                    reservationId        TEXT NOT NULL,
                    travelerName         TEXT,
                    age                  INTEGER,
                    identificationNumber TEXT,
                    isFirstClass         INTEGER,
                    price                REAL,
                    FOREIGN KEY (reservationId) REFERENCES Reservation(reservationId)
                );
            """);
        }
    }
}
