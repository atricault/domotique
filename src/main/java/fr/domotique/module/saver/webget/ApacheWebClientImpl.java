package fr.domotique.module.saver.webget;

import java.io.IOException;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class ApacheWebClientImpl {

	protected static String[] getUrlContent(String urlToCall, String params) throws IOException{
		 CloseableHttpClient httpclient = HttpClients.createDefault();
	     try {
	    	 StringBuffer encodedParam = new StringBuffer();
	    	 String[] paramArray = params.split("&");
	    	 for(int i = 0 ; i < paramArray.length ; i++){
	    		 String[] paramNameValueArray = paramArray[i].split("=");
	    		 paramNameValueArray[1] = URLEncoder.encode(paramNameValueArray[1], "UTF-8");
	    		 encodedParam.append(paramNameValueArray[0]).append("=").append(paramNameValueArray[1]).append("&");
	    	 }
	         HttpGet httpget = new HttpGet(urlToCall + "?" + encodedParam.toString());
	
	         // Create a custom response handler
	         ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
	
	             @Override
	             public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
	                 int status = response.getStatusLine().getStatusCode();
	                 if (status >= 200 && status < 300) {
	                     HttpEntity entity = response.getEntity();
	                     return entity != null ? EntityUtils.toString(entity) : null;
	                 } else {
	                     throw new ClientProtocolException("Unexpected response status: " + status);
	                 }
	             }
	
	         };
	         String responseBody = httpclient.execute(httpget, responseHandler);
	         String[] retValue = new String[1];
	         retValue[0] = responseBody;
	         return retValue;
	     } finally {
	         httpclient.close();
	     }
	}
	
	
	public static void main(String[] agrs){
		try {
			System.out.println(getUrlContent("http://tricault:yenyen@domotique.tricault.com/insert_info.php", "msgValType=TI&msgVal=21.20&msgType=I&moduleInitId=1&zone=1&receptionDate=java.util.GregorianCalendar[time=1483895364921,areFieldsSet=true,areAllFieldsSet=true,lenient=true,zone=sun.util.calendar.ZoneInfo[id=\"Europe/Paris\",offset=3600000,dstSavings=3600000,useDaylight=true,transitions=184,lastRule=java.util.SimpleTimeZone[id=Europe/Paris,offset=3600000,dstSavings=3600000,useDaylight=true,startYear=0,startMode=2,startMonth=2,startDay=-1,startDayOfWeek=1,startTime=3600000,startTimeMode=2,endMode=2,endMonth=9,endDay=-1,endDayOfWeek=1,endTime=3600000,endTimeMode=2,firstDayOfWeek=2,minimalDaysInFirstWeek=4,ERA=1,YEAR=2017,MONTH=0,WEEK_OF_YEAR=1,WEEK_OF_MONTH=1,DAY_OF_MONTH=8,DAY_OF_YEAR=8,DAY_OF_WEEK=1,DAY_OF_WEEK_IN_MONTH=2,AM_PM=1,HOUR=6,HOUR_OF_DAY=18,MINUTE=9,SECOND=24,MILLISECOND=921,ZONE_OFFSET=3600000,DST_OFFSET=0")[0]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
