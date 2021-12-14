package client;

import common.IAuctionServer;
import common.IAuctionListener;
import common.Item;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

@SuppressWarnings("removal")
public class Client implements IAuctionListener{
    @Override
    public void update(Item item) throws RemoteException {
        if(item.getTimeRemaining() == 0) {
            if(item.getCurrentBidder().equals(this.bidderName)) {
                this.ownedItems.add(item);
            }
        }
        else {
            this.biddingStrategy.bid(this.server, this, item);
        }
    }

    private void printHelp() {
        System.out.println("Program options, please insert specific key:");
        System.out.println("[1] Display bidding strategies");
        System.out.println("[2] Set bidding strategy");
        System.out.println("[3] Display available items");
        System.out.println("[4] Bid for item");
        System.out.println("[5] Register for item alerts");
        System.out.println("[6] Display bought items");
        System.out.println("[7] Put an item for bid");
        System.out.println("[0] Exit the application");
    }

    private void requestItems() {
        try {
            Item[] availableItems = server.getItems();
            for (int i = 0; i < availableItems.length; i++) {
                System.out.printf("%d. %s%n", i, availableItems[i].getItemName());
            }
        } catch (RemoteException e) {
            System.err.println("Error requesting items:");
            e.printStackTrace();
        }
    }

    private void setBiddingStrategy(String biddingStrategy) {
        if(biddingStrategyHashMap.containsKey(biddingStrategy)) {
            this.biddingStrategy = biddingStrategyHashMap.get(biddingStrategy);

            //if (biddingStrategy.equals(Strategies.LastMinute.name())) {
            //    //todo
            //}
            if(biddingStrategy.equals(Strategies.Maximum.name())) {
                this.setMaximumBid();
            }
        }
        else {
            System.err.println("Invalid bidding strategy");
            this.getBiddingStrategies();
        }
    }

    private void getBiddingStrategies() {
        System.out.println("Available strategies:");
        for (String s : biddingStrategyHashMap.keySet()) {
            System.out.println(s);
        }
    }

    private void setMaximumBid() {
        Scanner input = new Scanner(System.in);
        System.out.println("Please insert the maximum bid");
        String line = input.nextLine();

        this.maximumBid = Double.parseDouble(line);
    }

    private void getOwnedItems() {
        System.out.println("Owned items:");
        for (Item i : this.ownedItems) {
            System.out.println(i.getItemName());
        }
    }

    private enum Strategies {
        Maximum,
        LastMinute
    }

    private Server server;
    private final ArrayList<Item> ownedItems = new ArrayList<>();

    private String bidderName;
    private final HashMap<String, BiddingStrategy> biddingStrategyHashMap = new HashMap<>() {{
        put(Strategies.Maximum.name(), BiddingStrategy.Maximum());
        put(Strategies.LastMinute.name(), BiddingStrategy.LastMinute());
    }};

    public String getBidderName() {
        return this.bidderName;
    }

    private BiddingStrategy biddingStrategy =
            this.biddingStrategyHashMap.get(Strategies.Maximum.name());
    private double maximumBid = 0.00;

    public double getMaximumBid() {
        return this.maximumBid;
    }

    public String getBiddingStrategy() {
        for(Map.Entry<String, BiddingStrategy> strategy : biddingStrategyHashMap.entrySet()) {
            if(strategy.getValue().equals(this.biddingStrategy)) {
                return strategy.getKey();
            }
        }
        return null;
    }

    public Client() {
        try {
            String name = "Server";
            Registry registry = LocateRegistry.getRegistry();
            this.server = (Server)registry.lookup(name);
        } catch (Exception e) {
            System.err.println("Connection error:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (System.getSecurityManager() == null)
            System.setSecurityManager(new SecurityManager());
        Client client = new Client();

        Scanner input = new Scanner(System.in);

        System.out.println("Hello, what's your name?");
        client.bidderName = input.nextLine();

        System.out.println("Hi, " + client.bidderName + "!");
        String line = "";
        double bid;
        while(!line.equals("0")) {
            switch(line) {
                case "1":
                    client.getBiddingStrategies();
                    break;
                case "2":
                    System.out.println("Type in chosen strategy:");
                    line = input.nextLine();
                    client.setBiddingStrategy(line);
                    break;
                case "3":
                    client.requestItems();
                    break;
                case "4":
                    System.out.println("Type in the item name you wish to bid for:");
                    line = input.nextLine();
                    System.out.println("Type in the bid amount (example: 1.99):");
                    bid = Double.parseDouble(input.nextLine());
                    try {
                        client.server.bidOnItem(client.getBidderName(), line, bid);
                    } catch (RemoteException e) {
                        System.err.println("Bidding for item went wrong:");
                        e.printStackTrace();
                    }
                    break;
                case "5":
                    System.out.printf("Your current bidding strategy: %s%n", client.getBiddingStrategy());
                    System.out.println("Type in the item name you wish to register for:");
                    line = input.nextLine();
                    try {
                        client.server.registerListener(client, line);
                    } catch (RemoteException e) {
                        System.err.println("Registering for bidding went wrong:");
                        e.printStackTrace();
                    }
                    break;
                case "6":
                    client.getOwnedItems();
                    break;
                case "7":
                    System.out.println("Type in the item name you wish to place for bidding:");
                    String itemName = input.nextLine();
                    System.out.println("Type in the item description:");
                    String itemDesc = input.nextLine();
                    System.out.println("Type in the starting bid (example: 1.99):");
                    bid = Double.parseDouble(input.nextLine());
                    System.out.println("Type in the auction time in seconds:");
                    int time = Integer.parseInt(input.nextLine());
                    try {
                        client.server.placeItemForBid(
                                client.getBidderName(), itemName, itemDesc, bid, time
                        );
                    } catch (RemoteException e) {
                        System.err.println("Placing an item for auction went wrong:");
                        e.printStackTrace();
                    }
                    break;
                default:
                    client.printHelp();
            }
            line = input.nextLine();
        }
    }
}
