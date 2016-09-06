package me.ialistannen.math;

import org.bukkit.util.Vector;

import java.util.Objects;

/**
 * A class to deal with spherical coordinates
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class SphericalCoords implements Cloneable {

	private double theta, phi, rho;

	/**
	 * @param rho   The length of the vector
	 * @param theta The angle on the x - y plane
	 * @param phi   The angle between the z and the direct line
	 */
	public SphericalCoords(double rho, double theta, double phi) {
		this.rho = rho;
		this.theta = theta;
		this.phi = phi;
	}

	/**
	 * @return Rho
	 */
	public double getRho() {
		return rho;
	}

	/**
	 * @param rho The new rho
	 */
	public void setRho(double rho) {
		this.rho = rho;
	}

	/**
	 * @return Theta in radian
	 */
	public double getTheta() {
		return theta;
	}

	/**
	 * @return Theta in degrees
	 */
	public double getThetaDegrees() {
		return Math.toDegrees(theta);
	}

	/**
	 * @param theta Theta in radians
	 */
	public void setTheta(double theta) {
		this.theta = theta;
	}

	/**
	 * @return Phi in radian
	 */
	public double getPhi() {
		return phi;
	}

	/**
	 * @return Phi in degrees
	 */
	public double getPhiDegree() {
		return Math.toDegrees(phi);
	}

	/**
	 * @param phi Phi in radian
	 */
	public void setPhi(double phi) {
		this.phi = phi;
	}

	/**
	 * @param x The cartesian x
	 * @param y The cartesian y
	 * @param z The cartesian z
	 *
	 * @return The Spherical coords
	 */
	public static SphericalCoords fromCartesian(double x, double y, double z) {
		double rho = Math.sqrt(x * x + y * y + z * z);
		double phi = Math.acos(z / rho);
		double theta = Math.atan2(y, x);

		return new SphericalCoords(rho, theta, phi);
	}

	/**
	 * @param rho   The rho
	 * @param theta The theta
	 * @param phi   The phi
	 *
	 * @return The cartesian coords
	 */
	public static double[] toCartesian(double rho, double theta, double phi) {
		double x = Math.cos(theta) * Math.sin(phi) * rho;
		double y = Math.sin(theta) * Math.sin(phi) * rho;
		double z = Math.cos(phi) * rho;

		return new double[]{x, y, z};
	}

	/**
	 * @param coords The Spherical coordinates
	 *
	 * @return The cartesian coords
	 */
	public static double[] toCartesian(SphericalCoords coords) {
		return toCartesian(coords.getRho(), coords.getTheta(), coords.getPhi());
	}

	/**
	 * @param rho      rho
	 * @param thetaDeg theta in degrees
	 * @param phiDeg   phi in degrees
	 *
	 * @return The cartesian coords
	 */
	public static double[] toCartesianDegree(double rho, double thetaDeg, double phiDeg) {
		double theta = Math.toRadians(thetaDeg);
		double phi = Math.toRadians(phiDeg);
		double x = Math.cos(theta) * Math.sin(phi) * rho;
		double y = Math.sin(theta) * Math.sin(phi) * rho;
		double z = Math.cos(phi) * rho;

		return new double[]{x, y, z};
	}

	/**
	 * @return The vector. Does account for the x-y swap.
	 */
	public Vector toBukkitVector() {
		double[] values = toCartesian(this);
		return new Vector(values[0], values[2], values[1]);
	}

	@Override
	public SphericalCoords clone() {
		try {
			return (SphericalCoords) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		SphericalCoords that = (SphericalCoords) o;
		return Double.compare(that.theta, theta) == 0 &&
				Double.compare(that.phi, phi) == 0 &&
				Double.compare(that.rho, rho) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(theta, phi, rho);
	}
}