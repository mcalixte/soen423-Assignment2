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
        Item item = null;
        if(ManagerUtils.verifyID(managerID, this.provinceID)){
            for(int i = 0; i < quantity; i++){
                item = new Item(itemID, itemName, price);
                ManagerUtils.addToStock(item, store.getInventory());
            }

            ManagerUtils.handleWaitlistedCustomers(managerID, itemID, price, this.provinceID, store);

            String logString = ">>" +new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date())+" << Task SUCCESSFUL: Add Item to Inventory ManagerID: "+managerID+" ItemID: "+itemID+" ItemName: "+itemName+" Quantity: "+quantity+" Price: "+price;
            Logger.writeUserLog(managerID, logString);
            Logger.writeStoreLog(this.provinceID, logString);
            return item.toString();
        }
        else {
            String logString = ">>" +new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date())+" << Task UNSUCCESSFUL: Add Item to Inventory ManagerID: "+managerID+" ItemID: "+itemID+" ItemName: "+itemName+" Quantity: "+quantity+" Price: "+price+ "ALERT: You are not permitted to do this action on this store";
            Logger.writeUserLog(managerID, logString);
            Logger.writeStoreLog(this.provinceID, logString);
            return "ItemID: null, Item Name: null, Price: null";
        }
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