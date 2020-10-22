package implementation.utils.helpers;

import implementation.StoreImpl;
import implementation.entities.item.Item;
import implementation.utils.date.DateUtils;
import implementation.utils.helpers.clientUtils.ClientUtils;
import implementation.utils.logger.Logger;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.*;

public class ClientHelper {

    private String provinceID;
    public ClientHelper(String provinceID) {
        this.provinceID = provinceID;
    }

    public synchronized Boolean purchaseItem(String customerID, String itemID, String dateOfPurchase, StoreImpl store) {
        //TODO User can be added to a waitlist whether they have money or not for an item, but if purchase fails, try to give the item to any other person waiting and so on.
        //TODO ... If it doesn't succeed do nothing
        Date dateOfPurchaseDateObject =  DateUtils.createDateFromString(dateOfPurchase);
        Boolean isItemSuccessfullyPurchased = false;
        Item purchasedItem;
        if (!ClientUtils.verifyID(customerID, this.provinceID))
            if (store.getCustomerPurchaseLog().containsKey(customerID)) {
                isItemSuccessfullyPurchased = false;
                ClientUtils.log(isItemSuccessfullyPurchased, customerID, itemID, "purchase", this.provinceID);
                return isItemSuccessfullyPurchased;
            }

        if (ClientUtils.verifyID(itemID, this.provinceID)) {
            purchasedItem = ClientUtils.purchaseSingularItem(itemID, store.getInventory());
            isItemSuccessfullyPurchased = purchasedItem == null ? false : true;
            System.out.println("isItemSuccessfullyPurchased: " + isItemSuccessfullyPurchased);
            if (isItemSuccessfullyPurchased) {
                double price = 0.00;

                for (Map.Entry<String, List<Item>> entry : store.getInventory().entrySet())
                    if (entry.getKey().equalsIgnoreCase(itemID))
                        price = entry.getValue().get(0).getPrice();

                System.out.println("MKC1:");
                if (!ClientUtils.customerHasRequiredFunds(customerID, price, store.getCustomerBudgetLog())) {
                    ClientUtils.log(!isItemSuccessfullyPurchased, customerID, itemID, "purchase", this.provinceID);
                    return false;
                }
                System.out.println("MKC2:");
                if ( store.getCustomerBudgetLog().containsKey(customerID.toLowerCase()))
                    if (ClientUtils.customerHasRequiredFunds(customerID.toLowerCase(), price,  store.getCustomerBudgetLog()))
                        store.getCustomerBudgetLog().put(customerID.toLowerCase(),  store.getCustomerBudgetLog().get(customerID.toLowerCase()) - price);
                    else
                        System.out.println("Alert: Item to expensive for the customers budget...");

                else if (ClientUtils.customerHasRequiredFunds(customerID.toLowerCase(), price,  store.getCustomerBudgetLog())) {
                    System.out.println("MKC3:");
                    Double budget = 1000.00 - price;
                    store.getCustomerBudgetLog().put(customerID.toLowerCase(), budget);
                }

                HashMap<String, Date> itemIDandDateOfPurchase = new HashMap<>();
                itemIDandDateOfPurchase.put(itemID, dateOfPurchaseDateObject);
                store.getCustomerPurchaseLog().put(customerID, itemIDandDateOfPurchase);
                store.requestUpdateOfCustomerBudgetLog(customerID, price);
                ClientUtils.log(isItemSuccessfullyPurchased, customerID, itemID, "purchase", this.provinceID);
            } else {
                Scanner scanner = new Scanner(System.in);
                System.out.println("Do you want to be put on the waitlist for this item, yes or no: ");
                String answer = scanner.nextLine();
                if (answer.equalsIgnoreCase("yes")) {
                    store.waitList(customerID, itemID, dateOfPurchase);
                }

                ClientUtils.log(isItemSuccessfullyPurchased, customerID, itemID, "purchase", this.provinceID);
            }

        } else {
            System.out.println("Alert: Item does not seem to belong in this store, requesting item from the appropriate store...");
            isItemSuccessfullyPurchased = ClientUtils.requestItemFromCorrectStore(customerID, itemID, dateOfPurchase, this.provinceID);

            if (isItemSuccessfullyPurchased) {

                HashMap<String, Date> itemIDandDateOfPurchase = new HashMap<>();
                itemIDandDateOfPurchase.put(itemID, dateOfPurchaseDateObject);
                store.getCustomerPurchaseLog().put(customerID, itemIDandDateOfPurchase);
                ClientUtils.log(isItemSuccessfullyPurchased, customerID, itemID, "purchase", this.provinceID);
            }
            ClientUtils.log(isItemSuccessfullyPurchased, customerID, itemID, "purchase", this.provinceID);
        }


        return isItemSuccessfullyPurchased;
    }

    public String findItem(String customerID, String itemName, HashMap<String, List<Item>> inventory) {
        List<Item> locallyFoundItems = new ArrayList<>();
        HashMap<String, List<Item>> remotelyFoundItems = new HashMap<>();

        locallyFoundItems = getItemsByName(itemName, inventory);
        remotelyFoundItems = ClientUtils.getRemoteItemsByName(itemName, this.provinceID);
        List<Item> allFoundItems = ClientUtils.mergeAllFoundItems(locallyFoundItems, remotelyFoundItems);

        String logString = "";
        System.out.println(">>>>>>>>>>>> All Items Found <<<<<<<<<<<< \n");
        StringBuilder foundItems = new StringBuilder();
        for (Item item : allFoundItems)
            foundItems.append("\t" + item.toString() + "\n");

        if (allFoundItems != null)
            if (allFoundItems.size() == 0)
                logString = ">>" + new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " << Task SUCCESSFUL: Find Item from local and remote Inventory CustomerID: " + customerID + " Item name: " + itemName + ". HOWEVER, No items found.";

            else
                logString = ">>" + new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " << Task SUCCESSFUL: Find Item from local and remote Inventory CustomerID: " + customerID + " Item name: " + itemName + "." + allFoundItems.size() + " item(s) found.";
        else
            logString = ">>" + new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " << Task UNSUCCESSFUL: Find Item from local and remote Inventory CustomerID: " + customerID + " Item name: " + itemName + ". No items found.";

        Logger.writeStoreLog(this.provinceID, logString);
        Logger.writeUserLog(customerID, logString);

        return foundItems.toString();
    }

    public synchronized String returnItem(String customerID, String itemID, String dateOfReturn, StoreImpl store) {
        Date dateOfReturnDateObject =  DateUtils.createDateFromString(dateOfReturn);
        if (ClientUtils.verifyID(customerID, this.provinceID))
            if (store.getCustomerPurchaseLog().containsKey(customerID))
                if (store.getCustomerPurchaseLog().get(customerID).containsKey(itemID)) {
                    if (ClientUtils.isItemReturnWorthy((store.getCustomerPurchaseLog().get(customerID).get(itemID)), dateOfReturn, itemID)) {
                        store.getCustomerReturnLog().put(customerID, store.getCustomerPurchaseLog().get(customerID));
                        ClientUtils.returnItemToInventory(itemID, store.getItemLog(), store.getInventory());

                        String logString = ">>" + new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + "<< Task SUCCESSFUL: Return Item to Inventory CustomerID: " + customerID + " ItemID: " + itemID;
                        Logger.writeUserLog(customerID, logString);
                        Logger.writeStoreLog(this.provinceID, logString);
                        String itemIDToReturn;
                        itemIDToReturn = store.getInventory().get(itemID) != null &&  store.getInventory().get(itemID).size() > 0 ? itemID : "";

                        return itemIDToReturn+"\n"+true;
                    } else {
                        System.out.println("Alert: Customer has purchased this item in the past, but item purchase date exceeds 30days");
                        String logString = ">>" + new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " << Task UNSUCCESSFUL: Return Item to Inventory CustomerID: " + customerID + " ItemID: " + itemID;
                        Logger.writeUserLog(customerID, logString);
                        Logger.writeStoreLog(this.provinceID, logString);

                        String itemIDToReturn;
                        itemIDToReturn = store.getInventory().get(itemID) != null &&  store.getInventory().get(itemID).size() > 0 ? itemID : "";

                        return itemIDToReturn+" \n"+false;
                    }
                } else {
                    System.out.println("Alert: Customer has past purchases, but NOT of this item");
                    String logString = ">>" + new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " << Task UNSUCCESSFUL: Return Item to Inventory CustomerID: " + customerID + " ItemID: " + itemID;
                    Logger.writeUserLog(customerID, logString);
                    Logger.writeStoreLog(this.provinceID, logString);
                    return " \n"+false;
                }
            else {
                System.out.println("Alert: Customer has no record of past purchases");
                String logString = ">>" + new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " << Task UNSUCCESSFUL: Return Item to Inventory CustomerID: " + customerID + " ItemID: " + itemID;
                Logger.writeUserLog(customerID, logString);
                Logger.writeStoreLog(this.provinceID, logString);
                return " \n"+false;
            }
        else {
            System.out.println("Alert: Customer should not be attempting returns at this store, try an appropriate store...");
            String logString = ">>" + new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " << Task UNSUCCESSFUL: Return Item to Inventory CustomerID: " + customerID + " ItemID: " + itemID;
            Logger.writeUserLog(customerID, logString);
            return " \n"+false;
        }
    }

    public List<Item> getItemsByName(String itemName, HashMap<String, List<Item>> inventory) { return new ArrayList<>(); }


    ////////////////////////////////////
    ///     UDP Related Methods      ///
    ////////////////////////////////////
    public void sendCustomerBudgetUpdate(int customerBudgetPort, String customerID, double price, StoreImpl store) {
        DatagramSocket serverSocket = null;
        HashMap<String, Double> requestMap = new HashMap<>();
        Double updatedCustomerBudget = 1000.00;

        if(store.getCustomerBudgetLog().containsKey(customerID.toLowerCase()))
            updatedCustomerBudget = store.getCustomerBudgetLog().get(customerID);
        try
        {
            serverSocket = new DatagramSocket();
            InetAddress ip = InetAddress.getLocalHost();

            requestMap.put(customerID, updatedCustomerBudget);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(outputStream);
            os.writeObject(requestMap);

            byte[] data = outputStream.toByteArray();
            DatagramPacket sendPacket = new DatagramPacket(data, data.length , ip, customerBudgetPort);
            serverSocket.send(sendPacket); //TODO

        }
        catch(Exception e)
        {
            System.err.println("Exception " + e);
            System.out.println("Error in sending out the updated customer budget, restart process ....");
        }

    }
}
