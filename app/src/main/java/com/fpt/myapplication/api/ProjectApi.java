package com.fpt.myapplication.api;



import com.fpt.myapplication.dto.ResponseSuccess;
import com.fpt.myapplication.dto.request.ProjectCreateRequest;
import com.fpt.myapplication.dto.request.UpdateProjectRequest;
import com.fpt.myapplication.dto.response.ProjectResponse;
import com.fpt.myapplication.dto.response.SearchResponse;
import com.fpt.myapplication.dto.response.UserResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ProjectApi {

    @POST("/project/create")
    Call<ResponseSuccess> createProject(@Body ProjectCreateRequest body);

    @GET("/project/my-projects")
    Call<ResponseSuccess<List<ProjectResponse>>> getMyProjects();

    @GET("/project/get/{id}")
    Call<ResponseSuccess<ProjectResponse>> getProjectById(@Path("id") int id);

    @GET("/project/member/{id}")
    Call<ResponseSuccess<List<UserResponse>>> getProjectsByMemberId(@Path("id") int id);

    @POST("/project/add-members/{projectId}")
    Call<ResponseSuccess> addMembersToProject(@Path("projectId") int projectId, @Body List<Integer> memberIds);


    @GET("/project/public-projects")
    Call<ResponseSuccess<List<ProjectResponse>>> getPublicProjects();

    @POST("/project/join-request")
    Call<ResponseSuccess> sendJoinRequest(@Query("projectId") int projectId);

    @GET("/project/get-join-requests/{projectId}")
    Call<ResponseSuccess<List<UserResponse>>> getJoinRequests(@Path("projectId") int projectId);

    @PATCH("/project/update/{id}")
    Call<ResponseSuccess> updateMemberRole(@Path("id") int projectId, @Query("userId") int userId, @Query("role") int role);

    @POST("/project/handle-join-request")
    Call<ResponseSuccess> handleJoinRequest(@Query("projectId") int projectId, @Query("userId") int userId, @Query("isApproved") boolean isApproved);

    @DELETE("/project/remove-member/{projectId}/{userId}")
    Call<ResponseSuccess> removeMemberFromProject(@Path("projectId") int projectId, @Path("userId") int userId);

    @GET("/project/search-global")
    Call<ResponseSuccess<SearchResponse>> searchGlobal(@Query("keyword") String keyword);

    @PUT("/project/update-project/{projectId}")
    Call<ResponseSuccess> updateProject(@Path("projectId") int projectId, @Body UpdateProjectRequest request);

    @DELETE("/project/delete-project/{projectId}")
    Call<ResponseSuccess> deleteProject(@Path("projectId") int projectId);
}
