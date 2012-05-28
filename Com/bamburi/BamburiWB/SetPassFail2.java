package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;


public class SetPassFail2 extends VtiUserExit
{	
	public VtiUserExitResult execute() throws VtiExitException
	{
		
		boolean bNoComment = false;
		
		VtiUserExitScreenField P8Field = getScreenField("P8");
		VtiUserExitScreenField P9Field = getScreenField("P9");
		VtiUserExitScreenField P10Field = getScreenField("P10");
		VtiUserExitScreenField P11Field = getScreenField("P11");
		VtiUserExitScreenField P12Field = getScreenField("P12");
		VtiUserExitScreenField P13Field = getScreenField("P13");
		VtiUserExitScreenField P14Field = getScreenField("P14");
		
		VtiUserExitScreenField scr8Field = getScreenField("8");
		VtiUserExitScreenField scr9Field = getScreenField("9");
		VtiUserExitScreenField scr10Field = getScreenField("10");
		VtiUserExitScreenField scr11Field = getScreenField("11");
		VtiUserExitScreenField scr12Field = getScreenField("12");
		VtiUserExitScreenField scr13Field = getScreenField("13");
		VtiUserExitScreenField scr14Field = getScreenField("14");
		
		VtiUserExitScreenField F8Field = getScreenField("F8");
		VtiUserExitScreenField F9Field = getScreenField("F9");
		VtiUserExitScreenField F10Field = getScreenField("F10");
		VtiUserExitScreenField F11Field = getScreenField("F11");
		VtiUserExitScreenField F12Field = getScreenField("F12");
		VtiUserExitScreenField F13Field = getScreenField("F13");
		VtiUserExitScreenField F14Field = getScreenField("F14");

		VtiUserExitScreenField C8Field = getScreenField("C8");
		VtiUserExitScreenField C9Field = getScreenField("C9");
		VtiUserExitScreenField C10Field = getScreenField("C10");
		VtiUserExitScreenField C11Field = getScreenField("C11");
		VtiUserExitScreenField C12Field = getScreenField("C12");
		VtiUserExitScreenField C13Field = getScreenField("C13");
		VtiUserExitScreenField C14Field = getScreenField("C14");		
		
		VtiUserExitScreenField StatusField = getScreenField("STATUS2");

		VtiUserExitScreenField PassFailField = getScreenField("INSPSTATUS");
		VtiUserExitScreenField scrFStatus = getScreenField("STATUS");
		
		scrFStatus.setFieldValue("PASSED");
		
		StatusField.setFieldValue("PASSED");
		
		PassFailField.setFieldValue("P");

        if(F8Field.getFieldValue().equalsIgnoreCase("X"))
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
			StatusField.setFieldValue("FAILED");
			
			
			 if(C8Field.getFieldValue().length() == 0 && !scr8Field.getFieldValue().equalsIgnoreCase("X"))
				return new VtiUserExitResult(999,1,"Please comment on why failing point 8.",C8Field,null);
			 
			 scr8Field.setFieldValue("X");
		}
		else if(C8Field.getFieldValue().length() > 0 && !F8Field.getFieldValue().equalsIgnoreCase("X") && !scr8Field.getFieldValue().equalsIgnoreCase("X"))
		{
			F8Field.setFieldValue("X");
			P8Field.setFieldValue("");
			scr8Field.setFieldValue("X");
			bNoComment = true;
		}
		
        if(F9Field.getFieldValue().equalsIgnoreCase("X"))
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
    		StatusField.setFieldValue("FAILED");
			
			
			 if(C9Field.getFieldValue().length() == 0 && !scr9Field.getFieldValue().equalsIgnoreCase("X"))
				return new VtiUserExitResult(999,1,"Please comment on why failing point 9.",C9Field,null);
			 
			 scr9Field.setFieldValue("X");
		}
		else if(C9Field.getFieldValue().length() > 0 && !F9Field.getFieldValue().equalsIgnoreCase("X") && !scr9Field.getFieldValue().equalsIgnoreCase("X"))
		{
			F9Field.setFieldValue("X");
			P9Field.setFieldValue("");
			scr9Field.setFieldValue("X");
			bNoComment = true;
		}
		
        if(F10Field.getFieldValue().equalsIgnoreCase("X"))
		  {	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
    		StatusField.setFieldValue("FAILED");
			
			
			 if(C10Field.getFieldValue().length() == 0 && !scr10Field.getFieldValue().equalsIgnoreCase("X"))
				return new VtiUserExitResult(999,1,"Please comment on why failing point 10.",C10Field,null);
			 
			 scr10Field.setFieldValue("X");
		}
		else if(C10Field.getFieldValue().length() > 0  && !F10Field.getFieldValue().equalsIgnoreCase("X")&& !scr10Field.getFieldValue().equalsIgnoreCase("X"))
		{
			F10Field.setFieldValue("X");
			P10Field.setFieldValue("");
			scr10Field.setFieldValue("X");
			bNoComment = true;
		}
		
		if(F11Field.getFieldValue().equalsIgnoreCase("X"))
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
    		StatusField.setFieldValue("FAILED");	
			
			
			 if(C11Field.getFieldValue().length() == 0 && !scr11Field.getFieldValue().equalsIgnoreCase("X"))
				return new VtiUserExitResult(999,1,"Please comment on why failing point 11.",C11Field,null);
			 
			 scr11Field.setFieldValue("X");
		}
		else if(C11Field.getFieldValue().length() > 0 && !F11Field.getFieldValue().equalsIgnoreCase("X") && !scr11Field.getFieldValue().equalsIgnoreCase("X"))
		{
			F11Field.setFieldValue("X");
			P11Field.setFieldValue("");
			scr11Field.setFieldValue("X");
			bNoComment = true;
		}
		
        if(F12Field.getFieldValue().equalsIgnoreCase("X"))
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
    		StatusField.setFieldValue("FAILED");	
			
			
			 if(C12Field.getFieldValue().length() == 0 && !scr12Field.getFieldValue().equalsIgnoreCase("X"))
				return new VtiUserExitResult(999,1,"Please comment on why failing point 12.",C12Field,null);
			 
			 scr12Field.setFieldValue("X");
		}
		else if(C12Field.getFieldValue().length() > 0 && !F12Field.getFieldValue().equalsIgnoreCase("X") && !scr12Field.getFieldValue().equalsIgnoreCase("X"))
		{
			F12Field.setFieldValue("X");
			P12Field.setFieldValue("");
			scr12Field.setFieldValue("X");
			bNoComment = true;
		}
		
		if(F13Field.getFieldValue().equalsIgnoreCase("X"))
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
    		StatusField.setFieldValue("FAILED");
			
			
			 if(C13Field.getFieldValue().length() == 0 && !scr13Field.getFieldValue().equalsIgnoreCase("X"))
				return new VtiUserExitResult(999,1,"Please comment on why failing point 13.",C13Field,null);
			 
			 scr13Field.setFieldValue("X");
		}
		else if(C13Field.getFieldValue().length() > 0 && !F13Field.getFieldValue().equalsIgnoreCase("X") && !scr13Field.getFieldValue().equalsIgnoreCase("X"))
		{
			F13Field.setFieldValue("X");
			P13Field.setFieldValue("");
			scr13Field.setFieldValue("X");
			bNoComment = true;
		}
		
        if(F14Field.getFieldValue().equalsIgnoreCase("X"))
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
    		StatusField.setFieldValue("FAILED");
			
			
			 if(C14Field.getFieldValue().length() == 0 && !scr14Field.getFieldValue().equalsIgnoreCase("X"))
				return new VtiUserExitResult(999,1,"Please comment on why failing point 14.",C14Field,null);
			 
			 scr14Field.setFieldValue("X");
		}
		else if(C14Field.getFieldValue().length() > 0 && !F14Field.getFieldValue().equalsIgnoreCase("X") && !scr14Field.getFieldValue().equalsIgnoreCase("X"))
		{
			F14Field.setFieldValue("X");
			P14Field.setFieldValue("");
			scr14Field.setFieldValue("X");
			bNoComment = true;
		}
        
		if(bNoComment)
			return new VtiUserExitResult(999,1,"Please set the inspection to fail when commenting.");
				
		return new VtiUserExitResult();
	}
}

