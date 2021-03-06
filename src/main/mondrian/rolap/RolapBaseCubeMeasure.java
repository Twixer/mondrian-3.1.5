/*
// $Id: //open/mondrian-release/3.1/src/main/mondrian/rolap/RolapBaseCubeMeasure.java#4 $
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// Copyright (C) 2006-2009 Julian Hyde
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
*/
package mondrian.rolap;

import mondrian.olap.*;
import mondrian.resource.MondrianResource;
import mondrian.spi.Dialect;

import java.util.*;

/**
 * Measure which is computed from a SQL column (or expression) and which is
 * defined in a non-virtual cube.
 *
 * @see RolapVirtualCubeMeasure
 *
 * @author jhyde
 * @since 24 August, 2006
 * @version $Id: //open/mondrian-release/3.1/src/main/mondrian/rolap/RolapBaseCubeMeasure.java#4 $
 */
public class RolapBaseCubeMeasure
    extends RolapMember
    implements RolapStoredMeasure
{
    private static final List<String> datatypeList =
        Arrays.asList("Integer", "Numeric", "String");

    /**
     * For SQL generator. Column which holds the value of the measure.
     */
    private final MondrianDef.Expression expression;

    /**
     * For SQL generator. Has values "SUM", "COUNT", etc.
     */
    private final RolapAggregator aggregator;

    private final RolapCube cube;
    private final Map<String, Annotation> annotationMap;

    /**
     * Holds the {@link mondrian.rolap.RolapStar.Measure} from which this
     * member is computed. Untyped, because another implementation might store
     * it somewhere else.
     */
    private Object starMeasure;

    private CellFormatter formatter;

    /**
     * Creates a RolapBaseCubeMeasure.
     *
     * @param cube Cube
     * @param parentMember Parent member
     * @param level Level this member belongs to
     * @param name Name of this member
     * @param caption Caption
     * @param description Description
     * @param formatString Format string
     * @param expression Expression
     * @param aggregatorName Aggregator
     * @param datatype Data type
     * @param annotationMap Annotations
     */
    RolapBaseCubeMeasure(
        RolapCube cube,
        RolapMember parentMember,
        RolapLevel level,
        String name,
        String caption,
        String description,
        String formatString,
        MondrianDef.Expression expression,
        String aggregatorName,
        String datatype,
        Map<String, Annotation> annotationMap)
    {
        super(parentMember, level, name, null, MemberType.MEASURE);
        assert annotationMap != null;
        this.cube = cube;
        this.annotationMap = annotationMap;
        this.caption = caption;
        this.expression = expression;
        if (description != null) {
            setProperty(
                Property.DESCRIPTION.name,
                description);
        }
        if (formatString == null) {
            formatString = "";
        }
        setProperty(
            Property.FORMAT_EXP.name,
            Literal.createString(formatString));

        // Validate aggregator.
        this.aggregator =
            RolapAggregator.enumeration.getValue(aggregatorName, false);
        if (this.aggregator == null) {
            StringBuilder buf = new StringBuilder();
            for (String aggName : RolapAggregator.enumeration.getNames()) {
                if (buf.length() > 0) {
                    buf.append(", ");
                }
                buf.append('\'');
                buf.append(aggName);
                buf.append('\'');
            }
            throw MondrianResource.instance().UnknownAggregator.ex(
                aggregatorName,
                buf.toString());
        }

        setProperty(Property.AGGREGATION_TYPE.name, aggregator);
        if (datatype == null) {
            if (aggregator == RolapAggregator.Count
                || aggregator == RolapAggregator.DistinctCount)
            {
                datatype = "Integer";
            } else {
                datatype = "Numeric";
            }
        }
        // todo: End-user error.
        Util.assertTrue(
            RolapBaseCubeMeasure.datatypeList.contains(datatype),
            "invalid datatype " + datatype);
        setProperty(Property.DATATYPE.name, datatype);
    }

    public MondrianDef.Expression getMondrianDefExpression() {
        return expression;
    }

    public RolapAggregator getAggregator() {
        return aggregator;
    }

    public RolapCube getCube() {
        return cube;
    }

    public CellFormatter getFormatter() {
        return formatter;
    }

    public void setFormatter(CellFormatter formatter) {
        this.formatter = formatter;
    }

    public Object getStarMeasure() {
        return starMeasure;
    }

    void setStarMeasure(Object starMeasure) {
        this.starMeasure = starMeasure;
    }

    @Override
    public Map<String, Annotation> getAnnotationMap() {
        return annotationMap;
    }

    public Dialect.Datatype getDatatype() {
        Object datatype = getPropertyValue(Property.DATATYPE.name);
        try {
            return Dialect.Datatype.valueOf((String) datatype);
        } catch (ClassCastException e) {
            return Dialect.Datatype.String;
        } catch (IllegalArgumentException e) {
            return Dialect.Datatype.String;
        }
    }
}

// End RolapBaseCubeMeasure.java
