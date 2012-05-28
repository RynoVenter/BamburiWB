package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class SOSync extends VtiUserExit 
{
	public VtiUserExitResult execute() throws VtiExitException 
	{
		
		/*Get 4 values from the relevant tables and add the status value of each table into an array
		Create an array of each status found and then calculate the count of each status
		The highest status count wins*/
		
		VtiUserExitScreenField scrOrder = getScreenField("VBELN");
		VtiUserExitScreenField scrRef = getScreenField("VTIREF");
		VtiUserExitScreenField scrTruck = getScreenField("TRUCK_REG");
		
		if (scrOrder == null) return new VtiUserExitResult(999, "Unable to initialise field VBELN.");
		if (scrRef == null) return new VtiUserExitResult(999, "Unable to initialise field VTIREF.");
		if (scrTruck == null) return new VtiUserExitResult(999, "Unable to initialise field TRUCK_REG.");
		
		VtiUserExitHeaderInfo sessionInfo = getHeaderInfo();
		
		if (scrOrder.getFieldValue().length() == 0) return new VtiUserExitResult(999, "No order no found on screen.");
		if (scrRef.getFieldValue().length() == 0) return new VtiUserExitResult(999, "No VTI reference number found on screen.");
		if (scrTruck.getFieldValue().length() == 0) return new VtiUserExitResult(999, "No truck details on screen.");
		
		VtiExitLdbTable queueLdbTable = getLocalDatabaseTable("YSWB_QUEUE");
		VtiExitLdbTable regLdbTable = getLocalDatabaseTable("YSWB_REGISTER");
		VtiExitLdbTable soHLdbTable = getLocalDatabaseTable("YSWB_SO_HEADER");
		VtiExitLdbTable wbLdbTable = getLocalDatabaseTable("YSWB_WB");
		
		if (queueLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_QUEUE.");
		if (regLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_REGISTER.");
		if (soHLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_SO_HEADER.");
		if (wbLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_WB.");
		
		DBCalls dbCall = new DBCalls();
		String tblArray[][] = new String [2][4];
		String tblCount[][] = new String [2][4];
		String tableRef = "";
		
		//SO Header status get
		VtiExitLdbSelectCriterion [] soHSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrOrder.getFieldValue()),
							new VtiExitLdbSelectCondition("TRUCK", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruck.getFieldValue()),
								new VtiExitLdbSelectCondition("VTIREF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRef.getFieldValue()),			
		};
      
		VtiExitLdbSelectConditionGroup soHSelCondGrp = new VtiExitLdbSelectConditionGroup(soHSelConds, true);
		VtiExitLdbTableRow[] soHLdbRows = soHLdbTable.getMatchingRows(soHSelCondGrp);

		if(soHLdbRows.length == 0)
			return new VtiUserExitResult(999, "No sales order matching the screen criteria match the SO Header. Requires admin correction.");
		
		tblArray[0][0] = "soHLdbRows";
		tblArray[1][0] = soHLdbRows[0].getFieldValue("STATUS");
		
		
		//WB status get
		VtiExitLdbSelectCriterion [] wbSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrOrder.getFieldValue()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruck.getFieldValue()),
								new VtiExitLdbSelectCondition("VTIREFA", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRef.getFieldValue()),			
		};
      
		VtiExitLdbSelectConditionGroup wbSelCondGrp = new VtiExitLdbSelectConditionGroup(wbSelConds, true);
		VtiExitLdbTableRow[] wbLdbRows = wbLdbTable.getMatchingRows(wbSelCondGrp);

		if(wbLdbRows.length == 0)
			return new VtiUserExitResult(999, "Weigh bridge history not found with the criteria from the screen. Requires admin correction.");
		
		tblArray[0][2] = "wbLdbRows";
		tblArray[1][2] = wbLdbRows[0].getFieldValue("STATUS");
		
		//Register status get
		String regStatus = "";
		String regGetStatus = "";
		VtiExitLdbSelectCriterion [] regSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrOrder.getFieldValue()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruck.getFieldValue()),
								new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRef.getFieldValue()),			
		};
      
		VtiExitLdbSelectConditionGroup regSelCondGrp = new VtiExitLdbSelectConditionGroup(regSelConds, true);
		VtiExitLdbTableRow[] regLdbRows = regLdbTable.getMatchingRows(regSelCondGrp);

		if(regLdbRows.length == 0)
			return new VtiUserExitResult(999, "Registration not found with the criteria from the screen. Requires admin correction.");
		
		tblArray[0][1] = "regLdbRows";
		regGetStatus = regLdbRows[0].getFieldValue("INSPSTATUS");		
		
		if(regGetStatus.equalsIgnoreCase("W"))
			regStatus = "WEIGH 1";
		
		if(regGetStatus.equalsIgnoreCase("W") && wbLdbRows[0].getLongFieldValue("WEIGHT2") > 0)
			regStatus = "WEIGH 2";
			
		if(regGetStatus.equalsIgnoreCase("A"))
			regStatus = "REJECTED";
			
		if(regGetStatus.equalsIgnoreCase("C"))
			regStatus = "COMPLETE";
		
		tblArray[1][1] = regStatus;
		
		//Q status get
		VtiExitLdbSelectCriterion [] qSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("Q_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrOrder.getFieldValue()),
							new VtiExitLdbSelectCondition("Q_REGNO", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruck.getFieldValue()),
		};
      
		VtiExitLdbSelectConditionGroup qSelCondGrp = new VtiExitLdbSelectConditionGroup(qSelConds, true);
		VtiExitLdbTableRow[] qLdbRows = queueLdbTable.getMatchingRows(qSelCondGrp);

		if(qLdbRows.length == 0)
			return new VtiUserExitResult(999, "Queue not found with the criteria from the screen. Requires admin correction.");
		
		tblArray[0][3] = "qLdbRows";
		tblArray[1][3] = qLdbRows[0].getFieldValue("Q_STATUS");
		
		
		//Create status count | Loop through tblArray and update tblCount
		
		String arrayStatus = "";
		int na = -1;
		for(int i = 0; i < 4; i++)
		{
			arrayStatus = tblArray[1][i];
			
			for(int a = 0; a < 4 ; a++)
			{
				
				if(tblArray[1][i].equalsIgnoreCase(tblCount[0][a]))
				{
					if(tblCount[1][a] == null)
					{
						tblCount[1][a] = "0";
					}
					tblCount[1][a] = Long.toString((Long.parseLong(tblCount[1][a]))+1);
					break;
				}
				else
				{
					na++;
					tblCount[0][a+na] = tblArray[1][i];
					tblCount[1][a+na] = "1";
					break;
				}
				
			}
		}	
		
		//Get highest status count and set status
		long high = 0;
		String status = "";
		for(int c = 0;c < tblCount[0].length; c++)
		{
			if(tblCount[1][c] != null && Long.parseLong(tblCount[1][c]) > high)
			{
				high = Long.parseLong(tblCount[1][c]);
				status = tblCount[0][c];			
			}
		}
		
		if(high == 2)
			return new VtiUserExitResult(999,"Multiple records are out of sync and requires admin to correct the conflict.");
		if(high == 4)
			return new VtiUserExitResult(999,"Data not out of sync.");
		if(status.equalsIgnoreCase("Complete"))
		   return new VtiUserExitResult(999,"Data sync not allowable on a completed truck.");
		
		//Get tables out of sync and update with correct status
		String table2Correct = "";
		for(int e = 0;e < 4; e++)
		{
			if(!tblArray[1][e].equalsIgnoreCase(status))
			{
				table2Correct = tblArray[0][e];
			}
		}
		
		//Correct table out of sync
		if(table2Correct.equalsIgnoreCase("soHLdbRows"))
		{
			soHLdbRows[0].setFieldValue("STATUS",status);
			
			try
			{
				soHLdbTable.saveRow(soHLdbRows[0]);
			}
			catch (VtiExitException ee)
			{
				Log.error("Unable to Save data to the SO HEADER.",ee);
				return new VtiUserExitResult(999,"Unable to Save data to the sales order header table.");
			}
		}
		
		String rStatus = "";
		
		if(table2Correct.equalsIgnoreCase("regLdbRows"))
		{
			if(status.equalsIgnoreCase("WEIGH 1"))
				rStatus = "W";
			
			if(status.equalsIgnoreCase("WEIGH 2"))
				rStatus = "W";
			
			if(status.equalsIgnoreCase("REJECTED"))
				rStatus = "A";
			
			if(status.equalsIgnoreCase("COMPLETE"))
				rStatus = "C";
			
			regLdbRows[0].setFieldValue("INSPSTATUS",rStatus);
			
			try
			{
				regLdbTable.saveRow(regLdbRows[0]);
			}
			catch (VtiExitException ee)
			{
				Log.error("Unable to Save data to the registration.",ee);
				return new VtiUserExitResult(999,"Unable to Save data to the registration table.");
			}

		}
		
		if(table2Correct.equalsIgnoreCase("wbLdbRows"))
		{
			wbLdbRows[0].setFieldValue("STATUS",status);
			
			try
			{
				wbLdbTable.saveRow(wbLdbRows[0]);
			}
			catch (VtiExitException ee)
			{
				Log.error("Unable to Save data to the WB.",ee);
				return new VtiUserExitResult(999,"Unable to Save data to the WB table.");
			}

		}
		
		if(table2Correct.equalsIgnoreCase("qLdbRows"))
		{
			qLdbRows[0].setFieldValue("Q_STATUS",status);
			
			try
			{
				queueLdbTable.saveRow(qLdbRows[0]);
			}
			catch (VtiExitException ee)
			{
				Log.error("Unable to Save data to the Q Table.",ee);
				return new VtiUserExitResult(999,"Unable to Save data to the Queue table.");
			}

		}

		sessionInfo.setNextFunctionId("YSWB_TROUTBOUND");
		
		return new VtiUserExitResult();
	}
}
