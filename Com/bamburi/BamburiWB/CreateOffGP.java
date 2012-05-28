package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class CreateOffGP extends VtiUserExit 
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		long lngGP = 0;
		
		VtiUserExitScreenField stoNum = getScreenField("EBELN3");
		VtiUserExitScreenField gpWeight = getScreenField( "WEIGHT");
		
		if(stoNum == null) return new VtiUserExitResult(999, "Failed to initialize EBELN3");
		if(gpWeight == null) return new VtiUserExitResult(999, "Failed to initialize WEIGHT");
		
		if(gpWeight.getFieldValue().length()== 0)
			return new VtiUserExitResult(999, "Please indicate gate pass weight.");
		
		VtiExitLdbTable gatepassLdb = getLocalDatabaseTable("YSWB_GATEPASS");
		if(gatepassLdb == null) return new VtiUserExitResult(999,"Failed to open YSWB_GATEPASS");

		VtiExitLdbTable poItemsLdbTable = getLocalDatabaseTable("YSWB_PO_ITEMS");
		if (poItemsLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_PO_ITEMS.");
		
		VtiExitLdbTable configLdbTable = getLocalDatabaseTable("YSWB_CONFIG");
		if (configLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_CONFIG.");

		VtiExitLdbSelectCriterion [] serversSelConds = 
		{
			new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
				new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
					new VtiExitLdbSelectCondition("KEYFIELD", VtiExitLdbSelectCondition.EQ_OPERATOR, "SERVERS"),
						new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup serversSelCondGrp = new VtiExitLdbSelectConditionGroup(serversSelConds, true);
		VtiExitLdbTableRow[] serversLdbRows = configLdbTable.getMatchingRows(serversSelCondGrp);
				
		if(serversLdbRows.length == 0)
			return new VtiUserExitResult(999, "Servers not maintained in YSWB_CONFIG");

		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		if (sessionHeader == null) return new VtiUserExitResult(999, "Error Retrieving Session Header Info");

		VtiExitLdbSelectCriterion [] poSelConds = 
		{
			new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
				new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
					new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, stoNum.getFieldValue()),
						new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup poSelCondGrp = new VtiExitLdbSelectConditionGroup(poSelConds, true);
		VtiExitLdbTableRow[] poLdbRows = poItemsLdbTable.getMatchingRows(poSelCondGrp);
		
		if(poLdbRows.length > 0)
		{
			try
			{
				lngGP = getNextNumberFromNumberRange("YSWB_OFF_GP");
			}
			catch(VtiExitException ee)
			{
				Log.error("Error creating next gp No.",ee);
				return new VtiUserExitResult(999,"Unable to generate offline Gate pass number.");
			}
			
			 VtiExitLdbTableRow newGatePass = gatepassLdb.newRow();
			 
			 for(int i = 0; i < serversLdbRows.length; i++)
			 {
				newGatePass.setFieldValue("SERVERGRP", serversLdbRows[i].getFieldValue("KEYVAL1"));
				newGatePass.setFieldValue("SERVERID", serversLdbRows[i].getFieldValue("KEYVAL2"));
				newGatePass.setFieldValue("PASS_NUMB", lngGP);
				newGatePass.setFieldValue("EBELN", stoNum.getFieldValue());
				newGatePass.setFieldValue("MENGE", gpWeight.getIntegerFieldValue());
				newGatePass.setFieldValue("USERID", sessionHeader.getUserId());
			 
				try
				{
					 gatepassLdb.saveRow(newGatePass);
				}
				catch( VtiExitException ee)
				{
					 Log.error("Offline gate pass not created.");
					 return new VtiUserExitResult(999,"Offline gate pass not created.");
				}
			 }
			 return new VtiUserExitResult(000,1,"Gate pass, " + lngGP + " ,was created sucessfully.");
		}
		else
		{
			return new VtiUserExitResult(999,1,"This is not a valid Stock Transfer Order.");
		}
		
		//return new VtiUserExitResult();
	}
}
