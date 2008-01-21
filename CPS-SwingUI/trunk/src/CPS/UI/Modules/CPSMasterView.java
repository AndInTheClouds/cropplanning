/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package CPS.UI.Modules;

import CPS.Data.CPSRecord;
import CPS.Module.CPSDataModel;
import CPS.Module.CPSDataModelUser;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import sun.awt.DisplayChangedListener;

/**
 *
 * @author Clayton
 */
public abstract class CPSMasterView extends CPSDataModelUser 
                                    implements ActionListener, 
                                               ListSelectionListener, 
                                               MouseListener, 
                                               TableModelListener {

    private JPanel masterListPanel = null;
    protected JPanel jplAboveList = null;
    private JPanel jplList = null;
    private JPanel jplFilter = null;
    private JPanel jplBelowList  = null;
    
    protected JTable masterTable;
    private String sortColumn;
    private JTextField tfldFilter;
    private String filterString;
    
    private JButton btnFilterClear;

    private JButton btnNewRecord, btnDupeRecord, btnDeleteRecord;
    
    private CPSMasterDetailModule uiManager;
    
    private int detailRow = -1;
    /// selectedID is the ID of the currently selected record (as opposed to
    // the row number of the selected row.
    private int[] selectedRows = {};
    private ArrayList<Integer> selectedIDs = new ArrayList();
    
    
    public CPSMasterView( CPSMasterDetailModule ui ) {
       uiManager = ui;
       filterString = "";
       setSortColumn( "" );
       
       buildMainPanel( null );
    }
    
    protected abstract String getDisplayedTableName();
    protected abstract CPSRecord getDetailsForID( int id );
    protected abstract CPSRecord getDetailsForIDs( ArrayList<Integer> ids );
    
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
            return;
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
        if ( !lsm.isSelectionEmpty() ) {
            // retrieve the selected row (for single row selection mode)
            // selectedRow = lsm.getMinSelectionIndex();
           
           // retrieve the selected rows (for multi row selection mode)
           selectedRows = masterTable.getSelectedRows();
           selectedIDs.clear();
           for ( int i : selectedRows)
            selectedIDs.add( new Integer( getRecordIDForRow( i )) );
           
           // for single row selection mode
//           selectedIDs = Integer.parseInt( masterTable.getValueAt( selectedRow, -1 ).toString() );
           
            updateDetailView();
        }
    }

    private int getRecordIDForRow( int row ) {
       return new Integer( masterTable.getValueAt( row, -1 ).toString() ).intValue();
    }
           
    private void setSelectedRowByRecordID( int recordID ) {
       
       masterTable.clearSelection();
       
       int numRows = masterTable.getRowCount();
       for ( int row = 0; row < numRows; row++ ) {
          if ( getRecordIDForRow( row ) == recordID ) {
             masterTable.setRowSelectionInterval( row, row );
             break;
          }
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
        
        if ( init )
            initBelowListPanel();
        jplBelowList.add( btnNewRecord );
        jplBelowList.add( btnDupeRecord );
        jplBelowList.add( btnDeleteRecord );
        jplBelowList.add( Box.createHorizontalGlue() );
      
    }
    
    protected JPanel buildFilterComponent() {
        
        tfldFilter = new JTextField(10);
        tfldFilter.setMaximumSize(tfldFilter.getPreferredSize());
        // HACK! TODO, improve this; possibly by implementing a delay?
        // from: http://www.exampledepot.com/egs/javax.swing.text/ChangeEvt.html
        tfldFilter.getDocument().addDocumentListener( new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
             filterString = tfldFilter.getText(); updateMasterList(); }
            public void removeUpdate(DocumentEvent e) {
             filterString = tfldFilter.getText(); updateMasterList(); }
            public void changedUpdate(DocumentEvent evt) {}
       });
       
       btnFilterClear = new JButton( "X" );
       btnFilterClear.setMargin( new Insets( 0, 0, 0, 0 ));
       btnFilterClear.setContentAreaFilled(false);
       btnFilterClear.setFocusPainted(false);
       btnFilterClear.setBorderPainted(false);
       btnFilterClear.addActionListener(this);
       
       jplFilter = new JPanel();
       jplFilter.setLayout( new BoxLayout( jplFilter, BoxLayout.LINE_AXIS ) );
       jplFilter.add( tfldFilter );
       jplFilter.add( btnFilterClear );
       return jplFilter;
       
    }
    
    protected JPanel getListPanel() {
        if ( jplList == null ) { buildListPanel(); }
        return jplList;       
    }
    protected void initListPanel() {
        jplList = new JPanel();
        jplList.setLayout( new BoxLayout( jplList, BoxLayout.LINE_AXIS ) );
    }
    protected void buildListPanel() {
       
       masterTable = new JTable();
       Dimension d = new Dimension( 500, masterTable.getRowHeight() * 10 );
       masterTable.setPreferredScrollableViewportSize( d );
       masterTable.setMaximumSize( d );
       masterTable.getTableHeader().addMouseListener( this );
       
       /* setup selection parameters */
       // enable row selection, disable column selection (default)
       masterTable.setColumnSelectionAllowed( false );
       masterTable.setRowSelectionAllowed( true );
       // allow multiple rows to be selected
       masterTable.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
       // Ask to be notified of selection changes (see method: valueChanged)
       masterTable.getSelectionModel().addListSelectionListener( this );
       
       initListPanel(); // init listPanel
       jplList.add( new JScrollPane( masterTable ) );
       
    }
    
    
    // This might happen at any time.  So we need to update our view of the data
    // whenever it happens.
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
    }
    // retrieve fresh data and display it
    protected void updateMasterList() {
        if ( !isDataAvailable() )
            return;

        updateListTable( getMasterListData() );
        
    }
    /// Abstract method to retrieve new data and then hand it off to the
    // JTable to display.  Overriding class should do the fancy work of
    // figuring out which table to query, etc.  Returns a TableModel.
    protected abstract TableModel getMasterListData();
    protected void setSortColumn( String s ) { 
       if ( s == null ) s = ""; 
       sortColumn = s.toLowerCase(); 
    }
    protected String getSortColumn() { return sortColumn; }
    protected String getFilterString() { return filterString; }
    
    public void mouseClicked(MouseEvent evt) {
        JTable table = ((JTableHeader) evt.getSource()).getTable();
        TableColumnModel colModel = table.getColumnModel();

        // TODO implement multiple column sorting
        // this will help figure out if CTRL was pressed
        // evt.getModifiersEx();
        // The index of the column whose header was clicked
        int vColIndex = colModel.getColumnIndexAtX(evt.getX());

        // Return if not clicked on any column header
        if (vColIndex == -1)
            return;

        // TODO: modify the column header to show which column is being sorted
        //table.getTableHeader().getColumn.setBackground( Color.DARK_GRAY );
        // see: http://www.exampledepot.com/egs/javax.swing.table/CustHeadRend.html
        if ( getSortColumn() != null &&
             getSortColumn().indexOf( table.getColumnName(vColIndex).toLowerCase() ) != -1 ) {
           if ( getSortColumn().indexOf( "desc" ) != -1 )
                setSortColumn( table.getColumnName(vColIndex) + " asc" );
            else
                setSortColumn( table.getColumnName(vColIndex) + " desc" );
        }
        else
            setSortColumn( table.getColumnName(vColIndex) );

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

        System.out.println("DEBUG Action performed in CPSMasterView: " + action);

        /* Button FILTER CLEAR */
        if (action.equalsIgnoreCase(btnFilterClear.getText())) {
            tfldFilter.setText("");
            return;
        }
        
        // Note the return above, this implies that the following list of if's
        // should really start with an "else"
        if ( action.equalsIgnoreCase( btnNewRecord.getActionCommand() ) ) {
            if ( !isDataAvailable() ) {
                System.err.println("ERROR: cannot create new planting, data unavailable");
                return;
            }
            CPSRecord newRecord = createNewRecord();
            int newID = newRecord.getID();
            // table.setSelectedRow( newID );
            uiManager.displayDetail( newRecord );
            uiManager.setStatus( "Editing new record; save changes to add to list." );
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
            setSelectedRowByRecordID( newID );
            uiManager.displayDetail( newRecord );
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
    
}
