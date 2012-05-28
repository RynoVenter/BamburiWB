package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class CheckInspectionFail extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		boolean prefd = false;
		boolean ship = false;
		boolean stock = false;
		boolean rotate = false;

		Date currNow = new Date();
		
		String hostName = getHostInterfaceName();
		try
		{
			forceHeartbeat(hostName,true,250);
		}
		catch (VtiExitException ee)
		{
		}

		long interval =0;
		String currTime = DateFormatter.format("HH:mm:ss", currNow);
		String currDate = DateFormatter.format("dd/MM/yyyy", currNow);
		String currLdbDate = DateFormatter.format("yyyyMMdd", currNow);
		String currLdbTime = DateFormatter.format("HHmmss", currNow);
		String sErrorMsg;
		String docType = "";
		
		/*In the event that a truck fails inspection, remove the registration from the po and so headers, thus making that po or so availible for 
		pick up with another truck.
		*/
		
		//Declarations of variables and elements. Followed by the checking of the elements.

		VtiUserExitScreenField scrInsp = getScreenField("INSPSTATUS");
		VtiUserExitScreenField scrVbelnQt = getScreenField("DOC_NMBER");
		VtiUserExitScreenField scrVbeln = getScreenField("VBELN");
		VtiUserExitScreenField scrEbeln = getScreenField("EBELN");
		VtiUserExitScreenField scrWVtiRef = getScreenField("VTI_REF");
		VtiUserExitScreenField scrTime = getScreenField("TIME");
		VtiUserExitScreenField scrDate = getScreenField("DATE");
		VtiUserExitScreenField scrArrDate = getScreenField("ARR_DATE");
		VtiUserExitScreenField scrArrTime = getScreenField("ARR_TIME");
	//	VtiUserExitScreenField scrSelf = getScreenField("SELF");
	//	VtiUserExitScreenField scrContract = getScreenField("CONTRACTOR");
		VtiUserExitScreenField scrRegNo = getScreenField("REGNO");
		VtiUserExitScreenField scrInsp1 = getScreenField("STATUS1");
		VtiUserExitScreenField scrInsp2 = getScreenField("STATUS2");
		VtiUserExitScreenField scrInsp3 = getScreenField("STATUS3");
	//	VtiUserExitScreenField scrInsp4 = getScreenField("STATUS4");
	//	VtiUserExitScreenField scrInsp5 = getScreenField("STATUS5");
		VtiUserExitScreenField scrTrans = getScreenField("TRANSPORTER");
		VtiUserExitScreenField scrPref = getScreenField("PREF");
		VtiUserExitScreenField scrShip = getScreenField("CHKSHIP");
		VtiUserExitScreenField scrStock = getScreenField("CHKSTOCKPILE");
		VtiUserExitScreenField scrRotate = getScreenField("CHKROTATE");
		VtiUserExitScreenField scrGP = getScreenField("GATEPASS");
		VtiUserExitScreenField scrTruckType = getScreenField("TRUCKTYPE");
		
		VtiUserExitScreenField btnQ1 = getScreenField("BT_ONE");
		VtiUserExitScreenField btnQ2 = getScreenField("BT_TWO");
		VtiUserExitScreenField btnQ3 = getScreenField("BT_THREE");
		
		VtiUserExitScreenField scr1Field = getScreenField("1");
		VtiUserExitScreenField scr2Field = getScreenField("2");
		VtiUserExitScreenField scr3Field = getScreenField("3");
		VtiUserExitScreenField scr4Field = getScreenField("4");
		VtiUserExitScreenField scr5Field = getScreenField("5");
		VtiUserExitScreenField scr6Field = getScreenField("6");
		VtiUserExitScreenField scr7Field = getScreenField("7");
		VtiUserExitScreenField scr8Field = getScreenField("8");
		VtiUserExitScreenField scr9Field = getScreenField("9");
		VtiUserExitScreenField scr10Field = getScreenField("10");
		VtiUserExitScreenField scr11Field = getScreenField("11");
		VtiUserExitScreenField scr12Field = getScreenField("12");
		VtiUserExitScreenField scr13Field = getScreenField("13");
		VtiUserExitScreenField scr14Field = getScreenField("14");
		VtiUserExitScreenField scr15Field = getScreenField("15");
		VtiUserExitScreenField scr16Field = getScreenField("16");
		VtiUserExitScreenField scr17Field = getScreenField("17");
		VtiUserExitScreenField scr18Field = getScreenField("18");
		VtiUserExitScreenField scr19Field = getScreenField("19");
		VtiUserExitScreenField scr20Field = getScreenField("20");
		VtiUserExitScreenField scr21Field = getScreenField("21");
		
		VtiUserExitScreenField wfP1 = getScreenField("P1");
		VtiUserExitScreenField wfP2 = getScreenField("P2");
		VtiUserExitScreenField wfP3 = getScreenField("P3");
		VtiUserExitScreenField wfP4 = getScreenField("P4");
		VtiUserExitScreenField wfP5 = getScreenField("P5");
		VtiUserExitScreenField wfP6 = getScreenField("P6");
		VtiUserExitScreenField wfP7 = getScreenField("P7");
		VtiUserExitScreenField wfP8 = getScreenField("P8");
		VtiUserExitScreenField wfP9 = getScreenField("P9");
		VtiUserExitScreenField wfP10 = getScreenField("P10");
		VtiUserExitScreenField wfP11 = getScreenField("P11");
		VtiUserExitScreenField wfP12 = getScreenField("P12");
		VtiUserExitScreenField wfP13 = getScreenField("P13");
		VtiUserExitScreenField wfP14 = getScreenField("P14");
		VtiUserExitScreenField wfP15 = getScreenField("P15");
		VtiUserExitScreenField wfP16 = getScreenField("P16");
		VtiUserExitScreenField wfP17 = getScreenField("P17");
		VtiUserExitScreenField wfP18 = getScreenField("P18");
		VtiUserExitScreenField wfP19 = getScreenField("P19");
		VtiUserExitScreenField wfP20 = getScreenField("P20");
		VtiUserExitScreenField wfP21 = getScreenField("P21");
		//VtiUserExitScreenField wfP22 = getScreenField("P22");
		VtiUserExitScreenField wfF1 = getScreenField("F1");
		VtiUserExitScreenField wfF2 = getScreenField("F2");
		VtiUserExitScreenField wfF3 = getScreenField("F3");
		VtiUserExitScreenField wfF4 = getScreenField("F4");
		VtiUserExitScreenField wfF5 = getScreenField("F5");
		VtiUserExitScreenField wfF6 = getScreenField("F6");
		VtiUserExitScreenField wfF7 = getScreenField("F7");
		VtiUserExitScreenField wfF8 = getScreenField("F8");
		VtiUserExitScreenField wfF9 = getScreenField("F9");
		VtiUserExitScreenField wfF10 = getScreenField("F10");
		VtiUserExitScreenField wfF11 = getScreenField("F11");
		VtiUserExitScreenField wfF12 = getScreenField("F12");
		VtiUserExitScreenField wfF13 = getScreenField("F13");
		VtiUserExitScreenField wfF14 = getScreenField("F14");
		VtiUserExitScreenField wfF15 = getScreenField("F15");
		VtiUserExitScreenField wfF16 = getScreenField("F16");
		VtiUserExitScreenField wfF17 = getScreenField("F17");
		VtiUserExitScreenField wfF18 = getScreenField("F18");
		VtiUserExitScreenField wfF19 = getScreenField("F19");
		VtiUserExitScreenField wfF20 = getScreenField("F20");
		VtiUserExitScreenField wfF21 = getScreenField("F21");
		//VtiUserExitScreenField wfF22 = getScreenField("F22");
		VtiUserExitScreenField wfC1 = getScreenField("C1");
		VtiUserExitScreenField wfC2 = getScreenField("C2");
		VtiUserExitScreenField wfC3 = getScreenField("C3");
		VtiUserExitScreenField wfC4 = getScreenField("C4");
		VtiUserExitScreenField wfC5 = getScreenField("C5");
		VtiUserExitScreenField wfC6 = getScreenField("C6");
		VtiUserExitScreenField wfC7 = getScreenField("C7");
		VtiUserExitScreenField wfC8 = getScreenField("C8");
		VtiUserExitScreenField wfC9 = getScreenField("C9");
		VtiUserExitScreenField wfC10 = getScreenField("C10");
		VtiUserExitScreenField wfC11 = getScreenField("C11");
		VtiUserExitScreenField wfC12 = getScreenField("C12");
		VtiUserExitScreenField wfC13 = getScreenField("C13");
		VtiUserExitScreenField wfC14 = getScreenField("C14");
		VtiUserExitScreenField wfC15 = getScreenField("C15");
		VtiUserExitScreenField wfC16 = getScreenField("C16");
		VtiUserExitScreenField wfC17 = getScreenField("C17");
		VtiUserExitScreenField wfC18 = getScreenField("C18");
		VtiUserExitScreenField wfC19 = getScreenField("C19");
		VtiUserExitScreenField wfC20 = getScreenField("C20");
		VtiUserExitScreenField wfC21 = getScreenField("C21");
		//VtiUserExitScreenField wfC22 = getScreenField("C22");
		
		
		if (scrInsp == null) return new VtiUserExitResult(999, "Unable to initialise screen field INSPSTATUS.");
		if (scrInsp1 == null) return new VtiUserExitResult(999, "Unable to initialise screen field STATUS1.");
		if (scrInsp2 == null) return new VtiUserExitResult(999, "Unable to initialise screen field STATUS2.");
		if (scrInsp3 == null) return new VtiUserExitResult(999, "Unable to initialise screen field STATUS3.");
		//if (scrInsp4 == null) return new VtiUserExitResult(999, "Unable to initialise screen field STATUS4.");
		if (scrVbeln == null) return new VtiUserExitResult(999, "Unable to initialise screen field VBELN.");
		if (scrEbeln == null) return new VtiUserExitResult(999, "Unable to initialise screen field EBELN.");
		if (scrVbelnQt == null) return new VtiUserExitResult(999, "Unable to initialise screen field DOC_NMBER.");
//		if (scrSelf == null) return new VtiUserExitResult(999, "Unable to initialise screen field REGNO.");
//		if (scrContract == null) return new VtiUserExitResult(999, "Unable to initialise screen field SELF.");
		if (scrRegNo == null) return new VtiUserExitResult(999, "Unable to initialise screen field REGNO.");
		if (scrWVtiRef == null) return new VtiUserExitResult(999, "Unable to initialise screen field VTI_REF.");
		if (scrDate == null) return new VtiUserExitResult(999, "Unable to initialise screen field DATE.");
		if (scrTime == null) return new VtiUserExitResult(999, "Unable to initialise screen field TIME.");
		if (scrArrDate == null) return new VtiUserExitResult(999, "Unable to initialise screen field ARR_DATE.");
		if (scrArrTime == null) return new VtiUserExitResult(999, "Unable to initialise screen field ARR_TIME.");
		if (scrTrans == null) return new VtiUserExitResult(999, "Unable to initialise screen field TRANSPORTER.");
		if (scrPref == null) return new VtiUserExitResult(999, "Unable to initialise screen field PREF.");
		if (scrTruckType== null) return new VtiUserExitResult(999, "Unable to initialise screen field TRUCKTYPE.");
		if (scrGP == null) return new VtiUserExitResult(999, "Unable to initialise screen field GATEPASS.");
		
		if(wfP1 == null) return new VtiUserExitResult (999,"Failed to initialise P1.");
		if(wfP2 == null) return new VtiUserExitResult (999,"Failed to initialise P2.");
		if(wfP3 == null) return new VtiUserExitResult (999,"Failed to initialise P3.");
		if(wfP4 == null) return new VtiUserExitResult (999,"Failed to initialise P4.");
		if(wfP5 == null) return new VtiUserExitResult (999,"Failed to initialise P5.");
		if(wfP6 == null) return new VtiUserExitResult (999,"Failed to initialise P6.");
		if(wfP7 == null) return new VtiUserExitResult (999,"Failed to initialise P7.");
		if(wfP8 == null) return new VtiUserExitResult (999,"Failed to initialise P8.");
		if(wfP9 == null) return new VtiUserExitResult (999,"Failed to initialise P9.");
		if(wfP10 == null) return new VtiUserExitResult (999,"Failed to initialise P10.");
		if(wfP11 == null) return new VtiUserExitResult (999,"Failed to initialise P11.");
		if(wfP12 == null) return new VtiUserExitResult (999,"Failed to initialise P12.");
		if(wfP13 == null) return new VtiUserExitResult (999,"Failed to initialise P13.");
		if(wfP14 == null) return new VtiUserExitResult (999,"Failed to initialise P14.");
		if(wfP15 == null) return new VtiUserExitResult (999,"Failed to initialise P15.");
		if(wfP16 == null) return new VtiUserExitResult (999,"Failed to initialise P16.");
		if(wfP17 == null) return new VtiUserExitResult (999,"Failed to initialise P17.");
		if(wfP18 == null) return new VtiUserExitResult (999,"Failed to initialise P18.");
		if(wfP19 == null) return new VtiUserExitResult (999,"Failed to initialise P19.");
		if(wfP20 == null) return new VtiUserExitResult (999,"Failed to initialise P20.");
		if(wfP21 == null) return new VtiUserExitResult (999,"Failed to initialise P21.");
		//if(wfP22 == null) return new VtiUserExitResult (999,"Failed to initialise P22.");
		if(wfF1 == null) return new VtiUserExitResult (999,"Failed to initialise F1.");
		if(wfF2 == null) return new VtiUserExitResult (999,"Failed to initialise F2.");
		if(wfF3 == null) return new VtiUserExitResult (999,"Failed to initialise F3.");
		if(wfF4 == null) return new VtiUserExitResult (999,"Failed to initialise F4.");
		if(wfF5 == null) return new VtiUserExitResult (999,"Failed to initialise F5.");
		if(wfF6 == null) return new VtiUserExitResult (999,"Failed to initialise F6.");
		if(wfF7 == null) return new VtiUserExitResult (999,"Failed to initialise F7.");
		if(wfF8 == null) return new VtiUserExitResult (999,"Failed to initialise F8.");
		if(wfF9 == null) return new VtiUserExitResult (999,"Failed to initialise F9.");
		if(wfF10 == null) return new VtiUserExitResult (999,"Failed to initialise F10.");
		if(wfF11 == null) return new VtiUserExitResult (999,"Failed to initialise F11.");
		if(wfF12 == null) return new VtiUserExitResult (999,"Failed to initialise F12.");
		if(wfF13 == null) return new VtiUserExitResult (999,"Failed to initialise F13.");
		if(wfF14 == null) return new VtiUserExitResult (999,"Failed to initialise F14.");
		if(wfF15 == null) return new VtiUserExitResult (999,"Failed to initialise F15.");
		if(wfF16 == null) return new VtiUserExitResult (999,"Failed to initialise F16.");
		if(wfF17 == null) return new VtiUserExitResult (999,"Failed to initialise F17.");
		if(wfF18 == null) return new VtiUserExitResult (999,"Failed to initialise F18.");
		if(wfF19 == null) return new VtiUserExitResult (999,"Failed to initialise F19.");
		if(wfF20 == null) return new VtiUserExitResult (999,"Failed to initialise F20.");
		if(wfF21 == null) return new VtiUserExitResult (999,"Failed to initialise F21.");
		//if(wfF22 == null) return new VtiUserExitResult (999,"Failed to initialise F22.");
		if(wfC1 == null) return new VtiUserExitResult (999,"Failed to initialise C1.");
		if(wfC2 == null) return new VtiUserExitResult (999,"Failed to initialise C2.");
		if(wfC3 == null) return new VtiUserExitResult (999,"Failed to initialise C3.");
		if(wfC4 == null) return new VtiUserExitResult (999,"Failed to initialise C4.");
		if(wfC5 == null) return new VtiUserExitResult (999,"Failed to initialise C5.");
		if(wfC6 == null) return new VtiUserExitResult (999,"Failed to initialise C6.");
		if(wfC7 == null) return new VtiUserExitResult (999,"Failed to initialise C7.");
		if(wfC8 == null) return new VtiUserExitResult (999,"Failed to initialise C8.");
		if(wfC9 == null) return new VtiUserExitResult (999,"Failed to initialise C9.");
		if(wfC10 == null) return new VtiUserExitResult (999,"Failed to initialise C10.");
		if(wfC11 == null) return new VtiUserExitResult (999,"Failed to initialise C11.");
		if(wfC12 == null) return new VtiUserExitResult (999,"Failed to initialise C12.");
		if(wfC13 == null) return new VtiUserExitResult (999,"Failed to initialise C13.");
		if(wfC14 == null) return new VtiUserExitResult (999,"Failed to initialise C14.");
		if(wfC15 == null) return new VtiUserExitResult (999,"Failed to initialise C15.");
		if(wfC16 == null) return new VtiUserExitResult (999,"Failed to initialise C16.");
		if(wfC17 == null) return new VtiUserExitResult (999,"Failed to initialise C17.");
		if(wfC18 == null) return new VtiUserExitResult (999,"Failed to initialise C18.");
		if(wfC19 == null) return new VtiUserExitResult (999,"Failed to initialise C19.");
		if(wfC20 == null) return new VtiUserExitResult (999,"Failed to initialise C20.");
		if(wfC21 == null) return new VtiUserExitResult (999,"Failed to initialise C21.");
		//if(wfC22 == null) return new VtiUserExitResult (999,"Failed to initialise C22.");
		
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		if (sessionHeader == null) return new VtiUserExitResult(999, "Error Retrieving Session Header Info");
		
		DBCalls dbCall = new DBCalls();
		
		if(scrRegNo.getFieldValue().length() < 1)
			return new VtiUserExitResult(999, "Please enter Registration Number.");			

		if(scrInsp1.getFieldValue().equalsIgnoreCase("NONE") || scrInsp2.getFieldValue().equalsIgnoreCase("NONE")
		   || scrInsp3.getFieldValue().equalsIgnoreCase("NONE"))// || scrInsp4.getFieldValue().equalsIgnoreCase("NONE")  || scrInsp5.getFieldValue().equalsIgnoreCase("NONE"))
				return new VtiUserExitResult(999, "Perform full inspection.");
			
		if(scrInsp1.getFieldValue().equalsIgnoreCase("PASSED") && scrInsp2.getFieldValue().equalsIgnoreCase("PASSED")
		   && scrInsp3.getFieldValue().equalsIgnoreCase("PASSED"))//&& scrInsp4.getFieldValue().equalsIgnoreCase("PASSED") && scrInsp5.getFieldValue().equalsIgnoreCase("PASSED"))
		{
			scrInsp.setFieldValue("P");
		}
		else
		{
			scrInsp.setFieldValue("F");
		}
		//Database TBL Declaration

		VtiExitLdbTable registerLdbTable = getLocalDatabaseTable("YSWB_REGISTER");
		if (registerLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_REGISTER.");
		
		VtiExitLdbTable poHeaderCLdbTable = getLocalDatabaseTable("YSWB_PO_HEADER");
		if (poHeaderCLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_PO_HEADER.");
		
		VtiExitLdbTable statusLdbTable = getLocalDatabaseTable("YSWB_STATUS");
		if (statusLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_STATUS.");
		
		VtiExitLdbTable queueLdbTable = getLocalDatabaseTable("YSWB_QUEUE");
		if (queueLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSWB_QUEUE.");
		
		//Dataset Declaration
			
		//Archive old queue records.
		VtiExitLdbSelectCriterion [] qArcSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("Q_REGNO", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
				new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup qArcSelCondGrp = new VtiExitLdbSelectConditionGroup(qArcSelConds, true);
		VtiExitLdbTableRow[] qArcLdbRows = queueLdbTable.getMatchingRows(qArcSelCondGrp);
				
		if(qArcLdbRows.length > 0)
		{

			for(int iQ =0; iQ < qArcLdbRows.length;iQ++)
			{
				qArcLdbRows[iQ].setFieldValue("DEL_IND","X");
				qArcLdbRows[iQ].setFieldValue("TIMESTAMP","");
				
				
				try
				{
					queueLdbTable.saveRow(qArcLdbRows[iQ]);
					dbCall.ldbUpload("YSWB_QUEUE", this);
				}
				catch(VtiExitException ee)
				{
					Log.error("Queue not archived.", ee);
				}
				
			}
		}
		
		//Archive old inspections
		sErrorMsg = ArchiveInspections(Long.toString(scrWVtiRef.getLongFieldValue()),scrRegNo.getFieldValue());
		if(sErrorMsg.length() > 0)
			return new VtiUserExitResult(999, sErrorMsg);
		
		//Archive old status
		sErrorMsg = ArchiveStatus(Long.toString(scrWVtiRef.getLongFieldValue()),scrRegNo.getFieldValue());
		
		if(sErrorMsg.length() > 0)
			return new VtiUserExitResult(999, sErrorMsg);
		
		//If there is a doc #
		if(scrVbelnQt.getFieldValue().length() != 0)
		{//Register call
					
			VtiExitLdbSelectCriterion [] registerSelConds = 
			{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("DOC_NMBER", VtiExitLdbSelectCondition.EQ_OPERATOR, scrVbelnQt.getFieldValue()),
								new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWVtiRef.getFieldValue()),
									new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
										new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup registerSelCondGrp = new VtiExitLdbSelectConditionGroup(registerSelConds, true);
			VtiExitLdbTableRow[] registerLdbRows = registerLdbTable.getMatchingRows(registerSelCondGrp);
				
			if(registerLdbRows.length == 0)
				return new VtiUserExitResult(999,"Could not find this truck.");
			
			//Set reg inspection to fail.
			if(scrInsp.getFieldValue().equalsIgnoreCase("F"))
			{
				registerLdbRows[0].setFieldValue("INSPSTATUS","F");
				registerLdbRows[0].setFieldValue("TIMESTAMP", "");
				
				try
				{
					registerLdbTable.saveRow(registerLdbRows[0]);
				}
				catch (VtiExitException ee)
				{
					Log.error("Error failing the inspection.",ee);
					return new VtiUserExitResult(999,"The failed status was not written to the register.");
				}
			}
			else
			{				
				if(registerLdbRows.length > 1)
					return new VtiUserExitResult(999,"This truck is already associated with a Sales Order.");
			}
		}
		
		if(scrEbeln.getFieldValue().length() != 0)
		{
				//PO header call
				VtiExitLdbSelectCriterion [] poHeaderCSelConds = 
				{
						new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
							new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
								new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrEbeln.getFieldValue()),
									new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
      
				VtiExitLdbSelectConditionGroup poHeaderCSelCondGrp = new VtiExitLdbSelectConditionGroup(poHeaderCSelConds, true);
				VtiExitLdbTableRow[] poHeaderCLdbRows = poHeaderCLdbTable.getMatchingRows(poHeaderCSelCondGrp);
		
		
			if(poHeaderCLdbRows.length != 0)
			{	
				//set bsart for po status here
				docType = poHeaderCLdbRows[0].getFieldValue("BSART");
				//Register call
					VtiExitLdbSelectCriterion [] registerSelConds = 
					{
							new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
								new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
									new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrEbeln.getFieldValue()),
										new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWVtiRef.getFieldValue()),
											new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
												new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
					};
      
					VtiExitLdbSelectConditionGroup registerSelCondGrp = new VtiExitLdbSelectConditionGroup(registerSelConds, true);
					VtiExitLdbTableRow[] registerLdbRows = registerLdbTable.getMatchingRows(registerSelCondGrp);
				
					if(registerLdbRows.length == 0)
						return new VtiUserExitResult(999,"This truck has no relevant information regarding a Purchase Order.");
			//Remove Truck Registration from Purches Order
				if(scrInsp.getFieldValue().equalsIgnoreCase("F"))
				{
					registerLdbRows[0].setFieldValue("INSPSTATUS","F");
					registerLdbRows[0].setFieldValue("TIMESTAMP", "");

					try
					{
						registerLdbTable.saveRow(registerLdbRows[0]);
					}
					catch (VtiExitException ee)
					{
						Log.error("Error failing the inspection.",ee);
						return new VtiUserExitResult(999,"The failed status was not written to the register.");
					}
				}
				else
				{
					if(registerLdbRows.length > 1)
						return new VtiUserExitResult(999,"This truck is already associated with a Purchase Order.");
					//Add truck to the queue (on the inbound line)
					GetQ addInQ = new GetQ(this, scrEbeln.getFieldValue(),scrRegNo.getFieldValue());
					String queue = addInQ.getTruckQ();
								
					if(queue.length() == 0)
						return new VtiUserExitResult(999,"Q#: " + queue + " truck: " + scrRegNo.getFieldValue() + " Ebeln: " + scrEbeln.getFieldValue() + " time: " + registerLdbRows[0].getFieldValue("AUTIM")+ " date: " + registerLdbRows[0].getFieldValue("AUDAT"));
						
					try
					{
						interval = getNextNumberFromNumberRange("YSWB_QPOS");
					}
					catch(VtiExitException ee)
					{
						Log.error("Error creating next queue no.",ee);
						return new VtiUserExitResult(999,"Unable to generate next queue pos no.");
					}
				
					AddToQ qTruck = new AddToQ(this,scrRegNo.getFieldValue() , scrEbeln.getFieldValue()
											,false, queue, "RAW",registerLdbRows[0].getFieldValue("AUTIM"),registerLdbRows[0].getFieldValue("AUDAT")
											,interval, registerLdbRows[0].getFieldValue("DRIVER"));
				
				
				
					qTruck.addTruck2Q();
				}
			}
		}
		//No additional document availible
		if(scrVbelnQt.getFieldValue().length() == 0 )//&& soHeaderCLdbRows.length == 0 && poHeaderCLdbRows.length == 0)
		{//Register call
					
				VtiExitLdbSelectCriterion [] registerSelConds = 
				{
						new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
							new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
								new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWVtiRef.getFieldValue()),
									new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
										new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
				};
      
				VtiExitLdbSelectConditionGroup registerSelCondGrp = new VtiExitLdbSelectConditionGroup(registerSelConds, true);
				VtiExitLdbTableRow[] registerLdbRows = registerLdbTable.getMatchingRows(registerSelCondGrp);
				
				if(registerLdbRows.length == 0)
					return new VtiUserExitResult(999,"Could not find this truck.");
			//Remove Truck Registration 
			if(scrInsp.getFieldValue().equalsIgnoreCase("F"))
			{
				registerLdbRows[0].setFieldValue("INSPSTATUS","F");
				registerLdbRows[0].setFieldValue("TIMESTAMP", "");
				
				try
				{
					registerLdbTable.saveRow(registerLdbRows[0]);
				}
				catch (VtiExitException ee)
				{
					Log.error("Error failing the inspection.",ee);
					return new VtiUserExitResult(999,"The failed status was not written to the register.");
				}
			}
			else
			{				
				if(registerLdbRows.length > 1)
					return new VtiUserExitResult(999,"This truck is already associated with an Order.");
			}
		}
		
		if(scrStock.getFieldValue().length() > 0)
		{
			sErrorMsg = SetInspectionValidity(scrRegNo.getFieldValue(), scrWVtiRef.getFieldValue(), scrStock.getFieldValue(), scrShip.getFieldValue(), scrRotate.getFieldValue());
			if(sErrorMsg.length() > 0)
				return new VtiUserExitResult(999, sErrorMsg);
			
			scrTruckType.setFieldValue("STOCK");
		}
		
		if(scrRotate.getFieldValue().length() > 0 || scrTruckType.getFieldValue().equalsIgnoreCase("SCRAP"))
		{
			sErrorMsg = SetInspectionValidity(scrRegNo.getFieldValue(), scrWVtiRef.getFieldValue(), scrStock.getFieldValue(), scrShip.getFieldValue(), scrRotate.getFieldValue());
			if(sErrorMsg.length() > 0)
				return new VtiUserExitResult(999, sErrorMsg);
				
			
			VtiExitLdbSelectCriterion [] registerSelConds = 
			{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWVtiRef.getFieldValue()),
								new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
									new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup registerSelCondGrp = new VtiExitLdbSelectConditionGroup(registerSelConds, true);
			VtiExitLdbTableRow[] registerLdbRows = registerLdbTable.getMatchingRows(registerSelCondGrp);
				
			if(registerLdbRows.length == 0)
				return new VtiUserExitResult(999,"This truck has no relevant information regarding a Purchase Order.");
			//Add truck to the queue (on the inbound line)
			GetQ addInQ = new GetQ(this, "0",scrRegNo.getFieldValue());
			String queue = addInQ.getTruckQ();
								
			if(queue.length() == 0)
				return new VtiUserExitResult(999,"Q#: " + queue + " truck: " + scrRegNo.getFieldValue() + " Ebeln: " + scrEbeln.getFieldValue() + " time: " + registerLdbRows[0].getFieldValue("AUTIM")+ " date: " + registerLdbRows[0].getFieldValue("AUDAT"));
						
			try
			{
				interval = getNextNumberFromNumberRange("YSWB_QPOS");
			}
			catch(VtiExitException ee)
			{
				Log.error("Error creating next queue no.",ee);
				return new VtiUserExitResult(999,"Unable to generate next queue pos no.");
			}
				
			AddToQ qTruck = new AddToQ(this,scrRegNo.getFieldValue() , scrEbeln.getFieldValue()
									,false, queue, "RAW",registerLdbRows[0].getFieldValue("AUTIM"),registerLdbRows[0].getFieldValue("AUDAT")
									,interval, registerLdbRows[0].getFieldValue("DRIVER"));
				
			qTruck.addTruck2Q();
				
			
			scrTruckType.setFieldValue("ROTATE");
		}
		
		if(scrShip.getFieldValue().length() > 0)
		{
			sErrorMsg = SetInspectionValidity(scrRegNo.getFieldValue(), scrWVtiRef.getFieldValue(), scrStock.getFieldValue(), scrShip.getFieldValue(), scrRotate.getFieldValue());
			if(sErrorMsg.length() > 0)
				return new VtiUserExitResult(999, sErrorMsg);
			
			scrTruckType.setFieldValue("SHIP");
		}
		//Add entry to the YSWB_STATUS table
		if(scrInsp.getFieldValue().equalsIgnoreCase("P"))
		{
			//Entry for PO
			if(scrEbeln.getFieldValue().length() != 0 || 
			   scrTruckType.getFieldValue().equalsIgnoreCase("SCRAP") || 
			   scrTruckType.getFieldValue().equalsIgnoreCase("ROTATE"))
			{
				VtiExitLdbTableRow poStatus = statusLdbTable.newRow();

				poStatus.setFieldValue("SERVERGRP",getServerGroup());
				poStatus.setFieldValue("SERVERID",getServerId());
				poStatus.setFieldValue("VTIREF",scrWVtiRef.getFieldValue());
				if(!scrTruckType.getFieldValue().equalsIgnoreCase("SCRAP"))// && !scrTruckType.getFieldValue().equalsIgnoreCase("ROTATE"))
					poStatus.setFieldValue("EBELN",scrEbeln.getFieldValue());
				poStatus.setFieldValue("STATUS","A");
				poStatus.setFieldValue("WGH_STATUS","ASSIGNED");
				poStatus.setFieldValue("ARR_DATE",scrArrDate.getFieldValue());
				poStatus.setFieldValue("ARR_TIME",scrArrTime.getFieldValue());
				poStatus.setFieldValue("TRUCKREG",scrRegNo.getFieldValue());
				poStatus.setFieldValue("PREFERED",scrPref.getFieldValue());
				poStatus.setFieldValue("SHIP",scrShip.getFieldValue());
				poStatus.setFieldValue("STOCK",scrStock.getFieldValue());
				poStatus.setFieldValue("ROTATE",scrRotate.getFieldValue());
				poStatus.setFieldValue("INSP_VTI_REF",scrWVtiRef.getFieldValue());
				poStatus.setFieldValue("INSP_DATE",currLdbDate);
				poStatus.setFieldValue("INSP_TIME",scrTime.getFieldValue());
			//	poStatus.setFieldValue("USERID",sessionHeader.getUserId());
				if(!scrTruckType.getFieldValue().equalsIgnoreCase("SCRAP"))
					poStatus.setFieldValue("DOCTYPE", docType);
										   
				poStatus.setFieldValue("TIMESTAMP","");
				
				if(!scrTruckType.getFieldValue().equalsIgnoreCase("SCRAP") || !scrTruckType.getFieldValue().equalsIgnoreCase("ROTATE"))
				{
					VtiExitLdbSelectCriterion [] chngRegisterSelConds = 
					{
							new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
								new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
									new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWVtiRef.getFieldValue()),
										new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
											new VtiExitLdbSelectCondition("EBELN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrEbeln.getFieldValue()),
												new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
							
					};
      
					VtiExitLdbSelectConditionGroup chngRegisterSelCondGrp = new VtiExitLdbSelectConditionGroup(chngRegisterSelConds, true);
					VtiExitLdbTableRow[] chngRegisterLdbRows = registerLdbTable.getMatchingRows(chngRegisterSelCondGrp);
				
					if(chngRegisterLdbRows.length == 0)
						return new VtiUserExitResult(999,"Could not find this truck in the Register or Status table.");
				
					//chngRegisterLdbRows[0].setFieldValue("TRUCKTYPE",scrTruckType.getFieldValue());
					chngRegisterLdbRows[0].setFieldValue("INSPSTATUS","A");
					chngRegisterLdbRows[0].setFieldValue("TIMESTAMP", "");
					chngRegisterLdbRows[0].setFieldValue("ASSTIME",currLdbTime);
					chngRegisterLdbRows[0].setFieldValue("ASSDATE",currLdbDate);
					chngRegisterLdbRows[0].setFieldValue("ASSUSER",sessionHeader.getUserId());
				
					try
					{
						statusLdbTable.saveRow(poStatus);
						registerLdbTable.saveRow(chngRegisterLdbRows[0]);
					}
					catch(VtiExitException ee)
					{
						Log.error("Status Save Failed.",ee);
						return new VtiUserExitResult(999,"Failed to add the truck to the Status table.Tracking not possible anymore.");
					}
				}
				else
				{
					VtiExitLdbSelectCriterion [] chngRegisterSelConds = 
					{
							new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
								new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
									new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWVtiRef.getFieldValue()),
										new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),					
											new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
					};
      
					VtiExitLdbSelectConditionGroup chngRegisterSelCondGrp = new VtiExitLdbSelectConditionGroup(chngRegisterSelConds, true);
					VtiExitLdbTableRow[] chngRegisterLdbRows = registerLdbTable.getMatchingRows(chngRegisterSelCondGrp);
				
					if(chngRegisterLdbRows.length == 0)
						return new VtiUserExitResult(999,"Could not find this truck in the Register or Status table.");
				
					//chngRegisterLdbRows[0].setFieldValue("TRUCKTYPE",scrTruckType.getFieldValue());
					chngRegisterLdbRows[0].setFieldValue("INSPSTATUS","A");
					chngRegisterLdbRows[0].setFieldValue("TIMESTAMP", "");
					chngRegisterLdbRows[0].setFieldValue("ASSTIME",currLdbTime);
					chngRegisterLdbRows[0].setFieldValue("ASSDATE",currLdbDate);
					chngRegisterLdbRows[0].setFieldValue("ASSUSER",sessionHeader.getUserId());
				
					try
					{
						statusLdbTable.saveRow(poStatus);
						registerLdbTable.saveRow(chngRegisterLdbRows[0]);
					}
					catch(VtiExitException ee)
					{
						Log.error("Status Save Failed.",ee);
						return new VtiUserExitResult(999,"Failed to add the truck to the Status table.Tracking not possible anymore.");
					}
				}
			}
			
		}
	
		try
		{					
		// Trigger the uploads to SAP, if a connection is available.
				boolean hostConnected = isHostInterfaceConnected(hostName);

				if (hostConnected)
				{ 
					dbCall.ldbUpload("YSWB_REGISTER", this);
					dbCall.ldbUpload("YSWB_INSPECT", this);
				}
		}
		catch (VtiExitException ee)
		{
				Log.error("Host not connected to SAP to upload register data during Arrival save, check server.");
		}
		 scrInsp.setFieldValue("P");
		 scrVbeln.setFieldValue("");
		 scrEbeln.setFieldValue("");
		 scrRegNo.setFieldValue("");
		 scrWVtiRef.setFieldValue("");
		 scrDate.setFieldValue(currDate);
		 scrTime.setFieldValue(currTime);
		 scrTrans.setFieldValue("");
		 //scrSelf.setFieldValue("");
		// scrContract.setFieldValue("");
		 scrPref.setFieldValue("");
		 scrShip.setFieldValue("");
		 scrStock.setFieldValue("");
		 scrRotate.setFieldValue("");
		 scrVbelnQt.setFieldValue("");
		 scrGP.setFieldValue("");
		 scrInsp1.setFieldValue("NONE");
		 scrInsp2.setFieldValue("NONE");
		 scrInsp3.setFieldValue("NONE");
		 //scrInsp4.setFieldValue("NONE");
		// scrInsp5.setFieldValue("NONE");
		 
		 if(scrInsp1.getFieldValue().equalsIgnoreCase("NONE"))
			btnQ1.setFieldValue(" 01-07 ");
		else if(scrInsp1.getFieldValue().equalsIgnoreCase("PASSED"))
			btnQ1.setFieldValue("PASS 1-7  .");
		else if(scrInsp1.getFieldValue().equalsIgnoreCase("FAILED"))
			btnQ1.setFieldValue("FAIL 1-7  .");
		else
			btnQ1.setFieldValue(scrInsp1.getFieldValue());
			
		if(scrInsp2.getFieldValue().equalsIgnoreCase("NONE"))
			btnQ2.setFieldValue(" 08-14 ");
		else if(scrInsp2.getFieldValue().equalsIgnoreCase("PASSED"))
			btnQ2.setFieldValue("PASS 8-14  .");
		else if(scrInsp2.getFieldValue().equalsIgnoreCase("FAILED"))
			btnQ2.setFieldValue("FAIL 8-14  .");
		else
			btnQ2.setFieldValue(scrInsp2.getFieldValue());
			
		if(scrInsp3.getFieldValue().equalsIgnoreCase("NONE"))
			btnQ3.setFieldValue(" 15-21 ");
		else if(scrInsp3.getFieldValue().equalsIgnoreCase("PASSED"))
			btnQ3.setFieldValue("PASS 15-21 .");
		else if(scrInsp3.getFieldValue().equalsIgnoreCase("FAILED"))
			btnQ3.setFieldValue("FAIL 15-21 .");
		else
			btnQ3.setFieldValue(scrInsp3.getFieldValue());
			
		 
		 wfP1.setFieldValue("X");
		wfP2.setFieldValue("X");
		wfP3.setFieldValue("X");
		wfP4.setFieldValue("X");
		wfP5.setFieldValue("X");
		wfP6.setFieldValue("X");
		wfP7.setFieldValue("X");
		wfP8.setFieldValue("X");
		wfP9.setFieldValue("X");
		wfP10.setFieldValue("X");
		wfP11.setFieldValue("X");
		wfP12.setFieldValue("X");
		wfP13.setFieldValue("X");
		wfP14.setFieldValue("X");
		wfP15.setFieldValue("X");
		wfP16.setFieldValue("X");
		wfP17.setFieldValue("X");
		wfP18.setFieldValue("X");
		wfP19.setFieldValue("X");
		wfP20.setFieldValue("X");
		wfP21.setFieldValue("X");
//		wfP22.setFieldValue("X");
		
		wfF1.setFieldValue("");
		wfF2.setFieldValue("");
		wfF3.setFieldValue("");
		wfF4.setFieldValue("");
		wfF5.setFieldValue("");
		wfF6.setFieldValue("");
		wfF7.setFieldValue("");
		wfF8.setFieldValue("");
		wfF9.setFieldValue("");
		wfF10.setFieldValue("");
		wfF11.setFieldValue("");
		wfF12.setFieldValue("");
		wfF13.setFieldValue("");
		wfF14.setFieldValue("");
		wfF15.setFieldValue("");
		wfF16.setFieldValue("");
		wfF17.setFieldValue("");
		wfF18.setFieldValue("");
		wfF19.setFieldValue("");
		wfF20.setFieldValue("");
		wfF21.setFieldValue("");
//		wfF22.setFieldValue("");
		wfC1.setFieldValue("");
		wfC2.setFieldValue("");
		wfC3.setFieldValue("");
		wfC4.setFieldValue("");
		wfC5.setFieldValue("");
		wfC6.setFieldValue("");
		wfC7.setFieldValue("");
		wfC8.setFieldValue("");
		wfC9.setFieldValue("");
		wfC10.setFieldValue("");
		wfC11.setFieldValue("");
		wfC12.setFieldValue("");
		wfC13.setFieldValue("");
		wfC14.setFieldValue("");
		wfC15.setFieldValue("");
		wfC16.setFieldValue("");
		wfC17.setFieldValue("");
		wfC18.setFieldValue("");
		wfC19.setFieldValue("");
		wfC20.setFieldValue("");
		wfC21.setFieldValue("");
		
		scr1Field.setFieldValue("");
		scr2Field.setFieldValue("");
		scr3Field.setFieldValue("");
		scr4Field.setFieldValue("");
		scr5Field.setFieldValue("");
		scr6Field.setFieldValue("");
		scr7Field.setFieldValue("");
		scr8Field.setFieldValue("");
		scr9Field.setFieldValue("");
		scr10Field.setFieldValue("");
		scr11Field.setFieldValue("");
		scr12Field.setFieldValue("");
		scr13Field.setFieldValue("");
		scr14Field.setFieldValue("");
		scr15Field.setFieldValue("");
		scr16Field.setFieldValue("");
		scr17Field.setFieldValue("");
		scr18Field.setFieldValue("");
		scr19Field.setFieldValue("");
		scr20Field.setFieldValue("");
		scr21Field.setFieldValue("");
		//wfC22.setFieldValue("");
				
		return new VtiUserExitResult(000,"Truck inspection completed.");
	}
	
	private String SetInspectionValidity(String sRegNo, String sVti, String sStock, String sShip, String sRotate) throws VtiExitException
	{
		
		int hours = 0;		
		Calendar cal = Calendar.getInstance();
		String sErrorMsg = "";
		
		VtiExitLdbTable configLdbTable = getLocalDatabaseTable("YSWB_CONFIG");
			if (configLdbTable == null) return sErrorMsg =  "Unable to initialise table YSWB_CONFIG.";
		
			VtiExitLdbTable inspectionLdbTable = getLocalDatabaseTable("YSWB_INSPECT");
			if (inspectionLdbTable == null) return sErrorMsg = "Unable to initialise table YSWB_INSPECT.";
			
			VtiExitLdbSelectCriterion [] inspectSelConds = 
			{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, sVti),
								new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, sRegNo),
									new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup inspectSelCondGrp = new VtiExitLdbSelectConditionGroup(inspectSelConds, true);
			VtiExitLdbTableRow[] inspectLdbRows = inspectionLdbTable.getMatchingRows(inspectSelCondGrp);
					
			if(inspectLdbRows.length == 0)
				return sErrorMsg = "Could not find this truck's inspection.";
			
			VtiExitLdbSelectCriterion [] configSelConds = 
			{
					new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
						new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("KEYFIELD", VtiExitLdbSelectCondition.EQ_OPERATOR, "INSPVALID"),
								new VtiExitLdbSelectCondition("USERID", VtiExitLdbSelectCondition.EQ_OPERATOR, "*"),
									new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
			};
      
			VtiExitLdbSelectConditionGroup configSelCondGrp = new VtiExitLdbSelectConditionGroup(configSelConds, true);
			VtiExitLdbTableRow[] configLdbRows = configLdbTable.getMatchingRows(configSelCondGrp);
					
			if(configLdbRows.length == 0)
				return sErrorMsg = "Inspection validity periods not maintained in Config.";
			
			for(int i = 0; i < configLdbRows.length; i++)
			{
				if(sStock.length() > 0)
					if(configLdbRows[i].getFieldValue("KEYVAL1").equalsIgnoreCase("STOCK"))
						hours = configLdbRows[i].getIntegerFieldValue("KEYVAL2");
				
				if(sShip.length() > 0)
					if(configLdbRows[i].getFieldValue("KEYVAL1").equalsIgnoreCase("SHIP"))
						hours = configLdbRows[i].getIntegerFieldValue("KEYVAL2");
				
				if(sRotate.length() > 0)
					if(configLdbRows[i].getFieldValue("KEYVAL1").equalsIgnoreCase("ROTATE"))
						hours = configLdbRows[i].getIntegerFieldValue("KEYVAL2");
			}
			
			cal.add(Calendar.HOUR, hours);
			String sExDate = DateFormatter.format("yyyyMMdd", cal.getTime());
			String sExTime = DateFormatter.format("HHmmss", cal.getTime());
						
			inspectLdbRows[0].setFieldValue("EXPIRETIME",sExTime);
			inspectLdbRows[0].setFieldValue("EXPIREDATE",sExDate);
			inspectLdbRows[0].setFieldValue("TIMESTAMP", "");
		
			try
			{
				inspectionLdbTable.saveRow(inspectLdbRows[0]);
			}
			catch (VtiExitException ee)
			{
				Log.error("Error failing the inspection.",ee);
				return sErrorMsg = "The failed status was not written to the inspection table.";
			}
			
		return sErrorMsg;
	}
	
	private String ArchiveInspections(String Vti, String RegNo) throws VtiExitException
	{
		
		String sErrorMsg = "";
		Date currNow = new Date();
		String currLdbDate = DateFormatter.format("yyyyMMdd", currNow);
		String currLdbTime = DateFormatter.format("HHmmss", currNow);
		
		
		VtiExitLdbTable inspLdbTable = getLocalDatabaseTable("YSWB_INSPECT");
		if (inspLdbTable == null) return sErrorMsg = "Unable to initialise table YSWB_INSPECT.";
		
		
		VtiExitLdbSelectCriterion [] inspArcSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, RegNo),
							new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.NE_OPERATOR, Vti),
								new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup inspArcSelCondGrp = new VtiExitLdbSelectConditionGroup(inspArcSelConds, true);
		VtiExitLdbTableRow[] inspArcLdbRows = inspLdbTable.getMatchingRows(inspArcSelCondGrp);
 		if(inspArcLdbRows.length > 0)
		{
			for(int i =0; i < inspArcLdbRows.length;i++)
			{
					inspArcLdbRows[i].setFieldValue("DEL_IND","X");
					inspArcLdbRows[i].setFieldValue("TIMESTAMP","");				
									
				try
				{
					inspLdbTable.saveRow(inspArcLdbRows[i]);
				}
				catch(VtiExitException ee)
				{
					Log.error("Inspection not archiving.", ee);
				}
			}
		}
		
		return sErrorMsg;
	}
	
	private String ArchiveStatus(String sVti, String sRegNo) throws VtiExitException
	{
		
		String sErrorMsg = "";
		Date currNow = new Date();
		String currLdbDate = DateFormatter.format("yyyyMMdd", currNow);
		String currLdbTime = DateFormatter.format("HHmmss", currNow);
		
		
		VtiExitLdbTable statusLdbTable = getLocalDatabaseTable("YSWB_STATUS");
		if (statusLdbTable == null) return sErrorMsg = "Unable to initialise table YSWB_STATUS.";
		
		
		VtiExitLdbSelectCriterion [] statusArcSelConds = 
		{
				new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, sRegNo),
							new VtiExitLdbSelectCondition("INSP_VTI_REF", VtiExitLdbSelectCondition.NE_OPERATOR, sVti),
								new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		};
      
		VtiExitLdbSelectConditionGroup statusArcSelCondGrp = new VtiExitLdbSelectConditionGroup(statusArcSelConds, true);
		VtiExitLdbTableRow[] statusArcLdbRows = statusLdbTable.getMatchingRows(statusArcSelCondGrp);
		
		
		if(statusArcLdbRows.length > 0)
		{
			for(int i =0; i < statusArcLdbRows.length;i++)
			{
					statusArcLdbRows[i].setFieldValue("DEL_IND","X");
					statusArcLdbRows[i].setFieldValue("TIMESTAMP","");				
									
				try
				{
					statusLdbTable.saveRow(statusArcLdbRows[i]);
				}
				catch(VtiExitException ee)
				{
					Log.error("Status not archiving.", ee);
				}
			}
		}
		
		return sErrorMsg;
	}
}
