/**
 * TellMeFirst - A Knowledge Discovery Application
 *
 * Copyright (C) 2012 Federico Cairo, Giuseppe Futia, Federico Benedetto
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.polito.tellmefirst.classify.threads;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * Created by User
 */
public class GetWikiHtmlThread extends Thread {

	static Log LOG = LogFactory.getLog(GetWikiHtmlThread.class);
	private final CloseableHttpClient httpClient;
	private final HttpContext context;
	private final HttpGet httpget;
	private final int index;
	private final ArrayList<String[]> result;

	/**
	 * @param httpClient
	 * @param httpget
	 * @param result
	 * @param i
	 */
	public GetWikiHtmlThread(CloseableHttpClient httpClient, HttpGet httpget,
			ArrayList<String[]> result, int i) {
		this.httpClient = httpClient;
		this.context = HttpClientContext.create();
		this.httpget = httpget;
		this.index = i;
		this.result = result;
	}

	@Override
	public void run() {

		LOG.debug("[run] - BEGIN");
		LOG.debug("Thread " + this.getId() + " started.");
		long startTime = System.currentTimeMillis();

		HttpEntity entity = null;
		CloseableHttpResponse response = null;
		String wikihtml = null;
		String[] temp = null;
		JSONObject jsonObject = null;

		try {

			LOG.debug("Executing httpget = " + httpget.getURI());
			response = httpClient.execute(httpget, context);
			LOG.debug("Thread " + this.getId() + " get executed.");
			try {
				entity = response.getEntity();
				byte[] bytes = null;
				String resp = null;

				if (entity != null) {
					bytes = EntityUtils.toByteArray(entity);
					LOG.debug(" - " + bytes.length + " bytes read");
					resp = new String(bytes);
					jsonObject = new JSONObject(resp);
					wikihtml = (String) jsonObject.getJSONObject("parse")
							.getJSONObject("text").get("*");

					synchronized (result) {
						temp = result.get(index);
						temp[7] = wikihtml;
						result.set(index, temp);
					}

				}

			} catch (JSONException e) {
				LOG.error("[run] - EXCEPTION: ", e);
			} catch (Exception e) {
				LOG.error("[run] - EXCEPTION: ", e);
			} finally {
				response.close();
			}
		} catch (ClientProtocolException ex) {
			// Handle protocol errors
			LOG.error("[run] - EXCEPTION: ", ex);
		} catch (IOException ex) {
			// Handle I/O errors
			LOG.error("[run] - EXCEPTION: ", ex);
		}

		long endTime = System.currentTimeMillis();
		long duration = (endTime - startTime) / 1000;

		LOG.debug("Thread " + this.getId() + " finished and took " + duration
				+ " seconds. ###########");
		LOG.debug("[run] - END");
	}

}
