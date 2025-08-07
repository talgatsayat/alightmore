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
package de.hpi.bpmn2_0.util;

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

import org.oryxeditor.server.diagram.Bounds;
import org.oryxeditor.server.diagram.Point;
import org.oryxeditor.server.diagram.generic.GenericDiagram;
import org.oryxeditor.server.diagram.generic.GenericEdge;
import org.oryxeditor.server.diagram.generic.GenericShape;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sven Wagner-Boysen
 */
public class DiagramHelper {

    @Deprecated
    public static Bounds getAbsoluteBounds(GenericShape shape) {
        /* Handle invalid diagram and prevent from crashing the transformation */
        if (shape.getBounds() == null) {
            Bounds bounds = new Bounds(new Point(0.0, 0.0), new Point(0.0, 0.0));
            return bounds;
        }
        // clone bounds
        Bounds bounds = cloneBounds(shape.getBounds());

        GenericShape parent = shape.getParent();

        if (parent != null) {
            Bounds parentBounds = getAbsoluteBounds(parent);
            setBounds(bounds, parentBounds.getUpperLeft().getX().doubleValue(),
                    parentBounds.getUpperLeft().getY().doubleValue());
        }

        return bounds;
    }


    public static List<GenericEdge> getOutgoingEdges(GenericShape<?, ?> shape) {
        List<GenericEdge> list = new ArrayList<GenericEdge>();
        for (GenericShape outgoing : shape.getOutgoingsReadOnly()) {
            if (outgoing instanceof GenericEdge)
                list.add((GenericEdge) outgoing);
        }
        return list;
    }


    public static List<GenericEdge> getIncomingEdges(GenericShape<?, ?> shape) {
        List<GenericEdge> list = new ArrayList<GenericEdge>();
        for (GenericShape incoming : shape.getIncomingsReadOnly()) {
            if (incoming instanceof GenericEdge)
                list.add((GenericEdge) incoming);
        }
        return list;
    }


    public static List<GenericEdge> getAllEdges(GenericDiagram<?, ?> diagram) {
        List<GenericEdge> list = new ArrayList<GenericEdge>();
        for (GenericShape shape : diagram.getAllShapesReadOnly()) {
            if (shape instanceof GenericEdge) {
                GenericEdge edge = (GenericEdge) shape;
                if (edge.getSource() != null && "ConfigurationAnnotationShape".equals(edge.getSource().getStencilId())) { continue; }
                if (edge.getTarget() != null && "ConfigurationAnnotationShape".equals(edge.getTarget().getStencilId())) { continue; }
                list.add((GenericEdge) shape);
            }
        }
        return list;
    }


    public static double calculateCenterDistance(
            de.hpi.bpmn2_0.model.bpmndi.dc.Bounds b1,
            de.hpi.bpmn2_0.model.bpmndi.dc.Bounds b2) {

        de.hpi.bpmn2_0.model.bpmndi.dc.Point b1Center = new de.hpi.bpmn2_0.model.bpmndi.dc.Point();
        b1Center.setX(b1.getX() + b1.getWidth() / 2.0);
        b1Center.setY(b1.getY() + b1.getHeight() / 2.0);

        de.hpi.bpmn2_0.model.bpmndi.dc.Point b2Center = new de.hpi.bpmn2_0.model.bpmndi.dc.Point();
        b2Center.setX(b2.getX() + b2.getWidth() / 2.0);
        b2Center.setY(b2.getY() + b2.getHeight() / 2.0);

        return Math.sqrt(Math.pow(b1Center.getX() - b2Center.getX(), 2.0)
                + Math.pow(b1Center.getY() - b2Center.getY(), 2.0));
    }

    @Deprecated
    private static Bounds cloneBounds(Bounds bounds) {
        double lrx = (bounds.getLowerRight().getX() != null ? bounds
                .getLowerRight().getX() : 0.0);
        double lry = (bounds.getLowerRight().getY() != null ? bounds
                .getLowerRight().getY() : 0.0);
        double ulx = (bounds.getUpperLeft().getX() != null ? bounds
                .getUpperLeft().getX() : 0.0);
        double uly = (bounds.getUpperLeft().getY() != null ? bounds
                .getUpperLeft().getY() : 0.0);

        return new Bounds(new Point(lrx, lry), new Point(ulx, uly));
    }

    @Deprecated
    private static void setBounds(Bounds bounds, double offsetX, double offsetY) {
        Point ul = bounds.getUpperLeft();
        ul.setX(ul.getX() + offsetX);
        ul.setY(ul.getY() + offsetY);
        Point lr = bounds.getLowerRight();
        lr.setX(lr.getX() + offsetX);
        lr.setY(lr.getY() + offsetY);
    }

}
