/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.tol.lib;

import java.io.ByteArrayInputStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import org.apache.commons.lang3.StringEscapeUtils;
import org.dom4j.*;
import org.dom4j.io.*;

import mesquite.tol.lib.*;
import mesquite.lib.*;



/**
 * Utility class for http communication, built originally for TreeGrow 
 * @author dmandel
 *
 */
public class BaseHttpRequestMaker {
    private static final int MAX_BYTES = 5000000;   

	public static final String MESQUITE_VERSION_URI = "http://mesquiteproject.org/pyMesquiteStartup";

	public static boolean contactServer(String s, String URI, StringBuffer response) {
		HttpClient client = new HttpClient();
		GetMethod method = new GetMethod(URI);
		NameValuePair[] pairs = new NameValuePair[1];
		pairs[0] = new NameValuePair("build", StringEscapeUtils.escapeHtml3("\t" + s + "\tOS =\t" + System.getProperty("os.name") + "\t" + System.getProperty("os.version") + "\tjava =\t" + System.getProperty("java.version") +"\t" + System.getProperty("java.vendor")));
		method.setQueryString(pairs);

		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
	    		new DefaultHttpMethodRetryHandler(3, false));
		
		return executeMethod(client, method, response);
	}
	public static boolean postToServer(String s, String URI, StringBuffer response) {
		HttpClient client = new HttpClient();
		PostMethod method = new PostMethod(URI);
		method.addParameter("OS", StringEscapeUtils.escapeHtml3(System.getProperty("os.name") + "\t" + System.getProperty("os.version")));
		method.addParameter("JVM", StringEscapeUtils.escapeHtml3(System.getProperty("java.version") +"\t" + System.getProperty("java.vendor")));
		NameValuePair post = new NameValuePair();
		post.setName("post");
		post.setValue(StringEscapeUtils.escapeHtml3(s));
		method.addParameter(post);

		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
	    		new DefaultHttpMethodRetryHandler(3, false));
		
		return executeMethod(client, method, response);
	}
	public static boolean sendInfoToServer(NameValuePair[] pairs, String URI, StringBuffer response, int retryCount) {
		HttpClient client = new HttpClient();
		GetMethod method = new GetMethod(URI);
		method.setQueryString(pairs);

	//	if (retryCount>0)
			method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(retryCount, false));
		
		return executeMethod(client, method, response);
	}
	public static boolean sendInfoToServer(NameValuePair[] pairs, String URI, StringBuffer response) {
		return sendInfoToServer(pairs, URI, response, 3);
	}
	protected static boolean executeMethod(HttpClient client, HttpMethod method, StringBuffer response) {
		boolean success = true;
	    try {
	        // Execute the method.
	        int statusCode = client.executeMethod(method);

	        if (statusCode != HttpStatus.SC_OK) {
	          System.err.println("Method failed: " + method.getStatusLine());
	        }

	        // Read the response body.
	        byte[] responseBody = method.getResponseBody();
	       if (response != null)
	    	   response.append(new String(responseBody));
	        // Deal with the response.
	        // Use caution: ensure correct character encoding and is not binary data
	        System.out.println(new String(responseBody));

	      } catch (HttpException e) {
	      //  System.err.println("Fatal protocol violation: " + e.getMessage());
	       // e.printStackTrace();
	        success = false;
	      } catch (IOException e) {
	      //  System.err.println("Fatal transport error: " + e.getMessage());
	      //  e.printStackTrace();
	        success = false;
	      } finally {
	        // Release the connection.
	        method.releaseConnection();
	      } 		
	        return success;
	}
   public static String getTap4ExternalUrlString(String prefix, String pageName, Map additionalArgs) {
    	return getExternalUrlString(pageName, additionalArgs, prefix, null, null);
    }
    public static Document getTap4ExternalUrlDocument(String prefix, String pageName, Map additionalArgs) {
    	return getTap4ExternalUrlDocument(prefix, pageName, additionalArgs, false);
    }
    public static Document getTap4ExternalUrlDocument(String prefix, String pageName, Map additionalArgs, boolean isPost) {
    	if (!isPost) {
    		String url = getTap4ExternalUrlString(prefix, pageName, additionalArgs);    		
    		return getHttpResponseAsDocument(url);
    	} else {
    		String url = getTap4ExternalUrlString(prefix, pageName, null);
    		return doPostGetDocument(url, additionalArgs);
    	}
    }
    /**
     * Execute a multipart post method to a tapestry page
     * @param prefix The base url to open
     * @param pageName The name of the tapestry page
     * @param stringArgs Post arguments that have string keys and values
     * @param fileArgs Post arguments that have string keys and file values
     * @return
     */
    public static Document getTap4ExternalUrlDocumentMultipart(String prefix, String pageName, 
    		Map stringArgs, Map fileArgs) {
    	String url = getTap4ExternalUrlString(prefix, pageName, null);
    	return doMultipartPostGetDocument(url, stringArgs, fileArgs);
    }
    public static String doPostGetString(String url, Map additionalParameters) {
    	return new String(doPost(url, additionalParameters));
    }
    
    public static Document doPostGetDocument(String url, Map additionalParameters) {
    	byte[] response = doPost(url, additionalParameters);
    	return getDocumentFromBytes(response);
    }
    
    public static Document doMultipartPostGetDocument(String url, Map stringParams, Map fileParams) {
    	byte[] response = doMultipartPost(url, stringParams, fileParams);
    	return getDocumentFromBytes(response);
    }
    
    public static byte[] doMultipartPost(String url, Map stringParams, Map fileParams) { 
    	 PostMethod filePost = new PostMethod(url);
    	 Part[] parts = new Part[stringParams.size() + fileParams.size()];
    	 int i = 0;
    	 if (stringParams != null) {
    		 for (Iterator iter = stringParams.keySet().iterator(); iter.hasNext();) {
				String nextKey = (String) iter.next();
				String nextValue = stringParams.get(nextKey).toString();
				parts[i++] = new StringPart(nextKey, nextValue);
    		 }
    	 }
    	 if (fileParams != null) {
    		 for (Iterator iter = fileParams.keySet().iterator(); iter.hasNext();) {
				String nextKey = (String) iter.next();
				File nextValue = (File) fileParams.get(nextKey);
				try {
					parts[i++] = new FilePart(nextKey, nextValue);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
    	 } 
    	 /*System.out.println("going to post multipart to url: " + url + " with parts: " + parts);
    	 for (int j = 0; j < parts.length; j++) {
			Part part = parts[j];
			System.out.println("current part is: " + part);
		 }*/
    	 filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost.getParams()) );
    	 //System.out.println("just set request entity");
    	 return executePost(filePost);
    }
    
    public static byte[] doPost(String url, Map additionalParameters) {
        PostMethod xmlPost = new PostMethod(url);
        NameValuePair[] argsArray = new NameValuePair[additionalParameters.keySet().size()];
        int i = 0;
        for (Iterator iter = additionalParameters.keySet().iterator(); iter.hasNext();) {
            String nextParamName = (String) iter.next();
            String nextValue = (String) additionalParameters.get(nextParamName);
            argsArray[i++] = new NameValuePair(nextParamName, nextValue);
        }
        xmlPost.setRequestBody(argsArray);
        return executePost(xmlPost);
    }
	private static byte[] executePost(PostMethod xmlPost) {
		HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
        int status = -1;
        try {
            status = client.executeMethod(xmlPost);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        byte[] bytes = null;
        if (status == HttpStatus.SC_OK) {
            try {
				bytes = xmlPost.getResponseBody();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
        } else {
        	try {
				throw new RuntimeException("bad status is: " + status);
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
            return null;
        }
        return bytes;
	}
    
    public static String getExternalUrlString(String pageName, Map additionalArgs, String prefix, String userId, String password) {
        String returnString = prefix;
        returnString += pageName;
        if (additionalArgs != null) {
	        if (!StringUtil.blank(userId)) {
		        if (additionalArgs.get(ToLRequestParameters.USER_ID) == null) {
		            returnString = addParameter(returnString, ToLRequestParameters.USER_ID, URLEncoder.encode(userId));
		        }
		        if (additionalArgs.get(ToLRequestParameters.PASSWORD) == null) {
		            returnString = addParameter(returnString, ToLRequestParameters.PASSWORD, password);
		        }
	        }
	        returnString = addAdditionalArgsToUrl(additionalArgs, returnString);
        }
        return returnString;        
    }
	private static String addAdditionalArgsToUrl(Map additionalArgs, String returnString) {
		for (Iterator iter = additionalArgs.keySet().iterator(); iter.hasNext();) {
		    String key = (String) iter.next();
		    String value = additionalArgs.get(key).toString();
		    returnString = addParameter(returnString, key, value);
		}
		return returnString;
	}
    
    protected static String addParameter(String originalString, String paramName, String value) {
    	if (!StringUtil.blank(originalString)) {
    		String separatorChar = "&";
    		if (originalString.indexOf('?') < 0) {
    			separatorChar = "?";
    		}
	        originalString += separatorChar;
	        originalString += paramName;
	        originalString += "=";
	        originalString += URLEncoder.encode(value);
	        return originalString;
    	} else {
    		return "";
    	}
    }
    
    public static Document getHttpResponseAsDocument(String url) {
        byte[] bytes = makeHttpRequest(url);
        return getDocumentFromBytes(bytes);
    }
    
    public static Document getDocumentFromBytes(byte[] bytes) {
        Document doc;
        try {
            doc = new SAXReader().read(new ByteArrayInputStream(bytes));
            return doc;
        } catch (Exception e) {
        	System.err.println(" bad bytes are: " + new String(bytes));
            return null;
        }    	
    }

    public static byte[] makeHttpRequest(String url) {
    	return makeHttpRequest(url, null, "");
    }
    
    public static byte[] makeHttpRequest(String url, Credentials c, String realm) {
        return (byte[]) makeHttpRequest(url, c, realm, true);
    }

    
    public static Object[] makeHttpRequestAsStream(String url, Credentials c, String realm) {
        return (Object[]) makeHttpRequest(url, c, realm, false);
    }
    
    public static Object[] makeHttpRequestAsStream(String url) {
    	return makeHttpRequestAsStream(url, null, null);
    }
    
    public static Object[] makeHttpRequestAsStream(String url, Map args) {
    	url = addAdditionalArgsToUrl(args, url);
    	return makeHttpRequestAsStream(url);
    }
    
    //public static InputStream makeHttpRequest
    
    /**
     * Make the http request to the specified url
     * @return If returnArray is true, a byte array containing the response bytes,
     * if false, we return an ObjectArray with the first argument being an 
     * InputStream and the second being the GetMethod that was used to make the
     * request.  The GetMethod must have releaseConnection() called on it after
     * all of the InputStream has been read.
     */
    public static Object makeHttpRequest(String url, Credentials c, String realm, boolean returnArray) {    	
        GetMethod getMethod = new GetMethod(url);
        Object returnValue = null;
        HttpClient client;
        try {
            client = new HttpClient();
            if (c != null) {
            	client.getParams().setAuthenticationPreemptive(true);
                //client.getState().set
                //client.getState().setCredentials(new HttpAuthRealm(null, realm), c);
                //client.getState().setCredentials(realm, null, c);
                client.getState().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, realm, AuthScope.ANY_SCHEME), c);
            }
            int statusCode = -1;
            // We will retry up to 3 times.
            for (int attempt = 0; statusCode == -1 && attempt < 3; attempt++) {
                try {
                    // execute the method.
                    statusCode = client.executeMethod(getMethod);
                } catch (HttpRecoverableException e) {
                    System.err.println("A recoverable exception occurred, retrying.  " + e.getMessage());
                } catch (IOException e) {
                    System.err.println("Failed to download file.");
                    e.printStackTrace();
                    break;
                }
            }
            if (statusCode == HttpStatus.SC_OK) {
                if (returnArray) {
                    returnValue = getMethod.getResponseBodyAsStream();
                    InputStream stream = (InputStream) returnValue;
                    byte[] bytes;
                    long contentLength = getMethod.getResponseContentLength();
                    if (contentLength >= 0) {
                    	bytes = new byte[(int) contentLength];
                    }  else {
                    	// it returned -1, so it doesn't know how long the
                    	// length is.  We'll allocate a 5mb buffer in this case
                        bytes = new byte[MAX_BYTES];                    	
                    }
                    int offset = 0;
                    int nextByte = 0;
                    try {
                        while ((nextByte = stream.read()) != -1) {
                            bytes[offset++] = (byte) nextByte;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (stream != null) {
                            stream.close();
                        }
                    }
                    byte[] newBytes = new byte[offset];
                    System.arraycopy(bytes, 0, newBytes, 0, offset);
                    returnValue = newBytes;
                    // make sure these get garbage collected
                    bytes = null;
                } else {
                    InputStream stream = getMethod.getResponseBodyAsStream();
                    returnValue = new Object[] {stream, getMethod};
                }
            } else {
                System.err.println("bad status code is: " + statusCode);
                returnValue = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                getMethod.getResponseBody();
            } catch (Exception e2) {
                e2.printStackTrace();
            } finally {
            	if (returnArray) {
            		getMethod.releaseConnection();
            	}
            }
            returnValue = null;
        } finally {
        	if (returnArray) {
                getMethod.releaseConnection();
        	}
        }
        return returnValue;
    }

}


