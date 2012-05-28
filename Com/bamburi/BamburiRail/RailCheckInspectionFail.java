package com.bamburi.bamburirail;


import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class RailCheckInspectionFail extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		/*In the event that a truck fails inspection, remove the registration from the po and so headers, thus making that po or so availible for 
		pick up with another truck.
		*/
		
		//Declarations of variables and elements. Followed by the checking of the elements.

		VtiUserExitScreenField scrInsp = getScreenField("INSPSTATUS");
		VtiUserExitScreenField scrWVtiRef = getScreenField("VTI_REF");
		VtiUserExitScreenField scrWagVtiRef = getScreenField("WVTI_REF");
		VtiUserExitScreenField scrTime = getScreenField("TIME");
		VtiUserExitScreenField scrDate = getScreenField("DATE");
		VtiUserExitScreenField scrArrDate = getScreenField("ARR_DATE");
		VtiUserExitScreenField scrArrTime = getScreenField("ARR_TIME");
		VtiUserExitScreenField scrSelf = getScreenField("SELF");
		VtiUserExitScreenField scrContract = getScreenField("CONTRACTOR");
		VtiUserExitScreenField scrRegNo = getScreenField("REGNO");
		VtiUserExitScreenField scrInsp1 = getScreenField("STATUS1");
		VtiUserExitScreenField scrGP = getScreenField("GATEPASS");
		VtiUserExitScreenField scrWagNo = getScreenField("WAGNO");
		VtiUserExitScreenField scrTransp = getScreenField("TRANSPORTER");
		
		VtiUserExitScreenField wfP1 = getScreenField("P1");
		VtiUserExitScreenField wfP2 = getScreenField("P2");
		VtiUserExitScreenField wfP3 = getScreenField("P3");
		VtiUserExitScreenField wfP4 = getScreenField("P4");
		VtiUserExitScreenField wfP5 = getScreenField("P5");
/*		VtiUserExitScreenField wfP6 = getScreenField("P6");
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
		VtiUserExitScreenField wfC19 = getScreenField("C19");*/
		
		
		if (scrInsp == null) return new VtiUserExitResult(999, "Unable to initialise screen field INSPSTATUS.");
		if (scrInsp1 == null) return new VtiUserExitResult(999, "Unable to initialise screen field STATUS1.");
		if (scrSelf == null) return new VtiUserExitResult(999, "Unable to initialise screen field REGNO.");
		if (scrContract == null) return new VtiUserExitResult(999, "Unable to initialise screen field SELF.");
		if (scrRegNo == null) return new VtiUserExitResult(999, "Unable to initialise screen field CONTRACTOR.");
		if (scrWVtiRef == null) return new VtiUserExitResult(999, "Unable to initialise screen field VTIREF.");
		if (scrDate == null) return new VtiUserExitResult(999, "Unable to initialise screen field DATE.");
		if (scrTime == null) return new VtiUserExitResult(999, "Unable to initialise screen field TIME.");
		if (scrArrDate == null) return new VtiUserExitResult(999, "Unable to initialise screen field ARR_DATE.");
		if (scrArrTime == null) return new VtiUserExitResult(999, "Unable to initialise screen field ARR_TIME.");
		if (scrGP == null) return new VtiUserExitResult(999, "Unable to initialise screen field GATEPASS.");
		if (scrWagNo == null) return new VtiUserExitResult(999, "Unable to initialise screen field WAGNO.");
		if (scrWagVtiRef == null) return new VtiUserExitResult(999, "Unable to initialise screen field WVTI_REF.");
		if (scrTransp == null) return new VtiUserExitResult(999, "Unable to initialise screen field TRANSPORTER.");
		
		if(wfP1 == null) return new VtiUserExitResult (999,"Failed to initialise P1.");
		if(wfP2 == null) return new VtiUserExitResult (999,"Failed to initialise P2.");
		if(wfP3 == null) return new VtiUserExitResult (999,"Failed to initialise P3.");
		if(wfP4 == null) return new VtiUserExitResult (999,"Failed to initialise P4.");
		if(wfP5 == null) return new VtiUserExitResult (999,"Failed to initialise P5.");
		/*if(wfP6 == null) return new VtiUserExitResult (999,"Failed to initialise P6.");
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
		if(wfC19 == null) return new VtiUserExitResult (999,"Failed to initialise C19.");*/
		
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		if (sessionHeader == null) return new VtiUserExitResult(999, "Error Retrieving Session Header Info");
		
		sessionHeader.setNextFunctionId("YSWB_MAIN");
			
		Date currNow = new Date();
		
		String currTime = DateFormatter.format("HH:mm:ss", currNow);
		String currDate = DateFormatter.format("dd/MM/yyyy", currNow);
		String currLdbDate = DateFormatter.format("yyyymmdd", currNow);
		DBCalls dbCall = new DBCalls();//Temporary, remove during go-live
		
		if(scrRegNo.getFieldValue().length() < 1)
			return new VtiUserExitResult(999, "Please enter Registration Number.");			

		if(scrInsp1.getFieldValue().equalsIgnoreCase("NONE"))
				return new VtiUserExitResult(999, "Perform full inspection.");
			
		if(scrInsp1.getFieldValue().equalsIgnoreCase("PASSED") )
		{
			scrInsp.setFieldValue("P");
		}
		else
		{
			scrInsp.setFieldValue("F");
		}
		//Database TBL Declaration
				
		VtiExitLdbTable registerLdbTable = getLocalDatabaseTable("YSWB_REGISTER");
		//Register call
					
				VtiExitLdbSelectCriterion [] registerSelConds = 
				{
						new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
							new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
								new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWagVtiRef.getFieldValue()),
									new VtiExitLdbSelectCondition("TRAIN", VtiExitLdbSelectCondition.EQ_OPERATOR, scrRegNo.getFieldValue()),
										new VtiExitLdbSelectCondition("TRUCKREG", VtiExitLdbSelectCondition.EQ_OPERATOR, scrWagNo.getFieldValue()),
											new VtiExitLdbSelectCondition("WAGON", VtiExitLdbSelectCondition.EQ_OPERATOR, "X"),
				};
      
				VtiExitLdbSelectConditionGroup registerSelCondGrp = new VtiExitLdbSelectConditionGroup(registerSelConds, true);
				VtiExitLdbTableRow[] registerLdbRows = registerLdbTable.getMatchingRows(registerSelCondGrp);
				
				if(registerLdbRows.length == 0)
					return new VtiUserExitResult(999,"Could not find this train.");
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
		
		
		try
		{					
		// Trigger the uploads to SAP, if a connection is available.
				String hostName = getHostInterfaceName();
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
		 scrRegNo.setFieldValue("");
		 scrWVtiRef.setFieldValue("");
		 scrWagVtiRef.setFieldValue("");
		 scrDate.setFieldValue(currDate);
		 scrTime.setFieldValue(currTime);
		 scrDate.setFieldValue("");
		 scrSelf.setFieldValue("");
		 scrContract.setFieldValue("");
		 scrGP.setFieldValue("");
		 scrInsp1.setFieldValue("NONE");
		 scrWagNo.clearPossibleValues();
		 scrTransp.setFieldValue("");
		 
		 wfP1.setFieldValue("X");
		wfP2.setFieldValue("X");
		wfP3.setFieldValue("X");
		wfP4.setFieldValue("X");
		wfP5.setFieldValue("X");
		/*wfP6.setFieldValue("X");
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
		wfC19.setFieldValue("");*/
		
		
		
		return new VtiUserExitResult(000,"Truck inspection completed.");
	}
}
