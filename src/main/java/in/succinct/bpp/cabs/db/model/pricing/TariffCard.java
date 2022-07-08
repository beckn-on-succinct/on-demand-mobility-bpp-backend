package in.succinct.bpp.cabs.db.model.pricing;

import com.venky.swf.db.annotations.column.UNIQUE_KEY;
import com.venky.swf.db.model.Model;
import in.succinct.bpp.cabs.db.model.supply.DeploymentPurpose;

public interface TariffCard extends Model {
    @UNIQUE_KEY
    public Long getDeploymentPurposeId();
    public void setDeploymentPurposeId(Long id);
    public DeploymentPurpose getDeploymentPurpose();

    @UNIQUE_KEY(allowMultipleRecordsWithNull = false)
    public String getTag();
    public void setTag(String tag);

    @UNIQUE_KEY
    public Integer getFromKms();
    public void setFromKms(Integer fromKms);

    @UNIQUE_KEY
    public Integer getToKms();
    public void setToKms(Integer toKms);

    public Double getFixedPrice();
    public void setFixedPrice(Double fixedPrice);

    public Double getPricePerKm();
    public void setPricePerKm(Double pricePerKm);

    public Double getPricePerHour();
    public void setPricePerHour(Double pricePerHour);

    public Double getTaxRate();
    public void setTaxRate(Double taxRate);

}
