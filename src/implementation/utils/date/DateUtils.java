package implementation.utils.date;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    public static Date createDateFromString(String dateString) {
        Date localdatetime = null;

        try {
            localdatetime = new SimpleDateFormat("dd/MM/yyyy HH:mm").parse(dateString);
            return localdatetime;
        }catch(Exception e) {
            System.out.println("Error in creating the Date object, restart request creation...");
        }
        return localdatetime;
    }
}
