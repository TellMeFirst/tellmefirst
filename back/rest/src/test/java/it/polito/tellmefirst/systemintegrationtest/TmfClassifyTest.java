package it.polito.tellmefirst.systemintegrationtest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Assert;



//@RunWith(JUnit4.class)
public class TmfClassifyTest {
	
	static Log LOG = LogFactory.getLog(TmfClassifyTest.class);


	// String url = "http://localhost:81/ajax_it/rest/classify";
	HttpPost httpPost = null;
	DefaultHttpClient httpClient = null;
	boolean check = false;

	// endpoint rest classify (local)
	String url = "http://localhost:8888/rest/classify";
	
	// endpoint rest classify (remote)
	//String url = "http:/tmf.teamlife.it/rest/classify";

//	@Before
	public void beforeClassify() throws Exception {

		LOG.debug("Enter beforeClassify");
		httpClient = new DefaultHttpClient();
		httpPost = new HttpPost(url);
		StringBody text = new StringBody(
				"The final work of legendary director Stanley Kubrick, who died within a week"
						+ "of completing the edit, is based upon a novel by Arthur Schnitzler. Tom Cruise and"
						+ "Nicole Kidman play William and Alice Harford, a physician and a gallery manager who"
						+ "are wealthy, successful, and travel in a sophisticated social circle.");
		StringBody lang = new StringBody("english");
		StringBody numTopics = new StringBody("7");

		MultipartEntity reqEntity = new MultipartEntity();
		reqEntity.addPart("text", text);
		reqEntity.addPart("lang", lang);
		reqEntity.addPart("numTopics", numTopics);
		httpPost.setEntity(reqEntity);

	}

//	@org.junit.Test
	public void classify() throws Exception {

		LOG.debug("Enter execute classify");
		HttpResponse response = null;
		try {
			response = httpClient.execute(httpPost);
			System.out.println("httpClient.execute");
			HttpEntity resEntity = ((org.apache.http.HttpResponse) response)
					.getEntity();
			
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(
						resEntity.getContent(), "UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				resEntity.getContent().close();
			}

			LOG.debug("Response=" + writer.toString());

			Assert.assertNotNull(writer.toString());

			JSONObject jsonObj = new JSONObject(writer.toString());

			Assert.assertNotNull(jsonObj);
			String service = jsonObj.getString("service");
			Assert.assertNotNull(service);
			Assert.assertTrue(service.equals("Classify"));

			JSONArray resources = jsonObj.getJSONArray("Resources");
			Assert.assertNotNull(resources);
			Assert.assertTrue(resources.length() > 1);

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error=" + e.getMessage());
		}

	}

//	@AfterMethod()
	public void afterClassify() throws NamingException {

	}

}
