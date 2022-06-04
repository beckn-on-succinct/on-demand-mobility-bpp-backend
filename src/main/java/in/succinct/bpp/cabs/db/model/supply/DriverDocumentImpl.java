package in.succinct.bpp.cabs.db.model.supply;

public class DriverDocumentImpl extends VerifiableDocumentImpl<DriverDocument> {
    public DriverDocumentImpl(DriverDocument p){
        super(p);
    }


    public boolean isExpired(){
        DriverDocument p = getProxy();
        if (p.getValidFrom() != null && p.getValidTo() != null){
            if (System.currentTimeMillis() > p.getValidTo().getTime()){
                return true;
            }
        }
        return false;
    }
}
