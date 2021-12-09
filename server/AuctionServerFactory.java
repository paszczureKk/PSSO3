package server;

import common.AbstractFactory;

import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class AuctionServerFactory implements AbstractFactory<IAuctionServer>{
    public enum AuctionServerTypes {
        Server
    }

    @Override
    public IAuctionServer create(String serverType) {
        String name = Server.registryEntryName;

        Registry registry;
        boolean initialized = true;

        try {
            registry = LocateRegistry.getRegistry();
            registry.lookup(name);
        }
        catch (NotBoundException e) {
            initialized = false;
        }
        catch (Exception e) {
            System.err.println("Server exception:");
            e.printStackTrace();
        }

        if(initialized) {
            System.err.println("Server already exists!");
            return null;
        }

        if (serverType.equals(AuctionServerTypes.Server.name())) {
            return new Server();
        }
        else {
            return null;
        }
    }
}
