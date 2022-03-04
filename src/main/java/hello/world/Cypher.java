package hello.world;

import hello.world.types.RelTypes;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;
import org.openjdk.jmh.annotations.Benchmark;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;

import static org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME;
import static org.neo4j.internal.helpers.collection.Iterators.loop;

public class Cypher {

    private static Node firstNode;
    private static Node secondNode;
    private static Relationship relationship;
    private static GraphDatabaseService graphDb;
    private static DatabaseManagementService managementService;

    private Label label;
    private  int idToFind;
    private String nameToFind;
    private String nodeName;

    public static void main(String[] args) {
        Cypher cypher = new Cypher();

        cypher.start();


    }
    public void start() {

        Cypher cypher = new Cypher();

        cypher.connectionGraph();

        cypher.createIndexGraph();

        cypher.createUsers();
        cypher.createNodeCypher();

        label = Label.label( "User" );
        idToFind = 0;
        nameToFind = "user" + idToFind + "@neo4j.org";

        nodeName = "Hello";

        cypher.findUserCypher();
        cypher.findUserNodeCypher();

        cypher.removeUser(label, nameToFind);

        idToFind = 1;
        nameToFind = "user" + idToFind + "@neo4j.org";

        nodeName = "World!";

        cypher.findUserCypher();
        cypher.findUserNodeCypher();

        cypher.removeUser(label, nameToFind);

        cypher.removeIndex();
        cypher.removeNodes("Hello");
        cypher.removeNodes("World!");

        cypher.shutdownGraph();
    }

    public void connectionGraph() {
        Path databaseDirectory = Path.of("/Users/Artorias/Documents/JetBrains/LIB/neo4j-community-4.4.3-windows");
        managementService = new DatabaseManagementServiceBuilder( databaseDirectory ).build();
        graphDb = managementService.database( DEFAULT_DATABASE_NAME );
    }

    public void createIndexGraph() {
        IndexDefinition usernamesCypher;

        try ( Transaction tx = graphDb.beginTx() ) {
            Schema schema = tx.schema();

            usernamesCypher = schema.indexFor(Label.label("User"))
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
            for (int id = 0; id < 2; id++) {
                Node userNode = tx.createNode(label);
                userNode.setProperty("username", "user" + id + "@neo4j.org");
            }
            System.out.println("Users created");
            tx.commit();
        }
    }

    private void createNodeCypher() {
        try (Transaction tx = graphDb.beginTx() ) {
            firstNode = tx.createNode();
            firstNode.setProperty("message", "Hello");
            secondNode = tx.createNode();
            secondNode.setProperty("message", "World!");

            relationship = firstNode.createRelationshipTo(secondNode, RelTypes.KNOWS);
            relationship.setProperty("message", "brave Neo4j ");

            tx.commit();
        }
    }

    @Benchmark
    public void findUserNodeCypher() {
        try ( Transaction tx = graphDb.beginTx();
              Result result = tx.execute( "MATCH (n {message: '" + nodeName + "'}) RETURN n, n.message" ) )
        {
            String rows = "";
            while ( result.hasNext() )
            {
                Map<String,Object> row = result.next();

                for ( Map.Entry<String,Object> column : row.entrySet() )
                {
                    rows += column.getKey() + ": " + column.getValue() + "; ";
                }
                rows += "\n";
            }

            tx.commit();
            System.out.println(rows);
        }
    }

    @Benchmark
    public void findUserCypher() {
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
            System.out.println("User update");
        }
    }

    public void removeUser(Label label, String nameToFind) {
        try (Transaction tx = graphDb.beginTx()) {
            for (Node node : loop(tx.findNodes(label, "username", nameToFind))) {
                node.delete();
            }
            tx.commit();
            System.out.println("User remove");
        }
    }

    private void removeNodes(String nodeMessage) {
        try (Transaction tx = graphDb.beginTx(); Result result = tx.execute( "MATCH (n {message: '" + nodeMessage + "!'}) DETACH DELETE n" )) {

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
