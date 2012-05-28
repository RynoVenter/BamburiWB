package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class PrintInspection extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
			
		FormatUtilities fu = new FormatUtilities();
		
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
			if (sessionHeader == null) return new VtiUserExitResult(999, "Error Retrieving Session Header Info");
		
		VtiExitLdbTable inspLdbTable = getLocalDatabaseTable("YSWB_INSPECT");
			if (inspLdbTable == null) return new VtiUserExitResult(999, "Error initialising YSWB_INSPECT");
			
		VtiUserExitScreenField scfDate  = getScreenField("DATE");
		VtiUserExitScreenField scfTime  = getScreenField("TIME");
		VtiUserExitScreenField scfTruckReg  = getScreenField("TRUCKREG");
		VtiUserExitScreenField scfPO  = getScreenField("EBELN");
		VtiUserExitScreenField scfSO  = getScreenField("VBELN");
		VtiUserExitScreenField scfTrans  = getScreenField("TRANSPORTER");
		VtiUserExitScreenField scfWFDriver  = getScreenField("DRIVER");
		VtiUserExitScreenField scfWFUserId  = getScreenField("USERID");

		VtiUserExitScreenField scfP1  = getScreenField("P1");
		VtiUserExitScreenField scfP2  = getScreenField("P2");
		VtiUserExitScreenField scfP3  = getScreenField("P3");
		VtiUserExitScreenField scfP4  = getScreenField("P4");
		VtiUserExitScreenField scfP5  = getScreenField("P5");
		VtiUserExitScreenField scfP6  = getScreenField("P6");
		VtiUserExitScreenField scfP7  = getScreenField("P7");
		VtiUserExitScreenField scfP8  = getScreenField("P8");
		VtiUserExitScreenField scfP9  = getScreenField("P9");
		VtiUserExitScreenField scfP10  = getScreenField("P10");
		VtiUserExitScreenField scfP11  = getScreenField("P11");
		VtiUserExitScreenField scfP12  = getScreenField("P12");
		VtiUserExitScreenField scfP13  = getScreenField("P13");
		VtiUserExitScreenField scfP14  = getScreenField("P14");
		VtiUserExitScreenField scfP15  = getScreenField("P15");
		VtiUserExitScreenField scfP16  = getScreenField("P16");
		VtiUserExitScreenField scfP17  = getScreenField("P17");
		VtiUserExitScreenField scfP18  = getScreenField("P18");
		VtiUserExitScreenField scfP19  = getScreenField("P19");
		VtiUserExitScreenField scfP20  = getScreenField("P20");
		VtiUserExitScreenField scfP21  = getScreenField("P21");
		VtiUserExitScreenField scfP22  = getScreenField("P22");
		VtiUserExitScreenField scfF1  = getScreenField("F1");
		VtiUserExitScreenField scfF2  = getScreenField("F2");
		VtiUserExitScreenField scfF3  = getScreenField("F3");
		VtiUserExitScreenField scfF4  = getScreenField("F4");
		VtiUserExitScreenField scfF5  = getScreenField("F5");
		VtiUserExitScreenField scfF6  = getScreenField("F6");
		VtiUserExitScreenField scfF7  = getScreenField("F7");
		VtiUserExitScreenField scfF8  = getScreenField("F8");
		VtiUserExitScreenField scfF9  = getScreenField("F9");
		VtiUserExitScreenField scfF10  = getScreenField("F10");
		VtiUserExitScreenField scfF11  = getScreenField("F11");
		VtiUserExitScreenField scfF12  = getScreenField("F12");
		VtiUserExitScreenField scfF13  = getScreenField("F13");
		VtiUserExitScreenField scfF14  = getScreenField("F14");
		VtiUserExitScreenField scfF15  = getScreenField("F15");
		VtiUserExitScreenField scfF16  = getScreenField("F16");
		VtiUserExitScreenField scfF17  = getScreenField("F17");
		VtiUserExitScreenField scfF18  = getScreenField("F18");
		VtiUserExitScreenField scfF19  = getScreenField("F19");
		VtiUserExitScreenField scfF20  = getScreenField("F20");
		VtiUserExitScreenField scfF21  = getScreenField("F21");
		VtiUserExitScreenField scfF22  = getScreenField("F22");
		
		VtiUserExitScreenField scfC1  = getScreenField("C1");
		VtiUserExitScreenField scfC2  = getScreenField("C2");
		VtiUserExitScreenField scfC3  = getScreenField("C3");
		VtiUserExitScreenField scfC4  = getScreenField("C4");
		VtiUserExitScreenField scfC5  = getScreenField("C5");
		VtiUserExitScreenField scfC6  = getScreenField("C6");
		VtiUserExitScreenField scfC7  = getScreenField("C7");
		VtiUserExitScreenField scfC8  = getScreenField("C8");
		VtiUserExitScreenField scfC9  = getScreenField("C9");
		VtiUserExitScreenField scfC10  = getScreenField("C10");
		VtiUserExitScreenField scfC11  = getScreenField("C11");
		VtiUserExitScreenField scfC12  = getScreenField("C12");
		VtiUserExitScreenField scfC13  = getScreenField("C13");
		VtiUserExitScreenField scfC14  = getScreenField("C14");
		VtiUserExitScreenField scfC15  = getScreenField("C15");
		VtiUserExitScreenField scfC16  = getScreenField("C16");
		VtiUserExitScreenField scfC17  = getScreenField("C17");
		VtiUserExitScreenField scfC18  = getScreenField("C18");
		VtiUserExitScreenField scfC19  = getScreenField("C19");
		VtiUserExitScreenField scfC20  = getScreenField("C20");
		VtiUserExitScreenField scfC21  = getScreenField("C21");
		VtiUserExitScreenField scfC22  = getScreenField("C22");

		String type = "";
		String refDoc = "";

		if(scfPO.getFieldValue().length() > 0)
		{
			type = "Purchase";
			refDoc = scfPO.getFieldValue();
		}
		else
		{
			type = "Sales";
			refDoc = scfSO.getFieldValue();
		}

		VtiExitKeyValuePair[] keyValuePairs = 
			{
				new VtiExitKeyValuePair("&Date&", fu.shortDate(scfDate.getFieldValue())),
				new VtiExitKeyValuePair("&Time&", fu.shortTime(scfTime.getFieldValue())),
				new VtiExitKeyValuePair("&RegNo&", scfTruckReg.getFieldValue()),
				new VtiExitKeyValuePair("&Trans&", scfTrans.getFieldValue()),
				new VtiExitKeyValuePair("&Type&", type),
				new VtiExitKeyValuePair("&RefDoc&", refDoc),
				new VtiExitKeyValuePair("&P1&", scfP1.getFieldValue()),
				new VtiExitKeyValuePair("&P2&", scfP2.getFieldValue()),
				new VtiExitKeyValuePair("&P3&", scfP3.getFieldValue()),
				new VtiExitKeyValuePair("&P4&", scfP4.getFieldValue()),
				new VtiExitKeyValuePair("&P5&", scfP5.getFieldValue()),
				new VtiExitKeyValuePair("&P6&", scfP6.getFieldValue()),
				new VtiExitKeyValuePair("&P7&", scfP7.getFieldValue()),
				new VtiExitKeyValuePair("&P8&", scfP8.getFieldValue()),
				new VtiExitKeyValuePair("&P9&", scfP9.getFieldValue()),
				new VtiExitKeyValuePair("&P10&", scfP10.getFieldValue()),
				new VtiExitKeyValuePair("&P11&", scfP11.getFieldValue()),
				new VtiExitKeyValuePair("&P12&", scfP12.getFieldValue()),
				new VtiExitKeyValuePair("&P13&", scfP13.getFieldValue()),
				new VtiExitKeyValuePair("&P14&", scfP14.getFieldValue()),
				new VtiExitKeyValuePair("&P15&", scfP15.getFieldValue()),
				new VtiExitKeyValuePair("&P16&", scfP16.getFieldValue()),
				new VtiExitKeyValuePair("&P17&", scfP17.getFieldValue()),
				new VtiExitKeyValuePair("&P18&", scfP18.getFieldValue()),
				new VtiExitKeyValuePair("&P19&", scfP19.getFieldValue()),
				new VtiExitKeyValuePair("&P20&", scfP20.getFieldValue()),
				new VtiExitKeyValuePair("&P21&", scfP21.getFieldValue()),
				new VtiExitKeyValuePair("&P22&", scfP22.getFieldValue()),
				new VtiExitKeyValuePair("&F1&", scfF1.getFieldValue()),
				new VtiExitKeyValuePair("&F2&", scfF2.getFieldValue()),
				new VtiExitKeyValuePair("&F3&", scfF3.getFieldValue()),
				new VtiExitKeyValuePair("&F4&", scfF4.getFieldValue()),
				new VtiExitKeyValuePair("&F5&", scfF5.getFieldValue()),
				new VtiExitKeyValuePair("&F6&", scfF6.getFieldValue()),
				new VtiExitKeyValuePair("&F7&", scfF7.getFieldValue()),
				new VtiExitKeyValuePair("&F8&", scfF8.getFieldValue()),
				new VtiExitKeyValuePair("&F9&", scfF9.getFieldValue()),
				new VtiExitKeyValuePair("&F10&", scfF10.getFieldValue()),
				new VtiExitKeyValuePair("&F11&", scfF11.getFieldValue()),
				new VtiExitKeyValuePair("&F12&", scfF12.getFieldValue()),
				new VtiExitKeyValuePair("&F13&", scfF13.getFieldValue()),
				new VtiExitKeyValuePair("&F14&", scfF14.getFieldValue()),
				new VtiExitKeyValuePair("&F15&", scfF15.getFieldValue()),
				new VtiExitKeyValuePair("&F16&", scfF16.getFieldValue()),
				new VtiExitKeyValuePair("&F17&", scfF17.getFieldValue()),
				new VtiExitKeyValuePair("&F18&", scfF18.getFieldValue()),
				new VtiExitKeyValuePair("&F19&", scfF19.getFieldValue()),
				new VtiExitKeyValuePair("&F20&", scfF20.getFieldValue()),
				new VtiExitKeyValuePair("&F21&", scfF21.getFieldValue()),
				new VtiExitKeyValuePair("&F22&", scfF22.getFieldValue()),
				new VtiExitKeyValuePair("&C1&", scfC1.getFieldValue()),
				new VtiExitKeyValuePair("&C2&", scfC2.getFieldValue()),
				new VtiExitKeyValuePair("&C3&", scfC3.getFieldValue()),
				new VtiExitKeyValuePair("&C4&", scfC4.getFieldValue()),
				new VtiExitKeyValuePair("&C5&", scfC5.getFieldValue()),
				new VtiExitKeyValuePair("&C6&", scfC6.getFieldValue()),
				new VtiExitKeyValuePair("&C7&", scfC7.getFieldValue()),
				new VtiExitKeyValuePair("&C8&", scfC8.getFieldValue()),
				new VtiExitKeyValuePair("&C9&", scfC9.getFieldValue()),
				new VtiExitKeyValuePair("&C10&", scfC10.getFieldValue()),
				new VtiExitKeyValuePair("&C11&", scfC11.getFieldValue()),
				new VtiExitKeyValuePair("&C12&", scfC12.getFieldValue()),
				new VtiExitKeyValuePair("&C13&", scfC13.getFieldValue()),
				new VtiExitKeyValuePair("&C14&", scfC14.getFieldValue()),
				new VtiExitKeyValuePair("&C15&", scfC15.getFieldValue()),
				new VtiExitKeyValuePair("&C16&", scfC16.getFieldValue()),
				new VtiExitKeyValuePair("&C17&", scfC17.getFieldValue()),
				new VtiExitKeyValuePair("&C18&", scfC18.getFieldValue()),
				new VtiExitKeyValuePair("&C19&", scfC19.getFieldValue()),
				new VtiExitKeyValuePair("&C20&", scfC20.getFieldValue()),
				new VtiExitKeyValuePair("&C21&", scfC21.getFieldValue()),
				new VtiExitKeyValuePair("&C22&", scfC22.getFieldValue()),
				new VtiExitKeyValuePair("&user&", sessionHeader.getUserId()),
				new VtiExitKeyValuePair("&driver&", scfWFDriver.getFieldValue()),
			};
					
			VtiUserExitHeaderInfo headerInfo = getHeaderInfo();		
			int deviceNumber = headerInfo.getDeviceNumber();
			//Invoking the print
			try
			{
				invokePrintTemplate("InspRep" + deviceNumber, keyValuePairs);
			}
			catch (VtiExitException ee)
			{
				Log.error("Error with Printout", ee);
				return new VtiUserExitResult(999, "Printout Failed.");
			}
			
		return new VtiUserExitResult();
	}
}
