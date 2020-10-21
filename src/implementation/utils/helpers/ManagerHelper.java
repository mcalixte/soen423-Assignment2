package implementation.utils.helpers;

import implementation.StoreImpl;
import implementation.entities.item.Item;
import implementation.utils.helpers.managerUtils.ManagerUtils;
import implementation.utils.logger.Logger;

import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ManagerHelper {
    private String provinceID;

    public ManagerHelper(String provinceID) {
        this.provinceID = provinceID;
    }

    public synchronized String addItem(String managerID, String itemID, String itemName, int quantity, double price, StoreImpl store) {
        if(ManagerUtils.verifyID(managerID, this.provinceID)){
            for(int i = 0; i < quantity; i++){
                Item item = new Item(itemID, itemName, price);
                ManagerUtils.addToStock(item, store.getInventory());
            }

            for(Map.Entry<String, List<String>> entry : store.getItemWaitList().entrySet()){
                System.out.println("MKC3.5---:"+1);
                if(itemID.equalsIgnoreCase(entry.getKey())) {
                    System.out.println("MKC:4 ---: "+itemID.equalsIgnoreCase(entry.getKey()));
                    for(int i = 0; i < entry.getValue().size(); i++) {
                        System.out.println("MKC5: "+ManagerUtils.customerHasRequiredFunds(entry.getValue().get(i), price, store.getCustomerBudgetLog()));
                        if(ManagerUtils.customerHasRequiredFunds(entry.getValue().get(i), price, store.getCustomerBudgetLog())) {
                            HashMap<String, Date> map = new HashMap<>();
                            String dateString = new SimpleDateFormat("mm/dd/yyyy HH:mm").format(new Date());
                            try {
                                map.put(itemID, new SimpleDateFormat("mm/dd/yyyy HH:mm").parse(dateString));
                            } catch (ParseException e) {
                                System.out.println("Unable to purchase item due to a malformed date string... Restart the process of purchasing");
                            }

                            try {
                                store.purchaseItem(entry.getValue().get(i), itemID, dateString);
                            } catch (Exception e) {
                                System.out.println("Unable to purchase item due to a malformed date string... Restart the process of purchasing");
                            }
                            store.getCustomerPurchaseLog().put(entry.getKey(), map);

                            if(store.getCustomerBudgetLog().containsKey(entry.getKey().toLowerCase()))
                                store.getCustomerBudgetLog().put(entry.getKey().toLowerCase(), store.getCustomerBudgetLog().get(entry.getKey().toLowerCase()) - price);
                            else
                                store.getCustomerBudgetLog().put(entry.getKey().toLowerCase(), 1000.00 - price);

                            String logString = ">>" +new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date())+" << Task SUCCESSFUL: Purchased Item from inventory Customer: "+ entry.getKey() +"have now received their items ItemID: "+itemID;
                            Logger.writeUserLog(managerID, logString);
                            Logger.writeUserLog(entry.getKey(), logString);
                            Logger.writeStoreLog(this.provinceID, logString);
                        }
                    }
                }
            }

            String logString = ">>" +new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date())+" << Task SUCCESSFUL: Add Item to Inventory ManagerID: "+managerID+" ItemID: "+itemID+" ItemName: "+itemName+" Quantity: "+quantity+" Price: "+price;
            Logger.writeUserLog(managerID, logString);
            Logger.writeStoreLog(this.provinceID, logString);
        }
        else {
            System.out.println("\nALERT: You are not permitted to do this action on this store\n");
            String logString = ">>" +new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date())+" << Task UNSUCCESSFUL: Add Item to Inventory ManagerID: "+managerID+" ItemID: "+itemID+" ItemName: "+itemName+" Quantity: "+quantity+" Price: "+price;
            Logger.writeUserLog(managerID, logString);
            Logger.writeStoreLog(this.provinceID, logString);
        }
        return "managerHelper addItem";
    }


    public synchronized String removeItem(String managerID, String itemID, int quantity, HashMap<String, List<Item>> inventory) {
        Item item = null;
        if(ManagerUtils.verifyID(managerID, this.provinceID))
            if(quantity != -1)
                if(!(quantity > ManagerUtils.getItemQuantity(itemID, inventory))){
                    for(int i = 0; i<quantity; i++)
                        item = ManagerUtils.removeSingularItem(itemID, inventory);

                    String logString = ">>" +new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date())+" << Task SUCCESSFUL: Remove Item from Inventory ManagerID: "+managerID+" ItemID: "+itemID + " Quantity: "+quantity;
                    Logger.writeUserLog(managerID, logString);
                    Logger.writeStoreLog(this.provinceID, logString);
                    return item.toString();
                }
                else {
                    String logString = ">>" +new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date())+" << Task UNSUCCESSFUL: Remove Item from Inventory ManagerID: "+managerID+" ItemID: "+itemID + " Quantity: "+quantity;
                    System.out.println("Alert: Can not remove items greater then its availability");
                    Logger.writeStoreLog(this.provinceID, logString);

                    return item.toString();
                }

            else {
                ManagerUtils.removeItemTypeFromInventory(itemID, inventory);
                String logString = ">>" +new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date())+" << Task SUCCESSFUL: Completely Remove Item from Inventory ManagerID: "+managerID+" ItemID: "+itemID + " Quantity: "+quantity;
                System.out.println("\nALERT: You are not permitted to do this action on this store\n");
                Logger.writeUserLog(managerID, logString);
                Logger.writeStoreLog(this.provinceID, logString);

                return item.toString();

            }

        else {
            String logString = ">>" +new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date())+" << Task UNSUCCESSFUL: Remove Item from Inventory ManagerID: "+managerID+" ItemID: "+itemID + " Quantity: "+quantity;
            System.out.println("\nALERT: You are not permitted to do this action on this store\n");
            Logger.writeUserLog(managerID, logString);
            Logger.writeStoreLog(this.provinceID, logString);

            return item.toString();
        }
    }

    public String listItemAvailability(String managerID, HashMap<String, List<Item>> inventory) {
        String returnMessage;
        if(ManagerUtils.verifyID(managerID, this.provinceID)) {
            returnMessage = ManagerUtils.listItems(inventory);
            System.out.println(returnMessage);
            String logString = ">>" +new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date())+" << Task SUCCESSFUL: List Item from Inventory ManagerID: "+managerID+". See server consoles for List";

            Logger.writeUserLog(managerID, logString);
            Logger.writeStoreLog(this.provinceID, logString);
        }
        else {
            returnMessage = "\nALERT: You are not permitted to do this action on this store\n";
            Logger.writeUserLog(managerID, returnMessage);
            System.out.println(returnMessage);
        }
        return returnMessage;
    }



}