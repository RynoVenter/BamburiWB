package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;


public class SetPassFail1 extends VtiUserExit
{	
	public VtiUserExitResult execute() throws VtiExitException
	{
		
		boolean bNoComment = false;
		VtiUserExitScreenField P1Field = getScreenField("P1");
		VtiUserExitScreenField P2Field = getScreenField("P2");
		VtiUserExitScreenField P3Field = getScreenField("P3");
		VtiUserExitScreenField P4Field = getScreenField("P4");
		VtiUserExitScreenField P5Field = getScreenField("P5");
		VtiUserExitScreenField P6Field = getScreenField("P6");
		VtiUserExitScreenField P7Field = getScreenField("P7");
		
		VtiUserExitScreenField scr1Field = getScreenField("1");
		VtiUserExitScreenField scr2Field = getScreenField("2");
		VtiUserExitScreenField scr3Field = getScreenField("3");
		VtiUserExitScreenField scr4Field = getScreenField("4");
		VtiUserExitScreenField scr5Field = getScreenField("5");
		VtiUserExitScreenField scr6Field = getScreenField("6");
		VtiUserExitScreenField scr7Field = getScreenField("7");
		
		VtiUserExitScreenField F1Field = getScreenField("F1");
		VtiUserExitScreenField F2Field = getScreenField("F2");
		VtiUserExitScreenField F3Field = getScreenField("F3");
		VtiUserExitScreenField F4Field = getScreenField("F4");
		VtiUserExitScreenField F5Field = getScreenField("F5");
		VtiUserExitScreenField F6Field = getScreenField("F6");
		VtiUserExitScreenField F7Field = getScreenField("F7");
		
		VtiUserExitScreenField C1Field = getScreenField("C1");
		VtiUserExitScreenField C2Field = getScreenField("C2");
		VtiUserExitScreenField C3Field = getScreenField("C3");
		VtiUserExitScreenField C4Field = getScreenField("C4");
		VtiUserExitScreenField C5Field = getScreenField("C5");
		VtiUserExitScreenField C6Field = getScreenField("C6");
		VtiUserExitScreenField C7Field = getScreenField("C7");
		
		VtiUserExitScreenField StatusField = getScreenField("STATUS1");
		VtiUserExitScreenField scrQ1WField = getScreenField("Q1W");
	   

		VtiUserExitScreenField PassFailField = getScreenField("INSPSTATUS");
		VtiUserExitScreenField scrFStatus = getScreenField("STATUS");


		if(StatusField.getFieldValue().equalsIgnoreCase("NONE"))
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
		
		scrFStatus.setFieldValue("PASSED");
		
		PassFailField.setFieldValue("P");
		
		StatusField.setFieldValue("PASSED");

        if(F1Field.getFieldValue().equalsIgnoreCase("X") )
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
    		StatusField.setFieldValue("FAILED");
			
			
			 if(C1Field.getFieldValue().length() == 0 && !scr1Field.getFieldValue().equalsIgnoreCase("X"))
				return new VtiUserExitResult(999,1,"Please comment on why failing point 1.",C1Field,null);
			 
			 scr1Field.setFieldValue("X");
		}			
		else if(C1Field.getFieldValue().length() > 0 && !F1Field.getFieldValue().equalsIgnoreCase("X") && !scr1Field.getFieldValue().equalsIgnoreCase("X"))
		{
			F1Field.setFieldValue("X");
			P1Field.setFieldValue("");
			scr1Field.setFieldValue("X");
			bNoComment = true;
		}

		if(!P1Field.getFieldValue().equalsIgnoreCase("X"))
			if(!F1Field.getFieldValue().equalsIgnoreCase("X"))
				P1Field.setFieldValue("X");
		
        if(F2Field.getFieldValue().equalsIgnoreCase("X") )
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
    		StatusField.setFieldValue("FAILED");
			
			
			 if(C2Field.getFieldValue().length() == 0 && !scr2Field.getFieldValue().equalsIgnoreCase("X"))
				return new VtiUserExitResult(999,1,"Please comment on why failing point 2.",C2Field,null);
			 
			 scr2Field.setFieldValue("X");
		}
		else if(C2Field.getFieldValue().length() > 0 && !F2Field.getFieldValue().equalsIgnoreCase("X") && !scr2Field.getFieldValue().equalsIgnoreCase("X"))
		{
			F2Field.setFieldValue("X");
			P2Field.setFieldValue("");
			scr2Field.setFieldValue("X");
			bNoComment = true;
		}
		
		if(!P2Field.getFieldValue().equalsIgnoreCase("X"))
			if(!F2Field.getFieldValue().equalsIgnoreCase("X"))
				P2Field.setFieldValue("X");
		
        if(F3Field.getFieldValue().equalsIgnoreCase("X"))
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
    		StatusField.setFieldValue("FAILED");
			
			
			 if(C3Field.getFieldValue().length() == 0 && !scr3Field.getFieldValue().equalsIgnoreCase("X"))
				return new VtiUserExitResult(999,1,"Please comment on why failing point 3.",C3Field,null);
			 
			 scr3Field.setFieldValue("X");
		}			
		else if(C3Field.getFieldValue().length() > 0 && !F3Field.getFieldValue().equalsIgnoreCase("X") && !scr3Field.getFieldValue().equalsIgnoreCase("X"))
		{
			F3Field.setFieldValue("X");
			P3Field.setFieldValue("");
			scr3Field.setFieldValue("X");
			bNoComment = true;
		}

		if(!P3Field.getFieldValue().equalsIgnoreCase("X"))
			if(!F3Field.getFieldValue().equalsIgnoreCase("X"))
				P3Field.setFieldValue("X");
		
        if(F4Field.getFieldValue().equalsIgnoreCase("X"))
		  {	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
    		StatusField.setFieldValue("FAILED");
			
			
			 if(C4Field.getFieldValue().length() == 0 && !scr4Field.getFieldValue().equalsIgnoreCase("X"))
				return new VtiUserExitResult(999,1,"Please comment on why failing point 4.",C4Field,null);
			 
			 scr4Field.setFieldValue("X");
		}			
		else if(C4Field.getFieldValue().length() > 0 && !F4Field.getFieldValue().equalsIgnoreCase("X") && !scr4Field.getFieldValue().equalsIgnoreCase("X"))
		{
			F4Field.setFieldValue("X");
			P4Field.setFieldValue("");
			scr4Field.setFieldValue("X");
			bNoComment = true;
		}

		if(!P4Field.getFieldValue().equalsIgnoreCase("X"))
			if(!F4Field.getFieldValue().equalsIgnoreCase("X"))
				P4Field.setFieldValue("X");
		
        if(F5Field.getFieldValue().equalsIgnoreCase("X"))
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
    		StatusField.setFieldValue("FAILED");
			
			
			 if(C5Field.getFieldValue().length() == 0 && !scr5Field.getFieldValue().equalsIgnoreCase("X"))
				return new VtiUserExitResult(999,1,"Please comment on why failing point 5.",C5Field,null);
			 
			 scr5Field.setFieldValue("X");
		}			
		else if(C5Field.getFieldValue().length() > 0 && !F5Field.getFieldValue().equalsIgnoreCase("X") && !scr5Field.getFieldValue().equalsIgnoreCase("X"))
		{
			F5Field.setFieldValue("X");
			P5Field.setFieldValue("");
			scr5Field.setFieldValue("X");
			bNoComment = true;
		}

		if(!P5Field.getFieldValue().equalsIgnoreCase("X"))
			if(!F5Field.getFieldValue().equalsIgnoreCase("X"))
				P5Field.setFieldValue("X");
		
		if(F6Field.getFieldValue().equalsIgnoreCase("X"))
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
    		StatusField.setFieldValue("FAILED");	
			
			
			 if(C6Field.getFieldValue().length() == 0 && !scr6Field.getFieldValue().equalsIgnoreCase("X"))
				return new VtiUserExitResult(999,1,"Please comment on why failing point 6.",C6Field,null);
			 
			 scr6Field.setFieldValue("X");
		}			
		else if(C6Field.getFieldValue().length() > 0 && !F6Field.getFieldValue().equalsIgnoreCase("X") && !scr6Field.getFieldValue().equalsIgnoreCase("X"))
		{
			F6Field.setFieldValue("X");
			P6Field.setFieldValue("");
			scr6Field.setFieldValue("X");
			bNoComment = true;
		}

		if(!P6Field.getFieldValue().equalsIgnoreCase("X"))
			if(!F6Field.getFieldValue().equalsIgnoreCase("X"))
				P6Field.setFieldValue("X");
		
		if(F7Field.getFieldValue().equalsIgnoreCase("X"))
		{	
			PassFailField.setFieldValue("F");
			scrFStatus.setFieldValue("FAILED");
    		StatusField.setFieldValue("FAILED");
			
			
			 if(C7Field.getFieldValue().length() == 0 && !scr7Field.getFieldValue().equalsIgnoreCase("X"))
				return new VtiUserExitResult(999,1,"Please comment on why failing point 7.",C7Field,null);
			 
			 scr7Field.setFieldValue("X");
		}			
		else if(C7Field.getFieldValue().length() > 0 && !F7Field.getFieldValue().equalsIgnoreCase("X") && !scr7Field.getFieldValue().equalsIgnoreCase("X"))
		{
			F7Field.setFieldValue("X");
			P7Field.setFieldValue("");
			scr7Field.setFieldValue("X");
			bNoComment = true;
		}

		if(!P7Field.getFieldValue().equalsIgnoreCase("X"))
			if(!F7Field.getFieldValue().equalsIgnoreCase("X"))
				P7Field.setFieldValue("X");
        
		if(C1Field.getFieldValue().length() > 0 && !F1Field.getFieldValue().equalsIgnoreCase("X") && !scr1Field.getFieldValue().equalsIgnoreCase("X"))
		{
			F1Field.setFieldValue("X");
			P1Field.setFieldValue("");
			scr1Field.setFieldValue("X");
			bNoComment = true;
		}
		
		if(bNoComment)
			return new VtiUserExitResult(999,1,"Please set the inspection to fail when commenting.");
		
		return new VtiUserExitResult();
	}
}

