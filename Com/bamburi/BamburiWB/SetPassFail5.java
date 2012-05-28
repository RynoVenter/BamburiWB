package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;


public class SetPassFail5 extends VtiUserExit
{	
	public VtiUserExitResult execute() throws VtiExitException
	{
		VtiUserExitScreenField F21Field = getScreenField("F21");

		VtiUserExitScreenField StatusField = getScreenField("STATUS5");

		VtiUserExitScreenField PassFailField = getScreenField("INSPSTATUS");
		VtiUserExitScreenField scrFStatus = getScreenField("STATUS");
		
		scrFStatus.setFieldValue("PASSED");
		
		StatusField.setFieldValue("PASSED");
		
		PassFailField.setFieldValue("P");

        if(F21Field.getFieldValue().equalsIgnoreCase("X"))
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
    		StatusField.setFieldValue("FAILED");											
		}

		return new VtiUserExitResult();
	}
}

