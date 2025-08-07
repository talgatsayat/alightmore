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

package de.hpi.bpmn2_0.transformation;

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

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

import java.util.HashMap;
import java.util.Map;


/**
 * The namespace prefix mapper is responsible for the creation of user friendly
 * namespace prefixes in the BPMN 2.0 XML document.
 *
 * @author Sven Wagner-Boysen
 */
public class BPMNPrefixMapper extends NamespacePrefixMapper {

    private Map<String, String> nsDefs;

    private static Map<String, String> customExtensions = new HashMap<String, String>();

    /* (non-Javadoc)
      * @see com.sun.xml.bind.marshaller.NamespacePrefixMapper#getPreferredPrefix(java.lang.String, java.lang.String, boolean)
      */
    // @Override
    public String getPreferredPrefix(String namespace, String suggestion, boolean isRequired) {

        /* BPMN 2.0 Standard Namespaces */
        if (namespace.equals("http://www.omg.org/spec/BPMN/20100524/MODEL"))
            return "";
        else if (namespace.equals("http://www.omg.org/spec/BPMN/20100524/DI"))
            return "bpmndi";
        else if (namespace.equals("http://www.w3.org/2001/XMLSchema-instance"))
            return "xsi";
        else if (namespace.equals("http://www.omg.org/spec/DD/20100524/DI"))
            return "omgdi";
        else if (namespace.equals("http://www.omg.org/spec/DD/20100524/DC"))
            return "omgdc";

            /* Signavio */
        else if (namespace.equals("http://www.signavio.com"))
            return "signavio";

            /* Check custom extension */
        else if (getCustomExtensions().get(namespace) != null) {
            return getCustomExtensions().get(namespace);
        }

        /* Check namespace definitions from external XML elements */
        return getNsDefs().get(namespace);

    }

    public String[] getPreDeclaredNamespaceUris() {
        super.getPreDeclaredNamespaceUris();
        String[] s = {};
        return this.getNsDefs().keySet().toArray(s);
    }

    public static Map<String, String> getCustomExtensions() {

        Constants c = Diagram2BpmnConverter.getConstants();
        if (c == null) {
            return new HashMap<String, String>();
        }

        return new HashMap<String, String>(c.getCustomNamespacePrefixMappings());
    }

    /* Getter & Setter */

    public Map<String, String> getNsDefs() {
        if (nsDefs == null) {
            nsDefs = new HashMap<String, String>();
        }

        return nsDefs;
    }

    public void setNsDefs(Map<String, String> nsDefs) {
        this.nsDefs = nsDefs;
    }

}
