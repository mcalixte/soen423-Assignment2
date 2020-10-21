package implementation.utils.helpers;

import implementation.StoreImpl;
import implementation.entities.item.Item;

import java.util.HashMap;
import java.util.List;

public class ManagerHelper {

    public String addItem(String managerID, String itemID, String itemName, int quantity, double price, StoreImpl store) { return "managerHelper addItem"; }
    public String removeItem(String managerID, String itemID, int quantity, HashMap<String, List<Item>> inventory) { return "managerHelper removeItem"; }
    public String listItemAvailability(String managerID, HashMap<String, List<Item>> inventory) { return "managerHelper listItemAvailability"; }

}