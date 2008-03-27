/* HSQLUpdate.java - created: Feb 11, 2008
 * Copyright (C) 2008 Clayton Carter
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
 * 
 */

package CPS.Core.DB;

import CPS.Module.CPSModule;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class HSQLUpdate {
    
   protected static void updateDB( Connection con, long currentVersion ) {
       
       long previousVersion;
       
       try { 
           // if the previously used version was so old as to not even
           // have the CPS_METADATA table
           if ( ! HSQLConnect.tableExists( con, "CPS_METADATA" ) ) {
               System.out.println("DEBUG(DBUpdater): Metadata table DNE, attempting to create");
               Statement st = con.createStatement();
               st.executeUpdate( HSQLDBCreator.createTableDBMetaData() );
               HSQLDBCreator.setLastUsedVersion( con, 0 );
               st.close();
           }
       
           // get previous version number
           previousVersion = HSQLQuerier.getLastUsedVersion( con );
       
           if ( previousVersion == -1 ) {
               System.err.println("Error updating db: error retrieving previously used program version");
               return;
           }
           
           if ( previousVersion > currentVersion ) {
               System.err.println("Error updating db: previously used program version GREATER than current version" );
               return;
           }
           
           if ( previousVersion == currentVersion ) {
               System.out.println("DEBug(DBUpdater): DB is up to date!");
               // nothing to do
               return;
           }
           
           // version 0.1.2
           if ( previousVersion < CPSModule.versionAsLongInt( 0, 1, 2 ))
               updateForV000_001_002(con);
       
           // version 0.1.3
           if ( previousVersion < CPSModule.versionAsLongInt( 0, 1, 3 ))
               updateForV000_001_003(con);
           
       }
       catch ( Exception ignore ) { ignore.printStackTrace(); }
       
       HSQLDBCreator.setLastUsedVersion( con, currentVersion );
           
   }
   
   
   private static void updateForV000_001_002( Connection con ) throws java.sql.SQLException {
       
       System.out.println( "DEBUG(DBUpdater): Updating DB for changes in version 0.1.2" );
       
       ArrayList<String> plans = HSQLQuerier.getDistinctValuesForColumn( con, "CROP_PLANS", "plan_name" );
       Statement st = con.createStatement();
       String update;
       
       // REMOVE FOREIGN KEY crop_id from crop plans
       // this is heinous and annoying; there is no way to drop an unnamed 
       // FOREIGN KEY, but if we SELECT * INTO then the constraints are not copied
       // so we can work with that.
       for ( String plan : plans ) {
       
           String tempPlan = plan + "temptable";
           // copy the table (removing constraints)
           update  = "SELECT * INTO " + HSQLDB.escapeTableName( tempPlan ) + 
                     " FROM " + HSQLDB.escapeTableName( plan ) + "; ";
           // add the primary key back
           update += "ALTER TABLE " + HSQLDB.escapeTableName( tempPlan ) +
                     " ADD PRIMARY KEY ( id ); ";
           // make it auto-generate
           update += "ALTER TABLE " + HSQLDB.escapeTableName( tempPlan ) +
                     " ALTER COLUMN id INTEGER IDENTITY; ";
           // delete old table
           update += "DROP TABLE " +  HSQLDB.escapeTableName( plan ) + "; ";
           // rename working table to old table
           update += "ALTER TABLE " + HSQLDB.escapeTableName( tempPlan ) + 
                     " RENAME TO " + HSQLDB.escapeTableName( plan ) + "; ";
           System.out.println("DEBUG(DBUpdater): Executing update: " + update );
           st.executeUpdate(update);
           
       }
       
       // DELETE unused table PLANTING_METHODS
       update = "DROP TABLE planting_methods";
       System.out.println( "DEBUG(DBUpdater): Executing update: " + update );
       st.executeUpdate( update );
       
       // DELETE "common_plantings" table and remove it from list of crop plans
       update  = "DROP TABLE common_plantings; ";
       update += "DELETE FROM crop_plans WHERE LOWER( plan_name ) = 'common_plantings'";      
       System.out.println( "DEBUG(DBUpdater): Executing update: " + update );
       st.executeUpdate( update );
       
       st.close();
   }
   
   private static void updateForV000_001_003( Connection con ) throws java.sql.SQLException {
       
       System.out.println( "DEBUG(DBUpdater): Updating DB for changes in version 0.1.3" );
       
       Statement st = con.createStatement();
       String update;
       
       // Add columns to the crop_plans table
       update  = "ALTER TABLE crop_plans ADD COLUMN year        INTEGER; ";
       update += "ALTER TABLE crop_plans ADD COLUMN locked      BOOLEAN DEFAULT false; ";
       update += "ALTER TABLE crop_plans ADD COLUMN description VARCHAR; ";
       update += "UPDATE " + HSQLDB.escapeTableName( "CROP_PLANS" ) +
                 " SET year = " + HSQLDB.escapeValue( GregorianCalendar.getInstance().get( Calendar.YEAR ) ) +
                 " WHERE year IS NULL; ";
       
       System.out.println( "DEBUG(DBUpdater): Executing update: " + update );
       st.executeUpdate( update );
       
       // Add columns to the crops_varieties table
       update  = "ALTER TABLE crops_varieties ADD COLUMN direct_seed BOOLEAN; ";
       update += "ALTER TABLE crops_varieties ADD COLUMN frost_hardy BOOLEAN; ";
       
       System.out.println( "DEBUG(DBUpdater): Executing update: " + update );
       st.executeUpdate( update );
       
       ArrayList<String> plans = HSQLQuerier.getDistinctValuesForColumn( con, "CROP_PLANS", "plan_name" );
       update = "";
       for ( String plan : plans ) {
          if ( plan.matches( "^\\p{Alpha}+$" ) )
             update += "UPDATE " + HSQLDB.escapeTableName( "CROP_PLANS" ) +
                       " SET plan_name = " + HSQLDB.escapeValue( plan.toUpperCase() ) +
                       " WHERE plan_name = " + HSQLDB.escapeValue( plan ) + "; ";
       }
       if ( ! update.equals("")) {
          System.out.println( "DEBUG(DBUpdater): Executing update: " + update );
          st.executeUpdate( update );
       }
          
       plans = HSQLQuerier.getDistinctValuesForColumn( con, "CROP_PLANS", "plan_name" );
       for ( String plan : plans ) {
       
           // Add frost_hardy column to each crop plan (direct_seed is already there)
           update = "ALTER TABLE " + HSQLDB.escapeTableName( plan ) + " ADD COLUMN frost_hardy BOOLEAN; ";
       
           System.out.println("DEBUG(DBUpdater): Executing update: " + update );
           st.executeUpdate(update);
           
       }
       
   }
   
}
