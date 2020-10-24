package implementation.utils.helpers.managerUtils;

import implementation.StoreImpl;
import implementation.entities.item.Item;
import implementation.utils.logger.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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

    public static String addToStock(Item item, HashMap<String, List<Item>> inventory)  {
        String response = "";
        String formattedItemID = item.getItemID().toLowerCase();
        List<Item> itemsInInventoryList = inventory.get(formattedItemID);
        if(itemsInInventoryList != null)
            if(itemsInInventoryList.size() > 0)
                if(itemsInInventoryList.get(0).getPrice() == item.getPrice())
                    inventory.get(item.getItemID().toLowerCase()).add(item);
                else
                    response = "Alert: Item will not be added, this item does not have the same price as others of its kind... ";
            else{
                response = "Alert: Item will be added, this item is the first of its kind... ";
                itemsInInventoryList.add(item);
                inventory.put(item.getItemID().toLowerCase(), itemsInInventoryList);
            }
        else{
            response = "Alert: Item will be added for the first time... ";
            List<Item> itemList = new ArrayList<>();
            itemList.add(item);
            inventory.put(item.getItemID().toLowerCase(), itemList);
        }
        return response;
    }

    public static String removeSingularItem(String itemID, HashMap<String, List<Item>> inventory) {
        String formattedItemID = itemID.toLowerCase();
        List<Item> items = inventory.get(formattedItemID);
        String itemToBeRemoved = "";

        if (items != null )
            if(items.size() > 0) {
                itemToBeRemoved = items.get(0).toString();
                items.remove(0);
                return itemToBeRemoved;
            }
            else {
                System.out.println("\nThere are no more items of that type left with the itemID: "+itemID+ "\n");
                return "\nThere are no moore items of that type left with the itemID: "+itemID+ "\n";
            }
        else {
            System.out.println("\nAn item of that name does not exist in this store or has been removed completely\n");
            return itemToBeRemoved;
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

    public static String handleWaitlistedCustomers(String managerID, String itemID, double price, String provinceID, StoreImpl store) {
        String waitlistResponse = "";
        for(Map.Entry<String, List<String>> entry : store.getItemWaitList().entrySet()){
            if(itemID.equalsIgnoreCase(entry.getKey())) {
                for(int i = 0; i < entry.getValue().size() - 1; i++) {
                    if(ManagerUtils.customerHasRequiredFunds(entry.getValue().get(i), price, store.getCustomerBudgetLog())) {
                        HashMap<String, Date> map = new HashMap<>();
                        String dateString = new SimpleDateFormat("mm/dd/yyyy HH:mm").format(new Date());
                        try {
                            map.put(itemID, new SimpleDateFormat("mm/dd/yyyy HH:mm").parse(dateString));
                        } catch (ParseException e) {
                            System.out.println("Unable to purchase item due to a malformed date string... Restart the process of purchasing");
                        }
                        waitlistResponse = "Purchased Item from inventory Customer who was on waitlist: CustomerID:"+entry.getValue().get(i)+" itemID:"+itemID;
                        store.purchaseItem(entry.getValue().get(i), itemID, dateString);
                        store.removeItem(managerID, itemID, 1);
                        store.getCustomerPurchaseLog().put(entry.getKey(), map);
                        entry.getValue().remove(i); //Remove that User from the waitlist

                        String logString = ">>" +new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date())+" << Task SUCCESSFUL: Purchased Item from inventory Customer: "+ entry.getKey() +"have now received their items ItemID: "+ itemID;
                        Logger.writeUserLog(managerID, logString);
                        Logger.writeUserLog(entry.getKey(), logString);
                        Logger.writeStoreLog(provinceID, logString);
                        return waitlistResponse;
                    }
                }
            }
        }

        return "";
    }
}
