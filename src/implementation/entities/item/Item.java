package implementation.entities.item;

public class Item {
    private static final long serialVersionUID = 1L;
    private String itemName;
    private String itemID;
    private double price;

    public Item(String itemID, String itemName, double price) {
        this.itemID = itemID;
        this.itemName = itemName;
        this.price = price;
    }

    public String getItemID()  {
        return this.itemID;
    }

    public String getItemName()  {
        return this.itemName;
    }

    public double getPrice()  {
        return this.price;
    }

    public String toString(){
        return "ItemID: "+this.itemID+ " Item Name: "+ this.itemName +  " Price: "+this.price;
    }

}
