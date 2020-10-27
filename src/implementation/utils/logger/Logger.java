package implementation.utils.logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    private static File file;
    private static FileWriter fileWriter;

    private static String currentProvinceCode = "";
    private static String managerBaseRelativeFilePath = "./src/implementation/entities/managers/logs/";
    private static String customerBaseRelativeFilePath = "./src/entities/clients/logs/";
    private static String storeRelativeFilePath = "./src/servers"+currentProvinceCode.toLowerCase()+"/logs/";

    private static final String QC = "QC";
    private static final String BC = "BC";
    private static final String ON = "ON";

    public static void writeStoreLog(String storeID, String output) {
        if(storeID.toLowerCase().contains("on"))
            currentProvinceCode = "on";
        else if(storeID.toLowerCase().contains("qc"))
            currentProvinceCode = "qc";
        else if(storeID.toLowerCase().contains("bc"))
            currentProvinceCode = "bc";

        storeRelativeFilePath = "./src/storeservers/"+currentProvinceCode.toLowerCase()+"/logs/";
        System.out.println(storeRelativeFilePath);
        createFile(storeRelativeFilePath,storeID);
        createFileWriter(storeRelativeFilePath,storeID);
        if(file != null && fileWriter != null) {
            try {
                fileWriter.write(output+"\n\n\n");
                fileWriter.close();
            } catch (IOException e) {
               // e.printStackTrace();
            }
        }
    }



    public static void writeUserLog(String userID, String output) {
        if(userID == null || output == null)
            return;

        currentProvinceCode = userID.substring(0, 2).toLowerCase();
        String userType = userID.substring(2, 3);
        String numberSuffix = userID.substring(3, userID.length() - 1);

        if(userType.equalsIgnoreCase("M")) {
            createFile(managerBaseRelativeFilePath, userID);
            createFileWriter(managerBaseRelativeFilePath,userID);

            if(file != null && fileWriter != null) {
                try {
                    fileWriter.write(output+"\n\n\n");
                    fileWriter.close();
                } catch (IOException e) {
                   // e.printStackTrace();
                }
            }

        }
        else if(userType.equalsIgnoreCase("C")) {
            createFile(customerBaseRelativeFilePath,userID);
            createFileWriter(customerBaseRelativeFilePath,userID);

            if(file != null && fileWriter != null) {
                try {
                    fileWriter.write(output+"\n\n\n");
                    fileWriter.close();
                } catch (IOException e) {
                   // e.printStackTrace();
                }
            }

        }
    }


    private static void createFileWriter(String relativeFilePath, String userID) {
        try {
            fileWriter = new FileWriter(relativeFilePath + userID + ".txt", true);
        } catch (IOException e) {
           // e.printStackTrace();
        }
    }

    private static void createFile(String relativeFilePath, String userID) {
        file = new File(relativeFilePath + userID + ".txt");
        try {
            if (file.createNewFile()) {
            } else {
                System.out.println("Warning: File already exists.");
            }
        } catch (IOException e) {
           // e.printStackTrace();
        }
    }

}
