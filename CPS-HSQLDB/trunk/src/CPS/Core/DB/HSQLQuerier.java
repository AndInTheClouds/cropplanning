/* HSQLQuery.java - Created: March 15, 2007
 * Copyright (C) 2007, 2008 Clayton Carter
 * 
 * This file is part of the project "Crop Planning Software".  For more
 * information:
 *    website: http://cropplanning.googlecode.com
 *    email:   cropplanning@gmail.com 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package CPS.Core.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A class to handle the nitty-gritty queries of the database.
 * @author Clayton
 */
public class HSQLQuerier {
   
   Connection con;
   ResultSet rsCache;
   
   HSQLQuerier( Connection c ) {
      con = c;
   }
   
   public ResultSet getCachedResults() {
      return rsCache;
   }
   
//   public TableModel getCachedResultsAsTable() {
//      return tableResults( getCachedResults() );
//   }
//   
//   public TableModel tableQuery( String table, String columns, String conditional ) {
//      return tableResults( storeQuery( table, columns, conditional ));
//   }
      
   public ResultSet storeQuery( String table, String columns ) {
      return storeQuery( table, columns, null, null );
   }
   public ResultSet storeQuery( String table, String columns, String conditional ) {
      return storeQuery( table, columns, conditional, null );
   }
   public ResultSet storeQuery( String table, String columns, String conditional, String sort ) {
      return storeQuery( table, columns, conditional, sort, null );
   }
   public ResultSet storeQuery( String table, String columns, 
                                String conditional, String sort, String filter ) {
      return submitQuery( con, table, columns, conditional, sort, filter, true );
   }
   
   public ResultSet submitQuery( String table, String columns ) {
      return submitQuery( table, columns, null, null, null );
   }
   public ResultSet submitQuery( String table, String columns, String conditional ) {
      return submitQuery( table, columns, conditional, null, null );
   }
   public ResultSet submitQuery( String table, String columns, String conditional, String sort ) {
      return submitQuery( table, columns, conditional, sort, null);
   }
   public ResultSet submitQuery( String table, String columns, 
                                 String conditional, String sort, String filter ) {
      return submitQuery( con, table, columns, conditional, sort, filter, false );
   }

   
   /* ****************
    * Static Methods *
    * ****************/
   static String putTogetherConditionalSortAndFilterString( String conditional,
                                                            String sort,
                                                            String filter ) {
      String s = " ";
      
      boolean cond = conditional != null && conditional.length() > 0;
      boolean filt = filter != null      && filter.length() > 0;
      
      if ( cond || filt ) {
         s += " WHERE ( ";
         if ( cond )
            s += conditional;
         
         if ( filt ) {
            if ( cond )
               s += " AND ( " + filter + " )";
            else
               s += filter;
            
         }
         
         s += " ) ";
      }
      
      /* if sort doesn't include crop_name, then make crop_name secondary sort
       * if sort does include crop_name, then make var_name secondary sort
       */
      if ( sort != null && sort.length() > 0 ) {
          if ( sort.indexOf( "crop_name" ) == -1 )
              sort += ", crop_name";
          else if ( sort.indexOf( "var_name" ) == -1 )
              sort += ", var_name";
          
          s += " ORDER BY " + sort;
      }
      
      return s;
   }
   
   static synchronized ResultSet submitQuery( Connection con,
                                              String table,
                                              String columns, 
                                              String conditional,
                                              String sort,
                                              String filter,
                                              boolean prepared ) {
       
       table = HSQLDB.escapeTableName( table );
       
      if ( sort != null && sort.length() > 0 ) {
          String[] tok = sort.split( " " );
          columns += ", CASE WHEN " + tok[0] + " IS NULL ";
          columns +=        "THEN -1 ELSE 1 END ";
          columns += " AS nulls_last ";
          
          sort = "nulls_last DESC, " + sort;
      }
          
      String query = "SELECT " + columns + " FROM " + table;
      query += putTogetherConditionalSortAndFilterString( conditional, sort, filter );
      
      return submitRawQuery( con, query, prepared );
   }
   
   public static synchronized ResultSet submitRawQuery( Connection con, String query, boolean prepared ) {
      ResultSet rs;
      
      if ( query.length() < 500 )
           System.out.println( "DEBUG Submitting query: " + query );
      else
           System.out.println( "DEBUG Submitting query: " + query.substring( 0 , 500 ) + " ... (truncated)" );
      
      try {
         if ( prepared ) {
            PreparedStatement ps = con.prepareStatement( query,
                                                         ResultSet.TYPE_SCROLL_INSENSITIVE,
                                                         ResultSet.CONCUR_READ_ONLY );
            rs = ps.executeQuery();
         }
         else {            // These parameters are cribbed from ResultSetTableModelFactory
            Statement s = con.createStatement( ResultSet.TYPE_SCROLL_INSENSITIVE,
                                               ResultSet.CONCUR_READ_ONLY );
            rs = s.executeQuery( query );
         }  
      }
      catch ( SQLException e ) {
         e.printStackTrace();
         rs = null;
      }
      return rs;
   }
   
   public static synchronized long getLastUsedVersion( Connection con ) {
       
       try {
           Statement s = con.createStatement();
           String query = "SELECT prev_ver FROM " + 
                          HSQLDB.escapeTableName( "CPS_METADATA" );
           ResultSet rs = s.executeQuery( query );
           
           if ( rs.next() ) {
               long prev_ver = rs.getLong( "prev_ver" );
               return prev_ver;
           }
           else {
               System.out.println("ERROR: couldn't find previous version (no results)");
               return -1;
           }
           
      }
      catch ( SQLException e ) {
         e.printStackTrace();
         return -1;
      }
   }
   
   
   public static synchronized ArrayList<String> getDistinctValuesForColumn( Connection con, ArrayList<String> tables, String column ) {
       
       ArrayList<String> l = new ArrayList<String>();
       Set set = new HashSet();
      
       for ( String table : tables ) {
           l = HSQLQuerier.getDistinctValuesForColumn( con, table, column );
           set.addAll( l );
       }
      
       l.clear();
       l.addAll( set );
      
       return l;
       
   }
   
   public static synchronized ArrayList<String> getDistinctValuesForColumn( Connection con, String table, String column ) {
       if ( table == null || column == null ) 
           return new ArrayList<String>();
       
      try {
         String query = "SELECT DISTINCT " + column + " FROM " + HSQLDB.escapeTableName( table );
         Statement st = con.createStatement();
         ResultSet rs = st.executeQuery( query );
      
//         System.out.println("Executed query: " + query );
         
         ArrayList<String> l = new ArrayList<String>();
         while ( rs.next() ) {
            String s = (String) rs.getObject(1);
            if ( s == null || s.equals( "" ) )
               continue;
//            System.out.println("DEBUG Adding item " + (String) rs.getObject(1) );
            l.add( s );
         }
         Collections.sort( l, String.CASE_INSENSITIVE_ORDER );
         return l;
      }
      catch ( SQLException e ) {
         e.printStackTrace();
         return new ArrayList<String>();
      }
      
   }
   
   
   /**
    * A method to create and submit queries which conflate the results and return a
    * single row ResultSet whose column values are not null only if EVERY row queried 
    * contains identical values.  If any row in the selection contains a different value,
    * then the value for that column in the ResultSet is NULL.
    * 
    * @param table Name of the table to query.
    * @param columns ArrayList<String> of all columns to query.
    * @param ids ArrayList<Integer> of all row indices (actually, records with column id == id)
    * @return A ResultSet with only one row whose column values are either NULL (for heterogeneous
    *         column values data) or the value which is homogeneous across all queried rows.
    */
   public synchronized ResultSet submitCommonInfoQuery( String table,
                                                         ArrayList<String> columns,
                                                         ArrayList<Integer> ids ) {
      String idString = HSQLDB.intListToIDString(ids);
      
      /* start the query string */
      String query = "";
      query += "SELECT ";
      
      for ( String col : columns ) {
         query += "MIN( SELECT ";
         query += "( CASE WHEN count(*)=1 ";
         query += "THEN MIN( " + col + " ) ELSE null END ) ";
         query += "FROM( SELECT DISTINCT " + col + " FROM " + HSQLDB.escapeTableName( table ) + " ";
         query += "WHERE id IN ( " + idString + " ) ) ) AS " + col + ", "; 
      }
      query = query.substring( 0, query. lastIndexOf( ", " ));
      
      query += " FROM " + HSQLDB.escapeTableName( table );
      
      System.out.println("\n DEBUG(COMMON INFO query):\n" + query );
      
      return submitRawQuery( con, query, false );
   }

   
   public synchronized ResultSet submitSummedCropPlanQuery( String planName,
                                                              ArrayList<String[]> plantColMap,
                                                              ArrayList<String[]> cropColMap,
                                                              ArrayList<String> displayColumns,
                                                              String filterExp ) {
       String sumCols = "";
       for ( String s : displayColumns )
           sumCols += " SUM( " + s + " ) AS " + s + ", ";
       sumCols = sumCols.substring( 0, sumCols.lastIndexOf( ", " ) );
       
       return submitCalculatedCropPlanQuery( planName, plantColMap, cropColMap, sumCols, null, filterExp);
       
   }
   
   public synchronized ResultSet submitCalculatedCropAndVarQuery( ArrayList<String[]> colMap,
                                                                  String displayColumns,
                                                                  String sortColumn,
                                                                  String filterExp ) {
      
       String filledInQuery = createCoalescedCropAndVarQueryString( colMap );
      
      // using pass2 in parens as the tablename; this is sort of a virtual table
      return storeQuery( "( " + filledInQuery + " )", displayColumns, null, sortColumn, filterExp ); 
      
   }
   
   public synchronized ResultSet submitCalculatedCropPlanQuery( String planName,
                                                                ArrayList<String[]> plantColMap,
                                                                ArrayList<String[]> cropColMap,
                                                                String displayColumns,
                                                                String sortColumn,
                                                                String filterExp ) {
      
       String filledInQuery = createCoalescedCropPlanQueryString( planName, plantColMap, cropColMap );
      
      // using pass2 in parens as the tablename; this is sort of a virtual table
      return storeQuery( "( " + filledInQuery + " )", displayColumns, null, sortColumn, filterExp ); 
      
   }
   
    private String createCoalescedCropAndVarQueryString( ArrayList<String[]> colMap ) {
       
       int CROP_COL = 0;
       int MAP_COL = 1;
       
       String cropFillInQuery, varietySelect, cropSelect;
       /* This string represents the query which will fill in the "static" fields
        * in each crop (w/ variety) from the corresponding fields in the crop (w/o variety) */
       varietySelect = cropSelect = " SELECT ";
       for ( String[] s : colMap ) {
           cropSelect += s[CROP_COL] + ", ";
           if ( s[MAP_COL] == null ) {
               varietySelect += "v." + s[CROP_COL] + ", ";
               continue;
           }
           varietySelect += "COALESCE( v." + s[CROP_COL] + ", ";
           varietySelect += "c." + s[CROP_COL] + " ) AS " + s[CROP_COL];
           varietySelect += ", ";
       }
       varietySelect = varietySelect.substring( 0, varietySelect.lastIndexOf( ", " ) );
       cropSelect = cropSelect.substring( 0, cropSelect.lastIndexOf( ", " ) );
       
//       cropFillInQuery += " FROM crops_varieties AS v, crops_varieties AS c ";
//       cropFillInQuery += " FROM        crops_varieties                                        AS v ";
       
       cropFillInQuery  = varietySelect;
       cropFillInQuery += " FROM      ( SELECT * FROM crops_varieties WHERE var_name IS NOT NULL ) AS v ";
       cropFillInQuery += " LEFT JOIN ( SELECT * FROM crops_varieties WHERE var_name IS     NULL ) AS c ";
       cropFillInQuery += " ON v.crop_name = c.crop_name ";
       cropFillInQuery += " UNION " + cropSelect;
       cropFillInQuery += " FROM       ( SELECT * FROM crops_varieties WHERE var_name IS NOT NULL ) AS v ";
       cropFillInQuery += " RIGHT JOIN ( SELECT * FROM crops_varieties WHERE var_name IS     NULL ) AS c ";
       cropFillInQuery += " ON v.crop_name = c.crop_name ";
       
       return cropFillInQuery;
        
    }
   
   private String createCoalescedCropPlanQueryString( String planName,
                                                      ArrayList<String[]> plantColMap,
                                                      ArrayList<String[]> cropColMap ) {
       
       planName = HSQLDB.escapeTableName( planName );
       
       int PLANT_COL = 0;
       int CROP_COL = 1;
       int CALC = 2;
      
       String cropFillInQuery = createCoalescedCropAndVarQueryString( cropColMap );
       
       /* This string represents the query which will fill in the "static" fields
        * in each planting from the corresponding fields in the crop */
       String plantingFillInQuery = "SELECT ";
       for ( String[] s : plantColMap ) {
           if ( s[CROP_COL] == null )
               plantingFillInQuery += "p." + s[PLANT_COL];
           else {
               plantingFillInQuery += "COALESCE( p." + s[PLANT_COL] + ", ";
               plantingFillInQuery += "c." + s[CROP_COL] + " ) AS " + s[PLANT_COL];
           }
           plantingFillInQuery += ", ";
       }
       plantingFillInQuery = plantingFillInQuery.substring( 0, plantingFillInQuery.lastIndexOf( ", " ) );
//       plantingFillInQuery += " FROM " + planName + " AS p, ( " + cropFillInQuery + " ) AS c ";
       plantingFillInQuery += " FROM " + planName + " AS p "; 
       plantingFillInQuery += " LEFT JOIN ( " + cropFillInQuery + " ) AS c ";
//       plantingFillInQuery += " WHERE p.crop_id = c.id ";
       plantingFillInQuery += " ON p.crop_id = c.id ";

       
       String passQuery = "SELECT ";
       for ( String[] s : plantColMap ) {
           if ( s[CALC] == null )
               passQuery += s[PLANT_COL];
           else {
               passQuery += "COALESCE( " + s[PLANT_COL] + ", ";
               passQuery += s[CALC] + " ) AS " + s[PLANT_COL];
           }
           passQuery += ", ";
       }
       passQuery = passQuery.substring( 0, passQuery.lastIndexOf( ", " ) );

       String pass1 = passQuery + " FROM ( " + plantingFillInQuery + " ) ";
       String pass2 = passQuery + " FROM ( " + pass1 + " ) ";
       String pass3 = passQuery + " FROM ( " + pass2 + " ) ";

       return pass3;
       
   }
   
   /**
    * Creates a new HSQLTableModel for the given ResultSet.
    * 
    * @param rs ResultSet to embed in the new HSQLTableModel
    * @return A new HSQLTableModel wrapped around the given ResultSet
    */
   public static TableModel tableResults( HSQLDB dm, ResultSet rs ) {
      try {
         return new HSQLTableModel( dm, rs );
      }
      catch ( SQLException e ) {
         e.printStackTrace();
         return new DefaultTableModel();
      }
   }
   
   public static TableModel tableResults( HSQLDB dm, ResultSet rs, String tableName ) {
      try {
         return new HSQLTableModel( dm, rs, tableName );
      }
      catch ( SQLException e ) {
         e.printStackTrace();
         return new DefaultTableModel();
      }
   }
   
}
