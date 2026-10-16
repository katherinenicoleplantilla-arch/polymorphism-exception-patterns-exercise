import java.util.ArrayList;
import java.util.List;

public class ParcelManager {
    private static ParcelManager instance;
    private final List<Parcel> parcels = new ArrayList<>();

    private ParcelManager() { }

    public static ParcelManager getInstance() {
        if (instance == null) {
            instance = new ParcelManager();
        }
        return instance;
    }

    public boolean exists(String trackingNumber) {
        for (Parcel p : parcels) {
            if (p.getTrackingNumber().equalsIgnoreCase(trackingNumber)) {
                return true;
            }
        }
        return false;
    }

    public void addParcel(Parcel parcel) throws DuplicateParcelException {
        if (exists(parcel.getTrackingNumber())) {
            throw new DuplicateParcelException(parcel.getTrackingNumber());
        }
        parcels.add(parcel);
    }

    public Parcel findParcel(String trackingNumber) throws ParcelNotFoundException {
        for (Parcel p : parcels) {
            if (p.getTrackingNumber().equalsIgnoreCase(trackingNumber)) {
                return p;
            }
        }
        throw new ParcelNotFoundException();
    }

    public List<Parcel> getAllParcels() {
        return new ArrayList<>(parcels);
    }
}