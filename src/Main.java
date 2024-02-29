import java.awt.Color;
import java.awt.EventQueue;
import javax.swing.JOptionPane;
import javax.swing.UnsupportedLookAndFeelException;

public class Main {
	public static WindowManager window_manager;
	public static InputStreamHandler input_stream_handler;
	public static DataFiltering data_filtering;
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				// Set Nimbus Look and Feel
				boolean boolNimbus = false;
				for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
					if ("Nimbus".equals(info.getName())) {
						try {
							javax.swing.UIManager.setLookAndFeel(info.getClassName());
							javax.swing.UIManager.getLookAndFeelDefaults().put("nimbusOrange", new Color(169, 46, 34));
						} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
								| UnsupportedLookAndFeelException e) {
							JOptionPane.showMessageDialog(null, "Could not start the application with the preferred look and feel.\nUsing system defaults.", "BDU", JOptionPane.ERROR_MESSAGE, null);
						}
						
						boolNimbus = true;
				        break;
					}
			    }
				
				if (!boolNimbus) {
					JOptionPane.showMessageDialog(null, "Could not start the application with the preferred look and feel.\nUsing system defaults.", "BDU", JOptionPane.ERROR_MESSAGE, null);
				}
				
				// Open main menu
				window_manager = new WindowManager();
				window_manager.OpenWindow(0);
			}
		});
	}
}