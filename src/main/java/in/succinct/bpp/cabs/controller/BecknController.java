package in.succinct.bpp.cabs.controller;

import com.venky.core.util.ExceptionUtil;
import com.venky.core.util.ObjectUtil;
import com.venky.swf.controller.Controller;
import com.venky.swf.controller.annotations.RequireLogin;
import com.venky.swf.db.annotations.column.ui.mimes.MimeType;
import com.venky.swf.exceptions.AccessDeniedException;
import com.venky.swf.path.Path;
import com.venky.swf.routing.Config;
import com.venky.swf.views.BytesView;
import com.venky.swf.views.View;
import in.succinct.beckn.Error;
import in.succinct.beckn.Error.Type;
import in.succinct.beckn.Request;
import in.succinct.bpp.cabs.BecknUtil;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class BecknController extends Controller {

    public BecknController(Path path) {
        super(path);
    }

    private final BecknUtil becknUtil = new BecknUtil();


    @RequireLogin(false)
    public View search(){
        return api();
    }
    @RequireLogin(false)
    public View select(){
        return api();
    }
    @RequireLogin(false)
    public View support(){
        return api();
    }


    @RequireLogin(false)
    public View init(){
        return api();
    }
    @RequireLogin(false)
    public View confirm(){
        return api();
    }
    @RequireLogin(false)
    public View status(){
        return api();
    }
    @RequireLogin(false)
    public View cancel(){
        return api();
    }

    @RequireLogin(false)
    public View track(){
        return api();
    }

    @RequireLogin(false)
    public View update(){
        return api();
    }

    public void validateProtocolServer(){
        String accelerator = Config.instance().getProperty("l1.accelerator");
        String token = Config.instance().getProperty(String.format("l1.%s.token",accelerator));
        String value = Config.instance().getProperty(String.format("l1.%s.value",accelerator));
        if (token != null && value !=null){
            if (!ObjectUtil.equals(getPath().getHeader(token),value)){
                throw  new AccessDeniedException("Unauthorized");
            }
        }
    }

    /** this is the api called by the protocol adaptor */
    @RequireLogin(false)
    public View api(){
        validateProtocolServer();
        Request reply = new Request();
        try  {
            JSONObject object = (JSONObject) JSONValue.parse(new InputStreamReader(getPath().getInputStream()));
            Request request = new Request(object); // Request parser;;

            Method method = becknUtil.getClass().getMethod(request.getContext().getAction(),Request.class,Request.class);
            //Based on action, we call the right method here.
            becknUtil.createReplyContext(request,reply);
            method.invoke(becknUtil,request,reply);
        }catch (Exception ex){
            Config.instance().getLogger(getClass().getName()).log(Level.WARNING,"BPP Failed" , ex);
            reply.rm("message");
            reply.setError(new Error());
            reply.getError().setCode(ExceptionUtil.getRootCause(ex).toString());
            reply.getError().setMessage(ExceptionUtil.getRootCause(ex).toString());
            reply.getError().setType(Type.CORE_ERROR);
            // Do nothing;
        }
        return new BytesView(getPath(),reply.toString().getBytes(StandardCharsets.UTF_8),MimeType.APPLICATION_JSON);

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
