import Controller.Controller;
import View.UI;

public class Main {
    public static void main(String[] args) {
        Controller controller = new Controller();
        new UI(controller).setVisible(true);
    }
}
