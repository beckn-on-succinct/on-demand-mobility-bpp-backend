package in.succinct.bpp.cabs.extensions;

import in.succinct.bpp.cabs.db.model.supply.VehicleDocument;

public class BeforeValidateVehicleDocument extends BeforeValidateVerifiableDocument<VehicleDocument> {
    static {
        registerExtension(new BeforeValidateVehicleDocument());
    }
}
