package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;


public class SetPassFail3 extends VtiUserExit
{	
	public VtiUserExitResult execute() throws VtiExitException
	{
		
		boolean bNoComment = false;

		VtiUserExitScreenField P15Field = getScreenField("P15");
		VtiUserExitScreenField P16Field = getScreenField("P16");
		VtiUserExitScreenField P17Field = getScreenField("P17");
		VtiUserExitScreenField P18Field = getScreenField("P18");
		VtiUserExitScreenField P19Field = getScreenField("P19");
		VtiUserExitScreenField P20Field = getScreenField("P20");
		VtiUserExitScreenField P21Field = getScreenField("P21");
		
		VtiUserExitScreenField scr15Field = getScreenField("15");
		VtiUserExitScreenField scr16Field = getScreenField("16");
		VtiUserExitScreenField scr17Field = getScreenField("17");
		VtiUserExitScreenField scr18Field = getScreenField("18");
		VtiUserExitScreenField scr19Field = getScreenField("19");
		VtiUserExitScreenField scr20Field = getScreenField("20");
		VtiUserExitScreenField scr21Field = getScreenField("21");
		
		VtiUserExitScreenField F15Field = getScreenField("F15");
		VtiUserExitScreenField F16Field = getScreenField("F16");
		VtiUserExitScreenField F17Field = getScreenField("F17");
		VtiUserExitScreenField F18Field = getScreenField("F18");
		VtiUserExitScreenField F19Field = getScreenField("F19");
		VtiUserExitScreenField F20Field = getScreenField("F20");
		VtiUserExitScreenField F21Field = getScreenField("F21");
		
		VtiUserExitScreenField C15Field = getScreenField("C15");
		VtiUserExitScreenField C16Field = getScreenField("C16");
		VtiUserExitScreenField C17Field = getScreenField("C17");
		VtiUserExitScreenField C18Field = getScreenField("C18");
		VtiUserExitScreenField C19Field = getScreenField("C19");
		VtiUserExitScreenField C20Field = getScreenField("C20");
		VtiUserExitScreenField C21Field = getScreenField("C21");

		VtiUserExitScreenField StatusField = getScreenField("STATUS3");

		VtiUserExitScreenField PassFailField = getScreenField("INSPSTATUS");
		VtiUserExitScreenField scrFStatus = getScreenField("STATUS");
		
		scrFStatus.setFieldValue("PASSED");
		
		StatusField.setFieldValue("PASSED");

		PassFailField.setFieldValue("P");

        if(F15Field.getFieldValue().equalsIgnoreCase("X"))
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
			StatusField.setFieldValue("FAILED");
			
			
			if(C15Field.getFieldValue().length() == 0 && !scr15Field.getFieldValue().equalsIgnoreCase("X"))
				return new VtiUserExitResult(999,1,"Please comment on why failing point 15.",C15Field,null);
			
			scr15Field.setFieldValue("X");
		}
		else if(C15Field.getFieldValue().length() > 0 && !F15Field.getFieldValue().equalsIgnoreCase("X") && !scr15Field.getFieldValue().equalsIgnoreCase("X"))
		{
			F15Field.setFieldValue("X");
			P15Field.setFieldValue("");
			scr15Field.setFieldValue("X");
			bNoComment = true;
		}
		
		if(F16Field.getFieldValue().equalsIgnoreCase("X"))
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
    		StatusField.setFieldValue("FAILED");	
			
			
			if(C16Field.getFieldValue().length() == 0 && !scr16Field.getFieldValue().equalsIgnoreCase("X"))
				return new VtiUserExitResult(999,1,"Please comment on why failing point 16.",C16Field,null);
			
			scr16Field.setFieldValue("X");
		}
		else if(C16Field.getFieldValue().length() > 0 && !F16Field.getFieldValue().equalsIgnoreCase("X") && !scr16Field.getFieldValue().equalsIgnoreCase("X"))
		{
			F16Field.setFieldValue("X");
			P16Field.setFieldValue("");
			scr16Field.setFieldValue("X");
			bNoComment = true;
		}
		
        if(F17Field.getFieldValue().equalsIgnoreCase("X"))
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
    		StatusField.setFieldValue("FAILED");	
			
			
			if(C17Field.getFieldValue().length() == 0 && !scr17Field.getFieldValue().equalsIgnoreCase("X"))
				return new VtiUserExitResult(999,1,"Please comment on why failing point 17.",C17Field,null);
			
			scr17Field.setFieldValue("X");
		}
		else if(C17Field.getFieldValue().length() > 0 && !F17Field.getFieldValue().equalsIgnoreCase("X") && !scr17Field.getFieldValue().equalsIgnoreCase("X"))
		{
			F17Field.setFieldValue("X");
			P17Field.setFieldValue("");
			scr17Field.setFieldValue("X");
			bNoComment = true;
		}
		
        if(F18Field.getFieldValue().equalsIgnoreCase("X"))
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
    		StatusField.setFieldValue("FAILED");
			
			
			 if(C18Field.getFieldValue().length() == 0 && !scr18Field.getFieldValue().equalsIgnoreCase("X"))
				return new VtiUserExitResult(999,1,"Please comment on why failing point 18.",C18Field,null);
			 
			 scr18Field.setFieldValue("X");
		}
		else if(C18Field.getFieldValue().length() > 0 && !F18Field.getFieldValue().equalsIgnoreCase("X") && !scr18Field.getFieldValue().equalsIgnoreCase("X"))
		{
			F18Field.setFieldValue("X");
			P18Field.setFieldValue("");
			scr18Field.setFieldValue("X");
			bNoComment = true;
		}
		
        if(F19Field.getFieldValue().equalsIgnoreCase("X"))
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
    		StatusField.setFieldValue("FAILED");
			
			
			if(C19Field.getFieldValue().length() == 0 && !scr19Field.getFieldValue().equalsIgnoreCase("X"))
				return new VtiUserExitResult(999,1,"Please comment on why failing point 19.",C19Field,null);
			
			scr19Field.setFieldValue("X");
		}
		else if(C19Field.getFieldValue().length() > 0 && !F19Field.getFieldValue().equalsIgnoreCase("X") && !scr19Field.getFieldValue().equalsIgnoreCase("X"))
		{
			F19Field.setFieldValue("X");
			P19Field.setFieldValue("");
			scr19Field.setFieldValue("X");
			bNoComment = true;
		}
		
        if(F20Field.getFieldValue().equalsIgnoreCase("X"))
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
    		StatusField.setFieldValue("FAILED");
			
			
			if(C20Field.getFieldValue().length() == 0 && !scr20Field.getFieldValue().equalsIgnoreCase("X"))
				return new VtiUserExitResult(999,1,"Please comment on why failing point 20.",C20Field,null);
			
			scr20Field.setFieldValue("X");
		}
		else if(C20Field.getFieldValue().length() > 0 && !F20Field.getFieldValue().equalsIgnoreCase("X") && !scr20Field.getFieldValue().equalsIgnoreCase("X"))
		{
			F20Field.setFieldValue("X");
			P20Field.setFieldValue("");
			scr20Field.setFieldValue("X");
			bNoComment = true;
		}
		
        if(F21Field.getFieldValue().equalsIgnoreCase("X"))
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
    		StatusField.setFieldValue("FAILED");	
			
			
			if(C21Field.getFieldValue().length() == 0 && !scr21Field.getFieldValue().equalsIgnoreCase("X"))
				return new VtiUserExitResult(999,1,"Please comment on why failing point 21.",C21Field,null);
			
			scr21Field.setFieldValue("X");
		}	
		else if(C21Field.getFieldValue().length() > 0 && !F21Field.getFieldValue().equalsIgnoreCase("X") && !scr21Field.getFieldValue().equalsIgnoreCase("X"))
		{
			F21Field.setFieldValue("X");
			P21Field.setFieldValue("");
			scr21Field.setFieldValue("X");
			bNoComment = true;
		}
		
		if(bNoComment)
			return new VtiUserExitResult(999,1,"Please set the inspection to fail when commenting.");

		return new VtiUserExitResult();
	}
}

