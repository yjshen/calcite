/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.calcite.plan;

import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.CorrelationId;
import org.apache.calcite.rel.metadata.DefaultRelMetadataProvider;
import org.apache.calcite.rel.metadata.MetadataFactory;
import org.apache.calcite.rel.metadata.MetadataFactoryImpl;
import org.apache.calcite.rel.metadata.RelMetadataProvider;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.rex.RexNode;

import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An environment for related relational expressions during the
 * optimization of a query.
 */
public class RelOptCluster {
  //~ Instance fields --------------------------------------------------------

  private final RelDataTypeFactory typeFactory;
  private final RelOptPlanner planner;
  private final AtomicInteger nextCorrel;
  private final Map<String, RelNode> mapCorrelToRel;
  private RexNode originalExpression;
  private final RexBuilder rexBuilder;
  private RelMetadataProvider metadataProvider;
  private MetadataFactory metadataFactory;
  private final RelTraitSet emptyTraitSet;

  //~ Constructors -----------------------------------------------------------

  /**
   * Creates a cluster.
   */
  @Deprecated // to be removed before 2.0
  RelOptCluster(
      RelOptQuery query,
      RelOptPlanner planner,
      RelDataTypeFactory typeFactory,
      RexBuilder rexBuilder) {
    this(planner, typeFactory, rexBuilder, query.nextCorrel,
        query.mapCorrelToRel);
  }

  /**
   * Creates a cluster.
   *
   * <p>For use only from {@link #create} and {@link RelOptQuery}.
   */
  RelOptCluster(RelOptPlanner planner, RelDataTypeFactory typeFactory,
      RexBuilder rexBuilder, AtomicInteger nextCorrel,
      Map<String, RelNode> mapCorrelToRel) {
    this.nextCorrel = nextCorrel;
    this.mapCorrelToRel = mapCorrelToRel;
    this.planner = Preconditions.checkNotNull(planner);
    this.typeFactory = Preconditions.checkNotNull(typeFactory);
    this.rexBuilder = rexBuilder;
    this.originalExpression = rexBuilder.makeLiteral("?");

    // set up a default rel metadata provider,
    // giving the planner first crack at everything
    setMetadataProvider(DefaultRelMetadataProvider.INSTANCE);
    this.emptyTraitSet = planner.emptyTraitSet();
    assert emptyTraitSet.size() == planner.getRelTraitDefs().size();
  }

  /** Creates a cluster. */
  public static RelOptCluster create(RelOptPlanner planner,
      RexBuilder rexBuilder) {
    return new RelOptCluster(planner, rexBuilder.getTypeFactory(),
        rexBuilder, new AtomicInteger(0), new HashMap<String, RelNode>());
  }

  //~ Methods ----------------------------------------------------------------

  @Deprecated // to be removed before 2.0
  public RelOptQuery getQuery() {
    return new RelOptQuery(planner, nextCorrel, mapCorrelToRel);
  }

  @Deprecated // to be removed before 2.0
  public RexNode getOriginalExpression() {
    return originalExpression;
  }

  @Deprecated // to be removed before 2.0
  public void setOriginalExpression(RexNode originalExpression) {
    this.originalExpression = originalExpression;
  }

  public RelOptPlanner getPlanner() {
    return planner;
  }

  public RelDataTypeFactory getTypeFactory() {
    return typeFactory;
  }

  public RexBuilder getRexBuilder() {
    return rexBuilder;
  }

  public RelMetadataProvider getMetadataProvider() {
    return metadataProvider;
  }

  /**
   * Overrides the default metadata provider for this cluster.
   *
   * @param metadataProvider custom provider
   */
  public void setMetadataProvider(RelMetadataProvider metadataProvider) {
    this.metadataProvider = metadataProvider;
    this.metadataFactory = new MetadataFactoryImpl(metadataProvider);
  }

  public MetadataFactory getMetadataFactory() {
    return metadataFactory;
  }

  /**
   * Constructs a new id for a correlating variable. It is unique within the
   * whole query.
   */
  public CorrelationId createCorrel() {
    return new CorrelationId(nextCorrel.getAndIncrement());
  }

  /** Returns the default trait set for this cluster. */
  public RelTraitSet traitSet() {
    return emptyTraitSet;
  }

  /** @deprecated For {@code traitSetOf(t1, t2)},
   * use {@link #traitSet}().replace(t1).replace(t2). */
  @Deprecated // to be removed before 2.0
  public RelTraitSet traitSetOf(RelTrait... traits) {
    RelTraitSet traitSet = emptyTraitSet;
    for (RelTrait trait : traits) {
      traitSet = traitSet.replace(trait);
    }
    return traitSet;
  }

  public RelTraitSet traitSetOf(RelTrait trait) {
    return emptyTraitSet.replace(trait);
  }
}

// End RelOptCluster.java
