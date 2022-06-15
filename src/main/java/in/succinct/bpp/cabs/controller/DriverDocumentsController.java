package in.succinct.bpp.cabs.controller;

import com.venky.swf.path.Path;
import in.succinct.bpp.cabs.db.model.supply.DriverDocument;
import in.succinct.bpp.cabs.db.model.supply.VehicleDocument;

public class DriverDocumentsController extends VerifiableDocumentsController<DriverDocument> {
    public DriverDocumentsController(Path path) {
        super(path);
    }
}
