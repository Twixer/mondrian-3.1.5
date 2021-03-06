/*
// $Id: //open/mondrian-release/3.1/src/main/mondrian/rolap/MemberCacheHelper.java#2 $
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// Copyright (C) 2001-2002 Kana Software, Inc.
// Copyright (C) 2001-2009 Julian Hyde and others
// Copyright (C) 2004-2005 TONBELLER AG
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
//
// jhyde, 21 December, 2001
*/
package mondrian.rolap;

import mondrian.rolap.cache.SmartCache;
import mondrian.rolap.cache.SoftSmartCache;
import mondrian.rolap.sql.MemberChildrenConstraint;
import mondrian.rolap.sql.TupleConstraint;
import mondrian.spi.DataSourceChangeListener;
import mondrian.olap.Util;

import java.util.*;

/**
 * Encapsulation of member caching.
 *
 * @author Will Gorman (wgorman@pentaho.org)
 * @version $Id: //open/mondrian-release/3.1/src/main/mondrian/rolap/MemberCacheHelper.java#2 $
 */
public class MemberCacheHelper implements MemberCache {

    private final SqlConstraintFactory sqlConstraintFactory =
        SqlConstraintFactory.instance();

    /** maps a parent member to a list of its children */
    final SmartMemberListCache<RolapMember, List<RolapMember>>
        mapMemberToChildren;

    /** a cache for alle members to ensure uniqueness */
    SmartCache<Object, RolapMember> mapKeyToMember;
    RolapHierarchy rolapHierarchy;
    DataSourceChangeListener changeListener;

    /** maps a level to its members */
    final SmartMemberListCache<RolapLevel, List<RolapMember>>
        mapLevelToMembers;

    /**
     * Creates a MemberCacheHelper.
     *
     * @param rolapHierarchy Hierarchy
     */
    public MemberCacheHelper(RolapHierarchy rolapHierarchy) {
        this.rolapHierarchy = rolapHierarchy;
        this.mapLevelToMembers =
            new SmartMemberListCache<RolapLevel, List<RolapMember>>();
        this.mapKeyToMember =
            new SoftSmartCache<Object, RolapMember>();
        this.mapMemberToChildren =
            new SmartMemberListCache<RolapMember, List<RolapMember>>();

        if (rolapHierarchy != null) {
            changeListener =
                rolapHierarchy.getRolapSchema().getDataSourceChangeListener();
        } else {
            changeListener = null;
        }
    }

    // implement MemberCache
    // synchronization: Must synchronize, because uses mapKeyToMember
    public synchronized RolapMember getMember(
        Object key,
        boolean mustCheckCacheStatus)
    {
        if (mustCheckCacheStatus) {
            checkCacheStatus();
        }
        return mapKeyToMember.get(key);
    }


    // implement MemberCache
    // synchronization: Must synchronize, because modifies mapKeyToMember
    public synchronized Object putMember(Object key, RolapMember value) {
        return mapKeyToMember.put(key, value);
    }

    // implement MemberCache
    public Object makeKey(RolapMember parent, Object key) {
        return new MemberKey(parent, key);
    }

    // implement MemberCache
    // synchronization: Must synchronize, because modifies mapKeyToMember
    public synchronized RolapMember getMember(Object key) {
        return getMember(key, true);
    }

    public synchronized void checkCacheStatus() {
        if (changeListener != null) {
            if (changeListener.isHierarchyChanged(rolapHierarchy)) {
                flushCache();
            }
        }
    }

    /**
     * ???
     *
     * @param level
     * @param constraint
     * @param members
     */
    // synchronization: Must synchronize, because modifies mapKeyToMember
    public synchronized void putLevelMembersInCache(
        RolapLevel level,
        TupleConstraint constraint,
        List<RolapMember> members)
    {
        mapLevelToMembers.put(level, constraint, members);
    }

    public synchronized List<RolapMember> getChildrenFromCache(
        RolapMember member,
        MemberChildrenConstraint constraint)
    {
        if (constraint == null) {
            constraint =
                sqlConstraintFactory.getMemberChildrenConstraint(null);
        }
        return mapMemberToChildren.get(member, constraint);
    }

    public synchronized void putChildren(
        RolapMember member,
        MemberChildrenConstraint constraint,
        List<RolapMember> children)
    {
        if (constraint == null) {
            constraint =
                sqlConstraintFactory.getMemberChildrenConstraint(null);
        }
        mapMemberToChildren.put(member, constraint, children);
    }

    public synchronized List<RolapMember> getLevelMembersFromCache(
        RolapLevel level,
        TupleConstraint constraint)
    {
        if (constraint == null) {
            constraint = sqlConstraintFactory.getLevelMembersConstraint(null);
        }
        return mapLevelToMembers.get(level, constraint);
    }

    public synchronized void flushCache() {
        mapMemberToChildren.clear();
        mapKeyToMember.clear();
        mapLevelToMembers.clear();
    }

    public DataSourceChangeListener getChangeListener() {
        return changeListener;
    }

    public void setChangeListener(DataSourceChangeListener listener) {
        changeListener = listener;
    }

    public boolean isMutable()
    {
        return true;
    }

    public synchronized RolapMember removeMember(Object key)
    {
        RolapMember member = getMember(key);
        if (member == null) {
            // not in cache
            return null;
        }

        // Drop member from the level-to-members map.
        // Cache key is a (level, constraint) pair,
        // cache value is a list of RolapMember.
        // For each cache key whose level matches, remove from the list,
        // regardless of the constraint.
        RolapLevel level = member.getLevel();
        for (Map.Entry<
            SmartMemberListCache.Key2<RolapLevel, Object>,
            List<RolapMember>> entry : mapLevelToMembers.getCache())
        {
            if (entry.getKey().o1.equals(level)) {
                List<RolapMember> peers = entry.getValue();
                boolean removedIt = peers.remove(member);
                Util.discard(removedIt);
            }
        }

        // Drop member from the member-to-children map, wherever it occurs as
        // a parent or as a child, regardless of the constraint.
        RolapMember parent = member.getParentMember();
        final Iterator<
            Map.Entry<
                SmartMemberListCache.Key2<RolapMember, Object>,
                List<RolapMember>>> iter =
            mapMemberToChildren.getCache().iterator();
        while (iter.hasNext()) {
            Map.Entry<
                SmartMemberListCache.Key2<RolapMember, Object>,
                List<RolapMember>> entry = iter.next();
            final RolapMember member1 = entry.getKey().o1;
            final Object constraint = entry.getKey().o2;

            // Cache key is (member's parent, constraint);
            // cache value is a list of member's siblings;
            // If constraint is trivial remove member from list of siblings;
            // otherwise it's safer to nuke the cache entry
            if (member1.equals(parent)) {
                if (constraint == DefaultMemberChildrenConstraint.instance()) {
                    List<RolapMember> siblings = entry.getValue();
                    boolean removedIt = siblings.remove(member);
                    Util.discard(removedIt);
                } else {
                    iter.remove();
                }
            }

            // cache is (member, some constraint);
            // cache value is list of member's children;
            // remove cache entry
            if (member1.equals(member)) {
                iter.remove();
            }
        }

        // drop it from the lookup-cache
        return mapKeyToMember.put(key, null);
    }

    public synchronized RolapMember removeMemberAndDescendants(Object key) {
        // Can use mapMemberToChildren recursively. No need to update inferior
        // lists of children. Do need to update inferior lists of level-peers.
        return null; // STUB
    }

}

// End MemberCacheHelper.java
