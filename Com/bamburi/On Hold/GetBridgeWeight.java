package com.bamburi.bamburiwb;

import java.util.*;
import java.util.Hashtable.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class GetBridgeWeight extends VtiUserExit
{
	public String serialDeviceName = null;
	
	/*Class for getting and displaying the weight from the weighbridge
	 */
	public VtiUserExitResult execute() throws VtiExitException
	{	
		VtiUserExitScreenField scrFBridge = getScreenField("WEIGHBRIDGE");
		VtiUserExitScreenField scrFDispWeight = getScreenField("WEIGHT");
		
		if(scrFBridge == null) return new VtiUserExitResult (999,"Failed to initialise WEIGHBRIDGE.");
		if(scrFDispWeight == null) return new VtiUserExitResult (999,"Failed to initialise WEIGHT.");
		
		//Get the device to be used to do the weighin
		VtiUserExitHeaderInfo headerInfo = getHeaderInfo();
		int deviceNumber = headerInfo.getDeviceNumber();
		if(deviceNumber == 0)
			return new VtiUserExitResult(999,"Serial device number is not set up for this station.");
		
		scrFDispWeight.setFieldValue("");
		
		String deviceNum = Integer.toString(deviceNumber);
		String configSectionName = "DeviceGrouping:" + StringUtil.stringPad(deviceNum,3,'0',true);
		serialDeviceName = getConfigString(configSectionName, "WeighbridgeDevice",null);
		boolean stableW = false;
		boolean checkStable = false;
		int stableCount = 0;
		
		final String startCode = "";
		
		//Get the weight of the weighbridge indicated from the weighbridge selected on the screen.
		VtiExitSerialDevice serialDevice = getSerialDevice(serialDeviceName);	
		
		if(serialDevice.isConnected() == false)
			serialDevice.connect();
		
		boolean con = serialDevice.isConnected();
		

		serialDevice.write(startCode);

		
			if(serialDeviceName == null)
			{
				return new VtiUserExitResult(999,"Serial device name could not be found from the configuration file.");
			}
			//ReadingInformation.flush(serialDeviceName);//Added after phase 2 go-live
			Double dblWeight = (Double)ReadingInformation.get(serialDeviceName);
			
			try
			{
				dblWeight = (Double)ReadingInformation.get(serialDeviceName);
			}
			catch(NullPointerException npe)
			{
			}
			

		for(int run = 0; run < 2;run++)//Added after phase 2 go-live
		{
			//ReadingInformation.flush(serialDeviceName);
			serialDevice.write(startCode);

		
				if(serialDeviceName == null)
				{
					return new VtiUserExitResult(999,"Serial device name could not be found from the configuration file.");
				}
				
				dblWeight = (Double)ReadingInformation.get(serialDeviceName);
				
				
				try
				{
					dblWeight = (Double)ReadingInformation.get(serialDeviceName);
				}
				catch(NullPointerException npe)
				{
					scrFDispWeight.setFieldValue("0");
				}
				try
				{
					Thread.sleep(200);
				}
				catch(InterruptedException ie)
				{
					
				}
		}//Stop of add phase II
		
		
		double double2dbl = 0.0;
			
		try
		{
			double2dbl = dblWeight.doubleValue();
		}
		catch(NullPointerException pnex)
		{
			scrFDispWeight.setFieldValue("0");
			return new VtiUserExitResult(999,"Bridge unstable.");
		}
		
		
		String stWeightReading = Double.toString(double2dbl);
		
		try
		{
			if(stWeightReading.length() > 0)
			{
				scrFDispWeight.setFieldValue(stWeightReading);
			}
			else
			{
				scrFDispWeight.setFieldValue("0");
			}
		}
		catch( NullPointerException npe)
		{
		}
		
		
		VtiUserExitScreenTable scrTblCustom = getScreenTable("TB_CUSTOM");
		if(scrTblCustom == null) return new VtiUserExitResult (999,"Failed to initialise TB_CUSTOM.");

		int tr = 0; 
		
		VtiUserExitScreenTableRow wghbr = scrTblCustom.getRow(tr);
		
		wghbr.setFieldValue("FIELDVALUE",scrFBridge.getFieldValue());
		
		con = serialDevice.isConnected();
		
		ReadingInformation.flush(serialDeviceName);
		
		return new VtiUserExitResult();
	}
}
