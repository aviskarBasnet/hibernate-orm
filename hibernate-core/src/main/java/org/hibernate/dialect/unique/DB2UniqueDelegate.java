/* 
 * Hibernate, Relational Persistence for Idiomatic Java
 * 
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.hibernate.dialect.unique;

import java.util.Iterator;

import org.hibernate.dialect.Dialect;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.metamodel.spi.relational.Column;
import org.hibernate.metamodel.spi.relational.UniqueKey;

/**
 * DB2 does not allow unique constraints on nullable columns.  Rather than
 * forcing "not null", use unique *indexes* instead.
 * 
 * @author Brett Meyer
 */
@Deprecated
public class DB2UniqueDelegate extends DefaultUniqueDelegate {
	/**
	 * Constructs a DB2UniqueDelegate
	 *
	 * @param dialect The dialect
	 */
	public DB2UniqueDelegate( Dialect dialect ) {
		super( dialect );
	}

	@Override
	public String getAlterTableToAddUniqueKeyCommand(
			org.hibernate.mapping.UniqueKey uniqueKey,
			String defaultCatalog,
			String defaultSchema) {
		if ( hasNullable( uniqueKey ) ) {
			return org.hibernate.mapping.Index.buildSqlCreateIndexString(
					dialect,
					uniqueKey.getName(),
					uniqueKey.getTable(),
					uniqueKey.columnIterator(),
					uniqueKey.getColumnOrderMap(),
					true,
					defaultCatalog,
					defaultSchema
			);
		}
		else {
			return super.getAlterTableToAddUniqueKeyCommand( uniqueKey, defaultCatalog, defaultSchema );
		}
	}
	
	@Override
	public String getAlterTableToAddUniqueKeyCommand(UniqueKey uniqueKey) {
		if ( hasNullable( uniqueKey ) ) {
			// TODO: This borrows from Index's old way of doing things.  This
			// should be using StandardIndexExporter.  However, not all callers
			// have JdbcEnvironment available.  We'll need to refactor a bit...
			String keyName = dialect.qualifyIndexName() ? uniqueKey.getName()
					: StringHelper.unqualify( uniqueKey.getName() );
			StringBuilder buf = new StringBuilder( "create unique index " )
					.append( keyName ).append( " on " )
					.append( uniqueKey.getTable().getQualifiedName( dialect ) )
					.append( " (" );
			boolean first = true;
			for ( Column column : uniqueKey.getColumns() ) {
				if ( first ) {
					first = false;
				}
				else {
					buf.append( ", " );
				}
				buf.append( ( column.getColumnName().getText( dialect ) ) );
			}
			buf.append( ")" );
			return buf.toString();
		}
		else {
			return super.getAlterTableToAddUniqueKeyCommand( uniqueKey );
		}
	}
	
	@Override
	public String getAlterTableToDropUniqueKeyCommand(
			org.hibernate.mapping.UniqueKey uniqueKey,
			String defaultCatalog,
			String defaultSchema) {
		if ( hasNullable( uniqueKey ) ) {
			return org.hibernate.mapping.Index.buildSqlDropIndexString(
					dialect,
					uniqueKey.getTable(),
					uniqueKey.getName(),
					defaultCatalog,
					defaultSchema
			);
		}
		else {
			return super.getAlterTableToDropUniqueKeyCommand(
					uniqueKey, defaultCatalog, defaultSchema
			);
		}
	}
	
	@Override
	public String getAlterTableToDropUniqueKeyCommand(UniqueKey uniqueKey) {
		if ( hasNullable( uniqueKey ) ) {
			// TODO: This borrows from Index's old way of doing things.  This
			// should be using StandardIndexExporter.  However, not all callers
			// have JdbcEnvironment available.  We'll need to refactor a bit...
			return "drop index " + StringHelper.qualify(
					uniqueKey.getTable().getQualifiedName( dialect ),
							uniqueKey.getName() );
		}
		else {
			return super.getAlterTableToDropUniqueKeyCommand( uniqueKey );
		}
	}
	
	private boolean hasNullable(org.hibernate.mapping.UniqueKey uniqueKey) {
		final Iterator<org.hibernate.mapping.Column> iter = uniqueKey.columnIterator();
		while ( iter.hasNext() ) {
			if ( iter.next().isNullable() ) {
				return true;
			}
		}
		return false;
	}

	private boolean hasNullable(UniqueKey uniqueKey) {
		for ( Column column : uniqueKey.getColumns() ) {
			if ( column.isNullable() ) {
				return true;
			}
		}
		return false;
	}
}
