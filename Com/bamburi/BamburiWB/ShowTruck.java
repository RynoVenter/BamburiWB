package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class ShowTruck extends VtiUserExit 
{
	public VtiUserExitResult execute() throws VtiExitException 
	{
		VtiUserExitScreenField scrQCallNo = getScreenField("QCALLNO");
		VtiUserExitScreenField scrDispTruck = getScreenField("DISP_TRUCK");
		VtiUserExitScreenField scrLower = getScreenField("LOWER_COUNT");
		VtiUserExitScreenField scrUpper = getScreenField("UPPER_COUNT");
		
		if(scrQCallNo == null) return new VtiUserExitResult(999,"Field QCALLNO failed to load.");
		if(scrDispTruck == null) return new VtiUserExitResult(999,"Field DISP_TRUCK failed to load.");
		if(scrLower == null) return new VtiUserExitResult(999,"Field LOWER_COUNT failed to load.");
		if(scrUpper == null) return new VtiUserExitResult(999,"Field UPPER_COUNT failed to load.");
		
		VtiUserExitScreenTable tblQ1 = getScreenTable("TB_Q1");
		VtiUserExitScreenTable tblQ2 = getScreenTable("TB_Q2");
		VtiUserExitScreenTable tblQ3 = getScreenTable("TB_Q3");
		VtiUserExitScreenTable tblQ4 = getScreenTable("TB_Q4");
		VtiUserExitScreenTable tblQ5 = getScreenTable("TB_Q5");
		
		if(tblQ1 == null) return new VtiUserExitResult(999,"Table TB_Q1 failed to load.");
		if(tblQ2 == null) return new VtiUserExitResult(999,"Table TB_Q2 failed to load.");
		if(tblQ3 == null) return new VtiUserExitResult(999,"Table TB_Q3 failed to load.");
		if(tblQ4 == null) return new VtiUserExitResult(999,"Table TB_Q4 failed to load.");
		if(tblQ5 == null) return new VtiUserExitResult(999,"Table TB_Q5 failed to load.");
		
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		if (sessionHeader == null) return new VtiUserExitResult(999, "Error Retrieving Session Header Info");
		
		VtiExitLdbTable queueLdbTable = getLocalDatabaseTable("YSWB_QUEUE");
		if(queueLdbTable == null) return new VtiUserExitResult(999,"LDB Table YSWB_QUEUE failed to load.");
		
		long qCallNo = scrQCallNo.getLongFieldValue();
		
		if(qCallNo == 0)
		{
			
			VtiExitLdbSelectCriterion [] qNoSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("Q_STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "ASSIGNED"),
							new VtiExitLdbSelectCondition("Q_QUEUE", VtiExitLdbSelectCondition.NE_OPERATOR, "RAIL"),
								new VtiExitLdbSelectCondition("Q_NUMBER", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup qNoSelCondGrp = new VtiExitLdbSelectConditionGroup(qNoSelConds, true);
			
			VtiExitLdbOrderSpecification [] orderBy = 
			{
				new VtiExitLdbOrderSpecification("Q_NUMBER",true),
			};
			
			VtiExitLdbTableRow qNoLdbRows[] = queueLdbTable.getMatchingRows(qNoSelCondGrp, orderBy);
		
			if(qNoLdbRows.length == 0)
			{
				scrDispTruck.setFieldValue("");
				scrQCallNo.setLongFieldValue(0);	
				qCallNo = 0;
			}
			else
			{
				qCallNo = qNoLdbRows[0].getLongFieldValue("Q_NUMBER");
				scrLower.setLongFieldValue(qCallNo);
				scrUpper.setLongFieldValue(qNoLdbRows[qNoLdbRows.length - 1].getLongFieldValue("Q_NUMBER"));
				scrDispTruck.setFieldValue(qNoLdbRows[0].getFieldValue("Q_REGNO"));
			}
			
			/*
			if(tblQ1.getRowCount() > 0 && tblQ1.getRow(0).getLongFieldValue("Q1_CALL") > 0 && qCallNo == 0)
			{
				qCallNo = tblQ1.getRow(0).getLongFieldValue("Q1_CALL");
				scrDispTruck.setFieldValue(tblQ1.getRow(0).getFieldValue("Q1_TRUCK"));
			}
			if(tblQ2.getRowCount() > 0 && tblQ2.getRow(0).getLongFieldValue("Q2_CALL") > 0 && qCallNo == 0)
			{
				qCallNo = tblQ2.getRow(0).getLongFieldValue("Q2_CALL");
				scrDispTruck.setFieldValue(tblQ2.getRow(0).getFieldValue("Q2_TRUCK"));
			}
			if(tblQ3.getRowCount() > 0 && tblQ3.getRow(0).getLongFieldValue("Q3_CALL") > 0 && qCallNo == 0)
			{
				qCallNo = tblQ3.getRow(0).getLongFieldValue("Q3_CALL");
				scrDispTruck.setFieldValue(tblQ3.getRow(0).getFieldValue("Q3_TRUCK"));
			}
			if(tblQ4.getRowCount() > 0 && tblQ4.getRow(0).getLongFieldValue("Q4_CALL") > 0 && qCallNo == 0)
			{
				qCallNo = tblQ4.getRow(0).getLongFieldValue("Q4_CALL");
				scrDispTruck.setFieldValue(tblQ4.getRow(0).getFieldValue("Q4_TRUCK"));
			}
			if(tblQ5.getRowCount() > 0 && tblQ5.getRow(0).getLongFieldValue("Q5_CALL") > 0 && qCallNo == 0)
			{
				qCallNo = tblQ5.getRow(0).getLongFieldValue("Q5_CALL");
				scrDispTruck.setFieldValue(tblQ5.getRow(0).getFieldValue("Q5_TRUCK"));
			}
				*/
			
			scrQCallNo.setLongFieldValue(qCallNo);	
		}
		else
		{
			boolean nQNoFound = false;
			
			while(nQNoFound == false)
			{
				qCallNo++;
				scrQCallNo.setFieldValue(qCallNo);
			
				VtiExitLdbSelectCriterion [] queueSelConds = 
				{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
							new VtiExitLdbSelectCondition("Q_NUMBER", VtiExitLdbSelectCondition.EQ_OPERATOR, Long.toString(qCallNo)),
					new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
      
				VtiExitLdbSelectConditionGroup queueSelCondGrp = new VtiExitLdbSelectConditionGroup(queueSelConds, true);
				VtiExitLdbTableRow[] queueLdbRows = queueLdbTable.getMatchingRows(queueSelCondGrp);
			
				if(queueLdbRows.length == 0)
				{
					scrDispTruck.setFieldValue("");
				
					if(qCallNo >= scrUpper.getLongFieldValue())
					{
						scrDispTruck.setFieldValue("");
						scrQCallNo.setFieldValue("");	
						qCallNo = 0;
						nQNoFound = true;
					}
				}
				else
				{
					nQNoFound = true;
					scrDispTruck.setFieldValue(queueLdbRows[0].getFieldValue("Q_REGNO"));
				}
			}
		}
		
		if(qCallNo == 0 || scrDispTruck.getFieldValue().length() == 0)
			sessionHeader.setNextFunctionId("YSWB_QUEUEDISP");
		else
			sessionHeader.setNextFunctionId("YSWB_TRUCKDISP");
		
		return new VtiUserExitResult();
	}
}