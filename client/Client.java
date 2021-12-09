package client;

import common.IAuctionServer;
import common.IAuctionListener;
import common.Item;

import lombok.Getter;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;

public class Client implements IAuctionListener{
    @Override
    public void update(Item item) throws RemoteException {
        if(item.getTimeRemaining() == 0) {
            if(item.getCurrentBidder() == this.bidderName) {
                this.ownedItems.add(item);
            }
            this.removeWatch(item.getItemName());
        }
        else {
            if(this.biddingStrategy == Strategies.Maximum) {
                double bid = item.getCurrentBid() + 1.00;
                if(bid > this.maximumBid) {
                    System.out.println(String.format("Bid exceeds maximum amount, item: %s",
                            item.getItemName()));
                }
                else {
                    this.bidOnItem(item.getItemName(), bid);
                }
            }
        }
    }

    private void printHelp() {
        System.out.println("Program options, please insert specific key:");
        System.out.println("[1] Display bidding strategies");
        System.out.println("[2] Set bidding strategy");
        System.out.println("[3] Display available items");
        System.out.println("[4] Bid for item");
        System.out.println("[0] Exit the application");
    }

    private void requestItems() {
        try {
            this.availableItems = server.getItems();
        } catch (RemoteException e) {
            System.out.println("Error requesting items:");
            e.printStackTrace();
        }
        for (int i = 0; i < this.availableItems.length; i++) {
            System.out.println(String.format("%d. %s", i, this.availableItems[i].getItemName()));
        }
    }

    public void bidOnItem(String itemName, double bid) {
        try {
            server.bidOnItem(this.bidderName, itemName, bid);
        } catch (RemoteException e) {
            System.out.println(String.format("Bidding does not succeeded: %s", itemName));
            e.printStackTrace();
        }
    }

    private void setBiddingStrategy(String biddingStrategy) {
        this.biddingStrategy = Client.Strategies.valueOf(biddingStrategy);

        if(this.biddingStrategy == Client.Strategies.LastMinute) {
            this.clientWatcher.setStopped(false);
        }
        else {
            this.clientWatcher.setStopped(true);
        }
    }

    private void getBiddingStrategies() {
        System.out.println("Available strategies:");
        for (Strategies s : asList(Client.Strategies.values())) {
            System.out.println(s.name());
        }
    }

    public void removeWatch(String itemName) {
        this.watchedItems.removeIf(i -> (i.getItemName() == itemName));
    }

    private void addWatch(String itemName) {
        Item item = null;
        for(Item i : availableItems) {
            if(i.getItemName() == itemName) {
                item = i;
                break;
            }
        }
        if(item != null) {
            this.watchedItems.add(item);
        }
        else {
            System.out.println("No such item");
        }
    }

    private IAuctionServer server;
    private Item[] availableItems;
    private ArrayList<Item> ownedItems = new ArrayList<Item>();
    @Getter private ArrayList<Item> watchedItems = new ArrayList<Item>();

    private String bidderName;

    private static enum Strategies {
        Maximum,
        LastMinute
    }

    private Strategies biddingStrategy = Client.Strategies.Maximum;
    private double maximumBid = 0.00;
    private ScheduledExecutorService executor;
    private ClientWatcher clientWatcher;

    public Client() {
        try {
            String name = "Server";
            Registry registry = LocateRegistry.getRegistry();
            this.server = (IAuctionServer)registry.lookup(name);
        } catch (Exception e) {
            System.err.println("Connection error:");
            e.printStackTrace();
        }
        executor = Executors.newScheduledThreadPool(1);
        this.clientWatcher = new ClientWatcher(this, 60);
        executor.scheduleAtFixedRate(this.clientWatcher, 0, 1, TimeUnit.SECONDS);
    }

    public static void main(String[] args) {
        if (System.getSecurityManager() == null) System.setSecurityManager(new SecurityManager());
        Client client = new Client();

        Scanner input = new Scanner(System.in);

        System.out.println("Hello, what's your name?");
        client.bidderName = input.nextLine();

        System.out.println("Hi, " + client.bidderName + "!");
        String line = "";
        while(line != "0") {
            switch(line) {
                case "1":
                    client.getBiddingStrategies();
                    break;
                case "2":
                    line = input.nextLine();
                    client.setBiddingStrategy(line);
                    break;
                case "3":
                    client.requestItems();
                    break;
                case "4":
                    line = input.nextLine();
                    client.addWatch(line);
                    break;
                default:
                    client.printHelp();
            }
            line = input.nextLine();
        }
    }
}
