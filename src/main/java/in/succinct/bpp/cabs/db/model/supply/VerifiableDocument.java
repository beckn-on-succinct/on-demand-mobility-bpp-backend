package in.succinct.bpp.cabs.db.model.supply;

import com.venky.swf.db.annotations.column.COLUMN_DEF;
import com.venky.swf.db.annotations.column.IS_VIRTUAL;
import com.venky.swf.db.annotations.column.PASSWORD;
import com.venky.swf.db.annotations.column.defaulting.StandardDefault;
import com.venky.swf.db.annotations.column.relationship.CONNECTED_VIA;
import com.venky.swf.db.annotations.column.ui.HIDDEN;
import com.venky.swf.db.annotations.column.ui.PROTECTION;
import com.venky.swf.db.annotations.column.ui.PROTECTION.Kind;
import com.venky.swf.db.annotations.column.validations.Enumeration;

import java.io.InputStream;
import java.sql.Date;
import java.util.List;

public interface VerifiableDocument  {

    public static final String APPROVED="Approved";
    public static final String REJECTED="Rejected";
    public static final String PENDING="Pending";


    @IS_VIRTUAL
    public boolean isExpired();



    @Enumeration(APPROVED+","+REJECTED+","+PENDING)
    @PROTECTION(Kind.NON_EDITABLE)
    @COLUMN_DEF(value = StandardDefault.SOME_VALUE,args = PENDING)
    public String getVerificationStatus();

    public void setVerificationStatus(String status);


    public Date getValidFrom();
    public void setValidFrom(Date validFrom);

    public Date getValidTo();
    public void setValidTo(Date validTo);

    public InputStream getFile();
    public void setFile(InputStream is);


    @PASSWORD
    public String getPassword();
    public void setPassword(String password);

    @HIDDEN
    @PROTECTION(Kind.NON_EDITABLE)
    public String getFileContentName();
    public void setFileContentName(String name);

    @HIDDEN
    @PROTECTION(Kind.NON_EDITABLE)
    public String getFileContentType();
    public void setFileContentType(String contentType);

    @HIDDEN
    @PROTECTION(Kind.NON_EDITABLE)
    public int getFileContentSize();
    public void setFileContentSize(int size);


    @IS_VIRTUAL
    @PROTECTION(Kind.NON_EDITABLE)
    public String getImageUrl();
    public void setImageUrl(String imageUrl);

    public String getDocumentNumber();
    public void setDocumentNumber(String documentNumber);

    public String getRemarks();
    public void setRemarks(String remarks);

}
