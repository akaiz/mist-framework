package com.mist;

import com.github.kevinsawicki.http.HttpRequest;
import org.json.JSONArray;
import org.leibnizcenter.xml.TerseJson;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * Created by agabaisaac on 12/28/16.
 */
@RestController
public class TaskController {
    private static final TerseJson.WhitespaceBehaviour COMPACT_WHITE_SPACE = TerseJson.WhitespaceBehaviour.Compact;
    @RequestMapping("/")
    public String home() {
        return "Client is Ready ";
    }
    @RequestMapping(method = GET, path = "/xml/{town}")
    public String xml(@PathVariable String town) {
        final String uri = "http://www.ilmateenistus.ee/ilma_andmed/xml/observations.php";

        String result= HttpRequest.get(uri).body();

//        System.out.println(result);

        JSONObject xmlJSONObj = null;
        try {
            xmlJSONObj = XML.toJSONObject(result);
            String jsonPrettyPrintString = xmlJSONObj.toString(11);

            JSONObject observations=xmlJSONObj.getJSONObject("observations");
            JSONArray jsonArray = observations.getJSONArray("station");

           for (int i=0;i<jsonArray.length();i++){
               JSONObject jsonObject = jsonArray.getJSONObject(i);
               if(jsonObject.get("name").equals(town)){
                   return jsonObject.toString();
               }
           }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}

