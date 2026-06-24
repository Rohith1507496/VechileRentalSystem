public class TestDriver {
    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("SUCCESS - DRIVER LOADED");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}