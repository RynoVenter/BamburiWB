package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class  ServerWbActivity extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException 
	{
		VtiUserExitScreenField scrDevice1 = getScreenField("WEIGHDEVICE1");
		VtiUserExitScreenField scrBridge1 = getScreenField("BRIDGENAME1");
		VtiUserExitScreenField scrWeight1 = getScreenField("WEIGHT1");			
		
		final String startCode = "";
		boolean stableW = false;
		boolean checkStable = true;
		int stableCount = 0;
		boolean bReRun = false;
		boolean bRunOnce = true;
		Double dblWeight = new Double(0.0);	
		double double2dbl = 0.0;
		
		if(scrDevice1.getFieldValue().length() > 0)
		{
			String deviceNum1 = scrDevice1.getFieldValue();
			String configSectionName1 = "DeviceGrouping:" + StringUtil.stringPad(deviceNum1,3,'0',true);
			
			Log.trace(2, "Device number after correction " + deviceNum1);

			String serialDeviceName1 = getConfigString(configSectionName1, "WeighbridgeDevice",null);
			
			scrBridge1.setFieldValue(serialDeviceName1);

			//Get the weight of the weighbridge indicated from the weighbridge selected on the screen.
			VtiExitSerialDevice serialDevice1 = getSerialDevice(serialDeviceName1);	
			
			if(serialDevice1.isConnected() == false)
			{
				Log.trace(2, "Serial device not connected, attempting re-connect");
				serialDevice1.connect();
				
				if(serialDevice1.isConnected() == false)
				{
					Log.trace(2, "Serial device failed to re-connect");	
				}
			}
					
			//boolean con = serialDevice.isConnected();
			dblWeight = new Double(0.0);

			while((bRunOnce || bReRun) && stableCount < 2)
			{
				stableCount++;
				
				bReRun = false;
				
				serialDevice1.write(startCode);
					
				if(serialDeviceName1 == null)
				{
					return new VtiUserExitResult(000,"Serial device name 1 could not be found from the configuration file.");
				}	
					
				try
				{
					dblWeight = (Double)ReadingInformation.get(serialDeviceName1);
				}
				catch(NullPointerException npe)
				{
				}

				for(int run = 0; run < 2;run++)//Added after phase 2 go-live
				{
					//ReadingInformation.flush(serialDeviceName);
					serialDevice1.write(startCode);
					
					if(serialDeviceName1 == null)
					{
						return new VtiUserExitResult(000,"Serial device name 1 could not be found from the configuration file.");
					}
							
					//dblWeight = (Double)ReadingInformation.get(serialDeviceName);
							
					try
					{
						dblWeight = (Double)ReadingInformation.get(serialDeviceName1);
					}
					catch(NullPointerException npe)
					{
						Log.error("dblWeight = (Double)ReadingInformation.get(serialDeviceName1) failed ", npe);
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
					
				Log.trace(2, "Double from com port " + serialDeviceName1 + " was " + dblWeight);	
				double2dbl = 0.0;
						
				try
				{
					double2dbl = dblWeight.doubleValue();
				}
				catch(NullPointerException pnex)
				{
					
					  bReRun = true;
					
					//scrFDispWeight.setFieldValue("0");

						if(bRunOnce)
						{
							//Start and Stop Com Connection
					
							serialDevice1.disconnect();
							
							
							try
							{
								Thread.sleep(300);
							}
							catch(InterruptedException ie)
							{
								Log.error("Sleep failed", ie);	
							}
								
							if(serialDevice1.isConnected() == false)
							{
								Log.info("Attempting to reconnect " + serialDeviceName1);
								serialDevice1.connect();
								if(serialDevice1.isConnected() == false)
								{				
									bReRun = false;
									Log.error("Re-connection failed.");
									return new VtiUserExitResult(000,1,"Bridge " + deviceNum1 + " failed to reconnect. Weights cannot be picked from the bridge. Call support immediatly.");
										
								}
								else if(serialDevice1.isConnected() == true)
								{
									Log.info("Re-connection succeeded.");
								}
							}
						}
					double2dbl = 0;
				}
					
					
				String stWeightReading1 = Double.toString(double2dbl);
					
				try
				{
					if(stWeightReading1.length() > 0)
					{
						scrWeight1.setFieldValue(stWeightReading1);
					}
					else
					{
						scrWeight1.setFieldValue("0");
					}
				}
				catch( NullPointerException npe)
				{
					bReRun = true;
				}
				
				bRunOnce = false;
			}
					
			ReadingInformation.flush(serialDeviceName1);
		}
		//////////////////////////////////////////////////////////////////////
		VtiUserExitScreenField scrDevice2 = getScreenField("WEIGHDEVICE2");
		VtiUserExitScreenField scrBridge2 = getScreenField("BRIDGENAME2");
		VtiUserExitScreenField scrWeight2 = getScreenField("WEIGHT2");
		
		stableW = false;
		checkStable = true;
		stableCount = 0;
		bReRun = false;
		bRunOnce = true;
			
			
		if(scrDevice2.getFieldValue().length() > 0)
		{
			String deviceNum2 = scrDevice2.getFieldValue();
			String configSectionName2 = "DeviceGrouping:" + StringUtil.stringPad(deviceNum2,3,'0',true);
			
			Log.trace(2, "Device number after correction " + deviceNum2);

			String serialDeviceName2 = getConfigString(configSectionName2, "WeighbridgeDevice",null);
	
			scrBridge2.setFieldValue(serialDeviceName2);
			//Get the weight of the weighbridge indicated from the weighbridge selected on the screen.
			VtiExitSerialDevice serialDevice2 = getSerialDevice(serialDeviceName2);	
			
			if(serialDevice2.isConnected() == false)
			{
				Log.trace(2, "Serial device not connected, attempting re-connect");
				serialDevice2.connect();
				
				if(serialDevice2.isConnected() == false)
				{
					Log.trace(2, "Serial device failed to re-connect");	
				}
			}
					
			//boolean con = serialDevice.isConnected();
			dblWeight = new Double(0.0);

			while((bRunOnce || bReRun) && stableCount < 2)
			{
				stableCount++;
				
				bReRun = false;
				
				serialDevice2.write(startCode);
					
				if(serialDeviceName2 == null)
				{
					return new VtiUserExitResult(000,"Serial device name 2 could not be found from the configuration file.");
				}	
					
				try
				{
					dblWeight = (Double)ReadingInformation.get(serialDeviceName2);
				}
				catch(NullPointerException npe)
				{
				}

				for(int run = 0; run < 2;run++)//Added after phase 2 go-live
				{
					//ReadingInformation.flush(serialDeviceName);
					serialDevice2.write(startCode);
					
					if(serialDeviceName2 == null)
					{
						return new VtiUserExitResult(000,"Serial device name 2 could not be found from the configuration file.");
					}
							
					//dblWeight = (Double)ReadingInformation.get(serialDeviceName);
							
					try
					{
						dblWeight = (Double)ReadingInformation.get(serialDeviceName2);
					}
					catch(NullPointerException npe)
					{
						Log.error("dblWeight = (Double)ReadingInformation.get(serialDeviceName2) failed ", npe);
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
					
				Log.trace(2, "Double from com port " + serialDeviceName2 + " was " + dblWeight);	
				double2dbl = 0.0;
						
				try
				{
					double2dbl = dblWeight.doubleValue();
				}
				catch(NullPointerException pnex)
				{
					
					  bReRun = true;
					
					//scrFDispWeight.setFieldValue("0");

						if(bRunOnce)
						{
							//Start and Stop Com Connection
					
							serialDevice2.disconnect();
							
							
							try
							{
								Thread.sleep(300);
							}
							catch(InterruptedException ie)
							{
								Log.error("Sleep failed", ie);	
							}
								
							if(serialDevice2.isConnected() == false)
							{
								Log.info("Attempting to reconnect " + serialDeviceName2);
								serialDevice2.connect();
								if(serialDevice2.isConnected() == false)
								{				
									bReRun = false;
									Log.error("Re-connection failed.");
									return new VtiUserExitResult(000,1,"Bridge " + deviceNum2 + " failed to reconnect. Weights cannot be picked from the bridge. Call support immediatly.");
										
								}
								else if(serialDevice2.isConnected() == true)
								{
									Log.info("Re-connection succeeded.");
								}
							}
						}
					double2dbl = 0;
				}
					
					
				String stWeightReading2 = Double.toString(double2dbl);
					
				try
				{
					if(stWeightReading2.length() > 0)
					{
						scrWeight2.setFieldValue(stWeightReading2);
					}
					else
					{
						scrWeight2.setFieldValue("0");
					}
				}
				catch( NullPointerException npe)
				{
					bReRun = true;
				}
				
				bRunOnce = false;
			}
					
			ReadingInformation.flush(serialDeviceName2);
		}
		//////////////////////////////////////////////////////////////////////
		VtiUserExitScreenField scrDevice3 = getScreenField("WEIGHDEVICE3");
		VtiUserExitScreenField scrBridge3 = getScreenField("BRIDGENAME3");
		VtiUserExitScreenField scrWeight3 = getScreenField("WEIGHT3");
		
		stableW = false;
		checkStable = true;
		stableCount = 0;
		bReRun = false;
		bRunOnce = true;
			
			
		if(scrDevice3.getFieldValue().length() > 0)
		{
			String deviceNum3 = scrDevice3.getFieldValue();
			String configSectionName3 = "DeviceGrouping:" + StringUtil.stringPad(deviceNum3,3,'0',true);
			
			Log.trace(2, "Device number after correction " + deviceNum3);

			String serialDeviceName3 = getConfigString(configSectionName3, "WeighbridgeDevice",null);
	
			scrBridge3.setFieldValue(serialDeviceName3);
			//Get the weight of the weighbridge indicated from the weighbridge selected on the screen.
			VtiExitSerialDevice serialDevice3 = getSerialDevice(serialDeviceName3);	
			
			if(serialDevice3.isConnected() == false)
			{
				Log.trace(2, "Serial device not connected, attempting re-connect");
				serialDevice3.connect();
				
				if(serialDevice3.isConnected() == false)
				{
					Log.trace(2, "Serial device failed to re-connect");	
				}
			}
					
			//boolean con = serialDevice.isConnected();
			dblWeight = new Double(0.0);

			while((bRunOnce || bReRun) && stableCount < 2)
			{
				stableCount++;
				
				bReRun = false;
				
				serialDevice3.write(startCode);
					
				if(serialDeviceName3 == null)
				{
					return new VtiUserExitResult(000,"Serial device name 3 could not be found from the configuration file.");
				}	
					
				try
				{
					dblWeight = (Double)ReadingInformation.get(serialDeviceName3);
				}
				catch(NullPointerException npe)
				{
				}

				for(int run = 0; run < 2;run++)//Added after phase 2 go-live
				{
					//ReadingInformation.flush(serialDeviceName);
					serialDevice3.write(startCode);
					
					if(serialDeviceName3 == null)
					{
						return new VtiUserExitResult(000,"Serial device name 3 could not be found from the configuration file.");
					}
							
					//dblWeight = (Double)ReadingInformation.get(serialDeviceName);
							
					try
					{
						dblWeight = (Double)ReadingInformation.get(serialDeviceName3);
					}
					catch(NullPointerException npe)
					{
						Log.error("dblWeight = (Double)ReadingInformation.get(serialDeviceName3) failed ", npe);
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
					
				Log.trace(2, "Double from com port " + serialDeviceName3 + " was " + dblWeight);	
				double2dbl = 0.0;
						
				try
				{
					double2dbl = dblWeight.doubleValue();
				}
				catch(NullPointerException pnex)
				{
					
					  bReRun = true;
					
					//scrFDispWeight.setFieldValue("0");

						if(bRunOnce)
						{
							//Start and Stop Com Connection
					
							serialDevice3.disconnect();
							
							
							try
							{
								Thread.sleep(300);
							}
							catch(InterruptedException ie)
							{
								Log.error("Sleep failed", ie);	
							}
								
							if(serialDevice3.isConnected() == false)
							{
								Log.info("Attempting to reconnect " + serialDeviceName3);
								serialDevice3.connect();
								if(serialDevice3.isConnected() == false)
								{				
									bReRun = false;
									Log.error("Re-connection failed.");
									return new VtiUserExitResult(000,"Bridge " + deviceNum3 + " failed to reconnect. Weights cannot be picked from the bridge. Call support immediatly.");
										
								}
								else if(serialDevice3.isConnected() == true)
								{
									Log.info("Re-connection succeeded.");
								}
							}
						}
					double2dbl = 0;
				}
					
					
				String stWeightReading3 = Double.toString(double2dbl);
					
				try
				{
					if(stWeightReading3.length() > 0)
					{
						scrWeight3.setFieldValue(stWeightReading3);
					}
					else
					{
						scrWeight3.setFieldValue("0");
					}
				}
				catch( NullPointerException npe)
				{
					bReRun = true;
				}
				
				bRunOnce = false;
			}
					
			ReadingInformation.flush(serialDeviceName3);
		}
//////////////////////////////////////////////////////////////////////
		VtiUserExitScreenField scrDevice4 = getScreenField("WEIGHDEVICE4");
		VtiUserExitScreenField scrBridge4 = getScreenField("BRIDGENAME4");
		VtiUserExitScreenField scrWeight4 = getScreenField("WEIGHT4");
		
		stableW = false;
		checkStable = true;
		stableCount = 0;
		bReRun = false;
		bRunOnce = true;
			
			
		if(scrDevice4.getFieldValue().length() > 0)
		{
			String deviceNum4 = scrDevice4.getFieldValue();
			String configSectionName4 = "DeviceGrouping:" + StringUtil.stringPad(deviceNum4,3,'0',true);
			
			Log.trace(2, "Device number after correction " + deviceNum4);

			String serialDeviceName4 = getConfigString(configSectionName4, "WeighbridgeDevice",null);
	
			scrBridge4.setFieldValue(serialDeviceName4);
			//Get the weight of the weighbridge indicated from the weighbridge selected on the screen.
			VtiExitSerialDevice serialDevice4 = getSerialDevice(serialDeviceName4);	
			
			if(serialDevice4.isConnected() == false)
			{
				Log.trace(2, "Serial device not connected, attempting re-connect");
				serialDevice4.connect();
				
				if(serialDevice4.isConnected() == false)
				{
					Log.trace(2, "Serial device failed to re-connect");	
				}
			}
					
			//boolean con = serialDevice.isConnected();
			dblWeight = new Double(0.0);

			while((bRunOnce || bReRun) && stableCount < 2)
			{
				stableCount++;
				
				bReRun = false;
				
				serialDevice4.write(startCode);
					
				if(serialDeviceName4 == null)
				{
					return new VtiUserExitResult(000,"Serial device name 4 could not be found from the configuration file.");
				}	
					
				try
				{
					dblWeight = (Double)ReadingInformation.get(serialDeviceName4);
				}
				catch(NullPointerException npe)
				{
				}

				for(int run = 0; run < 2;run++)//Added after phase 2 go-live
				{
					//ReadingInformation.flush(serialDeviceName);
					serialDevice4.write(startCode);
					
					if(serialDeviceName4 == null)
					{
						return new VtiUserExitResult(000,"Serial device name 4 could not be found from the configuration file.");
					}
							
					//dblWeight = (Double)ReadingInformation.get(serialDeviceName);
							
					try
					{
						dblWeight = (Double)ReadingInformation.get(serialDeviceName4);
					}
					catch(NullPointerException npe)
					{
						Log.error("dblWeight = (Double)ReadingInformation.get(serialDeviceName4) failed ", npe);
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
					
				Log.trace(2, "Double from com port " + serialDeviceName4 + " was " + dblWeight);	
				double2dbl = 0.0;
						
				try
				{
					double2dbl = dblWeight.doubleValue();
				}
				catch(NullPointerException pnex)
				{
					
					  bReRun = true;
					
					//scrFDispWeight.setFieldValue("0");

						if(bRunOnce)
						{
							//Start and Stop Com Connection
					
							serialDevice4.disconnect();
							
							
							try
							{
								Thread.sleep(300);
							}
							catch(InterruptedException ie)
							{
								Log.error("Sleep failed", ie);	
							}
								
							if(serialDevice4.isConnected() == false)
							{
								Log.info("Attempting to reconnect " + serialDeviceName4);
								serialDevice4.connect();
								if(serialDevice4.isConnected() == false)
								{				
									bReRun = false;
									Log.error("Re-connection failed.");
									return new VtiUserExitResult(000,"Bridge " + deviceNum4 + " failed to reconnect. Weights cannot be picked from the bridge. Call support immediatly.");
										
								}
								else if(serialDevice4.isConnected() == true)
								{
									Log.info("Re-connection succeeded.");
								}
							}
						}
					double2dbl = 0;
				}
					
					
				String stWeightReading4 = Double.toString(double2dbl);
					
				try
				{
					if(stWeightReading4.length() > 0)
					{
						scrWeight4.setFieldValue(stWeightReading4);
					}
					else
					{
						scrWeight4.setFieldValue("0");
					}
				}
				catch( NullPointerException npe)
				{
					bReRun = true;
				}
				
				bRunOnce = false;
			}
					
			ReadingInformation.flush(serialDeviceName4);
			//////////////////////////////////////////////////////////////////////
			VtiUserExitScreenField scrDevice5 = getScreenField("WEIGHDEVICE5");
			VtiUserExitScreenField scrBridge5 = getScreenField("BRIDGENAME5");
			VtiUserExitScreenField scrWeight5 = getScreenField("WEIGHT5");
		
			stableW = false;
			checkStable = true;
			stableCount = 0;
			bReRun = false;
			bRunOnce = true;
				
				
			if(scrDevice5.getFieldValue().length() > 0)
			{
				String deviceNum5 = scrDevice5.getFieldValue();
				String configSectionName5 = "DeviceGrouping:" + StringUtil.stringPad(deviceNum5,3,'0',true);
				
				Log.trace(2, "Device number after correction " + deviceNum5);

				String serialDeviceName5 = getConfigString(configSectionName5, "WeighbridgeDevice",null);
	
				scrBridge5.setFieldValue(serialDeviceName5);
				//Get the weight of the weighbridge indicated from the weighbridge selected on the screen.
				VtiExitSerialDevice serialDevice5 = getSerialDevice(serialDeviceName5);	
				
				if(serialDevice5.isConnected() == false)
				{
					Log.trace(2, "Serial device not connected, attempting re-connect");
					serialDevice5.connect();
					
					if(serialDevice5.isConnected() == false)
					{
						Log.trace(2, "Serial device failed to re-connect");	
					}
				}
						
				//boolean con = serialDevice.isConnected();
				dblWeight = new Double(0.0);

				while((bRunOnce || bReRun) && stableCount < 2)
				{
					stableCount++;
					
					bReRun = false;
					
					serialDevice5.write(startCode);
						
					if(serialDeviceName5 == null)
					{
						return new VtiUserExitResult(000,"Serial device name 5 could not be found from the configuration file.");
					}	
						
					try
					{
						dblWeight = (Double)ReadingInformation.get(serialDeviceName5);
					}
					catch(NullPointerException npe)
					{
					}

					for(int run = 0; run < 2;run++)//Added after phase 2 go-live
					{
						//ReadingInformation.flush(serialDeviceName);
						serialDevice5.write(startCode);
						
						if(serialDeviceName5 == null)
						{
							return new VtiUserExitResult(000,"Serial device name 5 could not be found from the configuration file.");
						}
								
						//dblWeight = (Double)ReadingInformation.get(serialDeviceName);
								
						try
						{
							dblWeight = (Double)ReadingInformation.get(serialDeviceName5);
						}
						catch(NullPointerException npe)
						{
							Log.error("dblWeight = (Double)ReadingInformation.get(serialDeviceName5) failed ", npe);
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
						
					Log.trace(2, "Double from com port " + serialDeviceName5 + " was " + dblWeight);	
					double2dbl = 0.0;
							
					try
					{
						double2dbl = dblWeight.doubleValue();
					}
					catch(NullPointerException pnex)
					{
					
						  bReRun = true;
						
						//scrFDispWeight.setFieldValue("0");

							if(bRunOnce)
							{
								//Start and Stop Com Connection
						
								serialDevice5.disconnect();
								
								
								try
								{
									Thread.sleep(300);
								}
								catch(InterruptedException ie)
								{
									Log.error("Sleep failed", ie);	
								}
									
								if(serialDevice5.isConnected() == false)
								{
									Log.info("Attempting to reconnect " + serialDeviceName5);
									serialDevice5.connect();
									if(serialDevice5.isConnected() == false)
									{				
										bReRun = false;
										Log.error("Re-connection failed.");
										return new VtiUserExitResult(000,"Bridge " + deviceNum5 + " failed to reconnect. Weights cannot be picked from the bridge. Call support immediatly.");
											
									}
									else if(serialDevice5.isConnected() == true)
									{
										Log.info("Re-connection succeeded.");
									}
								}
							}
						double2dbl = 0;
					}
						
						
					String stWeightReading5 = Double.toString(double2dbl);
						
					try
					{
						if(stWeightReading5.length() > 0)
						{
							scrWeight5.setFieldValue(stWeightReading5);
						}
						else
						{
							scrWeight5.setFieldValue("0");
						}
					}
					catch( NullPointerException npe)
					{
						bReRun = true;
					}
					
					bRunOnce = false;
				}
						
				ReadingInformation.flush(serialDeviceName5);
			}
		
		
		}		return new VtiUserExitResult();
	}
}
