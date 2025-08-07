/*-
 * #%L
 * This file is part of "Apromore Core".
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
package org.apromore.processmining.models.graphbased.directed.bpmn.elements;

import java.awt.Graphics2D;

import org.apromore.processmining.models.graphbased.AttributeMap;
import org.apromore.processmining.models.graphbased.AttributeMap.ArrowType;
import org.apromore.processmining.models.graphbased.directed.ContainableDirectedGraphElement;
import org.apromore.processmining.models.graphbased.directed.ContainingDirectedGraphNode;
import org.apromore.processmining.models.graphbased.directed.bpmn.BPMNEdge;
import org.apromore.processmining.models.graphbased.directed.bpmn.BPMNNode;
import org.apromore.processmining.models.shapes.Decorated;

public class Flow extends BPMNEdge<BPMNNode, BPMNNode> implements Decorated {
	
	private IGraphElementDecoration decorator = null;
	
	private String conditionExpression;
	
	private String textAnnotation;

	public Flow(BPMNNode source, BPMNNode target, String label) {
		super(source, target);
		fillAttributes(label);
	}

	@Deprecated
	public Flow(BPMNNode source, BPMNNode target, SubProcess parentSubProcess, String label) {
		super(source, target, parentSubProcess);
		fillAttributes(label);
	}

	@Deprecated
	public Flow(BPMNNode source, BPMNNode target, Swimlane parentSwimlane, String label) {
		super(source, target, parentSwimlane);
		fillAttributes(label);
	}


	private void fillAttributes(String label) {
		getAttributeMap().put(AttributeMap.EDGEEND, ArrowType.ARROWTYPE_CLASSIC);
		getAttributeMap().put(AttributeMap.EDGEENDFILLED, true);
		if (label == null) {
			getAttributeMap().put(AttributeMap.SHOWLABEL, false);
		} else {
			getAttributeMap().put(AttributeMap.LABEL, label);
			getAttributeMap().put(AttributeMap.SHOWLABEL, true);
		}
	}

	@Deprecated
	public Swimlane getParentSwimlane() {
		if (getParent() != null) {
			if (getParent() instanceof Swimlane)
				return (Swimlane) getParent();
			else
				return null;
		}
		return null;
	}
	
	@Deprecated
	public Swimlane getParentPool() {
		ContainingDirectedGraphNode parent = getParent();
		while (parent != null) {
			if ((parent instanceof Swimlane) 
					&& ((Swimlane)parent).getSwimlaneType().equals(SwimlaneType.POOL)) {
				return (Swimlane) parent;
			}
			else {
				if(parent instanceof ContainableDirectedGraphElement) {
					parent = ((ContainableDirectedGraphElement)parent).getParent();
				} else {
					return null;
				}
			}
		}
		return null;
	}

	@Deprecated
	public SubProcess getParentSubProcess() {
		if (getParent() != null) {
			if (getParent() instanceof SubProcess)
				return (SubProcess) getParent();
			else
				return null;
		}
		return null;
	}
	
	public SubProcess getAncestorSubProcess() {
		if (getParent() != null) {
			if (getParent() instanceof SubProcess) {
				return (SubProcess) getParent();
			} else if (getParent() instanceof Swimlane) {
				return ((Swimlane)getParent()).getParentSubProcess();
			} else {
				return null;
			}
		}
		return null;
	}

	public boolean equals(Object o) {
		return (o == this);
	}

	public IGraphElementDecoration getDecorator() {
		return decorator;
	}

	public void setDecorator(IGraphElementDecoration decorator) {
		this.decorator = decorator;
	}

	public void decorate(Graphics2D g2d, double x, double y, double width, double height) {
		if (decorator != null) {
			decorator.decorate(g2d, x, y, width, height);
		}
	}
	
	public void setConditionExpression(String conditionExpression) {
		this.conditionExpression = conditionExpression;
	}
	
	public String getConditionExpression() {
		return conditionExpression;
	}
	
	public String getTextAnnotation() {
		return textAnnotation;
	}

	public void setTextAnnotation(String textAnnotation) {
		this.textAnnotation = textAnnotation;
	}
}
