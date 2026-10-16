import java.util.List;
import java.util.Scanner;

public class ParcelDeliverySystem {
    private final Scanner scanner = new Scanner(System.in);
    private final ParcelManager manager = ParcelManager.getInstance();

    public static void main(String[] args) {
        new ParcelDeliverySystem().run();
    }

    public void run() {
        int choice;
        do {
            printMenu();
            choice = readMenuChoice();
            switch (choice) {
                case 1: addParcel(); break;
                case 2: viewAllParcels(); break;
                case 3: changeDeliveryService(); break;
                case 4: markAsDelivered(); break;
                case 5: System.out.println("Goodbye!"); break;
                default: System.out.println("Invalid choice! Please enter 1 to 5.");
            }
        } while (choice != 5);
    }

    private void printMenu() {
        System.out.println("\n===== PARCEL DELIVERY SYSTEM =====");
        System.out.println("1. Add Parcel");
        System.out.println("2. View All Parcels");
        System.out.println("3. Change Delivery Service");
        System.out.println("4. Mark as Delivered");
        System.out.println("5. Exit");
    }

    private int readMenuChoice() {
        try {
            return Integer.parseInt(prompt("Enter choice: "));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void addParcel() {
        try {
            System.out.println("1. Document  2. Box  3. Fragile");
            int type = readInt("Select parcel type: ", "Parcel type");
            if (type < 1 || type > 3) {
                throw new InvalidInputException("Invalid parcel type!");
            }

            String tracking = prompt("Tracking number: ").toUpperCase();
            if (manager.exists(tracking)) {
                throw new DuplicateParcelException(tracking);
            }
            String receiver = prompt("Receiver name: ");
            double weight = readDouble("Weight (kg): ", "Weight");

            Parcel parcel;
            if (type == 1) {
                int pages = readInt("Number of pages: ", "Number of pages");
                parcel = new Document(tracking, receiver, weight, pages);
            } else {
                int l = readInt("Length (cm): ", "Length");
                int w = readInt("Width (cm): ", "Width");
                int h = readInt("Height (cm): ", "Height");
                if (type == 2) {
                    parcel = new Box(tracking, receiver, weight, l, w, h);
                } else {
                    String note = prompt("Handling note: ");
                    parcel = new Fragile(tracking, receiver, weight, l, w, h, note);
                }
            }

            while (true) {
                try {
                    parcel.setDeliveryService(chooseService());
                    break;
                } catch (SameDayNotAllowedException e) {
                    System.out.println(e.getMessage());
                }
            }

            manager.addParcel(parcel);
            System.out.printf("Parcel added successfully! Delivery fee: Php %.2f%n", parcel.getDeliveryFee());
        } catch (InvalidInputException | DuplicateParcelException e) {
            System.out.println(e.getMessage());
        }
    }

    private void viewAllParcels() {
        List<Parcel> parcels = manager.getAllParcels();
        if (parcels.isEmpty()) {
            System.out.println("No parcels yet.");
            return;
        }
        String format = "%-10s %-10s %-20s %-12s %-10s %-14s %-10s%n";
        System.out.printf(format, "Tracking", "Type", "Receiver", "Charge Wt", "Service", "Delivery Fee", "Status");
        System.out.println("-".repeat(92));
        for (Parcel p : parcels) {
            System.out.printf(format,
                    p.getTrackingNumber(),
                    p.getType(),
                    p.getReceiverName(),
                    String.format("%.0f kg", p.getChargeableWeight()),
                    p.getDeliveryService().getName(),
                    String.format("Php %.2f", p.getDeliveryFee()),
                    p.getStatus());
        }
    }

    private void changeDeliveryService() {
        try {
            Parcel parcel = manager.findParcel(prompt("Tracking number: "));
            if (parcel.getStatus() == Status.DELIVERED) {
                System.out.println("Parcel already delivered!");
                return;
            }
            DeliveryService service = chooseService();
            parcel.setDeliveryService(service);
            System.out.printf("Delivery service changed! New fee: Php %.2f%n", parcel.getDeliveryFee());
        } catch (ParcelNotFoundException | SameDayNotAllowedException e) {
            System.out.println(e.getMessage());
        }
    }

    private void markAsDelivered() {
        try {
            Parcel parcel = manager.findParcel(prompt("Tracking number: "));
            if (parcel.getStatus() == Status.DELIVERED) {
                System.out.println("Parcel was already delivered.");
            } else {
                parcel.markDelivered();
                System.out.println("Parcel delivered successfully!");
            }
        } catch (ParcelNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    private DeliveryService chooseService() {
        while (true) {
            System.out.println("1. Standard  2. Express  3. Same Day");
            String input = prompt("Select delivery service: ");
            switch (input) {
                case "1": return new StandardDelivery();
                case "2": return new ExpressDelivery();
                case "3": return new SameDayDelivery();
                default: System.out.println("Invalid choice!");
            }
        }
    }

    private String prompt(String message) {
        System.out.print(message);
        return scanner.nextLine().trim();
    }

    private int readInt(String message, String field) throws InvalidInputException {
        try {
            return Integer.parseInt(prompt(message));
        } catch (NumberFormatException e) {
            throw new InvalidInputException(field + " must be a whole number!");
        }
    }

    private double readDouble(String message, String field) throws InvalidInputException {
        try {
            return Double.parseDouble(prompt(message));
        } catch (NumberFormatException e) {
            throw new InvalidInputException(field + " must be a number!");
        }
    }
}