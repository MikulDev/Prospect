package com.momosoftworks.prospect.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Conversions
{
    public static String formatDate(long timestamp)
    {   SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy | hh:mm a");
        return sdf.format(new Date(timestamp));
    }
}
