package com.browserstack.json;




public class CookieList {


    public static JSONObject toJSONObject(String string) throws JSONException {
        JSONObject jo = new JSONObject();
        JSONTokener x = new JSONTokener(string);
        while (x.more()) {
            String name = Cookie.unescape(x.nextTo('='));
            x.next('=');
            jo.put(name, Cookie.unescape(x.nextTo(';')));
            x.next();
        }
        return jo;
    }


    public static String toString(JSONObject jo) throws JSONException {
        boolean             b = false;
        final StringBuilder sb = new StringBuilder();

        for (final String key : jo.keySet()) {
            final Object value = jo.opt(key);
            if (!JSONObject.NULL.equals(value)) {
                if (b) {
                    sb.append(';');
                }
                sb.append(Cookie.escape(key));
                sb.append("=");
                sb.append(Cookie.escape(value.toString()));
                b = true;
            }
        }
        return sb.toString();
    }
}
