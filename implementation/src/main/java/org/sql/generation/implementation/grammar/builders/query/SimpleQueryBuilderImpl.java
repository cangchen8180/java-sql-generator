/*
 * Copyright (c) 2010, Stanislav Muhametsin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.sql.generation.implementation.grammar.builders.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sql.generation.api.common.NullArgumentException;
import org.sql.generation.api.grammar.booleans.BooleanExpression;
import org.sql.generation.api.grammar.builders.query.QuerySpecificationBuilder;
import org.sql.generation.api.grammar.builders.query.SimpleQueryBuilder;
import org.sql.generation.api.grammar.common.TableName;
import org.sql.generation.api.grammar.common.ValueExpression;
import org.sql.generation.api.grammar.factories.ColumnsFactory;
import org.sql.generation.api.grammar.factories.QueryFactory;
import org.sql.generation.api.grammar.factories.TableReferenceFactory;
import org.sql.generation.api.grammar.query.ColumnReference;
import org.sql.generation.api.grammar.query.ColumnReferences.ColumnReferenceInfo;
import org.sql.generation.api.grammar.query.Ordering;
import org.sql.generation.api.grammar.query.QueryExpression;
import org.sql.generation.api.vendor.SQLVendor;
import org.sql.generation.implementation.grammar.common.SQLBuilderBase;
import org.sql.generation.implementation.transformation.spi.SQLProcessorAggregator;

/**
 * 
 * @author Stanislav Muhametsin
 */
public class SimpleQueryBuilderImpl extends SQLBuilderBase
    implements SimpleQueryBuilder
{

    private final List<ColumnReference> _columns;

    private final Map<Integer, String> _columnAliases;

    private final List<TableName> _from;

    private BooleanExpression _where;

    private final List<String> _groupBy;

    private BooleanExpression _having;

    private final List<String> _orderBy;

    private final List<Ordering> _orderings;

    private final SQLVendor _vendor;

    private boolean _selectAll;

    public SimpleQueryBuilderImpl( SQLProcessorAggregator processor, SQLVendor vendor )
    {
        super( processor );
        NullArgumentException.validateNotNull( "Vendor", vendor );

        this._vendor = vendor;
        this._columns = new ArrayList<ColumnReference>();
        this._columnAliases = new HashMap<Integer, String>();
        this._from = new ArrayList<TableName>();
        this._groupBy = new ArrayList<String>();
        this._orderBy = new ArrayList<String>();
        this._orderings = new ArrayList<Ordering>();
        this._selectAll = false;
    }

    public QueryExpression createExpression()
    {
        QueryFactory q = this._vendor.getQueryFactory();

        QuerySpecificationBuilder builda = q.querySpecificationBuilder();

        this.processQuerySpecBuilder( builda );

        return q.createQuery( builda.createExpression() );
    }

    protected void processQuerySpecBuilder( QuerySpecificationBuilder builda )
    {
        QueryFactory q = this._vendor.getQueryFactory();
        ColumnsFactory c = this._vendor.getColumnsFactory();
        TableReferenceFactory t = this._vendor.getTableReferenceFactory();

        if( this._selectAll )
        {
            builda.getSelect().selectAll();
        }
        else
        {
            for( Integer colIndex = 0; colIndex < this._columns.size(); ++colIndex )
            {
                ColumnReference ref = this._columns.get( colIndex );
                String alias = this._columnAliases.get( colIndex );
                builda.getSelect().addNamedColumns( new ColumnReferenceInfo( alias, ref ) );
            }
        }
        for( TableName tableName : this._from )
        {
            builda.getFrom().addTableReferences( t.tableBuilder( t.table( tableName ) ) );
        }

        builda.getWhere().reset( this._where );

        for( String groupBy : this._groupBy )
        {
            builda.getGroupBy().addGroupingElements( q.groupingElement( c.colName( groupBy ) ) );
        }

        builda.getHaving().reset( this._having );

        for( Integer orderByIndex = 0; orderByIndex < this._orderBy.size(); ++orderByIndex )
        {
            builda.getOrderBy().addSortSpecs(
                q.sortSpec( c.colName( this._orderBy.get( orderByIndex ) ), this._orderings.get( orderByIndex ) ) );
        }
    }

    protected SQLVendor getVendor()
    {
        return this._vendor;
    }

    public SimpleQueryBuilder select( String... columnNames )
    {
        this._selectAll = false;
        for( String col : columnNames )
        {
            this._columns.add( this._vendor.getColumnsFactory().colName( col ) );
        }
        return this;
    }

    public SimpleQueryBuilder select( ValueExpression... expressions )
    {
        this._selectAll = false;
        for( ValueExpression exp : expressions )
        {
            this._columns.add( this._vendor.getColumnsFactory().colExp( exp ) );
        }
        return this;
    }

    public SimpleQueryBuilder selectAllColumns()
    {
        this._selectAll = true;
        return this;
    }

    public SimpleQueryBuilder as( String columnAlias )
    {
        this._columnAliases.put( this._columns.size() - 1, columnAlias );
        return this;
    }

    public SimpleQueryBuilder from( TableName... tableNames )
    {
        for( TableName table : tableNames )
        {
            this._from.add( table );
        }
        return this;
    }

    public SimpleQueryBuilder where( BooleanExpression searchCondition )
    {
        this._where = searchCondition;
        return this;
    }

    public SimpleQueryBuilder groupBy( String... columns )
    {
        for( String col : columns )
        {
            this._groupBy.add( col );
        }
        return this;
    }

    public SimpleQueryBuilder having( BooleanExpression groupingCondition )
    {
        this._having = groupingCondition;
        return this;
    }

    public SimpleQueryBuilder orderByAsc( String... columns )
    {
        for( String col : columns )
        {
            this._orderBy.add( col );
            this._orderings.add( Ordering.ASCENDING );
        }
        return this;
    }

    public SimpleQueryBuilder orderByDesc( String... columns )
    {
        for( String col : columns )
        {
            this._orderBy.add( col );
            this._orderings.add( Ordering.DESCENDING );
        }
        return this;
    }
}
