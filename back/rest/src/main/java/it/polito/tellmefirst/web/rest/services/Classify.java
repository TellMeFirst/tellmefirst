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

package it.polito.tellmefirst.web.rest.services;

import static it.polito.tellmefirst.util.TMFUtils.getFileExtension;
import static it.polito.tellmefirst.util.TMFUtils.parseAssociation;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import it.polito.tellmefirst.classify.Text;
import it.polito.tellmefirst.exception.TMFVisibleException;
import it.polito.tellmefirst.parsing.HTMLparser;
import it.polito.tellmefirst.parsing.TMFTextParser;
import it.polito.tellmefirst.web.rest.interfaces.ClassifyInterface;
import it.polito.tellmefirst.web.rest.utils.LangDetectUtils;

import java.io.File;
import java.io.FileInputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cybozu.labs.langdetect.LangDetectException;
import com.sun.jersey.multipart.FormDataParam;

@Path("/classify")
public class Classify {

	static Log LOG = LogFactory.getLog(Classify.class);
	private static ClassifyInterface classifyInterface = new ClassifyInterface();

	LangDetectUtils langDetectUtils = LangDetectUtils.getLangDetectUtils();

	private Response ok(String response) {
		return Response.ok().entity(response)
				.header("Access-Control-Allow-Origin", "*").build();
	}

	@POST
	@Consumes("multipart/form-data")
	@Produces(MediaType.APPLICATION_JSON)
	public Response postJSON(@FormDataParam("text") String inputText,
			@FormDataParam("file") File file, @FormDataParam("url") String url,
			@FormDataParam("fileName") String fileName,
			@FormDataParam("numTopics") int numTopics,
			@FormDataParam("lang") String lang,
			@FormDataParam("wikihtml") boolean wikihtml,
			@QueryParam("image_policy") @DefaultValue("BASIC") String imagePolicy,
			@QueryParam("optional_fields") String optionalFields) {
		LOG.debug("[postJSON] - BEGIN");
		LOG.info("Classify REST Service called with lang=" + lang);
		try {
			long startTime = System.currentTimeMillis();

			// retrieving input text/url/file as text string
			Text text;
			if (isNotEmpty(inputText)) {
				text = new Text(inputText);
			} else if (isNotEmpty(url)) {
				HTMLparser parser = new HTMLparser();
				text = new Text(parser.htmlToTextGoose(url));
			} else if (file != null) {
				TMFTextParser parser = parseAssociation
						.get(getFileExtension(fileName));
				if (parser == null) {
					throw new TMFVisibleException(
							"File extension not valid: only 'pdf', 'doc' and 'txt' allowed.");
				} else {
					text = new Text(parser.parse(file));
				}
			} else {
				throw new TMFVisibleException(
						"No valid parameters in your request: all 'text', 'url' and 'file' are null.");
			}
			String textString = text.getText();

			// automatically detect lang using langDetect library
			LOG.debug("auto-detecting lang");
			String detectLang = null;
			try {
					detectLang = langDetectUtils.detect(textString);
			} catch (LangDetectException e1) {
				LOG.debug("Error detecting lang " + e1.getMessage(), e1);
				detectLang = LangDetectUtils.defaultLang;
			}
			LOG.debug("detected lang=" + detectLang);
			ImagePolicy policy = ImagePolicy.valueOf(imagePolicy);
			LOG.debug("image policy: " + policy);
			String response = classifyInterface.getJSON(textString, numTopics,detectLang, wikihtml, optionalFields, policy);
			long endTime = System.currentTimeMillis();
			long duration = (endTime - startTime) / 1000;
			// no prod
			LOG.info("########### Classification took " + duration
					+ " seconds. ###########");
			LOG.debug("[postJSON] - END");
			return ok(response);
		} catch (Exception e) {
			throw new WebApplicationException(Response
					.status(Response.Status.BAD_REQUEST).entity(e.getMessage())
					.header("TMF-error", e.getMessage()).build());
		}
	}

	public static enum ImagePolicy { 
		BASIC, 
		CHECK, 
		RATIO, 
		WIKIPARSE,
		ALTERNATIVE, 
		ALTERNATIVE_WITH_RATIO
	};
	
}
