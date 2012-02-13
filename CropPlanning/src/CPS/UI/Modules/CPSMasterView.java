/* CPSMasterView.java
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

package CPS.UI.Modules;

import CPS.Data.CPSPlanting;
import CPS.Data.CPSTextFilter;
import CPS.Data.CPSRecord;
import CPS.Module.CPSDataModel;
import CPS.Module.CPSDataModelUser;
import CPS.Module.CPSModule;
import CPS.UI.Swing.CPSTable;
import CPS.UI.Swing.CPSSearchField;
import ca.odell.glazedlists.matchers.Matcher;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.matchers.Matchers;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import java.awt.event.FocusListener;
import javax.swing.text.JTextComponent;

/**
 *
 * @author Clayton
 */
public abstract class CPSMasterView extends CPSDataModelUser 
                                    implements ActionListener, 
                                               ListSelectionListener, 
                                               MouseListener, 
                                               TableModelListener,
                                               ListEventListener {

    protected static final String KEY_DISPLAYED_COLUMNS = "DISPLAYED_COLUMNS";
    protected static final String KEY_DISPLAYED_TABLE = "DISPLAYED_TABLE";

    protected static final String STATUS_NO_PLAN_SELECTED = "No plan selected.  Select a plan to display or use \"New Plan\" button to create a new one.";
    protected static final String STATUS_NEW_RECORD = "Editing new record; save changes to add to list.";
    protected static final String STATUS_NO_RECORDS = "No records found.  Use \"New\" button to create some.";
    protected static final String STATUS_FILTER_NO_RECORDS = "Filter returned no records.  Check spelling or be less specific.";

    private JPanel masterListPanel = null;
    protected JPanel jplAboveList = null;
    private JPanel jplList = null;
    protected JPanel jplFilter = null;
    private JPanel jplBelowList  = null;
    
    protected JLabel lblStats;
    
    protected CPSTable masterTable;
    protected JPopupMenu pupColumnList;
    private CPSSearchField tfldFilter = null;

    private JButton btnNewRecord, btnDupeRecord, btnDeleteRecord;
    
    private CPSMasterDetailModule uiManager;

    private int detailRow = -1;
    /// selectedID is the ID of the currently selected record (as opposed to
    // the row number of the selected row.
    private int[] selectedRows = {};
    private ArrayList<Integer> selectedIDs = new ArrayList();
    private ArrayList<ColumnNameStruct> columnList = new ArrayList();


    // lists and such for the GlazedList sorting, filtering and such
    protected BasicEventList<CPSRecord> masterList = new BasicEventList<CPSRecord>();
    protected FilterList<CPSRecord> masterListFiltered = new FilterList<CPSRecord>( masterList );
    protected SortedList<CPSRecord> masterListSorted = new SortedList<CPSRecord>( masterListFiltered, new CPSComparator( CPSPlanting.PROP_ID ));
//    EventSelectionModel<CPSRecord> selectModel = new EventSelectionModel( masterList );
    EventSelectionModel<CPSRecord> selectModel = new EventSelectionModel( masterListFiltered );

    protected CompositeMatcherEditor<CPSRecord> compositeFilter = null;
    protected EventList<MatcherEditor<CPSRecord>> filterList;
    protected CPSTextFilter<CPSRecord> textFilter;


    public CPSMasterView( CPSMasterDetailModule ui ) {
       uiManager = ui;
       
       buildMainPanel( null );
    }
    
    public int init() { return 0; }
    protected int saveState() {
//       getPrefs().put( KEY_DISPLAYED_COLUMNS, getDisplayedColumnListAsString() );
       if ( getDisplayedTableName() == null )
          getPrefs().remove( KEY_DISPLAYED_TABLE );
       else
          getPrefs().put( KEY_DISPLAYED_TABLE, getDisplayedTableName() );
       return 0;
    }
    public int shutdown() {
       saveState();
       try {
          getPrefs().flush();
       } catch ( Exception e ) { e.printStackTrace(); }
       return 0;
    }
    
    protected Preferences getPrefs() { return uiManager.getPrefs(); }
    
    
    protected abstract String getDisplayedTableName();
    protected abstract CPSRecord getDetailsForID( int id );
    protected abstract CPSRecord getDetailsForIDs( List<Integer> ids );
    
    protected CPSRecord getRecordToDisplay() {
       if ( selectedIDs.size() < 1 ) {
          System.err.println("ERROR displaying record: no item selected from list");
          return null;
       }
       else if ( selectedIDs.size() == 1 )
          return getDetailsForID( selectedIDs.get(0).intValue() );
       else
          return getDetailsForIDs( selectedIDs );
    }
    
    protected void updateDetailView() {
        if ( selectedIDs.size() < 1 )
            uiManager.clearDetailDisplay();
        else if ( selectedIDs.size() == 1 )
           uiManager.displayDetail( getDetailsForID( selectedIDs.get(0).intValue() ) );
        else
           uiManager.displayDetail( getDetailsForIDs( selectedIDs ));
        
    }
    
    // pertinent method for TableModelListener
    // what does it listen for?  general changes to the table?
    public void tableChanged(TableModelEvent e) {
        // TODO this is a potential problem; in case table changes in the middle of an edit
        updateDetailView();
    }

    
    /** 
     * This method is called when a row in the table is selected.  It is from the 
     * ListSelectionListener interface.  In it, we retrieve the list of selected rows,
     * query the db on those rows and update the detail view.
     * 
     * @param e The ListSelectionEvent that describes this selection event.
     */
    public void valueChanged( ListSelectionEvent e ) {
        //Ignore extra messages.
        if ( e.getValueIsAdjusting() )
            return;
        ListSelectionModel lsm = (ListSelectionModel) e.getSource();

        if ( ! lsm.isSelectionEmpty() ) {
           
           selectedIDs.clear();
           for ( CPSRecord r : selectModel.getSelected() ) {
              CPSModule.debug( "CPSMasterView", "Record selected: " + r.getID() + " " + r.toString() );
              selectedIDs.add( new Integer( r.getID() ));
           }
           
            updateDetailView();
        }
    }
    
    protected void selectRecord( int id ) {
        setSelectedRowByRecordID( id );
    }

    private void setSelectedRowByRecordID( int recordID ) {
       
       masterTable.clearSelection();

       int i = 0;
       for ( CPSRecord r : masterListSorted ) {
           if ( r.getID() == recordID ) {
               selectModel.setSelectionInterval( i, i );
               break;
           }
           i++; // count rows
       }
          
    }

    private void setUnselectedRowByRecordID( int recordID ) {

       int i = 0;
       for ( CPSRecord r : masterListSorted ) {
           if ( r.getID() == recordID ) {
               CPSModule.debug( "CPSMasterView", "Unselecting row: " + i );
               selectModel.removeIndexInterval( i, i );
               break;
           }
           i++; // count rows
       }

    }

    public JPanel getJPanel() {
        return getMainPanel();
    }
    protected JPanel getMainPanel() {
        if ( masterListPanel == null ) { buildMainPanel( null ); }
        return masterListPanel;
    }   
    protected void initMainPanel( String title ) {
        
        masterListPanel = new JPanel( new BorderLayout() );
        
        if ( title != null )
            masterListPanel.setBorder( BorderFactory.createTitledBorder( title ) );
        else
            masterListPanel.setBorder( BorderFactory.createEtchedBorder() );
       
    }
    protected void buildMainPanel( String title ) {
        
        initMainPanel( title );
        
        masterListPanel.add( getAboveListPanel(), BorderLayout.PAGE_START );
        masterListPanel.add( getListPanel(),      BorderLayout.CENTER );
        masterListPanel.add( getBelowListPanel(), BorderLayout.PAGE_END );
       
    }
    
    protected JPanel getAboveListPanel() {
        if ( jplAboveList == null ) { buildAboveListPanel(); }
        return jplAboveList;
    }
    protected void initAboveListPanel() {
        jplAboveList = new JPanel();
        jplAboveList.setLayout( new BoxLayout( jplAboveList, BoxLayout.LINE_AXIS ) );    
    }
    protected void buildAboveListPanel() { buildAboveListPanel( true ); }
    protected void buildAboveListPanel( boolean init ) {
        if ( init )
            initAboveListPanel();
        jplAboveList.add( Box.createHorizontalGlue() );
        jplAboveList.add( buildFilterComponent() );
    }
    
    protected JPanel getBelowListPanel() {
        if ( jplBelowList == null ) { buildBelowListPanel(); }
        return jplBelowList;       
    }
    protected void initBelowListPanel() {
        jplBelowList = new JPanel();
        jplBelowList.setLayout( new BoxLayout( jplBelowList, BoxLayout.LINE_AXIS ) );
    }
    protected void buildBelowListPanel() { buildBelowListPanel( true ); }
    protected void buildBelowListPanel( boolean init ) {
        
        Insets small = new Insets( 1, 1, 1, 1 );
      
        btnNewRecord = new JButton( "New" );
        btnNewRecord.setActionCommand( "NewRecord" );
        btnDupeRecord = new JButton( "Duplicate" );
        btnDeleteRecord = new JButton( "Delete" );
        btnNewRecord.addActionListener( this );
        btnDupeRecord.addActionListener( this );
        btnDeleteRecord.addActionListener( this );
        btnNewRecord.setMargin( small );
        btnDupeRecord.setMargin( small );
        btnDeleteRecord.setMargin( small );
        
        lblStats = new JLabel();
        lblStats.setText("");
        lblStats.setToolTipText("Statistics regarding displayed or selected rows in the table.");
        
        if ( init )
            initBelowListPanel();
        jplBelowList.add( btnNewRecord );
        jplBelowList.add( btnDupeRecord );
        jplBelowList.add( btnDeleteRecord );
        jplBelowList.add( Box.createHorizontalGlue() );
        jplBelowList.add( lblStats );
      
    }
    
    protected void initFilterPanel() {
        jplFilter = new JPanel();
        jplFilter.setLayout( new BoxLayout( jplFilter, BoxLayout.LINE_AXIS ) );
        jplFilter.add( Box.createHorizontalGlue() );
    }
    protected JPanel buildFilterComponent() {
        return buildFilterComponent(true);
    }
    protected JPanel buildFilterComponent( boolean init ) {

       // build compositeFilter text box
       tfldFilter = new CPSSearchField( "Filter" );
       tfldFilter.setToolTipText( "Only show records matching ALL of these terms.  Leave blank to show everything." );
       tfldFilter.setMaximumSize( tfldFilter.getPreferredSize() );

       // setup the text filtering mechanism
       textFilter = new CPSTextFilter<CPSRecord>( tfldFilter, getTextFilterator() );
       // TODO add call to textFilter.setFields( .... ) to enable advanced field matching

       // setup the list of filters and add an "all" matcher
       filterList = new BasicEventList<MatcherEditor<CPSRecord>>();
       filterList.add( new AbstractMatcherEditor<CPSRecord>() {
                                                                @Override
                                                                public Matcher<CPSRecord> getMatcher() {
                                                                   return Matchers.trueMatcher();
                                                                }
                                                              } );

       // now setup the thing that will match all of the elements of the filter list
       compositeFilter = new CompositeMatcherEditor<CPSRecord>( filterList );
       compositeFilter.setMode( CompositeMatcherEditor.AND );

       // setup the filtered list
//       masterListFiltered = new FilterList<CPSRecord>( masterList );
       masterListFiltered.setMatcherEditor( compositeFilter );

       // add the focus listener so that the the compositeFilter box behaves correctly
       tfldFilter.addFocusListener( new FocusListener() {
                                                           public void focusGained( FocusEvent arg0 ) {
                                                              if ( ! filterList.contains( textFilter ))
                                                                 filterList.add( textFilter );
                                                           }

                                                           public void focusLost( FocusEvent arg0 ) {
                                                              if ( tfldFilter.getText().equals( "" ) ) {
                                                                 filterList.remove( textFilter );
                                                              }
                                                           }
                                                         } );

       masterListFiltered.addListEventListener( this );

       // finally, setup the compositeFilter panel
       if ( init )
           initFilterPanel();
       
       jplFilter.add( tfldFilter );
       return jplFilter;
       
    }
    protected JTextComponent getFilterComponent() { return tfldFilter; }
    
    protected JPanel getListPanel() {
        if ( jplList == null ) { buildListPanel(); }
        return jplList;       
    }
    protected void initListPanel() {
        jplList = new JPanel();
        jplList.setLayout( new BoxLayout( jplList, BoxLayout.LINE_AXIS ) );
    }
    protected void buildListPanel() {
       
       EventList<CPSRecord> el = masterListSorted;

       // in case masterListFiltered hasn't been init;ed yet
//       if ( el == null )
//           el = masterListSorted;
       masterTable = new CPSTable( new EventTableModel<CPSRecord>( el, getTableFormat() ) );
       
       Dimension d = new Dimension( 500, masterTable.getRowHeight() * 10 );
       masterTable.setPreferredScrollableViewportSize( d );
       masterTable.setMaximumSize( d );
       masterTable.getTableHeader().addMouseListener( this );
       
       // Ask to be notified of selection changes (see method: valueChanged)
       selectModel.addListSelectionListener( this );
       masterTable.setSelectionModel( selectModel );

       TableComparatorChooser tcc =
       TableComparatorChooser.install( masterTable, masterListSorted, TableComparatorChooser.SINGLE_COLUMN );

//       for ( int i = 0; i < masterTable.getColumnCount(); i++ ) {
//          tcc.getComparatorsForColumn( i ).clear();
//          tcc.getComparatorsForColumn( i ).add( getTableFormat().getColumnComparator( i ) );
//       }

       initListPanel(); // init listPanel
       jplList.add( new JScrollPane( masterTable ) );

       buildColumnListPopUpMenu();

    }

    protected void clearSelection() {
        selectModel.clearSelection();
    }
    
    protected abstract String getTableStatisticsString();
    protected void updateStatisticsLabel() {
        String stats = getTableStatisticsString();
        
        if ( stats == null || stats.equals("") ) {
            lblStats.setText("");
            lblStats.setToolTipText(null);
        }
        else {
            lblStats.setText( stats );
            lblStats.setToolTipText("Statistics regarding displayed or selected rows in the table.");
        }
        
    }
    
    protected abstract List<String[]> getColumnPrettyNameMap();
    protected abstract List<String> getDisplayableColumnList();
    protected abstract List<Integer> getDefaultDisplayableColumnList();
    protected void buildColumnListPopUpMenu() {
       pupColumnList = new JPopupMenu();
       //  create the empty submenu
       JMenu subMenu = new JMenu( "More ..." );
       
       // grab the column model and column count
       DefaultTableColumnModel model = (DefaultTableColumnModel) masterTable.getColumnModel();
       int nrColumns = model.getColumnCount();

       // generate all of the menu entries
       List<ColumnMenuItem> columnListItems = new ArrayList<ColumnMenuItem>( nrColumns );
       for ( int i = 0; i < nrColumns; i++ )
          columnListItems.add( new ColumnMenuItem( model, i ) );

       // process the entries and add them to the correct menu or submenu
       int j = 0;
       for ( ColumnMenuItem ci : columnListItems ) {

          // if not a default column, then unselect it
          if ( ! ci.isDefaultColumn() )
             ci.doClick();

          // We will only "feature" the first 20 entries, the rest will be buried in a submenu
          if ( j++ < 20 )
             pupColumnList.add( ci );
          else
             subMenu.add( ci );

       }

       if ( subMenu.getItemCount() > 0 )
           pupColumnList.add( subMenu );
       
    }
    
    // This might happen at any time.  So we need to update our view of the data
    // whenever it happens.
   @Override
    public void setDataSource( CPSDataModel dm ) {
       super.setDataSource( dm );
       dataUpdated();
    }
    
    // Reset the table to display new data, encapsulated in a new TableModel
    // We should consider renaming this as the name is rather ambiguous.  
    // Perhaps updateMasterTable or updateMasterListTable?
    private void updateListTable( TableModel tm ) {
        tm.addTableModelListener(this);
        masterTable.setModel(tm);
        masterTable.setColumnNamesAndToolTips( getColumnPrettyNameMap() );
    }
    
    // retrieve fresh data and display it
    protected void updateMasterList() {
        if ( !isDataAvailable() )
            return;

        // obtaining locks on the list before we edit it seems to have
        // fixed sporadic NullPointerExceptions related to concurrency
        masterList.getReadWriteLock().writeLock().lock();
        masterList.clear();
        masterList.addAll( getMasterListData() );
        masterList.getReadWriteLock().writeLock().unlock();

    }
    
    protected void setStatus( String s ) {
        uiManager.setStatus(s);
    }
    
    /// Abstract method to retrieve new data and then hand it off to the
    // JTable to display.  Overriding class should do the fancy work of
    // figuring out which table to query, etc.  Returns a TableModel.
    protected abstract List getMasterListData();
    protected abstract AdvancedTableFormat getTableFormat();
    protected abstract TextFilterator<CPSRecord> getTextFilterator();


    protected void addFilter( MatcherEditor me ) {
       filterList.add(me);
    }
    protected void removeFilter( MatcherEditor me ) {
       filterList.remove( me );
    }
    protected MatcherEditor getFilter() {
       return compositeFilter;
    }
    protected void updateFilter() {}

    protected abstract int getTypeOfDisplayedRecord();

    public void mouseClicked(MouseEvent evt) {
        JTable table = ((JTableHeader) evt.getSource()).getTable();
        TableColumnModel colModel = table.getColumnModel();

        // The index of the column whose header was clicked
        int vColIndex = colModel.getColumnIndexAtX(evt.getX());

        // Return if not clicked on any column header
        if (vColIndex == -1)
            return;
        
       if ( evt.getButton() == evt.BUTTON3 || // RIGHT mouse button on Windows
            evt.getButton() == evt.BUTTON1 && evt.isControlDown() // CTRL + click on Mac
                ) {
           pupColumnList.show( evt.getComponent(),
                               evt.getX(), evt.getY() );
           
        }
        
        updateMasterList();
    }
    public void mouseEntered(MouseEvent mouseEvent) {}
    public void mouseExited(MouseEvent mouseEvent) {}
    public void mousePressed(MouseEvent mouseEvent) {}
    public void mouseReleased(MouseEvent mouseEvent) {}

    
    public abstract CPSRecord createNewRecord();
    public abstract CPSRecord duplicateRecord( int id );
    public abstract void deleteRecord( int id );
    
    public void actionPerformed(ActionEvent actionEvent) {
        String action = actionEvent.getActionCommand();

        CPSModule.debug( "CPSMasterView", "Action performed: " + action);
         
        /*
         * OTHER BUTTONS: New, Dupe, Delete
         */
        // Note the return above, this implies that the following list of if's
        // should really start with an "else"
        if ( action.equalsIgnoreCase( btnNewRecord.getActionCommand() ) ) {
            if ( !isDataAvailable() ) {
                System.err.println("ERROR: cannot create new planting, data unavailable");
                return;
            }
            CPSRecord newRecord = createNewRecord();
            int newID = newRecord.getID();
            uiManager.displayDetail( newRecord );
            uiManager.setDetailViewForEditting();
            setSelectedRowByRecordID( newID );
            setStatus( STATUS_NEW_RECORD );
        }
        else if (action.equalsIgnoreCase(btnDupeRecord.getText())) {
            if (!isDataAvailable()) {
                System.err.println("ERROR: cannot duplicate planting, data unavailable");
                return;
            }
            else if ( selectedIDs.size() != 1 ) {
               // TODO, support mupltiple row duplication
               System.err.println("ERROR: at present, can only duplicate single rows");
               return;
            }
            CPSRecord newRecord = duplicateRecord( selectedIDs.get(0).intValue() );
            int newID = newRecord.getID();
            uiManager.displayDetail( newRecord );
            uiManager.setDetailViewForEditting();
            setSelectedRowByRecordID( newID );
        }
        else if (action.equalsIgnoreCase(btnDeleteRecord.getText())) {
            if (!isDataAvailable()) {
                System.err.println("ERROR: cannon delete entry, data unavailable");
                return;
            }
            else if ( selectedIDs.size() != 1 ) {
               // TODO support mupltiple row duplication
               System.err.println("ERROR: at present, can only delete single rows");
               return;
            }
            deleteRecord( selectedIDs.get(0).intValue() );
        }
        
    }
    
   @Override
   public void dataUpdated() {
      updateMasterList();
   }


   // for addListEventListener
   public void listChanged( ListEvent listChanges ) {

      Object source = listChanges.getSource();

      if ( source == masterListFiltered ) {

         // look through the list of selected elements and remove any that aren't in the current filtered list
//         EventList<CPSRecord> selected = selectModel.getSelected();
//         
//         for ( int i = 0; i < selected.size(); i++ ) {
//            CPSRecord record = selected.get( i );
//            if ( ! masterListFiltered.contains( record ) )
//               setUnselectedRowByRecordID( record.getID() );
//         }

         // no data in table
         if ( masterListFiltered.size() < 1 ) {

//            clearSelection();

            // if no records returned, we can't very well duplicate or delete any
            btnDupeRecord.setEnabled( false );
            btnDeleteRecord.setEnabled( false );

            // if the compositeFilter string is empty, then there really are no records
            // else we're just created an incorrect or too restrictive compositeFilter
            if ( tfldFilter.getText().equals( "" ) ) {
//              tfldFilter.setEnabled(false);
               if ( getDisplayedTableName() == null || getDisplayedTableName().equals( "" ) ) {
                  btnNewRecord.setEnabled( false );
               } else {
                  btnNewRecord.setEnabled( true );
               }
               setStatus( STATUS_NO_RECORDS );
            } else {
               btnNewRecord.setEnabled( false );
               tfldFilter.setEnabled( true );
               setStatus( STATUS_FILTER_NO_RECORDS );
            }
            
         } // table contains data; undo anything we might have just done (in the "if" clause)
         else {
            btnNewRecord.setEnabled( true );
            btnDeleteRecord.setEnabled( true );
            btnDupeRecord.setEnabled( true );
            tfldFilter.setEnabled( true );

            if ( selectedIDs.size() > 0 ) {
               // check that selected items are in table
               // if not, clear selection and display nothing
               setStatus( null );
            } else {
               setStatus( CPSMasterDetailModule.STATUS_NO_SELECTION );
            }
         }
      }
   }
    
   
   private class ColumnNameStruct {
      public String columnName;
      public String prettyName;
      public boolean selected;
      
      public ColumnNameStruct( String name, String pretty, boolean b ) {
         columnName = name;
         prettyName = pretty;
         selected = b;
      }
   }

   
   class ColumnMenuItem extends JCheckBoxMenuItem implements ActionListener {

      private DefaultTableColumnModel columnModel;
      private TableColumn column;
      private boolean defaultColumn = true;

      public ColumnMenuItem( DefaultTableColumnModel columnModel, int columnIndex ) {
         // first arg to super: column name
         super( columnModel.getColumn( columnIndex ).getHeaderValue().toString(), true );
         this.columnModel = columnModel;
         this.column = columnModel.getColumn( columnIndex );
         addActionListener( this );

         // record whether this column should be displayed by default
         if ( getTableFormat() instanceof CPSAdvancedTableFormat )
           defaultColumn = ( (CPSAdvancedTableFormat) getTableFormat() ).isDefaultColumn( columnIndex );

      }

      public void actionPerformed( ActionEvent e ) {
         if ( isSelected() ) {
            columnModel.addColumn( column );
         }
         else {
            columnModel.removeColumn( column );
         }
      }

      /** @return true if this column should be displayed by default */
      public boolean isDefaultColumn() {
         return defaultColumn;
      }

   }


}
