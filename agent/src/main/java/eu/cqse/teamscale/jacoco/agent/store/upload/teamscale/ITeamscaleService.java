package eu.cqse.teamscale.jacoco.agent.store.upload.teamscale;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** {@link Retrofit} API specification for the {@link TeamscaleUploadStore}. */
public interface ITeamscaleService {

	/** Enum of report formats. */
	enum EReportFormat {
		JACOCO,
		TESTWISE_COVERAGE,
		JUNIT,
		TEST_LIST
	}

	/** Report upload API. */
	@Multipart
	@POST("p/{projectName}/external-report/")
	Call<ResponseBody> uploadExternalReport(
			@Path("projectName") String projectName,
			@Query("format") EReportFormat format,
			@Query("t") CommitDescriptor commit,
			@Query("adjusttimestamp") boolean adjustTimestamp,
			@Query("movetolastcommit") boolean moveToLastCommit,
			@Query("partition") String partition,
			@Query("message") String message,
			@Part("report") RequestBody report
	);

	/**
	 * Uploads the given report body to Teamscale
	 * with adjusttimestamp and movetolastcommit set to true.
	 */
	default Call<ResponseBody> uploadReport(
			String projectName,
			CommitDescriptor commit,
			String partition,
			EReportFormat reportFormat,
			String message,
			RequestBody report
	) {
		return uploadExternalReport(projectName, reportFormat, commit, true, true, partition, message, report);
	}
}