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

package it.polito.tellmefirst.web.rest.apimanager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Federico Cairo
 */
public class VideoManager {

    static Log LOG = LogFactory.getLog(VideoManager.class);

    public String extractVideoIdFromResult(String input) {
        LOG.debug("[extractVideoIdFromResult] - BEGIN");
        String result;
        Document doc = Jsoup.parse(input);
        String idDirty = doc.select("id").get(1).text();
        System.out.println("ID dirty: "+ idDirty);
        String[] idArray = idDirty.split("video:");
        result = idArray[idArray.length-1];
        LOG.debug("[extractVideoIdFromResult] - END");
        return result;
    }

    // just for testing
    public static void main(String[] args) throws IOException {
        VideoManager vm = new VideoManager();
        // Ludovico Einaudi, Una Mattina
        System.out.println(vm.extractVideoIdFromResult("<?xml version='1.0' encoding='UTF-8'?><feed xmlns='ht" +
                "tp://www.w3.org/2005/Atom' xmlns:media='http://search.yahoo.com/mrss/' xmlns:openSearch='htt" +
                "p://a9.com/-/spec/opensearch/1.1/' xmlns:gd='http://schemas.google.com/g/2005' xmlns:yt='htt" +
                "p://gdata.youtube.com/schemas/2007' gd:etag='W/&quot;DEUFRnYyfSp7I2A9WhJXGEo.&quot;'><id>tag" +
                ":youtube.com,2008:videos</id><updated>2012-08-13T16:43:37.895Z</updated><category scheme='ht" +
                "tp://schemas.google.com/g/2005#kind' term='http://gdata.youtube.com/schemas/2007#video'/><ti" +
                "tle>YouTube Videos matching query: Ludovico Einaudi Una Mattina</title><logo>http://www.yout" +
                "ube.com/img/pic_youtubelogo_123x63.gif</logo><link rel='alternate' type='text/html' href='ht" +
                "tps://www.youtube.com'/><link rel='http://schemas.google.com/g/2005#feed' type='application/" +
                "atom+xml' href='https://gdata.youtube.com/feeds/api/videos?v=2'/><link rel='http://schemas.g" +
                "oogle.com/g/2005#batch' type='application/atom+xml' href='https://gdata.youtube.com/feeds/ap" +
                "i/videos/batch?v=2'/><link rel='self' type='application/atom+xml' href='https://gdata.youtub" +
                "e.com/feeds/api/videos?q=Ludovico+Einaudi+Una+Mattina&amp;start-index=1&amp;max-results=1&am" +
                "p;hd=true&amp;v=2'/><link rel='service' type='application/atomsvc+xml' href='https://gdata.y" +
                "outube.com/feeds/api/videos?alt=atom-service&amp;v=2'/><link rel='next' type='application/at" +
                "om+xml' href='https://gdata.youtube.com/feeds/api/videos?q=Ludovico+Einaudi+Una+Mattina&amp;" +
                "start-index=2&amp;max-results=1&amp;hd=true&amp;v=2'/><author><name>YouTube</name><uri>http:" +
                "//www.youtube.com/</uri></author><generator version='2.1' uri='http://gdata.youtube.com'>You" +
                "Tube data API</generator><openSearch:totalResults>1154</openSearch:totalResults><openSearch:" +
                "startIndex>1</openSearch:startIndex><openSearch:itemsPerPage>1</openSearch:itemsPerPage><ent" +
                "ry gd:etag='W/&quot;D0UDR347eCp7I2A9WhJXGEo.&quot;'><id>tag:youtube.com,2008:video:hHsnECVc_" +
                "DE</id><published>2011-12-04T20:20:45.000Z</published><updated>2012-08-13T16:27:56.000Z</upd" +
                "ated><category scheme='http://schemas.google.com/g/2005#kind' term='http://gdata.youtube.com" +
                "/schemas/2007#video'/><category scheme='http://gdata.youtube.com/schemas/2007/categories.cat" +
                "' term='Music' label='Musica'/><category scheme='http://gdata.youtube.com/schemas/2007/keywo" +
                "rds.cat' term='BO'/><category scheme='http://gdata.youtube.com/schemas/2007/keywords.cat' te" +
                "rm='Film'/><category scheme='http://gdata.youtube.com/schemas/2007/keywords.cat' term='Intou" +
                "chables'/><category scheme='http://gdata.youtube.com/schemas/2007/keywords.cat' term='Ludovi" +
                "co'/><category scheme='http://gdata.youtube.com/schemas/2007/keywords.cat' term='Einaudi'/><" +
                "category scheme='http://gdata.youtube.com/schemas/2007/keywords.cat' term='Una'/><category s" +
                "cheme='http://gdata.youtube.com/schemas/2007/keywords.cat' term='Mattina'/><category scheme=" +
                "'http://gdata.youtube.com/schemas/2007/keywords.cat' term='Cinéma'/><category scheme='http:/" +
                "/gdata.youtube.com/schemas/2007/keywords.cat' term='Musique'/><category scheme='http://gdata" +
                ".youtube.com/schemas/2007/keywords.cat' term='Fin'/><category scheme='http://gdata.youtube.c" +
                "om/schemas/2007/keywords.cat' term='Omar'/><category scheme='http://gdata.youtube.com/schema" +
                "s/2007/keywords.cat' term='Sy'/><category scheme='http://gdata.youtube.com/schemas/2007/keyw" +
                "ords.cat' term='François'/><category scheme='http://gdata.youtube.com/schemas/2007/keywords." +
                "cat' term='Cluzet'/><category scheme='http://gdata.youtube.com/schemas/2007/keywords.cat' te" +
                "rm='Cover'/><category scheme='http://gdata.youtube.com/schemas/2007/keywords.cat' term='Pian" +
                "o'/><title>BO Film \"Intouchables\" - Ludovico Einaudi - Una Mattina</title><content type='a" +
                "pplication/x-shockwave-flash' src='https://www.youtube.com/v/hHsnECVc_DE?version=3&amp;f=vid" +
                "eos&amp;app=youtube_gdata'/><link rel='alternate' type='text/html' href='https://www.youtube" +
                ".com/watch?v=hHsnECVc_DE&amp;feature=youtube_gdata'/><link rel='http://gdata.youtube.com/sch" +
                "emas/2007#video.responses' type='application/atom+xml' href='https://gdata.youtube.com/feeds" +
                "/api/videos/hHsnECVc_DE/responses?v=2'/><link rel='http://gdata.youtube.com/schemas/2007#vid" +
                "eo.related' type='application/atom+xml' href='https://gdata.youtube.com/feeds/api/videos/hHs" +
                "nECVc_DE/related?v=2'/><link rel='http://gdata.youtube.com/schemas/2007#mobile' type='text/h" +
                "tml' href='https://m.youtube.com/details?v=hHsnECVc_DE'/><link rel='http://gdata.youtube.com" +
                "/schemas/2007#uploader' type='application/atom+xml' href='https://gdata.youtube.com/feeds/ap" +
                "i/users/e7PBYB0wR7lqHyR7OFcZBQ?v=2'/><link rel='self' type='application/atom+xml' href='http" +
                "s://gdata.youtube.com/feeds/api/videos/hHsnECVc_DE?v=2'/><author><name>MrNewsMusic</name><ur" +
                "i>https://gdata.youtube.com/feeds/api/users/MrNewsMusic</uri><yt:userId>e7PBYB0wR7lqHyR7OFcZ" +
                "BQ</yt:userId></author><yt:accessControl action='comment' permission='allowed'/><yt:accessCo" +
                "ntrol action='commentVote' permission='allowed'/><yt:accessControl action='videoRespond' per" +
                "mission='moderated'/><yt:accessControl action='rate' permission='allowed'/><yt:accessControl" +
                " action='embed' permission='allowed'/><yt:accessControl action='list' permission='allowed'/>" +
                "<yt:accessControl action='autoPlay' permission='allowed'/><yt:accessControl action='syndicat" +
                "e' permission='allowed'/><gd:comments><gd:feedLink rel='http://gdata.youtube.com/schemas/200" +
                "7#comments' href='https://gdata.youtube.com/feeds/api/videos/hHsnECVc_DE/comments?v=2' count" +
                "Hint='2131'/></gd:comments><yt:hd/><media:group><media:category label='Musica' scheme='http:" +
                "//gdata.youtube.com/schemas/2007/categories.cat'>Music</media:category><media:content url='h" +
                "ttps://www.youtube.com/v/hHsnECVc_DE?version=3&amp;f=videos&amp;app=youtube_gdata' type='app" +
                "lication/x-shockwave-flash' medium='video' isDefault='true' expression='full' duration='401'" +
                " yt:format='5'/><media:content url='rtsp://v3.cache4.c.youtube.com/CiILENy73wIaGQkx_FwlECd7h" +
                "BMYDSANFEgGUgZ2aWRlb3MM/0/0/0/video.3gp' type='video/3gpp' medium='video' expression='full' " +
                "duration='401' yt:format='1'/><media:content url='rtsp://v3.cache5.c.youtube.com/CiILENy73wI" +
                "aGQkx_FwlECd7hBMYESARFEgGUgZ2aWRlb3MM/0/0/0/video.3gp' type='video/3gpp' medium='video' expr" +
                "ession='full' duration='401' yt:format='6'/><media:credit role='uploader' scheme='urn:youtub" +
                "e' yt:display='MrNewsMusic'>mrnewsmusic</media:credit><media:description type='plain'>BO du " +
                "Film \"Intouchables\" - Ludovico Einaudi - Una Mattina</media:description><media:keywords>BO" +
                ", Film, Intouchables, Ludovico, Einaudi, Una, Mattina, Cinéma, Musique, Fin, Omar, Sy, Franç" +
                "ois, Cluzet, Cover, Piano</media:keywords><media:license type='text/html' href='http://www.y" +
                "outube.com/t/terms'>youtube</media:license><media:player url='https://www.youtube.com/watch?" +
                "v=hHsnECVc_DE&amp;feature=youtube_gdata_player'/><media:thumbnail url='http://i.ytimg.com/vi" +
                "/hHsnECVc_DE/default.jpg' height='90' width='120' time='00:03:20.500' yt:name='default'/><me" +
                "dia:thumbnail url='http://i.ytimg.com/vi/hHsnECVc_DE/mqdefault.jpg' height='180' width='320'" +
                " yt:name='mqdefault'/><media:thumbnail url='http://i.ytimg.com/vi/hHsnECVc_DE/hqdefault.jpg'" +
                " height='360' width='480' yt:name='hqdefault'/><media:thumbnail url='http://i.ytimg.com/vi/h" +
                "HsnECVc_DE/1.jpg' height='90' width='120' time='00:01:40.250' yt:name='start'/><media:thumbn" +
                "ail url='http://i.ytimg.com/vi/hHsnECVc_DE/2.jpg' height='90' width='120' time='00:03:20.500" +
                "' yt:name='middle'/><media:thumbnail url='http://i.ytimg.com/vi/hHsnECVc_DE/3.jpg' height='9" +
                "0' width='120' time='00:05:00.750' yt:name='end'/><media:title type='plain'>BO Film \"Intouc" +
                "hables\" - Ludovico Einaudi - Una Mattina</media:title><yt:aspectRatio>widescreen</yt:aspect" +
                "Ratio><yt:duration seconds='401'/><yt:uploaded>2011-12-04T20:20:45.000Z</yt:uploaded><yt:upl" +
                "oaderId>UCe7PBYB0wR7lqHyR7OFcZBQ</yt:uploaderId><yt:videoid>hHsnECVc_DE</yt:videoid></media:" +
                "group><gd:rating average='4.9742002' max='5' min='1' numRaters='15349' rel='http://schemas.g" +
                "oogle.com/g/2005#overall'/><yt:statistics favoriteCount='13034' viewCount='3332813'/><yt:rat" +
                "ing numDislikes='99' numLikes='15250'/></entry></feed>"));
    }
}
