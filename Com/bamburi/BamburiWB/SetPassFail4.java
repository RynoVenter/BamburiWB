package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;


public class SetPassFail4 extends VtiUserExit
{	
	public VtiUserExitResult execute() throws VtiExitException
	{
		VtiUserExitScreenField F16Field = getScreenField("F16");
		VtiUserExitScreenField F17Field = getScreenField("F17");
		VtiUserExitScreenField F18Field = getScreenField("F18");
		VtiUserExitScreenField F19Field = getScreenField("F19");
		VtiUserExitScreenField F20Field = getScreenField("F20");

		VtiUserExitScreenField StatusField = getScreenField("STATUS4");

		VtiUserExitScreenField PassFailField = getScreenField("INSPSTATUS");
		VtiUserExitScreenField scrFStatus = getScreenField("STATUS");
		
		scrFStatus.setFieldValue("PASSED");
		
		StatusField.setFieldValue("PASSED");
		
		PassFailField.setFieldValue("P");

        if(F16Field.getFieldValue().equalsIgnoreCase("X"))
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
    		StatusField.setFieldValue("FAILED");											
		}
        if(F17Field.getFieldValue().equalsIgnoreCase("X"))
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
    		StatusField.setFieldValue("FAILED");											
		}
        if(F18Field.getFieldValue().equalsIgnoreCase("X"))
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
    		StatusField.setFieldValue("FAILED");											
		}
        if(F19Field.getFieldValue().equalsIgnoreCase("X"))
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
    		StatusField.setFieldValue("FAILED");								
		}
		if(F20Field.getFieldValue().equalsIgnoreCase("X"))
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
    		StatusField.setFieldValue("FAILED");								
		}
           		
		return new VtiUserExitResult();
	}
}

