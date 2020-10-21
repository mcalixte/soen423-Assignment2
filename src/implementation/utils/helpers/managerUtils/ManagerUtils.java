package implementation.utils.helpers.managerUtils;

import implementation.entities.item.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManagerUtils {

    public static boolean verifyID(String genericID, String provinceID) {
        return genericID.toLowerCase().replace(" ", "").contains(provinceID.toLowerCase().replace(" ",""));
    }

    public static int getItemQuantity(String itemID, HashMap<String, List<Item>> inventory){
        for(Map.Entry<String, List<Item>> entry : inventory.entrySet()){
            if(entry.getKey().equalsIgnoreCase(itemID))
                return entry.getValue().size();
        }
        return -1; //Meaning this item does not exist in the inventory
    }

    public static boolean customerHasRequiredFunds(String customerID, double price, HashMap<String, Double> customerBudgetLog) {
        boolean customerHasFunds = false;
        for(Map.Entry<String, Double> entry : customerBudgetLog.entrySet())
            if(entry.getKey().equalsIgnoreCase(customerID)) {
                System.out.println("Current customer budget: CustomerID: "+entry.getKey()+" Budget: "+entry.getValue());
                if(entry.getValue() != null && (entry.getValue() - price) >= 0.00)
                    return true;
                else
                    return false;
            }

        customerHasFunds = ! (price > 1000);

        return customerHasFunds;
    }

    public static void addToStock(Item item, HashMap<String, List<Item>> inventory)  {
        String formattedItemID = item.getItemID().toLowerCase();
        List<Item> itemsInInventoryList = inventory.get(formattedItemID);
        if(itemsInInventoryList != null)
            if(itemsInInventoryList.size() > 0)
                if(itemsInInventoryList.get(0).getPrice() == item.getPrice())
                    inventory.get(item.getItemID().toLowerCase()).add(item);
                else
                    System.out.println("Alert: Item will not be added, this item does not have the same price as others of its kind... ");
            else{
                System.out.println("Alert: Item will be added, this item is the first of its kind... ");
                itemsInInventoryList.add(item);
                inventory.put(item.getItemID().toLowerCase(), itemsInInventoryList);
            }
        else{
            System.out.println("Alert: Item will be added for the first time... ");
            List<Item> itemList = new ArrayList<>();
            itemList.add(item);
            inventory.put(item.getItemID().toLowerCase(), itemList);
        }
    }

    public static Item removeSingularItem(String itemID, HashMap<String, List<Item>> inventory) {
        String formattedItemID = itemID.toLowerCase();
        List<Item> items = inventory.get(formattedItemID);
        Item itemToBePurchased = null;
        if(items != null) {
            for(int i = 0; i<1 ; i++){
                itemToBePurchased = items.get(i);
                items.remove(i);
                System.out.println(items.get(i).toString());
            }

            return itemToBePurchased;
        }
        else {
            System.out.println("\nAn item of that name does not exist in this store or has been removed\n");
            return itemToBePurchased;
        }
    }

    public static void removeItemTypeFromInventory(String itemID, HashMap<String, List<Item>> inventory) {
        String formattedItemID = itemID.toLowerCase().replace(" ", "");
        if(inventory.get(formattedItemID) != null)
            inventory.remove(formattedItemID);
        else
            System.out.println("\nAn item of that ID does not exist in this store\n");
    }

    public static String listItems(HashMap<String, List<Item>> inventory) {
        StringBuilder returnMessage = new StringBuilder("This store contains the following items: \r\n"+"\t");
        for(Map.Entry<String, List<Item>> entry : inventory.entrySet()){
            for(Item item : entry.getValue()) {
                returnMessage.append("\t"+item.toString()+" "+ entry.getValue().size() +"\n");
            }
        }
        return returnMessage.toString();
    }
}
