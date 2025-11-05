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

    //Returns a Connection object used to send SQL commands to the database.
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    //Forces SQLite to enforce relationships declared in FOREIGN KEY clauses
    private static void enableForeignKeys(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
    }

    //Create tables
    private static void createTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {

            // Client table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Client (
                    clientID      INTEGER PRIMARY KEY,
                    name          TEXT NOT NULL,
                    age           INTEGER
                );
            """);

            // Route table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Route (
                    routeID           INTEGER PRIMARY KEY,
                    departureTime     TEXT,
                    arrivalTime       TEXT,
                    arrivalCity       TEXT,
                    departureCity     TEXT,
                    trainType         TEXT,
                    firstClassTicket  REAL,
                    secondClassTicket REAL,
                    daysOfOperation   TEXT
                );
            """);

            // Trip table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Trip (
                    tripID   INTEGER PRIMARY KEY,
                    clientID INTEGER NOT NULL,
                    routeID  INTEGER NOT NULL,
                    connection TEXT,
                    FOREIGN KEY (clientID) REFERENCES Client(clientID),
                    FOREIGN KEY (routeID)  REFERENCES Route(routeID)
                );
            """);

            // Reservation table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Reservation (
                    reservationID INTEGER PRIMARY KEY,
                    tripID        INTEGER NOT NULL,
                    FOREIGN KEY (tripID) REFERENCES Trip(tripID)
                );
            """);

            // Ticket table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Ticket (
                    ticketID      INTEGER PRIMARY KEY,
                    clientID      INTEGER NOT NULL,
                    reservationID INTEGER NOT NULL,
                    FOREIGN KEY (clientID)      REFERENCES Client(clientID),
                    FOREIGN KEY (reservationID) REFERENCES Reservation(reservationID)
                );
            """);

            // Connection
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Connection (
                    connectionID INTEGER PRIMARY KEY,
                    tripID       INTEGER NOT NULL,
                    FOREIGN KEY (tripID) REFERENCES Trip(tripID)
                );
            """);
        }
    }
}
