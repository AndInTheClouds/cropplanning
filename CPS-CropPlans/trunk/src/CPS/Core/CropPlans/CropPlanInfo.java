/* CropDBCropInfo.java - created: March 15, 2007
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

package CPS.Core.CropPlans;

import CPS.Data.CPSRecord;
import CPS.UI.Modules.CPSDetailView;
import CPS.UI.Swing.LayoutAssist;
import CPS.Data.CPSPlanting;
import CPS.Module.*;
import CPS.UI.Modules.CPSMasterDetailModule;
import CPS.UI.Swing.*;
import java.util.ArrayList;
import javax.swing.*;

public class CropPlanInfo extends CPSDetailView {
   
   private CPSTextField tfldCropName, tfldVarName;
   private CPSTextField tfldDatePlant, tfldDateTP, tfldDateHarvest;
   private CPSTextField tfldMatDays, tfldMatAdjust, tfldMatAdjustPlanting, tfldMatAdjustMisc;
   private CPSTextField tfldBedsToPlant, tfldRowFtToPlant, tfldPlantsNeeded,
                      tfldPlantsToStart, tfldFlatsNeeded;
   private CPSTextField tfldTimeToTP, tfldRowsPerBed, tfldInRowSpace, tfldBetRowSpace,
                        tfldFlatSize, tfldPlanter, tfldPlanterSetting;
   private CPSTextField tfldYieldPerFt, tfldTotalYield, tfldYieldNumWeeks, tfldYieldPerWeek,
                        tfldCropYieldUnit, tfldCropYieldUnitValue;
   private CPSTextArea tareGroups, tareKeywords, tareOtherReq, tareNotes;
   private CPSTextField tfldLocation, tfldStatus;
   
//   private CPSDatum<String> status;
//   private CPSCheckBox cbCompleted;
   
   private CPSPlanting displayedPlanting;
   
   CropPlanInfo( CPSMasterDetailModule mdm ) {
      super( mdm, "Planting Info" );
   }

   /** this constructor does nothing and is meant for testing purposes only */
   private CropPlanInfo() {}
   
    public CPSRecord getDisplayedRecord() {
       // TODO we should do some double checking in case the displayed info has changed but
       // has not been saved
       
       if ( displayedPlanting == null )
          return new CPSPlanting();
       else
          return displayedPlanting;
    }
   
    public void displayRecord( CPSRecord r ) { displayRecord( (CPSPlanting) r ); }
    public void displayRecord( CPSPlanting p ) {

        displayedPlanting = p;

        if ( ! isRecordDisplayed() ) {
            setRecordDisplayed();
            rebuildMainPanel();
            updateAutocompletionComponents();
        }

        tfldCropName.setInitialText( displayedPlanting.getCropName() );
        tfldVarName.setInitialText(displayedPlanting.getVarietyName(),
                                   displayedPlanting.getVarietyNameState() );

        tfldMatDays.setInitialText( displayedPlanting.getMaturityDaysString(),
                                   displayedPlanting.getMaturityDaysState() );
       tfldMatAdjust.setInitialText( displayedPlanting.getMatAdjustString(),
                                   displayedPlanting.getMatAdjustState() );
//       tfldMatAdjustPlanting.setInitialText( displayedPlanting.getPlantingAdjustString(),
//                                   displayedPlanting.getPlantingAdjustState() );
//       tfldMatAdjustMisc.setInitialText( displayedPlanting.getMiscAdjustString(),
//                                   displayedPlanting.getMiscAdjustState() );
        
        tfldDatePlant.setInitialText(displayedPlanting.getDateToPlantString(),
                                   displayedPlanting.getDateToPlantState() );
        tfldDateTP.setInitialText( displayedPlanting.getDateToTPString(),
                                   displayedPlanting.getDateToTPState() );
        tfldDateHarvest.setInitialText(displayedPlanting.getDateToHarvestString(),
                                   displayedPlanting.getDateToHarvestState());
        
       tfldBedsToPlant.setInitialText( displayedPlanting.getBedsToPlantString(),
                                   displayedPlanting.getBedsToPlantState() );
       tfldRowFtToPlant.setInitialText( displayedPlanting.getRowFtToPlantString(),
                                   displayedPlanting.getRowFtToPlantState() );
       tfldPlantsNeeded.setInitialText( displayedPlanting.getPlantsNeededString(),
                                   displayedPlanting.getPlantsNeededState() );
       tfldPlantsToStart.setInitialText( displayedPlanting.getPlantsToStartString(),
                                         displayedPlanting.getPlantsToStartState() );
       
       
       tfldFlatsNeeded.setInitialText( displayedPlanting.getFlatsNeededString(),
                                   displayedPlanting.getFlatsNeededState() );
       tfldRowsPerBed.setInitialText( displayedPlanting.getRowsPerBedString(), 
                                      displayedPlanting.getRowsPerBedState() );
       tfldTimeToTP.setInitialText( displayedPlanting.getTimeToTPString(),
                                   displayedPlanting.getTimeToTPState() );
       tfldInRowSpace.setInitialText( displayedPlanting.getInRowSpacingString(),
                                   displayedPlanting.getInRowSpacingState() );
       tfldBetRowSpace.setInitialText( displayedPlanting.getRowSpacingString(),
                                   displayedPlanting.getRowSpacingState() );
       tfldFlatSize.setInitialText( displayedPlanting.getFlatSize(),
                                   displayedPlanting.getFlatSizeState() );
       tfldPlanter.setInitialText( displayedPlanting.getPlanter(),
                                   displayedPlanting.getPlanterState() );
//       tfldPlanterSetting.setInitialText( displayedPlanting.getPlanterSetting(),
//                                   displayedPlanting.getPlanterSettingState() );
       
       tfldYieldPerFt.setInitialText( displayedPlanting.getYieldPerFootString(),
                                   displayedPlanting.getYieldPerFootState() );
       tfldTotalYield.setInitialText( displayedPlanting.getTotalYieldString(),
                                   displayedPlanting.getTotalYieldState() );
       tfldYieldNumWeeks.setInitialText( displayedPlanting.getYieldNumWeeksString(),
                                   displayedPlanting.getYieldNumWeeksState() );
       tfldYieldPerWeek.setInitialText( displayedPlanting.getYieldPerWeekString(),
                                   displayedPlanting.getYieldPerWeekState() );
       tfldCropYieldUnit.setInitialText( displayedPlanting.getCropYieldUnit(),
                                   displayedPlanting.getCropYieldUnitState() );
       tfldCropYieldUnitValue.setInitialText( displayedPlanting.getCropYieldUnitValueString(),
                                   displayedPlanting.getCropYieldUnitValueState() );
       
        tareGroups.setInitialText(displayedPlanting.getGroups(),
                                   displayedPlanting.getGroupsState() );
        tareOtherReq.setInitialText(displayedPlanting.getOtherRequirments(),
                                   displayedPlanting.getOtherRequirmentsState() );
        tareKeywords.setInitialText(displayedPlanting.getKeywords(),
                                   displayedPlanting.getKeywordsState());
        tareNotes.setInitialText(displayedPlanting.getNotes(),
                                   displayedPlanting.getNotesState());

        tfldLocation.setInitialText( displayedPlanting.getLocation(),
                                   displayedPlanting.getLocationState() );

//        cbCompleted.setInitialState( displayedPlanting.getDonePlanting() );
        
        // TODO  tfldStatus;
   
        setStatus("");
        if ( ! displayedPlanting.isSingleRecord() ) {
           String ids = "";
           for ( Integer i : displayedPlanting.getCommonIDs() )
              ids += i.toString() + ", ";
           ids = ids.substring( 0, ids.lastIndexOf(", ") );
           setStatus( "Displaying common data for records: " + ids );
       }
        
           
    }

    @Override
    public void setForEditting() {
        tfldCropName.requestFocus();
    }
   
    
   
    protected void saveChangesToRecord() {
       String selectedPlan = getDisplayedTableName();
       CPSPlanting diff = (CPSPlanting) displayedPlanting.diff( this.asPlanting() );
       if ( diff.getID() == -1 ) {
           System.out.println("DEBUG(CPInfo): no changes found (Invalid id) ... not saving;");
          return;
       }
       
       System.out.println("DEBUG(CPInfo): changes found (Valid id) ... attempting to save changes");    
       if ( ! displayedPlanting.isSingleRecord() )
          getDataSource().updatePlantings( selectedPlan, diff, displayedPlanting.getCommonIDs() );
       else
          getDataSource().updatePlanting( selectedPlan, diff );
       
       selectRecordInMasterView( displayedPlanting.getID() );
    }

   /** asPlanting - create a planting data struct to represent this detail view
    * 
    * @return a CPSPlanting object populated to represent the planting which is
    * currently displayed
    */
    public CPSPlanting asPlanting() {
      
       CPSPlanting p = new CPSPlanting();
       boolean ALLOW_NULL = true;
      
       p.setID( displayedPlanting.getID() );
      
       if ( tfldCropName.hasChanged() ) p.setCropName( tfldCropName.getText() );
       if ( tfldVarName.hasChanged() ) p.setVarietyName( tfldVarName.getText(), ALLOW_NULL );
      
       if ( tfldMatDays.hasChanged() ) p.setMaturityDays( tfldMatDays.getText(), ALLOW_NULL );
     
       if ( tfldDatePlant.hasChanged() ) p.setDateToPlant( tfldDatePlant.getText(), ALLOW_NULL );
       if ( tfldDateHarvest.hasChanged() ) p.setDateToHarvest( tfldDateHarvest.getText(), ALLOW_NULL );
 
       if ( tareGroups.hasChanged() ) p.setGroups( tareGroups.getText(), ALLOW_NULL );
       if ( tareOtherReq.hasChanged() ) p.setOtherRequirements( tareOtherReq.getText(), ALLOW_NULL );
       if ( tareKeywords.hasChanged() ) p.setKeywords( tareKeywords.getText(), ALLOW_NULL );
       if ( tareNotes.hasChanged() ) p.setNotes( tareNotes.getText( ) );
         
       if ( tfldDateTP.hasChanged() ) p.setDateToTP( tfldDateTP.getText(), ALLOW_NULL );
       if ( tfldMatAdjust.hasChanged() ) p.setMatAdjust( tfldMatAdjust.getText(), ALLOW_NULL );       
//       if ( tfldMatAdjustPlanting.hasChanged() ) p.setPlantingAdjust( tfldMatAdjustPlanting.getText(), ALLOW_NULL );
//       if ( tfldMatAdjustMisc.hasChanged() ) p.setMiscAdjust( tfldMatAdjustMisc.getText(), ALLOW_NULL );
       if ( tfldBedsToPlant.hasChanged() ) p.setBedsToPlant( tfldBedsToPlant.getText(), ALLOW_NULL );
       if ( tfldRowFtToPlant.hasChanged() ) p.setRowFtToPlant( tfldRowFtToPlant.getText(), ALLOW_NULL );
       if ( tfldPlantsNeeded.hasChanged() ) p.setPlantsNeeded( tfldPlantsNeeded.getText(), ALLOW_NULL );
       if ( tfldPlantsToStart.hasChanged() ) p.setPlantsToStart( tfldPlantsToStart.getText(), ALLOW_NULL );
       if ( tfldFlatsNeeded.hasChanged() ) p.setFlatsNeeded( tfldFlatsNeeded.getText(), ALLOW_NULL );
       if ( tfldTimeToTP.hasChanged() ) p.setTimeToTP( tfldTimeToTP.getText(), ALLOW_NULL );
       if ( tfldRowsPerBed.hasChanged() ) p.setRowsPerBed( tfldRowsPerBed.getText(), ALLOW_NULL );
       if ( tfldInRowSpace.hasChanged() ) p.setInRowSpacing( tfldInRowSpace.getText(), ALLOW_NULL );
       if ( tfldBetRowSpace.hasChanged() ) p.setRowSpacing( tfldBetRowSpace.getText(), ALLOW_NULL );
       if ( tfldFlatSize.hasChanged() ) p.setFlatSize( tfldFlatSize.getText(), ALLOW_NULL );
       if ( tfldPlanter.hasChanged() ) p.setPlanter( tfldPlanter.getText(), ALLOW_NULL );
//       if ( tfldPlanterSetting.hasChanged() ) p.setPlanterSetting( tfldPlanterSetting.getText(), ALLOW_NULL );
       if ( tfldYieldPerFt.hasChanged() ) p.setYieldPerFoot( tfldYieldPerFt.getText(), ALLOW_NULL );
       
       if ( tfldTotalYield.hasChanged() ) p.setTotalYield( tfldTotalYield.getText(), ALLOW_NULL );
       
       if ( tfldYieldNumWeeks.hasChanged() ) p.setYieldNumWeeks( tfldYieldNumWeeks.getText(), ALLOW_NULL );
       if ( tfldYieldPerWeek.hasChanged() ) p.setYieldPerWeek( tfldYieldPerWeek.getText(), ALLOW_NULL );
       if ( tfldCropYieldUnit.hasChanged() ) p.setCropYieldUnit( tfldCropYieldUnit.getText(), ALLOW_NULL );
       if ( tfldCropYieldUnitValue.hasChanged() ) p.setCropYieldUnitValue( tfldCropYieldUnitValue.getText(), ALLOW_NULL );
       if ( tfldLocation.hasChanged() ) p.setLocation( tfldLocation.getText(), ALLOW_NULL );
           
//       if ( cbCompleted.hasChanged() ) p.setDonePlanting( cbCompleted.isSelected() );
       
// TODO:          tfldStatus;
       return p;
      
   }
   
   protected void buildDetailsPanel() {
       
      ArrayList<String> names = new ArrayList<String>();
      if ( isDataAvailable() )
         names = getDataSource().getCropNameList();
      tfldCropName = new CPSTextField( FIELD_LEN_LONG, names, CPSTextField.MATCH_STRICT );
        
        tfldVarName = new CPSTextField( FIELD_LEN_LONG );
        tfldMatDays = new CPSTextField( FIELD_LEN_SHORT );
        tfldDatePlant = new CPSTextField( FIELD_LEN_LONG );
        tfldDateHarvest = new CPSTextField( FIELD_LEN_LONG );
        tareGroups = new CPSTextArea( 3, FIELD_LEN_WAY_LONG );
        tareKeywords = new CPSTextArea( 3, FIELD_LEN_WAY_LONG );
        tareOtherReq = new CPSTextArea( 3, FIELD_LEN_WAY_LONG );
        tareNotes = new CPSTextArea( 5, 40);

      tfldDateTP = new CPSTextField( FIELD_LEN_LONG );
      tfldMatAdjust = new CPSTextField( FIELD_LEN_SHORT );
      tfldMatAdjustPlanting = new CPSTextField( FIELD_LEN_SHORT );
      tfldMatAdjustMisc = new CPSTextField( FIELD_LEN_SHORT );
      tfldBedsToPlant = new CPSTextField( FIELD_LEN_SHORT );
      tfldRowFtToPlant = new CPSTextField( FIELD_LEN_MED );
      tfldPlantsNeeded = new CPSTextField( FIELD_LEN_MED );
      tfldPlantsToStart = new CPSTextField( FIELD_LEN_MED );
      tfldFlatsNeeded = new CPSTextField( FIELD_LEN_SHORT );
      tfldTimeToTP = new CPSTextField( FIELD_LEN_SHORT );
      tfldRowsPerBed = new CPSTextField( FIELD_LEN_SHORT );
      tfldInRowSpace = new CPSTextField( FIELD_LEN_SHORT );
      tfldBetRowSpace = new CPSTextField( FIELD_LEN_SHORT );
      tfldFlatSize = new CPSTextField( FIELD_LEN_MED );
      tfldPlanter = new CPSTextField( FIELD_LEN_MED );
      tfldPlanterSetting = new CPSTextField( FIELD_LEN_SHORT );
      tfldYieldPerFt = new CPSTextField( FIELD_LEN_SHORT );
      tfldTotalYield = new CPSTextField( FIELD_LEN_MED );
      tfldYieldNumWeeks = new CPSTextField( FIELD_LEN_SHORT );
      tfldYieldPerWeek = new CPSTextField( FIELD_LEN_SHORT );
      tfldCropYieldUnit = new CPSTextField( FIELD_LEN_MED );
      tfldCropYieldUnitValue = new CPSTextField( FIELD_LEN_SHORT );
      tfldLocation = new CPSTextField( FIELD_LEN_MED );
      
//      cbCompleted = new CPSCheckBox();
      
// TODO     tfldStatus;
        
      JPanel columnOne = initPanelWithVerticalBoxLayout();
      JPanel columnTwo = initPanelWithVerticalBoxLayout();
      JPanel columnThree = initPanelWithVerticalBoxLayout();
      JPanel columnFour = initPanelWithVerticalBoxLayout();
            
/* the format for these calls is: panel, column, row, component */
      /* ***********************************/
      /* COLUMN ONE (really zero and one)  */
      /* ***********************************/
      JPanel jplName = initPanelWithGridBagLayout();
      jplName.setBorder( BorderFactory.createEmptyBorder() );
      
      LayoutAssist.createLabel(  jplName, 0, 0, "Crop Name:" );
      LayoutAssist.addTextField( jplName, 1, 0, tfldCropName );

      LayoutAssist.createLabel(  jplName, 0, 1, "Variety:" );
      LayoutAssist.addTextField( jplName, 1, 1, tfldVarName );
      
      LayoutAssist.createLabel(  jplName, 0, 2, "<html>Belongs to <br>Groups:</html>" );
      LayoutAssist.addTextArea(  jplName, 1, 2, 1, 2, tareGroups );
      
//      LayoutAssist.createLabel(  jplName, 0, 4, "Completed?" );
//      LayoutAssist.addCheckBox(  jplName, 1, 4, cbCompleted );
      
      LayoutAssist.addPanelToColumn( columnOne, jplName );
      
      
      JPanel jplSucc = initPanelWithGridBagLayout();
      jplSucc.setBorder( BorderFactory.createTitledBorder( "Successions" ) );
      
      LayoutAssist.createLabel(  jplSucc, 0, 0, "Part of a succ.?:" );
      LayoutAssist.addTextField( jplSucc, 1, 0, new JTextField(1) );
      
      LayoutAssist.createLabel(  jplSucc, 0, 1, "Frequency" );
      LayoutAssist.addTextField( jplSucc, 1, 1, new JTextField(1) );
      
      LayoutAssist.createLabel(  jplSucc, 0, 2, "Succession group" );
      LayoutAssist.addTextField( jplSucc, 1, 2, new JTextField(1) );
      
      LayoutAssist.createLabel(  jplSucc, 0, 3, "BTN: Show only this group" );
      LayoutAssist.createLabel(  jplSucc, 0, 4, "BTN: Jump to next/last" );
      
//      LayoutAssist.addPanelToColumn( columnOne, jplSucc );
      LayoutAssist.finishColumn( columnOne );
      
      /* ***********************************/
      /* COLUMN TWO (really two and three) */
      /* ***********************************/
      JPanel jplDates = initPanelWithGridBagLayout();
      jplDates.setBorder( BorderFactory.createEmptyBorder() );
      
      LayoutAssist.createLabel(  jplDates, 2, 0, "Maturity Days:" );
      LayoutAssist.addTextField( jplDates, 3, 0, tfldMatDays );
      
      LayoutAssist.createLabel(  jplDates, 2, 1, "Planting Date:" );
      LayoutAssist.addTextField( jplDates, 3, 1, tfldDatePlant );

      LayoutAssist.createLabel(  jplDates, 2, 2, "TP Date" );
      LayoutAssist.addTextField( jplDates, 3, 2, tfldDateTP);
      
      LayoutAssist.createLabel(  jplDates, 2, 3, "Harvest Date:" );
      LayoutAssist.addTextField( jplDates, 3, 3, tfldDateHarvest );
      
      LayoutAssist.addPanelToColumn( columnTwo, jplDates );
      
      JPanel jplPlanting = initPanelWithGridBagLayout();
      jplPlanting.setBorder( BorderFactory.createTitledBorder( "Planting Info" ) );
      
      LayoutAssist.createLabel(  jplPlanting, 0, 0, "Rows/Bed" );
      LayoutAssist.addTextField( jplPlanting, 1, 0, tfldRowsPerBed );
      
      LayoutAssist.createLabel(  jplPlanting, 0, 1, "Row Spacing" );
      LayoutAssist.addTextField( jplPlanting, 1, 1, tfldBetRowSpace );
      
      LayoutAssist.createLabel(  jplPlanting, 0, 2, "Plant Spacing (in row)" );
      LayoutAssist.addTextField( jplPlanting, 1, 2, tfldInRowSpace );
      
      LayoutAssist.addSeparator( jplPlanting, 0, 3, 2);
      
      LayoutAssist.createLabel(  jplPlanting, 0, 4, "Flat Size" );
      LayoutAssist.addTextField( jplPlanting, 1, 4, tfldFlatSize );
      
      LayoutAssist.createLabel(  jplPlanting, 0, 5, "Weeks to TP" );
      LayoutAssist.addTextField( jplPlanting, 1, 5, tfldTimeToTP );
      
      LayoutAssist.addSeparator( jplPlanting, 0, 6, 2);
      
      LayoutAssist.createLabel(  jplPlanting, 0, 7, "Planter:" );
      LayoutAssist.addTextField( jplPlanting, 1, 7, tfldPlanter );
      
      LayoutAssist.createLabel(  jplPlanting, 0, 8, "Planter Setting" );
      LayoutAssist.addTextField( jplPlanting, 1, 8, tfldPlanterSetting );
      
      LayoutAssist.addPanelToColumn( columnTwo, jplPlanting );
      
      
      JPanel jplAdjust = initPanelWithGridBagLayout();
      jplAdjust.setBorder( BorderFactory.createTitledBorder( "Mat. Adjust" ) );
      
      LayoutAssist.createLabel(  jplAdjust, 0, 0, "General adj." );
      LayoutAssist.addTextField( jplAdjust, 1, 0, tfldMatAdjust );
      
      LayoutAssist.createLabel(  jplAdjust, 0, 1, "Adj. for planting meth." );
      LayoutAssist.addTextField( jplAdjust, 1, 1, tfldMatAdjustPlanting );
      
      LayoutAssist.createLabel(  jplAdjust, 0, 2, "Misc. adj." );
      LayoutAssist.addTextField( jplAdjust, 1, 2, tfldMatAdjustMisc );
      
      // TODO uncomment line below to add Mat Adjust panel back into the mix
//      LayoutAssist.addPanelToColumn( columnTwo, jplAdjust );
      LayoutAssist.finishColumn( columnTwo );
      
      
      /* *************************************/
      /* COLUMN THREE (really four and five) */
      /* *************************************/
      JPanel jplAmount = initPanelWithGridBagLayout();
      jplAmount.setBorder( BorderFactory.createTitledBorder( "Where and How Much" ) );
      
      LayoutAssist.createLabel(  jplAmount, 0, 0, "Location" );
      LayoutAssist.addTextField( jplAmount, 1, 0, tfldLocation);
      
      LayoutAssist.createLabel(  jplAmount, 0, 1, "Beds to Plant" );
      LayoutAssist.addTextField( jplAmount, 1, 1, tfldBedsToPlant );
      
      LayoutAssist.createLabel(  jplAmount, 0, 2, "Row Ft to Plant" );
      LayoutAssist.addTextField( jplAmount, 1, 2, tfldRowFtToPlant );
      
      LayoutAssist.createLabel(  jplAmount, 0, 3, "Plants Needed" );
      LayoutAssist.addTextField( jplAmount, 1, 3, tfldPlantsNeeded );
      
      LayoutAssist.createLabel(  jplAmount, 0, 4, "Plants to Start" );
      LayoutAssist.addTextField( jplAmount, 1, 4, tfldPlantsToStart );
      
      LayoutAssist.createLabel(  jplAmount, 0, 5, "Flats to Start" );
      LayoutAssist.addTextField( jplAmount, 1, 5, tfldFlatsNeeded );
      
      LayoutAssist.createLabel(  jplAmount, 0, 6, "Total Yield" );
      LayoutAssist.addTextField( jplAmount, 1, 6, tfldTotalYield );
      
      LayoutAssist.addPanelToColumn( columnThree, jplAmount );
      
      JPanel jplYield = initPanelWithGridBagLayout();
      jplYield.setBorder( BorderFactory.createTitledBorder( "Yield Info" ) );
      
      /* unit, per foot, weeks, per week, value */
      LayoutAssist.createLabel(  jplYield, 0, 0, "Yield Units" );
      LayoutAssist.addTextField( jplYield, 1, 0, tfldCropYieldUnit );
      
      LayoutAssist.createLabel(  jplYield, 0, 1, "Total Yield/Ft" );
      LayoutAssist.addTextField( jplYield, 1, 1, tfldYieldPerFt );
      
      LayoutAssist.createLabel(  jplYield, 0, 2, "Weeks of Yield" );
      LayoutAssist.addTextField( jplYield, 1, 2, tfldYieldNumWeeks );
      
      LayoutAssist.createLabel(  jplYield, 0, 3, "Yield/Week" );
      LayoutAssist.addTextField( jplYield, 1, 3, tfldYieldPerWeek );
      
      LayoutAssist.createLabel(  jplYield, 0, 4, "Value/Unit" );
      LayoutAssist.addTextField( jplYield, 1, 4, tfldCropYieldUnitValue );
      
      LayoutAssist.addPanelToColumn( columnThree, jplYield );
      LayoutAssist.finishColumn( columnThree );
      
      /* *************************************/
      /* COLUMN FOUR (actually six and seven */
      /* *************************************/
      JPanel jplMisc = initPanelWithGridBagLayout();
      jplMisc.setBorder( BorderFactory.createTitledBorder( "Misc Info" ) );

      LayoutAssist.createLabel(  jplMisc, 0, 0, "<html>Other <br>Requirements:</html>" );
      LayoutAssist.addTextArea(  jplMisc, 1, 0, 1, 1, tareOtherReq );
      
      LayoutAssist.createLabel(  jplMisc, 0, 3, "Keywords:" );
      LayoutAssist.addTextArea(  jplMisc, 1, 3, 1, 1, tareKeywords );
      
      LayoutAssist.addPanelToColumn( columnFour, jplMisc );
      LayoutAssist.finishColumn( columnFour );
      
      /* *************************************/
      /* BOTTOW ROW                          */
      /* *************************************/
      // Notes TextArea is set to span all remaining columns
      // TODO
//      LayoutAssist.createLabel(  jplDetails, 0, 13, "Notes:" );
//      LayoutAssist.addTextArea(  jplDetails, 1, 13, 7, tareNotes );
        
      initDetailsPanel();
      
      LayoutAssist.addSubPanel( jplDetails, 0, 0, 1, 1, columnOne );
      LayoutAssist.addSubPanel( jplDetails, 1, 0, 1, 1, columnTwo );
      LayoutAssist.addSubPanel( jplDetails, 2, 0, 1, 1, columnThree );
      LayoutAssist.addSubPanel( jplDetails, 3, 0, 1, 1, columnFour );

      if ( uiManager != null )
         uiManager.signalUIChanged();
   }

   @Override
   protected void buildBelowDetailsPanel() {
      super.buildBelowDetailsPanel();
      btnSaveChanges.setText( "Save & Recalculate" );
   }
   
   
   
   
   protected void updateAutocompletionComponents() {
       tfldCropName.updateAutocompletionList( getDataSource().getCropNameList(),
                                              CPSTextField.MATCH_PERMISSIVE );
       tfldVarName.updateAutocompletionList( getDataSource().getVarietyNameList( displayedPlanting.getCropName(), getDisplayedTableName() ),
                                             CPSTextField.MATCH_PERMISSIVE );
       tfldLocation.updateAutocompletionList( getDataSource().getFieldNameList( this.getDisplayedTableName() ),
                                              CPSTextField.MATCH_PERMISSIVE );
       tfldFlatSize.updateAutocompletionList( getDataSource().getFlatSizeList( this.getDisplayedTableName() ),
                                              CPSTextField.MATCH_PERMISSIVE );
   }
   
   @Override
   public void dataUpdated() {
      if ( isRecordDisplayed() ) {
         this.displayRecord( getDataSource().getPlanting( getDisplayedTableName(),
                                                          getDisplayedRecord().getID() ) );
         updateAutocompletionComponents();
      }
   }

    @Override
    protected int saveState() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }
    
   
   /* for testing only */
   public static void main( String[] args ) {
      // "test" construtor  
      CropPlanInfo ci = new CropPlanInfo();
      ci.buildDetailsPanel();
      
     JFrame frame = new JFrame();
     frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
     frame.setContentPane( ci.getDetailsPanel() );
     frame.setTitle( "CropPlan Info Layout" );
     frame.pack();
     frame.setVisible(true);
   }
}
