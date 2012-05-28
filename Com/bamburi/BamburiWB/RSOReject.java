package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class RSOReject extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		
		Date currNow = new Date();
		String currDate = DateFormatter.format("dd/MM/yyyy", currNow);
		String currTime = DateFormatter.format("HH:mm:ss", currNow);
		String currLdbDate = DateFormatter.format("ddMMyyyy", currNow);
		String currLDBTime = DateFormatter.format("HHmmss", currNow);
		
		//Declarations of variables and elements. Followed by the checking of the elements.
		VtiUserExitScreenField scrWFSalesOrd = getScreenField("VBELN");
		VtiUserExitScreenField scrWFRejReason = getScreenField("REJ_REASON");
		VtiUserExitScreenField scrFWeight1 = getScreenField("WEIGHT1");
		VtiUserExitScreenField scrFWeight2 = getScreenField("WEIGHT2");
		VtiUserExitScreenField scrFNettW = getScreenField("NETT_WEIGHT");
		VtiUserExitScreenField scrTruck = getScreenField("TRUCK_REG");
		VtiUserExitScreenField btnOk = getScreenField("BT_OKAY");
		VtiUserExitScreenField scrVtiRef = getScreenField("VTIREF");
		VtiUserExitScreenField scrFSlip = getScreenField("VTI_REF");
		VtiUserExitScreenField scrCBBridge = getScreenField("WEIGHBRIDGE");
				
		if(scrWFSalesOrd == null) return new VtiUserExitResult (999,"Failed to initialise VBELN.");
		if(scrWFRejReason == null) return new VtiUserExitResult (999,"Failed to initialise REJ_REASON.");
		if(scrFWeight1 == null) return new VtiUserExitResult (999,"Failed to initialise WEIGHT1.");
		if(scrFWeight2 == null) return new VtiUserExitResult (999,"Failed to initialise WEIGHT2.");
		if(scrFNettW == null) return new VtiUserExitResult (999,"Failed to initialise NETT_WEIGHT.");
		if(scrTruck == null) return new VtiUserExitResult (999,"Failed to initialise TRUCK_REG.");
		if(btnOk == null) return new VtiUserExitResult (999,"Failed to initialise BT_OKAY.");
		if(scrVtiRef == null) return new VtiUserExitResult (999,"Failed to initialise VTIREF.");
		if(scrFSlip == null) return new VtiUserExitResult (999,"Failed to initialise VTI_REF.");
		
		btnOk.setHiddenFlag(true);
		
		if(scrWFRejReason.getFieldValue().length() == 0)
			return new VtiUserExitResult(999, "Please select a rejection reason.");

				
		int tr = 0; 
		
		VtiUserExitScreenTable scrTblCustom = getScreenTable("TB_CUSTOM");
		if(scrTblCustom == null) return new VtiUserExitResult (999,"Failed to initialise TB_CUSTOM.");
		
		VtiUserExitScreenTable scrTblItems = getScreenTable("TB_ITEMS");
		if(scrTblItems == null) return new VtiUserExitResult (999,"Failed to initialise TB_ITEMS.");

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
		
		String rejTime = currTime;
		String truck = trckReg.getFieldValue("FIELDVALUE");
		String product = "";
		String type = tranType.getFieldValue("FIELDVALUE");
		double allocWght = allwgh.getDoubleFieldValue("FIELDVALUE");
		double wgh1 = scrFWeight1.getDoubleFieldValue();
		double wgh2 = scrFWeight2.getDoubleFieldValue();
		double nett = scrFNettW.getDoubleFieldValue();
		String pckline = packline.getFieldValue("FIELDVALUE");
		String aWgh = allwgh.getFieldValue("FIELDVALUE");
		String user = "";
		String driver = driv.getFieldValue("FIELDVALUE");
		String pshift = shift.getFieldValue("FIELDVALUE");
		String errorMsg = "Rejection successfull";
		
		StringBuffer rejSlip = new StringBuffer();
		StringBuffer Header= new StringBuffer();
		StringBuffer addDet= new StringBuffer();

		//Database TBL Declaration
		VtiExitLdbTable soHeaderTWRejLdbTable = getLocalDatabaseTable("YSWB_SO_HEADER");
		VtiExitLdbTable regRejLdbTable = getLocalDatabaseTable("YSWB_REGISTER");
		VtiExitLdbTable wbTWRejLdbTable = getLocalDatabaseTable("YSWB_WB");
		VtiExitLdbTable queueLdbTable = getLocalDatabaseTable("YSWB_QUEUE");
		
		if (soHeaderTWRejLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_SO_HEADER.");
		if (regRejLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_REGISTER.");
		if (wbTWRejLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_WB.");
		if (queueLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_QUEUE.");
		
		//Set next Function
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
				
		if (sessionHeader == null) return new VtiUserExitResult(999, "Error Retrieving Session Header Info");
		user = sessionHeader.getUserId();
		
		//Dataset Declaration
		VtiExitLdbSelectCriterion [] soHeaderTWRejSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("TRUCK", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruck.getFieldValue()),
								new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "REJECTED"),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup soHeaderTWRejSelCondGrp = new VtiExitLdbSelectConditionGroup(soHeaderTWRejSelConds, true);
		VtiExitLdbTableRow[] soHeaderTWRejLdbRows = soHeaderTWRejLdbTable.getMatchingRows(soHeaderTWRejSelCondGrp);

		if(soHeaderTWRejLdbRows.length > 1)
			return new VtiUserExitResult(999, "Multiple sales orders found.");
		
		if(soHeaderTWRejLdbRows.length == 0)
			return new VtiUserExitResult(999, "Order details not matched in the database.");
		
		String soStatus = soHeaderTWRejLdbRows[0].getFieldValue("STATUS");
		
		if(soStatus.equalsIgnoreCase("Failed") || soStatus.equalsIgnoreCase("Complete") 
				||soStatus.equalsIgnoreCase("New")  || soStatus.substring(0,1).equalsIgnoreCase("0"))
						return new VtiUserExitResult(999,"No processing allowed, check status.");
		
		//Set Reject Status
		
		soHeaderTWRejLdbRows[0].setFieldValue("STATUS","REJECTED");
		soHeaderTWRejLdbRows[0].setFieldValue("TIMESTAMP","");
		
		
		VtiExitLdbSelectCriterion [] regRejSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruck.getFieldValue()),
							new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVtiRef.getFieldValue()),
								new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "P"),
								new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "D"),
								new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "F"),
								new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, ""),
								new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "C"),
								//new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "A"),
								new VtiExitLdbSelectCondition("INSPSTATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "R"),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup regRejSelCondGrp = new VtiExitLdbSelectConditionGroup(regRejSelConds, true);
		VtiExitLdbTableRow[] regRejLdbRows = regRejLdbTable.getMatchingRows(regRejSelCondGrp);

		if(regRejLdbRows.length == 0 || regRejLdbRows.length > 1)
			return new VtiUserExitResult(999, "Please check status. Rejection not possible, registration count " + regRejLdbRows.length);
		
		regRejLdbRows[0].setFieldValue("INSPSTATUS", "A");
		regRejLdbRows[0].setFieldValue("TIMESTAMP", "");
		
		VtiExitLdbSelectCriterion [] wbTWRejSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
							new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, soHeaderTWRejLdbRows[0].getFieldValue("TRUCK")),
								new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "REJECTED"),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup wbTWRejSelCondGrp = new VtiExitLdbSelectConditionGroup(wbTWRejSelConds, true);
		VtiExitLdbTableRow[] wbTWRejLdbRows = wbTWRejLdbTable.getMatchingRows(wbTWRejSelCondGrp);

		if(wbTWRejLdbRows.length == 0)
		{
			//Create weight transactions in YSWB_WB
			VtiExitLdbTableRow ldbRowWeigh1 = wbTWRejLdbTable.newRow();
			
			//Populate TBL Fields
			ldbRowWeigh1.setFieldValue("SERVERGROUP", getServerGroup());
			ldbRowWeigh1.setFieldValue("SERVERID", getServerId());
			ldbRowWeigh1.setFieldValue("TRANDATE", currLdbDate);
			ldbRowWeigh1.setFieldValue("VTIREF", scrFSlip.getFieldValue());
			ldbRowWeigh1.setFieldValue("TRUCKREG", scrTruck.getFieldValue());
			ldbRowWeigh1.setFieldValue("VTIREFA", soHeaderTWRejLdbRows[0].getFieldValue("VTIREF"));
			ldbRowWeigh1.setFieldValue("VBELN", scrWFSalesOrd.getFieldValue());
			ldbRowWeigh1.setFieldValue("WEIGHT1", scrFWeight1.getFieldValue());
			ldbRowWeigh1.setFieldValue("WEIGHT1_D", currLdbDate);
			ldbRowWeigh1.setFieldValue("WEIGHT1_T", currLDBTime);
			ldbRowWeigh1.setFieldValue("PRINTFLAG", "X");
			ldbRowWeigh1.setFieldValue("WEIGHBRIDGE",scrCBBridge.getFieldValue());
			ldbRowWeigh1.setFieldValue("DRIVER",driv.getFieldValue("FIELDVALUE"));
			ldbRowWeigh1.setFieldValue("CUSTOMER",custsupp.getFieldValue("FIELDVALUE"));
			ldbRowWeigh1.setFieldValue("TRANSPORTER",transprtr.getFieldValue("FIELDVALUE"));
			ldbRowWeigh1.setFieldValue("TRANSPORTTYPE",tranType.getFieldValue("FIELDVALUE"));
			ldbRowWeigh1.setFieldValue("ALLOC_WHT",allwgh.getFieldValue("FIELDVALUE"));
			ldbRowWeigh1.setFieldValue("DELVNO",ordNo.getFieldValue("FIELDVALUE"));
			ldbRowWeigh1.setFieldValue("REBAG",rebag.getFieldValue("FIELDVALUE"));
			ldbRowWeigh1.setFieldValue("SHIFT",shift.getFieldValue("FIELDVALUE"));
			ldbRowWeigh1.setFieldValue("TRAILERNO",tralnum.getFieldValue("FIELDVALUE"));
			ldbRowWeigh1.setFieldValue("REMARKS","WB rejected before weigh done.");
			ldbRowWeigh1.setFieldValue("USERID",sessionHeader.getUserId());
			ldbRowWeigh1.setFieldValue("TIMESTAMP","");
			
			ldbRowWeigh1.setFieldValue("STATUS","Rejected");
			ldbRowWeigh1.setFieldValue("REJFLAG","X");
			ldbRowWeigh1.setFieldValue("REJTIME",currLDBTime);
			ldbRowWeigh1.setFieldValue("REJRSN",scrWFRejReason.getFieldValue());

			
			try
			{
				wbTWRejLdbTable.saveRow(ldbRowWeigh1);
			}
			catch (VtiExitException ee)
			{
				Log.error("Error updating weighin, please try again.",ee);
				return new VtiUserExitResult(999,"Unable to save rejection details.");
			}
		}
		else
		{
			wbTWRejLdbRows[0].setFieldValue("SHIFT",pshift);
			wbTWRejLdbRows[0].setFieldValue("USERID",sessionHeader.getUserId());
			wbTWRejLdbRows[0].setFieldValue("STATUS","Rejected");
			wbTWRejLdbRows[0].setFieldValue("REJFLAG","X");
			wbTWRejLdbRows[0].setFieldValue("REJTIME",currLDBTime);
			wbTWRejLdbRows[0].setFieldValue("REJRSN",scrWFRejReason.getFieldValue());
			wbTWRejLdbRows[0].setFieldValue("TIMESTAMP","");
			wbTWRejLdbRows[0].setFieldValue("ALLOC_WHT",allocWght);
			wbTWRejLdbRows[0].setFieldValue("DRIVER",driver);
			wbTWRejLdbRows[0].setFieldValue("TRANSPORTER",transprtr.getFieldValue("FIELDVALUE"));
			
			try
			{
				wbTWRejLdbTable.saveRow(wbTWRejLdbRows[0]);
			}
			catch (VtiExitException ee)
			{
				Log.error("Error updating weighin, please try again.",ee);
				return new VtiUserExitResult(999,"Unable to save rejections details.");
			}
		}
		
		try
		{
			soHeaderTWRejLdbTable.saveRow(soHeaderTWRejLdbRows[0]);
			regRejLdbTable.saveRow(regRejLdbRows[0]);
		}
		catch (VtiExitException ee)
		{
			Log.error("Unable to reject this order.",ee);
			return new VtiUserExitResult(999,"Unable to reject this order.");
		}
		
		for(int i = 0;i < scrTblItems.getRowCount();i++)
		{
			//Dataset Declaration
				VtiExitLdbSelectCriterion [] rsoItemRejSelConds = 
				{
						new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
							new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTblItems.getRow(i).getFieldValue("VBELN_I")),
									new VtiExitLdbSelectCondition("TRUCK", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTruck.getFieldValue()),
										new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "REJECTED"),
						new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
      
				VtiExitLdbSelectConditionGroup rsoItemRejSelCondGrp = new VtiExitLdbSelectConditionGroup(rsoItemRejSelConds, true);
				VtiExitLdbTableRow[] rsoItemRejLdbRows = soHeaderTWRejLdbTable.getMatchingRows(rsoItemRejSelCondGrp);

				if(rsoItemRejLdbRows.length > 1)
					return new VtiUserExitResult(999, "Multiple sales orders found.");
		
				if(rsoItemRejLdbRows.length == 0)
					return new VtiUserExitResult(999, "Order details not matched in the database.");
		
				String soIStatus = rsoItemRejLdbRows[0].getFieldValue("STATUS");
		
				if(soIStatus.equalsIgnoreCase("Failed") || soIStatus.equalsIgnoreCase("Complete") 
						||soIStatus.equalsIgnoreCase("New")  || soIStatus.substring(0,1).equalsIgnoreCase("0"))
								return new VtiUserExitResult(999,"No processing allowed, check status.");
		
				//Set Reject Status
		
				rsoItemRejLdbRows[0].setFieldValue("STATUS","REJECTED");
				rsoItemRejLdbRows[0].setFieldValue("TIMESTAMP","");
		
				VtiExitLdbSelectCriterion [] wbRsoItemRejSelConds = 
				{
						new VtiExitLdbSelectCondition("SERVERGROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
							new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
								new VtiExitLdbSelectCondition("VBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrTblItems.getRow(i).getFieldValue("VBELN_I")),
									new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, rsoItemRejLdbRows[0].getFieldValue("TRUCK")),
										new VtiExitLdbSelectCondition("STATUS", VtiExitLdbSelectCondition.NE_OPERATOR, "REJECTED"),
						new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
      
				VtiExitLdbSelectConditionGroup wbRsoItemRejSelCondGrp = new VtiExitLdbSelectConditionGroup(wbRsoItemRejSelConds, true);
				VtiExitLdbTableRow[] wbRsoItemRejLdbRows = wbTWRejLdbTable.getMatchingRows(wbRsoItemRejSelCondGrp);

				if(wbRsoItemRejLdbRows.length == 0)
				{
					//Create weight transactions in YSWB_WB
					VtiExitLdbTableRow ldbRowWeigh1 = wbTWRejLdbTable.newRow();
					
					//Populate TBL Fields
					ldbRowWeigh1.setFieldValue("SERVERGROUP", getServerGroup());
					ldbRowWeigh1.setFieldValue("SERVERID", getServerId());
					ldbRowWeigh1.setFieldValue("TRANDATE", currLdbDate);
					ldbRowWeigh1.setFieldValue("VTIREF", scrFSlip.getFieldValue());
					ldbRowWeigh1.setFieldValue("TRUCKREG", scrTruck.getFieldValue());
					ldbRowWeigh1.setFieldValue("VTIREFA", rsoItemRejLdbRows[0].getFieldValue("VTIREF"));
					ldbRowWeigh1.setFieldValue("VBELN", scrWFSalesOrd.getFieldValue());
					ldbRowWeigh1.setFieldValue("WEIGHT1", scrFWeight1.getFieldValue());
					ldbRowWeigh1.setFieldValue("WEIGHT1_D", currLdbDate);
					ldbRowWeigh1.setFieldValue("WEIGHT1_T", currLDBTime);
					ldbRowWeigh1.setFieldValue("PRINTFLAG", "X");
					ldbRowWeigh1.setFieldValue("WEIGHBRIDGE",scrCBBridge.getFieldValue());
					ldbRowWeigh1.setFieldValue("DRIVER",driv.getFieldValue("FIELDVALUE"));
					ldbRowWeigh1.setFieldValue("CUSTOMER",custsupp.getFieldValue("FIELDVALUE"));
					ldbRowWeigh1.setFieldValue("TRANSPORTER",transprtr.getFieldValue("FIELDVALUE"));
					ldbRowWeigh1.setFieldValue("TRANSPORTTYPE",tranType.getFieldValue("FIELDVALUE"));
					ldbRowWeigh1.setFieldValue("ALLOC_WHT",allwgh.getFieldValue("FIELDVALUE"));
					ldbRowWeigh1.setFieldValue("DELVNO",ordNo.getFieldValue("FIELDVALUE"));
					ldbRowWeigh1.setFieldValue("REBAG",rebag.getFieldValue("FIELDVALUE"));
					ldbRowWeigh1.setFieldValue("SHIFT",shift.getFieldValue("FIELDVALUE"));
					ldbRowWeigh1.setFieldValue("TRAILERNO",tralnum.getFieldValue("FIELDVALUE"));
					ldbRowWeigh1.setFieldValue("REMARKS","WB rejected before weigh done.");
					ldbRowWeigh1.setFieldValue("USERID",sessionHeader.getUserId());
					ldbRowWeigh1.setFieldValue("TIMESTAMP","");
					
					ldbRowWeigh1.setFieldValue("STATUS","Rejected");
					ldbRowWeigh1.setFieldValue("REJFLAG","X");
					ldbRowWeigh1.setFieldValue("REJTIME",currLDBTime);
					ldbRowWeigh1.setFieldValue("REJRSN",scrWFRejReason.getFieldValue());

					
					try
					{
						wbTWRejLdbTable.saveRow(ldbRowWeigh1);
					}
					catch (VtiExitException ee)
					{
						Log.error("Error updating weighin, please try again.",ee);
						return new VtiUserExitResult(999,"Unable to save rejection details.");
					}
				}
				else
				{
					wbRsoItemRejLdbRows[0].setFieldValue("SHIFT",pshift);
					wbRsoItemRejLdbRows[0].setFieldValue("USERID",sessionHeader.getUserId());
					wbRsoItemRejLdbRows[0].setFieldValue("STATUS","Rejected");
					wbRsoItemRejLdbRows[0].setFieldValue("REJFLAG","X");
					wbRsoItemRejLdbRows[0].setFieldValue("REJTIME",currLDBTime);
					wbRsoItemRejLdbRows[0].setFieldValue("REJRSN",scrWFRejReason.getFieldValue());
					wbRsoItemRejLdbRows[0].setFieldValue("TIMESTAMP","");
					wbRsoItemRejLdbRows[0].setFieldValue("ALLOC_WHT",allocWght);
					wbRsoItemRejLdbRows[0].setFieldValue("DRIVER",driver);
					wbRsoItemRejLdbRows[0].setFieldValue("TRANSPORTER",transprtr.getFieldValue("FIELDVALUE"));
					
					try
					{
						wbTWRejLdbTable.saveRow(wbRsoItemRejLdbRows[0]);
					}
					catch (VtiExitException ee)
					{
						Log.error("Error updating weighin, please try again.",ee);
						return new VtiUserExitResult(999,"Unable to save rejections details.");
					}
				}
		
				try
				{
					soHeaderTWRejLdbTable.saveRow(rsoItemRejLdbRows[0]);
				}
				catch (VtiExitException ee)
				{
					Log.error("Unable to reject this order.",ee);
					return new VtiUserExitResult(999,"Unable to reject this order.");
				}
		}
		
		Header.append("NAIROBI GRINDING PLANT");
		addDet.append("P.O Box 524, ATHI RIVER");
		
		
			
			VtiExitLdbSelectCriterion [] qSelConds = 
			{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("Q_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWFSalesOrd.getFieldValue()),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup qSelCondGrp = new VtiExitLdbSelectConditionGroup(qSelConds, true);
			VtiExitLdbTableRow[] qTLdbRows = queueLdbTable.getMatchingRows(qSelCondGrp);
			
			if(qTLdbRows.length > 0)
			{
				qTLdbRows[0].setFieldValue("Q_STATUS","Rejected");
				qTLdbRows[0].setFieldValue("TIMESTAMP","");
				try
				{
					queueLdbTable.saveRow(qTLdbRows[0]);
				}
				catch (VtiExitException ee)
				{
					Log.error("Error updating queue status, please correct in the queue.",ee);
					return new VtiUserExitResult(999,"Unable to update the Sales Order.");
				}
			}
			else
				errorMsg = "Rejection not updated in the queue for this truck.";
			
			
			
			VtiExitKeyValuePair[] keyValuePairs = 
			{
				new VtiExitKeyValuePair("&Header&", Header.toString()),
				new VtiExitKeyValuePair("&addDet&", addDet.toString()),
				new VtiExitKeyValuePair("&soTime&", rejTime),
				new VtiExitKeyValuePair("&truck&", truck),
				new VtiExitKeyValuePair("&pType&", type),
				new VtiExitKeyValuePair("&allocWght&", Double.toString(allocWght)),
				new VtiExitKeyValuePair("&product&", scrWFSalesOrd.getFieldValue()),
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
			sessionHeader.setNextFunctionId("YSWB_TROUTBOUND");
		return new VtiUserExitResult(000,errorMsg);
	}	
}

