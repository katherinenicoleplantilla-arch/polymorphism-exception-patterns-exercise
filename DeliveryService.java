public interface DeliveryService {
    String getName();
    double calculateFee(Parcel parcel);
    boolean isAllowed(Parcel parcel);
}

class StandardDelivery implements DeliveryService {
    @Override
    public String getName() { return "Standard"; }

    @Override
    public double calculateFee(Parcel parcel) { return parcel.getBaseFee(); }

    @Override
    public boolean isAllowed(Parcel parcel) { return true; }
}

class ExpressDelivery implements DeliveryService {
    @Override
    public String getName() { return "Express"; }

    @Override
    public double calculateFee(Parcel parcel) { return parcel.getBaseFee() * 1.5; }

    @Override
    public boolean isAllowed(Parcel parcel) { return true; }
}

class SameDayDelivery implements DeliveryService {
    @Override
    public String getName() { return "Same Day"; }

    @Override
    public double calculateFee(Parcel parcel) { return (parcel.getBaseFee() * 2) + 100; }

    @Override
    public boolean isAllowed(Parcel parcel) { return parcel.getChargeableWeight() <= 10; }
}