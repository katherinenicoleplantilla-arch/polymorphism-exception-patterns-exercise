import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class ParcelDeliverySystem {
    private static final String TRACKING_RULE = "ABC followed by 4 digits, e.g. ABC0001";
    private static final String[] KEYBOARD_RUNS = {
        "qwer", "asdf", "zxcv", "sdfg", "dfgh", "hjkl", "xcvb", "vbnm", "uiop"
    };
    private static final Pattern REPEATED_LETTER = Pattern.compile("(\\p{L})\\1{2,}");
    private static final Pattern TOO_MANY_CONSONANTS = Pattern.compile("[b-df-hj-np-tv-xz]{5,}");

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
                default: System.out.println("Invalid choice! Please enter a number from 1 to 5.");
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

    // ===== MENU OPERATIONS =====

    private void addParcel() {
        System.out.println("\n1. Document  2. Box  3. Fragile");
        int type = readInt("Parcel type: ", "Invalid input! Enter 1, 2, or 3.", 1, 3);

        String[] typeNames = {"Document", "Box", "Fragile"};
        double[] maxWeights = {Document.MAX_WEIGHT, Box.MAX_WEIGHT, Fragile.MAX_WEIGHT};
        String typeName = typeNames[type - 1];
        double maxWeight = maxWeights[type - 1];

        String tracking = readNewTrackingNumber();
        String receiver = readText("Receiver name", true, 50);
        double weight = readWeight(typeName, maxWeight);

        try {
            Parcel parcel;
            if (type == 1) {
                int pages = readInt("Number of pages (1-100): ",
                        "Invalid input! Number of pages must be a whole number from 1 to 100.", 1, 100);
                parcel = new Document(tracking, receiver, weight, pages);
            } else {
                int l = readDimension("Length");
                int w = readDimension("Width");
                int h = readDimension("Height");
                if (type == 2) {
                    parcel = new Box(tracking, receiver, weight, l, w, h);
                } else {
                    String note = readText("Handling note", false, 100);
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
        if (manager.isEmpty()) {
            System.out.println("No parcels yet.");
            return;
        }
        List<Parcel> parcels = manager.getAllParcels();
        String format = "%-10s %-10s %-20s %-12s %-10s %-14s %-10s%n";
        System.out.printf(format, "Tracking", "Type", "Receiver", "Charge Wt", "Service", "Delivery Fee", "Status");
        System.out.println("-".repeat(92));
        for (Parcel p : parcels) {
            System.out.printf(format,
                    p.getTrackingNumber(),
                    p.getType(),
                    p.getReceiverName(),
                    String.format("%.2f kg", p.getChargeableWeight()),
                    p.getDeliveryService().getName(),
                    String.format("Php %.2f", p.getDeliveryFee()),
                    p.getStatus());
        }
    }

    private void changeDeliveryService() {
        if (manager.isEmpty()) {
            System.out.println("No parcels yet.");
            return;
        }
        Parcel parcel = readExistingParcel();
        if (parcel == null) {
            System.out.println("Cancelled.");
            return;
        }
        if (parcel.getStatus() == Status.DELIVERED) {
            System.out.println("Parcel already delivered!");
            return;
        }
        try {
            parcel.setDeliveryService(chooseService());
            System.out.printf("Delivery service changed! New fee: Php %.2f%n", parcel.getDeliveryFee());
        } catch (SameDayNotAllowedException e) {
            System.out.println(e.getMessage());
            System.out.println("The parcel keeps its current service ("
                    + parcel.getDeliveryService().getName() + ").");
        }
    }

    private void markAsDelivered() {
        if (manager.isEmpty()) {
            System.out.println("No parcels yet.");
            return;
        }
        Parcel parcel = readExistingParcel();
        if (parcel == null) {
            System.out.println("Cancelled.");
            return;
        }
        if (parcel.getStatus() == Status.DELIVERED) {
            System.out.println("Parcel was already delivered.");
        } else {
            parcel.markDelivered();
            System.out.println("Parcel delivered successfully!");
        }
    }

    // ===== INPUT HELPERS (each one repeats until the input is valid) =====

    private String readNewTrackingNumber() {
        while (true) {
            String tracking = prompt("Tracking number (e.g. ABC0001): ").toUpperCase();
            try {
                validateTrackingFormat(tracking);
                if (manager.exists(tracking)) {
                    throw new DuplicateParcelException(tracking);
                }
                return tracking;
            } catch (InvalidInputException e) {
                System.out.println(e.getMessage());
            } catch (DuplicateParcelException e) {
                System.out.println(e.getMessage() + " Tracking numbers must be unique, try a different one.");
            }
        }
    }

    // returns null if the user cancels by typing 0
    private Parcel readExistingParcel() {
        while (true) {
            String tracking = prompt("Tracking number (e.g. ABC0001, 0 to cancel): ").toUpperCase();
            if (tracking.equals("0")) {
                return null;
            }
            try {
                validateTrackingFormat(tracking);
                return manager.findParcel(tracking);
            } catch (InvalidInputException | ParcelNotFoundException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void validateTrackingFormat(String tracking) throws InvalidInputException {
        if (!tracking.matches("ABC\\d{4}")) {
            throw new InvalidInputException("Invalid tracking number! It should be " + TRACKING_RULE + ".");
        }
    }

    // isName = true also restricts the characters to those used in names
    private String readText(String label, boolean isName, int maxLength) {
        while (true) {
            String input = prompt(label + ": ");
            String error = textError(label, input, isName, maxLength);
            if (error == null) {
                return input;
            }
            System.out.println("Invalid input! " + error);
        }
    }

    // returns an error message, or null if the text is fine
    private String textError(String label, String text, boolean isName, int maxLength) {
        if (text.isEmpty()) {
            return label + " cannot be empty.";
        }
        if (text.length() > maxLength) {
            return label + " is too long (max " + maxLength + " characters).";
        }
        if (isName && !text.matches("\\p{L}[\\p{L} .'-]*")) {
            return label + " should only have letters, spaces, hyphens, apostrophes, or periods.";
        }
        long letters = text.chars().filter(Character::isLetter).count();
        if (letters < 2) {
            return label + " should have at least 2 letters.";
        }
        String lower = text.toLowerCase();
        if (REPEATED_LETTER.matcher(lower).find()) {
            return label + " looks like gibberish (too many repeated letters).";
        }
        if (TOO_MANY_CONSONANTS.matcher(lower).find()) {
            return label + " looks like gibberish (too many consonants in a row).";
        }
        for (String run : KEYBOARD_RUNS) {
            if (lower.contains(run)) {
                return label + " looks like gibberish (random keyboard letters).";
            }
        }
        return null;
    }

        private int readDimension(String label) {
        int result = 0;
        boolean valid = false;
        while (!valid) {
            String input = prompt(label + " in cm (whole number > 0): ");
            try {
                int value = Integer.parseInt(input);
                if (value < 1) {
                    System.out.println("Invalid input! " + label + " must be a whole number greater than 0.");
                } else if (value > Box.MAX_DIMENSION) {
                    System.out.println("Invalid input! " + label + " is too large for a parcel.");
                } else {
                    result = value;
                    valid = true;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! " + label + " must be a whole number greater than 0.");
            }
        }
        return result;
    }

    private int readInt(String promptText, String errorMessage, int min, int max) {
        while (true) {
            String input = prompt(promptText);
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
            } catch (NumberFormatException e) {
                // falls through to the error message below
            }
            System.out.println(errorMessage);
        }
    }

    private double readWeight(String typeName, double maxWeight) {
        String error = String.format(
                "Invalid input! Weight must be a number greater than 0 and up to %.2f kg for %s.",
                maxWeight, typeName);
        while (true) {
            String input = prompt(String.format("Weight in kg (max %.2f): ", maxWeight));
            try {
                double value = Double.parseDouble(input);
                if (value > 0 && value <= maxWeight) {
                    return value;
                }
            } catch (NumberFormatException e) {
                // falls through to the error message below
            }
            System.out.println(error);
        }
    }

    private DeliveryService chooseService() {
        System.out.println("1. Standard  2. Express  3. Same Day");
        while (true) {
            String input = prompt("Delivery service: ");
            switch (input) {
                case "1": return new StandardDelivery();
                case "2": return new ExpressDelivery();
                case "3": return new SameDayDelivery();
                default: System.out.println("Invalid choice! Enter 1, 2, or 3.");
            }
        }
    }

    private String prompt(String message) {
        System.out.print(message);
        return scanner.nextLine().trim();
    }
}