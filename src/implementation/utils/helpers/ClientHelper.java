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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ClientHelper {

    private String provinceID;
    public ClientHelper(String provinceID) {
        this.provinceID = provinceID;
    }

    public synchronized String purchaseItem(String customerID, String itemID, String dateOfPurchase, StoreImpl store) {
        //TODO User can be added to a waitlist whether they have money or not for an item, but if purchase fails, try to give the item to any other person waiting and so on.
        //TODO ... If it doesn't succeed do nothing
        Date dateOfPurchaseDateObject =  DateUtils.createDateFromString(dateOfPurchase);
        Boolean isItemSuccessfullyPurchased = false;
        String purchasedItem;
        String response;
        if (!ClientUtils.verifyID(customerID, this.provinceID))
            if (store.getCustomerPurchaseLog().containsKey(customerID.toLowerCase())) {
                isItemSuccessfullyPurchased = false;
                ClientUtils.log(isItemSuccessfullyPurchased, customerID, itemID, "purchase", this.provinceID);
                return "Task UNSUCCESSFUL: Foreign Customer has purchased from this store once before " + customerID + "," + itemID + "," + dateOfPurchase + "," + "" + isItemSuccessfullyPurchased + "";
            }

        double price = 0.00;
        price = getSpecificItemPrice(itemID, store, price);

        if(price != -1) {
            if (!ClientUtils.customerHasRequiredFunds(customerID, price, store.getCustomerBudgetLog())) {
                ClientUtils.log(!isItemSuccessfullyPurchased, customerID, itemID, "purchase", this.provinceID);
                response = "Task UNSUCCESSFUL: Customer does not have the funds for this item,"+customerID + "," + itemID + "," + dateOfPurchase + "," + isItemSuccessfullyPurchased;

                return response;
            }
        }

        purchasedItem = ClientUtils.purchaseSingularItem(itemID, store.getInventory());
        isItemSuccessfullyPurchased = purchasedItem.equalsIgnoreCase("An item of that name does not exist in this store or has been removed") ? false : true;

        if (isItemSuccessfullyPurchased) {
            if (store.getCustomerBudgetLog().containsKey(customerID.toLowerCase())) {
                store.getCustomerBudgetLog().put(customerID.toLowerCase(), store.getCustomerBudgetLog().get(customerID.toLowerCase()) - price);
                updateCustomerPurchaseLog(customerID, itemID, store, dateOfPurchaseDateObject);
                System.out.println("MKC2: DateOfPurchaseObject: "+dateOfPurchaseDateObject.toString()+" "+dateOfPurchaseDateObject);
                store.requestUpdateOfCustomerBudgetLog(customerID.toLowerCase(), store.getCustomerBudgetLog().get(customerID.toLowerCase()));
            }
            else {
                Double budget = 1000.00 - price;
                store.getCustomerBudgetLog().put(customerID.toLowerCase(), budget);
                updateCustomerPurchaseLog(customerID, itemID, store, dateOfPurchaseDateObject);
                System.out.println("MKC2: DateOfPurchaseObject: "+dateOfPurchaseDateObject.toString()+" "+dateOfPurchaseDateObject);
                store.requestUpdateOfCustomerBudgetLog(customerID.toLowerCase(), store.getCustomerBudgetLog().get(customerID.toLowerCase()));
            }

            ClientUtils.log(isItemSuccessfullyPurchased, customerID, itemID, "purchase", this.provinceID);
            response = "Task SUCCESSFUL: Customer purchased Item "+customerID + "," + itemID + "," + dateOfPurchase + "," + isItemSuccessfullyPurchased;
            return response;
        } else {
            if(itemID.contains(this.provinceID.toLowerCase())) {
                store.waitList(customerID, itemID, dateOfPurchase);
                ClientUtils.log(isItemSuccessfullyPurchased, customerID, itemID, "purchase", this.provinceID);
                response = "Task UNSUCCESSFUL: However customer added to the waitlist for this item. "+customerID + "," + itemID + "," + dateOfPurchase + "," + isItemSuccessfullyPurchased;
                return response;
            }
            else{
                response = ClientUtils.requestItemFromCorrectStore(customerID, itemID, dateOfPurchase, this.provinceID);
                return response;
            }
        }
    }

    private void updateCustomerPurchaseLog(String customerID, String itemID, StoreImpl store, Date dateOfPurchaseDateObject) {
        HashMap<String, Date> itemIDandDateOfPurchase = new HashMap<>();
        itemIDandDateOfPurchase.put(itemID, dateOfPurchaseDateObject);
        String formattedCustomerID = customerID.toLowerCase();
        if(store.getCustomerPurchaseLog().containsKey(formattedCustomerID))
            if(store.getCustomerPurchaseLog().get(formattedCustomerID) != null)
                store.getCustomerPurchaseLog().get(formattedCustomerID).add(itemIDandDateOfPurchase);
            else{
                List<HashMap<String, Date>> list = new ArrayList<>();
                list.add(itemIDandDateOfPurchase);
                store.getCustomerPurchaseLog().put(formattedCustomerID, list);
            }
        else {
            List<HashMap<String, Date>> list = new ArrayList<>();
            list.add(itemIDandDateOfPurchase);
            store.getCustomerPurchaseLog().put(formattedCustomerID, list);
        }
    }

    private double getSpecificItemPrice(String itemID, StoreImpl store, double price) {
        for (Item item : store.getItemLog())
            if (item.getItemID().equalsIgnoreCase(itemID))
                price = item.getPrice();

        return price;
    }

    public synchronized String findItem(String customerID, String itemName, HashMap<String, List<Item>> inventory) {
        List<Item> locallyFoundItems = new ArrayList<>();
        HashMap<String, List<Item>> remotelyFoundItems = new HashMap<>();

        locallyFoundItems = getItemsByName(itemName, inventory);
        remotelyFoundItems = ClientUtils.getRemoteItemsByName(itemName, this.provinceID);
        List<Item> allFoundItems = ClientUtils.mergeAllFoundItems(locallyFoundItems, remotelyFoundItems);

        StringBuilder logString = new StringBuilder();
        logString.append(">>>>>>>>>>>> All Items Found <<<<<<<<<<<< \n");
        StringBuilder foundItems = new StringBuilder();
        for (Item item : allFoundItems)
            foundItems.append("\t" + item.toString() + "\n");

        if (allFoundItems != null)
            if (allFoundItems.size() == 0)
                logString.append(">>" + new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " << Task SUCCESSFUL: Find Item from local and remote Inventory CustomerID: " + customerID + " Item name: " + itemName + ". HOWEVER, No items found.");

            else
                logString.append(">>" + new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " << Task SUCCESSFUL: Find Item from local and remote Inventory CustomerID: " + customerID + " Item name: " + itemName + "." + allFoundItems.size() + " item(s) found.");
        else
            logString.append(">>" + new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " << Task UNSUCCESSFUL: Find Item from local and remote Inventory CustomerID: " + customerID + " Item name: " + itemName + ". No items found.");

        //Logger.writeStoreLog(this.provinceID, logString.toString());
        //Logger.writeUserLog(customerID, logString.toString());

        return foundItems.toString();
    }

    public synchronized String returnItem(String customerID, String itemID, String dateOfReturn, StoreImpl store) {
        Date dateOfReturnDate = null;
        String response = "";
        try {
            dateOfReturnDate = new SimpleDateFormat("mm/dd/yyyy HH:mm").parse(dateOfReturn);
        } catch (ParseException e) {
           // e.printStackTrace();
        }
        if (ClientUtils.verifyID(itemID, this.provinceID))
            if (store.getCustomerPurchaseLog().containsKey(customerID.toLowerCase()))
                if (store.getCustomerPurchaseLog().get(customerID.toLowerCase()) != null) {
                    Date dateOfPurchase = findDateFromCustomerPurchaseLog(customerID, itemID, store);
                    System.out.println("MKC1: dateOfPurchase: "+dateOfPurchase);
                    if (ClientUtils.isItemReturnWorthy(dateOfPurchase, dateOfReturn, itemID)) {
                        HashMap<String, Date> map = new HashMap<>();
                        map.put(itemID, dateOfReturnDate);
                        store.getCustomerReturnLog().put(customerID, map);
                        ClientUtils.returnItemToInventory(itemID, store.getItemLog(), store.getInventory());
                        removeCustomerFromPurchaseLog(customerID, itemID, store);

                        Double price = 0.00;
                        for(Item item : store.getItemLog())
                            if(item.getItemID().equalsIgnoreCase(itemID))
                                price = item.getPrice();

                        store.getCustomerBudgetLog().put(customerID.toLowerCase(), store.getCustomerBudgetLog().get(customerID.toLowerCase()) + price);
                        store.requestUpdateOfCustomerBudgetLog(customerID.toLowerCase(), store.getCustomerBudgetLog().get(customerID.toLowerCase()));
                        // String logString = ">>" + new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + "<< Task SUCCESSFUL: Return Item to Inventory CustomerID: " + customerID + " ItemID: " + itemID;
                        //Logger.writeUserLog(customerID, logString);
                        //Logger.writeStoreLog(this.provinceID, logString);
                        String itemIDToReturn;
                        itemIDToReturn = store.getInventory().get(itemID) != null &&  store.getInventory().get(itemID).size() > 0 ? itemID : "";
                        response = "Task SUCCESSFUL: Customer "+ customerID+ " returned Item" + itemID+" on "+ dateOfReturn+" "+true;
                        return response;
                    } else {
                        System.out.println("Alert: Customer has purchased this item in the past, but item purchase date exceeds 30days");
                        // String logString = ">>" + new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " << Task UNSUCCESSFUL: Return Item to Inventory CustomerID: " + customerID + " ItemID: " + itemID;
                        //Logger.writeUserLog(customerID, logString);
                        //Logger.writeStoreLog(this.provinceID, logString);

                        String itemIDToReturn;
                        itemIDToReturn = store.getInventory().get(itemID) != null &&  store.getInventory().get(itemID).size() > 0 ? itemID : "";

                        response = "Task UNSUCCESSFUL: Customer "+ customerID+ " returned Item" + itemID+" on "+ dateOfReturn+"\nAlert: Customer has purchased this item in the past, but item purchase date exceeds 30days";
                        return response;
                    }
                } else {
                    System.out.println("Alert: Customer has past purchases, but NOT of this item");
                    // String logString = ">>" + new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " << Task UNSUCCESSFUL: Return Item to Inventory CustomerID: " + customerID + " ItemID: " + itemID;
                    //Logger.writeUserLog(customerID, logString);
                    //Logger.writeStoreLog(this.provinceID, logString);
                    response = "Task UNSUCCESSFUL: Customer "+ customerID+ " returned Item" + itemID+" on "+ dateOfReturn+"\n"+"Alert: Customer has past purchases, but NOT of this item";
                    return response;
                }
            else {
                System.out.println("Alert: Customer has no record of past purchases");
                //// String logString = ">>" + new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " << Task UNSUCCESSFUL: Return Item to Inventory CustomerID: " + customerID + " ItemID: " + itemID;
                //Logger.writeUserLog(customerID, logString);
                //Logger.writeStoreLog(this.provinceID, logString);
                response = "Task UNSUCCESSFUL: Customer "+ customerID+ " returned Item" + itemID+" on "+ dateOfReturn+"\n"+"Alert: Customer has past purchases, but NOT of this item";;
                return response;
            }
        else {
            System.out.println("Alert: Item does not belong to this store...");
            //// String logString = ">>" + new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ").format(new Date()) + " << Task UNSUCCESSFUL: Return Item to Inventory CustomerID: " + customerID + " ItemID: " + itemID;
            //Logger.writeUserLog(customerID, logString);
            response = ClientUtils.returnItemToCorrectStore(customerID, itemID, dateOfReturn, provinceID);
            return response;
        }
    }

    private Date findDateFromCustomerPurchaseLog(String customerID, String itemID, StoreImpl store) {
        String dateOfPurchase = new SimpleDateFormat("dd/mm/yyyy HH:mm").format(new Date());
        Date dateOfPurchaseDate = new Date();
        try {
            dateOfPurchaseDate = new SimpleDateFormat("dd/mm/yyyy HH:mm").parse(dateOfPurchase);
        } catch (ParseException e) {
           // e.printStackTrace();
        }

        for(Map.Entry<String, List<HashMap<String, Date>>> entry : store.getCustomerPurchaseLog().entrySet())
            if(entry.getKey().equalsIgnoreCase(customerID.toLowerCase()))
                if(entry.getValue() != null)
                    if(entry.getValue().size() > 0)
                        for(int i = 0; i <= entry.getValue().size() - 1; i++)
                            if(entry.getValue().get(i).containsKey(itemID.toLowerCase()))
                                return entry.getValue().get(i).get(itemID.toLowerCase());
        System.out.println("Returning todays date:"+dateOfPurchaseDate);
        return dateOfPurchaseDate;
    }

    private void removeCustomerFromPurchaseLog(String customerID, String itemID, StoreImpl store) {
        for(Map.Entry<String, List<HashMap<String, Date>>> entry : store.getCustomerPurchaseLog().entrySet())
            if(entry.getKey().equalsIgnoreCase(customerID))
                if(entry.getValue() != null )
                    if(entry.getValue().size() != 0)
                        for(int i = 0; i < entry.getValue().size() - 1; i++)
                            if(entry.getValue().get(i).containsKey(itemID))
                                entry.getValue().remove(i);
                    else if(entry.getValue().size() == 0)
                        store.getCustomerPurchaseLog().remove(customerID);
                else if(entry.getValue() != null )
                    store.getCustomerPurchaseLog().remove(customerID);
    }

    public List<Item> getItemsByName(String itemName, HashMap<String, List<Item>> inventory) { List<Item> itemsWithSameName = new ArrayList<>();
        for(Map.Entry<String, List<Item>> entry : inventory.entrySet()){
            for(Item item : entry.getValue()){
                if(item.getItemName().equalsIgnoreCase(itemName))
                    itemsWithSameName.add(item);
            }
        }
        return itemsWithSameName;
    }


    ////////////////////////////////////
    ///     UDP Related Methods      ///
    ////////////////////////////////////
    public void sendCustomerBudgetUpdate(int customerBudgetPort, String customerID, double budget, StoreImpl store) {
        DatagramSocket serverSocket = null;
        HashMap<String, Double> requestMap = new HashMap<>();
        Double updatedCustomerBudget = budget;

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
