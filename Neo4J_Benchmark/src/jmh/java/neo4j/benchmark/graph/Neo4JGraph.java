package neo4j.benchmark.graph;

import neo4j.benchmark.types.RelTypes;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.*;

import java.nio.file.Path;

import static org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME;

//@State(Scope.Benchmark)
public class Neo4JGraph {

    private static GraphDatabaseService graphDb;
    private static Node firstNode;
    private static Node secondNode;
    private static Relationship relationship;
    private static DatabaseManagementService managementService;



    //@Benchmark
   //@BenchmarkMode(Mode.AverageTime)
    public static void main() {

        Neo4JGraph Neo4JGraph = new Neo4JGraph();

        Neo4JGraph.connectionGraph();

        try (Transaction tx = graphDb.beginTx()) {

            Neo4JGraph.createNodes(tx);

            Neo4JGraph.displayNodes();

            Neo4JGraph.removeNodes(tx);

            tx.commit();
        }

        Neo4JGraph.shutdownGraph();
    }

    public void connectionGraph() {
        Path databaseDirectory = Path.of("/tmp/neo4j/");
        managementService = new DatabaseManagementServiceBuilder( databaseDirectory ).build();
        graphDb = managementService.database( DEFAULT_DATABASE_NAME );
        registerShutdownHook( managementService );
    }

    private void createNodes(Transaction tx) {
        firstNode = tx.createNode();
        firstNode.setProperty("message", "Hello, ");
        secondNode = tx.createNode();
        secondNode.setProperty("message", "World!");

        relationship = firstNode.createRelationshipTo(secondNode, RelTypes.KNOWS);
        relationship.setProperty("message", "brave Neo4j ");
    }

    private void displayNodes() {
        System.out.print(firstNode.getProperty("message"));
        System.out.print(relationship.getProperty("message"));
        System.out.print(secondNode.getProperty("message"));
    }

    private void removeNodes(Transaction tx) {
        firstNode = tx.getNodeById(firstNode.getId());
        secondNode = tx.getNodeById(secondNode.getId());
        firstNode.getSingleRelationship(RelTypes.KNOWS, Direction.OUTGOING).delete();
        firstNode.delete();
        secondNode.delete();
    }

    private void shutdownGraph() {
        managementService.shutdown();
    }

    private void registerShutdownHook( final DatabaseManagementService managementService )
    {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                managementService.shutdown();
            }
        } );
    }
}
