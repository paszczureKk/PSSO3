package common;

import java.util.ArrayList;
import java.util.Observable;

@SuppressWarnings("deprecation")
public class Item extends Observable {
    public Item(String ownerName, String itemName, String itemDesc,
                double currentBid, int timeRemaining) {
        this.ownerName = ownerName;
        this.itemName = itemName;
        this.itemDescription = itemDesc;
        this.currentBid = currentBid;
        this.timeRemaining = timeRemaining;
    }

    ArrayList<Integer> watchClock = new ArrayList<>() {{
        add(0);
        add(60);
    }};

    private final String ownerName;
    public String getOwnerName() {
        return this.ownerName;
    }
    private final String itemName;
    public String getItemName() {
        return this.itemName;
    }
    private final String itemDescription;
    public String getItemDescription() {
        return this.itemDescription;
    }
    private double currentBid;
    public double getCurrentBid() {
        return this.currentBid;
    }
    private String currentBidder = null;
    public String getCurrentBidder() {
        return this.currentBidder;
    }
    public void setCurrentBidder(String currentBidder) {
        this.currentBidder = currentBidder;
    }
    private int timeRemaining;
    public int getTimeRemaining() {
        return this.timeRemaining;
    }
    public void setCurrentBid(double bid) {
        this.currentBid = bid;
        setChanged();
        notifyObservers();
    }

    public void setTimeRemaining(int time) {
        this.timeRemaining = time;
        if(watchClock.contains(time)) {
            setChanged();
            notifyObservers();
        }
    }
}
