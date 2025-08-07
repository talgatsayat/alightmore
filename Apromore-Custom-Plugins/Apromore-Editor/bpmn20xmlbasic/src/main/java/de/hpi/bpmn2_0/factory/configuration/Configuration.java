/*-
 * #%L
 * This file is part of "Apromore Core".
 * 
 * Copyright (C) 2015 - 2017 Queensland University of Technology.
 * %%
 * Copyright (C) 2018 - 2022 Apromore Pty Ltd.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

package de.hpi.bpmn2_0.factory.configuration;

/**
 * Copyright (c) 2006
 *
 * Philipp Berger, Martin Czuchra, Gero Decker, Ole Eckermann, Lutz Gericke,
 * Alexander Hold, Alexander Koglin, Oliver Kopp, Stefan Krumnow,
 * Matthias Kunze, Philipp Maschke, Falko Menge, Christoph Neijenhuis,
 * Hagen Overdick, Zhen Peng, Nicolas Peters, Kerstin Pfitzner, Daniel Polak,
 * Steffen Ryll, Kai Schlichting, Jan-Felix Schwarz, Daniel Taschik,
 * Willi Tscheschner, Björn Wagner, Sven Wagner-Boysen, Matthias Weidlich
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 **/

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A configuration holds information about linked diagrams, custom defined
 * attributes and the editor version.
 *
 * @author Sven Wagner-Boysen
 */
public class Configuration {
    /* Attributes */

    private String editorVersion;
    private Map<String, Set<String>> metaData;
    private Map<String, LinkedModel> linkedModels;

    public static boolean ensureSignavioStyleDefault = false;

    /*
      * Flag to enforce a check on IDs of style 'sid-...'
      */
    public boolean ensureSignavioStyle = ensureSignavioStyleDefault;


    /* Constructors */
    public Configuration() {
        super();
    }

    @SuppressWarnings("unchecked")
    public Configuration(Map<String, Object> configuration) {
        super();

        setEditorVersion((String) configuration.get("editorVersion"));

        if (configuration.get("metaData") != null && configuration.get("metaData") instanceof Map<?, ?>) {
            getMetaData().putAll((Map<? extends String, ? extends Set<String>>) configuration.get("metaData"));
        }

        if (configuration.get("linkedModels") != null && configuration.get("linkedModels") instanceof List<?>) {
            extractLinkedModels((List<Map<String, Object>>) configuration.get("linkedModels"));
        }

    }

    private void extractLinkedModels(List<Map<String, Object>> linkedModelsList) {
        for (Map<String, Object> linkedModelMap : linkedModelsList) {
            LinkedModel model = new LinkedModel(linkedModelMap);
            getLinkedModels().put(model.getId(), model);
        }
    }

    /* Getter & Setter */

    public String getEditorVersion() {
        return editorVersion;
    }


    public void setEditorVersion(String editorVersion) {
        this.editorVersion = editorVersion;
    }


    public Map<String, Set<String>> getMetaData() {
        if (metaData == null) {
            metaData = new HashMap<String, Set<String>>();
        }
        return metaData;
    }


    public Map<String, LinkedModel> getLinkedModels() {
        if (linkedModels == null) {
            linkedModels = new HashMap<String, LinkedModel>();
        }

        return linkedModels;
    }
}
