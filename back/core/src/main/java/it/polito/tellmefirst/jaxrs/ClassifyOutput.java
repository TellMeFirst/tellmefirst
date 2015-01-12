/*-
 * Copyright (C) 2014 Riccardo Muzzì.
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
package it.polito.tellmefirst.jaxrs;

public class ClassifyOutput {
    private String uri, label, title, score, mergedTypes, image, wikilink;

    public String getUri() {
        return uri;
    }

    public void setUri(String s) {
        uri = s;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String s) {
        label = s;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String s) {
        title = s;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String s) {
        score = s;
    }

    public String getMergedTypes() {
        return mergedTypes;
    }

    public void setMergedTypes(String s) {
        mergedTypes = s;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String s) {
        image = s;
    }

    public String getWikilink() {
        return wikilink;
    }

    public void setWikilink(String s) {
        wikilink = s;
    }
}
