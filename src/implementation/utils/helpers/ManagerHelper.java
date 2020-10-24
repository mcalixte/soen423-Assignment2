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
        String addToStockResponse = "";
        if(ManagerUtils.verifyID(managerID, this.provinceID)){
            for(int i = 0; i < quantity; i++){
                item = new Item(itemID, itemName, price);
                addToStockResponse = ManagerUtils.addToStock(item, store.getInventory());
            }
            store.getItemLog().add(item);

            String waitListResponse = ManagerUtils.handleWaitlistedCustomers(managerID, itemID, price, this.provinceID, store);

            String logString = ">>" +new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date())+" << Task SUCCESSFUL: Add Item to Inventory ManagerID: "+managerID+" ItemID: "+itemID+" ItemName: "+itemName+" Quantity: "+quantity+" Price: "+price;
            //Logger.writeUserLog(managerID, logString);
            //Logger.writeStoreLog(this.provinceID, logString);
            return addToStockResponse+"\n"+item.toString()+"\n"+waitListResponse;
        }
        else {
            String logString = ">>" +new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date())+" << Task UNSUCCESSFUL: Add Item to Inventory ManagerID: "+managerID+" ItemID: "+itemID+" ItemName: "+itemName+" Quantity: "+quantity+" Price: "+price+ "ALERT: You are not permitted to do this action on this store";
            //Logger.writeUserLog(managerID, logString);
            //Logger.writeStoreLog(this.provinceID, logString);
            return "This is a foreign manager, try a proper manager ...";
        }
    }




    public synchronized String removeItem(String managerID, String itemID, int quantity, HashMap<String, List<Item>> inventory) {
        String item = "";
        if(ManagerUtils.verifyID(managerID, this.provinceID))
            if(quantity != -1)
                if(!(quantity > ManagerUtils.getItemQuantity(itemID, inventory))){
                    for(int i = 0; i < quantity ; i++)
                        item = ManagerUtils.removeSingularItem(itemID, inventory);

                    String logString = ">>" +new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date())+" << Task SUCCESSFUL: Remove Item from Inventory ManagerID: "+managerID+" ItemID: "+itemID + " Quantity: "+quantity;
                    //Logger.writeUserLog(managerID, logString);
                    //Logger.writeStoreLog(this.provinceID, logString);
                    return item;
                }
                else {
                    String logString = ">>" +new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date())+" << Task UNSUCCESSFUL: Remove Item from Inventory ManagerID: "+managerID+" ItemID: "+itemID + " Quantity: "+quantity;
                    System.out.println("\nAlert: Can not remove items greater then its availability\n");
                    //Logger.writeStoreLog(this.provinceID, logString);

                    return "Task UNSUCCESSFUL: Remove Item from Inventory ManagerID: "+managerID+" ItemID: "+itemID + " Quantity: "+quantity+"\nAlert: Can not remove items greater then its availability\n";
                }

            else {
                ManagerUtils.removeItemTypeFromInventory(itemID, inventory);
                String logString = ">>" +new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date())+" << Task SUCCESSFUL: Completely Remove Item from Inventory ManagerID: "+managerID+" ItemID: "+itemID + " Quantity: "+quantity;
                //Logger.writeUserLog(managerID, logString);
                //Logger.writeStoreLog(this.provinceID, logString);

                return "Successful: Completely Remove Item from Inventory ManagerID: "+managerID+" ItemID: "+itemID + " Quantity: "+quantity;

            }

        else {
            String logString = ">>" +new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date())+" << Task UNSUCCESSFUL: Remove Item from Inventory ManagerID: "+managerID+" ItemID: "+itemID + " Quantity: "+quantity;
            System.out.println("\nALERT: You are not permitted to do this action on this store\n");
            //Logger.writeUserLog(managerID, logString);
            //Logger.writeStoreLog(this.provinceID, logString);

            return "Task UNSUCCESSFUL: Remove Item from Inventory ManagerID: "+managerID+" ItemID: "+itemID + " Quantity: "+quantity+"\nALERT: You are not permitted to do this action on this store\n";
        }
    }

    public String listItemAvailability(String managerID, HashMap<String, List<Item>> inventory) {
        String returnMessage;
        if(ManagerUtils.verifyID(managerID, this.provinceID)) {
            returnMessage = ManagerUtils.listItems(inventory);
            System.out.println(returnMessage);
            String logString = ">>" +new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date())+" << Task SUCCESSFUL: List Item from Inventory ManagerID: "+managerID+". See server consoles for List";

            //Logger.writeUserLog(managerID, logString);
            //Logger.writeStoreLog(this.provinceID, logString);
        }
        else {
            returnMessage = "\nALERT: You are not permitted to do this action on this store\n";
            //Logger.writeUserLog(managerID, returnMessage);
            System.out.println(returnMessage);
        }
        return returnMessage;
    }

}