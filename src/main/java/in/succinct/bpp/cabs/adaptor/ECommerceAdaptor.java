package in.succinct.bpp.cabs.adaptor;

import com.venky.swf.plugins.beckn.messaging.Subscriber;
import in.succinct.beckn.Items;
import in.succinct.beckn.Locations;
import in.succinct.beckn.Order;
import in.succinct.beckn.Request;
import in.succinct.bpp.cabs.BecknUtil;
import in.succinct.bpp.core.adaptor.CommerceAdaptor;
import in.succinct.bpp.core.adaptor.FulfillmentStatusAdaptor.FulfillmentStatusAudit;

import java.util.List;
import java.util.Map;

public class ECommerceAdaptor extends CommerceAdaptor {
    BecknUtil util ;
    public ECommerceAdaptor(Map<String,String> configuration, Subscriber subscriber){
         super(configuration,subscriber);
         this.util = new BecknUtil();
    }

    @Override
    public void search(Request request, Request response) {
        util.search(request,response);
    }

    @Override
    public void select(Request request, Request response) {
        util.select(request,response);
    }

    @Override
    public void init(Request request, Request response) {
        util.init(request,response);
    }

    @Override
    public void confirm(Request request, Request response) {
        util.confirm(request,response);
    }

    @Override
    public void track(Request request, Request response) {
        util.track(request,response);
    }

    @Override
    public void issue(Request request, Request response) {
        throw new RuntimeException("UnSupported operation");
    }

    @Override
    public void issue_status(Request request, Request response) {
        throw new RuntimeException("UnSupported operation");
    }

    @Override
    public void cancel(Request request, Request response) {
        util.cancel(request,response);
    }

    @Override
    public void update(Request request, Request response) {
        util.update(request,response);
    }

    @Override
    public void status(Request request, Request response) {
        util.status(request,response);
    }

    @Override
    public void rating(Request request, Request response) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void support(Request request, Request response) {
        util.support(request,response);
    }


}
