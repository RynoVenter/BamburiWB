package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class ConCom extends VtiUserExit 
{
	public String serialDeviceName = null;
	
	public VtiUserExitResult execute() throws VtiExitException
	{
		String errorMsg = null;
		
		VtiUserExitScreenField btnCon = getScreenField("BT_CONNECT");
		if(btnCon == null) return new VtiUserExitResult (999,"Failed to initialise BT_CONNECT.");
		//Get the device to be used to do the weighin
				VtiUserExitHeaderInfo headerInfo = getHeaderInfo();
				int deviceNumber = headerInfo.getDeviceNumber();
				
				headerInfo.setNextFunctionId("YSWB_MAIN");
				
				if(deviceNumber > 0)
				{
					VtiUserExitScreenField scrConStat = getScreenField("COM_STAT");
					if(scrConStat == null) return new VtiUserExitResult(999, "COM_STAT not found");
					
					String deviceNum = Integer.toString(deviceNumber);
					String configSectionName = "DeviceGrouping:" + StringUtil.stringPad(deviceNum,3,'0',true);
					serialDeviceName = getConfigString(configSectionName, "WeighbridgeDevice",null);
				
					//Start and Stop Com Connection
					VtiExitSerialDevice serialDevice = getSerialDevice(serialDeviceName);	
		
					if(serialDevice.isConnected() == false)
					{
						Log.info("Attempting to reconnect " + serialDeviceName);
						serialDevice.connect();
						if(serialDevice.isConnected() == false)
						{
							scrConStat.setFieldValue("Off");
							errorMsg = "Re-connection failed, try again please.";
							Log.info("Re-connection failed.");
							btnCon.setFieldValue("Connect Com");
						}
						else if(serialDevice.isConnected() == true)
						{
							scrConStat.setFieldValue("On");
							Log.info("Re-connection succeeded.");
							errorMsg = "Re-connection of " + serialDeviceName + " successfull";
							btnCon.setFieldValue("Disconnect Com");
			
						}
					}
					else
					{
						serialDevice.disconnect();
						if(serialDevice.isConnected() == false)
						{
							scrConStat.setFieldValue("Off");
							errorMsg = "Comm port " + serialDeviceName + " disconnected.";
							btnCon.setFieldValue("Connect Com");
						}
					}
				}
				else
					errorMsg = "No device maintained in config.";
		
		return new VtiUserExitResult(000,errorMsg);
	}
}
