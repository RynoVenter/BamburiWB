package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class DelGetTareWeight extends VtiUserExit 
{/*Get the Tare weight for the IC from the WB table as per the 1st weight.
  */
	public VtiUserExitResult execute() throws VtiExitException
	{
			//Generate Slip number into vti_ref field from number range object from yswb_slip
		VtiUserExitScreenField scrWDelivDoc = getScreenField("VBELN");
		VtiUserExitScreenField scrFWeight1 = getScreenField("WEIGHT1");
		VtiUserExitScreenField scrFWTStamp1 = getScreenField("WGH1_TIMESTAMP");
		VtiUserExitScreenField btnReject = getScreenField("BT_REJECT");
		VtiUserExitScreenField scrVRef = getScreenField("VTIREF");
		VtiUserExitScreenField scrFSlip = getScreenField("VTI_REF");
		VtiUserExitScreenField scrRBWeigh1 = getScreenField("RB_WEIGH1");
		VtiUserExitScreenField scrRBWeigh2 = getScreenField("RB_WEIGH2");
		VtiUserExitScreenField scrFWeight = getScreenField("WEIGHT");
		VtiUserExitScreenField scrFWeight2 = getScreenField("WEIGHT2");
		VtiUserExitScreenField scrFWTStamp2 = getScreenField("WGH2_TIMESTAMP");
		VtiUserExitScreenField scrChkPrn = getScreenField("CHK_PRINT");
		VtiUserExitScreenField scrFNettW = getScreenField("NETT_WEIGHT");
		VtiUserExitScreenField scrFNettTS = getScreenField("NETT_TIMESTAMP");
		VtiUserExitScreenField scfTolWarning = getScreenField("TOLL_MESSAGE");
		VtiUserExitScreenField scrCBBridge = getScreenField("WEIGHBRIDGE");
		VtiUserExitScreenField scrRBFull = getScreenField("RB_FULL");
		VtiUserExitScreenField scrRBPartial = getScreenField("RB_PARTIAL");
		VtiUserExitScreenField btnSave = getScreenField("BT_SAVE");
		VtiUserExitScreenField scrRegNo = getScreenField("TRUCK_REG");
		VtiUserExitScreenField btnTare = getScreenField("GET_TARE");

		
		if(btnReject == null) return new VtiUserExitResult (999,"Failed to initialise BT_REJECT.");
		if(scrWDelivDoc == null) return new VtiUserExitResult (999,"Failed to initialise EBELN.");
		if(scrFWeight1 == null) return new VtiUserExitResult (999,"Failed to initialise WEIGHT1.");
		if(scrFWTStamp1 == null) return new VtiUserExitResult (999,"Failed to initialise WGH1_TIMESTAMP.");
		if(scrVRef == null) return new VtiUserExitResult (999,"Failed to initialise VTIREF.");
		if(scrFSlip == null) return new VtiUserExitResult (999,"Failed to initialise VTI_REF.");
		if(scrRBWeigh1 == null) return new VtiUserExitResult (999,"Failed to initialise RB_WEIGH1.");
		if(scrRBWeigh2 == null) return new VtiUserExitResult (999,"Failed to initialise RB_WEIGH2.");
		if(scrFWeight == null) return new VtiUserExitResult (999,"Failed to initialise WEIGHT.");
		if(scrFWeight2 == null) return new VtiUserExitResult (999,"Failed to initialise WEIGHT2.");
		if(scrFWTStamp2 == null) return new VtiUserExitResult (999,"Failed to initialise WGH2_TIMESTAMP.");
		if(scrChkPrn == null) return new VtiUserExitResult (999,"Failed to initialise CHK_PRINT.");
		if(scrFNettW == null) return new VtiUserExitResult (999,"Failed to initialise NETT_WEIGHT.");
		if(scrFNettTS == null) return new VtiUserExitResult (999,"Failed to initialise NETT_TIMESTAMP.");
		if(scfTolWarning == null) return new VtiUserExitResult (999,"Failed to initialise TOLL_MESSAGE.");
		if(scrCBBridge == null) return new VtiUserExitResult (999,"Failed to initialise WEIGHBRIDGE.");
		if(scrRBFull == null) return new VtiUserExitResult (999,"Failed to initialise RB_FULL.");
		if(scrRBPartial == null) return new VtiUserExitResult (999,"Failed to initialise RB_PARTIAL.");
		if(btnSave == null) return new VtiUserExitResult (999,"Failed to initialise BT_SAVE.");
		if(scrRegNo == null) return new VtiUserExitResult (999,"Failed to initialise TRUCK_REG.");
		if(btnTare == null) return new VtiUserExitResult (999,"Failed to initialise GET_TARE.");

		DBCalls dbCalls = new DBCalls();
		
		
		VtiExitLdbTable wbLdbTable = getLocalDatabaseTable("YSWB_WB");

		if (wbLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_WB.");
		
		String icStatus ="";
		
		FormatUtilities fu = new FormatUtilities();
			
			VtiExitLdbSelectCriterion [] wbSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("DELIVDOC", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWDelivDoc.getFieldValue()),
							new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "Rejected"),
								new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
									new VtiExitLdbSelectCondition("WEIGHT1", VtiExitLdbSelectCondition.GT_OPERATOR, "0"),
										new VtiExitLdbSelectCondition("VTIREFA", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
											new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
			
			VtiExitLdbSelectConditionGroup wbSelCondGrp = new VtiExitLdbSelectConditionGroup(wbSelConds, true);
			
			VtiExitLdbOrderSpecification [] orderBy = 
			{
				new VtiExitLdbOrderSpecification("VTIREF",false),
			};
			
			VtiExitLdbTableRow[] wbLdbRows = wbLdbTable.getMatchingRows(wbSelCondGrp, orderBy);

			if(wbLdbRows.length == 0)
				return new VtiUserExitResult(999,"No previous weigh bridge data.");
			
			
			long tare = 0;
			int t = 0;
			for(int i = 0; i < wbLdbRows.length;i++)
			{
				if(wbLdbRows[i].getLongFieldValue("VTIREF") > tare)
					t = i;
			}
			
			if(wbLdbRows[t].getLongFieldValue("WEIGHT1") == 0)
				return new VtiUserExitResult(999,1,"Weigh 1 recorded a zero weight value, reweigh first weight.");
			
			scrFWeight1.setIntegerFieldValue(wbLdbRows[t].getIntegerFieldValue("WEIGHT1"));
			scrFWTStamp1.setFieldValue(fu.shortDate(wbLdbRows[t].getFieldValue("WEIGHT1_D")) + " " + fu.shortTime(wbLdbRows[t].getFieldValue("WEIGHT1_T")));

				//Declarations of variables and elements. Followed by the checking of the elements.
			
		scfTolWarning.setHiddenFlag(true);
		//Variable Declarations
		Date currNow = new Date();
		String currDate = DateFormatter.format("dd/MM/yyyy", currNow);
		String currTime = DateFormatter.format("HH:mm:ss", currNow);
		
		String currLdbDate = DateFormatter.format("yyyyMMdd", currNow);
		String currLdbTime = DateFormatter.format("HHmmss", currNow);
		float w1 = 0;
		float w2 = 0;
		float nett = 0;
		String weighTS = currDate + " " + currTime;
		
		if(scrFSlip.getFieldValue().length() < 1)
			return new VtiUserExitResult(999, "This Order is not ready for processing, please check the status.");
		

		//Database TBL Declaration
		VtiExitLdbTable icHeaderTWLdbTable = getLocalDatabaseTable("YSWB_IC_HEADER");
		VtiExitLdbTable icItemsTWLdbTable = getLocalDatabaseTable("YSWB_IC_ITEMS");
		VtiExitLdbTable wbTWLdbTable = getLocalDatabaseTable("YSWB_WB");
		VtiExitLdbTable statusLdbTable = getLocalDatabaseTable("YSWB_STATUS");
		VtiExitLdbTable registerTWLdbTable = getLocalDatabaseTable("YSWB_REGISTER");
		VtiExitLdbTable queueLdbTable = getLocalDatabaseTable("YSWB_QUEUE");
		
		if (icHeaderTWLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_IC_HEADER.");
		if (icItemsTWLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_IC_ITEMS.");
		if (wbTWLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_WB.");
		if (statusLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_STATUS.");
		if (queueLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_QUEUE.");
		
		
		//Dataset Declaration
				VtiExitLdbSelectCriterion [] registerTWSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("DELIVDOC", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWDelivDoc.getFieldValue()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
										new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "C"),
										new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "F"),
										new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "D"),
										new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "R"),
										new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "W"),
										new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
										new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "P"),
										new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "O"),
											new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup registerTWSelCondGrp = new VtiExitLdbSelectConditionGroup(registerTWSelConds, true);
		VtiExitLdbTableRow[] registerTWLdbRows = registerTWLdbTable.getMatchingRows(registerTWSelCondGrp);

		if(registerTWLdbRows.length == 0)
			return new VtiUserExitResult(999, "Registration not matching status.");
		
		registerTWLdbRows[0].setFieldValue("INSPSTATUS","W");
		registerTWLdbRows[0].setFieldValue("TIMESTAMP","");
		
		try
			{
				registerTWLdbTable.saveRow(registerTWLdbRows[0]);
			}
			catch (VtiExitException ee)
			{
				Log.error("Error updating weighin, please try again.",ee);
				return new VtiUserExitResult(999,"Unable to save status to the register.");
			}
		
		VtiExitLdbSelectCriterion [] icHeaderTWSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWDelivDoc.getFieldValue()),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup icHeaderTWSelCondGrp = new VtiExitLdbSelectConditionGroup(icHeaderTWSelConds, true);
		VtiExitLdbTableRow[] icHeaderTWLdbRows = icHeaderTWLdbTable.getMatchingRows(icHeaderTWSelCondGrp);

		if(icHeaderTWLdbRows.length == 0)
			return new VtiUserExitResult(999, "No purchase order detail exist.");
		
		VtiExitLdbSelectCriterion [] statusSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("DELIVDOC", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWDelivDoc.getFieldValue()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
								new VtiExitLdbSelectCondition("VTIREF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVRef.getFieldValue()),
									new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup statusSelCondGrp = new VtiExitLdbSelectConditionGroup(statusSelConds, true);
		VtiExitLdbTableRow[] statusLdbRows = statusLdbTable.getMatchingRows(statusSelCondGrp);

		if(statusLdbRows.length == 0)
			return new VtiUserExitResult(999, "No inter company order detail exist.");
		
		VtiExitLdbSelectCriterion [] icItemsTWSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWDelivDoc.getFieldValue()),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup icItemsTWSelCondGrp = new VtiExitLdbSelectConditionGroup(icItemsTWSelConds, true);
		VtiExitLdbTableRow[] icItemsTWLdbRows = icItemsTWLdbTable.getMatchingRows(icItemsTWSelCondGrp);

		if(statusLdbRows[0].getFieldValue("WGH_STATUS").equalsIgnoreCase("FAILED"))
		{
			return new VtiUserExitResult(999, "No vehicle inspection. No weigh in allowed.");
		}
			
		if(scrRBWeigh1.getFieldValue().equalsIgnoreCase("X"))
		{
			//Set weight and time fields
			if(scrFWeight1.getFieldValue().length() == 0)
			{
				if(scrFWeight.getFieldValue().length() == 0 && scrFWeight1.getFieldValue().length() == 0)
					return new VtiUserExitResult(999, "No weight retrieved from weigh bridge.");
				scrFWeight1.setIntegerFieldValue(scrFWeight.getIntegerFieldValue());
				scrFWTStamp1.setFieldValue(weighTS);
			}

			//Create weight transactions in YSWB_WB
			VtiExitLdbTableRow ldbRowWeigh1 = wbTWLdbTable.newRow();
			
			//Populate TBL Fields
			ldbRowWeigh1.setFieldValue("SERVERGROUP", getServerGroup());
			ldbRowWeigh1.setFieldValue("SERVERID", getServerId());
			ldbRowWeigh1.setFieldValue("TRANDATE", currLdbDate);
			ldbRowWeigh1.setFieldValue("VTIREF", scrFSlip.getFieldValue());
			ldbRowWeigh1.setFieldValue("DELIVDOC", scrWDelivDoc.getFieldValue());
			ldbRowWeigh1.setFieldValue("WEIGHT1", scrFWeight1.getFieldValue());
			ldbRowWeigh1.setFieldValue("WEIGHT1_D", currLdbDate);
			ldbRowWeigh1.setFieldValue("WEIGHT1_T", currLdbTime);
			ldbRowWeigh1.setFieldValue("PRINTFLAG", scrChkPrn.getFieldValue());
			ldbRowWeigh1.setFieldValue("STATUS", "Weigh 1");
			ldbRowWeigh1.setFieldValue("WEIGHBRIDGE",scrCBBridge.getFieldValue());
			ldbRowWeigh1.setFieldValue("TRUCKREG",scrRegNo.getFieldValue());
			ldbRowWeigh1.setFieldValue("VTIREFA",scrVRef.getFieldValue());
			ldbRowWeigh1.setFieldValue("TIMESTAMP","");
			
			try
			{
				wbTWLdbTable.saveRow(ldbRowWeigh1);
			}
			catch (VtiExitException ee)
			{
				Log.error("Error updating weighin, please try again.",ee);
				return new VtiUserExitResult(999,"Unable to save weighin to the Order.");
			}
			
			//Change Status
			statusLdbRows[0].setFieldValue("STATUS","W");
			statusLdbRows[0].setFieldValue("WGH_STATUS","Weigh 1");
			statusLdbRows[0].setFieldValue("TIMESTAMP","");
			
			try
			{
				statusLdbTable.saveRow(statusLdbRows[0]);
				scrRBWeigh2.setFieldValue("X");
				scrRBWeigh2.setDisplayOnlyFlag(false);
				scrRBWeigh1.setFieldValue("");
				scrRBWeigh1.setDisplayOnlyFlag(true);
				btnSave.setHiddenFlag(false);
			}
			catch (VtiExitException ee)
			{
				Log.error("Error updating weighin status, please try again.",ee);
				return new VtiUserExitResult(999,"Unable to update the Sales Order.");
			}
		}
		
		
		VtiExitLdbSelectCriterion [] qSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("Q_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWDelivDoc.getFieldValue()),
							new VtiExitLdbSelectCondition("Q_REGNO", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
								new VtiExitLdbSelectCondition("Q_STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "REJECTED"),
									new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup qSelCondGrp = new VtiExitLdbSelectConditionGroup(qSelConds, true);
			VtiExitLdbTableRow[] qTLdbRows = queueLdbTable.getMatchingRows(qSelCondGrp);
			
			if(qTLdbRows.length > 0)
			{
				qTLdbRows[0].setFieldValue("Q_STATUS","Weigh 1");
				qTLdbRows[0].setFieldValue("TIMESTAMP","");
			}
			
			try
			{
			
				if(qTLdbRows.length > 0)
				{
					queueLdbTable.saveRow(qTLdbRows[0]);
					dbCalls.ldbUpload("YSWB_QUEUE", this);
				}
			}
			catch (VtiExitException ee)
			{
				Log.error("Error updating weighin statusfor queue.",ee);
				return new VtiUserExitResult(000,"Unable to update the Queue.");
			}
			
			
		btnReject.setHiddenFlag(false);
		btnTare.setHiddenFlag(true);
		return new VtiUserExitResult();
	}

}
