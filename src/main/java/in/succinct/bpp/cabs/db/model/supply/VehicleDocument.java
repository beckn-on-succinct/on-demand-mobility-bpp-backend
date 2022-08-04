package in.succinct.bpp.cabs.db.model.supply;
package in.succinct.bpp.cabs.db.model.service;

import java.util.List;

import com.venky.swf.db.annotations.column.relationship.CONNECTED_VIA;
import com.venky.swf.db.annotations.column.pm.PARTICIPANT;
import com.venky.swf.db.annotations.column.validations.Enumeration;
import com.venky.swf.db.model.Model;

public interface VehicleDocument extends VerifiableDocument, Model {
    String[] DOCUMENTS_NEEDED = new String[]{"RC","FITNESS","INSURANCE"};

    @PARTICIPANT
    public Long getVehicleId();
    public void setVehicleId(Long id);
    public Vehicle getVehicle();

    @Enumeration("RC,FITNESS,INSURANCE")
    public String getDocument();
    public void setDocument(String documentType);

    @CONNECTED_VIA(value = "MODEL_ID", additional_join = "((NAME = 'VehicleDocument'))")
	public List<ModelAudit> getAudits();

}
