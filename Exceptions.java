class InvalidInputException extends Exception {
    public InvalidInputException(String message) { super(message); }
}

class DuplicateParcelException extends Exception {
    public DuplicateParcelException(String trackingNumber) {
        super("Tracking number " + trackingNumber + " already exists!");
    }
}

class ParcelNotFoundException extends Exception {
    public ParcelNotFoundException() { super("Parcel not found!"); }
}

class SameDayNotAllowedException extends Exception {
    public SameDayNotAllowedException() {
        super("Same Day delivery is only for parcels up to 10 kg!");
    }
}