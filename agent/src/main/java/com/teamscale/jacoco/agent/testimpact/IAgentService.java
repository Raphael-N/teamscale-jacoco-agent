package com.teamscale.jacoco.agent.testimpact;

import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Service to communicate with other agent instances. */
public interface IAgentService {

	/** Creates a {@link Retrofit} service instance for the given local port. */
	public static IAgentService create(int port) {
		OkHttpClient client = new OkHttpClient.Builder().connectTimeout(1, TimeUnit.SECONDS)
				.readTimeout(1, TimeUnit.SECONDS).writeTimeout(1, TimeUnit.SECONDS).build();
		HttpUrl url = new HttpUrl.Builder().scheme("http").host("localhost").port(port).build();
		return new Retrofit.Builder().baseUrl(url).client(client).build().create(IAgentService.class);
	}

	/** Shorthand for a callback that only acts on failures. */
	public static Callback<ResponseBody> failureCallback(Runnable errorHandler) {
		return new Callback<ResponseBody>() {
			@Override
			public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
				// Do nothing.
			}

			@Override
			public void onFailure(Call<ResponseBody> call, Throwable t) {
				errorHandler.run();
			}
		};
	}

	/** Get the current test ID. */
	@GET("test")
	abstract Call<ResponseBody> getTestId();

	/** Signal the start of the given test. */
	@POST("test/start/{testId}")
	abstract Call<ResponseBody> signalTestStart(@Path("testId") String testId);

	/** Signal the start of the given test. */
	@POST("test/end/{testId}")
	abstract Call<ResponseBody> signalTestEnd(@Path("testId") String testId);

	/** Register with a primary agent. */
	@POST("register")
	abstract Call<ResponseBody> register(@Query("port") int port);

	/** Unregister from a primary agent. */
	@DELETE("register")
	abstract Call<ResponseBody> unregister(@Query("port") int port);
}