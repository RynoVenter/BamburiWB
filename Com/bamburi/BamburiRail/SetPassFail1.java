package com.bamburi.bamburirail;


import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;


public class SetPassFail1 extends VtiUserExit
{	
	public VtiUserExitResult execute() throws VtiExitException
	{
		VtiUserExitScreenField F1Field = getScreenField("F1");
		VtiUserExitScreenField F2Field = getScreenField("F2");
		VtiUserExitScreenField F3Field = getScreenField("F3");
		VtiUserExitScreenField F4Field = getScreenField("F4");
		VtiUserExitScreenField F5Field = getScreenField("F5");
		
		VtiUserExitScreenField StatusField = getScreenField("STATUS1");
	   

		VtiUserExitScreenField PassFailField = getScreenField("INSPSTATUS");
		VtiUserExitScreenField scrFStatus = getScreenField("STATUS");
		
		scrFStatus.setFieldValue("PASSED");
		
		PassFailField.setFieldValue("P");
		
		StatusField.setFieldValue("PASSED");

        if(F1Field.getFieldValue().equalsIgnoreCase("X"))
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
    		StatusField.setFieldValue("FAILED");		
			
		}
        if(F2Field.getFieldValue().equalsIgnoreCase("X"))
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
    		StatusField.setFieldValue("FAILED");					
		}
        if(F3Field.getFieldValue().equalsIgnoreCase("X"))
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
    		StatusField.setFieldValue("FAILED");					
		}
        if(F4Field.getFieldValue().equalsIgnoreCase("X"))
		  {	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
    		StatusField.setFieldValue("FAILED");					
		}
        if(F5Field.getFieldValue().equalsIgnoreCase("X"))
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
    		StatusField.setFieldValue("FAILED");					
		}
           		
		return new VtiUserExitResult();
	}
}

