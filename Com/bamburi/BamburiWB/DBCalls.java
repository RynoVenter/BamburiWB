package com.bamburi.bamburiwb;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class DBCalls
{
	/*General stored database calls, improvement on sky database stored procedures.
	*/
	public void ldbRefresh (String ldb, VtiUserExit vUE) throws VtiExitException
	{
	//Declarations of variables and elements. Followed by the checking of the elements.
		VtiExitLdbTable ldbTable = vUE.getLocalDatabaseTable(ldb);

		try
		{				
			String hostName = vUE.getHostInterfaceName();
			vUE.forceHeartbeat(hostName,true,250);
			boolean hostConnected = vUE.isHostInterfaceConnected(hostName);
			VtiUserExitHeaderInfo sessionHeader = vUE.getHeaderInfo();
			
			if (hostConnected)
			{
				VtiExitLdbRequest ldbReqUpload = new VtiExitLdbRequest(ldbTable,VtiExitLdbRequest.UPLOAD);
				ldbReqUpload.submit(true);
			
				try
				{
					Thread.sleep(500);
				}
				catch (InterruptedException ie)
				{
					Log.error("Sleep Thread during table refresh failed.", ie);
				}
				
				VtiExitLdbRequest ldbReqRefresh = new VtiExitLdbRequest(ldbTable,VtiExitLdbRequest.REFRESH);
				ldbReqRefresh.submit(false);
			}
		}
		catch ( VtiExitException ee)
		{
			vUE.logError("Error refreshing the table.", ee);
		}
	}
	
		public void ldbRefreshOnly (String ldb, VtiUserExit vUE) throws VtiExitException
	{
	//Declarations of variables and elements. Followed by the checking of the elements.
		VtiExitLdbTable ldbTable = vUE.getLocalDatabaseTable(ldb);

		try
		{				
			String hostName = vUE.getHostInterfaceName();
			vUE.forceHeartbeat(hostName,true,250);
			boolean hostConnected = vUE.isHostInterfaceConnected(hostName);
			VtiUserExitHeaderInfo sessionHeader = vUE.getHeaderInfo();
			
			if (hostConnected)
			{				
				VtiExitLdbRequest ldbReqRefresh = new VtiExitLdbRequest(ldbTable,VtiExitLdbRequest.REFRESH);
				ldbReqRefresh.submit(false);
			}
		}
		catch ( VtiExitException ee)
		{
			vUE.logError("Error refreshing the table.", ee);
		}
	}
	
	public void ldbUpload (String ldb, VtiUserExit vUE) throws VtiExitException
	{
	//Declarations of variables and elements. Followed by the checking of the elements.
		VtiExitLdbTable ldbTable = vUE.getLocalDatabaseTable(ldb);

		try
		{
			String hostName = vUE.getHostInterfaceName();
			vUE.forceHeartbeat(hostName,true,100);
			boolean hostConnected = vUE.isHostInterfaceConnected(hostName);
			VtiUserExitHeaderInfo sessionHeader = vUE.getHeaderInfo();
		
			if (hostConnected)
			{ 
				VtiExitLdbRequest ldbReqUpload = new VtiExitLdbRequest(ldbTable,VtiExitLdbRequest.UPLOAD);
				ldbReqUpload.submit(true);
			}
		}
		catch ( VtiExitException ee)
		{
			vUE.logError("Error uploading the table.", ee);
		}
	}
	
	
	public void ldbDownload (String ldb, VtiUserExit vUE) throws VtiExitException
	{
	//Declarations of variables and elements. Followed by the checking of the elements.
		VtiExitLdbTable ldbTable = vUE.getLocalDatabaseTable(ldb);
		
		try
		{
			String hostName = vUE.getHostInterfaceName();
			vUE.forceHeartbeat(hostName,true,250);
			boolean hostConnected = vUE.isHostInterfaceConnected(hostName);
			VtiUserExitHeaderInfo sessionHeader = vUE.getHeaderInfo();
			
			if (hostConnected)
			{ 
				VtiExitLdbRequest ldbReqDownload = new VtiExitLdbRequest(ldbTable,VtiExitLdbRequest.DOWNLOAD);
				ldbReqDownload.submit(false);
			}
		}
		catch ( VtiExitException ee)
		{
			Log.error("Error uploading the table.", ee);
		}
	}

}
