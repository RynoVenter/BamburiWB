package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class FormatCSC extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		VtiUserExitScreenField scrVbeln = getScreenField("VBELN");
		VtiUserExitScreenField scrEbeln = getScreenField("EBELN");
		VtiUserExitScreenField scrEbeln2 = getScreenField("EBELN2");
		VtiUserExitScreenField scrEbeln3 = getScreenField("EBELN3");
		VtiUserExitScreenField scrVbeln_S = getScreenField("VBELN_S");
		VtiUserExitScreenField scrEbeln_S = getScreenField("EBELN_S");
		VtiUserExitScreenField scrEbeln2_S = getScreenField("EBELN2_S");
		VtiUserExitScreenField scrEbeln3_S = getScreenField("EBELN3_S");
		
		if(scrVbeln == null) return new VtiUserExitResult(999,"Screen field VBELN not loaded.");
		if(scrEbeln == null) return new VtiUserExitResult(999,"Screen field EBELN not loaded.");
		if(scrEbeln2 == null) return new VtiUserExitResult(999,"Screen field EBELN2 not loaded.");
		if(scrEbeln3 == null) return new VtiUserExitResult(999,"Screen field EBELN3 not loaded.");
		if(scrVbeln_S == null) return new VtiUserExitResult(999,"Screen field VBELN_S not loaded.");
		if(scrEbeln_S == null) return new VtiUserExitResult(999,"Screen field EBELN_S not loaded.");
		if(scrEbeln2_S == null) return new VtiUserExitResult(999,"Screen field EBELN2_S not loaded.");
		if(scrEbeln3_S == null) return new VtiUserExitResult(999,"Screen field EBELN3_S not loaded.");
				
		scrVbeln.setFieldValue(scrVbeln_S.getFieldValue());
		scrEbeln.setFieldValue(scrEbeln_S.getFieldValue());
		scrEbeln2.setFieldValue(scrEbeln2_S.getFieldValue());
		scrEbeln3.setFieldValue(scrEbeln3_S.getFieldValue());
				
		scrVbeln_S.setFieldValue(""); 
		scrEbeln_S.setFieldValue("");  
		scrEbeln2_S.setFieldValue("");   
		scrEbeln3_S.setFieldValue("");   
		
		
		
		return new VtiUserExitResult();
	}
}
