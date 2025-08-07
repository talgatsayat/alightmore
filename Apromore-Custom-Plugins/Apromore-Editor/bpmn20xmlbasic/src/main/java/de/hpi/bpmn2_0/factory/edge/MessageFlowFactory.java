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

package de.hpi.bpmn2_0.factory.edge;

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

import java.util.Map;

import org.oryxeditor.server.diagram.generic.GenericShape;

import de.hpi.bpmn2_0.annotations.StencilId;
import de.hpi.bpmn2_0.exceptions.BpmnConverterException;
import de.hpi.bpmn2_0.factory.AbstractEdgesFactory;
import de.hpi.bpmn2_0.factory.BPMNElement;
import de.hpi.bpmn2_0.model.bpmndi.BPMNEdge;
import de.hpi.bpmn2_0.model.bpmndi.di.MessageVisibleKind;
import de.hpi.bpmn2_0.model.connector.MessageFlow;
import de.hpi.bpmn2_0.model.data_object.Message;

/**
 * Factory that creates {@link MessageFlow}
 * 
 * @author Philipp Giese
 * @author Sven Wagner-Boysen
 *
 */
@StencilId("MessageFlow")
public class MessageFlowFactory extends AbstractEdgesFactory {

	@Override
	public BPMNElement createBpmnElement(GenericShape shape, BPMNElement parent, State state)
			throws BpmnConverterException {
		BPMNElement element = super.createBpmnElement(shape, parent, state);
		
		for(GenericShape child : ((GenericShape<?,?>)shape).getChildShapesReadOnly()) {
			if(child.getStencilId().equals("Message")) {
				/*
				Message m = new Message();
				
				// Name value
				String name = child.getProperty("name");
				if(name != null && name.length() > 0) {
					m.setName(name);
				}
				
				((MessageFlow) element.getNode()).setMessageRef(m);
				
				// Initiating
				String initiating = shape.getProperty("initiating");
				if(initiating != null && initiating.equals("false")) {
					((BPMNEdge) element.getShape()).setMessageVisibleKind(MessageVisibleKind.NON_INITIATING);
				} else {
					((BPMNEdge) element.getShape()).setMessageVisibleKind(MessageVisibleKind.INITIATING);
				}
				*/
			}
		}
		
		return element;
	}
	
	/* (non-Javadoc)
	 * @see de.hpi.bpmn2_0.factory.AbstractBpmnFactory#createProcessElement(org.oryxeditor.server.diagram.Shape)
	 */
	// @Override
	protected MessageFlow createProcessElement(GenericShape shape)
			throws BpmnConverterException {
		MessageFlow msgFlow = new MessageFlow();
		this.setCommonAttributes(msgFlow, shape);
		msgFlow.setId(shape.getResourceId());
		msgFlow.setName(shape.getProperty("name"));
		
		return msgFlow;
	}

}
