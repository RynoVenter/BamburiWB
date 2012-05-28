package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class DecriptTruckReg extends VtiUserExit 
{
	public VtiUserExitResult execute() throws VtiExitException 
	{
		VtiUserExitScreenField scrDispTruck = getScreenField("DISP_TRUCK");
		VtiUserExitScreenField scrP1 = getScreenField("P1");
		VtiUserExitScreenField scrP2 = getScreenField("P2");
		VtiUserExitScreenField scrP3 = getScreenField("P3");
		VtiUserExitScreenField scrP4 = getScreenField("P4");
		VtiUserExitScreenField scrP5 = getScreenField("P5");
		VtiUserExitScreenField scrP6 = getScreenField("P6");
		VtiUserExitScreenField scrP7 = getScreenField("P7");
		VtiUserExitScreenField scrP8 = getScreenField("P8");
		VtiUserExitScreenField scrP9 = getScreenField("P9");
		VtiUserExitScreenField scrP10 = getScreenField("P10");
		
		VtiUserExitScreenField scrPos1 = getScreenField("GPOS1");
		VtiUserExitScreenField scrPos2 = getScreenField("GPOS2");
		VtiUserExitScreenField scrPos3 = getScreenField("GPOS3");
		VtiUserExitScreenField scrPos4 = getScreenField("GPOS4");
		VtiUserExitScreenField scrPos5= getScreenField("GPOS5");
		VtiUserExitScreenField scrPos6 = getScreenField("GPOS6");
		VtiUserExitScreenField scrPos7 = getScreenField("GPOS7");
		VtiUserExitScreenField scrPos8 = getScreenField("GPOS8");
		VtiUserExitScreenField scrPos9 = getScreenField("GPOS9");
		VtiUserExitScreenField scrPos10 = getScreenField("GPOS10");
		
		if (scrPos1 == null) return new VtiUserExitResult(999, "Failed to load sreen field GPOS1");
		if (scrPos2 == null) return new VtiUserExitResult(999, "Failed to load sreen field GPOS2");
		if (scrPos3 == null) return new VtiUserExitResult(999, "Failed to load sreen field GPOS3");
		if (scrPos4 == null) return new VtiUserExitResult(999, "Failed to load sreen field GPOS4");
		if (scrPos5 == null) return new VtiUserExitResult(999, "Failed to load sreen field GPOS5");
		if (scrPos6 == null) return new VtiUserExitResult(999, "Failed to load sreen field GPOS6");
		if (scrPos7 == null) return new VtiUserExitResult(999, "Failed to load sreen field GPOS7");
		if (scrPos8 == null) return new VtiUserExitResult(999, "Failed to load sreen field GPOS8");
		if (scrPos9 == null) return new VtiUserExitResult(999, "Failed to load sreen field GPOS9");
		if (scrPos10 == null) return new VtiUserExitResult(999, "Failed to load sreen field GPOS10");
		
		
		if (scrDispTruck == null) return new VtiUserExitResult(999, "Failed to load sreen field DISP_TRUCK");
		if (scrP1 == null) return new VtiUserExitResult(999, "Failed to load sreen field P1");
		if (scrP2 == null) return new VtiUserExitResult(999, "Failed to load sreen field P2");
		if (scrP3 == null) return new VtiUserExitResult(999, "Failed to load sreen field P3");
		if (scrP4 == null) return new VtiUserExitResult(999, "Failed to load sreen field P4");
		if (scrP5 == null) return new VtiUserExitResult(999, "Failed to load sreen field P5");
		if (scrP6 == null) return new VtiUserExitResult(999, "Failed to load sreen field P6");
		if (scrP7 == null) return new VtiUserExitResult(999, "Failed to load sreen field P7");
		if (scrP8 == null) return new VtiUserExitResult(999, "Failed to load sreen field P8");
		if (scrP9 == null) return new VtiUserExitResult(999, "Failed to load sreen field P9");
		if (scrP10 == null) return new VtiUserExitResult(999, "Failed to load sreen field P10");
		
		String reg = scrDispTruck.getFieldValue();
		
		if(reg.length() > 0)
		{
			scrP1.setFieldValue(reg.substring(0,1));
			scrPos1.setFieldValue(scrP1.getFieldValue());
		}
		if(reg.length() > 1)
		{
			scrP2.setFieldValue(reg.substring(1,2));
			scrPos2.setFieldValue(scrP2.getFieldValue());
		}
		if(reg.length() > 2)
		{
			scrP3.setFieldValue(reg.substring(2,3));
			scrPos3.setFieldValue(scrP3.getFieldValue());
		}
		if(reg.length() > 3)
		{
			scrP4.setFieldValue(reg.substring(3,4));
			scrPos4.setFieldValue(scrP4.getFieldValue());
		}
		if(reg.length() > 4)
		{
			scrP5.setFieldValue(reg.substring(4,5));
			scrPos5.setFieldValue(scrP5.getFieldValue());
		}
		if(reg.length() > 5)
		{
			scrP6.setFieldValue(reg.substring(5,6));
			scrPos6.setFieldValue(scrP6.getFieldValue());
		}
		if(reg.length() > 6)
		{
			scrP7.setFieldValue(reg.substring(6,7));
			scrPos7.setFieldValue(scrP7.getFieldValue());
		}
		if(reg.length() > 7)
		{
			scrP8.setFieldValue(reg.substring(7,8));
			scrPos8.setFieldValue(scrP8.getFieldValue());
		}
		if(reg.length() > 8)
		{
			scrP9.setFieldValue(reg.substring(8,9));
			scrPos9.setFieldValue(scrP9.getFieldValue());
		}
		if(reg.length() > 9)
		{
			scrP10.setFieldValue(reg.substring(9,10));
			scrPos10.setFieldValue(scrP10.getFieldValue());
		}
		
		
		
		return new VtiUserExitResult();
	}
}
