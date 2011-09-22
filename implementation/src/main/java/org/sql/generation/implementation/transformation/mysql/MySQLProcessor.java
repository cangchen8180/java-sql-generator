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

package org.sql.generation.implementation.transformation.mysql;

import java.util.HashMap;
import java.util.Map;

import org.atp.api.Typeable;
import org.sql.generation.api.grammar.common.TableNameDirect;
import org.sql.generation.api.grammar.common.TableNameFunction;
import org.sql.generation.api.grammar.definition.schema.SchemaDefinition;
import org.sql.generation.api.grammar.manipulation.DropSchemaStatement;
import org.sql.generation.implementation.transformation.DefaultSQLProcessor;
import org.sql.generation.implementation.transformation.NoOpProcessor;
import org.sql.generation.implementation.transformation.mysql.MySQLDefinitionProcessing.MySQLSchemaDefinitionProcessor;
import org.sql.generation.implementation.transformation.mysql.MySQLTableProcessing.MySQLTableNameDirectProcessor;
import org.sql.generation.implementation.transformation.mysql.MySQLTableProcessing.MySQLTableNameFunctionProcessor;
import org.sql.generation.implementation.transformation.spi.SQLProcessor;

/**
 * 
 * @author Stanislav Muhametsin
 */
public class MySQLProcessor extends DefaultSQLProcessor
{

    private static final Map<Class<? extends Typeable<?>>, SQLProcessor> _defaultProcessors;

    static
    {
        Map<Class<? extends Typeable<?>>, SQLProcessor> processors = new HashMap<Class<? extends Typeable<?>>, SQLProcessor>(
            DefaultSQLProcessor.getDefaultProcessors() );

        // MySQL does not understand schema-qualified table names (or anything related to schemas)
        processors.put( TableNameDirect.class, new MySQLTableNameDirectProcessor() );
        processors.put( TableNameFunction.class, new MySQLTableNameFunctionProcessor() );

        // Only process schema elements from schema definition, and ignore drop schema statements
        processors.put( SchemaDefinition.class, new MySQLSchemaDefinitionProcessor() );
        processors.put( DropSchemaStatement.class, new NoOpProcessor() );

        _defaultProcessors = processors;
    }

    public MySQLProcessor()
    {
        super( _defaultProcessors );
    }
}
