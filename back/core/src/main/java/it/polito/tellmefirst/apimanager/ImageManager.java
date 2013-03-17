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

package it.polito.tellmefirst.apimanager;

import it.polito.tellmefirst.enhance.Enhancer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Created by IntelliJ IDEA.
 * User: Federico Cairo
 */
public class ImageManager {

    static Log LOG = LogFactory.getLog(ImageManager.class);

    public String scrapeImageFromPage(String pageURL) {
        LOG.debug("[scrapeImageFromPage] - BEGIN");
        String result = Enhancer.DEFAULT_IMAGE;
        try {
            Document doc = Jsoup.connect(pageURL).get();
            Element image = doc.select("img").first();
            result = image.attr("src");
        } catch (Exception e) {
            LOG.error("[scrapeImageFromPage] - EXCEPTION: ", e);
        }
        LOG.debug("[scrapeImageFromPage] - END");
        return result;
    }

    public int[] scrapeImageSizeFromPage(String pageURL) {
        LOG.debug("[scrapeImageSizeFromPage] - BEGIN");
        int[] result = {0,0};
        try {
            Document doc = Jsoup.connect(pageURL).get();
            Element image = doc.select("img").first();
            result[0] = Integer.valueOf(image.attr("width"));
            result[1] = Integer.valueOf(image.attr("height"));
        } catch (Exception e) {
            LOG.error("[scrapeImageSizeFromPage] - EXCEPTION: ", e);
        }
        LOG.debug("[scrapeImageSizeFromPage] - END");
        return result;
    }
}
