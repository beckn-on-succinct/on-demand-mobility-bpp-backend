package in.succinct.bpp.cabs.extensions;

import com.venky.core.util.ObjectUtil;
import com.venky.swf.plugins.collab.db.model.config.City;
import com.venky.swf.plugins.collab.db.model.config.Country;
import com.venky.swf.plugins.collab.db.model.config.PinCode;
import com.venky.swf.plugins.collab.db.model.config.State;
import com.venky.swf.plugins.collab.db.model.user.Email;
import com.venky.swf.plugins.collab.db.model.user.Phone;
import in.succinct.bpp.cabs.db.model.supply.AadharEKyc;
import in.succinct.bpp.cabs.db.model.supply.AadharEKyc.AadharData;
import in.succinct.bpp.cabs.db.model.supply.DriverDocument;
import in.succinct.bpp.cabs.db.model.supply.VerifiableDocument;

import java.sql.Date;

public class BeforeValidateDriverDocument extends BeforeValidateVerifiableDocument<DriverDocument> {
    static {
        registerExtension(new BeforeValidateDriverDocument());
    }

    @Override
    public void beforeValidate(DriverDocument userAddress) {
        if (ObjectUtil.isVoid(userAddress.getPhoneNumber())){
            userAddress.setPhoneNumber(userAddress.getDriver().getPhoneNumber());
        }
        if (ObjectUtil.isVoid(userAddress.getEmail())){
            userAddress.setEmail(userAddress.getDriver().getEmail());
        }
        if (!ObjectUtil.isVoid(userAddress.getPhoneNumber()) && userAddress.getRawRecord().isFieldDirty("PHONE_NUMBER")){
            userAddress.setPhoneNumber(Phone.sanitizePhoneNumber(userAddress.getPhoneNumber()));
        }
        if (!ObjectUtil.isVoid(userAddress.getEmail()) && userAddress.getRawRecord().isFieldDirty("EMAIL")){
            Email.validate(userAddress.getEmail());
        }

        if (userAddress.getDocument().equals(DriverDocument.AADHAR)){
            if (!userAddress.getVerificationStatus().equals(VerifiableDocument.APPROVED) && userAddress.getFileContentSize() > 0){
                try {
                    AadharData data = AadharEKyc.getInstance().parseZip(userAddress.getFile(),userAddress.getPassword());
                    if (data != null){
                        if (!ObjectUtil.isVoid(userAddress.getPhoneNumber())){
                            data.validatePhone(userAddress.getPhoneNumber());
                        }else if (!ObjectUtil.isVoid(userAddress.getDriver().getPhoneNumber())) {
                            data.validatePhone(userAddress.getDriver().getPhoneNumber());
                            userAddress.setPhoneNumber(userAddress.getDriver().getPhoneNumber());
                        }

                        if (!ObjectUtil.isVoid(userAddress.getEmail())){
                            data.validateEmail(userAddress.getEmail());
                        }else if (!ObjectUtil.isVoid(userAddress.getDriver().getEmail())) {
                            data.validateEmail(userAddress.getDriver().getEmail());
                            userAddress.setEmail(userAddress.getDriver().getEmail());
                        }
                        userAddress.setLongName(data.get(AadharEKyc.AadharData.NAME));
                        userAddress.setDateOfBirth(new Date(data.getDateOfBirth().getTime()));
                        userAddress.setAddressLine1(data.get(AadharEKyc.AadharData.HOUSE));
                        userAddress.setAddressLine2(data.get(AadharEKyc.AadharData.STREET));
                        userAddress.setAddressLine3(data.get(AadharEKyc.AadharData.LOCALITY));
                        userAddress.setAddressLine4(data.get(AadharEKyc.AadharData.POST_OFFICE));
                        userAddress.setCountryId(Country.findByName("India").getId());
                        State state = State.findByCountryAndName(userAddress.getCountryId(), data.get(AadharEKyc.AadharData.STATE));
                        if (state != null) {
                            userAddress.setStateId(state.getId());
                        }
                        City city = City.findByStateAndName(userAddress.getStateId(), data.get(AadharEKyc.AadharData.DISTRICT));
                        if (city == null) {
                            city = City.findByStateAndName(userAddress.getStateId(), data.get(AadharEKyc.AadharData.LOCALITY));
                        }
                        if (city != null) {
                            userAddress.setCityId(city.getId());
                        }


                        PinCode pinCode = PinCode.find(data.get(AadharEKyc.AadharData.PIN_CODE));
                        if (pinCode != null) {
                            userAddress.setPinCodeId(pinCode.getId());
                        }
                        userAddress.setVerificationStatus(VerifiableDocument.APPROVED);
                        userAddress.setTxnProperty("being.verified",true);
                        //userAddress.save();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                    //Config.instance().getLogger(getClass().getName()).log(Level.WARNING,"Failed Aadhar Validation", e);
                }
            }
        }
        super.beforeValidate(userAddress);
    }
}
