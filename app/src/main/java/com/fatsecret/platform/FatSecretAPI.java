/*
* FoodScanner: A free and open Food Analyzer (nutritional facts, allergens and chemicals)
*
* FoodScanner is a first-of-a-kind food analyzer offering valuable 
* information such as nutritional facts, allergens and 
* chemicals, about foods using ordinary smartphones.
*
* Authors: D. Stefanidis
* 
* Supervisor: Demetrios Zeinalipour-Yazti
*
* URL: http://foodscanner.cs.ucy.ac.cy
* Contact: foodscanner@cs.ucy.ac.cy
*
* Copyright (c) 2016, Data Management Systems Lab (DMSL), University of Cyprus.
* All rights reserved.
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of
* this software and associated documentation files (the "Software"), to deal in the
* Software without restriction, including without limitation the rights to use, copy,
* modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
* and to permit persons to whom the Software is furnished to do so, subject to the
* following conditions:
*
* The above copyright notice and this permission notice shall be included in all
* copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
* OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
* DEALINGS IN THE SOFTWARE.
*
*/
package com.fatsecret.platform;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import com.fatsecret.platform.Base64;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONException;
import org.json.JSONObject;

public class FatSecretAPI {

	/**
	 * A value FatSecret API issues to you which helps this API identify you
	 */
	final private String APP_KEY;

	/**
	 * A secret FatSecret API issues to you which helps this API establish that it really is you
	 */
	final private String APP_SECRET;

	/**
	 * Request URL
	 * <p>
	 * The URL to make API calls is http://platform.fatsecret.com/rest/server.api
	 */
	final private String APP_URL = "http://platform.fatsecret.com/rest/server.api";

	/**
	 * The signature method allowed by FatSecret API
	 * <p>
	 * They only support "HMAC-SHA1"
	 */
	final private String APP_SIGNATURE_METHOD = "HmacSHA1";

	/**
	 * The HTTP Method supported by FatSecret API
	 * <p>
	 * This API only supports GET method
	 */
	final private String HTTP_METHOD = "GET";

	/**
	 * Constructor to set values for APP_KEY and APP_SECRET
	 *
	 * @param APP_KEY
	 *			A value FatSecret API issues to you which helps this API identify you
	 * @param APP_SECRET
	 *			A secret FatSecret API issues to you which helps this API establish that it really is you
	 */
	public FatSecretAPI(String APP_KEY, String APP_SECRET) {
		this.APP_KEY = APP_KEY;
		this.APP_SECRET = APP_SECRET;
	}

	/**
	 * Returns randomly generated nonce value for calling the request.
	 *
	 * @return the randomly generated value for nonce.
	 */
	public String nonce() {
		Random r = new Random();
		StringBuffer n = new StringBuffer();
		for (int i = 0; i < r.nextInt(8) + 2; i++) {
			n.append(r.nextInt(26) + 'a');
		}
		return n.toString();
	}

	/**
	 * Get all the oauth parameters and other parameters.
	 *
	 * @return an array of parameter values as "key=value" pair
	 */
	public String[] generateOauthParams() {
		String[] a = {
				"oauth_consumer_key=" + APP_KEY,
				"oauth_signature_method=HMAC-SHA1",
				"oauth_timestamp=" + new Long(System.currentTimeMillis() / 1000).toString(),
				"oauth_nonce=" + nonce(),
				"oauth_version=1.0",
				"format=json"
		};
		return a;
	}

	/**
	 * Returns string generated using params and separator
	 *
	 * @param params
	 * 			An array of parameter values as "key=value" pair
	 * @param separator
	 * 			A separator for joining
	 *
	 * @return the string by appending separator after each parameter from params except the last.
	 */
	public String join(String[] params, String separator) {
		StringBuffer b = new StringBuffer();
		for (int i = 0; i < params.length; i++) {
			if (i > 0) {
				b.append(separator);
			}
			b.append(params[i]);
		}
		return b.toString();
	}

	/**
	 * Returns string generated using params and "&" for signature base and normalized parameters
	 *
	 * @param params
	 * 			An array of parameter values as "key=value" pair
	 *
	 * @return the string by appending separator after each parameter from params except the last.
	 */
	public String paramify(String[] params) {
		String[] p = Arrays.copyOf(params, params.length);
		Arrays.sort(p);
		return join(p, "&");
	}

	/**
	 * Returns the percent-encoded string for the given url
	 *
	 * @param url
	 * 			URL which is to be encoded using percent-encoding
	 *
	 * @return encoded url
	 */
	public String encode(String url) {
		if (url == null)
			return "";

		try {
			return URLEncoder.encode(url, "utf-8")
					.replace("+", "%20")
					.replace("!", "%21")
					.replace("*", "%2A")
					.replace("\\", "%27")
					.replace("(", "%28")
					.replace(")", "%29");
		}
		catch (UnsupportedEncodingException wow) {
			throw new RuntimeException(wow.getMessage(), wow);
		}
	}

	/**
	 * Returns signature generated using signature base as text and consumer secret as key
	 *
	 * @param method
	 * 			Http method
	 * @param uri
	 * 			Request URL - http://platform.fatsecret.com/rest/server.api (Always remains the same)
	 * @param params
	 * 			An array of parameter values as "key=value" pair
	 *
	 * @return oauth_signature which will be added to request for calling fatsecret api
	 */
	public String sign(String method, String uri, String[] params) throws UnsupportedEncodingException {

		String encodedURI = encode(uri);
		String encodedParams = encode(paramify(params));

		String[] p = {method, encodedURI, encodedParams};

		String text = join(p, "&");
		String key = APP_SECRET + "&";
	    SecretKey sk = new SecretKeySpec(key.getBytes(), APP_SIGNATURE_METHOD);
	    String sign = "";
	    try {
	      Mac m = Mac.getInstance(APP_SIGNATURE_METHOD);
	      m.init(sk);
	      sign = encode(new String(Base64.encode(m.doFinal(text.getBytes()), Base64.DEFAULT)).trim());
	    } catch(java.security.NoSuchAlgorithmException e) {

	    } catch(java.security.InvalidKeyException e) {

	    }
		return sign;
	}

	/**
	 * Returns JSONObject associated with the food items depending on the search query
	 *
	 *
	 * @return food items at page number '0' based on the query
	 */
    public class RetrieveFoodItems extends AsyncTask<String, String, Pair> {

        private Exception exception;

        @Override
        protected Pair<JSONObject,String> doInBackground(String... query) {
            JSONObject result = new JSONObject();

            List<String> params = new ArrayList<String>(Arrays.asList(generateOauthParams()));
            String[] template = new String[1];
            params.add("method=foods.search");
            params.add("max_results=50");
            params.add("search_expression=" + encode(query[0]));
            Pair<JSONObject,String> p = null;
            try {
                params.add("oauth_signature=" + sign(HTTP_METHOD, APP_URL, params.toArray(template)));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            try {
                URL url = new URL(APP_URL + "?" + paramify(params.toArray(template)));
                URLConnection api = url.openConnection();

                String line;
                StringBuilder builder = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(api.getInputStream()));

                while ((line = reader.readLine()) != null) builder.append(line);

                JSONObject json = new JSONObject(builder.toString());
                result.put("result", json);

                p = Pair.create(result,"OK");
            } catch (java.net.UnknownHostException e) {
                p = Pair.create(result,"Connection Error");
                e.printStackTrace();
            } catch (IOException e) {
                p = Pair.create(result,e.toString());
                e.printStackTrace();
            } catch (JSONException e) {
                p = Pair.create(result,e.toString());
                e.printStackTrace();
            }

            return p;
        }
    }


    public class RetrieveFoodIdUsingBarcode extends AsyncTask<String, String, Pair> {
        @Override
        protected Pair<JSONObject,String> doInBackground(String... barcode) {
            JSONObject result = new JSONObject();

            List<String> params = new ArrayList<String>(Arrays.asList(generateOauthParams()));
            String[] template = new String[1];
            params.add("method=food.find_id_for_barcode");
            params.add("barcode=" + encode(barcode[0]));

            Pair<JSONObject,String> p = null;

            try {
                params.add("oauth_signature=" + sign(HTTP_METHOD, APP_URL, params.toArray(template)));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            try {
                URL url = new URL(APP_URL + "?" + paramify(params.toArray(template)));
                URLConnection api = url.openConnection();

                String line;
                StringBuilder builder = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(api.getInputStream()));

                while ((line = reader.readLine()) != null) builder.append(line);

                JSONObject json = new JSONObject(builder.toString());
                result.put("result", json);

                p = Pair.create(result,"OK");
            } catch (java.net.UnknownHostException e) {
                p = Pair.create(result,"Connection Error");
                e.printStackTrace();
            } catch (IOException e) {
                p = Pair.create(result,e.toString());
                e.printStackTrace();
            } catch (JSONException e) {
                p = Pair.create(result,e.toString());
                e.printStackTrace();
            }

            return p;
        }
    }



    public class RetrieveFoodUsingId extends AsyncTask<String, String, Pair> {
        @Override
        protected Pair<JSONObject,String> doInBackground(String... id) {
            JSONObject result = new JSONObject();

            List<String> params = new ArrayList<String>(Arrays.asList(generateOauthParams()));
            String[] template = new String[1];
            params.add("method=food.get");
            params.add("food_id=" + encode(id[0]));

            Pair<JSONObject,String> p = null;

            try {
                params.add("oauth_signature=" + sign(HTTP_METHOD, APP_URL, params.toArray(template)));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            try {
                URL url = new URL(APP_URL + "?" + paramify(params.toArray(template)));
                URLConnection api = url.openConnection();

                String line;
                StringBuilder builder = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(api.getInputStream()));

                while ((line = reader.readLine()) != null) builder.append(line);

                JSONObject json = new JSONObject(builder.toString());
                result.put("result", json);

                p = Pair.create(result,"OK");
            } catch (java.net.UnknownHostException e) {
                p = Pair.create(result,"Connection Error");
                e.printStackTrace();
            } catch (IOException e) {
                p = Pair.create(result,e.toString());
                e.printStackTrace();
            } catch (JSONException e) {
                p = Pair.create(result,e.toString());
                e.printStackTrace();
            }

            return p;
        }
    }

	/**
	 * Returns JSONObject associated with the food items depending on the search query and page number
	 *
	 * @param query
	 * 			Search terms for querying fatsecret api
	 * @param pageNumber
	 * 			Page Number to search the food items
	 *
	 * @return food items at a particular page number based on the query
	 */
	public JSONObject getFoodItemsAtPageNumber(String query, int pageNumber) throws UnsupportedEncodingException {
		JSONObject result = new JSONObject();

		List<String> params = new ArrayList<String>(Arrays.asList(generateOauthParams()));
		String[] template = new String[1];
		params.add("method=foods.search");
		params.add("max_results=50");
		params.add("page_number="+pageNumber);
		params.add("search_expression=" + encode(query));
		params.add("oauth_signature=" + sign(HTTP_METHOD, APP_URL, params.toArray(template)));

		try {
			URL url = new URL(APP_URL + "?" + paramify(params.toArray(template)));
			URLConnection api = url.openConnection();
			String line;
			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(api.getInputStream()));

			while ((line = reader.readLine()) != null) builder.append(line);

			JSONObject json = new JSONObject(builder.toString());

			result.put("result", json);

		} catch (Exception e) {
			JSONObject error = new JSONObject();
			try {
				error.put("message", "There was an error in processing your request. Please try again later.");
				result.put("error", error);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}

		return result;
	}

	/**
	 * Returns JSONObject associated with the food id with nutritional information
	 *
	 * @param id
	 * 			The ID of the food to retrieve.
	 *
	 * @return food items based on the food id
	 */
	public JSONObject getFoodItem(long id) throws UnsupportedEncodingException {
		JSONObject result = new JSONObject();

		List<String> params = new ArrayList<String>(Arrays.asList(generateOauthParams()));
		String[] template = new String[1];
		params.add("method=food.get");
		params.add("food_id=" + encode(""+id));
		params.add("oauth_signature=" + sign(HTTP_METHOD, APP_URL, params.toArray(template)));

		try {
			URL url = new URL(APP_URL + "?" + paramify(params.toArray(template)));
			URLConnection api = url.openConnection();
			String line;
			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(api.getInputStream()));

			while ((line = reader.readLine()) != null) builder.append(line);

			JSONObject json = new JSONObject(builder.toString());

			result.put("result", json);

		} catch (Exception e) {
			JSONObject error = new JSONObject();
			try {
				error.put("message", "There was an error in processing your request. Please try again later.");
				result.put("error", error);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}

		return result;
	}

	/**
	 * Returns JSONObject associated with the recipes depending on the search query
	 *
	 * @param query
	 * 			Search terms for querying fatsecret api
	 *
	 * @return recipes at page number '0' based on the query
	 */
	public JSONObject getRecipes(String query) throws UnsupportedEncodingException {
		JSONObject result = new JSONObject();
		List<String> params = new ArrayList<String>(Arrays.asList(generateOauthParams()));
		String[] template = new String[1];
		params.add("method=recipes.search");
		params.add("max_results=50");
		params.add("search_expression=" + encode(query));
		params.add("oauth_signature=" + sign(HTTP_METHOD, APP_URL, params.toArray(template)));

		try {
			URL url = new URL(APP_URL + "?" + paramify(params.toArray(template)));
			URLConnection api = url.openConnection();
			String line;
			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(api.getInputStream()));

			while ((line = reader.readLine()) != null) builder.append(line);

			JSONObject json = new JSONObject(builder.toString());
			result.put("result", json);

		} catch (Exception e) {
			JSONObject error = new JSONObject();
			try {
				error.put("message", "There was an error in processing your request. Please try again later.");
				result.put("error", error);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * Returns JSONObject associated with the recipes depending on the search query
	 *
	 * @param query
	 * 			Search terms for querying fatsecret api
	 * @param pageNumber
	 * 			Page Number to search the food items
	 *
	 * @return recipes at a particular page number based on the query
	 */
	public JSONObject getRecipesAtPageNumber(String query, int pageNumber) throws UnsupportedEncodingException {
		JSONObject result = new JSONObject();
		List<String> params = new ArrayList<String>(Arrays.asList(generateOauthParams()));
		String[] template = new String[1];
		params.add("method=recipes.search");
		params.add("max_results=50");
		params.add("page_number="+pageNumber);
		params.add("search_expression=" + encode(query));
		params.add("oauth_signature=" + sign(HTTP_METHOD, APP_URL, params.toArray(template)));

		try {
			URL url = new URL(APP_URL + "?" + paramify(params.toArray(template)));
			URLConnection api = url.openConnection();
			String line;
			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(api.getInputStream()));

			while ((line = reader.readLine()) != null) builder.append(line);

			JSONObject json = new JSONObject(builder.toString());
			result.put("result", json);

		} catch (Exception e) {
			JSONObject error = new JSONObject();
			try {
				error.put("message", "There was an error in processing your request. Please try again later.");
				result.put("error", error);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * Returns JSONObject associated with general information about the recipe item with detailed nutritional information for the standard serving.
	 *
	 * @param id
	 * 			The ID of the recipe to retrieve.
	 *
	 * @return recipe based on the recipe id
	 */
	public JSONObject getRecipe(long id) throws UnsupportedEncodingException {
		JSONObject result = new JSONObject();

		List<String> params = new ArrayList<String>(Arrays.asList(generateOauthParams()));
		String[] template = new String[1];
		params.add("method=recipe.get");
		params.add("recipe_id=" + encode(""+id));
		params.add("oauth_signature=" + sign(HTTP_METHOD, APP_URL, params.toArray(template)));

		try {
			URL url = new URL(APP_URL + "?" + paramify(params.toArray(template)));
			URLConnection api = url.openConnection();
			String line;
			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(api.getInputStream()));

			while ((line = reader.readLine()) != null) builder.append(line);

			JSONObject json = new JSONObject(builder.toString());

			result.put("result", json);

		} catch (Exception e) {
			JSONObject error = new JSONObject();
			try {
				error.put("message", "There was an error in processing your request. Please try again later.");
				result.put("error", error);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		return result;
	}
}
