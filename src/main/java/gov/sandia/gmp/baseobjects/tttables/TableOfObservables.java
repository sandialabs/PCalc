package gov.sandia.gmp.baseobjects.tttables;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class TableOfObservables extends Table {
	public TableOfObservables() {
		super();
	}

	public TableOfObservables(double[] distances, double[] depths, double[][] values) {
		super();
		this.distances = distances;
		this.depths = depths;
		this.values = values;
	}

	@Override
	public Table read(File inputFile) throws FileNotFoundException {
		this.file = inputFile;

		Scanner input = new Scanner(inputFile);

		// skip first line. comment
		input.nextLine();

		// get number of depth samples
		int n = input.nextInt();
		depths = new double[n];

		input.nextLine();

		// read depths
		for (int iz = 0; iz < depths.length; iz++)
			depths[iz] = input.nextDouble();

		input.nextLine();

		// get number of distance samples
		distances = new double[input.nextInt()];

		input.nextLine();

		// read distances
		for (int i = 0; i < distances.length; i++)
			distances[i] = input.nextDouble();

		input.nextLine();

		values = new double[depths.length][distances.length];

		// Input the tau values depth by depth

		for (int iz = 0; iz < depths.length; iz++) {
			// Skip depth header line.
			input.nextLine();
			// read each line for depth iz
			for (int ix = 0; ix < distances.length; ix++)
				values[iz][ix] = input.nextDouble();
			input.nextLine();
		}

		input.close();

		return this;
	}

	@Override
	public double interpolate(double distance, double depth) throws OutOfRangeException {
		int x = hunt(distances, distance);
		if (x < 0 || x >= distances.length - 1)
			throw new OutOfRangeException(
					String.format("File = %s. Distance %1.6f is out of range.", file.getAbsolutePath(), distance));

		int z = hunt(depths, depth);
		if (z < 0 || z >= depths.length - 1)
			throw new OutOfRangeException(
					String.format("File = %s. Depth %1.6f is out of range.", file.getAbsolutePath(), depth));

		double dx = (distance - distances[x]) / (distances[x + 1] - distances[x]);

		double dz = (depth - depths[z]) / (depths[z + 1] - depths[z]);

		return values[z][x] * (1. - dx) * (1. - dz) + values[z][x + 1] * (1. - dz) * dx
				+ values[z + 1][x] * dz * (1. - dx) + values[z + 1][x + 1] * dz * dx;

	}

}
