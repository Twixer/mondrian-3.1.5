/*
// This software is subject to the terms of the Common Public License
// Agreement, available at the following URL:
// http://www.opensource.org/licenses/cpl.html.
// Copyright (C) 2004-2005 TONBELLER AG
// Copyright (C) 2005-2009 Julian Hyde and others
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
*/
package mondrian.rolap;

import mondrian.olap.Connection;
import mondrian.olap.MondrianProperties;
import mondrian.spi.Dialect;
import mondrian.test.SqlPattern;
import mondrian.test.TestContext;

/**
 * Tests for Filter and native Filters.
 *
 * @author Rushan Chen
 * @since April 28, 2009
 * @version $Id: //open/mondrian-release/3.1/testsrc/main/mondrian/rolap/FilterTest.java#2 $
 */
public class FilterTest extends BatchTestCase {
    public FilterTest() {
        super();
    }

    public FilterTest(String name) {
        super(name);
    }

    public void testInFilterSimple() throws Exception {
        propSaver.set(MondrianProperties.instance().ExpandNonNative, false);
        propSaver.set(MondrianProperties.instance().EnableNativeFilter, true);

        // Get a fresh connection; Otherwise the mondrian property setting
        // is not refreshed for this parameter.
        boolean requestFreshConnection = true;

        String query =
            "With " +
            "Set [*NATIVE_CJ_SET] as 'NonEmptyCrossJoin([*BASE_MEMBERS_Customers],[*BASE_MEMBERS_Product])' " +
            "Set [*BASE_MEMBERS_Customers] as 'Filter([Customers].[City].Members,Ancestor([Customers].CurrentMember, [Customers].[State Province]) In {[Customers].[All Customers].[USA].[CA]})' " +
            "Set [*BASE_MEMBERS_Product] as 'Filter([Product].[Product Family].Members,[Product].CurrentMember In {[Product].[All Products].[Drink]})' " +
            "Set [*CJ_ROW_AXIS] as 'Generate([*NATIVE_CJ_SET], {([Customers].currentMember,[Product].currentMember)})' " +
            "Select " +
            "{[Measures].[Customer Count]} on columns, " +
            "Non Empty [*CJ_ROW_AXIS] on rows " +
            "From [Sales]";

        checkNative(100, 45, query, null, requestFreshConnection);        		
    }

    public void testNotInFilterSimple() throws Exception {
        propSaver.set(MondrianProperties.instance().ExpandNonNative, false);
        propSaver.set(MondrianProperties.instance().EnableNativeFilter, true);

        // Get a fresh connection; Otherwise the mondrian property setting
        // is not refreshed for this parameter.
        boolean requestFreshConnection = true;

        String query =
            "With " +
            "Set [*NATIVE_CJ_SET] as 'NonEmptyCrossJoin([*BASE_MEMBERS_Customers],[*BASE_MEMBERS_Product])' " +
            "Set [*BASE_MEMBERS_Customers] as 'Filter([Customers].[City].Members,Ancestor([Customers].CurrentMember, [Customers].[State Province]) Not In {[Customers].[All Customers].[USA].[CA]})' " +
            "Set [*BASE_MEMBERS_Product] as 'Filter([Product].[Product Family].Members,[Product].CurrentMember Not In {[Product].[All Products].[Drink]})' " +
            "Set [*CJ_ROW_AXIS] as 'Generate([*NATIVE_CJ_SET], {([Customers].currentMember,[Product].currentMember)})' " +
            "Select " +
            "{[Measures].[Customer Count]} on columns, " +
            "Non Empty [*CJ_ROW_AXIS] on rows " +
            "From [Sales]";        		

        checkNative(100, 66, query, null, requestFreshConnection);        		
    }

    public void testInFilterAND() throws Exception {
        propSaver.set(MondrianProperties.instance().ExpandNonNative, false);
        propSaver.set(MondrianProperties.instance().EnableNativeFilter, true);

        // Get a fresh connection; Otherwise the mondrian property setting
        // is not refreshed for this parameter.
        boolean requestFreshConnection = true;

        String query =
            "With " +
            "Set [*NATIVE_CJ_SET] as 'NonEmptyCrossJoin([*BASE_MEMBERS_Customers],[*BASE_MEMBERS_Product])' " +
            "Set [*BASE_MEMBERS_Customers] as 'Filter([Customers].[City].Members," +
            "((Ancestor([Customers].CurrentMember, [Customers].[State Province]) In {[Customers].[All Customers].[USA].[CA]}) " +
            "AND ([Customers].CurrentMember Not In {[Customers].[All Customers].[USA].[CA].[Altadena]})))' " +
            "Set [*BASE_MEMBERS_Product] as 'Filter([Product].[Product Family].Members,[Product].CurrentMember Not In {[Product].[All Products].[Drink]})' " +
            "Set [*CJ_ROW_AXIS] as 'Generate([*NATIVE_CJ_SET], {([Customers].currentMember,[Product].currentMember)})' " +
            "Select " +
            "{[Measures].[Customer Count]} on columns, " +
            "Non Empty [*CJ_ROW_AXIS] on rows " +
            "From [Sales]";        		

        checkNative(100, 88, query, null, requestFreshConnection);        		
    }

    public void testIsFilterSimple() throws Exception {
        propSaver.set(MondrianProperties.instance().ExpandNonNative, false);
        propSaver.set(MondrianProperties.instance().EnableNativeFilter, true);

        // Get a fresh connection; Otherwise the mondrian property setting
        // is not refreshed for this parameter.
        boolean requestFreshConnection = true;

        String query =
            "With " +
            "Set [*NATIVE_CJ_SET] as 'NonEmptyCrossJoin([*BASE_MEMBERS_Customers],[*BASE_MEMBERS_Product])' " +
            "Set [*BASE_MEMBERS_Customers] as 'Filter([Customers].[City].Members,Ancestor([Customers].CurrentMember, [Customers].[State Province]) Is [Customers].[All Customers].[USA].[CA])' " +
            "Set [*BASE_MEMBERS_Product] as 'Filter([Product].[Product Family].Members,[Product].CurrentMember Is [Product].[All Products].[Drink])' " +
            "Set [*CJ_ROW_AXIS] as 'Generate([*NATIVE_CJ_SET], {([Customers].currentMember,[Product].currentMember)})' " +
            "Select " +
            "{[Measures].[Customer Count]} on columns, " +
            "Non Empty [*CJ_ROW_AXIS] on rows " +
            "From [Sales]";

        checkNative(100, 45, query, null, requestFreshConnection);        
    }

    public void testNotIsFilterSimple() throws Exception {
        propSaver.set(MondrianProperties.instance().ExpandNonNative, false);
        propSaver.set(MondrianProperties.instance().EnableNativeFilter, true);

        // Get a fresh connection; Otherwise the mondrian property setting
        // is not refreshed for this parameter.
        boolean requestFreshConnection = true;

        String query =
            "With " +
            "Set [*NATIVE_CJ_SET] as 'NonEmptyCrossJoin([*BASE_MEMBERS_Customers],[*BASE_MEMBERS_Product])' " +
            "Set [*BASE_MEMBERS_Customers] as 'Filter([Customers].[City].Members, not (Ancestor([Customers].CurrentMember, [Customers].[State Province]) Is [Customers].[All Customers].[USA].[CA]))' " +
            "Set [*BASE_MEMBERS_Product] as 'Filter([Product].[Product Family].Members,not ([Product].CurrentMember Is [Product].[All Products].[Drink]))' " +
            "Set [*CJ_ROW_AXIS] as 'Generate([*NATIVE_CJ_SET], {([Customers].currentMember,[Product].currentMember)})' " +
            "Select " +
            "{[Measures].[Customer Count]} on columns, " +                
            "Non Empty [*CJ_ROW_AXIS] on rows " +
            "From [Sales]";

        checkNative(100, 66, query, null, requestFreshConnection);        
    }

    public void testMixedInIsFilters() throws Exception {
        propSaver.set(MondrianProperties.instance().ExpandNonNative, false);
        propSaver.set(MondrianProperties.instance().EnableNativeFilter, true);

        // Get a fresh connection; Otherwise the mondrian property setting
        // is not refreshed for this parameter.
        boolean requestFreshConnection = true;

        String query =
            "With " +
            "Set [*NATIVE_CJ_SET] as 'NonEmptyCrossJoin([*BASE_MEMBERS_Customers],[*BASE_MEMBERS_Product])' " +
            "Set [*BASE_MEMBERS_Customers] as 'Filter([Customers].[City].Members," +
            "((Ancestor([Customers].CurrentMember, [Customers].[State Province]) Is [Customers].[All Customers].[USA].[CA]) " +
            "AND ([Customers].CurrentMember Not In {[Customers].[All Customers].[USA].[CA].[Altadena]})))' " +
            "Set [*BASE_MEMBERS_Product] as 'Filter([Product].[Product Family].Members, not ([Product].CurrentMember Is [Product].[All Products].[Drink]))' " +
            "Set [*CJ_ROW_AXIS] as 'Generate([*NATIVE_CJ_SET], {([Customers].currentMember,[Product].currentMember)})' " +
            "Select " +
            "{[Measures].[Customer Count]} on columns, " +                
            "Non Empty [*CJ_ROW_AXIS] on rows " +
            "From [Sales]";

        checkNative(100, 88, query, null, requestFreshConnection);        
    }


    /**
     * Here the filter is above(rather than as inputs to) the NECJ.
     * These types of filters are currently not natively evaluated.
     * 
     * To expand on this case, RlaopNativeFilter needs to be improved
     * so it knows how to represent the dimension filter constraint.
     * Currently the FilterConstraint is only used for filters on
     * measures.
     * 
     * @throws Exception
     */
    public void testInFilterNonNative() throws Exception {
        propSaver.set(MondrianProperties.instance().ExpandNonNative, false);
        propSaver.set(MondrianProperties.instance().EnableNativeFilter, true);

        String query =
            "With " +
            "Set [*BASE_CJ_SET] as 'CrossJoin([Customers].[City].Members,[Product].[Product Family].Members)' " +
            "Set [*NATIVE_CJ_SET] as 'Filter([*BASE_CJ_SET], " +
            "(Ancestor([Customers].CurrentMember,[Customers].[State Province]) In {[Customers].[All Customers].[USA].[CA]}) AND ([Product].CurrentMember In {[Product].[All Products].[Drink]}))' " +            
            "Set [*CJ_ROW_AXIS] as 'Generate([*NATIVE_CJ_SET], {([Customers].currentMember,[Product].currentMember)})' " +
            "Select " +
            "{[Measures].[Customer Count]} on columns, " +            
            "Non Empty [*CJ_ROW_AXIS] on rows " +
            "From [Sales]";

        checkNotNative(45, query);              
    }

    public void testTopCountOverInFilter() throws Exception {
        propSaver.set(MondrianProperties.instance().ExpandNonNative, false);
        propSaver.set(MondrianProperties.instance().EnableNativeFilter, true);
        propSaver.set(MondrianProperties.instance().EnableNativeTopCount, true);

        // Get a fresh connection; Otherwise the mondrian property setting
        // is not refreshed for this parameter.
        boolean requestFreshConnection = true;

        String query =
            "With " +
            "Set [*NATIVE_TOP_SET] as 'TopCount([*BASE_MEMBERS_Customers], 3, [Measures].[Customer Count])' " +
            "Set [*BASE_MEMBERS_Customers] as 'Filter([Customers].[City].Members,Ancestor([Customers].CurrentMember, [Customers].[State Province]) In {[Customers].[All Customers].[USA].[CA]})' " +
            "Set [*CJ_ROW_AXIS] as 'Generate([*NATIVE_TOP_SET], {([Customers].currentMember,[Product].currentMember)})' " +
            "Select " +
            "{[Measures].[Customer Count]} on columns, " +
            "Non Empty [*CJ_ROW_AXIS] on rows " +
            "From [Sales]";

        checkNative(100, 3, query, null, requestFreshConnection);              
    }

    /**
     * Test that if Null member is not explicitly excluded, then the native filter SQL should not
     * filter out null members.
     * @throws Exception
     */
    public void testNotInFilterKeepNullMember() throws Exception {
        propSaver.set(MondrianProperties.instance().ExpandNonNative, false);
        propSaver.set(MondrianProperties.instance().EnableNativeFilter, true);

        // Get a fresh connection; Otherwise the mondrian property setting
        // is not refreshed for this parameter.
        boolean requestFreshConnection = true;

        String query =
            "With " +
            "Set [*NATIVE_CJ_SET] as 'NonEmptyCrossJoin([*BASE_MEMBERS_Customers],[*BASE_MEMBERS_SQFT])' " +
            "Set [*BASE_MEMBERS_Customers] as 'Filter([Customers].[Country].Members, [Customers].CurrentMember In {[Customers].[All Customers].[USA]})' " +
            "Set [*BASE_MEMBERS_SQFT] as 'Filter([Store Size in SQFT].[Store Sqft].Members, [Store Size in SQFT].currentMember not in {[Store Size in SQFT].[All Store Size in SQFTs].[39696]})' " +
            "Set [*CJ_ROW_AXIS] as 'Generate([*NATIVE_CJ_SET], {([Customers].currentMember,[Store Size in SQFT].currentMember)})' " +
            "Set [*ORDERED_CJ_ROW_AXIS] as 'Order([*CJ_ROW_AXIS], [Store Size in SQFT].currentmember.OrderKey, BASC)' " +
            "Select " +
            "{[Measures].[Customer Count]} on columns, " +
            "Non Empty [*ORDERED_CJ_ROW_AXIS] on rows " +
            "From [Sales]";
        
        String result =
            "Axis #0:\n" +
            "{}\n" +
            "Axis #1:\n" +
            "{[Measures].[Customer Count]}\n" +
            "Axis #2:\n" +
            "{[Customers].[All Customers].[USA], [Store Size in SQFT].[All Store Size in SQFTs].[#null]}\n" +
            "{[Customers].[All Customers].[USA], [Store Size in SQFT].[All Store Size in SQFTs].[20319]}\n" +
            "{[Customers].[All Customers].[USA], [Store Size in SQFT].[All Store Size in SQFTs].[21215]}\n" +
            "{[Customers].[All Customers].[USA], [Store Size in SQFT].[All Store Size in SQFTs].[22478]}\n" +
            "{[Customers].[All Customers].[USA], [Store Size in SQFT].[All Store Size in SQFTs].[23598]}\n" +
            "{[Customers].[All Customers].[USA], [Store Size in SQFT].[All Store Size in SQFTs].[23688]}\n" +
            "{[Customers].[All Customers].[USA], [Store Size in SQFT].[All Store Size in SQFTs].[27694]}\n" +
            "{[Customers].[All Customers].[USA], [Store Size in SQFT].[All Store Size in SQFTs].[28206]}\n" +
            "{[Customers].[All Customers].[USA], [Store Size in SQFT].[All Store Size in SQFTs].[30268]}\n" +
            "{[Customers].[All Customers].[USA], [Store Size in SQFT].[All Store Size in SQFTs].[33858]}\n" +
            "Row #0: 1,153\n" +
            "Row #1: 563\n" +
            "Row #2: 906\n" +
            "Row #3: 296\n" +
            "Row #4: 1,147\n" +
            "Row #5: 1,059\n" +
            "Row #6: 474\n" +
            "Row #7: 190\n" +
            "Row #8: 84\n" +
            "Row #9: 278\n";          
        
        checkNative(0, 10, query, result, requestFreshConnection);
    }

    /**
     * Test that if Null member is explicitly excluded, then the native filter SQL should
     * filter out null members.
     * @throws Exception
     */
    public void testNotInFilterExcludeNullMember() throws Exception {
        propSaver.set(MondrianProperties.instance().ExpandNonNative, false);
        propSaver.set(MondrianProperties.instance().EnableNativeFilter, true);

        // Get a fresh connection; Otherwise the mondrian property setting
        // is not refreshed for this parameter.
        boolean requestFreshConnection = true;

        String query =
            "With " +
            "Set [*NATIVE_CJ_SET] as 'NonEmptyCrossJoin([*BASE_MEMBERS_Customers],[*BASE_MEMBERS_SQFT])' " +
            "Set [*BASE_MEMBERS_Customers] as 'Filter([Customers].[Country].Members, [Customers].CurrentMember In {[Customers].[All Customers].[USA]})' " +
            "Set [*BASE_MEMBERS_SQFT] as 'Filter([Store Size in SQFT].[Store Sqft].Members, " +
            "[Store Size in SQFT].currentMember not in {[Store Size in SQFT].[All Store Size in SQFTs].[#null], [Store Size in SQFT].[All Store Size in SQFTs].[39696]})' " +
            "Set [*CJ_ROW_AXIS] as 'Generate([*NATIVE_CJ_SET], {([Customers].currentMember,[Store Size in SQFT].currentMember)})' " +
            "Set [*ORDERED_CJ_ROW_AXIS] as 'Order([*CJ_ROW_AXIS], [Store Size in SQFT].currentmember.OrderKey, BASC)' " +
            "Select " +
            "{[Measures].[Customer Count]} on columns, " +
            "Non Empty [*ORDERED_CJ_ROW_AXIS] on rows " +
            "From [Sales]";
        
        String result =
            "Axis #0:\n" +
            "{}\n" +
            "Axis #1:\n" +
            "{[Measures].[Customer Count]}\n" +
            "Axis #2:\n" +
            "{[Customers].[All Customers].[USA], [Store Size in SQFT].[All Store Size in SQFTs].[20319]}\n" +
            "{[Customers].[All Customers].[USA], [Store Size in SQFT].[All Store Size in SQFTs].[21215]}\n" +
            "{[Customers].[All Customers].[USA], [Store Size in SQFT].[All Store Size in SQFTs].[22478]}\n" +
            "{[Customers].[All Customers].[USA], [Store Size in SQFT].[All Store Size in SQFTs].[23598]}\n" +
            "{[Customers].[All Customers].[USA], [Store Size in SQFT].[All Store Size in SQFTs].[23688]}\n" +
            "{[Customers].[All Customers].[USA], [Store Size in SQFT].[All Store Size in SQFTs].[27694]}\n" +
            "{[Customers].[All Customers].[USA], [Store Size in SQFT].[All Store Size in SQFTs].[28206]}\n" +
            "{[Customers].[All Customers].[USA], [Store Size in SQFT].[All Store Size in SQFTs].[30268]}\n" +
            "{[Customers].[All Customers].[USA], [Store Size in SQFT].[All Store Size in SQFTs].[33858]}\n" +
            "Row #0: 563\n" +
            "Row #1: 906\n" +
            "Row #2: 296\n" +
            "Row #3: 1,147\n" +
            "Row #4: 1,059\n" +
            "Row #5: 474\n" +
            "Row #6: 190\n" +
            "Row #7: 84\n" +
            "Row #8: 278\n";          
        
        checkNative(0, 9, query, result, requestFreshConnection);
    }

    /**
     * Test that null members are included when the filter excludes members
     * that contain multiple levels, but none being null.
     */
    public void testNotInMultiLevelMemberConstraintNonNullParent() {

        if (MondrianProperties.instance().UseAggregates.get()) {
            return;
        }

        String query =
            "With " +
            "Set [*NATIVE_CJ_SET] as 'NonEmptyCrossJoin([*BASE_MEMBERS_Customers],[*BASE_MEMBERS_Quarters])' " +
            "Set [*BASE_MEMBERS_Customers] as 'Filter([Customers].[Country].Members, [Customers].CurrentMember In {[Customers].[All Customers].[USA]})' " +
            "Set [*BASE_MEMBERS_Quarters] as 'Filter([Time].[Quarter].Members, " +
            "[Time].currentMember not in {[Time].[1997].[Q1], [Time].[1998].[Q3]})' " +
            "Set [*CJ_ROW_AXIS] as 'Generate([*NATIVE_CJ_SET], {([Customers].currentMember,[Time].currentMember)})' " +
            "Set [*ORDERED_CJ_ROW_AXIS] as 'Order([*CJ_ROW_AXIS], [Time].currentmember.OrderKey, BASC)' " +
            "Select " +
            "{[Measures].[Customer Count]} on columns, " +
            "Non Empty [*ORDERED_CJ_ROW_AXIS] on rows " +
            "From [Sales]";

        String necjSqlDerby =
            "select \"customer\".\"country\", \"time_by_day\".\"the_year\", " +
            "\"time_by_day\".\"quarter\" from \"customer\" as \"customer\", " +
            "\"sales_fact_1997\" as \"sales_fact_1997\", \"time_by_day\" as " +
            "\"time_by_day\" where \"sales_fact_1997\".\"customer_id\" = " +
            "\"customer\".\"customer_id\" and \"sales_fact_1997\".\"time_id\" = " +
            "\"time_by_day\".\"time_id\" and (\"customer\".\"country\" = 'USA') and " +
            "(not ((\"time_by_day\".\"the_year\" = 1997 and \"time_by_day\".\"quarter\" " +
            "= 'Q1') or (\"time_by_day\".\"the_year\" = 1998 and " +
            "\"time_by_day\".\"quarter\" = 'Q3')) or ((\"time_by_day\".\"quarter\" is " +
            "null or \"time_by_day\".\"the_year\" is null) and " +
            "not((\"time_by_day\".\"the_year\" = 1997 and \"time_by_day\".\"quarter\" " +
            "= 'Q1') or (\"time_by_day\".\"the_year\" = 1998 and " +
            "\"time_by_day\".\"quarter\" = 'Q3')))) group by \"customer\".\"country\", " +
            "\"time_by_day\".\"the_year\", \"time_by_day\".\"quarter\" order by " +
            "\"customer\".\"country\" ASC, \"time_by_day\".\"the_year\" ASC, " +
            "\"time_by_day\".\"quarter\" ASC";

        String necjSqlMySql =
            "select `customer`.`country` as `c0`, `time_by_day`.`the_year` as `c1`, " +
            "`time_by_day`.`quarter` as `c2` from `customer` as `customer`, " +
            "`sales_fact_1997` as `sales_fact_1997`, `time_by_day` as `time_by_day` " +
            "where `sales_fact_1997`.`customer_id` = `customer`.`customer_id` " +
            "and `sales_fact_1997`.`time_id` = `time_by_day`.`time_id` and " +
            "(`customer`.`country` = 'USA') and " +
            "(not ((`time_by_day`.`quarter`,`time_by_day`.`the_year`) in " +
            "(('Q1',1997),('Q3',1998))) or (`time_by_day`.`quarter` is null or " +
            "`time_by_day`.`the_year` is null)) " +
            "group by `customer`.`country`, `time_by_day`.`the_year`, " +
            "`time_by_day`.`quarter` order by ISNULL(`customer`.`country`), " +
            "`customer`.`country` ASC, ISNULL(`time_by_day`.`the_year`), " +
            "`time_by_day`.`the_year` ASC, ISNULL(`time_by_day`.`quarter`), " +
            "`time_by_day`.`quarter` ASC";

        SqlPattern[] patterns =
            new SqlPattern[]{
                new SqlPattern(Dialect.DatabaseProduct.DERBY, necjSqlDerby, necjSqlDerby),
                new SqlPattern(Dialect.DatabaseProduct.MYSQL, necjSqlMySql, necjSqlMySql)
            };

        assertQuerySql(query, patterns);
    }
  
    /**
     * Test that null members are included when the filter excludes members
     * that contain multiple levels, but none being null.  The members have
     * the same parent.
     */
    public void testNotInMultiLevelMemberConstraintNonNullSameParent() {
        if (MondrianProperties.instance().UseAggregates.get()) {
            return;
        }

        String query =
            "With " +
            "Set [*NATIVE_CJ_SET] as 'NonEmptyCrossJoin([*BASE_MEMBERS_Customers],[*BASE_MEMBERS_Quarters])' " +
            "Set [*BASE_MEMBERS_Customers] as 'Filter([Customers].[Country].Members, [Customers].CurrentMember In {[Customers].[All Customers].[USA]})' " +
            "Set [*BASE_MEMBERS_Quarters] as 'Filter([Time].[Quarter].Members, " +
            "[Time].currentMember not in {[Time].[1997].[Q1], [Time].[1997].[Q3]})' " +
            "Set [*CJ_ROW_AXIS] as 'Generate([*NATIVE_CJ_SET], {([Customers].currentMember,[Time].currentMember)})' " +
            "Set [*ORDERED_CJ_ROW_AXIS] as 'Order([*CJ_ROW_AXIS], [Time].currentmember.OrderKey, BASC)' " +
            "Select " +
            "{[Measures].[Customer Count]} on columns, " +
            "Non Empty [*ORDERED_CJ_ROW_AXIS] on rows " +
            "From [Sales]";

        String necjSqlDerby =
            "select \"customer\".\"country\", \"time_by_day\".\"the_year\", " +
            "\"time_by_day\".\"quarter\" from \"customer\" as \"customer\", " +
            "\"sales_fact_1997\" as \"sales_fact_1997\", \"time_by_day\" as " +
            "\"time_by_day\" where \"sales_fact_1997\".\"customer_id\" = " +
            "\"customer\".\"customer_id\" and \"sales_fact_1997\".\"time_id\" = " +
            "\"time_by_day\".\"time_id\" and (\"customer\".\"country\" = 'USA') and " +
            "((not (\"time_by_day\".\"quarter\" in ('Q1', 'Q3')) or " +
            "(\"time_by_day\".\"quarter\" is null)) or (not " +
            "(\"time_by_day\".\"the_year\" = 1997) or (\"time_by_day\".\"the_year\" is " +
            "null))) group by \"customer\".\"country\", \"time_by_day\".\"the_year\", " +
            "\"time_by_day\".\"quarter\" order by \"customer\".\"country\" ASC, " +
            "\"time_by_day\".\"the_year\" ASC, \"time_by_day\".\"quarter\" ASC";

        String necjSqlMySql =
            "select `customer`.`country` as `c0`, `time_by_day`.`the_year` as " +
            "`c1`, `time_by_day`.`quarter` as `c2` from `customer` as " +
            "`customer`, `sales_fact_1997` as `sales_fact_1997`, `time_by_day` " +
            "as `time_by_day` where `sales_fact_1997`.`customer_id` = " +
            "`customer`.`customer_id` and `sales_fact_1997`.`time_id` = " +
            "`time_by_day`.`time_id` and (`customer`.`country` = 'USA') and " +
            "((not (`time_by_day`.`quarter` in ('Q1', 'Q3')) or " +
            "(`time_by_day`.`quarter` is null)) or (not " +
            "(`time_by_day`.`the_year` = 1997) or (`time_by_day`.`the_year` " +
            "is null))) group by `customer`.`country`, `time_by_day`.`the_year`," +
            " `time_by_day`.`quarter` order by ISNULL(`customer`.`country`), " +
            "`customer`.`country` ASC, ISNULL(`time_by_day`.`the_year`), " +
            "`time_by_day`.`the_year` ASC, ISNULL(`time_by_day`.`quarter`), " +
            "`time_by_day`.`quarter` ASC";

        SqlPattern[] patterns =
            new SqlPattern[]{
                new SqlPattern(Dialect.DatabaseProduct.DERBY, necjSqlDerby, necjSqlDerby),
                new SqlPattern(Dialect.DatabaseProduct.MYSQL, necjSqlMySql, necjSqlMySql)
            };

        assertQuerySql(query, patterns);
    }

    /**
     * Test that null members are included when the filter explicitly excludes
     * certain members that contain nulls.  The members span multiple levels.
     */
    public void testNotInMultiLevelMemberConstraintMixedNullNonNullParent() {
        if (!isDefaultNullMemberRepresentation()) {
            return;
        }

        String dimension =
            "<Dimension name=\"Warehouse2\">\n" +
            "  <Hierarchy hasAll=\"true\" primaryKey=\"warehouse_id\">\n" +
            "    <Table name=\"warehouse\"/>\n" +
            "    <Level name=\"fax\" column=\"warehouse_fax\" uniqueMembers=\"true\"/>\n" +
            "    <Level name=\"address1\" column=\"wa_address1\" uniqueMembers=\"false\"/>\n" +
            "    <Level name=\"name\" column=\"warehouse_name\" uniqueMembers=\"false\"/>\n" +
            "  </Hierarchy>\n" +
            "</Dimension>\n";

        String cube =
            "<Cube name=\"Warehouse2\">\n" +
            "  <Table name=\"inventory_fact_1997\"/>\n" +
            "  <DimensionUsage name=\"Product\" source=\"Product\" foreignKey=\"product_id\"/>\n" +
            "  <DimensionUsage name=\"Warehouse2\" source=\"Warehouse2\" foreignKey=\"warehouse_id\"/>\n" +
            "  <Measure name=\"Warehouse Cost\" column=\"warehouse_cost\" aggregator=\"sum\"/>\n" +
            "  <Measure name=\"Warehouse Sales\" column=\"warehouse_sales\" aggregator=\"sum\"/>\n" +
            "</Cube>";

        String query =
            "with\n" +
            "set [Filtered Warehouse Set] as 'Filter([Warehouse2].[name].Members, [Warehouse2].CurrentMember Not In" +
            "{[Warehouse2].[#null].[234 West Covina Pkwy].[Freeman And Co]," +
            " [Warehouse2].[971-555-6213].[3377 Coachman Place].[Jones International]})' " +
            "set [NECJ] as NonEmptyCrossJoin([Filtered Warehouse Set], {[Product].[Product Family].Food}) " +
            "select [NECJ] on 0 from [Warehouse2]";

        String necjSqlDerby =
            "select \"warehouse\".\"warehouse_fax\", \"warehouse\".\"wa_address1\", " +
            "\"warehouse\".\"warehouse_name\", \"product_class\".\"product_family\" " +
            "from \"warehouse\" as \"warehouse\", \"inventory_fact_1997\" as " +
            "\"inventory_fact_1997\", \"product\" as \"product\", \"product_class\" as " +
            "\"product_class\" where \"inventory_fact_1997\".\"warehouse_id\" = " +
            "\"warehouse\".\"warehouse_id\" and \"product\".\"product_class_id\" = " +
            "\"product_class\".\"product_class_id\" and " +
            "\"inventory_fact_1997\".\"product_id\" = \"product\".\"product_id\" and " +
            "(\"product_class\".\"product_family\" = 'Food') and " +
            "(not ((\"warehouse\".\"wa_address1\" = '234 West Covina Pkwy' and " +
            "\"warehouse\".\"warehouse_fax\" is null and " +
            "\"warehouse\".\"warehouse_name\" = 'Freeman And Co') or " +
            "(\"warehouse\".\"wa_address1\" = '3377 Coachman Place' and " +
            "\"warehouse\".\"warehouse_fax\" = '971-555-6213' and " +
            "\"warehouse\".\"warehouse_name\" = 'Jones International')) or " +
            "((\"warehouse\".\"warehouse_name\" is null or " +
            "\"warehouse\".\"wa_address1\" is null or \"warehouse\".\"warehouse_fax\" " +
            "is null) and not((\"warehouse\".\"wa_address1\" = " +
            "'234 West Covina Pkwy' and \"warehouse\".\"warehouse_fax\" is null " +
            "and \"warehouse\".\"warehouse_name\" = 'Freeman And Co') or " +
            "(\"warehouse\".\"wa_address1\" = '3377 Coachman Place' and " +
            "\"warehouse\".\"warehouse_fax\" = '971-555-6213' and " +
            "\"warehouse\".\"warehouse_name\" = 'Jones International')))) " +
            "group by \"warehouse\".\"warehouse_fax\", \"warehouse\".\"wa_address1\", " +
            "\"warehouse\".\"warehouse_name\", \"product_class\".\"product_family\" " +
            "order by \"warehouse\".\"warehouse_fax\" ASC, " +
            "\"warehouse\".\"wa_address1\" ASC, \"warehouse\".\"warehouse_name\" ASC, " +
            "\"product_class\".\"product_family\" ASC";

        String necjSqlMySql =
            "select `warehouse`.`warehouse_fax` as `c0`, `warehouse`.`wa_address1` as `c1`, " +
            "`warehouse`.`warehouse_name` as `c2`, `product_class`.`product_family` as `c3` " +
            "from `warehouse` as `warehouse`, `inventory_fact_1997` as `inventory_fact_1997`, " +
            "`product` as `product`, `product_class` as `product_class` where " +
            "`inventory_fact_1997`.`warehouse_id` = `warehouse`.`warehouse_id` " +
            "and `product`.`product_class_id` = `product_class`.`product_class_id` " +
            "and `inventory_fact_1997`.`product_id` = `product`.`product_id` " +
            "and (`product_class`.`product_family` = 'Food') and " +
            "(not ((`warehouse`.`warehouse_name`,`warehouse`.`wa_address1`,`warehouse`.`warehouse_fax`) " +
            "in (('Jones International','3377 Coachman Place','971-555-6213')) " +
            "or (`warehouse`.`warehouse_fax` is null and (`warehouse`.`warehouse_name`,`warehouse`.`wa_address1`) " +
            "in (('Freeman And Co','234 West Covina Pkwy')))) or " +
            "((`warehouse`.`warehouse_name` is null or `warehouse`.`wa_address1` is null " +
            "or `warehouse`.`warehouse_fax` is null) and not((`warehouse`.`warehouse_fax` is null " +
            "and (`warehouse`.`warehouse_name`,`warehouse`.`wa_address1`) in " +
            "(('Freeman And Co','234 West Covina Pkwy')))))) " +
            "group by `warehouse`.`warehouse_fax`, `warehouse`.`wa_address1`, " +
            "`warehouse`.`warehouse_name`, `product_class`.`product_family` " +
            "order by ISNULL(`warehouse`.`warehouse_fax`), `warehouse`.`warehouse_fax` ASC, " +
            "ISNULL(`warehouse`.`wa_address1`), `warehouse`.`wa_address1` ASC, " +
            "ISNULL(`warehouse`.`warehouse_name`), `warehouse`.`warehouse_name` ASC, " +
            "ISNULL(`product_class`.`product_family`), `product_class`.`product_family` ASC";

        SqlPattern[] patterns =
            new SqlPattern[]{
                new SqlPattern(Dialect.DatabaseProduct.DERBY, necjSqlDerby, necjSqlDerby),
                new SqlPattern(Dialect.DatabaseProduct.MYSQL, necjSqlMySql, necjSqlMySql)
            };

        TestContext testContext =
            TestContext.create(
                dimension,
                cube,
                null,
                null,
                null,
                null);

        assertQuerySql(testContext, query, patterns);
    }
    
    /**
     * Test that null members are included when the filter explicitly excludes
     * a single member that has a null.  The members span multiple levels.
     */
    public void testNotInMultiLevelMemberConstraintSingleNullParent() {
        if (!isDefaultNullMemberRepresentation()) {
            return;
        }

        String dimension =
            "<Dimension name=\"Warehouse2\">\n" +
            "  <Hierarchy hasAll=\"true\" primaryKey=\"warehouse_id\">\n" +
            "    <Table name=\"warehouse\"/>\n" +
            "    <Level name=\"fax\" column=\"warehouse_fax\" uniqueMembers=\"true\"/>\n" +
            "    <Level name=\"address1\" column=\"wa_address1\" uniqueMembers=\"false\"/>\n" +
            "    <Level name=\"name\" column=\"warehouse_name\" uniqueMembers=\"false\"/>\n" +
            "  </Hierarchy>\n" +
            "</Dimension>\n";

        String cube =
            "<Cube name=\"Warehouse2\">\n" +
            "  <Table name=\"inventory_fact_1997\"/>\n" +
            "  <DimensionUsage name=\"Product\" source=\"Product\" foreignKey=\"product_id\"/>\n" +
            "  <DimensionUsage name=\"Warehouse2\" source=\"Warehouse2\" foreignKey=\"warehouse_id\"/>\n" +
            "  <Measure name=\"Warehouse Cost\" column=\"warehouse_cost\" aggregator=\"sum\"/>\n" +
            "  <Measure name=\"Warehouse Sales\" column=\"warehouse_sales\" aggregator=\"sum\"/>\n" +
            "</Cube>";

        String query =
            "with\n" +
            "set [Filtered Warehouse Set] as 'Filter([Warehouse2].[name].Members, [Warehouse2].CurrentMember Not In" +
            "{[Warehouse2].[#null].[234 West Covina Pkwy].[Freeman And Co]})' " +
            "set [NECJ] as NonEmptyCrossJoin([Filtered Warehouse Set], {[Product].[Product Family].Food}) " +
            "select [NECJ] on 0 from [Warehouse2]";

        String necjSqlDerby =
            "select \"warehouse\".\"warehouse_fax\", \"warehouse\".\"wa_address1\", " +
            "\"warehouse\".\"warehouse_name\", \"product_class\".\"product_family\" " +
            "from \"warehouse\" as \"warehouse\", \"inventory_fact_1997\" as " +
            "\"inventory_fact_1997\", \"product\" as \"product\", \"product_class\" " +
            "as \"product_class\" where \"inventory_fact_1997\".\"warehouse_id\" = " +
            "\"warehouse\".\"warehouse_id\" and \"product\".\"product_class_id\" = " +
            "\"product_class\".\"product_class_id\" and " +
            "\"inventory_fact_1997\".\"product_id\" = \"product\".\"product_id\" and " +
            "(\"product_class\".\"product_family\" = 'Food') and ((not " +
            "(\"warehouse\".\"warehouse_name\" = 'Freeman And Co') or " +
            "(\"warehouse\".\"warehouse_name\" is null)) or (not " +
            "(\"warehouse\".\"wa_address1\" = '234 West Covina Pkwy') or " +
            "(\"warehouse\".\"wa_address1\" is null)) or not " +
            "(\"warehouse\".\"warehouse_fax\" is null)) group by " +
            "\"warehouse\".\"warehouse_fax\", \"warehouse\".\"wa_address1\", " +
            "\"warehouse\".\"warehouse_name\", \"product_class\".\"product_family\" " +
            "order by \"warehouse\".\"warehouse_fax\" ASC, " +
            "\"warehouse\".\"wa_address1\" ASC, \"warehouse\".\"warehouse_name\" ASC, " +
            "\"product_class\".\"product_family\" ASC";

        String necjSqlMySql =
            "select `warehouse`.`warehouse_fax` as `c0`, " +
            "`warehouse`.`wa_address1` as `c1`, `warehouse`.`warehouse_name` " +
            "as `c2`, `product_class`.`product_family` as `c3` from " +
            "`warehouse` as `warehouse`, `inventory_fact_1997` as " +
            "`inventory_fact_1997`, `product` as `product`, `product_class` " +
            "as `product_class` where `inventory_fact_1997`.`warehouse_id` = " +
            "`warehouse`.`warehouse_id` and `product`.`product_class_id` = " +
            "`product_class`.`product_class_id` and " +
            "`inventory_fact_1997`.`product_id` = `product`.`product_id` and " +
            "(`product_class`.`product_family` = 'Food') and " +
            "((not (`warehouse`.`warehouse_name` = 'Freeman And Co') or " +
            "(`warehouse`.`warehouse_name` is null)) or (not " +
            "(`warehouse`.`wa_address1` = '234 West Covina Pkwy') or " +
            "(`warehouse`.`wa_address1` is null)) or not " +
            "(`warehouse`.`warehouse_fax` is null)) group by " +
            "`warehouse`.`warehouse_fax`, `warehouse`.`wa_address1`, " +
            "`warehouse`.`warehouse_name`, `product_class`.`product_family` " +
            "order by ISNULL(`warehouse`.`warehouse_fax`), " +
            "`warehouse`.`warehouse_fax` ASC, " +
            "ISNULL(`warehouse`.`wa_address1`), `warehouse`.`wa_address1` ASC, " +
            "ISNULL(`warehouse`.`warehouse_name`), " +
            "`warehouse`.`warehouse_name` ASC, " +
            "ISNULL(`product_class`.`product_family`), " +
            "`product_class`.`product_family` ASC";

        SqlPattern[] patterns =
            new SqlPattern[]{
                new SqlPattern(Dialect.DatabaseProduct.DERBY, necjSqlDerby, necjSqlDerby),
                new SqlPattern(Dialect.DatabaseProduct.MYSQL, necjSqlMySql, necjSqlMySql)
            };

        TestContext testContext =
            TestContext.create(
                dimension,
                cube,
                null,
                null,
                null,
                null);

        assertQuerySql(testContext, query, patterns);
    }

    public void testCachedNativeSetUsingFilters() throws Exception {
        propSaver.set(MondrianProperties.instance().ExpandNonNative, false);
        propSaver.set(MondrianProperties.instance().EnableNativeFilter, true);

        // Get a fresh connection; Otherwise the mondrian property setting
        // is not refreshed for this parameter.
        boolean requestFreshConnection = true;

        String query1 =
            "With " +
            "Set [*NATIVE_CJ_SET] as 'NonEmptyCrossJoin([*BASE_MEMBERS_Customers],[*BASE_MEMBERS_Product])' " +
            "Set [*BASE_MEMBERS_Customers] as 'Filter([Customers].[City].Members,Ancestor([Customers].CurrentMember, [Customers].[State Province]) In {[Customers].[All Customers].[USA].[CA]})' " +
            "Set [*BASE_MEMBERS_Product] as 'Filter([Product].[Product Family].Members,[Product].CurrentMember In {[Product].[All Products].[Drink]})' " +
            "Set [*CJ_ROW_AXIS] as 'Generate([*NATIVE_CJ_SET], {([Customers].currentMember,[Product].currentMember)})' " +
            "Select " +
            "{[Measures].[Customer Count]} on columns, " +
            "Non Empty [*CJ_ROW_AXIS] on rows " +
            "From [Sales]";

        checkNative(100, 45, query1, null, requestFreshConnection);

        // query2 has different filters; it should not reuse the result from query1.
        String query2 =
            "With " +
            "Set [*NATIVE_CJ_SET] as 'NonEmptyCrossJoin([*BASE_MEMBERS_Customers],[*BASE_MEMBERS_Product])' " +
            "Set [*BASE_MEMBERS_Customers] as 'Filter([Customers].[City].Members,Ancestor([Customers].CurrentMember, [Customers].[State Province]) In {[Customers].[All Customers].[USA].[OR]})' " +
            "Set [*BASE_MEMBERS_Product] as 'Filter([Product].[Product Family].Members,[Product].CurrentMember In {[Product].[All Products].[Drink]})' " +
            "Set [*CJ_ROW_AXIS] as 'Generate([*NATIVE_CJ_SET], {([Customers].currentMember,[Product].currentMember)})' " +
            "Select " +
            "{[Measures].[Customer Count]} on columns, " +
            "Non Empty [*CJ_ROW_AXIS] on rows " +
            "From [Sales]";

        checkNative(100, 11, query2, null, requestFreshConnection);        		

    }

    public void testNativeFilter() {
        propSaver.set(MondrianProperties.instance().ExpandNonNative, false);
        String query =
            "select {[Measures].[Store Sales]} ON COLUMNS, "
            + "Order(Filter(Descendants([Customers].[All Customers].[USA].[CA], [Customers].[Name]), ([Measures].[Store Sales] > 200.0)), [Measures].[Store Sales], DESC) ON ROWS "
            + "from [Sales] "
            + "where ([Time].[1997])";

        boolean origNativeFilter =
            MondrianProperties.instance().EnableNativeFilter.get();
        MondrianProperties.instance().EnableNativeFilter.set(true);

        // Get a fresh connection; Otherwise the mondrian property setting
        // is not refreshed for this parameter.
        boolean requestFreshConnection = true;
        checkNative(32, 18, query, null, requestFreshConnection);

        MondrianProperties.instance().EnableNativeFilter.set(origNativeFilter);
    }

    /**
     * Executes a Filter() whose condition contains a calculated member.
     */
    public void testCmNativeFilter() {
        propSaver.set(MondrianProperties.instance().ExpandNonNative, false);
        String query =
            "with member [Measures].[Rendite] as '([Measures].[Store Sales] - [Measures].[Store Cost]) / [Measures].[Store Cost]' "
            + "select NON EMPTY {[Measures].[Unit Sales], [Measures].[Store Cost], [Measures].[Rendite], [Measures].[Store Sales]} ON COLUMNS, "
            + "NON EMPTY Order(Filter([Product].[Product Name].Members, ([Measures].[Rendite] > 1.8)), [Measures].[Rendite], BDESC) ON ROWS "
            + "from [Sales] "
            + "where ([Store].[All Stores].[USA].[CA], [Time].[1997])";

        String result =
            "Axis #0:\n" +
            "{[Store].[All Stores].[USA].[CA], [Time].[1997]}\n" +
            "Axis #1:\n" +
            "{[Measures].[Unit Sales]}\n" +
            "{[Measures].[Store Cost]}\n" +
            "{[Measures].[Rendite]}\n" +
            "{[Measures].[Store Sales]}\n" +
            "Axis #2:\n" +
            "{[Product].[All Products].[Food].[Baking Goods].[Jams and Jellies].[Peanut Butter].[Plato].[Plato Extra Chunky Peanut Butter]}\n" +
            "{[Product].[All Products].[Food].[Snack Foods].[Snack Foods].[Popcorn].[Horatio].[Horatio Buttered Popcorn]}\n" +
            "{[Product].[All Products].[Food].[Canned Foods].[Canned Tuna].[Tuna].[Better].[Better Canned Tuna in Oil]}\n" +
            "{[Product].[All Products].[Food].[Produce].[Fruit].[Fresh Fruit].[High Top].[High Top Cantelope]}\n" +
            "{[Product].[All Products].[Non-Consumable].[Household].[Electrical].[Lightbulbs].[Denny].[Denny 75 Watt Lightbulb]}\n" +
            "{[Product].[All Products].[Food].[Breakfast Foods].[Breakfast Foods].[Cereal].[Johnson].[Johnson Oatmeal]}\n" +
            "{[Product].[All Products].[Drink].[Alcoholic Beverages].[Beer and Wine].[Wine].[Portsmouth].[Portsmouth Light Wine]}\n" +
            "{[Product].[All Products].[Food].[Produce].[Vegetables].[Fresh Vegetables].[Ebony].[Ebony Squash]}\n" +
            "Row #0: 42\n" +
            "Row #0: 24.06\n" +
            "Row #0: 1.93\n" +
            "Row #0: 70.56\n" +
            "Row #1: 36\n" +
            "Row #1: 29.02\n" +
            "Row #1: 1.91\n" +
            "Row #1: 84.60\n" +
            "Row #2: 39\n" +
            "Row #2: 20.55\n" +
            "Row #2: 1.85\n" +
            "Row #2: 58.50\n" +
            "Row #3: 25\n" +
            "Row #3: 21.76\n" +
            "Row #3: 1.84\n" +
            "Row #3: 61.75\n" +
            "Row #4: 43\n" +
            "Row #4: 59.62\n" +
            "Row #4: 1.83\n" +
            "Row #4: 168.99\n" +
            "Row #5: 34\n" +
            "Row #5: 7.20\n" +
            "Row #5: 1.83\n" +
            "Row #5: 20.40\n" +
            "Row #6: 36\n" +
            "Row #6: 33.10\n" +
            "Row #6: 1.83\n" +
            "Row #6: 93.60\n" +
            "Row #7: 46\n" +
            "Row #7: 28.34\n" +
            "Row #7: 1.81\n" +
            "Row #7: 79.58\n";

        boolean origNativeFilter =
            MondrianProperties.instance().EnableNativeFilter.get();
        MondrianProperties.instance().EnableNativeFilter.set(true);

        // Get a fresh connection; Otherwise the mondrian property setting
        // is not refreshed for this parameter.
        boolean requestFreshConnection = true;
        checkNative(0, 8, query, result, requestFreshConnection);

        MondrianProperties.instance().EnableNativeFilter.set(origNativeFilter);
    }

    public void testNonNativeFilterWithNullMeasure() {
        propSaver.set(MondrianProperties.instance().ExpandNonNative, false);
        String query =
            "select Filter([Store].[Store Name].members, " +
            "              Not ([Measures].[Store Sqft] - [Measures].[Grocery Sqft] < 10000)) on rows, " +
            "{[Measures].[Store Sqft], [Measures].[Grocery Sqft]} on columns " +
            "from [Store]";

        String result =
            "Axis #0:\n" +
            "{}\n" +
            "Axis #1:\n" +
            "{[Measures].[Store Sqft]}\n" +
            "{[Measures].[Grocery Sqft]}\n" +
            "Axis #2:\n" +
            "{[Store].[All Stores].[Mexico].[DF].[Mexico City].[Store 9]}\n" +
            "{[Store].[All Stores].[Mexico].[DF].[San Andres].[Store 21]}\n" +
            "{[Store].[All Stores].[Mexico].[Yucatan].[Merida].[Store 8]}\n" +
            "{[Store].[All Stores].[USA].[CA].[Alameda].[HQ]}\n" +
            "{[Store].[All Stores].[USA].[CA].[San Diego].[Store 24]}\n" +
            "{[Store].[All Stores].[USA].[WA].[Bremerton].[Store 3]}\n" +
            "{[Store].[All Stores].[USA].[WA].[Tacoma].[Store 17]}\n" +
            "{[Store].[All Stores].[USA].[WA].[Walla Walla].[Store 22]}\n" +
            "{[Store].[All Stores].[USA].[WA].[Yakima].[Store 23]}\n" +
            "Row #0: 36,509\n" +
            "Row #0: 22,450\n" +
            "Row #1: \n" +
            "Row #1: \n" +
            "Row #2: 30,797\n" +
            "Row #2: 20,141\n" +
            "Row #3: \n" +
            "Row #3: \n" +
            "Row #4: \n" +
            "Row #4: \n" +
            "Row #5: 39,696\n" +
            "Row #5: 24,390\n" +
            "Row #6: 33,858\n" +
            "Row #6: 22,123\n" +
            "Row #7: \n" +
            "Row #7: \n" +
            "Row #8: \n" +
            "Row #8: \n";

        boolean origNativeFilter =
            MondrianProperties.instance().EnableNativeFilter.get();
        MondrianProperties.instance().EnableNativeFilter.set(false);

        checkNotNative(9, query, result);

        MondrianProperties.instance().EnableNativeFilter.set(origNativeFilter);
    }

    public void testNativeFilterWithNullMeasure() {
        propSaver.set(MondrianProperties.instance().ExpandNonNative, false);
        // Currently this behaves differently from the non-native evaluation.
        String query =
            "select Filter([Store].[Store Name].members, " +
            "              Not ([Measures].[Store Sqft] - [Measures].[Grocery Sqft] < 10000)) on rows, " +
            "{[Measures].[Store Sqft], [Measures].[Grocery Sqft]} on columns " +
            "from [Store]";

        String result =
            "Axis #0:\n" +
            "{}\n" +
            "Axis #1:\n" +
            "{[Measures].[Store Sqft]}\n" +
            "{[Measures].[Grocery Sqft]}\n" +
            "Axis #2:\n" +
            "{[Store].[All Stores].[Mexico].[DF].[Mexico City].[Store 9]}\n" +
            "{[Store].[All Stores].[Mexico].[Yucatan].[Merida].[Store 8]}\n" +
            "{[Store].[All Stores].[USA].[WA].[Bremerton].[Store 3]}\n" +
            "{[Store].[All Stores].[USA].[WA].[Tacoma].[Store 17]}\n" +
            "Row #0: 36,509\n" +
            "Row #0: 22,450\n" +
            "Row #1: 30,797\n" +
            "Row #1: 20,141\n" +
            "Row #2: 39,696\n" +
            "Row #2: 24,390\n" +
            "Row #3: 33,858\n" +
            "Row #3: 22,123\n";

        boolean origNativeFilter =
            MondrianProperties.instance().EnableNativeFilter.get();
        MondrianProperties.instance().EnableNativeFilter.set(true);

        // Get a fresh connection; Otherwise the mondrian property setting
        // is not refreshed for this parameter.
        Connection conn = getTestContext().getFoodMartConnection(false);
        TestContext context = getTestContext(conn);
        context.assertQueryReturns(query, result);

        MondrianProperties.instance().EnableNativeFilter.set(origNativeFilter);
    }

    public void testNonNativeFilterWithCalcMember() {
        propSaver.set(MondrianProperties.instance().ExpandNonNative, false);
        // Currently this query cannot run natively
        String query =
            "with\n" +
            "member [Time].[Date Range] as 'Aggregate({[Time].[1997].[Q1]:[Time].[1997].[Q4]})'\n" +
            "select\n" +
            "{[Measures].[Unit Sales]} ON columns,\n" +
            "Filter ([Store].[Store State].members, [Measures].[Store Cost] > 100) ON rows\n" +
            "from [Sales]\n" +
            "where [Time].[Date Range]\n";

        String result =
            "Axis #0:\n" +
            "{[Time].[Date Range]}\n" +
            "Axis #1:\n" +
            "{[Measures].[Unit Sales]}\n" +
            "Axis #2:\n" +
            "{[Store].[All Stores].[USA].[CA]}\n" +
            "{[Store].[All Stores].[USA].[OR]}\n" +
            "{[Store].[All Stores].[USA].[WA]}\n" +
            "Row #0: 74,748\n" +
            "Row #1: 67,659\n" +
            "Row #2: 124,366\n";

        boolean origNativeFilter =
            MondrianProperties.instance().EnableNativeFilter.get();
        MondrianProperties.instance().EnableNativeFilter.set(false);

        checkNotNative(3, query, result);
        MondrianProperties.instance().EnableNativeFilter.set(origNativeFilter);
    }

    /**
     * Verify that filter with Not IsEmpty(storedMeasure) can be natively
     * evaluated.
     */
    public void testNativeFilterNonEmpty() {
        propSaver.set(MondrianProperties.instance().ExpandNonNative, false);
        String query =
            "select Filter(CrossJoin([Store].[Store Name].members, " +
            "                        [Store Type].[Store Type].members), " +
            "                        Not IsEmpty([Measures].[Store Sqft])) on rows, " +
            "{[Measures].[Store Sqft]} on columns " +
            "from [Store]";

        boolean origNativeFilter =
            MondrianProperties.instance().EnableNativeFilter.get();
        MondrianProperties.instance().EnableNativeFilter.set(true);

        // Get a fresh connection; Otherwise the mondrian property setting
        // is not refreshed for this parameter.
        boolean requestFreshConnection = true;
        checkNative(0, 20, query, null, requestFreshConnection);

        MondrianProperties.instance().EnableNativeFilter.set(origNativeFilter);
    }
}
