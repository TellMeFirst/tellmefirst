/**
 * TellMeFirst - A Knowledge Discovery Application
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

package it.polito.tellmefirst.web.rest.utils;

import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import com.cybozu.labs.langdetect.Language;

/**
 * Created by User:
 */
public class LangDetectUtils {

	static Log LOG = LogFactory.getLog(LangDetectUtils.class);

	private static LangDetectUtils langDetectUtils;

	public static final String defaultLang = "english";
	public static final HashMap<String, String> mapLangCode = new HashMap<String, String>();
	static {
		mapLangCode.put("en", "english");
		mapLangCode.put("it", "italian");
		// ... TODO if other languages will be supported
	}

	/** A private Constructor prevents any other class from instantiating. */
	private LangDetectUtils() {
		try {

			String dirname = "profiles/";
			Enumeration<URL> en = Detector.class.getClassLoader().getResources(
					dirname);
			List<String> profiles = new ArrayList<String>();
			if (en.hasMoreElements()) {
				URL url = en.nextElement();
				JarURLConnection urlcon = (JarURLConnection) url
						.openConnection();
				JarFile jar = urlcon.getJarFile();
				Enumeration<JarEntry> entries = jar.entries();
				while (entries.hasMoreElements()) {
					String entry = entries.nextElement().getName();
					if (entry.startsWith(dirname)) {
						InputStream in = Detector.class.getClassLoader()
								.getResourceAsStream(entry);
						profiles.add(IOUtils.toString(in));
					}
				}

			}
			DetectorFactory.loadProfile(profiles);
		} catch (LangDetectException e) {
			LOG.error("Error loading profiles directory " + e.getMessage());
		} catch (Exception e) {
			LOG.error("Error loading profiles directory " + e.getMessage());
		}
	}

	public static synchronized LangDetectUtils getLangDetectUtils() {
		if (langDetectUtils == null) {
			langDetectUtils = new LangDetectUtils();
		}
		return langDetectUtils;
	}

	public String detect(String text) throws LangDetectException {

		String langCode = null;
		String mappedLang = null;

		Detector detector = DetectorFactory.create();
		detector.append(text);

		langCode = detector.detect();
		LOG.debug("langCode=" + langCode);

		mappedLang = mapLangCode.get(langCode);
		if (mappedLang == null)
			mappedLang = defaultLang;
		LOG.debug("mappedLang=" + langCode);

		return mappedLang;
	}

	public ArrayList<Language> detectLangs(String text)
			throws LangDetectException {
		Detector detector = DetectorFactory.create();
		detector.append(text);
		return detector.getProbabilities();
	}

	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

}
