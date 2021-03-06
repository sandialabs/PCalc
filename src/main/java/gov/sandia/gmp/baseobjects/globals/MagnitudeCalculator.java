/**
 * Copyright 2009 Sandia Corporation. Under the terms of Contract
 * DE-AC04-94AL85000 with Sandia Corporation, the U.S. Government
 * retains certain rights in this software.
 * 
 * BSD Open Source License.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *    * Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the name of Sandia National Laboratories nor the names of its
 *      contributors may be used to endorse or promote products derived from
 *      this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package gov.sandia.gmp.baseobjects.globals;

import java.util.HashMap;

import gov.sandia.gmp.baseobjects.tttables.OutOfRangeException;
import gov.sandia.gmp.baseobjects.tttables.TableOfObservables;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Arrival;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Origin;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.AssocExtended;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.OriginExtended;

public class MagnitudeCalculator {

	// Veith-Clawson magnitude tables.
	static private final TableOfObservables attenuationTable;

	static {

		double[] distances = new double[] { 0.00, 1.00, 2.00, 3.00, 4.00, 5.00, 6.00, 7.00, 8.00, 9.00, 10.00, 11.00,
				12.00, 13.00, 14.00, 15.00, 16.00, 17.00, 18.00, 19.00, 20.00, 21.00, 22.00, 23.00, 24.00, 25.00, 26.00,
				27.00, 28.00, 29.00, 30.00, 31.00, 32.00, 33.00, 34.00, 35.00, 36.00, 37.00, 38.00, 39.00, 40.00, 41.00,
				42.00, 43.00, 44.00, 45.00, 46.00, 47.00, 48.00, 49.00, 50.00, 51.00, 52.00, 53.00, 54.00, 55.00, 56.00,
				57.00, 58.00, 59.00, 60.00, 61.00, 62.00, 63.00, 64.00, 65.00, 66.00, 67.00, 68.00, 69.00, 70.00, 71.00,
				72.00, 73.00, 74.00, 75.00, 76.00, 77.00, 78.00, 79.00, 80.00, 81.00, 82.00, 83.00, 84.00, 85.00, 86.00,
				87.00, 88.00, 89.00, 90.00, 91.00, 92.00, 93.00, 94.00, 95.00, 96.00, 97.00, 98.00, 99.00, 100.00 };

		double[] depths = new double[] { 0.00, 15.00, 40.00, 100.00, 200.00, 300.00, 400.00, 500.00, 600.00, 700.00,
				800.00 };

		double[][] values = new double[][] {
				{ 0.301, 1.191, 2.371, 2.501, 2.851, 3.061, 3.201, 3.321, 3.401, 3.451, 3.491, 3.531, 3.551, 3.561,
						3.561, 3.551, 3.511, 3.401, 3.281, 3.091, 3.071, 3.101, 3.151, 3.241, 3.341, 3.451, 3.551,
						3.651, 3.721, 3.741, 3.721, 3.681, 3.661, 3.661, 3.651, 3.641, 3.641, 3.641, 3.631, 3.631,
						3.621, 3.621, 3.621, 3.631, 3.631, 3.641, 3.641, 3.651, 3.661, 3.661, 3.671, 3.671, 3.681,
						3.691, 3.691, 3.701, 3.701, 3.711, 3.721, 3.721, 3.731, 3.741, 3.741, 3.751, 3.751, 3.761,
						3.761, 3.771, 3.781, 3.781, 3.791, 3.801, 3.801, 3.811, 3.811, 3.821, 3.831, 3.831, 3.841,
						3.841, 3.851, 3.861, 3.871, 3.881, 3.891, 3.811, 3.941, 3.961, 3.981, 4.021, 4.061, 4.101,
						4.151, 4.211, 4.281, 4.361, 4.441, 4.521, 4.601, 4.681, 4.761 },
				{ -1.519, 3.221, 2.321, 2.531, 2.831, 3.031, 3.161, 3.271, 3.341, 3.391, 3.431, 3.461, 3.481, 3.491,
						3.491, 3.471, 3.411, 3.301, 3.151, 3.011, 3.011, 3.051, 3.121, 3.191, 3.291, 3.391, 3.491,
						3.571, 3.631, 3.651, 3.631, 3.611, 3.591, 3.581, 3.571, 3.561, 3.551, 3.551, 3.541, 3.541,
						3.541, 3.541, 3.541, 3.541, 3.541, 3.551, 3.551, 3.561, 3.561, 3.571, 3.581, 3.581, 3.591,
						3.601, 3.601, 3.611, 3.621, 3.621, 3.631, 3.641, 3.641, 3.651, 3.651, 3.661, 3.661, 3.671,
						3.681, 3.681, 3.691, 3.691, 3.701, 3.701, 3.711, 3.721, 3.721, 3.731, 3.731, 3.741, 3.741,
						3.751, 3.751, 3.761, 3.761, 3.771, 3.791, 3.811, 3.831, 3.851, 3.881, 3.921, 3.961, 4.001,
						4.051, 4.111, 4.191, 4.271, 4.351, 4.431, 4.511, 4.591, 4.671 },
				{ -1.139, 2.461, 2.591, 2.831, 3.021, 3.171, 3.291, 3.381, 3.431, 3.471, 3.491, 3.501, 3.511, 3.511,
						3.511, 3.471, 3.381, 3.241, 3.051, 2.931, 2.921, 2.951, 3.011, 3.091, 3.171, 3.271, 3.361,
						3.431, 3.481, 3.491, 3.501, 3.501, 3.491, 3.481, 3.461, 3.451, 3.441, 3.431, 3.421, 3.421,
						3.411, 3.411, 3.411, 3.411, 3.411, 3.411, 3.421, 3.421, 3.431, 3.441, 3.441, 3.451, 3.461,
						3.461, 3.471, 3.481, 3.481, 3.491, 3.491, 3.501, 3.501, 3.511, 3.511, 3.521, 3.521, 3.531,
						3.541, 3.541, 3.551, 3.551, 3.561, 3.561, 3.571, 3.571, 3.581, 3.581, 3.591, 3.591, 3.601,
						3.601, 3.611, 3.611, 3.621, 3.631, 3.641, 3.661, 3.681, 3.701, 3.741, 3.781, 3.821, 3.861,
						3.911, 3.971, 4.051, 4.131, 4.211, 4.291, 4.371, 4.451, 4.531 },
				{ -0.499, -0.009, 0.691, 1.241, 1.661, 1.981, 2.251, 2.481, 2.671, 2.821, 2.931, 3.021, 3.091, 3.141,
						3.151, 3.091, 2.981, 2.841, 2.761, 2.751, 2.781, 2.841, 2.911, 2.991, 3.081, 3.181, 3.271,
						3.341, 3.371, 3.381, 3.381, 3.371, 3.361, 3.351, 3.341, 3.331, 3.321, 3.311, 3.301, 3.301,
						3.291, 3.291, 3.291, 3.291, 3.291, 3.301, 3.301, 3.311, 3.321, 3.321, 3.331, 3.341, 3.341,
						3.351, 3.361, 3.361, 3.371, 3.381, 3.381, 3.391, 3.401, 3.401, 3.411, 3.421, 3.421, 3.431,
						3.441, 3.441, 3.451, 3.461, 3.461, 3.461, 3.471, 3.471, 3.481, 3.481, 3.491, 3.491, 3.501,
						3.501, 3.501, 3.501, 3.511, 3.531, 3.551, 3.571, 3.601, 3.621, 3.641, 3.681, 3.721, 3.761,
						3.811, 3.871, 3.951, 4.031, 4.111, 4.191, 4.271, 4.351, 4.431 },
				{ -0.059, 0.111, 0.471, 0.851, 1.191, 1.491, 1.761, 1.991, 2.181, 2.331, 2.441, 2.511, 2.551, 2.561,
						2.531, 2.481, 2.451, 2.461, 2.491, 2.541, 2.601, 2.681, 2.761, 2.861, 2.961, 3.041, 3.101,
						3.131, 3.141, 3.151, 3.151, 3.141, 3.131, 3.121, 3.111, 3.101, 3.101, 3.091, 3.091, 3.091,
						3.081, 3.081, 3.091, 3.091, 3.101, 3.111, 3.111, 3.121, 3.131, 3.131, 3.141, 3.151, 3.161,
						3.161, 3.171, 3.181, 3.181, 3.191, 3.201, 3.211, 3.211, 3.221, 3.231, 3.231, 3.241, 3.251,
						3.251, 3.261, 3.271, 3.281, 3.281, 3.291, 3.301, 3.301, 3.311, 3.311, 3.321, 3.321, 3.321,
						3.331, 3.331, 3.331, 3.341, 3.351, 3.361, 3.381, 3.401, 3.441, 3.481, 3.521, 3.561, 3.611,
						3.671, 3.741, 3.821, 3.901, 3.981, 4.061, 4.141, 4.221, 4.301 },
				{ 0.201, 0.291, 0.491, 0.741, 0.991, 1.231, 1.441, 1.621, 1.771, 1.891, 1.991, 2.061, 2.091, 2.121,
						2.161, 2.201, 2.251, 2.311, 2.371, 2.441, 2.521, 2.601, 2.691, 2.781, 2.861, 2.921, 2.961,
						2.981, 2.991, 2.991, 2.981, 2.971, 2.961, 2.961, 2.951, 2.941, 2.941, 2.941, 2.931, 2.931,
						2.931, 2.941, 2.941, 2.951, 2.951, 2.961, 2.971, 2.971, 2.981, 2.991, 3.001, 3.001, 3.011,
						3.021, 3.031, 3.031, 3.041, 3.051, 3.061, 3.061, 3.071, 3.081, 3.091, 3.101, 3.101, 3.111,
						3.121, 3.131, 3.141, 3.141, 3.151, 3.161, 3.161, 3.171, 3.181, 3.181, 3.191, 3.191, 3.201,
						3.201, 3.201, 3.211, 3.221, 3.241, 3.261, 3.291, 3.311, 3.331, 3.371, 3.411, 3.451, 3.501,
						3.561, 3.641, 3.721, 3.801, 3.881, 3.961, 4.041, 4.121, 4.201 },
				{ 0.381, 0.421, 0.551, 0.711, 0.901, 1.081, 1.251, 1.401, 1.531, 1.651, 1.751, 1.841, 1.921, 1.981,
						2.041, 2.101, 2.171, 2.251, 2.321, 2.401, 2.481, 2.571, 2.651, 2.731, 2.801, 2.841, 2.861,
						2.871, 2.861, 2.841, 2.831, 2.821, 2.811, 2.811, 2.811, 2.811, 2.811, 2.811, 2.811, 2.811,
						2.821, 2.821, 2.831, 2.841, 2.841, 2.851, 2.861, 2.871, 2.871, 2.881, 2.891, 2.901, 2.911,
						2.921, 2.931, 2.941, 2.941, 2.951, 2.961, 2.971, 2.981, 2.981, 2.991, 3.001, 3.011, 3.021,
						3.031, 3.041, 3.041, 3.051, 3.061, 3.071, 3.071, 3.081, 3.091, 3.091, 3.101, 3.101, 3.111,
						3.111, 3.111, 3.131, 3.151, 3.171, 3.191, 3.211, 3.231, 3.251, 3.291, 3.331, 3.381, 3.431,
						3.501, 3.581, 3.661, 3.741, 3.821, 3.901, 3.981, 4.061, 4.141 },
				{ 0.521, 0.551, 0.631, 0.751, 0.881, 1.031, 1.171, 1.301, 1.421, 1.541, 1.641, 1.731, 1.821, 1.901,
						1.981, 2.061, 2.141, 2.221, 2.301, 2.371, 2.451, 2.521, 2.581, 2.631, 2.671, 2.691, 2.691,
						2.681, 2.681, 2.681, 2.671, 2.671, 2.671, 2.671, 2.671, 2.671, 2.671, 2.681, 2.681, 2.691,
						2.701, 2.701, 2.711, 2.721, 2.731, 2.731, 2.741, 2.751, 2.761, 2.771, 2.781, 2.791, 2.801,
						2.811, 2.821, 2.831, 2.841, 2.841, 2.851, 2.861, 2.871, 2.881, 2.891, 2.901, 2.911, 2.921,
						2.921, 2.931, 2.941, 2.951, 2.961, 2.971, 2.981, 2.981, 2.991, 3.001, 3.001, 3.001, 3.011,
						3.011, 3.021, 3.031, 3.041, 3.061, 3.081, 3.101, 3.131, 3.171, 3.211, 3.251, 3.301, 3.361,
						3.431, 3.511, 3.591, 3.671, 3.751, 3.831, 3.911, 3.991, 4.071 },
				{ 0.641, 0.661, 0.721, 0.801, 0.911, 1.031, 1.141, 1.251, 1.361, 1.471, 1.571, 1.671, 1.771, 1.861,
						1.941, 2.021, 2.101, 2.171, 2.241, 2.311, 2.371, 2.431, 2.471, 2.491, 2.501, 2.501, 2.491,
						2.491, 2.481, 2.481, 2.481, 2.491, 2.501, 2.511, 2.521, 2.531, 2.541, 2.541, 2.551, 2.561,
						2.571, 2.581, 2.591, 2.601, 2.611, 2.621, 2.631, 2.641, 2.651, 2.661, 2.671, 2.681, 2.691,
						2.701, 2.711, 2.721, 2.731, 2.741, 2.751, 2.761, 2.771, 2.781, 2.791, 2.801, 2.811, 2.821,
						2.831, 2.841, 2.851, 2.861, 2.861, 2.871, 2.881, 2.891, 2.891, 2.901, 2.911, 2.911, 2.921,
						2.921, 2.931, 2.941, 2.961, 2.981, 3.001, 3.031, 3.061, 3.101, 3.141, 3.181, 3.241, 3.301,
						3.381, 3.461, 3.541, 3.621, 3.701, 3.781, 3.861, 3.941, 4.021 },
				{ 0.741, 0.761, 0.801, 0.871, 0.951, 1.051, 1.141, 1.241, 1.341, 1.441, 1.531, 1.621, 1.711, 1.791,
						1.871, 1.941, 2.011, 2.081, 2.141, 2.181, 2.211, 2.231, 2.241, 2.241, 2.251, 2.261, 2.271,
						2.281, 2.291, 2.301, 2.311, 2.331, 2.341, 2.351, 2.361, 2.371, 2.391, 2.401, 2.411, 2.421,
						2.431, 2.451, 2.461, 2.471, 2.481, 2.491, 2.511, 2.521, 2.531, 2.541, 2.561, 2.571, 2.581,
						2.591, 2.611, 2.621, 2.631, 2.641, 2.651, 2.671, 2.681, 2.691, 2.701, 2.711, 2.721, 2.731,
						2.741, 2.751, 2.761, 2.771, 2.781, 2.791, 2.791, 2.801, 2.801, 2.811, 2.821, 2.821, 2.821,
						2.831, 2.841, 2.861, 2.881, 2.901, 2.931, 2.961, 2.991, 3.031, 3.071, 3.121, 3.181, 3.251,
						3.331, 3.411, 3.491, 3.571, 3.651, 3.731, 3.811, 3.891, 3.971 },
				{ 0.821, 0.831, 0.861, 0.921, 0.981, 1.061, 1.141, 1.221, 1.301, 1.391, 1.471, 1.551, 1.621, 1.691,
						1.751, 1.801, 1.851, 1.901, 1.941, 1.981, 2.011, 2.041, 2.061, 2.081, 2.101, 2.121, 2.141,
						2.161, 2.181, 2.201, 2.221, 2.241, 2.261, 2.271, 2.291, 2.301, 2.321, 2.341, 2.351, 2.371,
						2.381, 2.391, 2.411, 2.421, 2.431, 2.451, 2.461, 2.471, 2.481, 2.491, 2.511, 2.521, 2.531,
						2.541, 2.551, 2.561, 2.571, 2.591, 2.601, 2.611, 2.621, 2.631, 2.641, 2.651, 2.661, 2.671,
						2.681, 2.691, 2.701, 2.711, 2.721, 2.731, 2.741, 2.751, 2.761, 2.771, 2.771, 2.781, 2.781,
						2.791, 2.801, 2.821, 2.851, 2.881, 2.911, 2.951, 2.991, 3.031, 3.081, 3.141, 3.211, 3.271,
						3.331, 3.411, 3.491, 3.571, 3.651, 3.731, 3.811, 3.891, 3.971 } };

		attenuationTable = new TableOfObservables(distances, depths, values);
	}

	static public double getMagnitude(OriginExtended origin) {
		// Classic mb formula using Veith-Clawson attenuation, log10(amp/period) +
		// attenuation(distance, source depth)
		// The attenuation function is interpolated from a table
		double mb = 0;
		int mb_arrival_cnt = 0;

		HashMap<String, Integer> staIndex = new HashMap<String, Integer>();
		int nSta = 0;
		for (AssocExtended assoc : origin.getAssocs().values()) {
			Integer index = staIndex.get(assoc.getSta());
			if (index == null)
				staIndex.put(assoc.getSta(), nSta++);
		}

		// find the arrival from each station with the highest amplitude
		double[] highestAmpPer = new double[nSta];
		AssocExtended[] highestAmpPerAssoc = new AssocExtended[nSta];

		double amp_per_ratio;
		for (AssocExtended assoc : origin.getAssocs().values()) {
			int index = staIndex.get(assoc.getSta());

//			// use only array stations if so specified
//			if (magEstArrayOnly)
//				if (receivers.get(arrival.staPhaseIndex).getStaType() != StaType.ARRAY)
//					continue;

			// check whether an arrival from the same station has already been seen
			if (assoc.getArrival().getPer() > 0)
				amp_per_ratio = assoc.getArrival().getAmp() / assoc.getArrival().getPer();
			else
				amp_per_ratio = 0;
			if (amp_per_ratio > highestAmpPer[staIndex.get(assoc.getSta())]) {
				highestAmpPer[index] = amp_per_ratio;
				highestAmpPerAssoc[index] = assoc;
			}
		}

		// table extends from 0 to 100 degrees distance and 0 to 800 km depth.

		double depth = origin.getDepth();
		if (depth < 0)
			depth = 0;
		else if (depth > 800)
			depth = 800;

		// estimate magnitude from highest amplitude/period arrivals
		for (int i = 0; i < staIndex.size(); i++)
			if (highestAmpPerAssoc[i] != null && highestAmpPer[i] > 0) {
				try {
					mb += (Math.log10(highestAmpPer[i])
							+ attenuationTable.interpolate(highestAmpPerAssoc[i].getDelta(), depth));
					mb_arrival_cnt++;
				} catch (Exception e) {
					/* ignore out of range values */}
			}

		return mb_arrival_cnt > 0 ? mb / mb_arrival_cnt : Origin.MB_NA;
	}

	static public double getMagnitude(double distanceDeg, double depthKm, double amplitude, double period)
			throws OutOfRangeException {
		if (depthKm < 0)
			depthKm = 0;
		try {
			return Math.log10(amplitude / period) + attenuationTable.interpolate(distanceDeg, depthKm);
		} catch (Exception e) {
			return Origin.MB_NA;
		}
	}

	static public double getAmplitude(double mb, double distanceDeg, double depthKm, double periodSec)
			throws Exception {
		if (depthKm < 0)
			depthKm = 0;
		try {
			return periodSec * Math.pow(10., mb - attenuationTable.interpolate(distanceDeg, depthKm));
		} catch (Exception e) {
			return Arrival.AMP_NA;
		}
	}

}
