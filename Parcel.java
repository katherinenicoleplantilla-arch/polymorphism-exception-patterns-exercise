public abstract class Parcel {
    private final String trackingNumber;
    private final String receiverName;
    private final double weight;
    private DeliveryService deliveryService;
    private Status status;

    protected Parcel(String trackingNumber, String receiverName, double weight)
            throws InvalidInputException {
        if (trackingNumber == null || !trackingNumber.matches("ABC\\d{4}")) {
            throw new InvalidInputException("Tracking number must be ABC followed by 4 digits!");
        }
        if (receiverName == null || receiverName.trim().isEmpty()) {
            throw new InvalidInputException("Receiver name cannot be empty!");
        }
        if (!(weight > 0)) {
            throw new InvalidInputException("Weight must be greater than 0!");
        }
        if (weight > getMaxWeight()) {
            throw new InvalidInputException(String.format("Maximum weight for %s is %.2f kg!", getType(), getMaxWeight()));
        }
        this.trackingNumber = trackingNumber;
        this.receiverName = receiverName.trim();
        this.weight = weight;
        this.status = Status.PENDING;
    }

    public abstract String getType();
    public abstract double getMaxWeight();
    public abstract double getChargeableWeight();
    public abstract double getBaseFee();

    public double getDeliveryFee() {
        return deliveryService.calculateFee(this);
    }

    public void setDeliveryService(DeliveryService service) throws SameDayNotAllowedException {
        if (!service.isAllowed(this)) {
            throw new SameDayNotAllowedException();
        }
        this.deliveryService = service;
    }

    public void markDelivered() {
        this.status = Status.DELIVERED;
    }

    public String getTrackingNumber() { return trackingNumber; }
    public String getReceiverName() { return receiverName; }
    public double getWeight() { return weight; }
    public DeliveryService getDeliveryService() { return deliveryService; }
    public Status getStatus() { return status; }
}

class Document extends Parcel {
    public static final double MAX_WEIGHT = 0.5;

    private final int pages;

    public Document(String trackingNumber, String receiverName, double weight, int pages)
            throws InvalidInputException {
        super(trackingNumber, receiverName, weight);
        if (pages < 1 || pages > 100) {
            throw new InvalidInputException("Number of pages must be from 1 to 100!");
        }
        this.pages = pages;
    }

    @Override
    public String getType() { return "Document"; }

    @Override
    public double getMaxWeight() { return MAX_WEIGHT; }

    @Override
    public double getChargeableWeight() { return getWeight(); }

    @Override
    public double getBaseFee() { return 50; }

    public int getPages() { return pages; }
}

class Box extends Parcel {
    public static final double MAX_WEIGHT = 50;

    private final int length;
    private final int width;
    private final int height;

    public Box(String trackingNumber, String receiverName, double weight,
               int length, int width, int height) throws InvalidInputException {
        super(trackingNumber, receiverName, weight);
        if (length <= 0 || width <= 0 || height <= 0) {
            throw new InvalidInputException("Length, width, and height must be greater than 0!");
        }
        this.length = length;
        this.width = width;
        this.height = height;
    }

    public double getVolumetricWeight() {
        return ((double) length * width * height) / 3500.0;
    }

    @Override
    public String getType() { return "Box"; }

    @Override
    public double getMaxWeight() { return MAX_WEIGHT; }

    @Override
    public double getChargeableWeight() {
        return Math.ceil(Math.max(getWeight(), getVolumetricWeight()));
    }

    @Override
    public double getBaseFee() { return 40 * getChargeableWeight(); }
}

class Fragile extends Box {
    public static final double MAX_WEIGHT = 30;

    private final String handlingNote;

    public Fragile(String trackingNumber, String receiverName, double weight,
                   int length, int width, int height, String handlingNote)
            throws InvalidInputException {
        super(trackingNumber, receiverName, weight, length, width, height);
        if (handlingNote == null || handlingNote.trim().isEmpty()) {
            throw new InvalidInputException("Handling note cannot be empty!");
        }
        this.handlingNote = handlingNote.trim();
    }

    @Override
    public String getType() { return "Fragile"; }

    @Override
    public double getMaxWeight() { return MAX_WEIGHT; }

    @Override
    public double getBaseFee() { return super.getBaseFee() + 100; }

    public String getHandlingNote() { return handlingNote; }
}

enum Status {
    PENDING("Pending"),
    DELIVERED("Delivered");

    private final String label;

    Status(String label) { this.label = label; }

    @Override
    public String toString() { return label; }
}