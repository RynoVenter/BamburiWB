//Take the incomming message from the weighbridge, sort it out and populate the 
//weight against the appropriate weighbridge device in a hash table

package com.bamburi.bamburiwb;

import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class WeighbridgeDevice3 extends VtiCustomSerialDeviceExit
{
	private int messageStartPos_ = 0;
	private int messageFinishPos_ = 0;
	private boolean force7BitAscii_ = false;
	private int weightMultiplier_ = 0;
	private int stableQualifierPos_ = 0;
	private int stableQualifierVal_ = 0;
	
	public boolean initialise()
	{
		VtiExitSerialDevice serialDevice = getSerialDevice();
		String serialDeviceName = serialDevice.getName();
		String configSectionName = "CustomSerialDevice:" + serialDeviceName;
		
		Log.trace(2, "Initialise info is as follow: serialDevice->" + serialDevice + " | serialDeviceName->" + serialDeviceName + " | configSectionName->" + configSectionName);
/*
		try
		{
			force7BitAscii_ = getConfigBoolean (configSectionName, "Force7BitAscii");
			messageStartPos_ = getConfigInteger (configSectionName, "WeightStartPos");
			messageFinishPos_ = getConfigInteger (configSectionName, "WeightFinishPos");
			weightMultiplier_ = getConfigInteger (configSectionName, "WeightMultiplier");
		}
		catch (VtiExitException ee) { 
		};*/

		return true;
	}

	// Processing incoming weight messages from the serial device.

	public int processInput(String inputString, byte [] inputBytes)
	{
		Log.trace(2, "  ");

		//boolean blnRunOnce = true;
		//boolean blnReRun = false;
		
		Double string2DoubleCast;
		double wbWeight = 0.0;
		
		String serialDeviceName = getSerialDevice().getName();

		if(serialDeviceName.length() < 2)
			Log.error("Serialdevice name not determined");
		// Unset the high order bit.
			Log.trace(2, "Proces input parameters for " + serialDeviceName + " was = raw string " + inputString + " : " + inputBytes.toString());

		if(inputString.length() <= 60)
		{
			inputString = inputString.trim();
				
			int msgLength = inputString.length();
			int kgTokenPosition = 0;
			String wbWeightSt = "";
			Double isANum = new Double(0.0);

			String sPM = "";
			if (msgLength > 0)
			{
				try
				{
					// Check for Prescia Mollen Indicator first
					if(inputString.indexOf("") > 0 || inputString.startsWith(""))
				    {
						int iLc = 0;
						for(int i = 0; i < inputString.length();i++)
						{
							if(i == 0)
								Log.trace(2, "This is a Prescia Mollen string, loop started with " + inputString);
							
							if(inputString.length() < 50)
							{
								initialise();
								inputString = "0000kg";
								break;
							}
							
							if(i > inputString.length()-1)
								break;
									
							sPM = inputString.substring(inputString.indexOf("",i)+1, inputString.indexOf("",i)+3);

							if(sPM.equalsIgnoreCase("03"))
						    {
								inputString = inputString.substring(inputString.indexOf("",i)+4,inputString.indexOf(".kg",inputString.indexOf("",i))) + "kg";
								if(serialDeviceName.equalsIgnoreCase("Dumper") ||
								   serialDeviceName.equalsIgnoreCase("Mbaraki"))
									Log.trace(2, "String after decode " + serialDeviceName + " was " + inputString + ".");
								break;
							}
							
							if(sPM.equalsIgnoreCase("05"))
						    {
								inputString = "0000Kg";
								break;
							}
							
							i = inputString.indexOf("",i) + 1;
							
							iLc++;
							
							if(iLc > 60)
							{
								Log.error("The decode of the input string from the Prescia Mollen terminated due to the decode loop reaching a termination limit of 60 cycles.");
								Log.error("The input string at the time of termination was " + inputString);
								break;
							}
								
						}
					}
				}
				catch (StringIndexOutOfBoundsException siobe)
				{
					Log.error("Prescia Mollen string decode failed with Sting Index error.", siobe);
				}
					
				try
				{
					kgTokenPosition = inputString.lastIndexOf("g");	
					if(kgTokenPosition > 0)
					{
								
						inputString = inputString.substring (0, kgTokenPosition-1);
						if(serialDeviceName.equalsIgnoreCase("Dumper")||
						   serialDeviceName.equalsIgnoreCase("Mbaraki"))
							Log.trace(2, "String after kg is stripped for " + serialDeviceName + " was " + inputString + ".");
						if(inputString.trim().length() > 0)
						{
							String convString = "";
							convString = inputString.trim();	

							if(convString.substring(0,3).equalsIgnoreCase("99 "))
							{
								wbWeightSt = convString.trim().substring(2,convString.length());
								if(serialDeviceName.equalsIgnoreCase("Dumper")||
								   serialDeviceName.equalsIgnoreCase("Mbaraki"))
									Log.trace(2, "Weight string for " + serialDeviceName + " was " + wbWeightSt + ".");
							}
							else
							{
								wbWeightSt = convString.trim();
								if(serialDeviceName.equalsIgnoreCase("Dumper")||
								   serialDeviceName.equalsIgnoreCase("Mbaraki"))
									Log.trace(2, "Weight string for " + serialDeviceName + " was " + wbWeightSt + ".");
							}
										
							try 
							{
								isANum = new Double(wbWeightSt);
							}
							catch (NumberFormatException nfe)
							{
								Log.error("Is a Number check failed ",nfe);
								wbWeightSt = "0.0";
							} 
						}
					}
					else
					{
						if(inputString.trim().length() > 0)
						{
							String convString = inputString.trim();;
										
							if(convString.substring(0,3).equalsIgnoreCase("99 "))
							{
								wbWeightSt = convString.trim().substring(2,convString.length());
								if(serialDeviceName.equalsIgnoreCase("Dumper")||
								   serialDeviceName.equalsIgnoreCase("Mbaraki"))
									Log.trace(2, "Weight string for " + serialDeviceName + " was " + wbWeightSt + ".");
							}
							else
							{
								wbWeightSt = convString.trim();	
								if(serialDeviceName.equalsIgnoreCase("Dumper")||
								   serialDeviceName.equalsIgnoreCase("Mbaraki"))
									Log.trace(2, "Weight string for " + serialDeviceName + " was " + wbWeightSt + ".");
							}
										
							try 
							{
								isANum = new Double(wbWeightSt);
							}
							catch (NumberFormatException nfe)
							{
								Log.error("Is a Number check failed ",nfe);
								wbWeightSt = "0.0";
			
							} 
						}
					}
				}
				catch (StringIndexOutOfBoundsException siobe)
				{
					Log.error("String conversion failed ",siobe);
					wbWeightSt = "0.0";
				};
				wbWeightSt = wbWeightSt.trim();
			}
					
			if (msgLength > 0)
			{
				try 
				{
					string2DoubleCast = Double.valueOf(wbWeightSt);
					wbWeight = string2DoubleCast.doubleValue();

				}
				catch (NumberFormatException nfe)
				{
					Log.error("Number weight format for double to string failed", nfe);
					wbWeight = 0.0;
				} 

				ReadingInformation.flush(serialDeviceName);
						
					if(wbWeight > 0)
					{
						if(serialDeviceName.equalsIgnoreCase("Dumper")||
								   serialDeviceName.equalsIgnoreCase("Mbaraki"))
							Log.trace(2, "Weight double written to ReadingInformation for " + serialDeviceName + " was " + wbWeightSt + ".");
						ReadingInformation.put(serialDeviceName,new Double(wbWeight));
					}
					else
					{
						if(serialDeviceName.equalsIgnoreCase("Dumper")||
						   serialDeviceName.equalsIgnoreCase("Mbaraki"))
							Log.trace(2, "Weight double written to ReadingInformation for " + serialDeviceName + " was zero.");
						ReadingInformation.put(serialDeviceName,new Double(0));
					}
			}
		}
		else
		{
			ReadingInformation.put(serialDeviceName,new Double(0));
			initialise();
		}

		return 0;
	}
}
