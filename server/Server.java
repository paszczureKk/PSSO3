package server;

import common.IAuctionServer;
import common.IAuctionListener;
import common.Item;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Server implements IAuctionServer {
    @Override
    public void placeItemForBid(String ownerName, String itemName, String itemDesc, double startBid, int auctionTime)
            throws RemoteException {
        if(availableItems.get(itemName) == null) {
            availableItems.put(itemName,
                    new Item(ownerName, itemName, itemDesc, startBid, auctionTime));
        }
        else {
            throw new RemoteException("Item already exists");
        }
    }

    @Override
    public void bidOnItem(String bidderName, String itemName, double bid)
            throws RemoteException {
        Item item = availableItems.get(itemName);
        if(item == null) {
            throw new RemoteException("No such item");
        }
        else {
            if(bid < item.getCurrentBid()) {
                throw new RemoteException("Bid too small");
            }
            else {
                item.setCurrentBidder(bidderName);
                item.setCurrentBid(bid);

                this.update(item);
            }
        }
    }
    @Override
    public Item[] getItems()
            throws RemoteException {
        return (Item[])this.availableItems.values().toArray();
    }

    @Override
    public void registerListener(IAuctionListener al, String itemName)
            throws RemoteException {
        Item item = availableItems.get(itemName);
        if(item == null) {
            throw new RemoteException("No such item");
        }
        else {
            item.addListener(al);
        }
    }

    public void endAuction(String itemName)
            throws RemoteException {
        Item item = availableItems.get(itemName);
        if(item == null) {
            throw new RemoteException("No such item");
        }
        else {
            availableItems.remove(itemName);
            this.update(item);
        }
    }

    private void update(Item item) throws RemoteException {
        for(IAuctionListener al : item.getListeners()) {
            al.update(item);
        }
    }

    private void generateItems() {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random random = new Random();

        for(int i = 1; i <= 10; i++) {
            StringBuilder ownerName = new StringBuilder();
            StringBuilder itemName = new StringBuilder();
            for(int j = 0; j < 10; j++) {
                ownerName.append(alphabet.charAt(random.nextInt(alphabet.length())));
                itemName.append(alphabet.charAt(random.nextInt(alphabet.length())));
            }
            String itemDesc = String.format("item%d", i);
            int time = i * 10;
            Item item = new Item(ownerName.toString(), itemName.toString(), itemDesc, 0.00, time);
        }
    }

    private HashMap<String, Item> availableItems;
    private ScheduledExecutorService executor;

    public Server() {
        String name = "Server";
        Registry registry = null;
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
            System.exit(1);
        }

        try {
            availableItems = new HashMap<String, Item>();
            this.generateItems();

            executor = Executors.newScheduledThreadPool(1);
            executor.scheduleAtFixedRate(new ItemManager(this), 0, 1, TimeUnit.SECONDS);

            IAuctionServer stub = (IAuctionServer) UnicastRemoteObject.exportObject(this, 0);
            registry.rebind(name, stub);
            System.out.println("Server bound");
        } catch (Exception e) {
            System.err.println("Server exception:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (System.getSecurityManager() == null) System.setSecurityManager(new SecurityManager());
        new Server();
    }
}
