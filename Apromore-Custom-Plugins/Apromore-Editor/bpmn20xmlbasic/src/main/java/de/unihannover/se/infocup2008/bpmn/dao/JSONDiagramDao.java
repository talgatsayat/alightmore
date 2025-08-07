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
package de.unihannover.se.infocup2008.bpmn.dao;

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

import de.hpi.layouting.model.LayoutingBoundsImpl;
import de.unihannover.se.infocup2008.bpmn.model.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONDiagramDao {
    public BPMNDiagram getDiagramFromJSON(JSONObject node) throws JSONException {
        BPMNDiagramJSON dia = new BPMNDiagramJSON();
        walkChilds(node, dia, null);
        return dia;
    }

    /**
     * @param node
     * @param dia
     * @throws JSONException
     */
    private void walkChilds(JSONObject node, BPMNDiagramJSON dia, BPMNElement parent)
            throws JSONException {
        JSONArray shapes = node.getJSONArray("childShapes");
        for (int i = 0; i < shapes.length(); i++) {
            walkShape(shapes.getJSONObject(i), dia, parent);
        }
    }

    private void walkShape(JSONObject node, BPMNDiagramJSON dia, BPMNElement parent)
            throws JSONException {
        BPMNElementJSON elem = (BPMNElementJSON) dia.getElement(node.getString("resourceId"));
        elem.setElementJSON(node);
        JSONObject stencil = node.getJSONObject("stencil");
        elem.setType(BPMNType.PREFIX + stencil.getString("id"));
        elem.setParent(parent);

        JSONArray outLinks = node.getJSONArray("outgoing");
        for (int i = 0; i < outLinks.length(); i++) {
            JSONObject link = outLinks.getJSONObject(i);
            BPMNElementJSON target = (BPMNElementJSON) dia.getElement(link.getString("resourceId"));
            elem.addOutgoingLink(target);
            target.addIncomingLink(elem);
        }
        JSONObject bounds = node.getJSONObject("bounds");
        double x = bounds.getJSONObject("upperLeft").getDouble("x");
        double y = bounds.getJSONObject("upperLeft").getDouble("y");
        double x2 = bounds.getJSONObject("lowerRight").getDouble("x");
        double y2 = bounds.getJSONObject("lowerRight").getDouble("y");
        elem.setGeometry(new LayoutingBoundsImpl(x, y, x2 - x, y2 - y));
        elem.setBoundsJSON(bounds);

        JSONArray dockers = node.getJSONArray("dockers");
        elem.getDockers().getPoints().clear();
        for (int i = 0; i < dockers.length(); i++) {
            JSONObject point = dockers.getJSONObject(i);
            elem.getDockers().addPoint(point.getDouble("x"), point.getDouble("y"));
        }
        elem.setDockersJSON(dockers);

        walkChilds(node, dia, elem);

    }
}
