package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class RSOGetTareWeight extends VtiUserExit 
{/*Get the tare weight from the WB table as per weight 1
  */
	public VtiUserExitResult execute() throws VtiExitException
	{
		boolean isVBELN = true;
		//Generate Slip number into vti_ref field from number range object from yswb_slip
		VtiUserExitScreenField scrWFOrd = getScreenField("VBELN");
		VtiUserExitScreenField scrFWeight1 = getScreenField("WEIGHT1");
		VtiUserExitScreenField scrFWTStamp1 = getScreenField("WGH1_TIMESTAMP");
		VtiUserExitScreenField btnReject = getScreenField("BT_REJECT");
		VtiUserExitScreenField btnTare = getScreenField("GET_TARE");
		VtiUserExitScreenField scrFSlip = getScreenField("VTI_REF");
		
		if(scrWFOrd == null) return new VtiUserExitResult (999,"Failed to initialise VBELN.");
		if(scrFWeight1 == null) return new VtiUserExitResult (999,"Failed to initialise WEIGHT1.");
		if(scrFWTStamp1 == null) return new VtiUserExitResult (999,"Failed to initialise WGH1_TIMESTAMP.");
		if(btnReject == null) return new VtiUserExitResult (999,"Failed to initialise BT_REJECT.");
		if(btnTare == null) return new VtiUserExitResult (999,"Failed to initialise GET_TARE.");
		if(scrFSlip == null) return new VtiUserExitResult (999,"Failed to initialise VTI_REF.");
		
		VtiUserExitScreenTable scrTblItems = getScreenTable("TB_ITEMS");
		if(scrTblItems == null) return new VtiUserExitResult (999,"Failed to initialise TB_ITEMS.");
		
		VtiExitLdbTable wbLdbTable = getLocalDatabaseTable("YSWB_WB");

		if (wbLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_WB.");
		
		String soStatus ="";
		
		FormatUtilities fu = new FormatUtilities();
			
			VtiExitLdbSelectCriterion [] wbSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.EQ_OPERATOR, "Rejected"),
								new VtiExitLdbSelectCondition("WEIGHT1", VtiExitLdbSelectCondition.GT_OPERATOR, "0"),
									new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
			 
			VtiExitLdbSelectConditionGroup wbSelCondGrp = new VtiExitLdbSelectConditionGroup(wbSelConds, true);
			
			VtiExitLdbOrderSpecification [] orderBy = 
			{
				new VtiExitLdbOrderSpecification("VTIREF",false),
			};
			
			VtiExitLdbTableRow[] wbLdbRows = wbLdbTable.getMatchingRows(wbSelCondGrp, orderBy);

			if(wbLdbRows.length == 0)
				return new VtiUserExitResult(999,"No previous weigh bridge data for Retail Sales Order.");
			
			long tare = 0;
			int t = 0;
			for(int i = 0; i < wbLdbRows.length;i++)
			{
				if(wbLdbRows[i].getLongFieldValue("VTIREF") > tare)
					t = i;
			}
			
			if(wbLdbRows[t].getLongFieldValue("WEIGHT1") > 0)
			{
				scrFWeight1.setIntegerFieldValue(wbLdbRows[t].getIntegerFieldValue("WEIGHT1"));
				scrFWTStamp1.setFieldValue(fu.shortDate(wbLdbRows[t].getFieldValue("WEIGHT1_D")) + " " + fu.shortTime(wbLdbRows[t].getFieldValue("WEIGHT1_T")));
			}
			else
				return new VtiUserExitResult(999,1,"Weigh 1 recorded a zero weight value, reweigh first weight.");
	
//Declarations of variables and elements. Followed by the checking of the elements.

		VtiUserExitScreenField scrWFSalesOrd = getScreenField("VBELN");
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
		VtiUserExitScreenField scrTruck = getScreenField("TRUCK_REG");
		VtiUserExitScreenField scrVRef = getScreenField("VTIREF");
		

		if(scrWFSalesOrd == null) return new VtiUserExitResult (999,"Failed to initialise VBELN.");
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
		if(btnReject == null) return new VtiUserExitResult (999,"Failed to initialise BT_REJECT.");
		if(scrTruck == null) return new VtiUserExitResult (999,"Failed to initialise TRUCK_REG.");
		
		scfTolWarning.setHiddenFlag(true);
		//Variable Declarations
		Date currNow = new Date();
		String currDate = DateFormatter.format("dd/MM/yyyy", currNow);
		String currTime = DateFormatter.format("HH:mm:ss", currNow);
		
		String currLdbDate = DateFormatter.format("yyyyMMdd", currNow);
		String currLdbTime = DateFormatter.format("HHmmss", currNow);
		long w1 = 0;
		long w2 = 0;
		long nett = 0;
		String weighTS = currDate + " " + currTime;
		
		DBCalls dbCalls = new DBCalls();
		
		if(scrFSlip.getFieldValue().length() < 1)
			return new VtiUserExitResult(999, "This Sales Order is not ready for processing, please check the status.");
					
		//Database TBL Declaration
		VtiExitLdbTable soHeaderTWLdbTable = getLocalDatabaseTable("YSWB_SO_HEADER");
		VtiExitLdbTable soItemsTWLdbTable = getLocalDatabaseTable("YSWB_SO_ITEMS");
		VtiExitLdbTable wbTWLdbTable = getLocalDatabaseTable("YSWB_WB");
		VtiExitLdbTable registerTWLdbTable = getLocalDatabaseTable("YSWB_REGISTER");
		VtiExitLdbTable queueLdbTable = getLocalDatabaseTable("YSWB_QUEUE");
		
		if (soHeaderTWLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_SO_HEADER.");
		if (soItemsTWLdbTable == null) return new VtiUserExitResult(999, "Unable to YSWB_SO_ITEMS table YSWB_SO_HEADER.");
		if (wbTWLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_WB.");
		if (registerTWLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_REGISTER.");
		if (queueLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_QUEUE.");
		
		//Dataset Declaration
		VtiExitLdbSelectCriterion [] registerTWSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruck.getFieldValue()),
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
		
		VtiExitLdbSelectCriterion [] soHeaderTWSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup soHeaderTWSelCondGrp = new VtiExitLdbSelectConditionGroup(soHeaderTWSelConds, true);
		VtiExitLdbTableRow[] soHeaderTWLdbRows = soHeaderTWLdbTable.getMatchingRows(soHeaderTWSelCondGrp);

		if(soHeaderTWLdbRows.length == 0)
			return new VtiUserExitResult(999, "No sales order detail exist.");
		
		VtiExitLdbSelectCriterion [] soItemsTWSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup soItemsTWSelCondGrp = new VtiExitLdbSelectConditionGroup(soItemsTWSelConds, true);
		VtiExitLdbTableRow[] soItemsTWLdbRows = soItemsTWLdbTable.getMatchingRows(soItemsTWSelCondGrp);
		
		if(soHeaderTWLdbRows[0].getFieldValue("STATUS").equalsIgnoreCase("New") || soHeaderTWLdbRows[0].getFieldValue("STATUS").equalsIgnoreCase("FAILED"))
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
			ldbRowWeigh1.setFieldValue("VBELN", scrWFSalesOrd.getFieldValue());
			ldbRowWeigh1.setFieldValue("TRUCKREG", scrTruck.getFieldValue());
			ldbRowWeigh1.setFieldValue("WEIGHT1", scrFWeight1.getFieldValue());
			ldbRowWeigh1.setFieldValue("WEIGHT1_D", currLdbDate);
			ldbRowWeigh1.setFieldValue("WEIGHT1_T", currLdbTime);
			ldbRowWeigh1.setFieldValue("PRINTFLAG", scrChkPrn.getFieldValue());
			ldbRowWeigh1.setFieldValue("STATUS", "Weigh 1");
			ldbRowWeigh1.setFieldValue("WEIGHBRIDGE",scrCBBridge.getFieldValue());
			ldbRowWeigh1.setFieldValue("VTIREFA",scrVRef.getFieldValue());
			ldbRowWeigh1.setFieldValue("TIMESTAMP","");
			
			try
			{
				wbTWLdbTable.saveRow(ldbRowWeigh1);
				Log.trace(0,"RSO TARE saved ");
			}
			catch (VtiExitException ee)
			{
				Log.error("Error updating weighin, please try again.",ee);
				return new VtiUserExitResult(999,"Unable to save weighin to Sales Order.");
			}
			
			//Change Status
			soHeaderTWLdbRows[0].setFieldValue("STATUS","Weigh 1");
			soHeaderTWLdbRows[0].setFieldValue("TIMESTAMP","");
			
			try
			{
				soHeaderTWLdbTable.saveRow(soHeaderTWLdbRows[0]);
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
		
		//Set Weigh1 status for actual orders
		//
		for(int i = 0;i < scrTblItems.getRowCount();i++)
		{
			VtiExitLdbSelectCriterion [] soHeaderIWSelConds = 
			{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTblItems.getRow(i).getFieldValue("VBELN_I")),
								new VtiExitLdbSelectCondition("DELIVERY", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
								new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup soHeaderIWSelCondGrp = new VtiExitLdbSelectConditionGroup(soHeaderIWSelConds, true);
			VtiExitLdbTableRow[] soHeaderIWLdbRows = soHeaderTWLdbTable.getMatchingRows(soHeaderIWSelCondGrp);

			if(soHeaderIWLdbRows.length == 0)
				return new VtiUserExitResult(999, "No sales order detail exist for " + scrTblItems.getRow(i).getFieldValue("VBELN_I") + ".");
			
			VtiExitLdbTableRow ldbRowWeighI1 = wbTWLdbTable.newRow();
			
			//Populate TBL Fields
			ldbRowWeighI1.setFieldValue("SERVERGROUP", getServerGroup());
			ldbRowWeighI1.setFieldValue("SERVERID", getServerId());
			ldbRowWeighI1.setFieldValue("TRANDATE", currLdbDate);
			ldbRowWeighI1.setFieldValue("VTIREF", getNextNumberFromNumberRange("YSWB_SLIP"));
			ldbRowWeighI1.setFieldValue("VBELN", scrTblItems.getRow(i).getFieldValue("VBELN_I"));
			ldbRowWeighI1.setFieldValue("TRUCKREG", scrTruck.getFieldValue());
			ldbRowWeighI1.setFieldValue("WEIGHT1", scrFWeight1.getFieldValue());
			ldbRowWeighI1.setFieldValue("WEIGHT1_D", currLdbDate);
			ldbRowWeighI1.setFieldValue("WEIGHT1_T", currLdbTime);
			ldbRowWeighI1.setFieldValue("PRINTFLAG", scrChkPrn.getFieldValue());
			ldbRowWeighI1.setFieldValue("STATUS", "Weigh 1");
			ldbRowWeighI1.setFieldValue("WEIGHBRIDGE",scrCBBridge.getFieldValue());
			ldbRowWeighI1.setFieldValue("VTIREFA",scrVRef.getFieldValue());
			ldbRowWeighI1.setFieldValue("TIMESTAMP","");
			ldbRowWeighI1.setFieldValue("PACKLOADER",scrWFSalesOrd.getFieldValue());
			
			try
			{
				wbTWLdbTable.saveRow(ldbRowWeighI1);
				Log.trace(0,"RSO VBELN TARE saved for " + scrTblItems.getRow(i).getFieldValue("VBELN_I"));
			}
			catch (VtiExitException ee)
			{
				Log.error("Error updating weighin, please try again.",ee);
				return new VtiUserExitResult(999,"Unable to save weighin to Sales Order.");
			}
			
			soHeaderIWLdbRows[0].setFieldValue("STATUS","Weigh 1");
			soHeaderIWLdbRows[0].setFieldValue("TIMESTAMP","");
			
			try
			{
				soHeaderTWLdbTable.saveRow(soHeaderIWLdbRows[0]);
			}
			catch (VtiExitException ee)
			{
				Log.error("Error updating sales order status, please try again.",ee);
				return new VtiUserExitResult(999,"Unable to save weighin to Sales Order.");
			}
		}
			
		//
		VtiExitLdbSelectCriterion [] qSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("Q_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("Q_REGNO", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruck.getFieldValue()),
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

