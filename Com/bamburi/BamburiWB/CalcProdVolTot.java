package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class CalcProdVolTot extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		VtiUserExitScreenField scrVol1 = getScreenField("VOLCOMP1");
		VtiUserExitScreenField scrVol2 = getScreenField("VOLCOMP2");
		VtiUserExitScreenField scrVol3 = getScreenField("VOLCOMP3");
		
		VtiUserExitScreenField scrDen1 = getScreenField("DENCOMP1");
		VtiUserExitScreenField scrDen2 = getScreenField("DENCOMP2");
		VtiUserExitScreenField scrDen3 = getScreenField("DENCOMP3");
		
		VtiUserExitScreenField scrTemp1 = getScreenField("TEMPCOMP1");
		VtiUserExitScreenField scrTemp2 = getScreenField("TEMPCOMP2");
		VtiUserExitScreenField scrTemp3 = getScreenField("TEMPCOMP3");
		
		VtiUserExitScreenField scrTempAvg = getScreenField("TEMPCOMPTOT");
		
		VtiUserExitScreenField scrDenAvg = getScreenField("DENCOMPTOT");
		
		VtiUserExitScreenField scrVolTot = getScreenField("VOLCOMPTOT");
		
		float avgDen = 0f;
		float avgTemp = 0f;
		
		
		//Vol total
		scrVolTot.setLongFieldValue(scrVol1.getLongFieldValue() 
									+ scrVol2.getLongFieldValue()
									+ scrVol3.getLongFieldValue());
		
		
		//Den Average
		if(scrDen1.getFloatFieldValue() > 0)
			avgDen++;
		
		if(scrDen2.getFloatFieldValue() > 0)
			avgDen++;
		
		if(scrDen3.getFloatFieldValue() > 0)
			avgDen++;
		
		
		if(avgDen > 0)
			scrDenAvg.setFloatFieldValue(
									(scrDen1.getFloatFieldValue() + scrDen2.getFloatFieldValue() + scrDen3.getFloatFieldValue())
									/avgDen);
		
		//Temp Avg
		if(scrTemp1.getFloatFieldValue() > 0)
			avgTemp++;
		
		if(scrTemp2.getFloatFieldValue() > 0)
			avgTemp++;
		
		if(scrTemp3.getFloatFieldValue() > 0)
			avgTemp++;
		
		if(avgTemp > 0)
			scrTempAvg.setFloatFieldValue(
									(scrTemp1.getFloatFieldValue() + scrTemp2.getFloatFieldValue() + scrTemp3.getFloatFieldValue())
									/avgTemp);
		
			
		return new VtiUserExitResult();
	}
}
