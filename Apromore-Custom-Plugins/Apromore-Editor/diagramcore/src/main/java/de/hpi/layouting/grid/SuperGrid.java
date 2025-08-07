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
package de.hpi.layouting.grid;

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

import de.hpi.layouting.grid.Grid.Cell;
import de.hpi.layouting.grid.Grid.Row;

import java.util.*;

/**
 * A set of verticaly adjacent <tt>Grid</tt>'s. The
 * <p/>
 * <tt>Grid</tt> will be synchronized in width. E.g. if a column is inserted in
 * one grid it will be inserted in all grids at the same place. Provides some
 * access-method to the <tt>Grid</tt>'s making them feel as a single
 * <tt>Grid</tt>
 *
 * @param <T>
 * @author Team Royal Fawn
 */
public class SuperGrid<T> implements Iterable<Row<T>> {

    private static class RowIterator<T> implements Iterator<Row<T>> {

        private Queue<Iterator<Row<T>>> queue;

        /**
         * @param queue
         */
        public RowIterator(List<Grid<T>> grids) {
            super();
            this.queue = new LinkedList<Iterator<Row<T>>>();
            for (Grid<T> grid : grids) {
                Iterator<Row<T>> iterator = grid.iterator();
                queue.add(iterator);
            }
        }

        public boolean hasNext() {
            while (queue.peek() != null && !queue.peek().hasNext()) {
                queue.poll();
            }
            return !queue.isEmpty();
        }

        public Row<T> next() {
            while (!queue.peek().hasNext()) {
                queue.poll();
            }
            Row<T> result = queue.peek().next();
            return result;
        }

        public void remove() {
            queue.peek().remove();
        }

    }

    private List<Grid<T>> grids;
    private int width = -1;

    /**
     *
     **/
    public SuperGrid() {
        super();
        this.grids = new LinkedList<Grid<T>>();
    }

    /**
     * @return the subGrids
     */
    public List<Grid<T>> getGrids() {
        return Collections.unmodifiableList(grids);
    }

    /**
     * @param e
     * @return
     * @see java.util.List#add(java.lang.Object)
     */
    public boolean add(Grid<T> e) {
        this.add(grids.size(), e);
        return true;
    }

    /**
     * @param index
     * @param element
     * @see java.util.List#add(int, java.lang.Object)
     */
    public void add(int index, Grid<T> element) {

        element.setParent(null); // remove from other SuperGrid if nessesary
        element._setParent(this);
        grids.add(index, element);

        width = Math.max(element.getWidth(), width);
        for (Grid<T> grid : grids) {
            while (grid.getWidth() < width) {
                grid.addLastColumn();
            }
        }
    }

    /**
     * @param index
     * @return
     * @see java.util.List#get(int)
     */
    public Grid<T> get(int index) {
        return grids.get(index);
    }

    /**
     * @param index
     * @return
     * @see java.util.List#remove(int)
     */
    public Grid<T> remove(int index) {
        Grid<T> removed = grids.remove(index);
        removed._setParent(null);
        return removed;
    }

    /**
     * @param o
     * @return
     * @see java.util.List#remove(java.lang.Object)
     */
    public boolean remove(Object o) {
        int pos = grids.indexOf(o);
        if (pos >= 0) {
            this.remove(pos);
            return true;
        }
        return false;
    }

    void _insertColumnBefore(int col, int width) {
        if (width > this.width) {
            this.width++;
            for (Grid<T> grid : grids) {
                if (grid.getWidth() < width) {
                    grid.insertColumnBefore(col);
                }
            }
        }
    }

    public void pack() {
        for (Grid<T> grid : grids) {
            grid.pack();
        }
    }

    public int getHeight() {
        int height = 0;
        for (Grid<T> grid : grids) {
            height += grid.getHeight();
        }
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int findRow(Row<T> row) {
        int pos = 0;
        for (Grid<T> grid : grids) {
            if (row.getParent() == grid) {
                return pos + grid.find(row);
            } else {
                pos += grid.getHeight();
            }
        }
        return -1;
    }

    public Row<T> getRow(int i) {
        for (Grid<T> grid : grids) {
            if (i < grid.getHeight()) {
                return grid.get(i);
            } else {
                i -= grid.getHeight();
            }
        }
        return null;
    }

    public Iterator<Row<T>> iterator() {
        return new RowIterator<T>(grids);
    }

    public Cell<T> getCellOfItem(T item) {
        Cell<T> result = null;
        Iterator<Grid<T>> it = this.getGrids().iterator();
        while (result == null && it.hasNext()) {
            result = it.next().getCellOfItem(item);
        }
        return result;
    }

    public void setCellOfItem(T item, Cell<T> cell) {
        cell.getParent().getParent().setCellOfItem(item, cell);
    }

}
