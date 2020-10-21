package implementation.utils.helpers;

import implementation.StoreImpl;
import implementation.entities.item.Item;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ClientHelper {

    public Boolean purchaseItem(String customerID, String itemID, String dateOfPurchase, StoreImpl store) { return false; }
    public String findItem(String customerID, String itemName, HashMap<String, List<Item>> inventory) { return ""; }
    public String returnItem(String customerID, String itemID, String dateOfReturn, StoreImpl store) { return ""; }

    public List<Item> getItemsByName(String itemName, HashMap<String, List<Item>> inventory) { return new ArrayList<>(); }
}
