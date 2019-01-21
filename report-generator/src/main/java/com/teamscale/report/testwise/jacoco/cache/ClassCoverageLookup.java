package com.teamscale.report.testwise.jacoco.cache;

import com.teamscale.report.testwise.model.builder.FileCoverageBuilder;
import com.teamscale.report.util.ILogger;
import org.conqat.lib.commons.string.StringUtils;
import org.jacoco.core.data.ExecutionData;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Holds information about a class' probes and to which line ranges they refer.
 * <p>
 * - Create an instance of this class for every analyzed java class.
 * - Set the file name of the java source file from which the class has been created.
 * - Then call {@link #addProbe(int, Set)} for all probes and lines that belong to that probe.
 * - Afterwards call {@link #getFileCoverage(ExecutionData, ILogger)} to transform probes ({@link ExecutionData}) for
 * this class into covered lines ({@link FileCoverageBuilder}).
 */
public class ClassCoverageLookup {

	/** Fully qualified name of the class (with / as separators). */
	private String className;

	/** Name of the java source file. */
	private String sourceFileName;

	/**
	 * Mapping from probe IDs to sets of covered lines. The index in this list corresponds to the probe ID.
	 */
	private final List<Set<Integer>> probes = new ArrayList<>();

	/**
	 * Constructor.
	 *
	 * @param className Classname as stored in the bytecode e.g. com/company/Example
	 */
	ClassCoverageLookup(String className) {
		this.className = className;
	}

	/** Sets the file name of the currently analyzed class (without path). */
	public void setSourceFileName(String sourceFileName) {
		this.sourceFileName = sourceFileName;
	}

	/** Adjusts the size of the probes list to the total probes count. */
	public void setTotalProbeCount(int count) {
		ensureArraySize(count - 1);
	}

	/** Adds the probe with the given id to the method. */
	public void addProbe(int probeId, Set<Integer> lines) {
		ensureArraySize(probeId);
		probes.set(probeId, lines);
	}

	/**
	 * Ensures that the probes list is big enough to allow access to the given index.
	 * Intermediate list entries are filled with null.
	 */
	private void ensureArraySize(int index) {
		while (index >= probes.size()) {
			probes.add(null);
		}
	}

	/**
	 * Generates {@link FileCoverageBuilder} from an {@link ExecutionData}.
	 * {@link ExecutionData} holds coverage of exactly one class (whereby inner classes are a separate class).
	 * This method returns a {@link FileCoverageBuilder} object which is later merged with the {@link FileCoverageBuilder} of other
	 * classes that reside in the same file.
	 */
	public FileCoverageBuilder getFileCoverage(ExecutionData executionData, ILogger logger) throws CoverageGenerationException {
		boolean[] executedProbes = executionData.getProbes();

		if (checkProbeInvariant(executedProbes)) {
			throw new CoverageGenerationException("Probe lookup does not match with actual probe size for " +
					sourceFileName + " " + className + " (" + probes.size() + " vs " + executedProbes.length + ")! " +
					"This is a bug in the profiler tooling. Please report it back to CQSE.");
		}
		if (sourceFileName == null) {
			logger.warn(
					"No source file name found for class " + className + "! This class was probably not compiled with " +
							"debug information enabled!");
			return null;
		}

		String packageName = StringUtils.removeLastPart(className, '/');
		final FileCoverageBuilder fileCoverage = new FileCoverageBuilder(packageName, sourceFileName);
		fillFileCoverage(fileCoverage, executedProbes, logger);

		return fileCoverage;
	}

	private void fillFileCoverage(FileCoverageBuilder fileCoverage, boolean[] executedProbes, ILogger logger) {
		for (int i = 0; i < probes.size(); i++) {
			Set<Integer> coveredLines = probes.get(i);
			if (!executedProbes[i]) {
				continue;
			}
			// coveredLines is null if the probe is outside of a method
			// Happens e.g. for methods generated by Lombok
			if (coveredLines == null) {
				logger.info(sourceFileName + " " + className + " did contain a covered probe " + i + "(of " +
						executedProbes.length + ") that could not be " +
						"matched to any method. This could be a bug in the profiler tooling. Please report it back " +
						"to CQSE.");
				continue;
			}
			if (coveredLines.isEmpty()) {
				logger.debug(
						sourceFileName + " " + className + " did contain a method with no line information. " +
								"Does the class contain debug information?");
				continue;
			}
			fileCoverage.addLines(coveredLines);
		}
	}

	/** Checks that the executed probes is not smaller than the cached probes. */
	private boolean checkProbeInvariant(boolean[] executedProbes) {
		return probes.size() > executedProbes.length;
	}
}
