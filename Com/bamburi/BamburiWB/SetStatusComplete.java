package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;


public class SetStatusComplete extends VtiUserExit
{	
	public VtiUserExitResult execute() throws VtiExitException
	{
		VtiUserExitScreenField scrRegNo = getScreenField("TRUCKREG");
		VtiUserExitScreenField scrVti = getScreenField("VTI");
		VtiUserExitScreenField scrOrderNum = getScreenField("ORD_NUM");
		VtiUserExitScreenField scrCurStatus = getScreenField("CURSTATUS");
		
		VtiExitLdbTable registerLdbTable = getLocalDatabaseTable("YSWB_REGISTER");

		
		VtiExitLdbSelectCriterion [] regSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("ORD_NUM", VtiExitLdbSelectCondition.EQ_OPERATOR, scrOrderNum.getFieldValue()),
							new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVti.getFieldValue()),
								new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
									new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "C"),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				
		};
		
      
		VtiExitLdbSelectConditionGroup regSelCondGrp = new VtiExitLdbSelectConditionGroup(regSelConds, true);
		VtiExitLdbTableRow[] regLdbRows = registerLdbTable.getMatchingRows(regSelCondGrp);

			
		if(regLdbRows.length > 0)	
		{
			scrCurStatus.setFieldValue("Complete");
		}
		return new VtiUserExitResult();
	}
}
