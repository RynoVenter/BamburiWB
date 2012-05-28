package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class CheckInspStatus extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		
		//Screen Fields
		VtiUserExitScreenField scrInsp = getScreenField("INSPSTATUS");
		VtiUserExitScreenField scrDate = getScreenField("DATE");
		VtiUserExitScreenField scrTime = getScreenField("TIME");
		VtiUserExitScreenField scrRegNo = getScreenField("REGNO");
		VtiUserExitScreenField scrInsp1 = getScreenField("STATUS1");
		VtiUserExitScreenField scrInsp2 = getScreenField("STATUS2");
		VtiUserExitScreenField scrInsp3 = getScreenField("STATUS3");
		//VtiUserExitScreenField scrInsp4 = getScreenField("STATUS4");
		//VtiUserExitScreenField scrInsp5 = getScreenField("STATUS5");
		VtiUserExitScreenField scrPref = getScreenField("PREF");
		VtiUserExitScreenField scrShip = getScreenField("CHKSHIP");
		VtiUserExitScreenField scrStockpile = getScreenField("CHKSTOCKPILE");
		VtiUserExitScreenField scrRotate = getScreenField("CHKROTATE");
		VtiUserExitScreenField scrPO = getScreenField("EBELN");
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
		
		VtiUserExitScreenField P1Field = getScreenField("P1");
		VtiUserExitScreenField P2Field = getScreenField("P2");
		VtiUserExitScreenField P3Field = getScreenField("P3");
		VtiUserExitScreenField P4Field = getScreenField("P4");
		VtiUserExitScreenField P5Field = getScreenField("P5");
		VtiUserExitScreenField P6Field = getScreenField("P6");
		VtiUserExitScreenField P7Field = getScreenField("P7");
		
		VtiUserExitScreenField F1Field = getScreenField("F1");
		VtiUserExitScreenField F2Field = getScreenField("F2");
		VtiUserExitScreenField F3Field = getScreenField("F3");
		VtiUserExitScreenField F4Field = getScreenField("F4");
		VtiUserExitScreenField F5Field = getScreenField("F5");
		VtiUserExitScreenField F6Field = getScreenField("F6");
		VtiUserExitScreenField F7Field = getScreenField("F7");
		
		//Screenfield Validation
		if (scrInsp == null) return new VtiUserExitResult(999, "Unable to initialise screen field INSPSTATUS.");
		if (scrInsp1 == null) return new VtiUserExitResult(999, "Unable to initialise screen field STATUS1.");
		if (scrInsp2 == null) return new VtiUserExitResult(999, "Unable to initialise screen field STATUS2.");
		if (scrInsp3 == null) return new VtiUserExitResult(999, "Unable to initialise screen field STATUS3.");
		//if (scrInsp4 == null) return new VtiUserExitResult(999, "Unable to initialise screen field STATUS4.");
		//if (scrInsp5 == null) return new VtiUserExitResult(999, "Unable to initialise screen field STATUS5.");
		if (scrRegNo == null) return new VtiUserExitResult(999, "Unable to initialise screen field REGNO.");
		if (scrPref == null) return new VtiUserExitResult(999, "Unable to initialise screen field PREF.");
		if (scrShip == null) return new VtiUserExitResult(999, "Unable to initialise screen field CHKSHIP.");
		if (scrStockpile == null) return new VtiUserExitResult(999, "Unable to initialise screen field CHKSTOCKPILE.");
		if (scrRotate == null) return new VtiUserExitResult(999, "Unable to initialise screen field CHKROTATE.");
		if (scrPO == null) return new VtiUserExitResult(999, "Unable to initialise screen field EBELN.");
		if (scrTime == null) return new VtiUserExitResult(999, "Unable to initialise screen field TIME.");
		if (scrDate == null) return new VtiUserExitResult(999, "Unable to initialise screen field DATE.");


		if(	scrInsp1.getFieldValue().equalsIgnoreCase("NONE"))
		{
			 P1Field.setFieldValue("X"); 
			 P2Field.setFieldValue("X");
			 P3Field.setFieldValue("X");
			 P4Field.setFieldValue("X");
			 P5Field.setFieldValue("X");
			 P6Field.setFieldValue("X");
			 P7Field.setFieldValue("X");
			 
			 F1Field.setFieldValue(""); 
			 F2Field.setFieldValue(""); 
			 F3Field.setFieldValue("");  
			 F4Field.setFieldValue("");  
			 F5Field.setFieldValue("");  
			 F6Field.setFieldValue("");  
			 F7Field.setFieldValue("");
		 
		 }
				
		if(scrDate.getFieldValue().length() == 0 || scrTime.getFieldValue().length() == 0)
		{
			Date currNow = new Date();
		
			String currTime = DateFormatter.format("HH:mm:ss", currNow);
			String currDate = DateFormatter.format("dd/MM/yyyy", currNow);
			
			scrDate.setFieldValue(currDate);
			scrTime.setFieldValue(currTime);
		}
		
		
			if((scrInsp1.getFieldValue().equalsIgnoreCase("NONE") 
				|| scrInsp2.getFieldValue().equalsIgnoreCase("NONE")
				|| scrInsp3.getFieldValue().equalsIgnoreCase("NONE"))
			  &&
			   (!scrInsp1.getFieldValue().equalsIgnoreCase("NONE") 
				&& !scrInsp2.getFieldValue().equalsIgnoreCase("NONE")
				&& !scrInsp3.getFieldValue().equalsIgnoreCase("NONE"))) //|| scrInsp4.getFieldValue().equalsIgnoreCase("NONE"))
				if(scrRegNo.getFieldValue().length() != 0)
					return new VtiUserExitResult(500, "No inspection was performed.");
			
			if(scrInsp1.getFieldValue().equalsIgnoreCase("PASSED") && scrInsp2.getFieldValue().equalsIgnoreCase("PASSED")
			   && scrInsp3.getFieldValue().equalsIgnoreCase("PASSED"))// && scrInsp4.getFieldValue().equalsIgnoreCase("PASSED"))
				{
					scrInsp.setFieldValue("P");
				}
				else
				{
					scrInsp.setFieldValue("F");
				}
						
			if(scrPO.getFieldValue().length() > 0)
			{
				scrPref.setDisplayOnlyFlag(false);
				scrShip.setDisplayOnlyFlag(false);
				scrRotate.setDisplayOnlyFlag(false);
				scrStockpile.setDisplayOnlyFlag(false);
				scrRotate.setFieldValue("");
			}
			else
			{
				scrPref.setDisplayOnlyFlag(true);
				scrShip.setDisplayOnlyFlag(true);
				scrStockpile.setDisplayOnlyFlag(true);
				scrRotate.setDisplayOnlyFlag(false);
				scrPref.setFieldValue("");
				scrShip.setFieldValue("");
				scrStockpile.setFieldValue("");
				scrRotate.setFieldValue("");
			}
			
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

		return new VtiUserExitResult();
	}
}
