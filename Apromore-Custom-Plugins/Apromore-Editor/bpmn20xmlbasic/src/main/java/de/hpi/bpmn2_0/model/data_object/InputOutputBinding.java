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

package de.hpi.bpmn2_0.model.data_object;

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

import de.hpi.bpmn2_0.model.BaseElement;
import de.hpi.bpmn2_0.model.activity.misc.Operation;

import javax.xml.bind.annotation.*;


/**
 * <p>Java class for tInputOutputBinding complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="tInputOutputBinding">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.omg.org/spec/BPMN/20100524/MODEL}tBaseElement">
 *       &lt;attribute name="operationRef" use="required" type="{http://www.w3.org/2001/XMLSchema}QName" />
 *       &lt;attribute name="inputDataRef" use="required" type="{http://www.w3.org/2001/XMLSchema}IDREF" />
 *       &lt;attribute name="outputDataRef" use="required" type="{http://www.w3.org/2001/XMLSchema}IDREF" />
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tInputOutputBinding")
public class InputOutputBinding
        extends BaseElement {
    @XmlIDREF
    @XmlAttribute(name = "operationRef", required = true)
    protected Operation operationRef;
    @XmlAttribute(name = "inputDataRef", required = true)
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    protected Object inputDataRef;
    @XmlAttribute(name = "outputDataRef", required = true)
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    protected Object outputDataRef;

    /**
     * Gets the value of the operationRef property.
     *
     * @return possible object is
     *         {@link Operation }
     */
    public Operation getOperationRef() {
        return operationRef;
    }

    /**
     * Sets the value of the operationRef property.
     *
     * @param value allowed object is
     *              {@link Operation }
     */
    public void setOperationRef(Operation value) {
        this.operationRef = value;
    }

    /**
     * Gets the value of the inputDataRef property.
     *
     * @return possible object is
     *         {@link Object }
     */
    public Object getInputDataRef() {
        return inputDataRef;
    }

    /**
     * Sets the value of the inputDataRef property.
     *
     * @param value allowed object is
     *              {@link Object }
     */
    public void setInputDataRef(Object value) {
        this.inputDataRef = value;
    }

    /**
     * Gets the value of the outputDataRef property.
     *
     * @return possible object is
     *         {@link Object }
     */
    public Object getOutputDataRef() {
        return outputDataRef;
    }

    /**
     * Sets the value of the outputDataRef property.
     *
     * @param value allowed object is
     *              {@link Object }
     */
    public void setOutputDataRef(Object value) {
        this.outputDataRef = value;
    }

}
