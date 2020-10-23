package implementation.utils.helpers.clientUtils;

import implementation.entities.item.Item;
import implementation.utils.date.DateUtils;
import implementation.utils.logger.Logger;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.*;

public class ClientUtils {

    ////////////////////////////////////
    ///       UDP Related Ports      ///
    ////////////////////////////////////
    private static int quebecPurchaseItemUDPPort = 30000;
    private static int quebecListItemUDPPort = 30001;

    private static int britishColumbiaPurchaseItemUDPPort = 30003;
    private static int britishColumbiaListItemUDPPort = 30004;

    private static int ontarioPurchaseItemUDPPort = 30006;
    private static int ontarioListItemUDPPort = 30007;
    
    public static boolean verifyID(String genericID, String provinceID) {
        return genericID.toLowerCase().replace(" ", "").contains(provinceID.toLowerCase().replace(" ",""));
    }

    public static Item purchaseSingularItem(String itemID, HashMap<String, List<Item>> inventory) {
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

    public static Boolean requestItemFromCorrectStore(String customerID, String itemID, String dateOfPurchase, String provinceID) {
        Boolean purchaseSuccesful = false;
        if(itemID.toLowerCase().contains("qc")){
            purchaseSuccesful = requestItemOverUDP(quebecPurchaseItemUDPPort,customerID, itemID, dateOfPurchase,provinceID);
        }
        else if(itemID.toLowerCase().contains("on")){
            purchaseSuccesful = requestItemOverUDP(ontarioPurchaseItemUDPPort,customerID, itemID, dateOfPurchase,provinceID);
        }
        else if(itemID.toLowerCase().contains("bc")){
            purchaseSuccesful = requestItemOverUDP(britishColumbiaPurchaseItemUDPPort,customerID, itemID, dateOfPurchase, provinceID);
        }

        return purchaseSuccesful;
    }

    public static List<Item> mergeAllFoundItems(List<Item> locallyFoundItems, HashMap<String, List<Item>> remotelyFoundItems) {
        List<Item> allItems = new ArrayList<>();

        for(Item item : locallyFoundItems)
            allItems.add(item);

        for(Map.Entry<String, List<Item>> entry : remotelyFoundItems.entrySet())
            for(Item item : entry.getValue())
                allItems.add(item);

        return allItems;
    }

    public static boolean isItemReturnWorthy(Date dateOfPurchase, String dateOfReturn, String itemID) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(dateOfPurchase);
        calendar.add(Calendar.HOUR, 720); //720 hours = 30 days time

        Date dateOfReturnDateObject = DateUtils.createDateFromString(dateOfReturn);
        Date acceptableLastDayForReturn = calendar.getTime();

        return !dateOfReturnDateObject.after(acceptableLastDayForReturn);
    }

    public static void returnItemToInventory(String itemID, List<Item> itemLog, HashMap<String, List<Item>> inventory) {
        if(inventory.containsKey(itemID.toLowerCase()))
            if(inventory.get(itemID.toLowerCase()) != null)
                for(Item item : itemLog)
                    if(itemID.equalsIgnoreCase(item.getItemID()))
                        inventory.get(itemID).add(item);
                    //TODO You should have error checking for if this type of item does not exist yet(or anymore) in the inventory
        //TODO
    }

    public static void log(Boolean itemSuccessfullyPurchased, String customerID, String itemID, String actionType, String provinceID) {
        String logString = "";
        if(itemSuccessfullyPurchased)
            logString = ">>" +new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date())+" << Task SUCCESSFUL:" + actionType +"Item to Inventory CustomerID: "+customerID+" ItemID: "+itemID;
        else
            logString = ">>" +new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date())+" << Task UNSUCCESSFUL: " + actionType + " Item to Inventory CustomerID: "+customerID+" ItemID: "+itemID;

        Logger.writeUserLog(customerID, logString);
        Logger.writeStoreLog(provinceID, logString);
    }

    public static HashMap<String, List<Item>> getRemoteItemsByName(String itemName, String provinceID) {
        String currentProvinceID = provinceID.toLowerCase();
        HashMap<String, List<Item>> storesAndRemotelyFoundItems = new HashMap<>();

        List<Item> remotelyReceivedItems;

        switch(currentProvinceID){
            case "on":
                remotelyReceivedItems = requestRemoteItemList(quebecListItemUDPPort, itemName);
                storesAndRemotelyFoundItems.put("qc", remotelyReceivedItems);

                remotelyReceivedItems = requestRemoteItemList(britishColumbiaListItemUDPPort, itemName);
                storesAndRemotelyFoundItems.put("bc", remotelyReceivedItems);
                break;
            case "qc":
                remotelyReceivedItems = requestRemoteItemList(ontarioListItemUDPPort, itemName);
                storesAndRemotelyFoundItems.put("on", remotelyReceivedItems);

                remotelyReceivedItems = requestRemoteItemList(britishColumbiaListItemUDPPort, itemName);
                storesAndRemotelyFoundItems.put("bc", remotelyReceivedItems);
                break;
            case "bc":
                remotelyReceivedItems = requestRemoteItemList(britishColumbiaListItemUDPPort, itemName);
                storesAndRemotelyFoundItems.put("on", remotelyReceivedItems);

                remotelyReceivedItems = requestRemoteItemList(quebecListItemUDPPort, itemName);
                storesAndRemotelyFoundItems.put("qc", remotelyReceivedItems);
                break;
        }
        return storesAndRemotelyFoundItems;
    }

    private static List<Item> requestRemoteItemList(int listItemUDPPort, String itemName) {
        DatagramSocket socket = null;
        try {
            InetAddress ip = InetAddress.getLocalHost();
            socket = new DatagramSocket();
            //take input and send the packet
            byte[] b = itemName.getBytes();
            DatagramPacket dp = new DatagramPacket(b, b.length, ip, listItemUDPPort);
            System.out.println("Port we are making the request to list to: " + listItemUDPPort);
            socket.send(dp);
            //TODO Log the request
            Thread.sleep(3000);
            //now receive reply
            //buffer to receive incoming data
            int bufferSize = 1024 * 4;
            byte[] buffer = new byte[bufferSize];
            DatagramPacket reply = new DatagramPacket(buffer, bufferSize, ip, listItemUDPPort);
            socket.receive(reply);

            ByteArrayInputStream in = new ByteArrayInputStream(buffer);
            ObjectInputStream is = new ObjectInputStream(in);

            List<Item> items = null;

            items = (List<Item>) is.readObject();
            for (Item item : items)
                System.out.println(item.toString());
            System.out.println("Item List from storeport " + listItemUDPPort + "has been received...");
            return items;

//---------------------------------
            //Create buffer
        }
        catch(Exception e)
        {
            System.out.println("Error in requesting item remotely, restart process ....");
        }

        return new ArrayList<>();
    }

    private static Boolean requestItemOverUDP(int storePort, String customerID, String itemID, String dateOfPurchase, String provinceID) { //Sending out requests to purchase items
        DatagramSocket socket = null;
        String requestString;
        Boolean purchaseSuccesful = false;
        try
        {
            socket = new DatagramSocket();
            InetAddress host = InetAddress.getLocalHost();
            byte[] incomingData = new byte[1024];
            //take input and send the packet
            requestString = packageRequestAsString(customerID, itemID, dateOfPurchase);
            byte[] b = requestString.getBytes();
            DatagramPacket dp = new DatagramPacket(b, b.length, host, storePort);
            socket.send(dp);
            //TODO Log the request

            //now receive reply
            //buffer to receive incoming data
            DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
            socket.receive(incomingPacket); //TODO Could be receiving a null object, may need to refactor
            byte[] data = incomingPacket.getData();
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            ObjectInputStream is = new ObjectInputStream(in);

            purchaseSuccesful = (Boolean) is.readObject();
            is.close();
            System.out.println("Item object received and purchase successful: "+purchaseSuccesful);

            String logString = ">>" +new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date())+" << Task SUCCESSFUL: Purchase Item from another store CustomerID: "+customerID+" ItemID: "+itemID;
            Logger.writeUserLog(customerID, logString);
            Logger.writeStoreLog(provinceID, logString);

            //TODO Log the response
            return purchaseSuccesful;
        }
        catch(Exception e)
        {
            System.err.println("Could not effectuate request item over UDP due to a socket error... Restart process of purchase or finding...");

        }
        return purchaseSuccesful;
    }

    private static String packageRequestAsString(String customerID, String itemID, String dateOfPurchase) {
        StringBuilder requestMessage = new StringBuilder();
        requestMessage.append(customerID+"\n");
        requestMessage.append(itemID+"\n");
        requestMessage.append(dateOfPurchase+"\n");

        return requestMessage.toString();
    }

}
