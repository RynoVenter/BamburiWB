package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;


public class WeighAuth extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		//Declarations of variables and elements. Followed by the checking of the elements.
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		if (sessionHeader == null) return new VtiUserExitResult(999, "Error Retrieving Session Header Info");

		VtiUserExitScreenField userIDField = getScreenField("USERID");
		VtiUserExitScreenField passwIDField = getScreenField("PASSWORD");

		
		if (userIDField == null) return new VtiUserExitResult(999, "Screen field USERID does not exist");
		if (passwIDField == null) return new VtiUserExitResult(999, "Screen field PASSWORD does not exist");
		
		String validate = "";
		String wghOver = "";
		
		boolean runValidate = false;
	
		//Validate and update password details.
		//Logon Password dataset
		VtiExitLdbTable logonlLdbTable = getLocalDatabaseTable("YSWB_LOGON");
		if (logonlLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_LOGON.");
		
		VtiExitLdbSelectCriterion [] logonSelConds = 
				{
						new VtiExitLdbSelectCondition("USERID", VtiExitLdbSelectCondition.EQ_OPERATOR, userIDField.getFieldValue()),
						new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
        
		VtiExitLdbSelectConditionGroup logonSelCondGrp = new VtiExitLdbSelectConditionGroup(logonSelConds, true);
		VtiExitLdbTableRow[] logonLdbRows = logonlLdbTable.getMatchingRows(logonSelCondGrp);

		if(logonLdbRows.length == 0) return new VtiUserExitResult(999, "Unable to query table YSWB_LOGON.");
		
		validate = logonLdbRows[0].getFieldValue("PASSWORD");
		wghOver = logonLdbRows[0].getFieldValue("AUTHLEVEL");
		
		if(passwIDField.getFieldValue().equals(validate) && wghOver.equalsIgnoreCase("WGHOVER"))
			sessionHeader.setNextFunctionId("YSWB_WEIGH_ND");
		else
			return new VtiUserExitResult(999,"You are not authorised to overide the weigh-in.");
			
		return new VtiUserExitResult();
	}
}
