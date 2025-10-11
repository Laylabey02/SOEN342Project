import java.util.ArrayList;
import java.util.Comparator;

public class ConnectionCatalogue {


    private static ConnectionCatalogue instance;
    private final ArrayList<Connection> connections;
    //singleton design pattern private constr.
    private ConnectionCatalogue() { connections = new ArrayList<>(); }

    public static ConnectionCatalogue getInstance() {
        if(instance == null) instance = new ConnectionCatalogue();
        return instance;
    }

    public Connection create(ArrayList<Route> routes){
        Connection connection = new Connection(routes);
        connections.add(connection);
        return connection;
    }
    public ArrayList<Connection> getConnections() { return connections; }

}