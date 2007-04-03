/*
 * HSQLDB.java
 *
 * Created on January 16, 2007, 1:07 PM
 *
 *
 */

package CPS.Core.DB;

import CPS.Data.CPSCrop;
import CPS.Module.CPSDataModel;
import java.sql.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import org.hsqldb.*;
import resultsettablemodel.*;

/**
 *
 * @author Clayton
 */
public class HSQLDB extends CPSDataModel {
   
   private Connection con;
   private final String hsqlDriver = "org.hsqldb.jdbcDriver";
   private final String dbDir = System.getProperty("user.dir");
   private final String dbFile = "CPSdb";
   
   private ResultSet rsListCache = null;
   private ResultSet rsInfoCache = null;
   public String state = null;
   
   private HSQLQuerier query;
   
   public HSQLDB() {

      con = HSQLConnect.getConnection( dbDir, dbFile, hsqlDriver );
      query = new HSQLQuerier( con );
      
   }
   
   
   public synchronized ArrayList<String> getListOfCropPlans() {
      
      try {
         Statement st = con.createStatement();
         ResultSet rs = st.executeQuery( "SELECT plan_name FROM CROP_PLANS" );
      
         System.out.println("Executed query: " + "SELECT plan_name FROM CROP_PLANS" );
         
         ArrayList<String> l = new ArrayList<String>();
         while ( rs.next() ) {
            System.out.println("Found table entry: " + (String) rs.getObject(1) );
            l.add( (String) rs.getObject(1) );
         }
      
         return l;
      } 
      catch ( SQLException e ) { 
         e.printStackTrace();
         return new ArrayList<String>();
      }
      
   }

   private String getAbbreviatedColumnNames( boolean varieties ) {
      return "id, crop_name, " + ( varieties ? "var_name, " : "" ) + "fam_name, maturity";
   }
   
   private String getCropsColumnNames() {
      return "*";
   }
   
   private String getVarietiesColumnNames() {
      return getCropsColumnNames();
   }
   
   
   /** Method to cache results of a query and then return those results as a table */
   private TableModel cachedListTableQuery( String t, String col, String cond ) {
      rsListCache = query.storeQuery( t, col, cond );
      // return query.getCachedResultsAsTable();
      return query.tableResults( rsListCache );
   }

   /*
    * CROP LIST METHODS
    */
   
   /* TODO create a method that will take a column list, table name and
    * conditional statement and will construct and submit a query, returning
    * a ResultSet
    * TODO create a wrapper method to turn a ResultSet into a TableModel
    */   
   public TableModel getAbbreviatedCropList() {
      return cachedListTableQuery( "CROPS_VARIETIES", 
                                   getAbbreviatedColumnNames( false ),
                                   "var_name IS NULL" );
   }
   
   public TableModel getCropList() { 
      return cachedListTableQuery( "CROPS_VARIETIES", getCropsColumnNames(), null );
   }   

   public TableModel getVarietyList() {
      return cachedListTableQuery( "CROPS_VARIETIES", getVarietiesColumnNames(), null );
   }

   public TableModel getAbbreviatedVarietyList() {
      return cachedListTableQuery( "CROPS_VARIETIES", 
                                   getAbbreviatedColumnNames( true ),
                                   "var_name IS NOT NULL" ); 
   }

   public TableModel getCropAndVarietyList() {
      return cachedListTableQuery( "CROPS_VARIETIES", "*", null );
   }
   
   public TableModel getAbbreviatedCropAndVarietyList() {
      return cachedListTableQuery( "CROPS_VARIETIES", getAbbreviatedColumnNames( true ), null );
   }

   /*
    * CROP PLAN METHODS
    */
   public void createNewCropPlan( String plan_name ) {
      HSQLDBCreator.createCropPlan( con, plan_name );
   }

   public void retrieveCropPlan(String plan_name) {
   }

   public void filterCropPlan(String plan_name, String filter) {
   }

   /* we make the assumption that we're zero-based, ResultSets are not */
   public CPSCrop getCropInfoForRow( int selectedRow ) {
      try {
         rsListCache.absolute( selectedRow + 1 );
         int id = rsListCache.getInt( "id" );
         rsInfoCache = query.submitQuery( "CROPS_VARIETIES", "*", "id = " + id );
         return resultSetAsCrop( rsInfoCache );
      }
      catch ( SQLException e ) { e.printStackTrace(); }
      
      return null;
   }
   
   

   private CPSCrop resultSetAsCrop( ResultSet rs ) throws SQLException {
      
      CPSCrop crop = new CPSCrop();
      
      //move to the first (and only) row
      rs.next();
      
      crop.setID( rs.getInt( "ID" ));
      crop.setCropName( rs.getString( "crop_name" ));
      crop.setFamName( rs.getString( "fam_name" ));
      crop.setVarietyName( rs.getString( "var_name" ));
//      crop.setDS( rs.getBoolean("ds") );
//      crop.setTP( rs.getBoolean("tp") );
      crop.setMaturityDays( rs.getInt( "maturity" ));
      
      return crop;
   }

   public void shutdown() {
      try {
         Statement st = con.createStatement();
         st.execute("SHUTDOWN");
         con.close();
      }
      catch ( SQLException ex ) {
         ex.printStackTrace();
      }
   }

   public void updateCrop( CPSCrop crop ) {
      
      try {
         
         String sql = "UPDATE " + "CROPS_VARIETIES" + " SET ";
         
         sql += "crop_name = " + HSQLDBCreator.escapeString( crop.getCropName() ) + ", ";
         sql += "var_name = " + HSQLDBCreator.escapeString( crop.getVarietyName() ) + ", ";
         sql += "fam_name = " + HSQLDBCreator.escapeString( crop.getFamName() ) + ", ";
         
         sql += "maturity = " + crop.getMaturityDays() + " ";
         
         sql += "WHERE id = " + crop.getID();
         
         System.out.println("Attempting to execute: " + sql );

         
         Statement st = con.createStatement();
         st.executeUpdate( sql );
         // Update JTable?
      }
      catch ( SQLException ex ) { ex.printStackTrace(); }
   }
   
}
