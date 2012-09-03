//Take the incomming message from the weighbridge, sort it out and populate the 
//weight against the appropriate weighbridge device in a hash table

package com.bamburi.bamburiwb;

import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class WeighbridgeDevice extends VtiCustomSerialDeviceExit
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

		try
		{
			force7BitAscii_ = getConfigBoolean (configSectionName, "Force7BitAscii");
			messageStartPos_ = getConfigInteger (configSectionName, "WeightStartPos");
			messageFinishPos_ = getConfigInteger (configSectionName, "WeightFinishPos");
			weightMultiplier_ = getConfigInteger (configSectionName, "WeightMultiplier");
		}
		catch (VtiExitException ee) { };

		return true;
	}

	// Processing incoming weight messages from the serial device.

	public int processInput(String inputString, byte [] inputBytes)
	{
		Log.trace(2,"Message from processInput");
		Log.trace(2,"--------------------------------");
		Log.trace(2,"inputString = : " + inputString);
		Log.trace(2,"--------------------------------");		
		
		String serialDeviceName = getSerialDevice().getName();
		
		Log.trace(2,"For Bridge " + serialDeviceName);
		Log.trace(2,"--------------------------------");
		
		// Unset the high order bit.

		if (force7BitAscii_)
		{
			for (int i = inputBytes.length; i-- > 0; )
				inputBytes[i] = (byte)(inputBytes[i] & 0x7F);

			inputString = new String(inputBytes);
		}

		// Extract the weight from the incoming message.
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
				Log.trace(2,"Index of  " + inputString.indexOf(""));
				Log.trace(2,"--------------------------------");
				if(inputString.indexOf("") > 0 || inputString.startsWith(""))
			    {
					
					Log.trace(2,"PRESCIA MOLLEN DEVICE DECONSTRUCT BEGIN");
					Log.trace(2,"********************************");
					Log.trace(2,"--------------------------------");
					Log.trace(2,"Input string lenght is " + inputString.length());
					
					for(int i = 0; i < inputString.length();i++)
					{
						Log.trace(2,"inputString = : " + inputString);
						Log.trace(2,"i is at top of for " + i);
						Log.trace(2,"--------------------------------");
						
						if(i > 60)
							break;
						Log.trace(2,"Stepping through the input string " + i);
						Log.trace(2,"--------------------------------");
						
						sPM = inputString.substring(inputString.indexOf("",i)+1, inputString.indexOf("",i)+3);
						
						Log.trace(2,"Substring from protocol segment si" + (inputString.indexOf("",i)+1) + " |ei " + (inputString.indexOf("",i)+3));
						Log.trace(2,sPM);
						Log.trace(2,"--------------------------------");
						
						if(sPM.equalsIgnoreCase("03"))
					    {
							Log.trace(2,"Protocol 03 substring si " + (inputString.indexOf("",i)+4) + " |ei " + inputString.indexOf(".kg",inputString.indexOf("",i)));
							Log.trace(2,"--------------------------------");
							
							inputString = inputString.substring(inputString.indexOf("",i)+4,inputString.indexOf(".kg",inputString.indexOf("",i))) + "kg";
							
							Log.trace(2,"New input string is " + inputString);
							Log.trace(2,"PRESCIA MOLLEN INPUT DECONSTRUCT END");
							Log.trace(2,"--------------------------------");
							Log.trace(2,"********************************");
							break;
						}
											  
						i = inputString.indexOf("",i) + 1;
						Log.trace(2,"i is now " + i);
						Log.trace(2,"--------------------------------");
					}
				}
				
				kgTokenPosition = inputString.lastIndexOf("g");	
				if(kgTokenPosition > 0)
				{
					
					inputString = inputString.substring (0, kgTokenPosition-1);
					
					Log.trace(2,"Input string after kg removed is " + inputString);
					Log.trace(2,"--------------------------------");
				
					if(inputString.trim().length() > 0)
					{
						String convString = "";
						convString = inputString.trim();	

							
						if(convString.substring(0,3).equalsIgnoreCase("99 "))
						{
							wbWeightSt = convString.trim().substring(2,convString.length());
							Log.trace(2,"Weighbridge weight string is " + wbWeightSt);
							Log.trace(2,"--------------------------------");
						}
						else
						{
							wbWeightSt = convString.trim();		
							Log.trace(2,"Weighbridge weight string is " + wbWeightSt);
							Log.trace(2,"--------------------------------");						
						}
							
						try 
						{
							isANum = new Double(wbWeightSt);
							Log.trace(2,"Is weighbridge weight string a number " + isANum);
							Log.trace(2,"--------------------------------");
						}
						catch (NumberFormatException nfe)
						{
							
							wbWeightSt = "0.0";
							Log.trace(2,"Weighbridge weight string is not a number " + wbWeightSt);
							Log.trace(2,"--------------------------------");						} 
					}
				}
				else
				{
					if(inputString.trim().length() > 0)
					{
						String convString = inputString.trim();;
						Log.trace(2,"Input string with no kg UOM is " + convString);
						Log.trace(2,"--------------------------------");
							
						if(convString.substring(0,3).equalsIgnoreCase("99 "))
						{
							wbWeightSt = convString.trim().substring(2,convString.length());
							Log.trace(2,"Weighbridge weight string is " + wbWeightSt);
							Log.trace(2,"--------------------------------");						}
						else
						{
							wbWeightSt = convString.trim();	
							Log.trace(2,"Weighbridge weight string is " + wbWeightSt);
							Log.trace(2,"--------------------------------");
						}
							
						try 
						{
							isANum = new Double(wbWeightSt);
							Log.trace(2,"Is weighbridge weight string a number " + isANum);
							Log.trace(2,"--------------------------------");
						}
						catch (NumberFormatException nfe)
						{
							wbWeightSt = "0.0";
							Log.trace(2,"Weighbridge weight string is not a number " + wbWeightSt);
							Log.trace(2,"--------------------------------");
						} 
					}
				}
				
					
			}
			catch (StringIndexOutOfBoundsException ee)
			{
			};
			wbWeightSt = wbWeightSt.trim();
		}
			
			
		Double string2DoubleCast;
		double wbWeight = 0.0;

		if (msgLength > 0)
		{
			try 
			{
				Log.trace(2,"Weighbridge weight string before casting to double " + wbWeightSt);
				string2DoubleCast = Double.valueOf(wbWeightSt);
				wbWeight = string2DoubleCast.doubleValue();
				Log.trace(2,"Weighbridge weight string casting to double " + wbWeight);
				Log.trace(2,"@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
			}
			catch (NumberFormatException nfe)
			{
				wbWeight = 0.0;
				//return 0;
			} 

			catch (NullPointerException npex)
			{
				wbWeight = 0.0;
				//return 0;
			}


			ReadingInformation.flush(serialDeviceName);
			
				if(wbWeight > 0)
				{
					ReadingInformation.put (serialDeviceName,new Double(wbWeight));
				}
				else
				{
					ReadingInformation.put(serialDeviceName,new Double(0));
				}
				
		}
		return 0;
	}
}
