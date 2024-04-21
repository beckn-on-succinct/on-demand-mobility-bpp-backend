package in.succinct.bpp.cabs.extensions;

import com.venky.core.string.StringUtil;
import com.venky.core.util.ObjectUtil;
import com.venky.extension.Extension;
import com.venky.extension.Registry;
import com.venky.swf.path.Path;
import in.succinct.beckn.Context;
import in.succinct.beckn.Request;
import in.succinct.beckn.Subscriber;
import in.succinct.bpp.cabs.adaptor.ECommerceAdaptor;
import in.succinct.bpp.core.adaptor.CommerceAdaptor;
import in.succinct.bpp.core.adaptor.api.NetworkApiAdaptor;
import in.succinct.onet.core.adaptor.NetworkAdaptor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Webhook implements Extension {
    static {
        Registry.instance().registerExtension("in.succinct.bpp.shell.hook",new Webhook());
    }
    @Override
    public void invoke(Object... objects) {
        CommerceAdaptor adaptor = (CommerceAdaptor) objects[0];
        NetworkAdaptor networkAdaptor = (NetworkAdaptor)objects[1];
        Path path = (Path) objects[2];
        if (!(adaptor instanceof ECommerceAdaptor)) {
            return;
        }
        ECommerceAdaptor eCommerceAdaptor = (ECommerceAdaptor) adaptor;
        try {
            hook(eCommerceAdaptor, networkAdaptor,path);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    public void hook(ECommerceAdaptor eCommerceAdaptor, NetworkAdaptor networkAdaptor,Path path) throws Exception{
        if (path.getApplication() == null){
            return;
        }
        String payload = StringUtil.read(path.getInputStream());

        if (path.action().equals("order_hook")){
            String event = path.parameter();


            final Request request = new Request(payload);
            Context context = request.getContext();
            context.setBppId(eCommerceAdaptor.getSubscriber().getSubscriberId());
            context.setBppUri(eCommerceAdaptor.getSubscriber().getSubscriberUrl());
            context.setTimestamp(new Date());
            context.setAction("on_status");
            if (ObjectUtil.isVoid(context.getBapUri())) {
                List<Subscriber> subscriberList =networkAdaptor.lookup(new Subscriber(){{
                        setSubscriberId(context.getBapId());
                        setType(Subscriber.SUBSCRIBER_TYPE_BAP);
                }}, true);
                if (!subscriberList.isEmpty()){
                    context.setBapUri(subscriberList.get(0).getSubscriberUrl());
                }
            }
            context.setCountry(eCommerceAdaptor.getSubscriber().getCountry());
            context.setCity(eCommerceAdaptor.getSubscriber().getCity());
            context.setCoreVersion("0.9.1");
            context.setDomain(eCommerceAdaptor.getSubscriber().getDomain());
            //Fill any other attributes needed.
            //Send unsolicited on_status.
            context.setMessageId(UUID.randomUUID().toString());
            ((NetworkApiAdaptor)networkAdaptor.getApiAdaptor()).callback(eCommerceAdaptor,request);
        }
    }
}
