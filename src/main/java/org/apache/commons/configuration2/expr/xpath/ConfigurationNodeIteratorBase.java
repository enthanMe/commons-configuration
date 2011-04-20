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

package org.apache.commons.configuration2.expr.xpath;

import org.apache.commons.configuration2.expr.NodeHandler;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;

/**
 * <p>
 * A base class for implementing iterators over configuration nodes.
 * </p>
 * <p>
 * This class already provides common functionality for implementing the
 * iteration process. Derived classes will implement specific behavior based on
 * the concrete node type (child node or attribute node).
 * </p>
 *
 * @since 2.0
 * @author <a
 *         href="http://commons.apache.org/configuration/team-list.html">Commons
 *         Configuration team</a>
 * @version $Id$
 * @param <T> the type of the nodes this iterator deals with
 */
abstract class ConfigurationNodeIteratorBase<T> implements NodeIterator
{
    /** Stores the parent node pointer. */
    private ConfigurationNodePointer<T> parent;

    /** Stores the current position. */
    private int position;

    /** Stores the start offset of the iterator. */
    private int startOffset;

    /** Stores the reverse flag. */
    private boolean reverse;

    /**
     * Creates a new instance of <code>ConfigurationNodeIteratorBase</code>
     * and initializes it.
     *
     * @param parent the parent pointer
     * @param reverse the reverse flag
     */
    protected ConfigurationNodeIteratorBase(ConfigurationNodePointer<T> parent, boolean reverse)
    {
        this.parent = parent;
        this.reverse = reverse;
    }

    /**
     * Returns the position of the iteration.
     *
     * @return the position
     */
    public int getPosition()
    {
        return position;
    }

    /**
     * Sets the position of the iteration.
     *
     * @param pos the new position
     * @return a flag if this is a valid position
     */
    public boolean setPosition(int pos)
    {
        position = pos;
        return pos >= 1 && pos <= getMaxPosition();
    }

    /**
     * Returns the current node pointer.
     *
     * @return the current pointer in this iteration
     */
    public NodePointer getNodePointer()
    {
        if (getPosition() < 1 && !setPosition(1))
        {
            return null;
        }

        return createNodePointer(positionToIndex(getPosition()));
    }

    /**
     * Returns the parent node pointer.
     *
     * @return the parent node pointer
     */
    protected ConfigurationNodePointer<T> getParent()
    {
        return parent;
    }

    /**
     * Returns the node handler for the managed nodes. This is a convenience
     * method.
     *
     * @return the node handler
     */
    protected NodeHandler<T> getNodeHandler()
    {
        return getParent().getNodeHandler();
    }

    /**
     * Returns the start offset of the iteration.
     *
     * @return the start offset
     */
    protected int getStartOffset()
    {
        return startOffset;
    }

    /**
     * Sets the start offset of the iteration. This is used when a start element
     * was set.
     *
     * @param startOffset the start offset
     */
    protected void setStartOffset(int startOffset)
    {
        this.startOffset = startOffset;
        if (reverse)
        {
            this.startOffset--;
        }
        else
        {
            this.startOffset++;
        }
    }

    /**
     * Returns the maximum position for this iterator.
     *
     * @return the maximum allowed position
     */
    protected int getMaxPosition()
    {
        return reverse ? getStartOffset() + 1 : size() - getStartOffset();
    }

    /**
     * Returns the index in the data list for the given position. This method
     * also checks the reverse flag.
     *
     * @param pos the position (1-based)
     * @return the corresponding list index
     */
    protected int positionToIndex(int pos)
    {
        return (reverse ? 1 - pos : pos - 1) + getStartOffset();
    }

    /**
     * Creates the configuration node pointer for the current position. This
     * method is called by <code>getNodePointer()</code>. Derived classes
     * must create the correct pointer object.
     *
     * @param position the current position in the iteration
     * @return the node pointer
     */
    protected abstract NodePointer createNodePointer(int position);

    /**
     * Returns the number of elements in this iteration.
     *
     * @return the number of elements
     */
    protected abstract int size();
}