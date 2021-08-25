package gov.sandia.gmp.baseobjects.tttables;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import gov.sandia.gmp.seismicbasedata.SeismicBaseData;
import gov.sandia.gmp.util.exceptions.GMPException;

public class TableOfUncertainties extends Table {
	public TableOfUncertainties() {
		super();
	}

	@Override
	public Table read(File inputFile) throws GMPException, IOException {

		this.file = inputFile;

		InputStream inputStream = new SeismicBaseData(inputFile).getInputStream();
		Scanner input = new Scanner(inputStream);

		String line = input.nextLine();
		for (;;) {
			line = line.toLowerCase();
			if (line.contains("model") && line.contains("error"))
				break;

			if (!input.hasNext())
				throw new GMPException(
						"File " + inputFile.getAbsolutePath() + " does not contain any model error information.");

			line = input.nextLine();
		}

		distances = new double[input.nextInt()];
		depths = new double[input.nextInt()];

		// read distances
		if (distances.length > 1)
			for (int i = 0; i < distances.length; i++)
				distances[i] = input.nextDouble();

		// read depths
		if (depths.length > 1)
			for (int iz = 0; iz < depths.length; iz++)
				depths[iz] = input.nextDouble();

		values = new double[depths.length][distances.length];

		// Input the uncertainty values depth by depth
		input.nextLine();
		for (int iz = 0; iz < depths.length; iz++) {
			// Skip depth header line.
			input.nextLine();
			// read values for depth iz
			for (int ix = 0; ix < distances.length; ix++)
				values[iz][ix] = input.nextDouble();
			input.nextLine();
		}

		input.close();
		inputStream.close();

		return this;
	}

	@Override
	public double interpolate(double distance, double depth) {
		if (depths.length == 1 && distances.length == 1)
			return values[0][0];

		if (depths.length == 1) {
			distance = max(distances[0], min(distances[distances.length - 1], distance));
			int x = min(distances.length - 2, hunt(distances, distance));
			double dx = (distance - distances[x]) / (distances[x + 1] - distances[x]);
			return values[0][x] * (1. - dx) + values[0][x + 1] * dx;
		}

		if (distances.length == 1) {
			depth = max(depths[0], min(depths[depths.length - 1], depth));
			int z = min(depths.length - 2, hunt(depths, depth));
			double dz = (depth - depths[z]) / (depths[z + 1] - depths[z]);
			return values[z][0] * (1. - dz) + values[z + 1][0] * dz;
		}

		distance = max(distances[0], min(distances[distances.length - 1], distance));
		int x = min(distances.length - 2, hunt(distances, distance));
		double dx = (distance - distances[x]) / (distances[x + 1] - distances[x]);

		depth = max(depths[0], min(depths[depths.length - 1], depth));
		int z = min(depths.length - 2, hunt(depths, depth));
		double dz = (depth - depths[z]) / (depths[z + 1] - depths[z]);

		return values[z][x] * (1. - dx) * (1. - dz) + values[z][x + 1] * (1. - dz) * dx
				+ values[z + 1][x] * dz * (1. - dx) + values[z + 1][x + 1] * dz * dx;

	}

}
