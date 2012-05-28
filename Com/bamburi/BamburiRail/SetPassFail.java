package com.bamburi.bamburirail;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;


public class SetPassFail extends VtiUserExit
{	
	public VtiUserExitResult execute() throws VtiExitException
	{
		VtiUserExitScreenField F1Field = getScreenField("F1");
		VtiUserExitScreenField F2Field = getScreenField("F2");
		VtiUserExitScreenField F3Field = getScreenField("F3");
		VtiUserExitScreenField F4Field = getScreenField("F4");
		VtiUserExitScreenField F5Field = getScreenField("F5");
		VtiUserExitScreenField F6Field = getScreenField("F6");
		VtiUserExitScreenField F7Field = getScreenField("F7");
		VtiUserExitScreenField F8Field = getScreenField("F8");
		VtiUserExitScreenField F9Field = getScreenField("F9");
		VtiUserExitScreenField F10Field = getScreenField("F10");
		VtiUserExitScreenField F11Field = getScreenField("F11");
		VtiUserExitScreenField F12Field = getScreenField("F12");
		VtiUserExitScreenField F13Field = getScreenField("F13");
		VtiUserExitScreenField F14Field = getScreenField("F14");
		VtiUserExitScreenField F15Field = getScreenField("F15");
		VtiUserExitScreenField F16Field = getScreenField("F16");
		VtiUserExitScreenField F17Field = getScreenField("F17");
		VtiUserExitScreenField F18Field = getScreenField("F18");
		VtiUserExitScreenField F19Field = getScreenField("F19");

		VtiUserExitScreenField PassFailField = getScreenField("INSPSTATUS");
		VtiUserExitScreenField scrFStatus = getScreenField("STATUS");
		
		scrFStatus.setFieldValue("PASSED");
		
		PassFailField.setFieldValue("P");

        if(F1Field.getFieldValue().equalsIgnoreCase("X"))
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
		}
        if(F2Field.getFieldValue().equalsIgnoreCase("X"))
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
		}
        if(F3Field.getFieldValue().equalsIgnoreCase("X"))
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
		}
        if(F4Field.getFieldValue().equalsIgnoreCase("X"))
		  {	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
		}
        if(F5Field.getFieldValue().equalsIgnoreCase("X"))
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
		}
        if(F6Field.getFieldValue().equalsIgnoreCase("X"))
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
		}
        if(F7Field.getFieldValue().equalsIgnoreCase("X"))
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
		}
        if(F8Field.getFieldValue().equalsIgnoreCase("X"))
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
		}
        if(F9Field.getFieldValue().equalsIgnoreCase("X"))
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
		}
        if(F10Field.getFieldValue().equalsIgnoreCase("X"))
		  {	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
		}
        if(F11Field.getFieldValue().equalsIgnoreCase("X"))
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
		}
        if(F12Field.getFieldValue().equalsIgnoreCase("X"))
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
		}
        if(F13Field.getFieldValue().equalsIgnoreCase("X"))
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
		}
        if(F14Field.getFieldValue().equalsIgnoreCase("X"))
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
		}
        if(F15Field.getFieldValue().equalsIgnoreCase("X"))
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
		}
        if(F16Field.getFieldValue().equalsIgnoreCase("X"))
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
		}
        if(F17Field.getFieldValue().equalsIgnoreCase("X"))
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
		}
        if(F18Field.getFieldValue().equalsIgnoreCase("X"))
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
		}
        if(F19Field.getFieldValue().equalsIgnoreCase("X"))
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
		}
           		
		return new VtiUserExitResult();
	}
}

