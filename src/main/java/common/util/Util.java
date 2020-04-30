/*  Copyright (C) 2017  Vincenzo Gulisano
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  Contact: Vincenzo Gulisano info@vincenzogulisano.com
 *
 */

package common.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import common.InputRate;

public class Util {
	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static List<JSONObject> parseControlEvents(String path) {

		List<JSONObject> jsonObjects = new ArrayList<JSONObject>();

		if (path != null) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(path));
				String line;
				while ((line = br.readLine()) != null) {
					jsonObjects.add(new JSONObject(line));
				}
				br.close();

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return jsonObjects;
	}

	public static HashMap<Integer, List<Integer>> getMapping(JSONArray mapArray) {
		HashMap<Integer, List<Integer>> mapping = new HashMap<Integer, List<Integer>>();

		for (int i = 0; i < mapArray.length(); i++) {
			List<Integer> partitions = new ArrayList<Integer>();
			JSONArray partArray = mapArray.getJSONObject(i).getJSONArray("partitions");
			for (int j = 0; j < partArray.length(); j++) {
				partitions.add(partArray.getInt(j));
			}
			mapping.put(mapArray.getJSONObject(i).getInt("thread"), partitions);
		}
		return mapping;
	}

	public static List<InputRate> parseInputRates(String path) {
		List<InputRate> rates = new ArrayList<InputRate>();
		if (path != null) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(path));
				String line;
				while ((line = br.readLine()) != null) {
					String[] val = line.split(",");
					rates.add(
							new InputRate(Long.parseLong(val[0]), Integer.parseInt(val[1]), Integer.parseInt(val[2])));
				}
				br.close();

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return rates;
	}

	public static long lcm(long number1, long number2) {
		if (number1 == 0 || number2 == 0) {
			return 0;
		}
		long absNumber1 = Math.abs(number1);
		long absNumber2 = Math.abs(number2);
		long absHigherNumber = Math.max(absNumber1, absNumber2);
		long absLowerNumber = Math.min(absNumber1, absNumber2);
		long lcm = absHigherNumber;
		while (lcm % absLowerNumber != 0) {
			lcm += absHigherNumber;
		}
		return lcm;
	}

	public static long gcd(long p, long q) {
		if (q == 0) {
			return p;
		}
		return gcd(q, p % q);
	}
}
