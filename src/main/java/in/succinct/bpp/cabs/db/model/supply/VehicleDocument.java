package in.succinct.bpp.cabs.db.model.supply;

import com.venky.swf.db.annotations.column.validations.Enumeration;
import com.venky.swf.db.model.Model;

public interface VehicleDocument extends VerifiableDocument, Model {
    String[] DOCUMENTS_NEEDED = new String[]{"RC","FITNESS","INSURANCE"};

    public Long getVehicleId();
    public void setVehicleId(Long id);
    public Vehicle getVehicle();

    @Enumeration("RC,FITNESS,INSURANCE")
    public String getDocument();
    public void setDocument(String documentType);

}
