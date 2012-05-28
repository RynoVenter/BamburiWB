package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;


public class AssignGatePass extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		StringBuffer sbErrorMsg = new StringBuffer();
		
		VtiUserExitScreenField scrGPNo = getScreenField("GP_NO");
		VtiUserExitScreenField scrOrdNo = getScreenField("ORDERNO");
		VtiUserExitScreenField scrRegNo = getScreenField("TRUCKREG");
		VtiUserExitScreenField scrRefNo = getScreenField("REFNO");
		
		VtiExitLdbTable gatePassLdbTable = getLocalDatabaseTable("YSWB_GATEPASS");
		VtiExitLdbTable registerLdbTable = getLocalDatabaseTable("YSWB_REGISTER");
		
		//Gate Pass search
		VtiExitLdbSelectCriterion [] gpSelConds = 
		{
			new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
				new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
					new VtiExitLdbSelectCondition("PASS_NUMB", VtiExitLdbSelectCondition.EQ_OPERATOR, scrGPNo.getFieldValue()),
						//new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrOrdNo.getFieldValue()),
			new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup gpSelCondGrp = new VtiExitLdbSelectConditionGroup(gpSelConds, true);
		VtiExitLdbTableRow[] gpLdbRows = gatePassLdbTable.getMatchingRows(gpSelCondGrp);
		
		if(gpLdbRows.length > 0)
		{
			gpLdbRows[0].setFieldValue("STATUS","R");
			gpLdbRows[0].setFieldValue("TIMESTAMP","");
			gpLdbRows[0].setFieldValue("VTIREF",scrRefNo.getFieldValue());
			
			try
			{
				gatePassLdbTable.saveRow(gpLdbRows[0]);
			}
			catch(VtiExitException ee)
			{
				return new VtiUserExitResult(999,1,"Failed to save status to Gate Pass.");
			}
			
		}
		else
			sbErrorMsg.append("Gate pass " + scrGPNo.getFieldValue() + " for order number " + scrOrdNo.getFieldValue() + " was not found.");
			
		//Register search
		VtiExitLdbSelectCriterion [] regSelConds = 
		{
			new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
				new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
					new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
						new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRefNo.getFieldValue()),
			new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup regSelCondGrp = new VtiExitLdbSelectConditionGroup(regSelConds, true);
		VtiExitLdbTableRow[] regLdbRows = registerLdbTable.getMatchingRows(regSelCondGrp);
		
		if(regLdbRows.length > 0)
		{
			regLdbRows[0].setFieldValue("GATE_PASS",scrGPNo.getFieldValue());
			regLdbRows[0].setFieldValue("TIMESTAMP","");
			
			try
			{
				registerLdbTable.saveRow(regLdbRows[0]);
			}
			catch(VtiExitException ee)
			{
				return new VtiUserExitResult(999,1,"Failed to update register with released gate pass.");
			}
			
			sbErrorMsg.append("Gatepass assigned.");
		}
		else
			sbErrorMsg.append("Gate pass " + scrGPNo.getFieldValue() + "was not assigned to truck " + scrRegNo.getFieldValue()+ ".");
						
		if(sbErrorMsg.length() > 0)
			return new VtiUserExitResult(000,1, sbErrorMsg.toString());
		else
			return new VtiUserExitResult();
		
		
	}
}
