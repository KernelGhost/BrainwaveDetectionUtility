import java.util.ArrayList;

public class WindowManager {
	frmMainMenu frmMainMenu;
	frmDashboard frmDashboard;
	frmVideoGames frmVideoGames;
	frmSerialOut frmSerialOut;
	frmAbout frmAbout;
	
	public WindowManager() {
		frmMainMenu = new frmMainMenu();
		frmDashboard = new frmDashboard();
		frmVideoGames = new frmVideoGames();
		frmSerialOut = new frmSerialOut();
		frmAbout = new frmAbout();
	}
	
	public void UpdateWindow(byte byteWindow, ArrayList<int[]> intNewData, ArrayList<int[]> intAllData) {
		switch(byteWindow) {
		// Depending on which JFrame is open
		case (byte) 0:
			// Live Monitoring Dashboard
			frmDashboard.UpdateProgressBarsTable(intNewData);
			frmDashboard.GraphData(intAllData);
			break;
		case (byte) 1:
			// Game Screen
			frmVideoGames.panelGame.MovePaddle(intAllData);
			frmVideoGames.UpdateProgressBars(intAllData);
			break;
		case (byte) 2:
			// Serial Out Window
			frmSerialOut.SendData(intNewData);
			frmSerialOut.GraphData(intAllData);
			break;
		}
	}
	
	public void OpenWindow(int intWindow) {
		switch(intWindow) {
		  	case 0: // Main Menu
		  		frmDashboard.setVisible(false);
		  		frmVideoGames.setVisible(false);
				frmSerialOut.setVisible(false);
				frmAbout.setVisible(false);
				frmMainMenu.setVisible(true);
		  		break;
		  	case 1: // Live Monitoring Dashboard
		  		frmDashboard = new frmDashboard();
		  		frmDashboard.setVisible(true);
		  		frmMainMenu.setVisible(false);
		  		break;
		  	case 2: // Video Game Menu
		  		frmVideoGames = new frmVideoGames();
		  		frmVideoGames.setVisible(true);
		  		frmMainMenu.setVisible(false);
			    break;
		  	case 3: // Output Serial Data
		  		frmSerialOut = new frmSerialOut();
		  		frmSerialOut.setVisible(true);
		  		frmMainMenu.setVisible(false);
			    break;
		  	case 4: // About Window
		  		frmAbout = new frmAbout();
		  		frmAbout.setVisible(true);
		  		frmMainMenu.setVisible(false);
			    break;
		}
	}
}