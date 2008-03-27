/* HSQLDB.java - Created: January 16, 2007
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

import CPS.Data.*;
import CPS.Module.CPSDataModel;
import CPS.Module.CPSDataModelConstants;
import CPS.Module.CPSGlobalSettings;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import javax.swing.table.TableModel;
import org.hsqldb.*;
import resultsettablemodel.*;

/**
 *
 * @author Clayton
 */
public class HSQLDB extends CPSDataModel {
   
    private static final boolean DEBUG = true;
    
   private Connection con;
   private String hsqlDriver = "org.hsqldb.jdbcDriver";
   private String dbDir = System.getProperty("user.dir");
   private String dbFile = "CPSdb";
   
   private ResultSet rsListCache = null;
   private ResultSet rsCropCache = null;
   private ResultSet rsPlantCache = null;
   public String state = null;
   
   private HSQLColumnMap columnMap;
   private HSQLQuerier query;
   
   HSQLSettings localSettings;
   
   public HSQLDB() {

       setModuleName( "HSQLDB" );
       setModuleDescription( "A full featured DataModel based on HSQLDB.");
       setModuleVersion( GLOBAL_DEVEL_VERSION );
               
       localSettings = new HSQLSettings();
       
       if ( true || localSettings.getUseGlobalDir() )
           dbDir = CPSGlobalSettings.getDataOutputDir();
       else
           dbDir = localSettings.getCustomOutDir();
       
       initAndUpdateDB();
   }
   
   private void initAndUpdateDB() {
       
      con = HSQLConnect.getConnection( dbDir, dbFile, hsqlDriver );
      boolean newDB = false;
      
      if ( con == null ) { // db DNE
         con = HSQLConnect.createDB( dbDir, dbFile, getModuleVersionAsLongInt() );
         newDB = true;
      }
      /* only needed when we're using a server mode db */
      else if ( HSQLConnect.dbIsEmpty( con ) ) {
         HSQLDBCreator.createTables( con, getModuleVersionAsLongInt() );
         newDB = true;
      }
         
      HSQLUpdate.updateDB( con, getModuleVersionAsLongInt() );
      
      query = new HSQLQuerier( con );
      columnMap = new HSQLColumnMap();

      if ( false && newDB ) {
         this.importCropsAndVarieties( HSQLDBPopulator.loadDefaultCropList( dbDir )
                                                      .getCropsAndVarietiesAsList() );
      } 
       
   }
   
   public synchronized ArrayList<String> getFlatSizeList( String planName ) {
       return getDistinctValsFromCVAndPlan( planName, "flat_size" );
   }
   
   public synchronized ArrayList<String> getFieldNameList( String planName ) {
       // TODO should this query all crop plans, or just one.  Just one for now.
       return HSQLQuerier.getDistinctValuesForColumn( con, planName,  "location" );
   }
   
   public synchronized ArrayList<String> getCropNameList() {
      return HSQLQuerier.getDistinctValuesForColumn( con, "CROPS_VARIETIES", "crop_name" );
   }
   
   public synchronized ArrayList<String> getVarietyNameList( String crop_name, String cropPlan ) {
      return getDistinctValsFromCVAndPlan( cropPlan, "var_name" );
   }
   
   private synchronized ArrayList<String> getDistinctValsFromCVAndPlan( String planName, String column ) {
       ArrayList<String> tables = new ArrayList<String>();
       if ( planName != null )
           tables.add( planName );
       tables.add( "CROPS_VARIETIES" );
       
       return HSQLQuerier.getDistinctValuesForColumn( con, tables, column );
   }
   
   public synchronized ArrayList<String> getFamilyNameList() {
      return HSQLQuerier.getDistinctValuesForColumn( con, "CROPS_VARIETIES", "fam_name" );
   }
   
   public synchronized ArrayList<String> getListOfCropPlans() {
      
       return HSQLQuerier.getDistinctValuesForColumn( con, "CROP_PLANS", "plan_name" );

   }
   
   /* COLUMN NAMES */

   private String getMandatoryColumnNames() {
      return "id, crop_name";
   }
   private String getAbbreviatedCropVarColumnNames( boolean varieties ) {
      return listToCSVString( columnMap.getDefaultCropColumnList() );
//       return getMandatoryColumnNames() + ", " +
//             ( varieties ? "var_name, " : "" ) + "fam_name, maturity";
   }
   private ArrayList<String> getCropVarFilterColumnNames() {
      return columnMap.getCropFilterColumnNames();
//       return getAbbreviatedCropVarColumnNames( true ) + ", keywords, groups";
   }
   
   private String getCropsColumnNames() {
      return  listToCSVString( columnMap.getCropColumnList() );
//       return "*";
   }
   
   private String getVarietiesColumnNames() {
      return getCropsColumnNames();
   }
   
   
   private ArrayList<String> getCropColumnList() {
       return columnMap.getCropColumns();
   }

   public ArrayList<Integer> getCropDefaultProperties() {
      return columnMap.getDefaultCropPropertyList();
   }

   public ArrayList<Integer> getCropDisplayableProperties() {
      return columnMap.getCropPropertyList();
   }
   
   public ArrayList<String> getCropDefaultPropertyNames() {
      return columnMap.getDefaultCropColumnList();
   }
   
   public ArrayList<String> getCropDisplayablePropertyNames() {
      return columnMap.getCropDisplayableColumns();
   }
   public ArrayList<String[]> getCropPrettyNames() {
      return columnMap.getCropPrettyNameMapping(); 
   }
   
   private ArrayList<String[]> getCropInheritanceColumnMapping() {
       return columnMap.getCropInheritanceColumnMapping();
   }
   
   private ArrayList<String[]> getPlantingCropColumnMapping() {
      return columnMap.getPlantingToCropColumnMapping();
//       return plantingCropColumnMapping;
   }

   public ArrayList<Integer> getPlantingDefaultProperties() {
      return columnMap.getDefaultPlantingPropertyList();
   }

   public ArrayList<Integer> getPlantingDisplayableProperties() {
      return columnMap.getPlantingPropertyList();
   }
   
   public ArrayList<String> getPlantingDefaultPropertyNames() {
      return columnMap.getDefaultPlantingColumnList();
   }
   
   public ArrayList<String> getPlantingDisplayablePropertyNames() {
      return columnMap.getPlantingDisplayableColumns();
   }
   
   public ArrayList<String[]> getPlantingPrettyNames() {
      return columnMap.getPlantingPrettyNameMapping(); 
   }
   
   public ArrayList<String[]> getPlantingShortNames() {
      return columnMap.getPlantingShortNameMapping(); 
   }
   
   private String getAbbreviatedCropPlanColumnNames() {
       return listToCSVString( columnMap.getDefaultPlantingColumnList() );
   }
   private ArrayList<String> getCropPlanFilterColumnNames() {
       return columnMap.getPlantingFilterColumnNames();
   }
   
   private String includeMandatoryColumns( String columns ) {
       if ( columns.indexOf( "crop_name" ) == -1 )
           columns = "crop_name, " + columns;
       if ( columns.indexOf( "id" ) == -1 )
           columns = "id, " + columns;
       return columns;
   }
   
   /* END COLUMN NAMES */

   /** Method to cache results of a query and then return those results as a table
    *  @param t Table name
    *  @param col list of columns to select
    *  @param cond conditional statement
    *  @param sort sort statement
    *  @param filter filter statement
    */
   private TableModel cachedListTableQuery( String t, String col, 
                                            String cond, String sort, String filter ) {
      rsListCache = query.storeQuery( t, col, cond, sort, filter );
//      rsListCache.get
      // return query.getCachedResultsAsTable();
      return HSQLQuerier.tableResults( this, rsListCache );
   }
   
   /*
    * CROP LIST METHODS
    */

   public TableModel getCropTable() { 
      return getCropTable( null, null );
   }
   public TableModel getCropTable( String sortCol ) { 
      return getCropTable( sortCol, null );
   }   
   public TableModel getCropTable( int sortProp ) { 
      return getCropTable( sortColumnFromPropertyNum( CPSDataModelConstants.RECORD_TYPE_CROP, sortProp) );
   }
   public TableModel getCropTable( String sortCol, CPSComplexFilter filter ) { 
      return getCropTable( getCropsColumnNames(), sortCol, filter );
   }
   public TableModel getCropTable( int sortProp, CPSComplexFilter filter ) { 
      return getCropTable( sortColumnFromPropertyNum( CPSDataModelConstants.RECORD_TYPE_CROP, sortProp ), filter );
   }
   public TableModel getCropTable( String columns, String sortCol, CPSComplexFilter filter ) { 
      return cachedListTableQuery( "CROPS_VARIETIES", includeMandatoryColumns(columns), "var_name IS NULL", sortCol,
                                   buildGeneralFilterExpression( getCropVarFilterColumnNames(), filter ));
   }
   public TableModel getCropTable( ArrayList<Integer> properties , int sortProp, CPSComplexFilter filter ) { 
      return getCropTable( propertyNumListToColumnString( CPSDataModelConstants.RECORD_TYPE_CROP, properties),
                           sortColumnFromPropertyNum( CPSDataModelConstants.RECORD_TYPE_CROP, sortProp ),
                           filter );
   }
   
   public TableModel getVarietyTable() {
      return getVarietyTable( null, null );
   }
   public TableModel getVarietyTable( String sortCol ) {
      return getVarietyTable( sortCol, null );
   }
   public TableModel getVarietyTable( int sortProp ) { 
      return getVarietyTable( sortColumnFromPropertyNum( CPSDataModelConstants.RECORD_TYPE_CROP, sortProp ));
   }
   public TableModel getVarietyTable( String sortCol, CPSComplexFilter filter ) {
       return getVarietyTable( getVarietiesColumnNames(), sortCol, filter );
   }
   public TableModel getVarietyTable( int sortProp, CPSComplexFilter filter ) { 
      return getVarietyTable( sortColumnFromPropertyNum( CPSDataModelConstants.RECORD_TYPE_CROP, sortProp ), filter );
   }
   public TableModel getVarietyTable( String columns, String sortCol, CPSComplexFilter filter ) {
      return cachedListTableQuery( "CROPS_VARIETIES", 
                                   includeMandatoryColumns(columns),
                                   "var_name IS NOT NULL", 
                                   sortCol,
                                   buildGeneralFilterExpression( getCropVarFilterColumnNames(), filter ));
   }
   public TableModel getVarietyTable( ArrayList<Integer> properties, int sortProp, CPSComplexFilter filter ) { 
      return getVarietyTable( propertyNumListToColumnString( CPSDataModelConstants.RECORD_TYPE_CROP, properties),
                              sortColumnFromPropertyNum( CPSDataModelConstants.RECORD_TYPE_CROP, sortProp ),
                              filter );
   }
   
   public TableModel getCropAndVarietyTable() {
      return getCropAndVarietyTable( null, null );
   }
   public TableModel getCropAndVarietyTable( String sortCol ) {
      return getCropAndVarietyTable( sortCol, null );
   }
   public TableModel getCropAndVarietyTable( int sortProp ) { 
      return getCropAndVarietyTable( sortColumnFromPropertyNum( CPSDataModelConstants.RECORD_TYPE_CROP, sortProp ) );
   }
   public TableModel getCropAndVarietyTable( String sortCol, CPSComplexFilter filter ) {
       return getCropAndVarietyTable( getCropsColumnNames(), sortCol, filter );
   }
   public TableModel getCropAndVarietyTable( int sortProp, CPSComplexFilter filter ) { 
      return getCropAndVarietyTable( sortColumnFromPropertyNum( CPSDataModelConstants.RECORD_TYPE_CROP, sortProp ), filter );
   }
   public TableModel getCropAndVarietyTable( String columns, String sortCol, CPSComplexFilter filter ) {
      return HSQLQuerier.tableResults( this, 
                                       query.submitCalculatedCropAndVarQuery( getCropInheritanceColumnMapping(),
                                                                              includeMandatoryColumns( columns ),
                                                                              sortCol,
                                                                              buildGeneralFilterExpression( getCropVarFilterColumnNames(),
                                                                                                            filter ) ),
                                       "CROPS_VARIETIES" );
//      return cachedListTableQuery( "CROPS_VARIETIES", includeMandatoryColumns(columns), null, sortCol,
//                                   buildGeneralFilterExpression( getCropVarFilterColumnNames(), filter ) );
   }
   public TableModel getCropAndVarietyTable( ArrayList<Integer> properties, int sortProp, CPSComplexFilter filter ) { 
      return getCropAndVarietyTable( propertyNumListToColumnString( CPSDataModelConstants.RECORD_TYPE_CROP, properties),
                                     sortColumnFromPropertyNum( CPSDataModelConstants.RECORD_TYPE_CROP, sortProp ),
                                     filter );
   }
   
   /*
    * CROP PLAN METHODS
    */
   public void createCropPlan( String planName, int year, String desc ) {
      HSQLDBCreator.createCropPlan( con, planName, year, desc );
      updateDataListeners();
   }
   
   public void updateCropPlan( String planName, int year, String desc ) {
      HSQLDBCreator.updateCropPlan( con, planName, year, desc );
      // TODO commented out because I can't imagine a time (yet) when 
      // we'll need to update when a plan changes metadata
      // updateDataListeners();
   }
   
   public void deleteCropPlan( String planName ) {
      HSQLDBCreator.deleteCropPlan( con, planName );
      updateDataListeners();
   }
   
   public int getCropPlanYear( String planName ) {
      return HSQLQuerier.getCropPlanYear( con, planName );
   }
   
   public String getCropPlanDescription( String planName ) {
      return HSQLQuerier.getCropPlanDescription( con, planName );
   }
   
   public TableModel getCropPlan(String plan_name) {
      return getCropPlan( plan_name, null, null );
   }
   
   public TableModel getCropPlan( String plan_name, String sortCol ) {
      return getCropPlan( plan_name, sortCol, null );
   }
   public TableModel getCropPlan( String plan_name, int sortProp ) {
      return getCropPlan( plan_name, sortColumnFromPropertyNum( CPSDataModelConstants.RECORD_TYPE_PLANTING, sortProp ) );
   }

   public TableModel getCropPlan( String plan_name, String sortCol, CPSComplexPlantingFilter filter ) {
      return getCropPlan( plan_name, getAbbreviatedCropPlanColumnNames(), sortCol, filter );
   }
   public TableModel getCropPlan( String plan_name, int sortProp, CPSComplexPlantingFilter filter ) {
      return getCropPlan( plan_name, 
                          sortColumnFromPropertyNum( CPSDataModelConstants.RECORD_TYPE_PLANTING, sortProp ),
                          filter );
   }
   
   public TableModel getCropPlan( String plan_name, String columns, String sortCol, CPSComplexPlantingFilter filter ) {
      return HSQLQuerier.tableResults( this, 
              query.submitCalculatedCropPlanQuery( plan_name, 
                                                   getPlantingCropColumnMapping(),
                                                   getCropInheritanceColumnMapping(),
                                                   includeMandatoryColumns(columns),
                                                   sortCol,
                                                   buildComplexFilterExpression( this.getCropPlanFilterColumnNames(),
                                                                                 filter ) ),
               plan_name );
      
   }
   public TableModel getCropPlan( String plan_name, ArrayList<Integer> properties, int sortProp, CPSComplexPlantingFilter filter ) {
      return getCropPlan( plan_name,
                          propertyNumListToColumnString( CPSDataModelConstants.RECORD_TYPE_PLANTING, properties),
                          sortColumnFromPropertyNum( CPSDataModelConstants.RECORD_TYPE_PLANTING, sortProp ),
                          filter );
   }

      
   public CPSPlanting getSumsForCropPlan( String plan_name, CPSComplexPlantingFilter filter ) {
       ArrayList<String> colsToSum = new ArrayList<String>( 4 );
       colsToSum.add( "beds_to_plant" );
       colsToSum.add( "rowft_to_plant" );
       colsToSum.add( "plants_needed" );
       colsToSum.add( "plants_to_start" );
       colsToSum.add( "flats_needed" ); 
       colsToSum.add( "total_yield" );
       
       try {
           return resultSetAsPlanting( query.submitSummedCropPlanQuery( plan_name,
                                                                        getPlantingCropColumnMapping(),
                                                                        getCropInheritanceColumnMapping(),
                                                                        colsToSum,
                                                                        buildComplexFilterExpression( this.getCropPlanFilterColumnNames(),
                                                                                                      filter )));
       }
       catch ( SQLException e ) {
           e.printStackTrace();
           return new CPSPlanting();
       }
   }
   
   
   /*
    * CROP AND VARIETY METHODS
    */
   
   public CPSPlanting getCommonInfoForPlantings( String plan_name, ArrayList<Integer> plantingIDs ) {
      try {
         rsPlantCache = query.submitCommonInfoQuery( plan_name, columnMap.getPlantingColumns(), plantingIDs );
         
         CPSPlanting p = resultSetAsPlanting( rsPlantCache );
         p.setCommonIDs( plantingIDs );
         return p;
      }
      catch ( SQLException e ) {
         e.printStackTrace();
         return null;
      }
   }
   
   public CPSCrop getCommonInfoForCrops( ArrayList<Integer> cropIDs ) {
      try {
         ArrayList<String> columns = getCropColumnList();
         rsCropCache = query.submitCommonInfoQuery( "CROPS_VARIETIES", columns, cropIDs );
         
         CPSCrop c = resultSetAsCrop( rsCropCache );
         c.setCommonIDs( cropIDs );
         return c;
      }
      catch ( SQLException e ) {
         e.printStackTrace();
         return null;
      }
   }
   
   /* we make the assumption that we're zero-based, ResultSets are not */
   public CPSCrop getCropInfo( int id ) {
      if ( id != -1 )
         try {
            // TODO figure out better way to handle result caching
            // we could just make this a string based query (ie getCropInfoForRow
            // disabling this makes it a simple query on an ID
            if ( false && rsListCache != null ) {
               rsListCache.absolute( id + 1 );
               id = rsListCache.getInt( "id" );
            }
            rsCropCache = query.submitQuery( "CROPS_VARIETIES", "*", "id = " + id );
            return resultSetAsCrop( rsCropCache );
         }
         catch ( SQLException e ) { e.printStackTrace(); }

      return new CPSCrop();
   }
   
   
   public CPSCrop getVarietyInfo( String cropName, String varName ) {
      
      if ( cropName == null || cropName.equalsIgnoreCase("null") || cropName.equals("") )
         return new CPSCrop();
      
      String condExp = "crop_name = " + escapeValue( cropName );
      
      varName = escapeValue( varName );
      
      if ( varName == null || varName.equalsIgnoreCase( "null" ) || varName.equals("") )
         condExp += " AND var_name IS NULL";
      else
         condExp += " AND var_name = " + varName;
      
      try {
         return resultSetAsCrop( query.submitQuery( "CROPS_VARIETIES", "*", condExp ));
      } catch ( SQLException e ) { 
         e.printStackTrace(); 
         return new CPSCrop();
      }
   }
   
   /* Another TODO: we could totally automate this with an iterator:
    *  if ( datum instanceof String )
    *     datum.setDatum( captureString( rs.getString( datum.getColumnName() ))
    *  and so on...
    */  
   // TODO this is where we can implement crop dependencies
   // if a value is blank, leave it blank
   // if a value is null, we can request that info from
   //    for crops: the similar crop (var_name == null)
   //    for varieties: the crop
   private CPSCrop resultSetAsCrop( ResultSet rs ) throws SQLException {
      
      CPSCrop crop = new CPSCrop();
      
      // move to the first (and only) row
      if ( rs.next() ) {
         try {
            
            crop.setID( rs.getInt( "id" ));
            crop.setCropName( captureString( rs.getString( "crop_name" ) ));            
            crop.setVarietyName( captureString( rs.getString( "var_name" ) ));
            crop.setFamilyName( captureString( rs.getString( "fam_name" ) ));
          } catch ( SQLException ignore ) { System.out.println( "WARNING(HSQLDB.java): " + ignore.getMessage() ); }
         
         try {
            
            crop.setMaturityDays( getInt( rs, "maturity" ));
            crop.setMaturityAdjust( getInt( rs, "mat_adjust" ));
//            crop.setSuccessions( rs.getBoolean("successions") );
            
            crop.setTimeToTP( getInt( rs, "time_to_tp" ));
            
            // we have to go through these acrobatics because these fields
            // are inheritable and can be null
            boolean b = rs.getBoolean( "direct_seed" );
            if ( rs.wasNull() )
                 crop.setDirectSeeded( (Boolean) null );
            else
                 crop.setDirectSeeded( b );
            
            b = rs.getBoolean( "frost_hardy" );
            if ( rs.wasNull() )
                 crop.setFrostHardy( (Boolean) null );
            else
                 crop.setFrostHardy( b );
            
          } catch ( SQLException ignore ) { System.out.println( "WARNING(HSQLDB.java): " + ignore.getMessage() ); }
         
         try {

            crop.setRowsPerBed( getInt( rs, "rows_p_bed" ));
            crop.setSpaceInRow( getInt( rs, "space_inrow" ));
            crop.setSpaceBetweenRow( getInt( rs, "space_betrow" ));
            crop.setFlatSize( captureString( rs.getString( "flat_size" )));
            crop.setPlanter( captureString( rs.getString( "planter" )));
            crop.setPlanterSetting( captureString( rs.getString( "planter_setting" )));
          } catch ( SQLException ignore ) { System.out.println( "WARNING(HSQLDB.java): " + ignore.getMessage() ); }
         
         try {
            
            crop.setYieldPerFoot( getFloat( rs, "yield_p_foot" ));
            crop.setYieldNumWeeks( getInt( rs, "yield_num_weeks" ));
            crop.setYieldPerWeek( getInt( rs, "yield_p_week" ));
            crop.setCropYieldUnit( captureString( rs.getString( "crop_unit" )));
            crop.setCropUnitValue( getFloat( rs, "crop_unit_value" ));
          } catch ( SQLException ignore ) { System.out.println( "WARNING(HSQLDB.java): " + ignore.getMessage() ); }
         
         try {

            crop.setGroups( captureString( rs.getString( "groups" ) ));
            crop.setOtherRequirements( captureString( rs.getString( "other_req" ) ));
            crop.setKeywords( captureString( rs.getString( "keywords" ) ));
          } catch ( SQLException ignore ) { System.out.println( "WARNING(HSQLDB.java): " + ignore.getMessage() ); }
         
         try {

            crop.setBotanicalName( captureString( rs.getString( "bot_name" ) ) );
            crop.setCropDescription( captureString( rs.getString("description") ));
            crop.setNotes( captureString( rs.getString( "notes" ) ));
            
          } catch ( SQLException ignore ) { System.out.println( "WARNING(HSQLDB.java): " + ignore.getMessage() ); }
         
         
          /* for varieties, inherit info from their crop, too */
          if ( !crop.getVarietyName().equals( "" ) ) {
              CPSCrop superCrop = getCropInfo( crop.getCropName() );
              crop.inheritFrom( superCrop );            
          }
            
      }
      
      return crop;
   }

   public void updateCrop( CPSCrop crop ) {
      HSQLDBCreator.updateCrop( con, crop );
      updateDataListeners();
   }
   
   public void updateCrops( CPSCrop changes, ArrayList<Integer> ids ) {
      HSQLDBCreator.updateCrops( con, changes, ids );
      updateDataListeners();
   }

   public CPSCrop createCrop(CPSCrop crop) {
      int newID = HSQLDBCreator.insertCrop( con, crop );
      // TODO is this really a good idea?
      updateDataListeners();
      if ( newID == -1 )
         return new CPSCrop();
      else
         return getCropInfo( newID );
   }
   
   public void deleteCrop( int cropID ) {
       HSQLDBCreator.deleteRecord(con, "CROPS_VARIETIES", cropID);   
       updateDataListeners();
   }
   
   public void deletePlanting( String planName, int plantingID ) {
       HSQLDBCreator.deleteRecord( con, planName, plantingID );
       updateDataListeners();
   }
   
   // also called with "empty" or new CPSPlantings, so should handle case where
   // the "cropID" is not valid
   public CPSPlanting createPlanting( String planName, CPSPlanting planting ) {
      int cropID = getVarietyInfo( planting.getCropName(), planting.getVarietyName() ).getID();
      int newID = HSQLDBCreator.insertPlanting( con, planName, planting, cropID );
      updateDataListeners();
      if ( newID == -1 )
         return new CPSPlanting();
      else
         return getPlanting( planName, newID );
   }

   
   /* we make the assumption that we're zero-based, ResultSets are not */
   public CPSPlanting getPlanting( String planName, int id ) {
      
      if ( id != -1 )
         try {
            // TODO figure out better way to handle result caching
            // we could just make this a string based query (ie getCropInfoForRow
            // disabling this makes it a simple query on an ID
            if ( false && rsListCache != null ) {
               rsListCache.absolute( id + 1 );
               id = rsListCache.getInt( "id" );
            }
            rsPlantCache = query.submitQuery( planName, "*", "id = " + id );
            return resultSetAsPlanting( rsPlantCache );
         }
         catch ( SQLException e ) { e.printStackTrace(); }
      
      return new CPSPlanting();
   }

   public void updatePlanting( String planName, CPSPlanting changes ) {
//      CPSCrop c = getVarietyInfo( planting.getCropName(), planting.getVarietyName() );
      ArrayList<Integer> changedID = new ArrayList();
      changedID.add( new Integer( changes.getID() ));
//      HSQLDBCreator.updatePlanting( con, planName, planting, c.getID() );
      updatePlantings( planName, changes, changedID );
      updateDataListeners();
   }
   
   public void updatePlantings( String planName, CPSPlanting changes, ArrayList<Integer> changedIDs ) {
      ArrayList<Integer> cropIDs = new ArrayList();
      for ( Integer i : changedIDs ) {
         CPSPlanting p = getPlanting( planName, i.intValue() );
         if ( changes.getCropNameState().isValid() )
            p.setCropName( changes.getCropName() );
         if ( changes.getVarietyNameState().isValid() )
            p.setVarietyName( changes.getVarietyName() );
         
         int cropID = getVarietyInfo( p.getCropName(), p.getVarietyName() ).getID();
         
         if ( cropID == -1 )
            cropID = getCropInfo( p.getCropName() ).getID();
         
         // TODO error if cropID == -1 again
         
         cropIDs.add( new Integer( cropID  ));
      }
      
      HSQLDBCreator.updatePlantings( con, planName, changes, changedIDs, cropIDs );
      updateDataListeners();
   }
   
   private CPSPlanting resultSetAsPlanting( ResultSet rs ) throws SQLException {
      return resultSetAsPlanting( rs, false );
   }
   private CPSPlanting resultSetAsPlanting( ResultSet rs, boolean summedPlanting ) throws SQLException {
      
      CPSPlanting p = new CPSPlanting();
      
      // move to the first (and only) row
      if ( rs.next() ) {
          // sometimes it's OK for some columns not to exist.  Instead of surrounding each "get..."
          // with a try clause, we'll group this into logical components
         try {

            // Not yet implemented:
            // PROP_CROP_ID, PROP_STATUS, PROP_COMPLETED     

            p.setID( rs.getInt( "id" ));
            p.setCropName( captureString( rs.getString( "crop_name" ) ));            
            p.setVarietyName( captureString( rs.getString( "var_name" ) ));

            p.setLocation( captureString( rs.getString( "location" )));
          } catch ( SQLException ignore ) { 
            if ( !summedPlanting )
                System.out.println( "WARNING(HSQLDB.java): " + ignore.getMessage() ); 
          }
         
         try {           
            p.setDateToPlant( captureDate( rs.getDate( "date_plant" )));
            p.setDateToTP( captureDate( rs.getDate( "date_tp" )));
            p.setDateToHarvest( captureDate( rs.getDate( "date_harvest" )));

            p.setDonePlanting( rs.getBoolean( "done_plant" ) );
            p.setDoneTP( rs.getBoolean( "done_TP" ));
            p.setDoneHarvest( rs.getBoolean( "done_harvest" ));
//          } catch ( SQLException ignore ) {}
          } catch ( SQLException ignore ) { 
            if ( !summedPlanting )
               System.out.println( "WARNING(HSQLDB.java): " + ignore.getMessage() );
         }
         
         try {
             
             // These are all used for the "summed" plantings
            p.setBedsToPlant( getFloat( rs, "beds_to_plant") );
            p.setPlantsNeeded( getInt( rs, "plants_needed") );
            p.setPlantsToStart( getInt( rs, "plants_to_start") );
            p.setRowFtToPlant( getInt( rs, "rowft_to_plant") );
            p.setFlatsNeeded( getFloat( rs, "flats_needed") );
            p.setTotalYield( getFloat( rs, "total_yield" ));
            
          } catch ( SQLException ignore ) { 
            if ( !summedPlanting )
               System.out.println( "WARNING(HSQLDB.java): " + ignore.getMessage() );
         }
         
         try {
            p.setMaturityDays( getInt( rs, "maturity") );
            p.setMatAdjust( getInt( rs, "mat_adjust" ));
            p.setPlantingAdjust( getInt( rs, "planting_adjust" ));
            p.setMiscAdjust( getInt( rs, "misc_adjust" ) );
            
            p.setTimeToTP( getInt( rs, "time_to_tp" ));
            p.setRowsPerBed( getInt( rs, "rows_p_bed" ));
            p.setInRowSpacing( getInt( rs, "inrow_space" ) );
            p.setRowSpacing( getInt( rs, "row_space" ));
            
            p.setFlatSize( captureString( rs.getString( "flat_size" )));
            p.setPlanter( captureString( rs.getString( "planter" )));
            p.setPlanterSetting( captureString( rs.getString( "planter_setting" )));

            // we have to go through these acrobatics because these fields
            // are inheritable and can be null
            boolean b = rs.getBoolean( "direct_seed" );
            if ( rs.wasNull() )
                 p.setDirectSeeded( (Boolean) null );
            else
                 p.setDirectSeeded( b );
            
            b = rs.getBoolean( "frost_hardy" );
            if ( rs.wasNull() )
                 p.setFrostHardy( (Boolean) null );
            else
                 p.setFrostHardy( b );
            
         } catch ( SQLException ignore ) { 
            if ( !summedPlanting )
               System.out.println( "WARNING(HSQLDB.java): " + ignore.getMessage() );
         }
         
         try {

            p.setYieldPerFoot( getFloat( rs, "yield_p_foot" ) ) ;
            p.setYieldNumWeeks( getInt( rs, "yield_num_weeks" ));
            p.setYieldPerWeek( getFloat( rs, "yield_p_week" ));
            p.setCropYieldUnit( captureString( rs.getString( "crop_unit" )));
            p.setCropYieldUnitValue( getFloat( rs, "crop_unit_value" ));

            p.setGroups( captureString( rs.getString( "groups" ) ));
            p.setOtherRequirements( captureString( rs.getString( "other_req" ) ));
            p.setKeywords( captureString( rs.getString( "keywords" ) ));
            p.setNotes( captureString( rs.getString( "notes" ) ));
          } catch ( SQLException ignore ) { 
            if ( !summedPlanting )
               System.out.println( "WARNING(HSQLDB.java): " + ignore.getMessage() );
         }
         
         try {            
            p.setCustomField1( rs.getString( "custom1" ));
            p.setCustomField2( rs.getString( "custom2" ));
            p.setCustomField3( rs.getString( "custom3" ));
            p.setCustomField4( rs.getString( "custom4" ));
            p.setCustomField5( rs.getString( "custom5" ));
          } catch ( SQLException ignore ) { 
            if ( !summedPlanting )
               System.out.println( "WARNING(HSQLDB.java): " + ignore.getMessage() );
         }
         
         try {
            
            /* handle data inheritance */
            // we have to call a get* method before we can call wasNull
            int cid = rs.getInt( "crop_id" );
            if ( ! rs.wasNull() )
                 p.inheritFrom( getCropInfo( cid ) );
            
//          } catch ( SQLException ignore ) {}
          } catch ( SQLException ignore ) { 
            if ( !summedPlanting )
               System.out.println( "WARNING(HSQLDB.java): " + ignore.getMessage() );
         }
         
      }
      
      return p;
   }

   public ArrayList<CPSPlanting> getCropPlanAsList( String planName ) {
       
       ArrayList<CPSPlanting> l = new ArrayList<CPSPlanting>();
       
       // this is excessive but functional; might be better/simple to create a
       // custom query that just returns the id column as a list or something
       TableModel tm = getCropPlan( planName );
       
       for ( int row = 0; row < tm.getRowCount(); row++ ) {
           // the results will contain the crop_id, which the HSQLTableModel will
           // try to hide, so we have to "trick" it into actually giving it to us
           l.add( this.getPlanting( planName, ((Integer) tm.getValueAt( row, -1 ) ).intValue() ) );
       }
      
       return l;
   }
   
   public ArrayList<CPSCrop> getCropsAndVarietiesAsList() { 
   
       ArrayList<CPSCrop> l = new ArrayList<CPSCrop>();
        
       TableModel tm = getCropAndVarietyTable();
       
       for ( int row = 0; row < tm.getRowCount(); row++ ) {
           // the results will contain the crop_id, which the HSQLTableModel will
           // try to hide, so we have to "trick" it into actually giving it to us
           l.add( this.getCropInfo( ((Integer) tm.getValueAt( row, -1 ) ).intValue() ) );
       }
      
       return l;

   }
   
   /** 
    * SQL distinguishes between values that are NULL and those that are just
    * blank.  This method is meant to capture our default values
    * and format them properly so that we might detect proper null values
    * upon read.  We use this to distinguish between null values and just
    * data with no entry.
    * 
    * OK, so what constitutes a null value, blank value, and one that is
    * just a default value?  Let's try to answer that for different data types:
    *   Object: only 'null' objects are SQL NULL
    *   String: a string "null" is read as SQL NULL
    *           anything else is considered a valid entry
    *           Therefore we must decide when to pass back "null" vs "" when
    *             strings w/o entries are encountered.
    *  Integer: a value of -1 is an SQL NULL
    *           anything else is valid
    *  Float:   a value of -1.0 is an SQL NULL, all else valid
    *  Date:    a millisecond date of 0 is an SQL NULL
    *           anything else is valid
    * 
    * @param o The object to test and format.
    * @return A string representing the SQL value of Object o.
    */
   /* Currently handles: null, String, Integer, CPSCrop, CPSPlanting */
   public static String escapeValue( Object o ) {
      // if the datum doesn't exist, use NULL
      if      ( o == null )
         return "NULL";
      // if the datum is a string and is only "", use NULL, else escape it
      else if ( o instanceof String )
         if ( ((String) o).equalsIgnoreCase( "null" ) || ((String) o).equals( "" ) )
            return "NULL";
         else {
            String val = o.toString();
            val = val.replaceAll( "'", "''" );
//            val = val.replaceAll( "\"", "\"\"" );
            return "'" + val + "'";
         }
      // if the datum is an int whose value is -1, use NULL
      else if ( o instanceof Integer && ((Integer) o).intValue() == -1 )
         return "NULL";
      else if ( o instanceof Float && ((Float) o).floatValue() == -1.0 )
         return "NULL";
      else if ( o instanceof java.util.Date || o instanceof java.sql.Date ) {
          // cast to util.Date to cover all of our bases, sql.Date is in scope
          // here, so we must use fully qualified name
          if ( ((java.util.Date) o).getTime() == 0 )
              return "NULL";
          else {
              // TODO figure how to make the date format more flexible
              SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
              return "'" + sdf.format((java.util.Date) o) + "'";
          }
      }
      /* Not entirely sure what these are here for.  Actually, the CPSCrop
       * case is probably for data cascading.  Planting case might not be 
       * appropriate */
      else if ( o instanceof CPSCrop )
         return escapeValue( ((CPSCrop) o).getCropName() );
      else if ( o instanceof CPSPlanting )
         return escapeValue( ((CPSPlanting) o).getCropName() );
      else
         return o.toString();
   }

   public static String escapeTableName( String t ) {       
       // if the "table" is an embedded select statement (enclosed in "()")
       // then don't quote it
       if ( t.trim().matches( "\\(.*\\)" ))
           return t;
       else {
           // if the string contains a quotation mark, escape all quotation marks
           if ( t.matches( ".*\".*" ) )
               t = t.replaceAll( "\\\"", "\"\"" );
           return "\"" + t + "\"";
       }
   }
   
   /** captureValue methods are opposite of escapeValue method.
    *  Takes an SQL value and converts it to the correct "default" or "null"
    *  value for our program.
    */
   public static Date captureDate( Date d ) {
      if ( d == null )
         // PENDING this is totally bogus and needs to have a sane "default" date
         return new Date( 0 );
      else
         return d;
   }
   
   public static float getFloat( ResultSet rs, String columnName ) throws SQLException {
       
       float f = rs.getFloat( columnName );
       
       if ( rs.wasNull() )
           f = -1f;
       return captureFloat( f );
   }
   
   public static float captureFloat( float f ) {
//      if ( f <= 0 )
      if ( f == -1 )
         return (float) -1.0;
      else
         return f;
   }
   
   
   public static int getInt( ResultSet rs, String columnName ) throws SQLException {
//       captureInt( rs.getInt( "time_to_tp" ) );
       int i = rs.getInt( columnName );
       
       if ( rs.wasNull() )
           i = -1;
       return captureInt( i );
   }
   
   public static int captureInt( int i ) {
//      if ( i <= 0 )
      if ( i == -1 )
         return -1;
      else
         return i;
   }
   
   public static String captureString( String s ) {
      if ( s == null || s.equalsIgnoreCase("null") || s.equals("") )
         return null;
      else
         return s;
   }
   
   /**
    * Create a comma delimited string of integers from an ArrayList of Integers.
    * @param ids ArrayList<Integer> to convert
    * @return comma delimited string of integers
    */
   public static String intListToIDString( ArrayList<Integer> ids ) {
      String idString = "";
      for ( Integer i : ids )
         idString += i.toString() + ", ";
      idString = idString.substring( 0, idString.lastIndexOf( ", " ));
      return idString;
   }
   
   public static String listToCSVString( ArrayList l ) {
       String s = "";
       for ( Object o : l )
           s += o.toString() + ", ";
       s = s.substring( 0, s.lastIndexOf( ", " ));
       return s;
   }

   
   private String sortColumnFromPropertyNum( int recordType, int prop ) {
      String sortCol = " asc";
      
      if ( prop < 0 )
         sortCol = " desc";
              
      if      ( recordType == CPSDataModelConstants.RECORD_TYPE_CROP )
         sortCol = columnMap.getCropColumnNameForProperty(prop) + sortCol;
      else if ( recordType == CPSDataModelConstants.RECORD_TYPE_PLANTING )
         sortCol = columnMap.getPlantingColumnNameForProperty(prop) + sortCol;
      else
         sortCol = "";
      
      return sortCol;
   }
   
   public String propNameFromPropNum( int recordType, int propertyNum ) {
      if      ( recordType == CPSDataModelConstants.RECORD_TYPE_CROP )
         return columnMap.getCropColumnNameForProperty( propertyNum );
      else if ( recordType == CPSDataModelConstants.RECORD_TYPE_PLANTING )
         return columnMap.getPlantingColumnNameForProperty( propertyNum );
      else
         return "UnknownProperty";
   }

   public int propNumFromPropName( int recordType, String propertyName ) {
      if      ( recordType == CPSDataModelConstants.RECORD_TYPE_CROP )
         return columnMap.getCropPropertyNumFromName( propertyName );
      else if ( recordType == CPSDataModelConstants.RECORD_TYPE_PLANTING )
         return columnMap.getPlantingPropertyNumFromName( propertyName );
      else
         return 0;
   }

   
   
   public String propertyNumListToColumnString( int recordType, ArrayList<Integer> props ) {
      String cols = "";
      for ( Integer prop : props )
         if ( recordType == CPSDataModelConstants.RECORD_TYPE_CROP )
            cols += columnMap.getCropColumnNameForProperty( prop ) + ", ";
         else if ( recordType == CPSDataModelConstants.RECORD_TYPE_PLANTING )
            cols += columnMap.getPlantingColumnNameForProperty( prop ) + ", ";
      cols = cols.substring( 0, cols.lastIndexOf( ", " ));
      return cols;
   }
   
   
   /* based upon a complex filter including various boolean values and a
    * space delimited uesr input, create an 
    * SQL expression that can be used as a WHERE clause.  Does not include
    * the WHERE keyword
    */
   private String buildComplexFilterExpression( ArrayList<String> colList, CPSComplexPlantingFilter filter ) {
       
       String filterString = buildGeneralFilterExpression( colList, filter );
       
       if ( filterString == null )
           filterString = "";
       
       
       if ( filter != null && filter.isViewLimited() ) {
          
           if ( ! filterString.equals( "" ) )
               filterString += " AND ";
       
           if ( filter.filterOnPlantingMethod() )
//               filterString += "time_to_tp " + (( filter.filterMethodDirectSeed() ) ? " IS " : " IS NOT " ) + " NULL AND ";
               filterString += "direct_seed = " + 
                               (( filter.filterMethodDirectSeed() ) ? " TRUE " : " FALSE OR direct_seed IS NULL " ) + 
                               " AND ";
//              if ( filter.filterMethodDirectSeed() )
//                 filterString += "direct_seed = TRUE AND ";
//              else
//                 filterString += "direct_seed = FALSE OR direct_seed IS NULL AND ";
                 
           
           if ( filter.filterOnPlanting() )
               filterString += "done_plant = " + (( filter.isDonePlanting() ) ? "TRUE " : "FALSE " ) + " AND ";
           if ( filter.filterOnTransplanting() )
               filterString += "done_tp = " + (( filter.isDoneTransplanting() ) ? "TRUE " : "FALSE " ) + " AND ";
           if ( filter.filterOnHarvest() )
               filterString += "done_harvest = " + (( filter.isDoneHarvesting() ) ? "TRUE " : "FALSE " ) + " AND ";
       
           // TODO add date filters
           if ( filter.filterOnPlantingDate() )
               if      ( filter.getPlantingRangeEnd() == null )
                   filterString += "date_plant >= " + escapeValue( filter.getPlantingRangeStart() ) + " AND ";
               else if ( filter.getPlantingRangeStart() == null )
                   filterString += "date_plant <= " + escapeValue( filter.getPlantingRangeEnd() ) + " AND ";
               else // both != null
                   filterString += "date_plant BETWEEN " + escapeValue( filter.getPlantingRangeStart() ) + " AND " +
                                                           escapeValue( filter.getPlantingRangeEnd() ) + " AND ";
           
           if ( filter.filterOnTPDate() )
               if      ( filter.getTpRangeEnd() == null )
                   filterString += "date_tp >= " + escapeValue( filter.getTpRangeStart() ) + " AND ";
               else if ( filter.getTpRangeStart() == null )
                   filterString += "date_tp <= " + escapeValue( filter.getTpRangeEnd() ) + " AND ";
               else // both != null
                   filterString += "date_tp BETWEEN " + escapeValue( filter.getTpRangeStart() ) + " AND " +
                                                        escapeValue( filter.getTpRangeEnd() ) + " AND ";
           
           if ( filter.filterOnHarvestDate() )
               if      ( filter.getHarvestDateEnd() == null )
                   filterString += "date_harvest >= " + escapeValue( filter.getHarvestDateStart() ) + " AND ";
               else if ( filter.getHarvestDateStart() == null )
                   filterString += "date_harvest <= " + escapeValue( filter.getHarvestDateEnd() ) + " AND ";
               else // both != null
                   filterString += "date_harvest BETWEEN " + escapeValue( filter.getHarvestDateStart() ) + " AND " +
                                                             escapeValue( filter.getHarvestDateEnd() ) + " AND ";
           
           filterString = filterString.substring( 0, filterString.lastIndexOf( " AND " ));
       }
           
       System.out.println("DEBUG(HSQLDB): Using filter string: " + filterString );
       
       return filterString;
       
   }
   
   /* based upon a simple filter of space delimited uesr input, create an 
    * SQL expression that can be used as a WHERE clause.  Does not include
    * the WHERE
    */
   private String buildGeneralFilterExpression( ArrayList<String> colList, CPSComplexFilter filter ) {
      
      if ( filter == null )
          return null;
      
      String filterString = filter.getFilterString();
       
      if ( filterString == null || filterString.length() == 0 )
         return null;
      
      // TODO: if filterString not all lower, then omit LOWER below
      filterString = filterString.toLowerCase();
      
      String exp = "";
      // loop over the filter string (seperated by spaces)
      for ( String filt : filterString.split(" ") ) {
          
          String innerExp = "";
                  
          // for cases where just spaces or double spaces are entered
          if ( filt.length() == 0 )
              continue;
          
          // loop over the list of column names (seperated by commas)
          for ( String col : colList ) {
              if ( col.startsWith("date") )
                  col = "MONTHNAME( " + col + " )";
              innerExp += "LOWER( " + col + " ) LIKE " +
                          escapeValue( "%" + filt + "%" ) + " OR ";
          }
          // remove the last " OR " and tack on an " AND "
          exp += " ( " + innerExp.substring( 0, innerExp.lastIndexOf( " OR " )) + " ) AND ";
      }

      // strip off the final " AND "
      if ( exp.length() > 0 )
           exp = exp.substring( 0, exp.lastIndexOf( " AND " ) );
      
       return exp;
       
   }
   
   public int shutdown() {
      try {
          // TODO check to see which which mode we're in: if server, don't do the following
         Statement st = con.createStatement();
         st.execute("SHUTDOWN");
         con.close();
      }
      catch ( SQLException ex ) {
         ex.printStackTrace();
      }
      return 0;
   }

    @Override
    protected void updateDataListeners() {
        super.updateDataListeners();
    }
    
    
    @Override
    public int init() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    protected int saveState() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

}
