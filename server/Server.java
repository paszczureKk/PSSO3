package server;

import common.IAuctionServer;
import common.IAuctionListener;
import common.Item;
import common.WrappedObserver;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("removal")
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
            }
        }
    }
    @Override
    public Item[] getItems()
            throws RemoteException {
        int size = this.availableItems.values().size();
        return this.availableItems.values().toArray(new Item[size]);
    }

    @Override
    public void registerListener(IAuctionListener al, String itemName)
            throws RemoteException {
        Item item = availableItems.get(itemName);
        if(item == null) {
            throw new RemoteException("No such item");
        }
        else {
            item.addObserver(new WrappedObserver(al));
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
            try {
                this.placeItemForBid(ownerName.toString(), itemName.toString(), itemDesc, 0.00, time);
            } catch (RemoteException e) {
                System.err.println("Error during placing the item to bidding:");
                e.printStackTrace();
            }
        }
    }

    private final HashMap<String, Item> availableItems = new HashMap<>();

    public static String registryEntryName = "Server";

    public Server() {
        try {
            Registry registry = LocateRegistry.getRegistry();
            registry.lookup(Server.registryEntryName);
            this.generateItems();

            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            executor.scheduleAtFixedRate(new ItemManager(this), 0, 1, TimeUnit.SECONDS);

            IAuctionServer stub = (IAuctionServer) UnicastRemoteObject.exportObject(this, 0);
            registry.rebind(Server.registryEntryName, stub);
            System.out.println("Server bound");
        } catch (Exception e) {
            System.err.println("Server exception:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (System.getSecurityManager() == null)
            System.setSecurityManager(new SecurityManager());
        AuctionServerFactory auctionServerFactory = new AuctionServerFactory();
        if(auctionServerFactory.create(AuctionServerFactory.AuctionServerTypes.Server.name()) == null) {
            System.err.println("Server creation failed");
            System.exit(1);
        }
    }
}
