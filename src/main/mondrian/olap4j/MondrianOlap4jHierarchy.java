/*
// $Id: //open/mondrian-release/3.1/src/main/mondrian/olap4j/MondrianOlap4jHierarchy.java#2 $
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// Copyright (C) 2007-2009 Julian Hyde
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
*/
package mondrian.olap4j;

import java.util.*;

import org.olap4j.impl.AbstractNamedList;
import org.olap4j.impl.Named;
import org.olap4j.impl.NamedListImpl;
import org.olap4j.impl.Olap4jUtil;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.NamedList;

/**
 * Implementation of {@link org.olap4j.metadata.Hierarchy}
 * for the Mondrian OLAP engine.
 *
 * @author jhyde
 * @version $Id: //open/mondrian-release/3.1/src/main/mondrian/olap4j/MondrianOlap4jHierarchy.java#2 $
 * @since May 25, 2007
 */
class MondrianOlap4jHierarchy implements Hierarchy, Named {
    private final MondrianOlap4jSchema olap4jSchema;
    private final mondrian.olap.Hierarchy hierarchy;

    MondrianOlap4jHierarchy(
        MondrianOlap4jSchema olap4jSchema,
        mondrian.olap.Hierarchy hierarchy)
    {
        this.olap4jSchema = olap4jSchema;
        this.hierarchy = hierarchy;
    }

    public boolean equals(Object obj) {
        return obj instanceof MondrianOlap4jHierarchy
            && hierarchy.equals(((MondrianOlap4jHierarchy) obj).hierarchy);
    }

    public int hashCode() {
        return hierarchy.hashCode();
    }

    public Dimension getDimension() {
        return new MondrianOlap4jDimension(
            olap4jSchema, hierarchy.getDimension());
    }

    public NamedList<Level> getLevels() {
        final NamedList<MondrianOlap4jLevel> list =
            new NamedListImpl<MondrianOlap4jLevel>();
        final MondrianOlap4jConnection olap4jConnection =
            olap4jSchema.olap4jCatalog.olap4jDatabaseMetaData.olap4jConnection;
        for (mondrian.olap.Level level : hierarchy.getLevels()) {
            list.add(olap4jConnection.toOlap4j(level));
        }
        return Olap4jUtil.cast(list);
    }

    public boolean hasAll() {
        return hierarchy.hasAll();
    }

    public Member getDefaultMember() {
        final MondrianOlap4jConnection olap4jConnection =
            olap4jSchema.olap4jCatalog.olap4jDatabaseMetaData.olap4jConnection;
        return olap4jConnection.toOlap4j(hierarchy.getDefaultMember());
    }

    public NamedList<Member> getRootMembers() {
        final MondrianOlap4jConnection olap4jConnection =
            olap4jSchema.olap4jCatalog.olap4jDatabaseMetaData.olap4jConnection;
        final List<mondrian.olap.Member> levelMembers =
            olap4jConnection.connection.getSchemaReader().getLevelMembers(
                hierarchy.getLevels()[0], true);

        return new AbstractNamedList<Member>() {
            protected String getName(Member member) {
                return member.getName();
            }

            public Member get(int index) {
                return olap4jConnection.toOlap4j(levelMembers.get(index));
            }

            public int size() {
                return levelMembers.size();
            }
        };
    }

    public String getName() {
        return hierarchy.getName();
    }

    public String getUniqueName() {
        return hierarchy.getUniqueName();
    }

    public String getCaption(Locale locale) {
        // todo: localize caption
        return hierarchy.getCaption();
    }

    public String getDescription(Locale locale) {
        // todo: localize description
        return hierarchy.getDescription();
    }
}

// End MondrianOlap4jHierarchy.java
