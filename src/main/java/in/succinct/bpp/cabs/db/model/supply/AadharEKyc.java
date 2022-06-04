package in.succinct.bpp.cabs.db.model.supply;

import com.venky.cache.Cache;
import com.venky.core.date.DateUtils;
import com.venky.core.io.SeekableByteArrayOutputStream;
import com.venky.core.util.ObjectUtil;
import com.venky.digest.Encryptor;
import com.venky.xml.XMLDocument;
import com.venky.xml.XMLElement;
import net.lingala.zip4j.model.LocalFileHeader;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.text.ParseException;
import java.util.Date;

public class AadharEKyc {

    private AadharEKyc(){

    }
    private  static AadharEKyc instance = null;
    public static AadharEKyc getInstance(){
        if (instance != null){
            return instance;
        }
        synchronized (AadharEKyc.class){
            if (instance == null){
                instance = new AadharEKyc();
            }
        }
        return instance;
    }

    public static class AadharData {
        Cache<String,Object> aadharData = new Cache<String, Object>() {
            @Override
            protected Object getValue(String fieldName) {
                return null;
            }
        };

        public static final String REFERENCE_ID = "referenceId";


        public static final String DATE_OF_BIRTH = "dob";
        public static final String EMAIL_ID_HASH = "e";
        public static final String MOBILE_NUMBER_HASH = "m";
        public static final String GENDER = "gender";
        public static final String NAME = "name";

        public static final String CARE_OF = "careof";
        public static final String HOUSE = "house";
        public static final String STREET = "street";
        public static final String LOCALITY = "loc";
        public static final String DISTRICT = "dist";
        public static final String POST_OFFICE = "po";
        public static final String VTC = "vtc";
        public static final String PIN_CODE = "pc";
        public static final String STATE = "state";

        String  shareCode = null;
        AadharData(String shareCode){
            this.shareCode = shareCode;
        }
        public <T> T get(String name){
            return (T)aadharData.get(name);
        }
        public <T> void put(String name, T value){
            aadharData.put(name,value);
        }

        public Date getDateOfBirth() throws ParseException {
            return DateUtils.getFormat("dd-MM-yyyy").parse(get(DATE_OF_BIRTH));
        }


        public void validatePhone(String phoneNumber){
            if (phoneNumber.length() > 10 ){
                phoneNumber = phoneNumber.substring(phoneNumber.length()-10,phoneNumber.length());
            }
            if (!ObjectUtil.isVoid(phoneNumber)) {
                String hash = phoneNumber + shareCode;
                long lastAadharDigit = Long.parseLong(getLast4DigitsOfAadhar()) % 10;

                for (int i = 0 ; i < Math.max(1,lastAadharDigit) ; i ++){
                    hash = Encryptor.encrypt(hash,"SHA-256");
                }
                if (!ObjectUtil.isVoid(get(MOBILE_NUMBER_HASH)) && !ObjectUtil.equals(hash,get(MOBILE_NUMBER_HASH))){
                    throw new RuntimeException(phoneNumber + " is not the phone registered with aadhar.");
                }
            }

        }

        public void validateEmail(String email){
            if (!ObjectUtil.isVoid(email)) {
                String hash = email + shareCode;
                long lastAadharDigit = Long.parseLong(getLast4DigitsOfAadhar()) % 10;

                for (int i = 0 ; i < Math.max(1,lastAadharDigit) ; i ++){
                    hash = Encryptor.encrypt(hash,"SHA-256");
                }

                if (!ObjectUtil.isVoid(get(EMAIL_ID_HASH)) && !ObjectUtil.equals(hash,get(EMAIL_ID_HASH))){
                    throw new RuntimeException(email + "is not the email registered with aadhar.");
                }
            }

        }

        public String getLast4DigitsOfAadhar(){
            String ref = get("referenceId");
            return ref.substring(0,4);
        }

    }

    public AadharData parseZip(InputStream zipStream, String shareCode) throws Exception {
        XMLDocument aadharXML = parseXMLFromZip(zipStream, shareCode);
        if (aadharXML == null) {
            return null;
        }

        AadharData data = new AadharData(shareCode);
        XMLElement root = aadharXML.getDocumentRoot();


        XMLElement poi = root.getChildElement("UidData").getChildElement("Poi");
        XMLElement poa = root.getChildElement("UidData").getChildElement("Poa");



        for (String aadharFieldName : new String[] {AadharData.REFERENCE_ID} ){
            data.put(aadharFieldName,root.getAttribute(aadharFieldName));
        }

        for (String aadharFieldName : new String[] {AadharData.DATE_OF_BIRTH, AadharData.EMAIL_ID_HASH, AadharData.MOBILE_NUMBER_HASH, AadharData.GENDER, AadharData.NAME} ){
            data.put(aadharFieldName,poi.getAttribute(aadharFieldName));
        }

        for (String aadharFieldName : new String[] {AadharData.CARE_OF, AadharData.HOUSE, AadharData.STREET, AadharData.LOCALITY,
                AadharData.DISTRICT, AadharData.POST_OFFICE, AadharData.VTC, AadharData.PIN_CODE, AadharData.STATE} ){
            data.put(aadharFieldName,poa.getAttribute(aadharFieldName));
        }

        return data;
    }


    private boolean verifySignature(XMLDocument aadharXML) throws Exception {
        Document document = aadharXML.getDocument();

        CertificateFactory fact = CertificateFactory.getInstance("X.509");
        InputStream certStream = getClass().getResourceAsStream("/config/uidai_auth_sign_prod_2023.cer");
        Certificate cer = fact.generateCertificate(certStream);
        PublicKey key = cer.getPublicKey();

        NodeList nl = document.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
        if (nl.getLength() == 0)
            throw new IllegalArgumentException("Cannot find Signature element");

        XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

        DOMValidateContext valContext = new DOMValidateContext(key, nl.item(0));
        valContext.setProperty("org.jcp.xml.dsig.secureValidation",Boolean.FALSE);
        XMLSignature signature = fac.unmarshalXMLSignature(valContext);

        return signature.validate(valContext);

    }


    private XMLDocument parseXMLFromZip(InputStream zipInputStream, String shareCode) throws Exception {
        net.lingala.zip4j.io.inputstream.ZipInputStream is = null ;
        if (!ObjectUtil.isVoid(shareCode)){
            is = new net.lingala.zip4j.io.inputstream.ZipInputStream(zipInputStream, shareCode.toCharArray());
        }else { 
            is = new net.lingala.zip4j.io.inputstream.ZipInputStream(zipInputStream);
        }
        SeekableByteArrayOutputStream baos = new SeekableByteArrayOutputStream();
        LocalFileHeader entry;
        while ((entry = is.getNextEntry()) != null) {
            if (entry.getFileName().endsWith(".xml")) {
                break;
            }
        }

        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) > 0) {
            baos.write(buffer, 0, len);
        }

        XMLDocument doc =  XMLDocument.getDocumentFor(new ByteArrayInputStream(baos.toByteArray()));
        if (!verifySignature(doc)){
            throw new RuntimeException("Could not verify aadhar's signature on the kyc document.");
        }
        return doc;

    }


}
