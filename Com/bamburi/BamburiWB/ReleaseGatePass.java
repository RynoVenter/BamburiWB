package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;


public class ReleaseGatePass extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		StringBuffer sbErrorMsg = new StringBuffer();
		
		VtiUserExitScreenField scrGPNo = getScreenField("GP_NO");
		VtiUserExitScreenField scrOrdNo = getScreenField("ORDERNO");
		VtiUserExitScreenField scrRegNo = getScreenField("REGNO");
		VtiUserExitScreenField scrRefNo = getScreenField("REFNO");
		
		if(scrGPNo.getFieldValue().length() == 0)
			return new VtiUserExitResult(999,1,"Please indicate which gate pass to release.");
		
		if(scrOrdNo.getFieldValue().length() == 0)
			return new VtiUserExitResult(999,1,"Please indicate the order number by searching for the gate pass.");
		
		if(scrRefNo.getFieldValue().length() == 0)
			return new VtiUserExitResult(999,1,"Please indicate the VTI reference by searching for the gate pass.");
		
		VtiExitLdbTable gatePassLdbTable = getLocalDatabaseTable("YSWB_GATEPASS");
		VtiExitLdbTable registerLdbTable = getLocalDatabaseTable("YSWB_REGISTER");
		
		//Gate Pass search
		VtiExitLdbSelectCriterion [] gpSelConds = 
		{
			new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
				new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
					new VtiExitLdbSelectCondition("PASS_NUMB", VtiExitLdbSelectCondition.EQ_OPERATOR, scrGPNo.getFieldValue()),
						new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrOrdNo.getFieldValue()),
			new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup gpSelCondGrp = new VtiExitLdbSelectConditionGroup(gpSelConds, true);
		VtiExitLdbTableRow[] gpLdbRows = gatePassLdbTable.getMatchingRows(gpSelCondGrp);
		
		if(gpLdbRows.length > 0)
		{
			gpLdbRows[0].setFieldValue("STATUS","");
			gpLdbRows[0].setFieldValue("TIMESTAMP","");
			gpLdbRows[0].setFieldValue("VTIREF","");
			
			try
			{
				gatePassLdbTable.saveRow(gpLdbRows[0]);
			}
			catch(VtiExitException ee)
			{
				return new VtiUserExitResult(999,1,"Failed to save status to Gate Pass.");
			}
			sbErrorMsg.append("Gatepass released, and usable.");
		}
		else
			sbErrorMsg.append("Gate pass " + scrGPNo.getFieldValue() + " for order number " + scrOrdNo.getFieldValue() + " was not found.");
			
		//Register search
		VtiExitLdbSelectCriterion [] regSelConds = 
		{
			new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
				new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
					//new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
						new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRefNo.getFieldValue()),
			new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup regSelCondGrp = new VtiExitLdbSelectConditionGroup(regSelConds, true);
		VtiExitLdbTableRow[] regLdbRows = registerLdbTable.getMatchingRows(regSelCondGrp);
		
		if(regLdbRows.length > 0)
		{
			regLdbRows[0].setFieldValue("GATE_PASS","");
			regLdbRows[0].setFieldValue("TIMESTAMP","");
			
			try
			{
				registerLdbTable.saveRow(regLdbRows[0]);
			}
			catch(VtiExitException ee)
			{
				return new VtiUserExitResult(999,1,"Failed to update register with released gate pass.");
			}
		}
		else
			sbErrorMsg.append("Gate pass " + scrGPNo.getFieldValue() + "was not released from truck " + scrRegNo.getFieldValue()+ ".");
						
		if(sbErrorMsg.length() > 0)
			return new VtiUserExitResult(000,1, sbErrorMsg.toString());
		else
			return new VtiUserExitResult();
		
		
	}
}
