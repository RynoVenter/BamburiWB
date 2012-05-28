package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;


public class ValidateSoOff extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{

		VtiUserExitScreenField scrBtnSave = getScreenField("BTNSAVE");
		
		VtiUserExitScreenField scrAuArt = getScreenField("AUART"); 
		VtiUserExitScreenField scrCustomer = getScreenField("CUSTOMER");
		VtiUserExitScreenField scrKunNr = getScreenField("KUNNR");
		VtiUserExitScreenField scrVkorg = getScreenField("VKORG");
		VtiUserExitScreenField scrVTweg = getScreenField("VTWEG");
		VtiUserExitScreenField scrCustomer2 = getScreenField("CUSTOMER2");
		VtiUserExitScreenField scrKunNr2 = getScreenField("KUNNR2");
		VtiUserExitScreenField scrSpart = getScreenField("SPART");
		VtiUserExitScreenField scrOff_ShipTxt = getScreenField("OFF_SHIPTXT");
		VtiUserExitScreenField scrVkBur = getScreenField("VKBUR");
		VtiUserExitScreenField scrBsTnk = getScreenField("BSTNK");
		VtiUserExitScreenField scrBsArk = getScreenField("BSARK");
		VtiUserExitScreenField scrMatNr = getScreenField("MATNR");
		VtiUserExitScreenField scrArkTx = getScreenField("ARKTX");
		VtiUserExitScreenField scrWerks = getScreenField("WERKS");
		VtiUserExitScreenField scrLgort = getScreenField("LGORT");
		VtiUserExitScreenField scrMtArt = getScreenField("MTART");
		VtiUserExitScreenField scrSubTotal = getScreenField("SUBTOTAL");	
		VtiUserExitScreenField scrMatNr2 = getScreenField("MATNR2");
		VtiUserExitScreenField scrArkTx2 = getScreenField("ARKTX2");
		VtiUserExitScreenField scrWerks2 = getScreenField("WERKS2");
		VtiUserExitScreenField scrLgort2 = getScreenField("LGORT2");
		VtiUserExitScreenField scrMtArt2 = getScreenField("MTART2");
		VtiUserExitScreenField scrSubTotal2 = getScreenField("SUBTOTAL2");	
		VtiUserExitScreenField scrMatNr3 = getScreenField("MATNR3");
		VtiUserExitScreenField scrArkTx3 = getScreenField("ARKTX3");
		VtiUserExitScreenField scrWerks3 = getScreenField("WERKS3");
		VtiUserExitScreenField scrLgort3 = getScreenField("LGORT3");
		VtiUserExitScreenField scrMtArt3 = getScreenField("MTART3");
		VtiUserExitScreenField scrSubTotal3 = getScreenField("SUBTOTAL3");	
		VtiUserExitScreenField scrMatNr4 = getScreenField("MATNR4");
		VtiUserExitScreenField scrArkTx4 = getScreenField("ARKTX4");
		VtiUserExitScreenField scrWerks4 = getScreenField("WERKS4");
		VtiUserExitScreenField scrLgort4 = getScreenField("LGORT4");
		VtiUserExitScreenField scrMtArt4 = getScreenField("MTART4");
		VtiUserExitScreenField scrSubTotal4 = getScreenField("SUBTOTAL4");	
		
		if(scrAuArt.getFieldValue().length() == 0)
			return new VtiUserExitResult(999,1,"Please indicate the Document Type.");

		if(scrVkorg.getFieldValue().length() == 0)
			return new VtiUserExitResult(999,1,"Please indicate the Sales Organization.");
		if(scrVTweg.getFieldValue().length() == 0)
			return new VtiUserExitResult(999,1,"Please indicate the Distribution Channel.");
		if(scrSpart.getFieldValue().length() == 0)
			return new VtiUserExitResult(999,1,"Please indicate the Division.");
		if(scrOff_ShipTxt.getFieldValue().length() == 0)
			return new VtiUserExitResult(999,1,"Please indicate the Shipping Text.");
		if(scrVkBur.getFieldValue().length() == 0)
			return new VtiUserExitResult(999,1,"Please indicate the Sales Office.");
		if(scrBsTnk.getFieldValue().length() == 0)
			return new VtiUserExitResult(999,1,"Please indicate the Customer Purchase Order.");
		if(scrBsArk.getFieldValue().length() == 0)
			return new VtiUserExitResult(999,1,"Please indicate the Purchase Order Type.");
		
		if(scrCustomer.getFieldValue().length() == 0 || scrKunNr.getFieldValue().length() == 0)
			return new VtiUserExitResult(999,1,"Please indicate the Sold to Party.");

		if(scrCustomer2.getFieldValue().length() == 0 || scrKunNr2.getFieldValue().length() == 0)
			return new VtiUserExitResult(999,1,"Please indicate the Ship to Party.");
		
		if(scrMatNr.getFieldValue().length() > 0 && scrArkTx.getFieldValue().length() > 0)
		{
				if(scrWerks.getFieldValue().length() == 0)
					return new VtiUserExitResult(999,1,"Please indicate the Plant for line 10.");
				
				if(scrLgort.getFieldValue().length() == 0 && !scrMtArt.getFieldValue().equalsIgnoreCase("DIEN"))
					return new VtiUserExitResult(999,1,"Please indicate the SLOC for line 10.");
				
				if(scrMtArt.getFieldValue().length() == 0)
					return new VtiUserExitResult(999,1,"Please indicate the Type of line item for line 10.");
				else if(scrMtArt.getFieldValue().equalsIgnoreCase("DIEN"))
					scrLgort.setFieldValue("");
				
				if(scrSubTotal.getFieldValue().length() == 0)
					return new VtiUserExitResult(999,1,"Please indicate the order qty for line 10.");
		}
		else if(scrMatNr.getFieldValue().length() == 0 || scrArkTx.getFieldValue().length() == 0)
			return new VtiUserExitResult(999,1,"Please indicate the Material for line 10.");
		
		if(scrMatNr2.getFieldValue().length() > 0 && scrArkTx2.getFieldValue().length() > 0)
		{
				if(scrWerks2.getFieldValue().length() == 0)
					return new VtiUserExitResult(999,1,"Please indicate the Plant for line 20.");
				
				if(scrLgort2.getFieldValue().length() == 0 && !scrMtArt2.getFieldValue().equalsIgnoreCase("DIEN"))
					return new VtiUserExitResult(999,1,"Please indicate the SLOC for line 20.");
				
				if(scrMtArt2.getFieldValue().length() == 0)
					return new VtiUserExitResult(999,1,"Please indicate the Type of line item for line 20.");
				else if(scrMtArt2.getFieldValue().equalsIgnoreCase("DIEN"))
					scrLgort2.setFieldValue("");
				
				if(scrSubTotal2.getFieldValue().length() == 0)
					return new VtiUserExitResult(999,1,"Please indicate the order qty for line 20.");
		}
		else if((scrMatNr2.getFieldValue().length() == 0 || scrArkTx2.getFieldValue().length() == 0)
				&& (scrMatNr2.getFieldValue().length() > 0 || scrArkTx2.getFieldValue().length() > 0))
					return new VtiUserExitResult(999,1,"Please indicate the Material for line 20.");
		
		if(scrMatNr3.getFieldValue().length() > 0 && scrArkTx3.getFieldValue().length() > 0)
		{
				if(scrWerks3.getFieldValue().length() == 0)
					return new VtiUserExitResult(999,1,"Please indicate the Plant for line 30.");
				
				if(scrLgort3.getFieldValue().length() == 0 && !scrMtArt3.getFieldValue().equalsIgnoreCase("DIEN"))
					return new VtiUserExitResult(999,1,"Please indicate the SLOC for line 30.");
				
				if(scrMtArt3.getFieldValue().length() == 0)
					return new VtiUserExitResult(999,1,"Please indicate the Type of line item for line 30.");
				else if(scrMtArt3.getFieldValue().equalsIgnoreCase("DIEN"))
					scrLgort3.setFieldValue("");
				
				if(scrSubTotal3.getFieldValue().length() == 0)
					return new VtiUserExitResult(999,1,"Please indicate the order qty for line 30.");
		}
		else if((scrMatNr3.getFieldValue().length() == 0 || scrArkTx3.getFieldValue().length() == 0)
				&& (scrMatNr3.getFieldValue().length() > 0 || scrArkTx3.getFieldValue().length() > 0))
					return new VtiUserExitResult(999,1,"Please indicate the Material for line 30.");
		
		if(scrMatNr4.getFieldValue().length() > 0 && scrArkTx4.getFieldValue().length() > 0)
		{
				if(scrWerks4.getFieldValue().length() == 0)
					return new VtiUserExitResult(999,1,"Please indicate the Plant for line 40.");
				
				if(scrLgort4.getFieldValue().length() == 0 && !scrMtArt4.getFieldValue().equalsIgnoreCase("DIEN"))
					return new VtiUserExitResult(999,1,"Please indicate the SLOC for line 40.");
				
				if(scrMtArt4.getFieldValue().length() == 0)
					return new VtiUserExitResult(999,1,"Please indicate the Type of line item for line 40.");
				else if(scrMtArt4.getFieldValue().equalsIgnoreCase("DIEN"))
					scrLgort4.setFieldValue("");
				
				if(scrSubTotal4.getFieldValue().length() == 0)
					return new VtiUserExitResult(999,1,"Please indicate the order qty for line 40.");
		}
		else if((scrMatNr4.getFieldValue().length() == 0 || scrArkTx4.getFieldValue().length() == 0) 
				&& (scrMatNr4.getFieldValue().length() > 0 || scrArkTx4.getFieldValue().length() > 0))
					return new VtiUserExitResult(999,1,"Please indicate the Material for line 40.");
		
		scrBtnSave.setHiddenFlag(false);
			
		return new VtiUserExitResult();
	}
}
