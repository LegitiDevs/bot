package net.legitimoose.bot.db;

// Source - https://stackoverflow.com/a/5711200
// Posted by Simon
// Retrieved 2026-02-09, License - CC BY-SA 3.0

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

// https://stackoverflow.com/a/5711200
public class MongoUtil {
    public static DBObject encode(JSONArray a) {
        BasicDBList result = new BasicDBList();
        try {
            for (int i = 0; i < a.length(); ++i) {
                Object o = a.get(i);
                if (o instanceof JSONObject) {
                    result.add(encode((JSONObject)o));
                } else if (o instanceof JSONArray) {
                    result.add(encode((JSONArray)o));
                } else {
                    result.add(o);
                }
            }
            return result;
        } catch (JSONException je) {
            return null;
        }
    }

    public static DBObject encode(JSONObject o) {
        BasicDBObject result = new BasicDBObject();
        try {
            Iterator i = o.keys();
            while (i.hasNext()) {
                String k = (String)i.next();
                Object v = o.get(k);
                if (v instanceof JSONArray) {
                    result.put(k, encode((JSONArray)v));
                } else if (v instanceof JSONObject) {
                    result.put(k, encode((JSONObject)v));
                } else {
                    result.put(k, v);
                }
            }
            return result;
        } catch (JSONException je) {
            return null;
        }
    }
}