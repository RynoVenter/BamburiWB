package com.bamburi.bamburiwb;

import java.util.*;
import java.util.Hashtable.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class GetBridgeWeight5 extends VtiUserExit
{
	public String serialDeviceName = null;
	
	/*Class for getting and displaying the weight from the weighbridge
	 */
	public VtiUserExitResult execute() throws VtiExitException
	{	
		VtiUserExitScreenField scrFDispWeight = getScreenField("WEIGHT");
		
		if(scrFDispWeight == null) return new VtiUserExitResult (999,"Failed to initialise WEIGHT.");
		
		//Get the device to be used to do the weighin
		VtiUserExitHeaderInfo headerInfo = getHeaderInfo();
		int deviceNumber = headerInfo.getDeviceNumber();
		
		Log.trace(2, "Device number is " + deviceNumber);
		
		if(deviceNumber == 0)
			return new VtiUserExitResult(999,"Serial device number is not set up for this station.");
		
		scrFDispWeight.setFieldValue("");
		
		String deviceNum = Integer.toString(deviceNumber);
		String configSectionName = "DeviceGrouping:" + StringUtil.stringPad(deviceNum,3,'0',true);
		
		Log.trace(2, "Device number after correction " + deviceNumber);

		serialDeviceName = getConfigString(configSectionName, "WeighbridgeDevice",null);
		ReadingInformation.flush(serialDeviceName);
		
		boolean stableW = false;
		boolean checkStable = true;		
		final String startCode = "";
		
		//Get the weight of the weighbridge indicated from the weighbridge selected on the screen.
		VtiExitSerialDevice serialDevice = getSerialDevice(serialDeviceName);	
		
		if(serialDevice.isConnected() == false)
		{
			Log.trace(2, "Serial device not connected, attempting re-connect");
			serialDevice.connect();
			
			if(serialDevice.isConnected() == false)
			{
				Log.trace(2, "Serial device failed to re-connect");	
			}
		}
		
		
		
		//boolean con = serialDevice.isConnected();
		Double dblWeight = new Double(0.0);
			
		serialDevice.write(startCode);
				
		if(serialDeviceName == null)
		{
			return new VtiUserExitResult(999,"Serial device name could not be found from the configuration file.");
		}
		//Added after phase 2 go-live
		dblWeight = (Double)ReadingInformation.get(serialDeviceName);
	
		try
		{
			Thread.sleep(500);
		}
		catch (InterruptedException ie)
		{
			Log.error("Sleep loop for weigh 1 get failed.");
		}
		
		
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
						
			//dblWeight = (Double)ReadingInformation.get(serialDeviceName);
						
			try
			{
				dblWeight = (Double)ReadingInformation.get(serialDeviceName);
			}
			catch(NullPointerException npe)
			{
				Log.error("dblWeight = (Double)ReadingInformation.get(serialDeviceName) failed ", npe);
				scrFDispWeight.setFieldValue("0");
			}
			try
			{
				Thread.sleep(200);
			}
			catch(InterruptedException ie)
			{
				Log.error("Thread.sleep(200) failed ", ie);		
			}
		}//Stop of add phase II
				
		Log.trace(2, "Double from com port " + serialDeviceName + " was " + dblWeight);	
		double double2dbl = 0.0;
					
		try
		{
			double2dbl = dblWeight.doubleValue();
		}
		catch(NullPointerException pnex)
		{
			new VtiUserExitResult(999,1,"WIMS is experiencing some difficulty communication with the bridge, please re-connect manually if this persists.");
				
			//scrFDispWeight.setFieldValue("0");

				/*if(bRunOnce)
				{
					//Start and Stop Com Connection
				
					serialDevice.disconnect();
						
						
					try
					{
						Thread.sleep(300);
					}
					catch(InterruptedException ie)
					{
						Log.error("Sleep failed", ie);	
					}
							
					if(serialDevice.isConnected() == false)
					{
						Log.info("Attempting to reconnect " + serialDeviceName);
						serialDevice.connect();
						if(serialDevice.isConnected() == false)
						{				
							Log.error("Re-connection failed.");
							return new VtiUserExitResult(000,1,"Bridge " + serialDeviceName + " failed to reconnect. Weights cannot be picked from the bridge. Call support immediatly.");
									
						}
						else if(serialDevice.isConnected() == true)
						{
							Log.info("Re-connection succeeded.");
						}
					}
				}*/
			double2dbl = 0;
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
			
			
		if(scrFDispWeight.getDoubleFieldValue() == 0)
		{
			dblWeight = new Double(0.0);
			
			serialDevice.write(startCode);
				
			if(serialDeviceName == null)
			{
				return new VtiUserExitResult(999,"Serial device name could not be found from the configuration file.");
			}
			
			try
			{
				dblWeight = (Double)ReadingInformation.get(serialDeviceName);
			}
			catch(NullPointerException npe)
			{
			}
	
				
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
				
				try
				{
					dblWeight = (Double)ReadingInformation.get(serialDeviceName);
				}
				catch(NullPointerException npe)
				{
					Log.error("dblWeight = (Double)ReadingInformation.get(serialDeviceName) failed ", npe);
					scrFDispWeight.setFieldValue("0");
				}
				
				try
				{
					dblWeight = (Double)ReadingInformation.get(serialDeviceName);
				}
				catch(NullPointerException npe)
				{
					Log.error("dblWeight = (Double)ReadingInformation.get(serialDeviceName) failed ", npe);
					scrFDispWeight.setFieldValue("0");
				}
				try
				{
					Thread.sleep(200);
				}
				catch(InterruptedException ie)
				{
					Log.error("Thread.sleep(200) failed ", ie);		
				}
			}//Stop of add phase II
				
			Log.trace(2, "Double from com port " + serialDeviceName + " was " + dblWeight);	
			double2dbl = 0.0;
					
			try
			{
				double2dbl = dblWeight.doubleValue();
			}
			catch(NullPointerException pnex)
			{
				new VtiUserExitResult(999,1,"WIMS is having difficulty to communicate, please re-connect the bridge manually if this persists.");
								
				//scrFDispWeight.setFieldValue("0");
				double2dbl = 0;
			}
				
				
			stWeightReading = Double.toString(double2dbl);
				
			try
			{
				if(stWeightReading.length() > 0)
				{
					scrFDispWeight.setFieldValue(stWeightReading);
					
					VtiUserExitScreenField scrFZerod = getScreenField("ZEROD");
					if(scrFZerod != null)
					{
						if(scrFDispWeight.getFieldValue().equalsIgnoreCase("0.0"))
							scrFZerod.setFieldValue("ZEROD");
					}
				}
				else
				{
					scrFDispWeight.setFieldValue("0");
					
					VtiUserExitScreenField scrFZerod = getScreenField("ZEROD");
					if(scrFZerod != null)
					{
						if(scrFDispWeight.getFieldValue().equalsIgnoreCase("0.0"))
							scrFZerod.setFieldValue("ZEROD");
					}
				}
			}
			catch( NullPointerException npe)
			{
			}
		}
			
		//con = serialDevice.isConnected();
		
		VtiUserExitScreenField scrFBridge = getScreenField("WEIGHBRIDGE");
		if(scrFBridge != null) 
		{
		
			VtiUserExitScreenTable scrTblCustom = getScreenTable("TB_CUSTOM");
			if(scrTblCustom == null) return new VtiUserExitResult (999,"Failed to initialise TB_CUSTOM.");

			int tr = 0; 
		
			VtiUserExitScreenTableRow wghbr = scrTblCustom.getRow(tr);
		
			wghbr.setFieldValue("FIELDVALUE",scrFBridge.getFieldValue());
		}
				
		ReadingInformation.flush(serialDeviceName);
		
		return new VtiUserExitResult();
	}
}
