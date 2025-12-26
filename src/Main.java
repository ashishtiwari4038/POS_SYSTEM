import main.ui.LoginFrame;
import main.database.DatabaseSetup;

public class Main {
    public static void main(String[] args) {

        DatabaseSetup.initializeDatabase();

        new LoginFrame().setVisible(true);
    }
}

