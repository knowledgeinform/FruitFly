package edu.jhuapl.nstd.swarm;

import java.util.logging.*;

import edu.jhuapl.jlib.math.*;
import edu.jhuapl.nstd.swarm.util.ByteManipulator;
import java.awt.*;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class CloudGeocentricMatrix extends PrimitiveTypeGeocentricMatrix {

	protected float[][] _altMatrix;
	
	public CloudGeocentricMatrix(PrimitiveTypeGeocentricMatrix.Header header) {
		super(header);
		_altMatrix = new float[cols][rows];
	}

	public CloudGeocentricMatrix(PrimitiveTypeGeocentricMatrix matrix) {
		super(matrix.getHeader());
		_altMatrix = new float[cols][rows];
	}

	public CloudGeocentricMatrix() {
		super();
		_altMatrix = new float[cols][rows];
	}

	public double getAltitudeAsDouble(int col, int row) {
		return _altMatrix[col][row];
	}

	public Altitude getAltitude(int col, int row) {
		double alt = getAltitudeAsDouble(col, row);
		return new Altitude(alt, Length.METERS);
	}

	public void setAltitude(int col, int row, float alt) {
		_altMatrix[col][row] = alt;
	}

	public void setAltitude(Point p, float alt) {
		setAltitude(p.x, p.y, alt);
	}

	public boolean add(PrimitiveTypeGeocentricMatrix matrix) {
		Logger.getLogger("GLOBAL").info("adding clouds together detection");
		float newVal = 0;
		CloudGeocentricMatrix cm = null;
		if (matrix instanceof CloudGeocentricMatrix) {
			cm = (CloudGeocentricMatrix)matrix;
		}
		
		if (this.getXSize() != matrix.getXSize() || this.getYSize() != matrix.getYSize() ||
						!getSWCorner().equals(matrix.getSWCorner()) || !getNECorner().equals(matrix.getNECorner())) {
			for (int x = 0; x < getXSize(); x++) {
				for (int y = 0; y < getYSize(); y++) {
					Point matrixPoint = matrix.getPoint(this.getPosition(x, y));
					if (matrixPoint == null) {
						continue;
					}
					float val = this.get(x, y);
					float matrixVal = matrix.get(matrixPoint);

					if (val > matrixVal) {
						this.set(x, y, val);
						this.setAltitude(x, y, (float)this.getAltitudeAsDouble(x, y));

						newVal = val;
					} else if (matrixVal > val) {
						if (matrixVal > maxVal.value) {
							maxVal.value = matrixVal;
							maxVal.index.setLocation(x, y);
						}

						this.set(x, y, matrixVal);
						if (cm != null) {
							this.setAltitude(x, y, (float)cm.getAltitudeAsDouble(x, y));
						}
						
						newVal = matrixVal;
					}
				}
			}
			return true;
		} else {
			for (int x = 0; x < getXSize(); x++) {
				for (int y = 0; y < getYSize(); y++) {
					float val = this.get(x, y);
					float matrixVal = matrix.get(x, y);

					if (val > matrixVal) {
						this.set(x, y, val);
						this.setAltitude(x, y, (float)this.getAltitudeAsDouble(x, y));
						newVal = val;
					} else if (matrixVal > val) {
						if (matrixVal > maxVal.value) {
							maxVal.value = matrixVal;
							maxVal.index.setLocation(x, y);
						}

						this.set(x, y, matrixVal);
						if (cm != null) {
							this.setAltitude(x, y, (float)cm.getAltitudeAsDouble(x, y));
						}
						newVal = matrixVal;
					}
				}
			}
			return false;
		}
	}

	public void writeExternal(DataOutput out) throws IOException {
		super.writeExternal(out);
		byte[] bytes = new byte[rows * cols * 4];

		int index = 0;
		for (int x=0; x<getXSize(); x++) {
			for (int y=0; y<getYSize(); y++) {
				index = ByteManipulator.addFloat(bytes, (float)this.getAltitudeAsDouble(x, y), index, false);
			}
		}

		out.write(bytes);
	}

	public void readExternal(DataInput in) throws IOException {
		super.readExternal(in);
		_altMatrix = new float[cols][rows];
		byte[] bytes = new byte[rows * cols * 4];
		in.readFully(bytes);
		int index = 0;
		for (int x=0; x<cols; x++) {
			for (int y=0; y<rows; y++) {
				setAltitude(x,y,ByteManipulator.getFloat(bytes, index, false));
				index += 4;
			}
		}
	}
}
