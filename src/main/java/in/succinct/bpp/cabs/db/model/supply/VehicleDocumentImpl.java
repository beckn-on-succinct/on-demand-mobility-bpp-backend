package in.succinct.bpp.cabs.db.model.supply;

public class VehicleDocumentImpl extends VerifiableDocumentImpl<VehicleDocument> {
    public VehicleDocumentImpl(VehicleDocument p){
        super(p);
    }


    public boolean isExpired(){
        VehicleDocument p = getProxy();
        if (p.getValidFrom() != null && p.getValidTo() != null){
            if (System.currentTimeMillis() > p.getValidTo().getTime()){
                return true;
            }
        }
        return false;
    }
}
