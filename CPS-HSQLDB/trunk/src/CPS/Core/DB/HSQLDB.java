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
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
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
   private ResultSet rsCropCache = null;
   private ResultSet rsPlantCache = null;
   public String state = null;
   
//   private ArrayList<String> cropColumnList, plantingColumnList;
//   private ArrayList<String[]> plantingCropColumnMapping;
   private HSQLColumnMap columnMap;
   
   private HSQLQuerier query;
   
   public HSQLDB() {

      con = HSQLConnect.getConnection( dbDir, dbFile, hsqlDriver );
      boolean newDB = false;
      
      if ( con == null ) { // db DNE
         con = HSQLConnect.createDB( dbDir, dbFile );
         newDB = true;
      }
      /* only needed when we're using a server mode db */
      else if ( HSQLConnect.dbIsEmpty( con ) ) {
         HSQLDBCreator.createTables( con );
         newDB = true;
      }
         
      query = new HSQLQuerier( con );

      if ( newDB ) {
         this.importCropsAndVarieties( HSQLDBPopulator.loadDefaultCropList( dbDir )
                                                      .exportCropsAndVarieties() );
      }
      
      columnMap = new HSQLColumnMap();
      
//      plantingCropColumnMapping = buildPlantingCropColumnMapping();
//      cropColumnList = buildCropColumnList();
//      buildPlantingColumnList();
      
   }
   
   private synchronized ArrayList<String> getDistinctValuesForColumn( String table, String column ) {
      try {
         String query = "SELECT DISTINCT " + column + " FROM " + table;
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
   
   public synchronized ArrayList<String> getCropNames() {
      return getDistinctValuesForColumn( "CROPS_VARIETIES", "crop_name" );
   }
   
   public synchronized ArrayList<String> getVarietyNames( String crop_name ) {
      // FIXME this queries ALL varieties, but should only query varieties of crop `crop_name`
      return getDistinctValuesForColumn( "CROPS_VARIETIES", "var_name" );
   }
   
   public synchronized ArrayList<String> getFamilyNames() {
      return getDistinctValuesForColumn( "CROPS_VARIETIES", "fam_name" );
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
   
   public ArrayList<String> getCropDefaultColumns() {
      return columnMap.getDefaultCropColumnList();
   }
   
   public ArrayList<String> getCropDisplayableColumns() {
      return columnMap.getCropDisplayableColumns();
   }
   public ArrayList<String[]> getCropPrettyNames() {
      return columnMap.getCropPrettyNameMapping(); 
   }
   
   private ArrayList<String[]> getPlantingCropColumnMapping() {
      return columnMap.getPlantingToCropColumnMapping();
//       return plantingCropColumnMapping;
   }
   
   public ArrayList<String> getPlantingDefaultColumns() {
      return columnMap.getDefaultPlantingColumnList();
   }
   
   public ArrayList<String> getPlantingDisplayableColumns() {
      return columnMap.getPlantingDisplayableColumns();
   }
   
   public ArrayList<String[]> getPlantingPrettyNames() {
      return columnMap.getPlantingPrettyNameMapping(); 
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
      return query.tableResults( rsListCache );
   }
   
   /*
    * CROP LIST METHODS
    */
      
   // TODO all of these non sorting methods can be folded into the sorting methods (ie get() => get(null) )
   public TableModel getAbbreviatedCropList() {
      return getAbbreviatedCropList( null );
   }
   public TableModel getAbbreviatedCropList( String sortCol ) {
      return getAbbreviatedCropList( sortCol, null );
   }
   public TableModel getAbbreviatedCropList( String sortCol, String filterString ) {
       return getAbbreviatedCropList( getAbbreviatedCropVarColumnNames( false ), sortCol, filterString );
   }
   public TableModel getAbbreviatedCropList( String columns, String sortCol, String filterString ) {
      return cachedListTableQuery( "CROPS_VARIETIES", 
                                   includeMandatoryColumns(columns),
                                   "var_name IS NULL",
                                   sortCol,
                                   buildFilterExpression( getCropVarFilterColumnNames(), filterString ));
   }
   
   public TableModel getCropList() { 
      return getCropList( null, null );
   }
   public TableModel getCropList( String sortCol ) { 
      return getCropList( sortCol, null );
   }   
   public TableModel getCropList( String sortCol, String filterString ) { 
      return getCropList( getCropsColumnNames(), sortCol, filterString );
   }
   public TableModel getCropList( String columns, String sortCol, String filterString ) { 
      return cachedListTableQuery( "CROPS_VARIETIES", includeMandatoryColumns(columns), null, sortCol,
                                   buildFilterExpression( getCropVarFilterColumnNames(), filterString ));
   }
   
   public TableModel getVarietyList() {
      return getVarietyList( null, null );
   }
   public TableModel getVarietyList( String sortCol ) {
      return getVarietyList( sortCol, null );
   }
   public TableModel getVarietyList( String sortCol, String filterString ) {
       return getVarietyList( getVarietiesColumnNames(), sortCol, filterString );
   }
   public TableModel getVarietyList( String columns, String sortCol, String filterString ) {
      return cachedListTableQuery( "CROPS_VARIETIES", includeMandatoryColumns(columns), null, sortCol,
                                   buildFilterExpression( getCropVarFilterColumnNames(), filterString ));
   }

   public TableModel getAbbreviatedVarietyList() {
      return getAbbreviatedVarietyList( null, null ); 
   }
   public TableModel getAbbreviatedVarietyList( String sortCol ) {
      return getAbbreviatedVarietyList( sortCol, null ); 
   }
   public TableModel getAbbreviatedVarietyList( String sortCol, String filterString ) {
       return getAbbreviatedCropAndVarietyList( getAbbreviatedCropVarColumnNames( true ), sortCol, filterString );
   }
   public TableModel getAbbreviatedVarietyList( String columns, String sortCol, String filterString ) {
      return cachedListTableQuery( "CROPS_VARIETIES", 
                                   includeMandatoryColumns(columns),
                                   "var_name IS NOT NULL",
                                   sortCol,
                                   buildFilterExpression( getCropVarFilterColumnNames(), filterString )); 
   }
   
   public TableModel getCropAndVarietyList() {
      return getCropAndVarietyList( null, null );
   }
   public TableModel getCropAndVarietyList( String sortCol ) {
      return getCropAndVarietyList( sortCol, null );
   }
   public TableModel getCropAndVarietyList( String sortCol, String filterString ) {
       return getCropAndVarietyList( getCropsColumnNames(), sortCol, filterString );
   }
   public TableModel getCropAndVarietyList( String columns, String sortCol, String filterString ) {
      return cachedListTableQuery( "CROPS_VARIETIES", includeMandatoryColumns(columns), null, sortCol,
                                   buildFilterExpression( getCropVarFilterColumnNames(), filterString ));
   }
   
   public TableModel getAbbreviatedCropAndVarietyList() {
      return getAbbreviatedCropAndVarietyList( null, null );
   }
   public TableModel getAbbreviatedCropAndVarietyList( String sortCol ) {
      return getAbbreviatedCropAndVarietyList( sortCol, null );
   }
   public TableModel getAbbreviatedCropAndVarietyList( String sortCol, String filterString ) {
       return getAbbreviatedCropAndVarietyList( getAbbreviatedCropVarColumnNames( true ), sortCol, filterString );
   }
   public TableModel getAbbreviatedCropAndVarietyList( String columns, String sortCol, String filterString ) {
      return cachedListTableQuery( "CROPS_VARIETIES", includeMandatoryColumns(columns), null, sortCol,
                                   buildFilterExpression( getCropVarFilterColumnNames(), filterString ) );
   }

   /*
    * CROP PLAN METHODS
    */
   public void createCropPlan( String plan_name ) {
      HSQLDBCreator.createCropPlan( con, plan_name );
      updateDataListeners();
   }
   
   // TODO implement updateCropPlan
   public void updateCropPlan( String plan_name ) {
   }
   
   public TableModel getCropPlan(String plan_name) {
      return getCropPlan( plan_name, null, null );
   }
   
   public TableModel getCropPlan( String plan_name, String sortCol ) {
      return getCropPlan( plan_name, sortCol, null );
   }

   public TableModel getCropPlan( String plan_name, String sortCol, String filterString ) {
      return getCropPlan( plan_name, getAbbreviatedCropPlanColumnNames(), sortCol, filterString );
   }
   
   public TableModel getCropPlan( String plan_name, String columns, String sortCol, String filterString ) {
      return HSQLQuerier.tableResults( 
              query.submitCalculatedCropPlanQuery( plan_name, 
                                                   getPlantingCropColumnMapping(),
                                                   includeMandatoryColumns(columns),
                                                   sortCol,
                                                   buildFilterExpression( this.getCropPlanFilterColumnNames(),
                                                                          filterString ) ),
               plan_name );
      
   }

   public CPSPlanting getSumsForCropPlan( String plan_name, String filterString ) {
       ArrayList<String> colsToSum = new ArrayList<String>( 4 );
       colsToSum.add( "beds_to_plant" );
       colsToSum.add( "rowft_to_plant" );
       colsToSum.add( "plants_needed" );
       colsToSum.add( "plants_to_start" );
       colsToSum.add( "flats_needed" ); 
       
       try {
           return resultSetAsPlanting( query.submitSummedCropPlanQuery( plan_name,
                                                                        getPlantingCropColumnMapping(),
                                                                        colsToSum,
                                                                        buildFilterExpression( this.getCropPlanFilterColumnNames(),
                                                                                               filterString )));
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
            crop.setBotanicalName( captureString( rs.getString( "bot_name" ) ) );
            crop.setFamilyName( captureString( rs.getString( "fam_name" ) ));
            crop.setCropDescription( captureString( rs.getString("description") ));
            
            crop.setMaturityDays( captureInt( rs.getInt( "maturity" )));
            crop.setMaturityAdjust( captureInt( rs.getInt( "mat_adjust" )));
//            crop.setSuccessions( rs.getBoolean("successions") );
            
            crop.setGroups( captureString( rs.getString( "groups" ) ));
            crop.setOtherRequirements( captureString( rs.getString( "other_req" ) ));
            crop.setKeywords( captureString( rs.getString( "keywords" ) ));
            crop.setNotes( captureString( rs.getString( "notes" ) ));

            crop.setTimeToTP( captureInt( rs.getInt( "time_to_tp" )));
            crop.setRowsPerBed( captureInt( rs.getInt( "rows_p_bed" )));
            crop.setSpaceInRow( captureInt( rs.getInt( "space_inrow" )));
            crop.setSpaceBetweenRow( captureInt( rs.getInt( "space_betrow" )));
            crop.setFlatSize( captureString( rs.getString( "flat_size" )));
            crop.setPlanter( captureString( rs.getString( "planter" )));
            crop.setPlanterSetting( captureString( rs.getString( "planter_setting" )));
            crop.setYieldPerFoot( captureFloat( rs.getFloat( "yield_p_foot" )));
            crop.setYieldNumWeeks( captureInt( rs.getInt( "yield_num_weeks" )));
            crop.setYieldPerWeek( captureInt( rs.getInt( "yield_p_week" )));
            crop.setCropYieldUnit( captureString( rs.getString( "crop_unit" )));
            crop.setCropUnitValue( captureFloat( rs.getFloat( "crop_unit_value" )));

            
            /* Now handle the data inheritance */
//            crop.setSimilarCrop( captureString( rs.getString("similar_to") ));
//   
//            CPSCrop similarCrop = getCropInfo( crop.getSimilarCrop() );            
//            crop.inheritFrom( similarCrop );
            
            /* for varieties, inherit info from their crop, too */
            if ( ! crop.getVarietyName().equals("") ) {
               CPSCrop superCrop = getCropInfo( crop.getCropName() );
               crop.inheritFrom( superCrop );            
            }
            
            
         }  catch ( SQLException e ) { e.printStackTrace(); }
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
           
            p.setDateToPlant( captureDate( rs.getDate( "date_plant" )));
            p.setDateToTP( captureDate( rs.getDate( "date_tp" )));
            p.setDateToHarvest( captureDate( rs.getDate( "date_harvest" )));

          } catch ( SQLException ignore ) { System.out.println("WARNING(HSQLDB.java): column not found (mess 1)"); }
         
         // these are used for the plan summing function
         try {
            p.setBedsToPlant( captureFloat( rs.getFloat( "beds_to_plant") ));
            p.setPlantsNeeded( captureInt( rs.getInt( "plants_needed") ));
            p.setPlantsToStart( captureInt( rs.getInt( "plants_to_start") ));
            p.setRowFtToPlant( captureInt( rs.getInt( "rowft_to_plant") ));
            p.setFlatsNeeded( captureFloat( rs.getFloat( "flats_needed") ));
            
          } catch ( SQLException ignore ) { System.out.println("WARNING(HSQLDB.java): column not found (mess 2)"); }
         
         try {
            p.setMaturityDays( captureInt( rs.getInt("maturity") ));
            
            p.setGroups( captureString( rs.getString( "groups" ) ));
            p.setOtherRequirements( captureString( rs.getString( "other_req" ) ));
            p.setKeywords( captureString( rs.getString( "keywords" ) ));
            p.setNotes( captureString( rs.getString( "notes" ) ));
            
            p.setMatAdjust( captureInt( rs.getInt( "mat_adjust" )));
            p.setPlantingAdjust( captureInt( rs.getInt( "planting_adjust" )));
            p.setMiscAdjust( captureInt( rs.getInt( "misc_adjust" ) ) );
 
            p.setTimeToTP( captureInt( rs.getInt( "time_to_tp" )));
            p.setRowsPerBed( captureInt( rs.getInt( "rows_p_bed" )));
            p.setInRowSpacing( captureInt( rs.getInt( "inrow_space" ) ) );
            p.setRowSpacing( captureInt( rs.getInt( "row_space" )));
            
            p.setFlatSize( captureString( rs.getString( "flat_size" )));
            p.setPlanter( captureString( rs.getString( "planter" )));
            p.setPlanterSetting( captureString( rs.getString( "planter_setting" )));

            p.setYieldPerFoot( captureFloat( rs.getFloat( "yield_p_foot" ) ) );
            p.setTotalYield( captureFloat( rs.getFloat( "total_yield" )));
            p.setYieldNumWeeks( captureInt( rs.getInt( "yield_num_weeks" )));
            p.setYieldPerWeek( captureFloat( rs.getFloat( "yield_p_week" )));
            p.setCropYieldUnit( captureString( rs.getString( "crop_unit" )));
            p.setCropYieldUnitValue( captureFloat( rs.getFloat( "crop_unit_value" )));

            p.setDonePlanting( rs.getBoolean( "done_plant" ) );
            p.setDoneTP( rs.getBoolean( "done_TP" ));
            p.setDoneHarvest( rs.getBoolean( "done_harvest" ));
            
            p.setCustomField1( rs.getString( "custom1" ));
            p.setCustomField2( rs.getString( "custom2" ));
            p.setCustomField3( rs.getString( "custom3" ));
            p.setCustomField4( rs.getString( "custom4" ));
            p.setCustomField5( rs.getString( "custom5" ));
            
            /* handle data inheritance */
            p.inheritFrom( getCropInfo( rs.getInt( "crop_id" ) ));
//            p.inheritFrom( getCropInfo( p.getCropName() ));
            
          } catch ( SQLException ignore ) { System.out.println("WARNING(HSQLDB.java): column not found (mess 3)"); }
      }
      
      return p;
   }

   
   public ArrayList<CPSCrop> exportCropsAndVarieties() { return null; }
   
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
         else
            return "'" + o.toString() + "'";
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
   
   public static float captureFloat( float f ) {
      if ( f <= 0 )
         return (float) -1.0;
      else
         return f;
   }
   
   public static int captureInt( int i ) {
      if ( i <= 0 )
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
   
   
   
   private String buildFilterExpression( ArrayList<String> colList, String filterString ) {
      if ( filterString == null || filterString.length() == 0 )
         return null;
      
      // TODO: if filterString not all lower, then omit LOWER below
      filterString = filterString.toLowerCase();
      
      String exp = "";
      // loop over the filter string (seperated by spaces)
      for ( String filt : filterString.split(" ") ) {
          
          // loop over the list of column names (seperated by commas)
          for ( String col : colList ) {
              if ( col.startsWith("date") )
                  col = "MONTHNAME( " + col + " )";
              exp += "LOWER( " + col + " ) LIKE " +
                      escapeValue( "%" + filt + "%" ) + " OR ";
          }
          // remove the last " OR " and tack on an " AND "
          exp = " ( " + exp.substring( 0, exp.lastIndexOf( " OR " )) + " ) AND ";
      }
      // strip off the final " AND "
      return exp.substring( 0, exp.lastIndexOf( " AND " ));
   }
   
   public void shutdown() {
      try {
//         Statement st = con.createStatement();
//         st.execute("SHUTDOWN");
         con.close();
      }
      catch ( SQLException ex ) {
         ex.printStackTrace();
      }
   }
   
}
