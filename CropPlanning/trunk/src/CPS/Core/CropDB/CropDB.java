/*
 * CropDB.java
 *
 * Created on March 12, 2007, 10:27 PM
 *
 *
 */

package CPS.Core.CropDB;

import CPS.Core.DB.HSQLDB;
import CPS.Module.*;
import javax.swing.JPanel;

/**
 *
 * @author Clayton
 */
public class CropDB extends CPSCoreModule {
   
    private String ModuleName = "CropDB";
    private String ModuleType = "Core";
    private String ModuleVersion = "0.1";

    private CropDBUI ui;

    public CropDB () {
       
       setModuleName( "CropDB" );
       setModuleType( "Core" );
       setModuleVersion( "0.1" );
     
       HSQLDB h = new HSQLDB();
       
       ui = new CropDBUI();
       ui.setDataSource( h );
       if ( ui.verifyVersion( this.getModuleVersion() ) ) { /* Hooray! */ }
       else { /* Oops */ }
    }

    public JPanel display () {
	return ui.getUI();
    }
}
