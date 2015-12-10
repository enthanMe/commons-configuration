/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.configuration2.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.commons.configuration2.ConfigurationRuntimeException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for FlatNodeHandler.
 *
 * @author <a
 *         href="http://commons.apache.org/configuration/team-list.html">Commons
 *         Configuration team</a>
 * @version $Id$
 */
public class TestFlatNodeHandler
{
    /** An array with the names of the test child nodes. */
    private static final String[] CHILD_NAMES = {
            "child1", "anotherChild", "differentChild", "child1", "againAChild"
    };

    /** Constant for the name of a test property. */
    private static final String NAME = "testProperty";

    /** The node handler to be tested. */
    private FlatNodeHandlerTestImpl handler;

    /** The configuration source associated with the node handler. */
    private FlatConfigurationSource source;

    /** Stores the internal update flag of the node handler. */
    private Boolean internalUpdate;

    @Before
    public void setUp() throws Exception
    {
        source = new ConfigurationSourceEventWrapper(
                new MapConfigurationSource());
        source.addConfigurationSourceListener(new ConfigurationSourceListener()
        {
            /**
             * Tests the internal update status of the node handler.
             */
            public void configurationSourceChanged(
                    ConfigurationSourceEvent event)
            {
                internalUpdate = handler.isInternalUpdate();
            }
        });
        handler = new FlatNodeHandlerTestImpl();
    }

    /**
     * Clears the test environment. This implementation also checks whether an
     * unexpected change event was received.
     */
    @After
    public void tearDown() throws Exception
    {
        assertNull("Unexpected change event", internalUpdate);
    }

    /**
     * Creates a flat root node with some test child nodes.
     *
     * @return the root node
     */
    private FlatNode setUpTestNode()
    {
        FlatRootNode root = new FlatRootNode(source);
        for (String c : CHILD_NAMES)
        {
            root.addChild(c);
        }
        return root;
    }

    /**
     * Tests the internal update flag of the node handler.
     *
     * @param expected the expected value
     */
    private void checkUpdate(boolean expected)
    {
        assertEquals("Wrong value of update flag", expected, internalUpdate);
        internalUpdate = null;
    }

    /**
     * Tests querying the child nodes of a node.
     */
    @Test
    public void testGetChildren()
    {
        List<FlatNode> children = handler.getChildren(setUpTestNode());
        assertEquals("Wrong number of children", CHILD_NAMES.length, children
                .size());
        for (int i = 0; i < CHILD_NAMES.length; i++)
        {
            assertEquals("Wrong child at " + i, CHILD_NAMES[i], children.get(i)
                    .getName());
        }
    }

    /**
     * Tests querying children by name.
     */
    @Test
    public void testGetChildrenName()
    {
        List<FlatNode> children = handler.getChildren(setUpTestNode(),
                CHILD_NAMES[0]);
        assertEquals("Wrong number of children", 2, children.size());
        for (FlatNode n : children)
        {
            assertEquals("Wrong child", CHILD_NAMES[0], n.getName());
        }
    }

    /**
     * Tests querying the number of children.
     */
    @Test
    public void testGetChildrenCount()
    {
        FlatNode root = setUpTestNode();

        for (String name : CHILD_NAMES)
        {
            int count = 0;
            for (String n : CHILD_NAMES)
            {
                if (name.equals(n))
                {
                    count++;
                }
            }
            assertEquals("Wrong number for child " + name, count, handler
                    .getChildrenCount(root, name));
        }
    }

    /**
     * Tests querying the total number of children.
     */
    @Test
    public void testGetChildrenCountTotal()
    {
        assertEquals("Wrong total number of children", CHILD_NAMES.length,
                handler.getChildrenCount(setUpTestNode(), null));
    }

    /**
     * Tests querying children by their index.
     */
    @Test
    public void testGetChild()
    {
        FlatNode node = setUpTestNode();
        for (int i = 0; i < CHILD_NAMES.length; i++)
        {
            FlatNode child = handler.getChild(node, i);
            assertEquals("Wrong child at " + i, CHILD_NAMES[i], child.getName());
        }
    }

    /**
     * Tests adding a new child node.
     */
    @Test
    public void testAddChild()
    {
        FlatNode node = setUpTestNode();
        FlatNode child = handler.addChild(node, NAME);
        assertEquals("Wrong name of child", NAME, child.getName());
        assertTrue("Config not empty", source.isEmpty());
        child.setValue(TestFlatNodes.VALUE);
        assertEquals("Value not added", TestFlatNodes.VALUE, source
                .getProperty(NAME));
        checkUpdate(false);
    }

    /**
     * Tests removing a child node.
     */
    @Test
    public void testRemoveChild()
    {
        FlatNode node = setUpTestNode();
        source.setProperty(NAME, "test");
        FlatNode child = node.addChild(NAME);
        handler.removeChild(node, child);
        List<FlatNode> children = node.getChildren();
        assertEquals("No child removed", CHILD_NAMES.length, children.size());
        assertFalse("Child still found", children.contains(child));
        assertFalse("Property not removed", source.containsKey(NAME));
        checkUpdate(true);
    }

    /**
     * Tests setting the value of a node.
     */
    @Test
    public void testSetValue()
    {
        FlatNode node = setUpTestNode();
        FlatNode child = node.addChild(NAME);
        handler.setValue(child, TestFlatNodes.VALUE);
        assertEquals("Property not added to config", TestFlatNodes.VALUE,
                source.getProperty(NAME));
        checkUpdate(true);
    }

    /**
     * Tests querying the value of a node.
     */
    @Test
    public void testGetValue()
    {
        FlatNode node = setUpTestNode();
        source.setProperty(NAME, TestFlatNodes.VALUE);
        checkUpdate(false);
        FlatNode child = node.addChild(NAME);
        assertEquals("Wrong value of node", TestFlatNodes.VALUE, handler
                .getValue(child));
    }

    /**
     * Tests querying the name of nodes.
     */
    @Test
    public void testNodeName()
    {
        FlatNode node = setUpTestNode();
        assertNull("Wrong name of root node", handler.nodeName(node));
        for (int i = 0; i < CHILD_NAMES.length; i++)
        {
            assertEquals("Wrong name for child " + i, CHILD_NAMES[i], handler
                    .nodeName(node.getChild(i)));
        }
    }

    /**
     * Tests querying the parent of a node.
     */
    @Test
    public void testGetParent()
    {
        FlatNode node = setUpTestNode();
        assertNull("Wrong parent of root node", handler.getParent(node));
        assertEquals("Wrong parent", node, handler.getParent(node.getChild(0)));
    }

    /**
     * Tests adding a value to an attribute. Because attributes are not
     * supported, this should cause an exception.
     */
    @Test(expected = ConfigurationRuntimeException.class)
    public void testAddAttributeValue()
    {
        handler.addAttributeValue(setUpTestNode(), "attr", "test");
    }

    /**
     * Tests querying the value of an attribute. Because flat nodes do not
     * support attributes, result should always be null.
     */
    @Test
    public void testGetAttributeValue()
    {
        assertNull("Wrong attribute value", handler.getAttributeValue(
                setUpTestNode(), "test"));
    }

    /**
     * Tests querying the existing attributes. Here always an empty list should
     * be returned.
     */
    @Test
    public void testGetAttributes()
    {
        assertTrue("Found attributes", handler.getAttributes(setUpTestNode())
                .isEmpty());
    }

    /**
     * Tests removing an attribute. This should be a no-op. We only test whether
     * no exception is thrown.
     */
    @Test
    public void testRemoveAttribute()
    {
        handler.removeAttribute(setUpTestNode(), "test");
    }

    /**
     * Tests setting the value of an attribute. Because attributes are not
     * supported, this should cause an exception.
     */
    @Test(expected = ConfigurationRuntimeException.class)
    public void testSetAttributeValue()
    {
        handler.setAttributeValue(setUpTestNode(), "attr", "test");
    }

    /**
     * A test flat node handler implementation. It is used mainly for checking
     * the internal update flag.
     */
    private static class FlatNodeHandlerTestImpl extends FlatNodeHandler
    {
        /** Stores the internal update flag. */
        private boolean internalUpdate;

        public boolean isInternalUpdate()
        {
            return internalUpdate;
        }

        /**
         * Records this invocation and stores the value of the flag.
         */
        @Override
        void setInternalUpdate(FlatNode node, boolean f)
        {
            internalUpdate = f;
            super.setInternalUpdate(node, f);
        }
    }
}