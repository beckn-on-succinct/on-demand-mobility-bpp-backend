package in.succinct.bpp.cabs.db.model.supply;

import com.venky.swf.db.annotations.column.IS_VIRTUAL;
import com.venky.swf.db.annotations.column.PASSWORD;
import com.venky.swf.db.annotations.column.ui.HIDDEN;
import com.venky.swf.db.annotations.column.ui.PROTECTION;
import com.venky.swf.db.annotations.column.ui.PROTECTION.Kind;

import java.io.InputStream;
import java.util.Date;

public interface VerifiableDocument  {

    @IS_VIRTUAL
    public boolean isExpired();

    @PROTECTION(Kind.NON_EDITABLE)
    public boolean isVerified();
    public void setVerified(boolean verified);


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


}
