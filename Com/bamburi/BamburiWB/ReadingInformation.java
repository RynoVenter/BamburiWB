//Class to host the hash table which is used to put, get, and flush 
//weighbridge readings

package com.bamburi.bamburiwb;
import au.com.skytechnologies.ecssdk.log.*;
import java.util.*;

public class ReadingInformation
{
    private static Hashtable readings_ = new Hashtable();

    public final static Object get(String serialDeviceName)
    { 
        return readings_.get(serialDeviceName); 
    }

    public final static void put(String serialDeviceName, Object readingInfo)
    { 
		flush(serialDeviceName);
        readings_.put(serialDeviceName, readingInfo);
    }
    
    public final static void flush(String serialDeviceName)
    { 
        readings_.remove(serialDeviceName);
    }
}