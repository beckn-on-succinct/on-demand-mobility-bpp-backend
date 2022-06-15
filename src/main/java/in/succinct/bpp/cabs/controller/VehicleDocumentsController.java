package in.succinct.bpp.cabs.controller;

import com.venky.swf.path.Path;
import in.succinct.bpp.cabs.db.model.supply.VehicleDocument;

public class VehicleDocumentsController extends VerifiableDocumentsController<VehicleDocument> {
    public VehicleDocumentsController(Path path) {
        super(path);
    }
}
