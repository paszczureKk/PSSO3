package common;

import lombok.Getter;
import lombok.Setter;

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

    @Getter private final String ownerName;
    @Getter private final String itemName;
    @Getter private final String itemDescription;
    @Getter private double currentBid;
    @Getter @Setter private String currentBidder = null;
    @Getter private int timeRemaining;

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
