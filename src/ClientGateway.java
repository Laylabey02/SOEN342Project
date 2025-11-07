import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientGateway {
    private static final String DB_URL = "jdbc:sqlite:mydatabase.db";
    private static ClientGateway instance;

    private ClientGateway() {}

    public static ClientGateway getInstance() {
        if (instance == null) instance = new ClientGateway();
        return instance;
    }

    public void insert(Client client) {
        String sql = "INSERT OR IGNORE INTO Client (identificationNumber, firstName, lastName) VALUES (?, ?, ?)";
        try (java.sql.Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, client.getIdentificationNumber());
            ps.setString(2, client.getFirstName());
            ps.setString(3, client.getLastName());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Client insert failed: " + e.getMessage());
        }
    }

    public Client findByIdentification(String identificationNumber) {
        String sql = "SELECT identificationNumber, firstName, lastName FROM Client WHERE identificationNumber = ?";
        try (java.sql.Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, identificationNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Client(
                            rs.getString("lastName"),
                            rs.getString("firstName"),
                            rs.getString("identificationNumber")
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("Client query failed: " + e.getMessage());
        }
        return null;
    }

    public List<Client> getAll() {
        ArrayList<Client> out = new ArrayList<>();
        String sql = "SELECT identificationNumber, firstName, lastName FROM Client";
        try (java.sql.Connection conn = DriverManager.getConnection(DB_URL);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                out.add(new Client(
                        rs.getString("lastName"),
                        rs.getString("firstName"),
                        rs.getString("identificationNumber")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Client getAll failed: " + e.getMessage());
        }
        return out;
    }
}
