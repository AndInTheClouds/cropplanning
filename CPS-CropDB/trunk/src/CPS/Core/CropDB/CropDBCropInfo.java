/*
 * CropDBCropInfo.java
 *
 * Created on March 15, 2007, 11:26 AM by Clayton
 */

package CPS.Core.CropDB;

import CPS.Data.*;
import CPS.Module.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.table.TableModel;

public class CropDBCropInfo extends CPSDataModelUser implements ActionListener {
   
   private JPanel mainPanel, cropInfoPanel;
   
   private JPanel buttonPanel;
   private JLabel lblChanges;
   private JButton btnSaveChanges, btnDiscardChanges, btnDelete, btnNew;
   
   private boolean noItemSelected = true;
   
   private JCheckBox chkDS, chkTP, chkSucc;
   private JTextField tfldCropName, tfldVarName, tfldFamName, tfldDesc;
   private JTextField tfldMatDays;
   private JTextArea tareGroups, tareKeywords, tareOtherReq, tareNotes;
   private JLabel lblSimilar;
   private JComboBox cmbxSimilar;
   
   private CPSCrop displayedCrop;
   
   private CropDBUI uiManager;
   
   CropDBCropInfo( CropDBUI ui ) {
      uiManager = ui;
      buildMainPanel();
      buildCropInfoPanel();
      buildButtonPanel();
   }
   
   private void buildMainPanel() {
      /*
       * Create the crop info panel
       */
      mainPanel = new JPanel();
      mainPanel.setBorder( BorderFactory.createTitledBorder( "Crop Info" ));
      mainPanel.setLayout( new BorderLayout() );
      
      JLabel lblNoItemSelected = new JLabel( "Select item from table to display detailed information." );
      
      mainPanel.add( lblNoItemSelected, BorderLayout.CENTER );
      
   }
   
   private void buildButtonPanel() {
      
      
      btnNew = new JButton( "New" );
      btnDelete = new JButton( "Delete");
      btnNew.addActionListener( this );
      btnDelete.addActionListener( this );
      
      lblChanges = new JLabel( "Changes: " ); 
      btnSaveChanges = new JButton( "Save" );
      btnDiscardChanges = new JButton( "Discard" );
      btnSaveChanges.addActionListener( this );
      btnDiscardChanges.addActionListener( this );
      
      buttonPanel = new JPanel();
      buttonPanel.setLayout( new BoxLayout( buttonPanel, BoxLayout.LINE_AXIS ) );
      
      buttonPanel.add( btnNew );
      buttonPanel.add( btnDelete );
      buttonPanel.add( Box.createHorizontalGlue() );
      buttonPanel.add( lblChanges );
      buttonPanel.add( btnSaveChanges );
      buttonPanel.add( btnDiscardChanges );
      
   }
   
   /* this should be renamed */
   public void displayCropInfo() {
      mainPanel.removeAll();
      mainPanel.add( cropInfoPanel, BorderLayout.CENTER );
      mainPanel.add( buttonPanel, BorderLayout.PAGE_END );
      mainPanel.validate();
      // mainPanel.repaint();
   }
   
   public JPanel getJPanel() {
      return mainPanel;
   }

   // add new crop data here
   public void displayCrop( CPSCrop crop ) {
      
      displayedCrop = crop;
      
      if ( noItemSelected ) {
         displayCropInfo();
         noItemSelected = false;
      }
      
      // chkDS.setSelected( displayedCrop.getDS() );
      // chkTP.setSelected( displayedCrop.getTP() ); 
      
      tfldCropName.setText( displayedCrop.getCropName() );
      tfldVarName.setText( displayedCrop.getVarietyName() );
      
      if ( displayedCrop.isCrop() ) {

         lblSimilar.setVisible( true );
         cmbxSimilar.setVisible( true );
         
         if ( displayedCrop.getSimilarCrop().getID() != -1 )
            cmbxSimilar.setSelectedItem( displayedCrop.getSimilarCrop().getCropName() );
         else
            cmbxSimilar.setSelectedItem( "None" );
      } 
      else {
         lblSimilar.setVisible( false );
         cmbxSimilar.setVisible( false ); 
      }
      
      tfldFamName.setText( displayedCrop.getFamilyName() );
      tfldDesc.setText( displayedCrop.getCropDescription() );
      
      if ( displayedCrop.getMaturityDays() > 0 )
         tfldMatDays.setText( "" + displayedCrop.getMaturityDays() );
      else
         tfldMatDays.setText("");
      
      chkSucc.setSelected( displayedCrop.getSuccessions() );
      tareGroups.setText( displayedCrop.getGroups() );
      tareOtherReq.setText( displayedCrop.getOtherRequirments() );
      tareKeywords.setText( displayedCrop.getKeywords() );
      tareNotes.setText( displayedCrop.getNotes() );
      
   }
   
   // add new crop data here
   public CPSCrop asCrop() {
      
      CPSCrop crop = new CPSCrop();
      
      crop.setID( displayedCrop.getID() );
      
      crop.setCropName( tfldCropName.getText() );
      crop.setVarietyName( tfldVarName.getText() );
      crop.setFamilyName( tfldFamName.getText() );
      
      if ( ! tfldMatDays.getText().equals("") )
         crop.setMaturityDays( Integer.parseInt( tfldMatDays.getText() ));
     
      if ( ! cmbxSimilar.getSelectedItem().toString().equalsIgnoreCase("None") )
         crop.setSimilarCrop( dataModel.getCropInfo( cmbxSimilar.getSelectedItem().toString() ));
      
      crop.setCropDescription( tfldDesc.getText() );
      
      crop.setSuccessions( chkSucc.isSelected() );
      
      crop.setGroups( tareGroups.getText() );
      crop.setOtherRequirements( tareOtherReq.getText() );
      crop.setKeywords( tareKeywords.getText() );
      crop.setNotes( tareNotes.getText() );
      
      return crop;
      
   }
   
   // add new crop data here
   public JPanel buildCropInfoPanel() {
      
      cropInfoPanel = new JPanel();
      
      chkDS = new JCheckBox("DS?");
      chkTP = new JCheckBox("TP?");
      chkSucc = new JCheckBox("By succession?");
      tfldCropName = new JTextField(10);
      tfldVarName = new JTextField(10);
      tfldFamName = new JTextField(10);
      tfldDesc = new JTextField(10);
      
      tfldMatDays = new JTextField(5);

      tareGroups = new JTextArea( 3, 10 );
      tareKeywords = new JTextArea( 3, 10 );
      tareOtherReq = new JTextArea( 3, 10 );
      tareNotes = new JTextArea( 5, 20 );
      
      cmbxSimilar = new JComboBox( new String[] {"None"} );
      
      cropInfoPanel.setLayout( new GridBagLayout() );
      GridBagConstraints c = new GridBagConstraints();
      cropInfoPanel.setAlignmentX( JPanel.LEFT_ALIGNMENT );
      cropInfoPanel.setAlignmentY( JPanel.TOP_ALIGNMENT );

      Insets il  = new Insets( 0, 0, 0, 5 );
      Insets ita = new Insets( 0, 2, 2, 5 );

      
      /* the format for these calls is: panel, column, row, component */
      
      CropDBDataTranslata.createLabel(  cropInfoPanel, 0, 0, "Crop Name:" );
      CropDBDataTranslata.addTextField( cropInfoPanel, 1, 0, tfldCropName );

      // TODO maybe: if ( isVariety )
      CropDBDataTranslata.createLabel(  cropInfoPanel, 0, 1, "Variety:" );
      CropDBDataTranslata.addTextField( cropInfoPanel, 1, 1, tfldVarName );

      lblSimilar = new JLabel( "Similar to:" );
      CropDBDataTranslata.addLabel(     cropInfoPanel, 2, 0, lblSimilar );
      CropDBDataTranslata.addComboBox(  cropInfoPanel, 3, 0, cmbxSimilar );
      
      CropDBDataTranslata.createLabel(  cropInfoPanel, 2, 1, "Family:" );
      CropDBDataTranslata.addTextField( cropInfoPanel, 3, 1, tfldFamName );
      
      CropDBDataTranslata.createLabel(  cropInfoPanel, 0, 2, "Description:" );
      CropDBDataTranslata.addTextField( cropInfoPanel, 1, 2, tfldDesc );
      
      CropDBDataTranslata.createLabel(  cropInfoPanel, 0, 3, "Mat. Days:" );
      CropDBDataTranslata.addTextField( cropInfoPanel, 1, 3, tfldMatDays );
      
      // TODO add these to a subpanel, then add subpanel
      CropDBDataTranslata.addCheckBox(  cropInfoPanel, 0, 4, chkDS );
      CropDBDataTranslata.addCheckBox(  cropInfoPanel, 1, 4, chkTP );
      CropDBDataTranslata.addCheckBox(  cropInfoPanel, 2, 4, chkSucc );

      CropDBDataTranslata.createLabel(  cropInfoPanel, 2, 3, "Belongs to Groups:" );
      CropDBDataTranslata.addTextArea(  cropInfoPanel, 3, 3, tareGroups );

      CropDBDataTranslata.createLabel(  cropInfoPanel, 0, 5, "Other Requirements:" );
      CropDBDataTranslata.addTextArea(  cropInfoPanel, 1, 5, tareOtherReq );
      
      CropDBDataTranslata.createLabel(  cropInfoPanel, 2, 5, "Keywords:" );
      CropDBDataTranslata.addTextArea(  cropInfoPanel, 3, 5, tareKeywords );
      
      CropDBDataTranslata.createLabel(  cropInfoPanel, 0, 6, "Notes:" );
      CropDBDataTranslata.addTextArea(  cropInfoPanel, 1, 6, tareNotes );
      
      return cropInfoPanel;
      
   }

   public void setDataSource( CPSDataModel dm ) {
      super.setDataSource(dm);
      
      // now fill in the SimilarTo combobox
      TableModel tm = dataModel.getCropList( "crop_name" );
      int nameCol = 0;
      // find the column storing the crop names
      while ( ! tm.getColumnName( nameCol ).equalsIgnoreCase("crop_name") && 
              nameCol < tm.getColumnCount() ) {
         nameCol++;
      }
      // add the crop names to the combo box
      for ( int i = 0; i < tm.getRowCount(); i++ )
         cmbxSimilar.addItem( tm.getValueAt( i, nameCol ) );
      
   }
   
   public void actionPerformed(ActionEvent actionEvent) {
      String action = actionEvent.getActionCommand();
      
      if      ( action.equalsIgnoreCase( btnSaveChanges.getText() ) ) {
         System.out.println("SAVE");
         if ( isDataAvailable() ) {
            // update only the differences between the originally displayed crop
            // and what is currently displayed
            System.out.println("Calculating crop difference.");
            CPSCrop diff = displayedCrop.diff( this.asCrop() );
            if ( diff.getID() != -1 ) {
               System.out.println("Updating crop " + displayedCrop.getCropName() );
               dataModel.updateCrop( diff );
            }
         }
         uiManager.refreshCropList();
      } 
      else if ( action.equalsIgnoreCase( btnDiscardChanges.getText() )) {
         displayCrop( displayedCrop );
         // reset the "this had changed" bit on all components
      }
      else if ( action.equalsIgnoreCase( btnNew.getText() )) {
         if ( isDataAvailable() )
            displayCrop( dataModel.createCrop( new CPSCrop() ));
         uiManager.refreshCropList();
      }
      else if ( action.equalsIgnoreCase( btnDelete.getText() )) {
         System.err.println("Function not supported.");
      }
      
      // resetColorsCHANGETHISNAME();
   }
   
   public void resetColorsCHANGETHISNAME() {
      Component[] allComponents = cropInfoPanel.getComponents();
      
      for ( Component c : allComponents )
         c.setBackground( Color.WHITE );
   }
   
}
