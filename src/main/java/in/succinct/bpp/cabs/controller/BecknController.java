package in.succinct.bpp.cabs.controller;

import com.venky.core.util.ObjectUtil;
import com.venky.swf.controller.Controller;
import com.venky.swf.db.annotations.column.ui.mimes.MimeType;
import com.venky.swf.path.Path;
import com.venky.swf.views.BytesView;
import com.venky.swf.views.View;
import in.succinct.beckn.Context;
import in.succinct.beckn.Request;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

public class BecknController extends Controller {
    public BecknController(Path path) {
        super(path);
    }
    public void search(Request request,Request reply){

    }
    private void createReplyContext(Request from , Request to){
        Context newContext = ObjectUtil.clone(from.getContext());
        String  action = from.getContext().getAction();

        if (action.startsWith("get_")){
            newContext.setAction(action.substring(4));
        }else {
            newContext.setAction("on_" + from.getContext().getAction());
        }
        to.setContext(newContext);
    }

    /** this is the api called by the protocol adaptor */
    public View api(){
        try  {
            JSONObject object = (JSONObject) JSONValue.parse(new InputStreamReader(getPath().getInputStream()));
            Request request = new Request(object); // Request parser;;

            Method method = getClass().getMethod(request.getContext().getAction(),Request.class,Request.class);
            //Based on action, we call the right method here.
            Request reply = new Request();
            createReplyContext(request,reply);
            method.invoke(this,request,reply);
            return new BytesView(getPath(),reply.toString().getBytes(StandardCharsets.UTF_8),MimeType.APPLICATION_JSON);
        }catch (Exception ex){
            // Do nothing;
        }
        return no_content();

    }
    protected View no_content(){
        return new BytesView(getPath(),new byte[]{}, MimeType.APPLICATION_JSON){
            @Override
            public void write() throws IOException {
                super.write(HttpServletResponse.SC_NO_CONTENT);
            }
        };
    }
}
