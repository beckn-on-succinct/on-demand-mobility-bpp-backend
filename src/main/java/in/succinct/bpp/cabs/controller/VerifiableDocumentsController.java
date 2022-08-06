package in.succinct.bpp.cabs.controller;

import com.venky.swf.controller.ModelController;
import com.venky.swf.controller.annotations.SingleRecordAction;
import com.venky.swf.db.Database;
import com.venky.swf.db.model.Model;
import com.venky.swf.path.Path;
import com.venky.swf.views.View;
import in.succinct.bpp.cabs.db.model.supply.VerifiableDocument;

public class VerifiableDocumentsController<M extends VerifiableDocument & Model> extends ModelController<M> {
    public VerifiableDocumentsController(Path path) {
        super(path);
    }

    @SingleRecordAction(icon = "fas fa-check", tooltip = "Mark Approved")
    public View approve(long id){
        M document = Database.getTable(getModelClass()).get(id);
        document.setVerified(true);
        document.setTxnProperty("being.verified",true);
        document.setRejected(false);
        document.setTxnProperty("being.rejected",false);
        document.save();
        return show(document);
    }

    @SingleRecordAction(icon = "fas fa-check", tooltip = "Mark Rejected")
    public View reject(long id){
        M document = Database.getTable(getModelClass()).get(id);
        document.setVerified(true);
        document.setTxnProperty("being.verified",true);
        document.setRejected(true);
        document.setTxnProperty("being.rejected",true);
        document.save();
        return show(document);
    }
}
