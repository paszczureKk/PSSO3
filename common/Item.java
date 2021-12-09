package common;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

public class Item {
    public Item(String ownerName, String itemName, String itemDesc,
                double currentBid, int timeRemaining) {
        this.ownerName = ownerName;
        this.itemName = itemName;
        this.itemDescription = itemDesc;
        this.currentBid = currentBid;
        this.timeRemaining = timeRemaining;
    }

    private ArrayList<IAuctionListener> listeners = new ArrayList<IAuctionListener>();

    @Getter @Setter private String ownerName;
    @Getter @Setter private String itemName;
    @Getter @Setter private String itemDescription;
    @Getter @Setter private double currentBid;
    @Getter @Setter private String currentBidder = null;
    @Getter @Setter private int timeRemaining;

    public void addListener(IAuctionListener listener) {
        listeners.add(listener);
    }
    public ArrayList<IAuctionListener> getListeners() {
        return listeners;
    }
}
