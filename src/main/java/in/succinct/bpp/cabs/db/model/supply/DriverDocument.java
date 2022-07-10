package in.succinct.bpp.cabs.db.model.supply;

import com.venky.swf.db.annotations.column.pm.PARTICIPANT;
import com.venky.swf.db.annotations.column.validations.Enumeration;
import com.venky.swf.db.model.Model;
import com.venky.swf.plugins.collab.db.model.participants.admin.Address;

import java.sql.Date;

public interface DriverDocument extends Model,VerifiableDocument, Address {

    @PARTICIPANT
    public Long getDriverId();
    public void setDriverId(Long id);
    public User getDriver();

    public static final String AADHAR = "Aadhar";
    public static final String LICENSE = "Licence";
    static final String[] DOCUMENTS_NEEDED = new String[]{AADHAR,LICENSE};


    @Enumeration(LICENSE+","+AADHAR)
    public String getDocument();
    public void setDocument(String documentType);

    public Date getDateOfBirth();
    public void setDateOfBirth(Date dateOfBirth);

    public String getLongName();
    public void setLongName(String longName);

}
