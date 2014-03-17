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
package org.apache.commons.configuration;

import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.configuration.tree.ImmutableNode;
import org.apache.commons.configuration.tree.InMemoryNodeModel;
import org.apache.commons.configuration.tree.NodeModel;
import org.apache.commons.configuration.tree.NodeSelector;
import org.apache.commons.configuration.tree.TrackedNodeModel;

/**
 * <p>
 * A specialized hierarchical configuration class with a node model that uses a
 * tracked node of another node model as its root node.
 * </p>
 * <p>
 * Configurations of this type are initialized with a special {@link NodeModel}
 * operating on a specific tracked node of the parent configuration and the
 * corresponding {@link NodeSelector}. All property accessor methods are
 * evaluated relative to this root node. A good use case for a
 * {@code SubnodeConfiguration} is when multiple properties from a specific sub
 * tree of the whole configuration need to be accessed. Then a
 * {@code SubnodeConfiguration} can be created with the parent node of the
 * affected sub tree as root node. This allows for simpler property keys and is
 * also more efficient.
 * </p>
 * <p>
 * By making use of a tracked node as root node, a {@code SubnodeConfiguration}
 * and its parent configuration initially operate on the same hierarchy of
 * configuration nodes. So if modifications are performed at the subnode
 * configuration, these changes are immediately visible in the parent
 * configuration. Analogously will updates of the parent configuration affect
 * the {@code SubnodeConfiguration} if the sub tree spanned by the
 * {@code SubnodeConfiguration}'s root node is involved.
 * </p>
 * <p>
 * Note that by making use of a {@code NodeSelector} the
 * {@code SubnodeConfiguration} is not associated with a physical node instance,
 * but the selection criteria stored in the selector are evaluated after each
 * change of the nodes structure. As an example consider that the selector uses
 * a key with an index into a list element, say index 2. Now if an update occurs
 * on the underlying nodes structure which removes the first element in this
 * list structure, the {@code SubnodeConfiguration} still references the element
 * with index 2 which is now another one.
 * </p>
 * <p>
 * There are also possible changes of the underlying nodes structure which
 * completely detach the {@code SubnodeConfiguration} from its parent
 * configuration. For instance, the key referenced by the
 * {@code SubnodeConfiguration} could be removed in the parent configuration. If
 * this happens, the {@code SubnodeConfiguration} stays functional; however, it
 * now operates on a separate node model than its parent configuration. Changes
 * made by one configuration are no longer visible for the other one (as the
 * node models have no longer overlapping nodes, there is no way to have a
 * synchronization here).
 * </p>
 * <p>
 * When a subnode configuration is created, it inherits the settings of its
 * parent configuration, e.g. some flags like the
 * {@code throwExceptionOnMissing} flag or the settings for handling list
 * delimiters) or the expression engine. If these settings are changed later in
 * either the subnode or the parent configuration, the changes are not visible
 * for each other. So you could create a subnode configuration, and change its
 * expression engine without affecting the parent configuration.
 * </p>
 * <p>
 * Because the {@code SubnodeConfiguration} operates on the same nodes structure
 * as its parent it uses the same {@code Synchronizer} instance per default.
 * This means that locks held on one {@code SubnodeConfiguration} also impact
 * the parent configuration and all of its other {@code SubnodeConfiguration}
 * objects. You should not change this without a good reason! Otherwise, there
 * is the risk of data corruption when multiple threads access these
 * configuration concurrently.
 * </p>
 * <p>
 * From its purpose this class is quite similar to {@link SubsetConfiguration}.
 * The difference is that a subset configuration of a hierarchical configuration
 * may combine multiple configuration nodes from different sub trees of the
 * configuration, while all nodes in a subnode configuration belong to the same
 * sub tree. If an application can live with this limitation, it is recommended
 * to use this class instead of {@code SubsetConfiguration} because creating a
 * subset configuration is more expensive than creating a subnode configuration.
 * </p>
 * <p>
 * It is strongly recommended to create {@code SubnodeConfiguration} instances
 * only through the {@code configurationAt()} methods of a hierarchical
 * configuration. These methods ensure that all necessary initializations are
 * done. Creating instances manually without doing proper initialization may
 * break some of the functionality provided by this class.
 * </p>
 *
 * @since 1.3
 * @version $Id$
 */
public class SubnodeConfiguration extends BaseHierarchicalConfiguration
{
    /**
     * The serial version UID.
     */
    private static final long serialVersionUID = 3105734147019386480L;

    /** Stores the parent configuration. */
    private final BaseHierarchicalConfiguration parent;

    /** The node selector selecting the root node of this configuration. */
    private final NodeSelector rootSelector;

    /**
     * Creates a new instance of {@code SubnodeConfiguration} and initializes it
     * with all relevant properties.
     *
     * @param parent the parent configuration
     * @param model the {@code TrackedNodeModel} to be used for this configuration
     * @param selector the {@code NodeSelector} selecting the root node
     *                 @throws IllegalArgumentException if a required argument is missing
     */
    public SubnodeConfiguration(BaseHierarchicalConfiguration parent,
            TrackedNodeModel model, NodeSelector selector)
    {
        super(model);
        if (parent == null)
        {
            throw new IllegalArgumentException(
                    "Parent configuration must not be null!");
        }
        if (model == null)
        {
            throw new IllegalArgumentException("Node model must not be null!");
        }
        if(selector == null)
        {
            throw new IllegalArgumentException("Node selector must not be null!");
        }

        this.parent = parent;
        rootSelector = selector;
        initFromParent(parent);
        initInterpolator();
    }

    /**
     * Returns the parent configuration of this subnode configuration.
     *
     * @return the parent configuration
     */
    public BaseHierarchicalConfiguration getParent()
    {
        return parent;
    }

    /**
     * Returns the selector to the root node of this configuration.
     *
     * @return the {@code NodeSelector} to the root node
     */
    public NodeSelector getRootSelector()
    {
        return rootSelector;
    }

    /**
     * {@inheritDoc} This implementation returns a copy of the current node
     * model with the same settings. However, it has to be ensured that the
     * track count for the node selector is increased.
     *
     * @return the node model for the clone
     */
    @Override
    protected NodeModel<ImmutableNode> cloneNodeModel()
    {
        InMemoryNodeModel parentModel =
                (InMemoryNodeModel) getParent().getModel();
        parentModel.trackNode(getRootSelector(), getParent());
        return new TrackedNodeModel(parentModel, getRootSelector(), true);
    }

    /**
     * Returns a hierarchical configuration object for the given sub node that
     * is aware of structural changes of its parent. Works like the method with
     * the same name, but also sets the subnode key for the new subnode
     * configuration, so it can check whether the parent has been changed. This
     * only works if this subnode configuration has itself a valid subnode key.
     * So if a subnode configuration that should be aware of structural changes
     * is created from an already existing subnode configuration, this subnode
     * configuration must also be aware of such changes.
     *
     * @param node the sub node, for which the configuration is to be created
     * @param subKey the construction key
     * @return a hierarchical configuration for this sub node
     * @since 1.5
     */
    @Override
    protected SubnodeConfiguration createSubnodeConfiguration(
            ConfigurationNode node, String subKey)
    {
//        String key =
//                (subKey != null && subnodeKey != null) ? constructSubKeyForSubnodeConfig(node)
//                        : null;
//        return new SubnodeConfiguration(getParent(), node, key);
        //TODO implementation
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    /**
     * Initializes this subnode configuration from the given parent
     * configuration. This method is called by the constructor. It will copy
     * many settings from the parent.
     *
     * @param parentConfig the parent configuration
     */
    private void initFromParent(BaseHierarchicalConfiguration parentConfig)
    {
        setExpressionEngine(parentConfig.getExpressionEngine());
        setListDelimiterHandler(parentConfig.getListDelimiterHandler());
        setThrowExceptionOnMissing(parentConfig.isThrowExceptionOnMissing());
    }

    /**
     * Initializes the {@code ConfigurationInterpolator} for this sub configuration.
     * This is a standard {@code ConfigurationInterpolator} which also references
     * the {@code ConfigurationInterpolator} of the parent configuration.
     */
    private void initInterpolator()
    {
        getInterpolator().setParentInterpolator(getParent().getInterpolator());
    }
}
