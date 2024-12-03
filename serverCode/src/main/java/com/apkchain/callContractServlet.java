package com.apkchain;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import java.util.concurrent.*;

import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
import io.ipfs.multihash.Multihash;

/**
 * Servlet implementation class callContractServlet
 */
public class callContractServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public callContractServlet() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    private static String uploadIPFS(String path) {
    	try {
    		long IPFSStart=System.currentTimeMillis();
    		File file_get = new File(path);
    		IPFS ipfs = new IPFS("/ip4/127.0.0.1/tcp/5001");
            ipfs.refs.local();
            NamedStreamable.FileWrapper file = new NamedStreamable.FileWrapper(file_get);
            MerkleNode addResult = ipfs.add(file).get(0);
            JSONObject jsonObj = new JSONObject(addResult.toJSONString());
            String file_hash = (String) jsonObj.get("Hash");
            
            System.out.println("The file IPFS hash is: " + file_hash);
            long IPFSEnd=System.currentTimeMillis();
            System.out.println("IPFS Upload total time:  "+(IPFSEnd-IPFSStart)+"ms");
            Multihash filePointer = Multihash.fromBase58(file_hash);
            byte[] fileContents = ipfs.cat(filePointer);
            return file_hash;
    	}catch (Exception e) {
            System.out.println("Exception: " + e);
            return "";
        }
    	
    }
    
    private static void StoreApp(JSONObject info) {
    	try{
    		int countDown = 20;
    		while(countDown>0) {
        		countDown = countDown -1;
        		TimeUnit.SECONDS.sleep(1);
        	}
    	} catch (InterruptedException e) {
            e.printStackTrace();
        }
    	long ContractStart=System.currentTimeMillis();
    	String ipfsHash = info.getString("Hash");
    	String pkgName = info.getString("package_name");
    	String version = info.getString("version_number");
    	String url = info.getString("originalUrl");
    	String cert1 = info.getString("certificates").replace("[", "");
    	String cert2 = cert1.replace("]", "");
    	JSONObject certificate = new JSONObject(cert2);
    	String certID = certificate.getString("serial_number");
    	String repkgStatus = info.getString("repackage_check");
    	String txnID = info.getString("txnID");
    	System.out.println("Python parameter txn: "+ txnID);
    	String result = "";
    	try {
   			 String[] param = new String[] { "/usr/bin/python", "/home/ubuntu/agchain/contractTool/callContract.py", ipfsHash, pkgName, version, url,certID,repkgStatus,txnID};
   			 
             Process proc = Runtime.getRuntime().exec(param);

             BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
             String line = null;

             while ((line = in.readLine()) != null) {
                 System.out.println(line);
                 result +=line;
             }    
             in.close();
             int re = proc.waitFor();
             System.out.println(re); 
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    	long ContractEnd=System.currentTimeMillis();
    	System.out.println("Call Contract total time:  "+(ContractEnd-ContractStart)+"ms");
    }
    
    private static JSONObject GetAPKInfo(String path) {
        String result = "";
        long ParseStart=System.currentTimeMillis();
    	 try {
    		 File file = new File(path);
    		 String[] param = new String[] { "/usr/bin/python", "/home/ubuntu/agchain/apkTool/apkExtract.py", path };

    		 System.out.println("filePath: " + path);
             Process proc = Runtime.getRuntime().exec(param);
             BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
             String line = null;

             while ((line = in.readLine()) != null) {
                 System.out.println(line);
                 result +=line;
             }    
             in.close();
             int re = proc.waitFor();
             System.out.println(re);

         } catch (IOException e) {
             e.printStackTrace();
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
    	 long ParseEnd=System.currentTimeMillis();
     	 System.out.println("Parse APK total time:  "+(ParseEnd-ParseStart)+"ms");
    	 if (result == "") {
    		 JSONObject res = new JSONObject();
    		 res.put("result", "fail");
    		 return res;
    	 }else {
    		 JSONObject res = new JSONObject(result);
    		 res.put("result", "success");
    		 return res;
    	 }
    }
    
    private boolean verifyTxn(String Txn, String PkgName, String Version) {
        boolean insertRes = UserDAO.insertTxn(Txn,PkgName,Version,"ipfs");
        if (insertRes) {
        	//insert successfully: this txn hasn't been used 
        	return true;
        }else {
        	//insert failed: this txn has been used.
        	return false;
        }

    }
    
    private Integer verify(String PackageName, String CertID) {
    	long startVerify=System.currentTimeMillis();
        APK apk_file = UserDAO.query(PackageName);
        long endVerify=System.currentTimeMillis();
        System.out.println("Duplicate check time:  "+(endVerify-startVerify)+"ms");
        if (null == apk_file) {
        	return 0;
        }else if(CertID.equals(apk_file.getCertID())) {
        	return 1;
        }else {
        	return 2;
        }
    
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(request,response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.setContentType("text/html;charset=utf-8");
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        try (PrintWriter out = response.getWriter()){
            String txn = request.getParameter("txn").trim();
            String path = request.getParameter("path").trim();
            String url = request.getParameter("url").trim();
            JSONObject final_res = new JSONObject();
            System.out.println("We got txn: "+ txn);
            System.out.println("We got path: "+ path);
            System.out.println("We got url: "+ url);
            Map<String, String> params = new HashMap<>();
            long startTime=System.currentTimeMillis();
            String pckgName = "";
            String version = "";	
            JSONObject apk_info = GetAPKInfo(path);
            String certID = "00000000000";
            if (apk_info.getString("result")=="success") {
            	System.out.println("Enter in parsing APK successfully");
            	//parse apk file
            	pckgName = apk_info.getString("package_name");
                version = apk_info.getString("version_number");
                String certificates = apk_info.get("certificates").toString().replace("/", "");
                String cert_to1 = certificates.replace("[", "");
                String cert_to2 = cert_to1.replace("]", "");
                JSONObject jsobj = new JSONObject(cert_to2);
                certID = jsobj.getString("serial_number");
                System.out.println("The content of certificates is: " + apk_info.get("certificates"));
                params.put("package_name", pckgName);
                params.put("version_number", version);
                params.put("certificates", certificates);
                
                // check repackage status
                int verifyResult = verify(pckgName, certID);                        
                System.out.println("The verification result is: " + verifyResult);
                if (verifyResult == 0) {
                	params.put("repackage_check", "No related data in current DB for reference");                        	
                }else if (verifyResult == 1){
                	params.put("repackage_check", "Pass");
                }else if (verifyResult == 2) {
                	params.put("repackage_check", "Fail");
                }
                
                //call smart contract
                if(verifyTxn(txn,pckgName,version)) {
                	//upload apk to IPFS
                	String ipfs_Hash = uploadIPFS(path);
                	System.out.println("The ipfs hash is: "+ipfs_Hash);
                	System.out.println("The txn is: " + txn);
                	UserDAO.updateHash(ipfs_Hash, txn);
                	params.put("Hash", ipfs_Hash);
                	JSONObject infoJson = new JSONObject();
                    infoJson.put("Hash", ipfs_Hash);
                    infoJson.put("package_name", params.get("package_name"));
                    infoJson.put("version_number", params.get("version_number"));
                    infoJson.put("originalUrl", url);
                    infoJson.put("certificates", params.get("certificates"));
                    infoJson.put("serial_number", certID);
                    infoJson.put("repackage_check", params.get("repackage_check"));
                    infoJson.put("txnID", txn);
                    StoreApp(infoJson);
                    params.put("call_result", "success"); //call_result.getString("call_result")
                    params.put("Result", "Success");
                }else {
                	params.put("Result", "Fail");
                }
            }else {
            	params.put("version_number", "undefined");
            	params.put("package_name", "undefined");
            	params.put("certificates", "undefined");
            	params.put("result", "Fail");
            }
            
            final_res.put("params", params); 
            long endTime=System.currentTimeMillis();
            System.out.println("Server side Phase II time:  "+(endTime-startTime)+"ms"); 
            out.print(final_res);
            
        }
	}

}
