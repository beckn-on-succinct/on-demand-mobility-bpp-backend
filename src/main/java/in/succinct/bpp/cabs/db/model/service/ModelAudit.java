package in.succinct.bpp.cabs.db.model.service;

import com.venky.swf.db.annotations.column.COLUMN_NAME;

import in.succinct.bpp.cabs.db.model.supply.DriverDocument;
import in.succinct.bpp.cabs.db.model.supply.VehicleDocument;

public interface ModelAudit extends com.venky.swf.plugins.audit.db.model.ModelAudit {
    @COLUMN_NAME("MODEL_ID")
    public Long getDriverDocumentId();
    public void setDriverDocumentId(Long id);
    public DriverDocument getDriverDocument();
    
    @COLUMN_NAME("MODEL_ID")
    public Long getVehicleDocumentId();
    public void setVehicleDocumentId(Long id);
    public VehicleDocument getVehicleDocument();
    
}
