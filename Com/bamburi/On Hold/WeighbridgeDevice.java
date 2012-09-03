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
		boolean blnRunOnce = true;
		boolean blnReRun = false;
		
		String serialDeviceName = getSerialDevice().getName();
		
		// Unset the high order bit.
		while(blnRunOnce || blnReRun)
		{
			blnReRun = false;
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
					if(inputString.indexOf("") > 0 || inputString.startsWith(""))
				    {
						for(int i = 0; i < inputString.length();i++)
						{
								
							if(i > 60)
								break;
								
							sPM = inputString.substring(inputString.indexOf("",i)+1, inputString.indexOf("",i)+3);
														
							if(sPM.equalsIgnoreCase("03"))
						    {
								inputString = inputString.substring(inputString.indexOf("",i)+4,inputString.indexOf(".kg",inputString.indexOf("",i))) + "kg";
								break;
							}
							i = inputString.indexOf("",i) + 1;
						}
					}
						
					kgTokenPosition = inputString.lastIndexOf("g");	
					if(kgTokenPosition > 0)
					{
							
						inputString = inputString.substring (0, kgTokenPosition-1);
						
						if(inputString.trim().length() > 0)
						{
							String convString = "";
							convString = inputString.trim();	

							if(convString.substring(0,3).equalsIgnoreCase("99 "))
							{
								wbWeightSt = convString.trim().substring(2,convString.length());
							}
							else
							{
								wbWeightSt = convString.trim();							
							}
									
							try 
							{
								isANum = new Double(wbWeightSt);
							}
							catch (NumberFormatException nfe)
							{
								blnReRun = true;
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
							}
							else
							{
								wbWeightSt = convString.trim();	
							}
									
							try 
							{
								isANum = new Double(wbWeightSt);
							}
							catch (NumberFormatException nfe)
							{
								blnReRun = true;
								wbWeightSt = "0.0";
							} 
						}
					}
						
							
				}
				catch (StringIndexOutOfBoundsException ee)
				{
					blnReRun = true;
				};
				wbWeightSt = wbWeightSt.trim();
			}
				
				
			Double string2DoubleCast;
			double wbWeight = 0.0;

			if (msgLength > 0)
			{
				try 
				{
					string2DoubleCast = Double.valueOf(wbWeightSt);
					wbWeight = string2DoubleCast.doubleValue();

				}
				catch (NumberFormatException nfe)
				{
					wbWeight = 0.0;
					blnReRun = true;
					//return 0;
				} 

				catch (NullPointerException npex)
				{
					wbWeight = 0.0;
					blnReRun = true;
					//return 0;
				}


				ReadingInformation.flush(serialDeviceName);
					
					if(wbWeight > 0)
					{
						ReadingInformation.put(serialDeviceName,new Double(wbWeight));
					}
					else
					{
						ReadingInformation.put(serialDeviceName,new Double(0));
					}
			}
			blnRunOnce = false;
		}
		return 0;
	}
}
