/*
 * TellMeFirst - A Knowledge Discovery Application.
 * Copyright (C) 2014 Federico Cairo, Giuseppe Futia, Federico Benedetto
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
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Giuseppe Futia
 * 
 */

function callClassifyAPI (url) {

	var data = new FormData();
    data.append('url', url)
    data.append('numTopics', 7)
    data.append('lang',"english")
    data.append('key',"")

    var options = {
        host: 'tellmefirst.polito.it',
        port: 2222,
        path: '/rest/classify',
        method: 'POST',
        headers: data.getHeaders()
    };

    var req = http.request(options, function (res) {
        res.setEncoding('utf8');
        res.on('data', function (chunk) {
        	try {
        		console.info(JSON.parse(chunk)["Resources"]);
        	} catch (e) {
               // TODO
        	}	
        });
    });
    data.pipe(req);
}