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

package org.oryxeditor.server.diagram.generic;

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

import org.oryxeditor.server.diagram.Point;
import org.oryxeditor.server.diagram.exception.TooManyDockersException;

import java.util.List;

/**
 * Represents a node element in a diagram. Nodes may only have at most one docker.
 *
 * @param <S> the actual type of shape to be used (must inherit from {@link GenericShape}); calls to {@link #getChildShapesReadOnly()}, ... will return this type
 * @param <D> the actual type of diagram to be used (must inherit from {@link GenericDiagram}); {@link #getDiagram()} will return this type
 * @author Philipp Maschke, Robert Gurol
 */
public abstract class GenericNode<S extends GenericShape<S, D>, D extends GenericDiagram<S, D>> extends GenericShapeImpl<S, D> {

    public GenericNode(String resourceId) {
        super(resourceId);
    }

    public GenericNode(String resourceId, String stencilId) {
        super(resourceId, stencilId);
    }


    /**
     * Sets the given docker as the nodes docker.
     *
     * @param p
     * @throws TooManyDockersException if the node already has a docker (nodes may only have one docker)
     */
    @Override
    public void addDocker(Point p) throws TooManyDockersException {
        if (!getDockersReadOnly().isEmpty())
            throw new TooManyDockersException("Trying to set more than 1 docker for node '" + getResourceId() + "'");
        super.addDocker(p);
    }

    /**
     * Sets the given docker as the nodes docker.
     *
     * @param p
     * @param position
     * @throws TooManyDockersException if the node already has a docker (nodes may only have one docker)
     */
    @Override
    public void addDocker(Point p, int position) {
        if (!getDockersReadOnly().isEmpty())
            throw new TooManyDockersException("Trying to set more than 1 docker for node '" + getResourceId() + "'");

        super.addDocker(p, position);
    }

    /**
     * Sets the given list of dockers as the nodes dockers.
     * <p/>
     * <b>Beware:</b> Nodes may only have one docker -> the given list may only have 0 or 1 points or an exception will be thrown
     *
     * @param dockers list containing 0 or 1 docker
     * @throws TooManyDockersException if p is not null and the node already has a docker (nodes may only have one docker)
     */
    @Override
    public void setDockers(List<Point> dockers) {
        if (dockers != null && dockers.size() > 1)
            throw new TooManyDockersException("Trying to set more than 1 docker for node '" + getResourceId() + "'");

        super.setDockers(dockers);
    }

    // TODO do the other operations actually make sense / have to be redefined
    // here?

}
