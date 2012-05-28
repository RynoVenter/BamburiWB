package com.bamburi.bamburirail;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;


public class FormatMain extends VtiUserExit
{
	public String serialDeviceName = null;
	
	public VtiUserExitResult execute() throws VtiExitException
	{
		
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		if (sessionHeader == null) return new VtiUserExitResult(999, "Error Retrieving Session Header Info");
		
		int app = sessionHeader.getApplicationNumber();
		
		if(app == 1)
		{
			//Declarations of variables and elements. Followed by the checking of the elements.
		
			VtiUserExitScreenField scrTruck = getScreenField("TRUCK_REG");
			VtiUserExitScreenField scrDate = getScreenField("MDATE");
			VtiUserExitScreenField btnCon = getScreenField("BT_CONNECT");
		
			if(scrTruck == null) return new VtiUserExitResult(999, "TRUCK_REG not found");
			if(scrDate == null) return new VtiUserExitResult(999, "MDATE not found");
			if(btnCon == null) return new VtiUserExitResult(999, "BT_CONNECT not found");
		
			//Declare DB Tables
			VtiExitLdbTable statusLdb = getLocalDatabaseTable("YSWB_STATUS");
			if(statusLdb == null) return new VtiUserExitResult(999,"Status table not opened.");
		
			VtiExitLdbTable registerLdb = getLocalDatabaseTable("YSWB_REGISTER");
			if(registerLdb == null) return new VtiUserExitResult(999,"Register table not opened.");
			//Variables
			String poNum = "";
			String stoNum = "";
			String icNum = "";
			String doctype = "";
				
			//Get the device to be used to do the weighin
				VtiUserExitHeaderInfo headerInfo = getHeaderInfo();
				int deviceNumber = headerInfo.getDeviceNumber();
				
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
						scrConStat.setFieldValue("Off");
						btnCon.setFieldValue("Connect Com");
					}
					else if(serialDevice.isConnected() == true)
					{
						scrConStat.setFieldValue("On");
						btnCon.setFieldValue("Disconnect Com");
					}
				}
								
			scrTruck.clearPossibleValues();
			
			if(scrDate.getFieldValue().length() != 0)
			{
				VtiExitLdbSelectCriterion [] registerSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("AUDAT", VtiExitLdbSelectCondition.EQ_OPERATOR, scrDate.getFieldValue()),
								new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
									new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
										new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "R"),
										new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "F"),
										new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "C"),
										new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "P"),
										new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "O"),
										new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "D"),
										new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
				};
      
				VtiExitLdbSelectConditionGroup registerSelCondGrp = new VtiExitLdbSelectConditionGroup(registerSelConds, true);
				VtiExitLdbTableRow[] registerLdbRows = registerLdb.getMatchingRows(registerSelCondGrp);
				
				if(registerLdbRows.length != 0)
				{
					for(int cmb = 0; cmb < registerLdbRows.length;cmb++)
					{
						scrTruck.addPossibleValue(registerLdbRows[cmb].getFieldValue("TRUCKREG") +" : S " + registerLdbRows[cmb].getFieldValue("VBELN"));
					}
				}
			}
			else
			{
				VtiExitLdbSelectCriterion [] registerSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
								new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
									new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "R"),
									new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "F"),
									new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "C"),
									new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "P"),
									new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "O"),
									new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "D"),
									new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
				};
      
				VtiExitLdbSelectConditionGroup registerSelCondGrp = new VtiExitLdbSelectConditionGroup(registerSelConds, true);
				VtiExitLdbTableRow[] registerLdbRows = registerLdb.getMatchingRows(registerSelCondGrp);
				
				if(registerLdbRows.length != 0)
				{
					for(int cmb = 0; cmb < registerLdbRows.length;cmb++)
					{
							scrTruck.addPossibleValue(registerLdbRows[cmb].getFieldValue("TRUCKREG") +" : S  " + registerLdbRows[cmb].getFieldValue("VBELN"));
					}
				}
			}
			
			if(scrDate.getFieldValue().length() != 0)
			{
				VtiExitLdbSelectCriterion [] statusSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("ARR_DATE", VtiExitLdbSelectCondition.EQ_OPERATOR, scrDate.getFieldValue()),
								new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
									new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "R"),
									new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "F"),
									new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "C"),
									new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "P"),
									new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "O"),
									new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "D"),
									new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
				};
      
				VtiExitLdbSelectConditionGroup statusSelCondGrp = new VtiExitLdbSelectConditionGroup(statusSelConds, true);
				VtiExitLdbTableRow[] statusLdbRows = statusLdb.getMatchingRows(statusSelCondGrp);
				
				
				if(statusLdbRows.length != 0)
				{
					for(int cmb = 0; cmb < statusLdbRows.length;cmb++)
					{
						poNum = statusLdbRows[cmb].getFieldValue("EBELN");
						stoNum = statusLdbRows[cmb].getFieldValue("STOCKTRNF");
						icNum = statusLdbRows[cmb].getFieldValue("DELIVDOC");
				
						if(poNum.length() > 0)
							scrTruck.addPossibleValue(statusLdbRows[cmb].getFieldValue("TRUCKREG") +" : PO " + poNum);
						if(stoNum.length() > 0)
							scrTruck.addPossibleValue(statusLdbRows[cmb].getFieldValue("TRUCKREG") +" : ST " + stoNum);
						if(icNum.length() > 0)
							scrTruck.addPossibleValue(statusLdbRows[cmb].getFieldValue("TRUCKREG") +" : IC " + icNum);
					}	
				}
			}
			else
			{
				VtiExitLdbSelectCriterion [] statusSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
									new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "R"),
									new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "F"),
									new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "C"),
									new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "P"),
									new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "O"),
									new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
									new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "D"),
				};
      
				VtiExitLdbSelectConditionGroup statusSelCondGrp = new VtiExitLdbSelectConditionGroup(statusSelConds, true);
				VtiExitLdbTableRow[] statusLdbRows = statusLdb.getMatchingRows(statusSelCondGrp);
				
				
				if(statusLdbRows.length != 0)
				{
					for(int cmb = 0; cmb < statusLdbRows.length;cmb++)
					{
						poNum = statusLdbRows[cmb].getFieldValue("EBELN");
						stoNum = statusLdbRows[cmb].getFieldValue("STOCKTRNF");
						icNum = statusLdbRows[cmb].getFieldValue("DELIVDOC");
						
						doctype = statusLdbRows[cmb].getFieldValue("DOCTYPE");
				
						if(poNum.length() > 0 && doctype.equalsIgnoreCase("NB"))
							scrTruck.addPossibleValue(statusLdbRows[cmb].getFieldValue("TRUCKREG") +" : PO " + poNum);
						if(stoNum.length() > 0 && doctype.equalsIgnoreCase("UB"))
							scrTruck.addPossibleValue(statusLdbRows[cmb].getFieldValue("TRUCKREG") +" : ST " + stoNum);
						if(icNum.length() > 0 && doctype.equalsIgnoreCase("ZIC"))
							scrTruck.addPossibleValue(statusLdbRows[cmb].getFieldValue("TRUCKREG") +" : IC " + icNum);
					}	
				}
			}
		
			if(scrDate.getFieldValue().length() != 0)
			{
				VtiExitLdbSelectCriterion [] ndSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("AUDAT", VtiExitLdbSelectCondition.EQ_OPERATOR, scrDate.getFieldValue()),
								new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, ""),
								new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, ""),
								new VtiExitLdbSelectCondition("STOCKTRNF", VtiExitLdbSelectCondition.EQ_OPERATOR, ""),
								new VtiExitLdbSelectCondition("DELIVDOC", VtiExitLdbSelectCondition.EQ_OPERATOR, ""),
									new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "R"),
									new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "F"),
									new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "C"),
									new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
				};
      
				VtiExitLdbSelectConditionGroup ndSelCondGrp = new VtiExitLdbSelectConditionGroup(ndSelConds, true);
				VtiExitLdbTableRow[] ndLdbRows = registerLdb.getMatchingRows(ndSelCondGrp);
				
				if(ndLdbRows.length != 0)
				{
					for(int cmb = 0; cmb < ndLdbRows.length;cmb++)
					{
						scrTruck.addPossibleValue(ndLdbRows[cmb].getFieldValue("TRUCKREG"));
					}
				}
			}
		}

		return new VtiUserExitResult();
	}

}
