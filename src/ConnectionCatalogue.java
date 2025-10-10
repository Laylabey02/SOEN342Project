import java.util.ArrayList;

public class ConnectionCatalogue {
    private static ConnectionCatalogue instance;
    private ArrayList<Connection> routes;

    private ConnectionCatalogue() {
    }
    public static ConnectionCatalogue getInstance() {
        if(instance == null) {
            instance = new ConnectionCatalogue();
        }
        return instance;
    }
    public Connection create(ArrayList<Route> routes){
        Connection connection = new Connection(routes);
        return null;
    }
}
