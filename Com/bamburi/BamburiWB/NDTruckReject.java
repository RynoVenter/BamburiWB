package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class NDTruckReject extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		//Declarations of variables and elements. Followed by the checking of the elements.
		Date currNow = new Date();
		String currDate = DateFormatter.format("dd/MM/yyyy", currNow);
		String currTime = DateFormatter.format("HH:mm:ss", currNow);
		String currLdbDate = DateFormatter.format("ddMMyyyy", currNow);
		String currLDBTime = DateFormatter.format("HHmmss", currNow);
		
		VtiUserExitScreenField scrWFRejReason = getScreenField("REJ_REASON");
		VtiUserExitScreenField scrVRef = getScreenField("VTIREF");
		VtiUserExitScreenField scrRegNo = getScreenField("REGNO");
		VtiUserExitScreenField scrSlipNo = getScreenField("VTI_REF");
		VtiUserExitScreenField scrFWeight1 = getScreenField("WEIGHT1");
		VtiUserExitScreenField scrFWeight2 = getScreenField("WEIGHT2");
		VtiUserExitScreenField scrFNettW = getScreenField("NETT_WEIGHT");
		VtiUserExitScreenField scrMatDisc = getScreenField("ARKTX");
		
		if(scrWFRejReason == null) return new VtiUserExitResult (999,"Failed to initialise REJ_REASON.");
		if(scrVRef == null) return new VtiUserExitResult (999,"Failed to initialise REFNO.");
		if(scrRegNo == null) return new VtiUserExitResult (999,"Failed to initialise REGNO.");

		//Database TBL Declaration
		VtiExitLdbTable regRejLdbTable = getLocalDatabaseTable("YSWB_REGISTER");
		VtiExitLdbTable wbTWRejLdbTable = getLocalDatabaseTable("YSWB_WB");
		VtiExitLdbTable queueLdbTable = getLocalDatabaseTable("YSWB_QUEUE");
		VtiExitLdbTable statusLdbTable = getLocalDatabaseTable("YSWB_STATUS");
		
		if (wbTWRejLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_WB.");
		if (regRejLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_REGISTER.");
		if (queueLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_QUEUE.");
		if (statusLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_status.");
		
		int tr = 0; 
		
		VtiUserExitScreenTable scrTblCustom = getScreenTable("TB_CUSTOM");
		if(scrTblCustom == null) return new VtiUserExitResult (999,"Failed to initialise TB_CUSTOM.");

		VtiUserExitScreenTableRow wghbr = scrTblCustom.getRow(tr);
		VtiUserExitScreenTableRow trckReg = scrTblCustom.getRow(tr + 1);
		VtiUserExitScreenTableRow driv = scrTblCustom.getRow(tr + 2);
		VtiUserExitScreenTableRow custsupp = scrTblCustom.getRow(tr + 3);
		VtiUserExitScreenTableRow transprtr = scrTblCustom.getRow(tr + 4);
		VtiUserExitScreenTableRow tranType = scrTblCustom.getRow(tr + 5);
		VtiUserExitScreenTableRow allwgh = scrTblCustom.getRow(tr + 6);
		VtiUserExitScreenTableRow ordNo = scrTblCustom.getRow(tr + 7);
		VtiUserExitScreenTableRow rebag = scrTblCustom.getRow(tr + 8);
		VtiUserExitScreenTableRow packline = scrTblCustom.getRow(tr + 9);
		VtiUserExitScreenTableRow packLoad = scrTblCustom.getRow(tr + 10);
		VtiUserExitScreenTableRow segtype = scrTblCustom.getRow(tr + 11);
		VtiUserExitScreenTableRow shift = scrTblCustom.getRow(tr + 12);
		VtiUserExitScreenTableRow remarks = scrTblCustom.getRow(tr + 13);
		VtiUserExitScreenTableRow tralnum = scrTblCustom.getRow(tr + 14);
		
		String truck = trckReg.getFieldValue("FIELDVALUE");
		String product = "";
		String type = tranType.getFieldValue("FIELDVALUE");
		double allocWght = allwgh.getDoubleFieldValue("FIELDVALUE");
		String pckline = packline.getFieldValue("FIELDVALUE");
		String user = "";
		String driver = driv.getFieldValue("FIELDVALUE");
		String pshift = shift.getFieldValue("FIELDVALUE");
		String rejTime = currTime;
		double wgh1 = scrFWeight1.getDoubleFieldValue();
		double wgh2 = scrFWeight2.getDoubleFieldValue();
		double nett = scrFNettW.getDoubleFieldValue();
		String aWgh = allwgh.getFieldValue("FIELDVALUE");
		String errorMsg = "Rejection successfull";
		
		StringBuffer rejSlip = new StringBuffer();
		StringBuffer Header= new StringBuffer();
		StringBuffer addDet= new StringBuffer();

		if(scrWFRejReason.getFieldValue().length() == 0)
			return new VtiUserExitResult(999, "Please select a rejection reason.");
		//Set next Function
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		
		if (sessionHeader == null) return new VtiUserExitResult(999, "Error Retrieving Session Header Info");
		
			sessionHeader.setNextFunctionId("YSWB_MAIN");

		//Dataset Declaration
		VtiExitLdbSelectCriterion [] wbSelConds = 
		{
			new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
				new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
					new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
						new VtiExitLdbSelectCondition("VTIREFA", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
						new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
			
		VtiExitLdbSelectConditionGroup wbSelCondGrp = new VtiExitLdbSelectConditionGroup(wbSelConds, true);
		VtiExitLdbTableRow[] wbLdbRows = wbTWRejLdbTable.getMatchingRows(wbSelCondGrp);
		
		if(wbLdbRows.length == 0)
			return new VtiUserExitResult(999, "No vehicle weigh-in found.");
			
			
		String poStatus = wbLdbRows[0].getFieldValue("STATUS");
		
		if(poStatus.equalsIgnoreCase("Failed") || poStatus.equalsIgnoreCase("Complete") 
				||poStatus.equalsIgnoreCase("New")  || poStatus.substring(0,1).equalsIgnoreCase("0"))
						return new VtiUserExitResult(999,"No processing allowed, check status.");
		
		//Update Register. Change back to Assigned status
		
		VtiExitLdbSelectCriterion [] regRejSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
							new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup regRejSelCondGrp = new VtiExitLdbSelectConditionGroup(regRejSelConds, true);
		VtiExitLdbTableRow[] regRejLdbRows = regRejLdbTable.getMatchingRows(regRejSelCondGrp);

		if(regRejLdbRows.length == 0)
			return new VtiUserExitResult(999, "No order detail exist in the Status table.");
		
		regRejLdbRows[0].setFieldValue("INSPSTATUS","A");
		regRejLdbRows[0].setFieldValue("TIMESTAMP","");
		
		VtiExitLdbSelectCriterion [] wbTWRejSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "REJECTED"),
							new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "COMPLETE"),
								new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
									new VtiExitLdbSelectCondition("VTIREFA", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup wbTWRejSelCondGrp = new VtiExitLdbSelectConditionGroup(wbTWRejSelConds, true);
		VtiExitLdbTableRow[] wbTWRejLdbRows = wbTWRejLdbTable.getMatchingRows(wbTWRejSelCondGrp);

		if(wbTWRejLdbRows.length == 0)
			return new VtiUserExitResult(999, "Rejection not possible. Process already rejected or complete.");
		
		wbTWRejLdbRows[0].setFieldValue("STATUS","Rejected");
		wbTWRejLdbRows[0].setFieldValue("REJFLAG","X");
		wbTWRejLdbRows[0].setFieldValue("REJRSN",scrWFRejReason.getFieldValue());
		wbTWRejLdbRows[0].setFieldValue("TIMESTAMP","");
		
		
		VtiExitLdbSelectCriterion [] statusRejSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "REJECTED"),
							new VtiExitLdbSelectCondition("WGH_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "COMPLETE"),
								new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
									new VtiExitLdbSelectCondition("VTIREF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup statusRejSelCondGrp = new VtiExitLdbSelectConditionGroup(statusRejSelConds, true);
		VtiExitLdbTableRow[] statusRejLdbRows = statusLdbTable.getMatchingRows(statusRejSelCondGrp);

		if(statusRejLdbRows.length == 0)
			return new VtiUserExitResult(999, "Rejection not possible. Process already rejected or complete.");
		
		statusRejLdbRows[0].setFieldValue("WGH_STATUS","Rejected");
		statusRejLdbRows[0].setFieldValue("STATUS","A");
		statusRejLdbRows[0].setFieldValue("TIMESTAMP","");
		
		try
		{
			wbTWRejLdbTable.saveRow(wbTWRejLdbRows[0]);
			statusLdbTable.saveRow(statusRejLdbRows[0]);
			regRejLdbTable.saveRow(regRejLdbRows[0]);
		}
		catch (VtiExitException ee)
		{
			Log.error("Unable to reject this order.",ee);
			return new VtiUserExitResult(999,"Unable to reject this order.");
		}
		
			VtiExitLdbSelectCriterion [] qSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("Q_REGNO", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
								new VtiExitLdbSelectCondition("Q_STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "Complete"),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup qSelCondGrp = new VtiExitLdbSelectConditionGroup(qSelConds, true);
			VtiExitLdbTableRow[] qTLdbRows = queueLdbTable.getMatchingRows(qSelCondGrp);
			
			if(qTLdbRows.length > 0)
			{			
				qTLdbRows[0].setFieldValue("Q_STATUS","Rejected");
				qTLdbRows[0].setFieldValue("TIMESTAMP", "");
			
				try
				{
					queueLdbTable.saveRow(qTLdbRows[0]);
				}
				catch (VtiExitException ee)
				{
					Log.error("Error updating queue status, please correct in the queue.",ee);
					return new VtiUserExitResult(000,"Unable to update the queue.");
				}
			}
			
			Header.append("NAIROBI GRINDING PLANT");
			addDet.append("P.O Box 524, ATHI RIVER");
		
			
			VtiExitKeyValuePair[] keyValuePairs = 
			{
				new VtiExitKeyValuePair("&Header&", Header.toString()),
				new VtiExitKeyValuePair("&addDet&", addDet.toString()),
				new VtiExitKeyValuePair("&soTime&", rejTime),
				new VtiExitKeyValuePair("&truck&", truck),
				new VtiExitKeyValuePair("&pType&", type),
				new VtiExitKeyValuePair("&allocWght&", Double.toString(allocWght)),
				new VtiExitKeyValuePair("&product&", scrMatDisc.getFieldValue()),
				new VtiExitKeyValuePair("&w1&", Double.toString(wgh1)),
				new VtiExitKeyValuePair("&w2&", Double.toString(wgh2)),
				new VtiExitKeyValuePair("&nett&", Double.toString(nett)),
				new VtiExitKeyValuePair("&rejreason&",scrWFRejReason.getStringFieldValue()),
				new VtiExitKeyValuePair("&user&", user),
				new VtiExitKeyValuePair("&driver&", driver),
				new VtiExitKeyValuePair("&pl&", pckline),
			};
					
			VtiUserExitHeaderInfo headerInfo = getHeaderInfo();		
			int deviceNumber = headerInfo.getDeviceNumber();

			//Invoking the print
				try
				{
					invokePrintTemplate("WBRej" + deviceNumber, keyValuePairs);
				}
				catch (VtiExitException ee)
				{
					Log.error("Error with Printout", ee);
				}
		
		return new VtiUserExitResult();
	}	
}
