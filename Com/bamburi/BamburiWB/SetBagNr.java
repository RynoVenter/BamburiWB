package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class SetBagNr extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		VtiUserExitScreenTable items = getScreenTable("TB_ITEMS");
		
		if(items == null) return new VtiUserExitResult(999,"Screen table TB_ITEMS not loaded.");
		
		int tableRows = items.getRowCount();
		
		VtiUserExitScreenTableRow actRow = items.getActiveRow();
		int colonIndex = 0;
		String docNum = "";
		try
		{
			if(actRow.getFieldValue("BAGDESC").length() != 0)
			{
				colonIndex = actRow.getFieldValue("BAGDESC").indexOf(":");
				docNum = actRow.getFieldValue("BAGDESC").substring(0,colonIndex);
				actRow.setFieldValue("BAGNR",docNum);
			}
		}
		catch(StringIndexOutOfBoundsException siob)
		{
			return new VtiUserExitResult(999,"Please select an item from the packing line list.");
		}

		
		return new VtiUserExitResult();
	}
}
