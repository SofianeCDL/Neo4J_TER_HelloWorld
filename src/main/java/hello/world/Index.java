package hello.world;

import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;

import java.nio.file.Path;
import java.util.ArrayList;

import static org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME;
import static org.neo4j.internal.helpers.collection.Iterators.loop;

public class Index {

    private static GraphDatabaseService graphDb;
    private static DatabaseManagementService managementService;

    public static void main(String[] args) {

        Index index = new Index();

        index.connectionGraph();

        index.createIndexGraph();

        index.createUsers();

        Label label = Label.label( "User" );
        int idToFind = 45;
        String nameToFind = "user" + idToFind + "@neo4j.org";

        index.findUser(label, idToFind, nameToFind);

        index.updateUser(label, idToFind, nameToFind);

        idToFind = 46;
        nameToFind = "user" + idToFind + "@neo4j.org";

        index.findUser(label, idToFind, nameToFind);

        index.removeUser(label, nameToFind);

        index.removeIndex();

        index.shutdownGraph();

    }

    public void connectionGraph() {
        Path databaseDirectory = Path.of("/Users/Artorias/Documents/JetBrains/LIB/neo4j-community-4.4.3-windows");
        managementService = new DatabaseManagementServiceBuilder( databaseDirectory ).build();
        graphDb = managementService.database( DEFAULT_DATABASE_NAME );
    }

    public void createIndexGraph() {
        IndexDefinition usernamesIndex;

        try ( Transaction tx = graphDb.beginTx() ) {
            Schema schema = tx.schema();

            usernamesIndex = schema.indexFor(Label.label("User"))
                    .on("username")
                    .withName("usernames")
                    .create();

            tx.commit();
        }
    }

    public void createUsers() {
        Label label = Label.label( "User" );

        try ( Transaction tx = graphDb.beginTx() ) {
            // Create some users
            for (int id = 0; id < 100; id++) {
                Node userNode = tx.createNode(label);
                userNode.setProperty("username", "user" + id + "@neo4j.org");
            }
            System.out.println("Users created");
            tx.commit();
        }
    }

    public void findUser(Label label, int idToFind, String nameToFind) {
        try ( Transaction tx = graphDb.beginTx() ) {
            try (ResourceIterator<Node> users = tx.findNodes(label, "username", nameToFind)) {
                ArrayList<Node> userNodes = new ArrayList<>();
                while (users.hasNext()) {
                    userNodes.add(users.next());
                }

                for (Node node : userNodes) {
                    System.out.println(
                            "The username of user " + idToFind + " is " + node.getProperty("username"));
                }
            }
        }
    }

    public void updateUser(Label label, int idToFind, String nameToFind) {
        try ( Transaction tx = graphDb.beginTx() ) {
            for (Node node : loop(tx.findNodes(label, "username", nameToFind))) {
                node.setProperty("username", "user" + (idToFind + 1) + "@neo4j.org");
            }
            tx.commit();
        }
    }

    public void removeUser(Label label, String nameToFind) {
        try (Transaction tx = graphDb.beginTx()) {
            for (Node node : loop(tx.findNodes(label, "username", nameToFind))) {
                node.delete();
            }
            tx.commit();
        }
    }

    public void removeIndex() {
        try ( Transaction tx = graphDb.beginTx() )
        {
            IndexDefinition usernamesIndex = tx.schema().getIndexByName( "usernames" );
            usernamesIndex.drop();
            tx.commit();
        }
    }

    private void shutdownGraph() {
        managementService.shutdown();
    }
}
