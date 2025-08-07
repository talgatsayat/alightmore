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

import static com.processconfiguration.DefinitionsIDResolverTest.assertValidBPMN;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.processconfiguration.common.Constants;
import de.hpi.bpmn2_0.exceptions.BpmnConverterException;
import de.hpi.bpmn2_0.factory.AbstractBpmnFactory;
import de.hpi.bpmn2_0.model.Definitions;
import de.hpi.bpmn2_0.model.FlowNode;
import de.hpi.bpmn2_0.model.Process;
import de.hpi.bpmn2_0.model.activity.Task;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import org.json.JSONException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.oryxeditor.server.diagram.basic.BasicDiagram;
import org.oryxeditor.server.diagram.basic.BasicDiagramBuilder;
import org.xml.sax.SAXException;

/**
 * Test suite for {@link Diagram2BpmnConverter}.
 * 
 * @author <a href="mailto:simon.raboczi@uqconnect.edu.au">Simon Raboczi</a>
 * @see de.hpi.bpmn2_0.BPMNSerializationTest
 */
class Diagram2BpmnConverterTest {

    /** Logger.  Named after the class. */
    final static private Logger logger = Logger.getLogger(Diagram2BpmnConverterTest.class.getCanonicalName());

    /**
     * Test the {@link Diagram2BpmnConverter#getDefinitionsFromDiagram} method.
     */
    @Disabled
    @Test
    void testGetDefinitionsFromDiagram1() throws BpmnConverterException, IOException, JAXBException, JSONException, SAXException {

        // Read the test JSON
        BufferedReader br = new BufferedReader(new FileReader(new File(new File(Constants.testsDirectory, "data"), "GetDiagramFromBpmn20_1-expected.json")));
        String bpmnJson = "";
        String line;
        while ((line = br.readLine()) != null) {
           bpmnJson += line;
        }
        BasicDiagram diagram = BasicDiagramBuilder.parseJson(bpmnJson);

        // Execute the conversion
        Diagram2BpmnConverter converter = new Diagram2BpmnConverter(diagram, AbstractBpmnFactory.getFactoryClasses());
        Definitions definitions = converter.getDefinitionsFromDiagram();

        // Examine the resultant BPMN XML
        assertValidBPMN(definitions, "test-GetDefinitionsFromDiagram.bpmn.xml");
        Task airbus = (Task) ((Process) definitions.getRootElement().get(0)).getFlowElement().get(0);
        assertEquals("sid-B8EA9D11-3DF2-46E9-8498-9351EEB1C3B4", airbus.getId());
        assertEquals(1, airbus.getIncoming().size());
        assertEquals("sid-B6C60809-2232-4F2B-B290-A4639AD05BCD", airbus.getIncoming().get(0).getId());
        assertEquals(1, ((FlowNode) airbus).getIncomingSequenceFlows().size());
        assertEquals("sid-B6C60809-2232-4F2B-B290-A4639AD05BCD", ((FlowNode) airbus).getIncomingSequenceFlows().get(0).getId());
        assertEquals(1, airbus.getOutgoing().size());
        assertEquals("sid-F44D88A1-2E18-43E6-93C7-7F038AB2C2A1", airbus.getOutgoing().get(0).getId());
        assertEquals(1, ((FlowNode) airbus).getOutgoingSequenceFlows().size());
        assertEquals("sid-F44D88A1-2E18-43E6-93C7-7F038AB2C2A1", ((FlowNode) airbus).getOutgoingSequenceFlows().get(0).getId());
    }
}
