package hello.world;

import hello.world.types.RelTypes;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.*;

import java.nio.file.Path;
import java.util.Random;

import static org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME;

public class NodeTypes<T> {

    private static GraphDatabaseService graphDb;
    private static Node firstNode;
    private static Node secondNode;
    private static Relationship relationship;
    private static DatabaseManagementService managementService;


    public static void main(String[] args) {


        // Test with Interger type.
        NodeTypes<Integer> nodeTypesInteger = new NodeTypes<>();

        nodeTypesInteger.connectionGraph();

        try (Transaction tx = graphDb.beginTx()) {

            nodeTypesInteger.createNodes(tx, 1, 2);

            nodeTypesInteger.displayNodes();

            nodeTypesInteger.removeNodes(tx);

            tx.commit();
        }

        nodeTypesInteger.shutdownGraph();

        // Test with Boolean type.
        NodeTypes<Boolean> nodeTypeBoolean = new NodeTypes<>();

        nodeTypeBoolean.connectionGraph();

        try (Transaction tx = graphDb.beginTx()) {

            nodeTypeBoolean.createNodes(tx, true, false);

            nodeTypeBoolean.displayNodes();

            nodeTypeBoolean.removeNodes(tx);

            tx.commit();
        }

        nodeTypeBoolean.shutdownGraph();

        // Test with Boolean type.
        NodeTypes<Pokemon> nodeTypePokemon = new NodeTypes<>();

        nodeTypePokemon.connectionGraph();

        try (Transaction tx = graphDb.beginTx()) {

            nodeTypePokemon.createNodes(tx, new Pokemon("Pikachu"), new Pokemon("Dracaufeu"));

            nodeTypePokemon.displayNodes();

            nodeTypePokemon.removeNodes(tx);

            tx.commit();
        }

        nodeTypePokemon.shutdownGraph();
    }

    private void connectionGraph() {
        Path databaseDirectory = Path.of("/Users/Artorias/Documents/JetBrains/LIB/neo4j-community-4.4.3-windows");
        managementService = new DatabaseManagementServiceBuilder( databaseDirectory ).build();
        graphDb = managementService.database( DEFAULT_DATABASE_NAME );
        registerShutdownHook( managementService );
    }

    private void createNodes(Transaction tx, T property1, T property2) {
        firstNode = tx.createNode();
        firstNode.setProperty("message", property1);
        secondNode = tx.createNode();
        secondNode.setProperty("message", property2);

        relationship = firstNode.createRelationshipTo(secondNode, RelTypes.KNOWS);
        relationship.setProperty("message", " brave Neo4j ");
    }

    private void displayNodes() {
        System.out.print(firstNode.getProperty("message"));
        System.out.print(relationship.getProperty("message"));
        System.out.print(secondNode.getProperty("message"));
        System.out.println();
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

class Pokemon {

    private final int idPokemon;
    private final String pokemonName;
    private final int PV;
    private final int XP;

    public Pokemon(String pokemonName) {
        Random r = new Random();
        this.idPokemon = r.nextInt();
        this.pokemonName = pokemonName;
        this.PV = 20;
        this.XP = 0;
    }

    public int getIdPokemon() {
        return idPokemon;
    }

    public String getPokemonName() {
        return pokemonName;
    }

    public int getPV() {
        return PV;
    }

    public int getXP() {
        return XP;
    }

    @Override
    public String toString() {
        return "Pokemon{" +
                "pokemonName='" + pokemonName + '\'' +
                '}';
    }
}
