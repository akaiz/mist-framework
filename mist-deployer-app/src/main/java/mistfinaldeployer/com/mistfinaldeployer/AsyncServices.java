package mistfinaldeployer.com.mistfinaldeployer;

import com.google.gson.Gson;
import okhttp3.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import java.util.concurrent.Future;

@Service
public class AsyncServices {
	
	Logger log = LoggerFactory.getLogger(this.getClass().getName());

	@Async
	public Future<String> deploy(Node node, CredentialsProvider credsProvider, int i , String baseFolder) throws ClientProtocolException, IOException {
		String mistFilesPath =node.mist_files_path;
		System.out.println(node.getPayload());
		CsvFile.write(node.processId+",node"+i,"Process Start",baseFolder);
		File war = new File(mistFilesPath+"mist-0.war");
		File payload_file = null;
		if(node.getPayload().equals("yes")){
			if(node.getPlatform().equals("cloud")){
				payload_file = new File(mistFilesPath+"payload-heavy2.jpeg");
			}
			else {
				payload_file = new File(mistFilesPath+"payload-heavy.jpeg");
			}
		}


		File mist_file = new File(mistFilesPath+node.getMist_file());
		System.out.println("node--->"+node.getNode_one());
		System.out.println("node--->"+node.getNode_two());

		if(i==1){

			return new AsyncResult<>(deployToCamunda(node.processId+",mist-one",node.getNode_one(),mist_file,war,baseFolder,payload_file,node.getPlatform()));
		}
		else if(i==2){

			return new AsyncResult<>(deployToCamunda(node.processId+",mist-two",node.getNode_two(),mist_file,war,baseFolder,payload_file,node.getPlatform()));
		}
		else{
			return new AsyncResult<>(deployToCamunda(node.processId,node.getNode_one(),mist_file,war,baseFolder,payload_file,node.getPlatform()));
		}
	}

	public   String deployToCamunda(String processId, String url , File mistFile, File war,String baseFolder,File payload,String platform) throws IOException {

		String mist = null;
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(AuthScope.ANY,new UsernamePasswordCredentials("tomcat", "tomcat"));
		BufferedReader br = new BufferedReader(new FileReader(mistFile));
		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				if(line.contains("log_id")){
					line =" \"log_id\":{\"value\" :\""+processId+"\",\"type\": \"String\"},";
				}
				if(line.contains("camundaHost")){
					line =" \"camundaHost\":{\"value\" :\""+url+"\",\"type\": \"String\"}";
				}


				sb.append(line);
				sb.append("\n");
				line = br.readLine();

			}
			mist=sb.toString();
		} finally {
			br.close();
		}
		System.out.println("Making deployment to Camunda");
	       return depolyCamunda(war,url,processId,baseFolder,mist,payload,platform);

	}
	public String depolyCamunda(File filename , String url,String processId,String baseFolder, String mist,File payload,String platform) throws IOException {
		MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
		if(payload!=null){
			System.out.println("File image sending");
			CsvFile.write(processId,"Uploading payload started",baseFolder);
			OkHttpClient client = new OkHttpClient();
			final MediaType MEDIA_TYPE = MediaType.parse(mimeTypesMap.getContentType(payload));
			if(platform.equals("cloud")){
				okhttp3.RequestBody requestBody = new MultipartBody.Builder()
						.setType(MultipartBody.FORM)
						.addFormDataPart("baseFolder", baseFolder)
						.addFormDataPart("processId", processId)
						.addFormDataPart("payload", payload.getName(),okhttp3.RequestBody.create(MEDIA_TYPE, payload))
						.addFormDataPart("payload2", payload.getName(),okhttp3.RequestBody.create(MEDIA_TYPE, payload))
						.build();
				Request request = new Request.Builder()
						.url(url+":8098/deploy/image")
						.post(requestBody)
						.build();
				okhttp3.Response response = client.newCall(request).execute();
				if (response.isSuccessful() ) {
					response.body().close();
				}
			}else{

				okhttp3.RequestBody requestBody = new MultipartBody.Builder()
						.setType(MultipartBody.FORM)
						.addFormDataPart("baseFolder", baseFolder)
						.addFormDataPart("processId", processId)
						.addFormDataPart("payload", payload.getName(),
								okhttp3.RequestBody.create(MEDIA_TYPE, filename))
						.build();
				Request request = new Request.Builder()
						.url(url+":8098/deploy/image")
						.post(requestBody)
						.build();
				okhttp3.Response response = client.newCall(request).execute();
				if (response.isSuccessful() ) {
					response.body().close();
				}
			}



			CsvFile.write(processId,"Uploading payload finished",baseFolder);
			System.out.println("File upload image sending");
		}

		CsvFile.write(processId,"Uploading war started",baseFolder);
		OkHttpClient client = new OkHttpClient();

		final MediaType MEDIA_TYPE = MediaType.parse(mimeTypesMap.getContentType(filename));
		okhttp3.RequestBody requestBody = new MultipartBody.Builder()
				.setType(MultipartBody.FORM)
				.addFormDataPart("attachment", filename.getName(),
						okhttp3.RequestBody.create(MEDIA_TYPE, filename))
				.build();
		String credential = Credentials.basic("tomcat", "tomcat");
		Request request = new Request.Builder()
				.header("Authorization", credential)
				.url(url+":8080/manager/text/deploy?path=/mistBpmn&update=true")
				.put(requestBody)
				.build();

		okhttp3.Response response = client.newCall(request).execute();
		CsvFile.write(processId,"Uploading war finished",baseFolder);
		System.out.println("Deploy finished");
			response.body().close();
			CsvFile.write(processId, "Finished deployment to Camunda", baseFolder);
			CsvFile.write(processId, "Start  Remote Process Engine ", baseFolder);
			System.out.println("Request being processed .......................");

			MediaType JSON
					= MediaType.parse("application/json; charset=utf-8");
			RequestBody requestBody2 = RequestBody.create(JSON, mist);

			OkHttpClient client2 = new OkHttpClient();

			Request request2 = new Request.Builder()
					.header("Authorization", credential)
					.url(url + ":8080/engine-rest/message")
					.post(requestBody2)
					.build();
			okhttp3.Response response2 = client2.newCall(request2).execute();
			response2.body().close();
			System.out.println("Request process end");
			return "done";

	}
	public String executeRequest(HttpRequestBase requestBase, CredentialsProvider credsProvider) throws ClientProtocolException {
		CloseableHttpClient client = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
		InputStream responseStream = null;
		String res = null;
		HttpResponse response = null;
		try {
			response = client.execute(requestBase);
		} catch (IOException e) {
			System.out.println("Retry sending request");
			executeRequest(requestBase,credsProvider);
		}
		HttpEntity responseEntity = response.getEntity() ;
		try {
			responseStream = responseEntity.getContent() ;
		} catch (IOException e) {
			e.printStackTrace();
		}

		BufferedReader br = new BufferedReader (new InputStreamReader (responseStream)) ;
		StringBuilder sb = new StringBuilder();
		String line;
		try {
			while ((line = br.readLine()) != null) {
				sb.append(line);
				sb.append(System.getProperty("line.separator"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			br.close() ;
		} catch (IOException e) {
			e.printStackTrace();
		}
		res = sb.toString();

		return res;
	}
   @Async
	public  void undeploy(String localhost) throws ClientProtocolException, IOException{
	   CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(AuthScope.ANY,new UsernamePasswordCredentials("tomcat", "tomcat"));
		String url = localhost+":8080/manager/text/undeploy?path=/mistBpmn";
		HttpGet req = new HttpGet(url) ;
		String response = executeRequest (req, credsProvider);

	}
	}


