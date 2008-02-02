/* CSV.java - Created on April 3, 2007
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

// TODO, if this class is to be fleshed out, be sure to add the "updateDataListeners" calls

package CPS;

import CPS.Data.*;
import CPS.Module.CPSDataModel;
import java.util.ArrayList;
import javax.swing.table.TableModel;

public class CSV extends CPSDataModel {

    
   CSVTableModel ctm;
   
   public CSV( String file ) {
      ctm = new CSVTableModel( file );
   }


   public ArrayList<CPSCrop> exportCropsAndVarieties() {
      ArrayList<CPSCrop> l = new ArrayList<CPSCrop>();
      CPSCrop temp;
       
      System.out.println( "Exporting data." );
        
      for ( int i = 0; i < ctm.getRowCount(); i++ ) {
         l.add( this.getCropInfoForRow(i) );
      }
      
      return l;
   }

   
   public CPSCrop getCropInfoForRow( int selectedRow ) {
        
      CPSCrop temp = new CPSCrop();
         
      //"crop_name","var_name","similar_to","bot_name","fam_name",
      //"Groups","Successions","Desc","Keywords","Fudge","other_req","Notes",
      //"Maturity","mat_adjust","misc_adjust",
      //"seeds_sources","seeds_item_codes","seeds_unit_size"
         
      //temp.setID( (int) ( Math.random() * 10000 ) );
      temp.setID( selectedRow );
      int col = 0;
      temp.setCropName( ctm.getStringAt( selectedRow, col++ ) );
      temp.setVarietyName( ctm.getStringAt( selectedRow, col++ ));

      temp.setSimilarCrop( ctm.getStringAt( selectedRow, col++ ) );
      
      temp.setBotanicalName( ctm.getStringAt( selectedRow, col++ ));
      temp.setFamilyName( ctm.getStringAt( selectedRow, col++ ));

      temp.setGroups( ctm.getStringAt( selectedRow, col++ ));
      // temp.setSuccessions( Boolean.parseBoolean( ctm.getStringAt( selectedRow, col++ )));
      col++; // skip the successions column
      temp.setCropDescription( ctm.getStringAt( selectedRow, col++ ));
      temp.setKeywords( ctm.getStringAt( selectedRow, col++ ));
      col++; // fudge
      temp.setOtherRequirements( ctm.getStringAt( selectedRow, col++ ));
      temp.setNotes( ctm.getStringAt( selectedRow, col++ ));
         
      int i = ctm.getIntAt( selectedRow, col++ );
      if ( i <= 0 )
         i = -1;
      temp.setMaturityDays( i );
      
      return temp;

   }
   
   public CPSCrop getCropInfo( String cropName ) {
   
      CPSCrop temp = new CPSCrop();
      
      if ( ! cropName.equals("") )
         for ( int i = 0; i < ctm.getRowCount(); i ++ ) {
            // match crop_name == cropName and var_name == ""
            if ( ctm.getStringAt( i, 0 ).equalsIgnoreCase( cropName ) &&
                 ctm.getStringAt( i, 1 ).equalsIgnoreCase( "" ) ) {
               temp = getCropInfoForRow( i );
               break;
            }
         }
      
      return temp;
   }
   
   public TableModel getCropAndVarietyList() { return null; }
   
   public ArrayList<String> getListOfCropPlans() { return null; }
   public ArrayList<String> getCropNames() { return null; }
   
   public void createNewCropPlan(String plan_name) {}
   public void retrieveCropPlan(String plan_name) {}
   public void filterCropPlan(String plan_name, String filter) {}
   public TableModel getCropList() { return null; }
   public TableModel getAbbreviatedCropList() { return null; }
   public TableModel getVarietyList() { return null; }
   public TableModel getAbbreviatedVarietyList() { return null; }
   public TableModel getAbbreviatedCropAndVarietyList() { return null; }
   public void shutdown() {}
   
   public void updateCrop(CPSCrop crop) {}
   public CPSCrop createCrop(CPSCrop crop) { return null; }

   public TableModel getCropList(String sortCol) { return null; }
   public TableModel getAbbreviatedCropList(String sortCol) { return null; }
   public TableModel getVarietyList(String sortCol) { return null; }
   public TableModel getAbbreviatedVarietyList(String sortCol) { return null; }
   public TableModel getCropAndVarietyList(String sortCol) { return null; }
   public TableModel getAbbreviatedCropAndVarietyList(String sortCol) { return null; }
   public TableModel getCropList(String sortCol, CPSComplexFilter filter) { return null; }
   public TableModel getAbbreviatedCropList(String sortCol, CPSComplexFilter filter) { return null; }
   public TableModel getVarietyList(String sortCol, CPSComplexFilter filter) { return null; }
   public TableModel getAbbreviatedVarietyList(String sortCol, CPSComplexFilter filter) { return null; }
   public TableModel getCropAndVarietyList(String sortCol, CPSComplexFilter filter) { return null; }
   public TableModel getAbbreviatedCropAndVarietyList(String sortCol, CPSComplexFilter filter) { return null; }

   public CPSCrop getVarietyInfo(String cropName, String varName) { return null; }
   public CPSCrop getCropInfo(int CropID) { return null; }

   public void createCropPlan(String plan_name) {}
   public void updateCropPlan(String plan_name) {}
   public TableModel getCropPlan(String plan_name) { return null; }
   public TableModel getCropPlan(String plan_name, String sortCol) { return null; }
   public TableModel getCropPlan(String plan_name, String sortCol, CPSComplexPlantingFilter filterString) { return null; }
   
   public CPSPlanting createPlanting( String plan_name, CPSPlanting planting ) { return null; };
   public CPSPlanting getPlanting(String planName, int PlantingID) { return null; }
   public void updatePlanting(String planName, CPSPlanting planting) {}
   
   public void deletePlanting( String planting, int plantingID ) {}
   public void deleteCrop( int cropID ) {}
   
   public CPSCrop getCommonInfoForCrops( ArrayList<Integer> cropIDs ) { return null; }
   @Override
   public CPSPlanting getCommonInfoForPlantings( String planName,
                                                 ArrayList<Integer> plantingIDs ) {
      throw new UnsupportedOperationException( "Not supported yet." );
   }
   @Override
   public ArrayList<String> getVarietyNames( String crop_name ) {
      throw new UnsupportedOperationException( "Not supported yet." );
   }
   @Override
   public ArrayList<String> getFamilyNames() {
      throw new UnsupportedOperationException( "Not supported yet." );
   }
   @Override
   public void updateCrops( CPSCrop changes,
                            ArrayList<Integer> cropIDs ) {
      throw new UnsupportedOperationException( "Not supported yet." );
   }
   @Override
   public void updatePlantings( String planName, CPSPlanting changes,
                                ArrayList<Integer> plantingIDs ) {
      throw new UnsupportedOperationException( "Not supported yet." );
   }

    @Override
    public TableModel getCropPlan(String plan_name, String columns, String sortCol, CPSComplexPlantingFilter filterString) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ArrayList<String> getPlantingDisplayableColumns() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ArrayList<String> getCropDisplayableColumns() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ArrayList<String> getPlantingDefaultColumns() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public ArrayList<String[]> getPlantingPrettyNames() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public ArrayList<String> getCropDefaultColumns() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public ArrayList<String[]> getCropPrettyNames() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public TableModel getCropList( String columns, String sortCol, CPSComplexFilter filterString ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public TableModel getAbbreviatedCropList( String columns, String sortCol, CPSComplexFilter filterString ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public TableModel getVarietyList( String columns, String sortCol, CPSComplexFilter filterString ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public TableModel getAbbreviatedVarietyList( String columns, String sortCol, CPSComplexFilter filterString ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public TableModel getCropAndVarietyList( String columns, String sortCol, CPSComplexFilter filterString ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public TableModel getAbbreviatedCropAndVarietyList( String columns, String sortCol, CPSComplexFilter filterString ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public CPSPlanting getSumsForCropPlan( String plan_name, CPSComplexPlantingFilter filterString ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }
   @Override
    public ArrayList<String[]> getPlantingShortNames() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }
   
}
